/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.common.api.clustering;

import org.opendaylight.yangtools.concepts.Path;

/**
 * An interface for a class that listens for entity ownership changes.
 *
 * @author Thomas Pantelis
 *
 * @param <P> the instance identifier path type
 * @param <C> the GenericEntityOwnershipChange type
 */
public interface GenericEntityOwnershipListener<P extends Path<P>,
        C extends GenericEntityOwnershipChange<P, ? extends GenericEntity<P>>> {

    /**
     * A notification that is generated when the ownership status of an entity changes.
     *
     * @param ownershipChange contains the entity and its ownership change state
     */
    void ownershipChanged(C ownershipChange);
}
