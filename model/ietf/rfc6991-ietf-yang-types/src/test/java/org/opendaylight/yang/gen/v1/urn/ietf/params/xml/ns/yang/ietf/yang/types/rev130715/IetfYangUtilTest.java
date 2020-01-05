/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.IetfYangUtil.INSTANCE;

import org.junit.Test;

public class IetfYangUtilTest {
    @Test
    public void testDottedQuad() {
        assertArrayEquals(new byte[] { 1, 2, 3, 4 }, INSTANCE.dottedQuadBytes(new DottedQuad("1.2.3.4")));
        assertEquals(new DottedQuad("1.2.3.4"), INSTANCE.dottedQuadFor(new byte[] { 1, 2, 3, 4 }));
    }

    @Test
    public void testHexString() {
        assertArrayEquals(new byte[] { 0, 1 }, INSTANCE.hexStringBytes(new HexString("00:01")));
        assertEquals(new HexString("00:01"), INSTANCE.hexStringFor(new byte[] { 0, 1 }));
    }

    @Test
    public void testPhysAddress() {
        assertArrayEquals(new byte[] { 0, 1} , INSTANCE.physAddressBytes(new PhysAddress("00:01")));
        assertEquals(new PhysAddress("00:01"), INSTANCE.physAddressFor(new byte[] { 0, 1 }));
    }

    @Test
    public void testMacAddress() {
        assertArrayEquals(new byte[] { 0, 1, 2, 3, 4, 5 },
            INSTANCE.macAddressBytes(new MacAddress("00:01:02:03:04:05")));
        assertEquals(new MacAddress("00:01:02:03:04:05"), INSTANCE.macAddressFor(new byte[] { 0, 1, 2, 3, 4, 5 }));
    }
}
