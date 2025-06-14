/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.net.InetAddresses;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import org.junit.jupiter.api.Test;

class IetfInetUtilTest {
    @Test
    void testIpv4Address() {
        final var ipv4Address = new Ipv4AddressNoZone("192.168.1.1");
        final var ipv4Prefix = new Ipv4Prefix("192.0.2.1/24");
        final var ipAddress = new IpAddress(ipv4Address);
        final var ipPrefix = new IpPrefix(ipv4Prefix);

        assertEquals(ipv4Prefix, ipPrefix.getIpv4Prefix());
        assertEquals(ipAddress, new IpAddress(ipv4Address));
    }

    @Test
    void testIpv6Address() {
        final var ipv6Address = new Ipv6AddressNoZone("ABCD:EF01:2345:6789:ABCD:EF01:2345:6789");
        final var ipv6Prefix = new Ipv6Prefix("ff00::/8");
        final var ipAddress = new IpAddress(ipv6Address);
        final var ipPrefix = new IpPrefix(ipv6Prefix);

        assertEquals(ipv6Prefix, ipPrefix.getIpv6Prefix());
        assertEquals(ipAddress, new IpAddress(ipv6Address));
    }

    @Test
    void testAddressToString() {
        assertEquals(new Ipv4Prefix("1.2.3.4/8"), IetfInetUtil.ipv4PrefixFor(new Ipv4Address("1.2.3.4%1"), 8));
        assertEquals(new Ipv6Prefix("ff00::/8"), IetfInetUtil.ipv6PrefixFor(new Ipv6Address("ff00::%bar"), 8));
    }

    @Test
    void testIpv4ZoneStripping() {
        final var noZone = new Ipv4AddressNoZone("1.2.3.4");
        assertSame(noZone, IetfInetUtil.ipv4AddressNoZoneFor(noZone));

        final var withoutZone = new Ipv4Address(noZone);
        final var stripped = IetfInetUtil.ipv4AddressNoZoneFor(withoutZone);
        assertSame(withoutZone.getValue(), stripped.getValue());

        assertEquals(noZone, IetfInetUtil.ipv4AddressNoZoneFor(new Ipv4Address("1.2.3.4%1")));
    }

    @Test
    void testIpv6ZoneStripping() {
        final var noZone = new Ipv6AddressNoZone("ff00::");
        assertSame(noZone, IetfInetUtil.ipv6AddressNoZoneFor(noZone));

        final var withoutZone = new Ipv6Address(noZone);
        final var stripped = IetfInetUtil.ipv6AddressNoZoneFor(withoutZone);
        assertSame(withoutZone.getValue(), stripped.getValue());

        assertEquals(noZone, IetfInetUtil.ipv6AddressNoZoneFor(new Ipv6Address("ff00::%1")));
    }

    @Test
    void testIpToBytesAndBack() {
        assertV4Equals("1.2.3.4");
        assertV4Equals("12.23.34.45");
        assertV4Equals("255.254.253.252");
        assertV4Equals("128.16.0.127");

        assertV4Equals("1.2.3.4", "%5");
        assertV4Equals("12.23.34.45", "%5");
        assertV4Equals("255.254.253.252", "%5");
        assertV4Equals("128.16.0.127", "%5");

        assertEquals(new IpAddress(new Ipv4Address("1.2.3.4")),
            IetfInetUtil.ipAddressFor(IetfInetUtil.ipv4AddressBytes(new Ipv4Address("1.2.3.4"))));
        assertNotEquals(new IpAddress(new Ipv4Address("2.3.4.5")),
            IetfInetUtil.ipAddressFor(IetfInetUtil.ipv4AddressBytes(new Ipv4Address("1.2.3.4"))));

        assertEquals(new IpAddress(new Ipv6Address("fe80::2002:b3ff:fe1e:8329")),
            IetfInetUtil.ipAddressFor(IetfInetUtil.ipv6AddressBytes(new Ipv6Address("FE80::2002:B3FF:FE1E:8329"))));
        assertNotEquals(new IpAddress(new Ipv6Address("feff::2002:b3ff:fe1e:8329")),
            IetfInetUtil.ipAddressFor(IetfInetUtil.ipv6AddressBytes(new Ipv6Address("FE80::2002:B3FF:FE1E:8329"))));

        assertEquals(new IpAddress(new Ipv4Address("1.2.3.4")),
            IetfInetUtil.ipAddressFor(IetfInetUtil.inetAddressFor(new IpAddress(new Ipv4Address("1.2.3.4")))));
        assertNotEquals(new IpAddress(new Ipv4Address("2.3.4.5")),
            IetfInetUtil.ipAddressFor(IetfInetUtil.inetAddressFor(new IpAddress(new Ipv4Address("1.2.3.4")))));

        assertEquals(new IpAddress(new Ipv6Address("fe80::2002:b3ff:fe1e:8329")),
            IetfInetUtil.ipAddressFor(IetfInetUtil.inetAddressFor(
                new IpAddress(new Ipv6Address("FE80::2002:B3FF:FE1E:8329")))));
        assertNotEquals(new IpAddress(new Ipv6Address("FEFF::2002:B3FF:FE1E:8329")),
            IetfInetUtil.ipAddressFor(IetfInetUtil.inetAddressFor(
                new IpAddress(new Ipv6Address("FE80::2002:B3FF:FE1E:8329")))));
    }

