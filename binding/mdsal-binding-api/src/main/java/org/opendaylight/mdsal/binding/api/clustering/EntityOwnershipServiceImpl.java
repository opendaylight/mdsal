/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.clustering;

import com.google.common.base.Optional;
import org.opendaylight.mdsal.common.api.clustering.CandidateAlreadyRegisteredException;
import org.opendaylight.mdsal.common.api.clustering.EntityOwnershipState;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * @author Thomas Pantelis
 */
public class EntityOwnershipServiceImpl implements EntityOwnershipService {

    class TestDataObject implements DataObject {

        @Override
        public Class<? extends DataContainer> getImplementedInterface() {
            // TODO Auto-generated method stub
            return null;
        }

    }

    class TestEntityOwnershipListener implements EntityOwnershipListener {

        @Override
        public void ownershipChanged(EntityOwnershipChange ownershipChange) {
            Entity entity = ownershipChange.getEntity();
            InstanceIdentifier<?> id = entity.getId();

            InstanceIdentifier<TestDataObject> iid = InstanceIdentifier.create(TestDataObject.class);
            Entity e = new Entity("", iid);
            // TODO Auto-generated method stub

        }
    }

    class TestEntityOwnershipCandidateRegistration implements EntityOwnershipCandidateRegistration {

        @Override
        public void close() {
            // TODO Auto-generated method stub

        }

        @Override
        public Entity getInstance() {
            // TODO Auto-generated method stub
            return null;
        }
    }

    class TestEntityOwnershipListenerRegistration implements EntityOwnershipListenerRegistration {

        @Override
        public String getEntityType() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void close() {
            // TODO Auto-generated method stub

        }

        @Override
        public EntityOwnershipListener getInstance() {
            // TODO Auto-generated method stub
            return null;
        }

    }

    @Override
    public EntityOwnershipCandidateRegistration registerCandidate(Entity entity)
            throws CandidateAlreadyRegisteredException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EntityOwnershipListenerRegistration registerListener(String entityType, EntityOwnershipListener listener) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<EntityOwnershipState> getOwnershipState(Entity forEntity) {
        // TODO Auto-generated method stub
        return null;
    }
}
