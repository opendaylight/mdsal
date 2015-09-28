/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api.clustering;

import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.concepts.Path;

/**
 * An interface that records a request to register a Candidate for a given Entity. Calling close on the
 * registration will remove the Candidate from any future ownership considerations for that Entity.
 *
 * @author Thomas Pantelis
 *
 * @param <T> the instance identifier type
 */
public interface GenericEntityOwnershipCandidateRegistration<T extends Path<T>>
        extends ObjectRegistration<GenericEntity<T>> {

   /**
    * Unregister the candidate
    */
   @Override
   void close();
}
