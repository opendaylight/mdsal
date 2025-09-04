/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.ExactDataObjectStep;

/**
 * A change to a generated {@link DataObject}.
 *
 * @param <T> the {@link DataObject} type
 */
public sealed interface DataObjectChange<T extends DataObject>
        permits DataObjectDeleted, DataObjectChange.WithDataAfter {
    /**
     * A {@link DataObjectChange} after which there is the instance value available.
     */
    sealed interface WithDataAfter<T extends DataObject> extends DataObjectChange<T>
            permits DataObjectModified, DataObjectWritten {
        /**
         * {@return the after-image}
         */
        @NonNull T dataAfter();
    }

    /**
     * {@return the {@link ExactDataObjectStep} step this change corresponds to}
     */
    @NonNull ExactDataObjectStep<T> steo();
}