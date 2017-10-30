/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.java.api.generator.range_generators;

import org.opendaylight.yangtools.yang.common.Uint16;

final class Uint16RangeGenerator extends AbstractUnsignedIntegerRangeGenerator<Uint16> {
    Uint16RangeGenerator() {
        super(Uint16.class, Uint16.class.getName(), Uint16.valueOf(0), Uint16.valueOf(65535));
    }

    @Override
    protected Uint16 convert(final Number value) {
        return Uint16.valueOf(value.intValue());
    }
}