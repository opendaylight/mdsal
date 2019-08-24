/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import org.opendaylight.yangtools.yang.common.Uint16;

final class Uint16RangeGenerator extends AbstractSubIntegerRangeGenerator<Uint16> {
    Uint16RangeGenerator() {
        // FIXME: primitive type does not work here
        super(Uint16.class, int.class.getName(), Uint16.valueOf(0), Uint16.valueOf(65535));
    }

    @Override
    @Deprecated
    protected Uint16 convert(final Number value) {
        return Uint16.valueOf(value.intValue());
    }
}
