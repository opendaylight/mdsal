/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.clustering;

import com.google.common.base.Optional;
import org.opendaylight.mdsal.binding.api.clustering.Entity;
import org.opendaylight.mdsal.binding.api.clustering.EntityOwnershipCandidateRegistration;
import org.opendaylight.mdsal.binding.api.clustering.EntityOwnershipChange;
import org.opendaylight.mdsal.binding.api.clustering.EntityOwnershipListener;
import org.opendaylight.mdsal.binding.api.clustering.EntityOwnershipListenerRegistration;
import org.opendaylight.mdsal.binding.api.clustering.EntityOwnershipService;
import org.opendaylight.mdsal.binding.dom.adapter.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.common.api.clustering.CandidateAlreadyRegisteredException;
import org.opendaylight.mdsal.common.api.clustering.EntityOwnershipState;
import org.opendaylight.mdsal.dom.api.clustering.DOMEntity;
import org.opendaylight.mdsal.dom.api.clustering.DOMEntityOwnershipCandidateRegistration;
import org.opendaylight.mdsal.dom.api.clustering.DOMEntityOwnershipChange;
import org.opendaylight.mdsal.dom.api.clustering.DOMEntityOwnershipListener;
import org.opendaylight.mdsal.dom.api.clustering.DOMEntityOwnershipListenerRegistration;
import org.opendaylight.mdsal.dom.api.clustering.DOMEntityOwnershipService;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter that bridges between the binding and DOM EntityOwnershipService interfaces.
 *
 * @author Thomas Pantelis
 */
public class BindingDOMEntityOwnershipServiceAdapter implements EntityOwnershipService {
    private static final Logger LOG = LoggerFactory.getLogger(BindingDOMEntityOwnershipServiceAdapter.class);

    private final DOMEntityOwnershipService domService;
    private final BindingToNormalizedNodeCodec conversionCodec;

    public BindingDOMEntityOwnershipServiceAdapter(DOMEntityOwnershipService domService,
            BindingToNormalizedNodeCodec conversionCodec) {
        this.domService = domService;
        this.conversionCodec = conversionCodec;
    }

    @Override
    public EntityOwnershipCandidateRegistration registerCandidate(Entity entity)
            throws CandidateAlreadyRegisteredException {
        return new BindingEntityOwnershipCandidateRegistration(domService.registerCandidate(toDOMEntity(entity)), entity);
    }

    @Override
    public EntityOwnershipListenerRegistration registerListener(String entityType, EntityOwnershipListener listener) {
        return new BindingEntityOwnershipListenerRegistration(entityType, listener, domService.
                registerListener(entityType, new DOMEntityOwnershipListenerAdapter(listener, conversionCodec)));
    }

    @Override
    public Optional<EntityOwnershipState> getOwnershipState(Entity forEntity) {
        return domService.getOwnershipState(toDOMEntity(forEntity));
    }

    @Override
    public boolean isCandidateRegistered(Entity forEntity) {
        return domService.isCandidateRegistered(toDOMEntity(forEntity));
    }

    private DOMEntity toDOMEntity(Entity entity) {
        return new DOMEntity(entity.getType(), conversionCodec.toNormalized(entity.getIdentifier()));
    }

    private static class BindingEntityOwnershipCandidateRegistration extends AbstractObjectRegistration<Entity>
               implements EntityOwnershipCandidateRegistration {
        private final DOMEntityOwnershipCandidateRegistration domRegistration;

        BindingEntityOwnershipCandidateRegistration(DOMEntityOwnershipCandidateRegistration domRegistration,
                Entity entity) {
            super(entity);
            this.domRegistration = domRegistration;
        }

        @Override
        protected void removeRegistration() {
            domRegistration.close();
        }
    }

    private static class BindingEntityOwnershipListenerRegistration extends AbstractObjectRegistration<EntityOwnershipListener>
            implements EntityOwnershipListenerRegistration {
        private final String entityType;
        private final DOMEntityOwnershipListenerRegistration domRegistration;

        BindingEntityOwnershipListenerRegistration(String entityType, EntityOwnershipListener listener,
                DOMEntityOwnershipListenerRegistration domRegistration) {
            super(listener);
            this.entityType = entityType;
            this.domRegistration = domRegistration;
        }

        @Override
        public String getEntityType() {
            return entityType;
        }

        @Override
        protected void removeRegistration() {
            domRegistration.close();
        }
    }

    private static class DOMEntityOwnershipListenerAdapter implements DOMEntityOwnershipListener {
        private final BindingToNormalizedNodeCodec conversionCodec;
        private final EntityOwnershipListener bindingListener;

        DOMEntityOwnershipListenerAdapter(EntityOwnershipListener bindingListener,
                BindingToNormalizedNodeCodec conversionCodec) {
            this.bindingListener = bindingListener;
            this.conversionCodec = conversionCodec;
        }

        @Override
        public void ownershipChanged(DOMEntityOwnershipChange ownershipChange) {
            try {
                Entity entity = new Entity(ownershipChange.getEntity().getType(), conversionCodec.toBinding(
                        ownershipChange.getEntity().getIdentifier()).get());
                bindingListener.ownershipChanged(new EntityOwnershipChange(entity, ownershipChange.getState()));
            } catch (Exception e) {
                LOG.error("Error converting DOM entity ID {} to binding InstanceIdentifier",
                        ownershipChange.getEntity().getIdentifier(), e);
            }
        }
    }
}
