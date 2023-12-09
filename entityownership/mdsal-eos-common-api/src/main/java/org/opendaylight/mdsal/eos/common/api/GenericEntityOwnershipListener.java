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
     * @param entity the entity whose ownership status changed
     * @param change the change the entity underwent
     * @param inJeopardy {@code true} if ownership is in jeopardy and the reported change may be inaccurate
     */
    void ownershipChanged(@NonNull E entity, @NonNull EntityOwnershipStateChange change, boolean inJeopardy);
}
