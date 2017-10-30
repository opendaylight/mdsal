/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.java.api.generator.range_generators;

import org.opendaylight.yangtools.yang.common.Uint8;

final class Uint8RangeGenerator extends AbstractUnsignedIntegerRangeGenerator<Uint8> {
    Uint8RangeGenerator() {
        super(Uint8.class, Uint8.class.getName(), Uint8.valueOf(0), Uint8.valueOf(255));
    }

    @Override
    protected Uint8 convert(final Number value) {
        return Uint8.valueOf(value.shortValue());
    }
}