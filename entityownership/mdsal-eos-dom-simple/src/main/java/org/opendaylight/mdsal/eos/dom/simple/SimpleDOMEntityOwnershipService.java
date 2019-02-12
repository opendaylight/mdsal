/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.dom.simple;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.mdsal.eos.common.api.EntityOwnershipChangeState.LOCAL_OWNERSHIP_GRANTED;
import static org.opendaylight.mdsal.eos.common.api.EntityOwnershipChangeState.LOCAL_OWNERSHIP_LOST_NO_OWNER;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.concurrent.GuardedBy;
import org.kohsuke.MetaInfServices;
import org.opendaylight.mdsal.eos.common.api.CandidateAlreadyRegisteredException;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipChangeState;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipState;
import org.opendaylight.mdsal.eos.dom.api.DOMEntity;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipCandidateRegistration;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipChange;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipListener;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipListenerRegistration;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipService;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple {@link DOMEntityOwnershipService} operating as an isolated island. It has no awareness of the world outside
 * of itself.
 *
 * @author Robert Varga
 */
@MetaInfServices
public final class SimpleDOMEntityOwnershipService implements DOMEntityOwnershipService {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleDOMEntityOwnershipService.class);

    @GuardedBy("entities")
    private final Table<String, YangInstanceIdentifier, DOMEntity> entities = HashBasedTable.create();

    @GuardedBy("listeners")
    private final Multimap<String, DOMEntityOwnershipListener> listeners = ArrayListMultimap.create(0, 1);

    private final UUID uuid;

    @VisibleForTesting
    SimpleDOMEntityOwnershipService(final UUID uuid) {
        this.uuid = requireNonNull(uuid);
    }

    public SimpleDOMEntityOwnershipService() {
        this(UUID.randomUUID());
    }

    @Override
    public DOMEntityOwnershipCandidateRegistration registerCandidate(final DOMEntity entity)
            throws CandidateAlreadyRegisteredException {
        synchronized (entities) {
            final DOMEntity prev = entities.get(entity.getType(), entity.getIdentifier());
            if (prev != null) {
                throw new CandidateAlreadyRegisteredException(prev);
            }

            entities.put(entity.getType(), entity.getIdentifier(), entity);
            LOG.debug("{}: registered candidate {}", uuid, entity);
        }

        notifyListeners(entity, LOCAL_OWNERSHIP_GRANTED);
        return new EntityRegistration(entity);
    }

    @Override
    public DOMEntityOwnershipListenerRegistration registerListener(final String entityType,
            final DOMEntityOwnershipListener listener) {

        final Collection<DOMEntity> owned;
        synchronized (entities) {
            owned = ImmutableList.copyOf(entities.row(entityType).values());
            LOG.trace("{}: acquired candidates {} for new listener {}", uuid, owned, listener);
        }

        synchronized (listeners) {
            listeners.put(entityType, listener);
        }

        for (DOMEntity entity : owned) {
            notifyListener(listener, new DOMEntityOwnershipChange(entity, LOCAL_OWNERSHIP_GRANTED));
        }
        LOG.debug("{}: registered listener {}", uuid, listener);
        return new ListenerRegistration(entityType, listener);
    }

    @Override
    public Optional<EntityOwnershipState> getOwnershipState(final DOMEntity forEntity) {
        return isCandidateRegistered(forEntity) ? Optional.of(EntityOwnershipState.IS_OWNER) : Optional.empty();
    }

    @Override
    public boolean isCandidateRegistered(final DOMEntity forEntity) {
        synchronized (entities) {
            return entities.contains(forEntity.getType(), forEntity.getIdentifier());
        }
    }

    private void removeEntity(final DOMEntity entity) {
        synchronized (entities) {
            entities.remove(entity.getType(), entity.getIdentifier());
            LOG.debug("{}: unregistered candidate {}", uuid, entity);
        }

        notifyListeners(entity, LOCAL_OWNERSHIP_LOST_NO_OWNER);
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    private void notifyListener(final DOMEntityOwnershipListener listener, final DOMEntityOwnershipChange change) {
        try {
            LOG.trace("{} notifying listener {} change {}", uuid, listener, change);
            listener.ownershipChanged(change);
        } catch (RuntimeException e) {
            LOG.warn("{}: Listener {} change {} failed", uuid, listener, change, e);
        }
    }

    private void notifyListeners(final DOMEntity entity, final EntityOwnershipChangeState state) {
        final DOMEntityOwnershipChange change = new DOMEntityOwnershipChange(entity, state);

        final Collection<DOMEntityOwnershipListener> snap;

        synchronized (listeners) {
            snap = ImmutableList.copyOf(listeners.get(entity.getType()));
        }

        for (DOMEntityOwnershipListener listener : snap) {
            notifyListener(listener, change);
        }
    }

    void unregisterListener(final ListenerRegistration reg) {
        synchronized (listeners) {
            listeners.remove(reg.getEntityType(), reg.getInstance());
            LOG.debug("{}: unregistered listener {}", uuid, reg.getInstance());
        }
    }

    @Override
    public String toString() {
        final ToStringHelper h = MoreObjects.toStringHelper(SimpleDOMEntityOwnershipService.class).add("uuid", uuid);

        synchronized (entities) {
            h.add("entities", entities);
        }
        synchronized (listeners) {
            h.add("listeners", listeners);
        }

        return h.toString();
    }

    private final class EntityRegistration extends AbstractObjectRegistration<DOMEntity> implements
            DOMEntityOwnershipCandidateRegistration {
        EntityRegistration(final DOMEntity entity) {
            super(entity);
        }

        @Override
        protected void removeRegistration() {
            removeEntity(getInstance());
        }
    }

    private final class ListenerRegistration extends AbstractObjectRegistration<DOMEntityOwnershipListener>
            implements DOMEntityOwnershipListenerRegistration {
        private final String entityType;

        ListenerRegistration(final String entityType, final DOMEntityOwnershipListener listener) {
            super(listener);
            this.entityType = requireNonNull(entityType);
        }

        @Override
        public String getEntityType() {
            return entityType;
        }

        @Override
        protected void removeRegistration() {
            unregisterListener(this);
        }
    }
}
