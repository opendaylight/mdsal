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
     * The following outlines valid combinations of the ownership status flags in the EntityOwnershipChange
     * parameter and their meanings:
     * <ul>
     * <li><b>wasOwner = false, isOwner = true, hasOwner = true</b> - this process has been granted ownership</li>
     * <li><b>wasOwner = true, isOwner = false, hasOwner = true</b> - this process was the owner but ownership
     *     transitioned to another process</li>
     * <li><b>wasOwner = false, isOwner = false, hasOwner = true</b> - ownership transitioned to another process
     *     and this process was not the previous owner</li>
     * <li><b>wasOwner = false, isOwner = false, hasOwner = false</b> - the entity no longer has any candidates and
     *     thus no owner and this process was not the previous owner</li>
     * <li><b>wasOwner = true, isOwner = false, hasOwner = false</b> - the entity no longer has any candidates and
     *     thus no owner and this process was the previous owner</li>
     * </ul>
     * @param ownershipChange contains the entity and its ownership status flags
     */
    void ownershipChanged(C ownershipChange);
}
