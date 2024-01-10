/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
record ListDataObjectInstace<T extends DataObject>(ImmutableList<ExactDataObjectStep<?>> steps)
        implements DataObjectInstance<T> {
    ListDataObjectInstace {
        if (steps.isEmpty()) {
            throw new IllegalArgumentException("steps must not be empty");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public ExactDataObjectStep<T> lastStep() {
        return (ExactDataObjectStep<T>) steps.get(steps.size() - 1);
    }
}
