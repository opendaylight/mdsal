/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A leading part of {@code instance-identifier} referencing a {@link DataObject}. The target {@link DataObject} is
 * available through {@link #lastStep()}.
 *
 * @param <T> DataObject type
 */
@NonNullByDefault
public sealed interface DataObjectInstance<T extends DataObject>
        permits ListDataObjectInstace, StackedDataObjectInstance {
    /**
     * Return the {@link ExactDataObjectStep}s which make up this {@link DataObjectInstance}. Guaranteed to be
     * non-empty.
     *
     * @return steps which make up this reference
     */
    Iterable<ExactDataObjectStep<?>> steps();

    /**
     * Return the last {@link ExactDataObjectStep}.
     *
     * @return the last step
     */
    ExactDataObjectStep<T> lastStep();
}
