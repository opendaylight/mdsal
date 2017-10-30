/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.java.api.generator.range_generators;

final class ShortRangeGenerator extends AbstractSubIntegerRangeGenerator<Short> {
    ShortRangeGenerator() {
        super(Short.class, short.class.getName(), Short.MIN_VALUE, Short.MAX_VALUE);
    }

    @Override
    protected Short convert(final Number value) {
        return value.shortValue();
    }
}