/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;

abstract class AbstractBoundedRangeGenerator<T extends Number & Comparable<T>> extends AbstractRangeGenerator<T> {
    private final @NonNull T minValue;
    private final @NonNull T maxValue;

    AbstractBoundedRangeGenerator(final Class<T> typeClass, final T minValue, final T maxValue) {
        super(typeClass);
        this.minValue = requireNonNull(minValue);
        this.maxValue = requireNonNull(maxValue);
    }

    protected final boolean needsMaximumEnforcement(final T maxToEnforce) {
        return maxValue.compareTo(maxToEnforce) > 0;
    }

    protected final boolean needsMinimumEnforcement(final T minToEnforce) {
        return minValue.compareTo(minToEnforce) < 0;
    }
}
