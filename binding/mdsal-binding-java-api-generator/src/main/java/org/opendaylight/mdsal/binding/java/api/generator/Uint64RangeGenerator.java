/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import org.opendaylight.yangtools.yang.common.Uint64;

final class Uint64RangeGenerator extends AbstractBoundedRangeGenerator<Uint64> {
    Uint64RangeGenerator() {
        super(Uint64.class, Uint64.valueOf(0), Uint64.valueOf("18446744073709551615"));
    }

    @Override
    @Deprecated
    protected Uint64 convert(final Number value) {
        return Uint64.valueOf(value.toString());
    }
}
