/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import org.opendaylight.yangtools.yang.common.Uint64;

final class Uint64RangeGenerator extends AbstractBigRangeGenerator<Uint64> {
    Uint64RangeGenerator() {
        super(Uint64.class);
    }

    @Override
    @Deprecated
    protected Uint64 convert(final Number value) {
        return Uint64.valueOf(value.toString());
    }

    @Override
    protected String format(final Uint64 value) {
        if (Uint64.MIN_VALUE.equals(value)) {
            return "org.opendaylight.yangtools.yang.common.Uint64.MIN_VALUE";
        }
        if (Uint64.MAX_VALUE.equals(value)) {
            return "org.opendaylight.yangtools.yang.common.Uint64.MAX_VALUE";
        }
        return "org.opendaylight.yangtools.yang.common.Uint64.fromLongBits(" + value.longValue() + ")";
    }
}
