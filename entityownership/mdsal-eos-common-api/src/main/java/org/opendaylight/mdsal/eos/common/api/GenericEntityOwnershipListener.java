/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.common.api;

import org.eclipse.jdt.annotation.NonNull;

/**
 * An interface for a class that listens for entity ownership changes.
 *
 * @param <E> the {@link GenericEntity} type
 * @author Thomas Pantelis
 */
public interface GenericEntityOwnershipListener<E extends GenericEntity<?>> {
    /**
     * A notification that is generated when the ownership status of an entity changes.
     *
     * @param ownershipChange contains the entity and its ownership change state
     */
    void ownershipChanged(@NonNull EntityOwnershipChange<E> ownershipChange);
}
