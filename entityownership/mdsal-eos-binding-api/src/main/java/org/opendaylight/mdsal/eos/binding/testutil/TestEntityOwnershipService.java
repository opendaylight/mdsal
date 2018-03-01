/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.binding.testutil;

import static org.opendaylight.yangtools.testutils.mockito.MoreAnswers.realOrException;

import com.google.common.base.Optional;

import org.mockito.Mockito;
import org.opendaylight.mdsal.eos.binding.api.Entity;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipCandidateRegistration;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipListener;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipListenerRegistration;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipService;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipState;

/**
 * Fake EntityOwnershipService suitable for non-clustered component tests.
 *
 */
public abstract class TestEntityOwnershipService implements EntityOwnershipService {

    private static final EntityOwnershipState STATE = EntityOwnershipState.from(true, true);
    private EntityOwnershipCandidateRegistration entityOwnershipCandidateRegistration;

    public static EntityOwnershipService newInstance() {
        return Mockito.mock(TestEntityOwnershipService.class, realOrException());
    }

    @Override
    public TestEntityOwnershipCandidateRegistration registerCandidate(Entity entity) {
        return Mockito.mock(TestEntityOwnershipCandidateRegistration.class, realOrException());
    }

    @Override
    public EntityOwnershipListenerRegistration registerListener(String entityType, EntityOwnershipListener listener) {
        return Mockito.mock(EntityOwnershipListenerRegistration.class, realOrException());
    }

    @Override
    public Optional<EntityOwnershipState> getOwnershipState(Entity forEntity) {
        return Optional.of(STATE);
    }

    @Override
    public boolean isCandidateRegistered(Entity entity) {
        return true;
    }

}