    @Test
    void illegalArrayLengthForAddressTest() {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> IetfInetUtil.ipAddressFor(new byte[] { 0, 0, 0 }));
        assertEquals("Invalid array length 3", ex.getMessage());
    }

    @Test
    void unhandledAddressTest() {
        final var adr = mock(InetAddress.class);
        doReturn("testAddress").when(adr).toString();
        final var ex = assertThrows(IllegalArgumentException.class, () -> IetfInetUtil.ipAddressFor(adr));
        assertEquals("Unhandled address testAddress", ex.getMessage());
    }

    @Test
    void illegalArrayLengthforPrefixTest() {
        final var ex = assertThrows(IllegalArgumentException.class,
            () -> IetfInetUtil.ipPrefixFor(new byte[] { 0, 0, 0 }, 0));
        assertEquals("Invalid array length 3", ex.getMessage());
    }

    @Test
    void illegalAddressforPrefixTest() {
        final var adr = mock(InetAddress.class);
        doReturn("testAddress").when(adr).toString();

        final var ex = assertThrows(IllegalArgumentException.class, () -> IetfInetUtil.ipPrefixFor(adr, 0));
        assertEquals("Unhandled address testAddress", ex.getMessage());
    }

    @Test
    void ipv4Tests() {
        assertEquals("1.2.3.4", IetfInetUtil.ipv4AddressFrom(new Ipv4Prefix("1.2.3.4/16")).getValue());
        final var ipv4address = new Ipv4Address("1.2.3.4");
        final var ipAddress = new IpAddress(ipv4address);
        assertEquals("1.2.3.4/32", IetfInetUtil.ipv4PrefixFor(IetfInetUtil.ipv4AddressBytes(ipv4address)).getValue());
        assertEquals("1.2.3.4/32", IetfInetUtil.ipv4PrefixFor(IetfInetUtil.inetAddressFor(ipAddress)).getValue());
        assertEquals("1.2.3.4/32", IetfInetUtil.ipv4PrefixFor(ipv4address).getValue());
        assertEquals("1.2.3.4/16", IetfInetUtil.ipv4PrefixFor(ipv4address, 16).getValue());

        assertEquals("0.0.0.0/0",
            IetfInetUtil.ipv4PrefixForShort(IetfInetUtil.ipv4AddressBytes(ipv4address), 0).getValue());
        assertEquals("1.2.3.4/32",
            IetfInetUtil.ipv4PrefixForShort(IetfInetUtil.ipv4AddressBytes(ipv4address), 32).getValue());
        assertEquals("0.0.0.0/0",
            IetfInetUtil.ipv4PrefixForShort(IetfInetUtil.ipv4AddressBytes(ipv4address), 0, 0).getValue());
        assertEquals("1.2.3.4/32",
            IetfInetUtil.ipv4PrefixForShort(IetfInetUtil.ipv4AddressBytes(ipv4address), 0, 32).getValue());
        assertEquals("2.3.4.5/32", IetfInetUtil.ipv4PrefixForShort(new byte[] { 1, 2, 3, 4, 5 }, 1, 32).getValue());
        assertEquals("1.0.0.0/1", IetfInetUtil.ipv4PrefixForShort(new byte[] { 1, 2, 3, 4, 5 }, 0, 1).getValue());

        final var ipv4Prefix = new Ipv4Prefix("1.2.3.4/16");
        assertEquals("1.2.3.4", IetfInetUtil.splitIpv4Prefix(ipv4Prefix).getKey().getValue());
        assertEquals((Integer) 16, IetfInetUtil.splitIpv4Prefix(ipv4Prefix).getValue());
        assertArrayEquals(new byte[] { 1,2,3,4,16 }, IetfInetUtil.ipv4PrefixToBytes(ipv4Prefix));
    }

    @Test
    void ipv6Tests() {
        assertEquals("::0", IetfInetUtil.ipv6AddressFrom(new Ipv6Prefix("::0/128")).getValue());
        final var ipv6address = new Ipv6Address("::0");
        final var ipAddress = new IpAddress(ipv6address);

        assertEquals("::/128", IetfInetUtil.ipv6PrefixFor(IetfInetUtil.ipv6AddressBytes(ipv6address)).getValue());
        assertEquals("::/128", IetfInetUtil.ipv6PrefixFor(IetfInetUtil.inetAddressFor(ipAddress)).getValue());
        assertEquals("::0/128", IetfInetUtil.ipv6PrefixFor(ipv6address).getValue());
        assertEquals("::0/16", IetfInetUtil.ipv6PrefixFor(ipv6address, 16).getValue());

        assertEquals("::0/0",
            IetfInetUtil.ipv6PrefixForShort(IetfInetUtil.ipv6AddressBytes(ipv6address), 0).getValue());
        assertEquals("::/64",
            IetfInetUtil.ipv6PrefixForShort(IetfInetUtil.ipv6AddressBytes(ipv6address), 64).getValue());
        assertEquals("::0/0",
            IetfInetUtil.ipv6PrefixForShort(IetfInetUtil.ipv6AddressBytes(ipv6address), 0, 0).getValue());
        assertEquals("::/32",
            IetfInetUtil.ipv6PrefixForShort(IetfInetUtil.ipv6AddressBytes(ipv6address), 0, 32).getValue());

        assertEquals(Map.entry(new Ipv6Address("::"), 32), IetfInetUtil.splitIpv6Prefix(new Ipv6Prefix("::/32")));
        assertArrayEquals(new byte[] { 0, 10, 0, 0, 0, 0, 0, 0, 0, 11, 0, 12, 0, 13, 0, 14, 64 },
            IetfInetUtil.ipv6PrefixToBytes(new Ipv6Prefix("A::B:C:D:E/64")));

        // verify that an IPv4-mapped IPv6 address gets parsed as an IPv6 address
        assertEquals("::ffff:ab0:eb", IetfInetUtil.ipv6AddressFor(
                new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xff, (byte) 0xff, 0x0a, (byte) 0xb0, 0, (byte) 0xeb})
                .getValue());
    }

    @Test
    void prefixTest() {
        assertEquals(new IpPrefix(new Ipv4Prefix("0.0.0.0/16")),
            IetfInetUtil.ipPrefixFor(IetfInetUtil.inetAddressFor(new IpAddress(new Ipv4Address("0.0.0.0"))), 16));
        assertEquals(new IpPrefix(new Ipv6Prefix("::/64")),
            IetfInetUtil.ipPrefixFor(IetfInetUtil.inetAddressFor(new IpAddress(new Ipv6Address("::"))), 64));

        assertEquals(new IpPrefix(new Ipv4Prefix("0.0.0.0/16")),
            IetfInetUtil.ipPrefixFor(new byte[] { 0, 0, 0, 0 }, 16));
        assertEquals(new IpPrefix(new Ipv6Prefix("::/64")),
            IetfInetUtil.ipPrefixFor(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, 64));
    }

    @Test
    void inetAddressTest() {
        assertInstanceOf(Inet4Address.class, IetfInetUtil.inetAddressFor(new IpAddress(new Ipv4Address("1.2.3.4"))));
        assertInstanceOf(Inet6Address.class,
            IetfInetUtil.inetAddressFor(new IpAddress(new Ipv6Address("FE80::2002:B3FF:FE1E:8329"))));
    }

    @Test
    void inet4AddressForWithExceptionTest() {
        final var ipClass = mock(Ipv4Address.class);
        doReturn("testClass").when(ipClass).toString();
        doAnswer(inv -> {
            throw new UnknownHostException();
        }).when(ipClass).getValue();

        final var ex = assertThrows(IllegalArgumentException.class, () -> IetfInetUtil.inet4AddressFor(ipClass));
        assertEquals("Invalid address testClass", ex.getMessage());
    }

    @Test
    void inet6AddressForWithExceptionTest() {
        final var ipClass = mock(Ipv6Address.class);
        doReturn("testClass").when(ipClass).toString();
        doAnswer(inv -> {
            throw new UnknownHostException();
        }).when(ipClass).getValue();

        final var ex = assertThrows(IllegalArgumentException.class, () -> IetfInetUtil.inet6AddressFor(ipClass));
        assertEquals("Invalid address testClass", ex.getMessage());
    }

    @Test
    void testIpv4AddressForBits() {
        assertEquals("1.2.3.4", IetfInetUtil.ipv4AddressFor(0x01020304).getValue());
        assertEquals("255.255.255.255", IetfInetUtil.ipv4AddressFor(0xFFFFFFFF).getValue());
    }

    @Test
    void testIpv4AddressBits() {
        assertEquals(0x01020304, IetfInetUtil.ipv4AddressBits(new Ipv4Address("1.2.3.4")));
        assertEquals(0xFFFFFFFF, IetfInetUtil.ipv4AddressBits(new Ipv4Address("255.255.255.255")));
    }

    @Test
    void testIpv4AddressNoZoneBits() {
        assertEquals(0x01020304, IetfInetUtil.ipv4AddressNoZoneBits(new Ipv4AddressNoZone("1.2.3.4")));
        assertEquals(0xFFFFFFFF, IetfInetUtil.ipv4AddressNoZoneBits(new Ipv4AddressNoZone("255.255.255.255")));
    }

    private static void assertV4Equals(final String literal, final String append) {
        final byte[] expected = InetAddresses.forString(literal).getAddress();
        final byte[] actual = IetfInetUtil.ipv4AddressBytes(new Ipv4Address(literal + append));
        assertArrayEquals(expected, actual);
    }

    private static void assertV4Equals(final String literal) {
        assertV4Equals(literal, "");
    }
}
