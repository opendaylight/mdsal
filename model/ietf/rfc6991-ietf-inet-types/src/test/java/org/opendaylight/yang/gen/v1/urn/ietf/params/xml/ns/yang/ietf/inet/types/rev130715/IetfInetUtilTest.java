/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IetfInetUtil.INSTANCE;

import com.google.common.net.InetAddresses;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import org.junit.Test;

public class IetfInetUtilTest {
    @Test
    public void testIpv4Address() {
        final Ipv4AddressNoZone ipv4Address = new Ipv4AddressNoZone("192.168.1.1");
        final Ipv4Prefix ipv4Prefix = new Ipv4Prefix("192.0.2.1/24");
        final IpAddress ipAddress = new IpAddress(ipv4Address);
        final IpPrefix ipPrefix = new IpPrefix(ipv4Prefix);

        assertEquals(ipv4Prefix, ipPrefix.getIpv4Prefix());
        assertEquals(ipAddress, new IpAddress(ipv4Address));
    }

    @Test
    public void testIpv6Address() {
        final Ipv6AddressNoZone ipv6Address = new Ipv6AddressNoZone("ABCD:EF01:2345:6789:ABCD:EF01:2345:6789");
        final Ipv6Prefix ipv6Prefix = new Ipv6Prefix("ff00::/8");
        final IpAddress ipAddress = new IpAddress(ipv6Address);
        final IpPrefix ipPrefix = new IpPrefix(ipv6Prefix);

        assertEquals(ipv6Prefix, ipPrefix.getIpv6Prefix());
        assertEquals(ipAddress, new IpAddress(ipv6Address));
    }

    @Test
    public void testAddressToString() {
        assertEquals(new Ipv4Prefix("1.2.3.4/8"), INSTANCE.ipv4PrefixFor(new Ipv4Address("1.2.3.4%1"), 8));
        assertEquals(new Ipv6Prefix("ff00::/8"), INSTANCE.ipv6PrefixFor(new Ipv6Address("ff00::%bar"), 8));
    }

    @Test
    public void testIpv4ZoneStripping() {
        final Ipv4AddressNoZone noZone = new Ipv4AddressNoZone("1.2.3.4");
        assertSame(noZone, INSTANCE.ipv4AddressNoZoneFor(noZone));

        final Ipv4Address withoutZone = new Ipv4Address(noZone);
        final Ipv4AddressNoZone stripped = INSTANCE.ipv4AddressNoZoneFor(withoutZone);
        assertSame(withoutZone.getValue(), stripped.getValue());

        assertEquals(noZone, INSTANCE.ipv4AddressNoZoneFor(new Ipv4Address("1.2.3.4%1")));
    }

    @Test
    public void testIpv6ZoneStripping() {
        final Ipv6AddressNoZone noZone = new Ipv6AddressNoZone("ff00::");
        assertSame(noZone, INSTANCE.ipv6AddressNoZoneFor(noZone));

        final Ipv6Address withoutZone = new Ipv6Address(noZone);
        final Ipv6AddressNoZone stripped = INSTANCE.ipv6AddressNoZoneFor(withoutZone);
        assertSame(withoutZone.getValue(), stripped.getValue());

        assertEquals(noZone, INSTANCE.ipv6AddressNoZoneFor(new Ipv6Address("ff00::%1")));
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

        assertEquals(new IpAddress(new Ipv4Address("1.2.3.4")),
            INSTANCE.ipAddressFor(INSTANCE.ipv4AddressBytes(new Ipv4Address("1.2.3.4"))));
        assertNotEquals(new IpAddress(new Ipv4Address("2.3.4.5")),
            INSTANCE.ipAddressFor(INSTANCE.ipv4AddressBytes(new Ipv4Address("1.2.3.4"))));

        assertEquals(new IpAddress(new Ipv6Address("fe80::2002:b3ff:fe1e:8329")),
            INSTANCE.ipAddressFor(INSTANCE.ipv6AddressBytes(new Ipv6Address("FE80::2002:B3FF:FE1E:8329"))));
        assertNotEquals(new IpAddress(new Ipv6Address("feff::2002:b3ff:fe1e:8329")),
            INSTANCE.ipAddressFor(INSTANCE.ipv6AddressBytes(new Ipv6Address("FE80::2002:B3FF:FE1E:8329"))));

        assertEquals(new IpAddress(new Ipv4Address("1.2.3.4")),
            INSTANCE.ipAddressFor(INSTANCE.inetAddressFor(new IpAddress(new Ipv4Address("1.2.3.4")))));
        assertNotEquals(new IpAddress(new Ipv4Address("2.3.4.5")),
            INSTANCE.ipAddressFor(INSTANCE.inetAddressFor(new IpAddress(new Ipv4Address("1.2.3.4")))));

        assertEquals(new IpAddress(new Ipv6Address("fe80::2002:b3ff:fe1e:8329")),
            INSTANCE.ipAddressFor(INSTANCE.inetAddressFor(
                new IpAddress(new Ipv6Address("FE80::2002:B3FF:FE1E:8329")))));
        assertNotEquals(new IpAddress(new Ipv6Address("FEFF::2002:B3FF:FE1E:8329")),
            INSTANCE.ipAddressFor(INSTANCE.inetAddressFor(
                new IpAddress(new Ipv6Address("FE80::2002:B3FF:FE1E:8329")))));
    }

