/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api.clustering;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.concepts.Path;

/**
 * An interface that records a request to register a ownership status change listener for a given Entity.
 * Calling close on the registration will unregister listeners and future ownership changes will not
 * be delivered to the listener.
 *
 * @author Thomas Pantelis
 *
 * @param <P> the instance identifier path type
 */
public interface GenericEntityOwnershipListenerRegistration<P extends Path<P>,
        L extends GenericEntityOwnershipListener<P, ? extends GenericEntityOwnershipChange<P, ? extends GenericEntity<P>>>>
            extends ObjectRegistration<L> {

    /**
     * @return the entity type that the listener was registered for
     */
    @Nonnull String getEntityType();

    /**
     * Unregister the listener
     */
    @Override
    void close();
}
