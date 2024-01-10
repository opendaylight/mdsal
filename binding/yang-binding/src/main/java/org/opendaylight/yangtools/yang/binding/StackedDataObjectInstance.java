/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Iterables;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
record StackedDataObjectInstance<T extends DataObject>(
        ExactDataObjectStep<T> lastStep,
        @Nullable DataObjectInstance<?> prev) implements DataObjectInstance<T> {
    StackedDataObjectInstance {
        requireNonNull(lastStep);
    }

    @Override
    public Iterable<ExactDataObjectStep<?>> steps() {
        final var local = prev;
        return local == null ? List.of(lastStep) : Iterables.concat(local.steps(), List.of(lastStep));
    }
}
