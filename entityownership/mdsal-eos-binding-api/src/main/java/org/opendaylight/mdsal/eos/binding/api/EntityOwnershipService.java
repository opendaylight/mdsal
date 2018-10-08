/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.binding.api;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.opendaylight.mdsal.eos.common.api.CandidateAlreadyRegisteredException;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipState;
import org.opendaylight.mdsal.eos.common.api.GenericEntityOwnershipService;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Binding version of {@link GenericEntityOwnershipService}.
 *
 * @author Thomas Pantelis
 */
@Beta
public interface EntityOwnershipService extends
        GenericEntityOwnershipService<InstanceIdentifier<?>, Entity, EntityOwnershipListener> {

    @Override
    EntityOwnershipCandidateRegistration registerCandidate(Entity entity)
            throws CandidateAlreadyRegisteredException;

    @Override
    EntityOwnershipListenerRegistration registerListener(String entityType,
            EntityOwnershipListener listener);

    @Override
    Optional<EntityOwnershipState> getOwnershipState(Entity forEntity);

    @Override
    boolean isCandidateRegistered(Entity forEntity);
}
