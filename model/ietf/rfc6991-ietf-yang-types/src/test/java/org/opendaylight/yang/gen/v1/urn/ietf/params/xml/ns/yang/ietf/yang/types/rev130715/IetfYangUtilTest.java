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

import java.util.UUID;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.Test;

public class IetfYangUtilTest {
    private static final byte @NonNull [] BYTES = new byte[] { 1, 2, 30, 90, -5, -120 };
    private static final String CANON = "01:02:1e:5a:fb:88";

    @Test
    public void testBytesToMac() {
        final var mac = IetfYangUtil.macAddressFor(BYTES);
        assertEquals(CANON, mac.getValue());
    }

    @Test
    public void testMacToBytes() {
        final byte[] bytes1 = IetfYangUtil.macAddressBytes(new MacAddress(CANON));
        assertArrayEquals(BYTES, bytes1);

        final byte[] bytes2 = IetfYangUtil.macAddressBytes(new MacAddress("01:02:1E:5a:Fb:88"));
        assertArrayEquals(BYTES, bytes2);
    }

    @Test
    public void testBytesToHex() {
        final HexString hex = IetfYangUtil.hexStringFor(BYTES);
        assertEquals(CANON, hex.getValue());
    }

    @Test
    public void testHexToBytes() {
        final byte[] bytes1 = IetfYangUtil.hexStringBytes(new HexString(CANON));
        assertArrayEquals(BYTES, bytes1);

        final byte[] bytes2 = IetfYangUtil.hexStringBytes(new HexString("01:02:1E:5a:Fb:88"));
        assertArrayEquals(BYTES, bytes2);
    }

    @Test
    public void testPhysToBytes() {
        final byte[] bytes1 = IetfYangUtil.physAddressBytes(new PhysAddress(CANON));
        assertArrayEquals(BYTES, bytes1);

        final byte[] bytes2 = IetfYangUtil.physAddressBytes(new PhysAddress("01:02:1E:5a:Fb:88"));
        assertArrayEquals(BYTES, bytes2);

        assertArrayEquals(new byte[0], IetfYangUtil.physAddressBytes(new PhysAddress("")));
        assertArrayEquals(new byte[] { (byte) 0xaa }, IetfYangUtil.physAddressBytes(new PhysAddress("aa")));
        assertArrayEquals(new byte[] { (byte) 0xaa, (byte) 0xbb },
            IetfYangUtil.physAddressBytes(new PhysAddress("aa:bb")));
    }

    @Test
    public void testQuadBits() {
        assertEquals(0x01020304, IetfYangUtil.dottedQuadBits(new DottedQuad("1.2.3.4")));
        assertEquals(0xFFFFFFFF, IetfYangUtil.dottedQuadBits(new DottedQuad("255.255.255.255")));
    }

    @Test
    public void testQuadBytes() {
        assertArrayEquals(new byte[] { 1, 2, 3, 4 }, IetfYangUtil.dottedQuadBytes(new DottedQuad("1.2.3.4")));
    }

    @Test
    public void testQuadForBits() {
        assertEquals("1.2.3.4", IetfYangUtil.dottedQuadFor(0x01020304).getValue());
        assertEquals("255.255.255.255", IetfYangUtil.dottedQuadFor(0xFFFFFFFF).getValue());
    }

    @Test
    public void testQuadForBytes() {
        assertEquals("1.2.3.4", IetfYangUtil.dottedQuadFor(new byte[] { 1, 2, 3, 4 }).getValue());
    }

    @Test
    public void testUuidFor() {
        final var uuid = UUID.fromString("f81d4fae-7dec-11d0-a765-00a0c91e6bf6");
        assertEquals("f81d4fae-7dec-11d0-a765-00a0c91e6bf6", IetfYangUtil.uuidFor(uuid).getValue());
    }

    @Test
    public void canonizeMACTest() {
        assertEquals(CANON, IetfYangUtil.canonizeMacAddress(new MacAddress("01:02:1E:5A:FB:88")).getValue());
    }

    @Test
    public void testDottedQuad() {
        assertArrayEquals(new byte[] { 1, 2, 3, 4 }, IetfYangUtil.dottedQuadBytes(new DottedQuad("1.2.3.4")));
        assertEquals(new DottedQuad("1.2.3.4"), IetfYangUtil.dottedQuadFor(new byte[] { 1, 2, 3, 4 }));
    }

    @Test
    public void testHexString() {
        assertArrayEquals(new byte[] { 0, 1 }, IetfYangUtil.hexStringBytes(new HexString("00:01")));
        assertEquals(new HexString("00:01"), IetfYangUtil.hexStringFor(new byte[] { 0, 1 }));
    }

    @Test
    public void testPhysAddress() {
        assertArrayEquals(new byte[] { 0, 1} , IetfYangUtil.physAddressBytes(new PhysAddress("00:01")));
        assertEquals(new PhysAddress("00:01"), IetfYangUtil.physAddressFor(new byte[] { 0, 1 }));
    }

    @Test
    public void testMacAddress() {
        assertArrayEquals(new byte[] { 0, 1, 2, 3, 4, 5 },
            IetfYangUtil.macAddressBytes(new MacAddress("00:01:02:03:04:05")));
        assertEquals(new MacAddress("00:01:02:03:04:05"), IetfYangUtil.macAddressFor(new byte[] { 0, 1, 2, 3, 4, 5 }));
    }

    @Test
    public void testUuid() {
        final UUID java = UUID.randomUUID();
        assertEquals(new Uuid(java.toString()), IetfYangUtil.uuidFor(java));
    }
}