/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

// FIXME: this generator is less than optimal, we should be able to do better by specializing it via
//        AbstractPrimitiveRangeGenerator, extracting a long on which to operate
final class Uint32RangeGenerator extends AbstractBigRangeGenerator<Uint32> {
    Uint32RangeGenerator() {
        super(Uint32.class);
    }

    @Override
    @Deprecated
    protected Uint32 convert(final Number value) {
        if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long
                || value instanceof Uint8 || value instanceof Uint16) {
            return Uint32.valueOf(value.longValue());
        }
        return Uint32.valueOf(value.toString());
    }

    @Override
    protected String format(final Uint32 value) {
        return "org.opendaylight.yangtools.yang.common.Uint32.valueOf(" + value.longValue() + "L)";
    }
}
