/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.java.api.generator.range_generators;

import javax.annotation.Nonnull;

abstract class AbstractUnsignedIntegerRangeGenerator<T extends Number & Comparable<T>>
        extends AbstractPrimitiveRangeGenerator<T> {
    protected AbstractUnsignedIntegerRangeGenerator(final Class<T> typeClass, final String primitiveName,
            final T minValue, final T maxValue) {
        super(typeClass, primitiveName, minValue, maxValue);
    }

    @Override
    protected final String format(final T value) {
        // Make sure the number constant is cast to the corresponding primitive type
        return getPrimitiveName() + ".valueOf(" + value + ")";
    }

    @Nonnull
    @Override
    protected String gtExpression(T value) {
        return "value.compareTo(" + format(value) + ") >= 0";
    }

    @Nonnull
    @Override
    protected String ltExpression(T value) {
        return "value.compareTo(" + format(value) + ") <= 0";
    }
}