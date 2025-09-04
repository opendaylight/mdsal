/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import org.opendaylight.mdsal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.yangtools.binding.DataObject;

/**
 * A {@link DataObjectModification} indicating that the internals of a {@link DataObject} have changed.
 */
public non-sealed interface DataObjectModified<T extends DataObject> extends DataObjectModification.WithDataAfter<T> {
    @Override
    @Deprecated(since = "15.0.0", forRemoval = true)
    default ModificationType modificationType() {
        return ModificationType.SUBTREE_MODIFIED;
    }
}
