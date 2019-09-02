/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import org.opendaylight.yangtools.yang.common.Uint8;

// FIXME: this generator is less than optimal, we should be able to do better by specializing it via
//        AbstractPrimitiveRangeGenerator, extracting a short on which to operate
final class Uint8RangeGenerator extends AbstractBigRangeGenerator<Uint8> {
    Uint8RangeGenerator() {
        super(Uint8.class);
    }

    @Override
    @Deprecated
    protected Uint8 convert(final Number value) {
        if (value instanceof Byte) {
            return Uint8.valueOf(value.byteValue());
        }
        return Uint8.valueOf(value.toString());
    }

    @Override
    protected String format(final Uint8 value) {
        return "org.opendaylight.yangtools.yang.common.Uint8.valueOf(" + value.intValue() + ")";
    }
}
