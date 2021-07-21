/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.model.ietf.util;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.net.InetAddresses;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.junit.Test;

public class AbstractIetfInetUtilTest {
    private static final IpUtil UTIL = new IpUtil();

    private static void assertV4Equals(final String literal, final String append) {
        final byte[] expected = InetAddresses.forString(literal).getAddress();
        final byte[] actual = UTIL.ipv4AddressBytes(new IpClass(literal + append));
        assertArrayEquals(expected, actual);
    }

    private static void assertV4Equals(final String literal) {
        assertV4Equals(literal, "");
    }

    @Test
    public void testIpToBytesAndBack() {
        assertV4Equals("1.2.3.4");
        assertV4Equals("12.23.34.45");
        assertV4Equals("255.254.253.252");
        assertV4Equals("128.16.0.127");

        assertV4Equals("1.2.3.4", "%5");
        assertV4Equals("12.23.34.45", "%5");
        assertV4Equals("255.254.253.252", "%5");
        assertV4Equals("128.16.0.127", "%5");

        assertEquals(new IpClass("1.2.3.4").getValue().toLowerCase(),
                UTIL.ipAddressFor(UTIL.ipv4AddressBytes(new IpClass("1.2.3.4"))).getValue().toLowerCase());
        assertNotEquals(new IpClass("2.3.4.5").getValue().toLowerCase(),
                UTIL.ipAddressFor(UTIL.ipv4AddressBytes(new IpClass("1.2.3.4"))).getValue().toLowerCase());

        assertEquals(new IpClass("FE80::2002:B3FF:FE1E:8329").getValue().toLowerCase(),
                UTIL.ipAddressFor(
                        UTIL.ipv6AddressBytes(new IpClass("FE80::2002:B3FF:FE1E:8329"))).getValue().toLowerCase());
        assertNotEquals(new IpClass("FEFF::2002:B3FF:FE1E:8329").getValue().toLowerCase(),
                UTIL.ipAddressFor(
                        UTIL.ipv6AddressBytes(new IpClass("FE80::2002:B3FF:FE1E:8329"))).getValue().toLowerCase());

        assertEquals(new IpClass("1.2.3.4").getValue().toLowerCase(),
                UTIL.ipAddressFor(UTIL.inetAddressFor(new IpClass("1.2.3.4"))).getValue().toLowerCase());
        assertNotEquals(new IpClass("2.3.4.5").getValue().toLowerCase(),
                UTIL.ipAddressFor(UTIL.inetAddressFor(new IpClass("1.2.3.4"))).getValue().toLowerCase());

        assertEquals(new IpClass("FE80::2002:B3FF:FE1E:8329").getValue().toLowerCase(),
                UTIL.ipAddressFor(
                        UTIL.inetAddressFor(new IpClass("FE80::2002:B3FF:FE1E:8329"))).getValue().toLowerCase());
        assertNotEquals(new IpClass("FEFF::2002:B3FF:FE1E:8329").getValue().toLowerCase(),
                UTIL.ipAddressFor(
                        UTIL.inetAddressFor(new IpClass("FE80::2002:B3FF:FE1E:8329"))).getValue().toLowerCase());
    }

