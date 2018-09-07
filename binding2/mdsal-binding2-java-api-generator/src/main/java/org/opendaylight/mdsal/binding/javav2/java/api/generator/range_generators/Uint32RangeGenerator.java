/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.java.api.generator.range_generators;

import org.opendaylight.yangtools.yang.common.Uint32;

final class Uint32RangeGenerator extends AbstractUnsignedIntegerRangeGenerator<Uint32> {
    Uint32RangeGenerator() {
        super(Uint32.class, Uint32.class.getName(), Uint32.valueOf(0),
            Uint32.valueOf(0xffffffffL));
    }

    @Override
    protected Uint32 convert(final Number value) {
        return Uint32.valueOf(value.longValue());
    }
}