    @Test
    public void illegalArrayLengthForAddressTest() {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> INSTANCE.ipAddressFor(new byte[] { 0, 0, 0 }));
        assertEquals("Invalid array length 3", ex.getMessage());
    }

    @Test
    public void unhandledAddressTest() {
        final InetAddress adr = mock(InetAddress.class);
        doReturn("testAddress").when(adr).toString();
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> INSTANCE.ipAddressFor(adr));
        assertEquals("Unhandled address testAddress", ex.getMessage());
    }

    @Test
    public void illegalArrayLengthforPrefixTest() {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> INSTANCE.ipPrefixFor(new byte[] { 0, 0, 0 }, 0));
        assertEquals("Invalid array length 3", ex.getMessage());
    }

    @Test
    public void illegalAddressforPrefixTest() {
        final InetAddress adr = mock(InetAddress.class);
        doReturn("testAddress").when(adr).toString();

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> INSTANCE.ipPrefixFor(adr, 0));
        assertEquals("Unhandled address testAddress", ex.getMessage());
    }

    @Test
    public void ipv4Tests() {
        assertEquals("1.2.3.4", INSTANCE.ipv4AddressFrom(new Ipv4Prefix("1.2.3.4/16")).getValue());
        final var ipv4address = new Ipv4Address("1.2.3.4");
        final var ipAddress = new IpAddress(ipv4address);
        assertEquals("1.2.3.4/32", INSTANCE.ipv4PrefixFor(INSTANCE.ipv4AddressBytes(ipv4address)).getValue());
        assertEquals("1.2.3.4/32", INSTANCE.ipv4PrefixFor(INSTANCE.inetAddressFor(ipAddress)).getValue());
        assertEquals("1.2.3.4/32", INSTANCE.ipv4PrefixFor(ipv4address).getValue());
        assertEquals("1.2.3.4/16", INSTANCE.ipv4PrefixFor(ipv4address, 16).getValue());

        assertEquals("0.0.0.0/0", INSTANCE.ipv4PrefixForShort(INSTANCE.ipv4AddressBytes(ipv4address), 0).getValue());
        assertEquals("1.2.3.4/32", INSTANCE.ipv4PrefixForShort(INSTANCE.ipv4AddressBytes(ipv4address), 32).getValue());
        assertEquals("0.0.0.0/0", INSTANCE.ipv4PrefixForShort(INSTANCE.ipv4AddressBytes(ipv4address), 0, 0).getValue());
        assertEquals("1.2.3.4/32",
            INSTANCE.ipv4PrefixForShort(INSTANCE.ipv4AddressBytes(ipv4address), 0, 32).getValue());
        assertEquals("2.3.4.5/32", INSTANCE.ipv4PrefixForShort(new byte[] { 1, 2, 3, 4, 5 }, 1, 32).getValue());
        assertEquals("1.0.0.0/1", INSTANCE.ipv4PrefixForShort(new byte[] { 1, 2, 3, 4, 5 }, 0, 1).getValue());

        final var ipv4Prefix = new Ipv4Prefix("1.2.3.4/16");
        assertEquals("1.2.3.4", INSTANCE.splitIpv4Prefix(ipv4Prefix).getKey().getValue());
        assertEquals((Integer) 16, INSTANCE.splitIpv4Prefix(ipv4Prefix).getValue());
        assertArrayEquals(new byte[] { 1,2,3,4,16 }, INSTANCE.ipv4PrefixToBytes(ipv4Prefix));
    }

    @Test
    public void ipv6Tests() {
        assertEquals("::0", INSTANCE.ipv6AddressFrom(new Ipv6Prefix("::0/128")).getValue());
        final var ipv6address = new Ipv6Address("::0");
        final var ipAddress = new IpAddress(ipv6address);

        assertEquals("::/128", INSTANCE.ipv6PrefixFor(INSTANCE.ipv6AddressBytes(ipv6address)).getValue());
        assertEquals("::/128", INSTANCE.ipv6PrefixFor(INSTANCE.inetAddressFor(ipAddress)).getValue());
        assertEquals("::0/128", INSTANCE.ipv6PrefixFor(ipv6address).getValue());
        assertEquals("::0/16", INSTANCE.ipv6PrefixFor(ipv6address, 16).getValue());

        assertEquals("::0/0", INSTANCE.ipv6PrefixForShort(INSTANCE.ipv6AddressBytes(ipv6address), 0).getValue());
        assertEquals("::/64", INSTANCE.ipv6PrefixForShort(INSTANCE.ipv6AddressBytes(ipv6address), 64).getValue());
        assertEquals("::0/0", INSTANCE.ipv6PrefixForShort(INSTANCE.ipv6AddressBytes(ipv6address), 0, 0).getValue());
        assertEquals("::/32", INSTANCE.ipv6PrefixForShort(INSTANCE.ipv6AddressBytes(ipv6address), 0, 32).getValue());

        assertEquals(Map.entry(new Ipv6Address("::"), 32), INSTANCE.splitIpv6Prefix(new Ipv6Prefix("::/32")));
        assertArrayEquals(new byte[] { 0, 10, 0, 0, 0, 0, 0, 0, 0, 11, 0, 12, 0, 13, 0, 14, 64 },
            INSTANCE.ipv6PrefixToBytes(new Ipv6Prefix("A::B:C:D:E/64")));

        // verify that an IPv4-mapped IPv6 address gets parsed as an IPv6 address
        assertEquals("::ffff:ab0:eb", INSTANCE.ipv6AddressFor(
                new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xff, (byte) 0xff, 0x0a, (byte) 0xb0, 0, (byte) 0xeb})
                .getValue());
    }

    @Test
    public void prefixTest() {
        assertEquals(new IpPrefix(new Ipv4Prefix("0.0.0.0/16")),
            INSTANCE.ipPrefixFor(INSTANCE.inetAddressFor(new IpAddress(new Ipv4Address("0.0.0.0"))), 16));
        assertEquals(new IpPrefix(new Ipv6Prefix("::/64")),
            INSTANCE.ipPrefixFor(INSTANCE.inetAddressFor(new IpAddress(new Ipv6Address("::"))), 64));

        assertEquals(new IpPrefix(new Ipv4Prefix("0.0.0.0/16")),
            INSTANCE.ipPrefixFor(new byte[] { 0, 0, 0, 0 }, 16));
        assertEquals(new IpPrefix(new Ipv6Prefix("::/64")),
            INSTANCE.ipPrefixFor(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, 64));
    }

    @Test
    public void inetAddressTest() {
        assertThat(INSTANCE.inetAddressFor(new IpAddress(new Ipv4Address("1.2.3.4"))),
            instanceOf(Inet4Address.class));
        assertThat(INSTANCE.inetAddressFor(new IpAddress(new Ipv6Address("FE80::2002:B3FF:FE1E:8329"))),
            instanceOf(Inet6Address.class));
    }

    @Test
    public void inet4AddressForWithExceptionTest() {
        final Ipv4Address ipClass = mock(Ipv4Address.class);
        doReturn("testClass").when(ipClass).toString();
        doAnswer(inv -> {
            throw new UnknownHostException();
        }).when(ipClass).getValue();

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> INSTANCE.inet4AddressFor(ipClass));
        assertEquals("Invalid address testClass", ex.getMessage());
    }

    @Test
    public void inet6AddressForWithExceptionTest() {
        final Ipv6Address ipClass = mock(Ipv6Address.class);
        doReturn("testClass").when(ipClass).toString();
        doAnswer(inv -> {
            throw new UnknownHostException();
        }).when(ipClass).getValue();

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> INSTANCE.inet6AddressFor(ipClass));
        assertEquals("Invalid address testClass", ex.getMessage());
    }

    @Test
    public void testIpv4AddressForBits() {
        assertEquals("1.2.3.4", INSTANCE.ipv4AddressFor(0x01020304).getValue());
        assertEquals("255.255.255.255", INSTANCE.ipv4AddressFor(0xFFFFFFFF).getValue());
    }

    @Test
    public void testIpv4AddressBits() {
        assertEquals(0x01020304, INSTANCE.ipv4AddressBits(new Ipv4Address("1.2.3.4")));
        assertEquals(0xFFFFFFFF, INSTANCE.ipv4AddressBits(new Ipv4Address("255.255.255.255")));
    }

    @Test
    public void testIpv4AddressNoZoneBits() {
        assertEquals(0x01020304, INSTANCE.ipv4AddressNoZoneBits(new Ipv4AddressNoZone("1.2.3.4")));
        assertEquals(0xFFFFFFFF, INSTANCE.ipv4AddressNoZoneBits(new Ipv4AddressNoZone("255.255.255.255")));
    }

    private static void assertV4Equals(final String literal, final String append) {
        final byte[] expected = InetAddresses.forString(literal).getAddress();
        final byte[] actual = INSTANCE.ipv4AddressBytes(new Ipv4Address(literal + append));
        assertArrayEquals(expected, actual);
    }

    private static void assertV4Equals(final String literal) {
        assertV4Equals(literal, "");
    }

}
