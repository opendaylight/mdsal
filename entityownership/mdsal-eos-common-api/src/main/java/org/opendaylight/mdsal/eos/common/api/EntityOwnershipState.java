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
 * Enumerates the current ownership state for an entity.
 *
 * @author Thomas Pantelis
 */
public enum EntityOwnershipState {
    /**
     * The local process instance is the owner of the entity.
     */
    IS_OWNER,

    /**
     * A remote process instance is the owner of the entity.
     */
    OWNED_BY_OTHER,

    /**
     * The entity has no owner and thus no candidates.
     */
    NO_OWNER;

    public static @NonNull EntityOwnershipState from(final boolean isOwner, final boolean hasOwner) {
        if (isOwner) {
            return IS_OWNER;
        }
        return hasOwner ? OWNED_BY_OTHER : NO_OWNER;
    }
}
