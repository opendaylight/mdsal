/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.dom.simple;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.mdsal.eos.common.api.EntityOwnershipStateChange.LOCAL_OWNERSHIP_GRANTED;
import static org.opendaylight.mdsal.eos.common.api.EntityOwnershipStateChange.LOCAL_OWNERSHIP_LOST_NO_OWNER;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.kohsuke.MetaInfServices;
import org.opendaylight.mdsal.eos.common.api.CandidateAlreadyRegisteredException;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipChange;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipState;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipStateChange;
import org.opendaylight.mdsal.eos.dom.api.DOMEntity;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipListener;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipService;
import org.opendaylight.mdsal.eos.dom.simple.di.LocalDOMEntityOwnershipService;
import org.opendaylight.yangtools.concepts.AbstractRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@MetaInfServices
public sealed class SimpleDOMEntityOwnershipService implements DOMEntityOwnershipService
        permits LocalDOMEntityOwnershipService {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleDOMEntityOwnershipService.class);

    @GuardedBy("entities")
    private final Table<String, YangInstanceIdentifier, DOMEntity> entities = HashBasedTable.create();

    @GuardedBy("listeners")
    private final Multimap<String, DOMEntityOwnershipListener> listeners = ArrayListMultimap.create(0, 1);

    private final UUID uuid;

    @Activate
    public SimpleDOMEntityOwnershipService() {
        this(UUID.randomUUID());
    }

    @VisibleForTesting
    SimpleDOMEntityOwnershipService(final UUID uuid) {
        this.uuid = requireNonNull(uuid);
    }

    @Override
    public Registration registerCandidate(final DOMEntity entity) throws CandidateAlreadyRegisteredException {
        synchronized (entities) {
            final var prev = entities.get(entity.getType(), entity.getIdentifier());
            if (prev != null) {
                throw new CandidateAlreadyRegisteredException(prev);
            }

            entities.put(entity.getType(), entity.getIdentifier(), entity);
            LOG.debug("{}: registered candidate {}", uuid, entity);
        }

        notifyListeners(entity, LOCAL_OWNERSHIP_GRANTED);
        return new AbstractRegistration() {
            @Override
            protected void removeRegistration() {
                removeEntity(entity);
            }
        };
    }

    @Override
    public Registration registerListener(final String entityType, final DOMEntityOwnershipListener listener) {
        final Collection<DOMEntity> owned;
        synchronized (entities) {
            owned = ImmutableList.copyOf(entities.row(entityType).values());
            LOG.trace("{}: acquired candidates {} for new listener {}", uuid, owned, listener);
        }

        synchronized (listeners) {
            listeners.put(entityType, listener);
        }

        for (var entity : owned) {
            notifyListener(listener, new EntityOwnershipChange<>(entity, LOCAL_OWNERSHIP_GRANTED));
        }
        LOG.debug("{}: registered listener {}", uuid, listener);
        return new AbstractRegistration() {
            @Override
            protected void removeRegistration() {
                synchronized (listeners) {
                    listeners.remove(entityType, listener);
                    LOG.debug("{}: unregistered listener {}", uuid, listener);
                }
            }
        };
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
    private void notifyListener(final DOMEntityOwnershipListener listener,
            final EntityOwnershipChange<DOMEntity> change) {
        try {
            LOG.trace("{} notifying listener {} change {}", uuid, listener, change);
            listener.ownershipChanged(change);
        } catch (RuntimeException e) {
            LOG.warn("{}: Listener {} change {} failed", uuid, listener, change, e);
        }
    }

    private void notifyListeners(final DOMEntity entity, final EntityOwnershipStateChange state) {

        final Collection<DOMEntityOwnershipListener> snap;

        synchronized (listeners) {
            snap = ImmutableList.copyOf(listeners.get(entity.getType()));
        }

        final var change = new EntityOwnershipChange<>(entity, state);
        for (var listener : snap) {
            notifyListener(listener, change);
        }
    }

    @Override
    public String toString() {
        final var helper = MoreObjects.toStringHelper(SimpleDOMEntityOwnershipService.class).add("uuid", uuid);
        synchronized (entities) {
            helper.add("entities", entities);
        }
        synchronized (listeners) {
            helper.add("listeners", listeners);
        }
        return helper.toString();
    }
}