    @Test
    public void illegalArrayLengthForAddressTest() {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> UTIL.ipAddressFor(new byte[] { 0, 0, 0 }));
        assertEquals("Invalid array length 3", ex.getMessage());
    }

    @Test
    public void unhandledAddressTest() {
        final InetAddress adr = mock(InetAddress.class);
        doReturn("testAddress").when(adr).toString();
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> UTIL.ipAddressFor(adr));
        assertEquals("Unhandled address testAddress", ex.getMessage());
    }

    @Test
    public void illegalArrayLengthforPrefixTest() {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> UTIL.ipPrefixFor(new byte[] { 0, 0, 0 }, 0));
        assertEquals("Invalid array length 3", ex.getMessage());
    }

    @Test
    public void illegalAddressforPrefixTest() {
        final InetAddress adr = mock(InetAddress.class);
        doReturn("testAddress").when(adr).toString();

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> UTIL.ipPrefixFor(adr, 0));
        assertEquals("Unhandled address testAddress", ex.getMessage());
    }

    @Test
    public void ipv4Tests() {
        assertEquals("1.2.3.4", UTIL.ipv4AddressFrom(new IpClass("1.2.3.4/16")).getValue());
        final IpClass ipClass = new IpClass("1.2.3.4");
        assertEquals("1.2.3.4/32", UTIL.ipv4PrefixFor(UTIL.ipv4AddressBytes(ipClass)).getValue());
        assertEquals("1.2.3.4/32", UTIL.ipv4PrefixFor(UTIL.inetAddressFor(ipClass)).getValue());
        assertEquals("1.2.3.4/32", UTIL.ipv4PrefixFor(ipClass).getValue());
        assertEquals("1.2.3.4/16", UTIL.ipv4PrefixFor(ipClass, 16).getValue());

        assertEquals("0.0.0.0/0", UTIL.ipv4PrefixForShort(UTIL.ipv4AddressBytes(ipClass), 0).getValue());
        assertEquals("1.2.3.4/32", UTIL.ipv4PrefixForShort(UTIL.ipv4AddressBytes(ipClass), 32).getValue());
        assertEquals("0.0.0.0/0", UTIL.ipv4PrefixForShort(UTIL.ipv4AddressBytes(ipClass), 0, 0).getValue());
        assertEquals("1.2.3.4/32", UTIL.ipv4PrefixForShort(UTIL.ipv4AddressBytes(ipClass), 0, 32).getValue());
        assertEquals("2.3.4.5/32", UTIL.ipv4PrefixForShort(new byte[] { 1, 2, 3, 4, 5 }, 1, 32).getValue());
        assertEquals("1.0.0.0/1", UTIL.ipv4PrefixForShort(new byte[] { 1, 2, 3, 4, 5 }, 0, 1).getValue());

        assertEquals("1.2.3.4", UTIL.splitIpv4Prefix(new IpClass("1.2.3.4/16")).getKey().getValue());
        assertEquals((Integer) 16, UTIL.splitIpv4Prefix(new IpClass("1.2.3.4/16")).getValue());
        assertArrayEquals(new byte[] { 1,2,3,4,16 }, UTIL.ipv4PrefixToBytes(new IpClass("1.2.3.4/16")));
    }

    @Test
    public void ipv6Tests() {
        assertEquals("::0", UTIL.ipv6AddressFrom(new IpClass("::0/128")).getValue());
        final IpClass ipClass = new IpClass("::0");
        assertEquals("::/128", UTIL.ipv6PrefixFor(UTIL.ipv6AddressBytes(ipClass)).getValue());
        assertEquals("::/128", UTIL.ipv6PrefixFor(UTIL.inetAddressFor(ipClass)).getValue());
        assertEquals("::0/128", UTIL.ipv6PrefixFor(ipClass).getValue());
        assertEquals("::0/16", UTIL.ipv6PrefixFor(ipClass, 16).getValue());

        assertEquals("::0/0", UTIL.ipv6PrefixForShort(UTIL.ipv6AddressBytes(ipClass), 0).getValue());
        assertEquals("::/64", UTIL.ipv6PrefixForShort(UTIL.ipv6AddressBytes(ipClass), 64).getValue());
        assertEquals("::0/0", UTIL.ipv6PrefixForShort(UTIL.ipv6AddressBytes(ipClass), 0, 0).getValue());
        assertEquals("::/32", UTIL.ipv6PrefixForShort(UTIL.ipv6AddressBytes(ipClass), 0, 32).getValue());

        assertEquals("::", UTIL.splitIpv6Prefix(new IpClass("::/32")).getKey().getValue());
        assertEquals((Integer) 32, UTIL.splitIpv6Prefix(new IpClass("::/32")).getValue());
        assertArrayEquals(new byte[] { 0, 10, 0, 0, 0, 0, 0, 0, 0, 11, 0, 12, 0, 13, 0, 14, 64 },
                UTIL.ipv6PrefixToBytes(new IpClass("A::B:C:D:E/64")));

        // verify that an IPv4-mapped IPv6 address gets parsed as an IPv6 address
        assertEquals("::ffff:ab0:eb", UTIL.ipv6AddressFor(
                new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xff, (byte) 0xff, 0x0a, (byte) 0xb0, 0, (byte) 0xeb})
                .getValue());
    }

    @Test
    public void prefixTest() {
        assertEquals("0.0.0.0/16", UTIL.ipPrefixFor(UTIL.inetAddressFor(new IpClass("0.0.0.0")), 16).getValue());
        assertEquals("::/64", UTIL.ipPrefixFor(UTIL.inetAddressFor(new IpClass("::")), 64).getValue());

        assertEquals("0.0.0.0/16", UTIL.ipPrefixFor(new byte[] { 0, 0, 0, 0 }, 16).getValue());
        assertEquals("::/64",
            UTIL.ipPrefixFor(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, 64).getValue());
    }

    @Test
    public void inetAddressTest() {
        assertThat(UTIL.inetAddressFor(new IpClass("1.2.3.4")), instanceOf(Inet4Address.class));
        assertThat(UTIL.inetAddressFor(new IpClass("FE80::2002:B3FF:FE1E:8329")), instanceOf(Inet6Address.class));
    }

    @Test
    public void inet4AddressForWithExceptionTest() {
        final IpClass ipClass = mock(IpClass.class);
        doReturn("testClass").when(ipClass).toString();
        doAnswer(inv -> {
            throw new UnknownHostException();
        }).when(ipClass).getValue();

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> UTIL.inet4AddressFor(ipClass));
        assertEquals("Invalid address testClass", ex.getMessage());
    }

    @Test
    public void inet6AddressForWithExceptionTest() {
        final IpClass ipClass = mock(IpClass.class);
        doReturn("testClass").when(ipClass).toString();
        doAnswer(inv -> {
            throw new UnknownHostException();
        }).when(ipClass).getValue();

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> UTIL.inet6AddressFor(ipClass));
        assertEquals("Invalid address testClass", ex.getMessage());
    }

    @Test
    public void testIpv4AddressForBits() {
        assertEquals("1.2.3.4", UTIL.ipv4AddressFor(0x01020304).getValue());
        assertEquals("255.255.255.255", UTIL.ipv4AddressFor(0xFFFFFFFF).getValue());
    }

    @Test
    public void testIpv4AddressBits() {
        assertEquals(0x01020304, UTIL.ipv4AddressBits(new IpClass("1.2.3.4")));
        assertEquals(0xFFFFFFFF, UTIL.ipv4AddressBits(new IpClass("255.255.255.255")));
    }

    @Test
    public void testIpv4AddressNoZoneBits() {
        assertEquals(0x01020304, UTIL.ipv4AddressNoZoneBits(new IpClass("1.2.3.4")));
        assertEquals(0xFFFFFFFF, UTIL.ipv4AddressNoZoneBits(new IpClass("255.255.255.255")));
    }
}
