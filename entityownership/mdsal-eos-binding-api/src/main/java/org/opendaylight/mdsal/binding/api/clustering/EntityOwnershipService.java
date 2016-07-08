/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.clustering;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.common.api.clustering.CandidateAlreadyRegisteredException;
import org.opendaylight.mdsal.common.api.clustering.EntityOwnershipState;
import org.opendaylight.mdsal.common.api.clustering.GenericEntityOwnershipService;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Binding version of {@link GenericEntityOwnershipService}.
 *
 * @author Thomas Pantelis
 */
@Beta
public interface EntityOwnershipService extends
        GenericEntityOwnershipService<InstanceIdentifier<?>, Entity, EntityOwnershipListener> {

    /**
     * {@inheritDoc}
     */
    @Override
    EntityOwnershipCandidateRegistration registerCandidate(@Nonnull Entity entity)
            throws CandidateAlreadyRegisteredException;

    /**
     * {@inheritDoc}
     */
    @Override
    EntityOwnershipListenerRegistration registerListener(@Nonnull String entityType,
            @Nonnull EntityOwnershipListener listener);

    /**
     * {@inheritDoc}
     */
    @Override
    Optional<EntityOwnershipState> getOwnershipState(@Nonnull Entity forEntity);

    /**
     * {@inheritDoc}
     */
    @Override
    boolean isCandidateRegistered(@Nonnull Entity forEntity);
}
