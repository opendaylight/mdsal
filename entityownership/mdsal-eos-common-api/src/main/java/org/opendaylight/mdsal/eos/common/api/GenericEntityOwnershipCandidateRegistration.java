/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.common.api;

import com.google.common.util.concurrent.ListenableFuture;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.concepts.Path;
import java.util.concurrent.Future;

/**
 * An interface that records a request to register a Candidate for a given Entity. Calling close on the
 * registration will remove the Candidate from any future ownership considerations for that Entity.
 *
 * @author Thomas Pantelis
 *
 * @param <P> the instance identifier type
 * @param <E> the GenericEntity type
 */
public interface GenericEntityOwnershipCandidateRegistration<P extends Path<P>, E extends GenericEntity<P>>
        extends ObjectRegistration<E> {

    /**
     * Unregister the candidate.
     */
    @Override
    void close();

    @Nullable
    default ListenableFuture<Boolean> getRegistrationResult(){
        return null;
    }
}
