/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import org.opendaylight.yangtools.binding.DataObject;

/**
 * A {@link DataObjectModification} indicating that a {@link DataObject} disappeared.
 */
public non-sealed interface DataObjectDeleted<T extends DataObject> extends DataObjectModification<T> {
    @Override
    @Deprecated(since = "15.0.0", forRemoval = true)
    default ModificationType modificationType() {
        return ModificationType.DELETE;
    }

    @Override
    @Deprecated(since = "15.0.0", forRemoval = true)
    default T dataAfter() {
        return null;
    }
}
