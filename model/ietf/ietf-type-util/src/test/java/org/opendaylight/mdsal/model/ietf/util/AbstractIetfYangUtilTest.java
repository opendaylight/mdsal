/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.model.ietf.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.UUID;
import org.junit.Test;

public class AbstractIetfYangUtilTest {
    private static final MacUtil UTIL = new MacUtil();
    private static final byte[] BYTES = new byte[] { 1, 2, 30, 90, -5, -120 };
    private static final String CANON = "01:02:1e:5a:fb:88";

    @Test
    public void testBytesToMac() throws Exception {
        final MacClass mac = UTIL.macAddressFor(BYTES);
        assertEquals(CANON, mac.getValue());
    }

    @Test
    public void testMacToBytes() throws Exception {
        final byte[] bytes1 = UTIL.macAddressBytes(new MacClass(CANON));
        assertArrayEquals(BYTES, bytes1);

        final byte[] bytes2 = UTIL.macAddressBytes(new MacClass("01:02:1E:5a:Fb:88"));
        assertArrayEquals(BYTES, bytes2);
    }

    @Test
    public void testBytesToHex() {
        final HexClass hex = UTIL.hexStringFor(BYTES);
        assertEquals(CANON, hex.getValue());
    }

    @Test
    public void testHexToBytes() {
        final byte[] bytes1 = UTIL.hexStringBytes(new HexClass(CANON));
        assertArrayEquals(BYTES, bytes1);

        final byte[] bytes2 = UTIL.hexStringBytes(new HexClass("01:02:1E:5a:Fb:88"));
        assertArrayEquals(BYTES, bytes2);
    }

    @Test
    public void testPhysToBytes() {
        final byte[] bytes1 = UTIL.physAddressBytes(new PhysClass(CANON));
        assertArrayEquals(BYTES, bytes1);

        final byte[] bytes2 = UTIL.physAddressBytes(new PhysClass("01:02:1E:5a:Fb:88"));
        assertArrayEquals(BYTES, bytes2);

        assertArrayEquals(new byte[0], UTIL.physAddressBytes(new PhysClass("")));
        assertArrayEquals(new byte[] { (byte) 0xaa }, UTIL.physAddressBytes(new PhysClass("aa")));
        assertArrayEquals(new byte[] { (byte) 0xaa, (byte) 0xbb }, UTIL.physAddressBytes(new PhysClass("aa:bb")));
    }

    @Test
    public void testQuadBytes() {
        assertArrayEquals(new byte[] { 1, 2, 3, 4 }, UTIL.dottedQuadBytes(new QuadClass("1.2.3.4")));
    }

    @Test
    public void testQuadFor() {
        assertEquals("1.2.3.4", UTIL.dottedQuadFor(new byte[] { 1, 2, 3, 4 }).getValue());
    }

    @Test
    public void testUuidFor() {
        final UUID uuid = UUID.fromString("f81d4fae-7dec-11d0-a765-00a0c91e6bf6");
        assertEquals("f81d4fae-7dec-11d0-a765-00a0c91e6bf6", UTIL.uuidFor(uuid).getValue());
    }

    @Test
    public void canonizeMACTest() throws Exception {
        assertEquals(CANON, UTIL.canonizeMacAddress(new MacClass("01:02:1E:5A:FB:88")).getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void hexValueWithExceptionTest() throws Exception {
        AbstractIetfYangUtil.hexValue(Character.highSurrogate(1000));
        fail("Expected invalid character exception");
    }
}
