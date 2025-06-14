/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.Test;

class IetfYangUtilTest {
    private static final byte @NonNull [] BYTES = new byte[] { 1, 2, 30, 90, -5, -120 };
    private static final String CANON = "01:02:1e:5a:fb:88";

    @Test
    void testBytesToMac() {
        final var mac = IetfYangUtil.macAddressFor(BYTES);
        assertEquals(CANON, mac.getValue());
    }

    @Test
    void testMacToBytes() {
        assertArrayEquals(BYTES, IetfYangUtil.macAddressBytes(new MacAddress(CANON)));
        assertArrayEquals(BYTES, IetfYangUtil.macAddressBytes(new MacAddress("01:02:1E:5a:Fb:88")));
    }

    @Test
    void testBytesToHex() {
        final var hex = IetfYangUtil.hexStringFor(BYTES);
        assertEquals(CANON, hex.getValue());
    }

    @Test
    void testHexToBytes() {
        assertArrayEquals(BYTES, IetfYangUtil.hexStringBytes(new HexString(CANON)));
        assertArrayEquals(BYTES, IetfYangUtil.hexStringBytes(new HexString("01:02:1E:5a:Fb:88")));
    }

    @Test
    void testPhysToBytes() {
        assertArrayEquals(BYTES, IetfYangUtil.physAddressBytes(new PhysAddress(CANON)));
        assertArrayEquals(BYTES, IetfYangUtil.physAddressBytes(new PhysAddress("01:02:1E:5a:Fb:88")));

        assertArrayEquals(new byte[0], IetfYangUtil.physAddressBytes(new PhysAddress("")));
        assertArrayEquals(new byte[] { (byte) 0xaa }, IetfYangUtil.physAddressBytes(new PhysAddress("aa")));
        assertArrayEquals(new byte[] { (byte) 0xaa, (byte) 0xbb },
            IetfYangUtil.physAddressBytes(new PhysAddress("aa:bb")));
    }

    @Test
    void testQuadBits() {
        assertEquals(0x01020304, IetfYangUtil.dottedQuadBits(new DottedQuad("1.2.3.4")));
        assertEquals(0xFFFFFFFF, IetfYangUtil.dottedQuadBits(new DottedQuad("255.255.255.255")));
    }

    @Test
    void testQuadBytes() {
        assertArrayEquals(new byte[] { 1, 2, 3, 4 }, IetfYangUtil.dottedQuadBytes(new DottedQuad("1.2.3.4")));
    }

    @Test
    void testQuadForBits() {
        assertEquals("1.2.3.4", IetfYangUtil.dottedQuadFor(0x01020304).getValue());
        assertEquals("255.255.255.255", IetfYangUtil.dottedQuadFor(0xFFFFFFFF).getValue());
    }

    @Test
    void testQuadForBytes() {
        assertEquals("1.2.3.4", IetfYangUtil.dottedQuadFor(new byte[] { 1, 2, 3, 4 }).getValue());
    }

    @Test
    void testUuidFor() {
        final var uuid = UUID.fromString("f81d4fae-7dec-11d0-a765-00a0c91e6bf6");
        assertEquals("f81d4fae-7dec-11d0-a765-00a0c91e6bf6", IetfYangUtil.uuidFor(uuid).getValue());
    }

    @Test
    void canonizeMACTest() {
        assertEquals(CANON, IetfYangUtil.canonizeMacAddress(new MacAddress("01:02:1E:5A:FB:88")).getValue());
    }

    @Test
    void testDottedQuad() {
        assertArrayEquals(new byte[] { 1, 2, 3, 4 }, IetfYangUtil.dottedQuadBytes(new DottedQuad("1.2.3.4")));
        assertEquals(new DottedQuad("1.2.3.4"), IetfYangUtil.dottedQuadFor(new byte[] { 1, 2, 3, 4 }));
    }

    @Test
    void testHexString() {
        assertArrayEquals(new byte[] { 0, 1 }, IetfYangUtil.hexStringBytes(new HexString("00:01")));
        assertEquals(new HexString("00:01"), IetfYangUtil.hexStringFor(new byte[] { 0, 1 }));
    }

    @Test
    void testPhysAddress() {
        assertArrayEquals(new byte[] { 0, 1} , IetfYangUtil.physAddressBytes(new PhysAddress("00:01")));
        assertEquals(new PhysAddress("00:01"), IetfYangUtil.physAddressFor(new byte[] { 0, 1 }));
    }

    @Test
    void testMacAddress() {
        assertArrayEquals(new byte[] { 0, 1, 2, 3, 4, 5 },
            IetfYangUtil.macAddressBytes(new MacAddress("00:01:02:03:04:05")));
        assertEquals(new MacAddress("00:01:02:03:04:05"), IetfYangUtil.macAddressFor(new byte[] { 0, 1, 2, 3, 4, 5 }));
    }

    @Test
    void testUuid() {
        final var java = UUID.randomUUID();
        assertEquals(new Uuid(java.toString()), IetfYangUtil.uuidFor(java));
    }
}
