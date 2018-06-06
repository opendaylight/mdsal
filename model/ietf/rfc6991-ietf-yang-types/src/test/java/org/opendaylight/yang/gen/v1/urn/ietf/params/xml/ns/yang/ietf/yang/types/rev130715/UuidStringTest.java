/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.UuidStringSupport;

@Deprecated
public class UuidStringTest {
    @Test
    public void testValueOfInt() {
        checkString("f81d4fae-7dec-11d0-a765-00a0c91e6bf6");
    }

    private static void checkString(final String input) {
        assertEquals(input, UuidStringSupport.getInstance().fromString(input).getFirst().toString());
        assertEquals(input, UuidStringSupport.getInstance().fromString(input.toUpperCase()).getFirst().toString());
    }
}
