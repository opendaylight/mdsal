/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.dom.api;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.opendaylight.mdsal.eos.common.api.CandidateAlreadyRegisteredException;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipState;
import org.opendaylight.mdsal.eos.common.api.GenericEntityOwnershipService;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * DOM version of {@link GenericEntityOwnershipService}.
 *
 * @author Thomas Pantelis
 */
@Beta
public interface DOMEntityOwnershipService extends
        GenericEntityOwnershipService<YangInstanceIdentifier, DOMEntity, DOMEntityOwnershipListener> {
    @Override
    DOMEntityOwnershipCandidateRegistration registerCandidate(DOMEntity entity)
            throws CandidateAlreadyRegisteredException;

    @Override
    DOMEntityOwnershipListenerRegistration registerListener(String entityType,
            DOMEntityOwnershipListener listener);

    @Override
    Optional<EntityOwnershipState> getOwnershipState(DOMEntity forEntity);

    @Override
    boolean isCandidateRegistered(DOMEntity forEntity);
}
