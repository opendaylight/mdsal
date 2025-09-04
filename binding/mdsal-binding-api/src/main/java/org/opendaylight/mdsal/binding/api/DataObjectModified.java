/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataObjectModification.WithDataAfter;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.ExactDataObjectStep;

/**
 * A {@link DataObjectModification} indicating that the internals of a {@link DataObject} have changed.
 */
public abstract non-sealed class DataObjectModified<T extends DataObject> extends WithDataAfter<T> {
    @SuppressWarnings("deprecation")
    protected DataObjectModified(final @NonNull ExactDataObjectStep<T> step) {
        super(ModificationType.SUBTREE_MODIFIED, step);
    }
}