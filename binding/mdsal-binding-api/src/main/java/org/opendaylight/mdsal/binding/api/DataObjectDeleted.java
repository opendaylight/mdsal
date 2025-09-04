/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.DataObject;

/**
 * A {@link DataObjectModification} indicating that a {@link DataObject} disappeared.
 *
 * @param <T> the {@link DataObject} type
 * @since 15.0.0
 */
public abstract non-sealed class DataObjectDeleted<T extends DataObject> extends DataObjectModification<T> {
    @SuppressWarnings("deprecation")
    protected DataObjectDeleted() {
        super(ModificationType.DELETE);
    }

    @Override
    @Deprecated(since = "15.0.0", forRemoval = true)
    public final @Nullable T dataAfter() {
        return null;
    }
}