/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.java.api.generator.range_generators;

import com.google.common.primitives.UnsignedLong;
import java.math.BigInteger;
import org.opendaylight.yangtools.yang.common.Uint64;

final class Uint64RangeGenerator extends AbstractUnsignedIntegerRangeGenerator<Uint64> {
    Uint64RangeGenerator() {
        super(Uint64.class, Uint64.class.getName(), Uint64.valueOf(0), Uint64.fromUnsignedLong(
            UnsignedLong.fromLongBits(0xffffffffffffffffL)));
    }

    @Override
    protected Uint64 convert(final Number value) {
        return Uint64.valueOf(BigInteger.class.cast(value));
    }
}