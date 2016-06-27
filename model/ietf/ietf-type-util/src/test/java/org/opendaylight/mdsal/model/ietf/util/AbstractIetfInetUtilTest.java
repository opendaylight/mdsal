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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.net.InetAddresses;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
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
    public void testIpToBytesAndBack() throws Exception {
        assertV4Equals("1.2.3.4");
        assertV4Equals("12.23.34.45");
        assertV4Equals("255.254.253.252");
        assertV4Equals("128.16.0.127");

        assertV4Equals("1.2.3.4", "%5");
        assertV4Equals("12.23.34.45", "%5");
        assertV4Equals("255.254.253.252", "%5");
        assertV4Equals("128.16.0.127", "%5");

        assertEquals(new IpClass("1.2.3.4")._value.toLowerCase(),
                UTIL.ipAddressFor(UTIL.ipv4AddressBytes(new IpClass("1.2.3.4")))._value.toLowerCase());
        assertNotEquals(new IpClass("2.3.4.5")._value.toLowerCase(),
                UTIL.ipAddressFor(UTIL.ipv4AddressBytes(new IpClass("1.2.3.4")))._value.toLowerCase());

        assertEquals(new IpClass("FE80::2002:B3FF:FE1E:8329")._value.toLowerCase(),
                UTIL.ipAddressFor(UTIL.ipv6AddressBytes(new IpClass("FE80::2002:B3FF:FE1E:8329")))._value.toLowerCase());
        assertNotEquals(new IpClass("FEFF::2002:B3FF:FE1E:8329")._value.toLowerCase(),
                UTIL.ipAddressFor(UTIL.ipv6AddressBytes(new IpClass("FE80::2002:B3FF:FE1E:8329")))._value.toLowerCase());

        assertEquals(new IpClass("1.2.3.4")._value.toLowerCase(),
                UTIL.ipAddressFor(UTIL.inetAddressFor(new IpClass("1.2.3.4")))._value.toLowerCase());
        assertNotEquals(new IpClass("2.3.4.5")._value.toLowerCase(),
                UTIL.ipAddressFor(UTIL.inetAddressFor(new IpClass("1.2.3.4")))._value.toLowerCase());

        assertEquals(new IpClass("FE80::2002:B3FF:FE1E:8329")._value.toLowerCase(),
                UTIL.ipAddressFor(UTIL.inetAddressFor(new IpClass("FE80::2002:B3FF:FE1E:8329")))._value.toLowerCase());
        assertNotEquals(new IpClass("FEFF::2002:B3FF:FE1E:8329")._value.toLowerCase(),
                UTIL.ipAddressFor(UTIL.inetAddressFor(new IpClass("FE80::2002:B3FF:FE1E:8329")))._value.toLowerCase());
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArrayLengthForAddressTest() throws Exception {
        UTIL.ipAddressFor(new byte[] { 0, 0, 0 });
    }

    @Test(expected = IllegalArgumentException.class)
    public void unhandledAddressTest() throws Exception {
        final InetAddress adr = mock(InetAddress.class);
        doReturn("testAddress").when(adr).toString();
        UTIL.ipAddressFor(adr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArrayLengthforPrefixTest() throws Exception {
        UTIL.ipPrefixFor(new byte[] { 0, 0, 0 }, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalAddressforPrefixTest() throws Exception {
        final InetAddress adr = mock(InetAddress.class);
        doReturn("testAddress").when(adr).toString();
        UTIL.ipPrefixFor(adr, 0);
    }

    @Test
    public void ipv4Tests() throws Exception {
        IpClass ipClass = new IpClass("1.2.3.4/16");
        assertEquals("1.2.3.4", UTIL.ipv4AddressFrom(ipClass)._value);
        assertTrue(UTIL.ipv4PrefixFor(UTIL.ipv4AddressBytes(ipClass))._value.contains("/32"));
        ipClass = new IpClass("1.2.3.4");
        assertTrue(UTIL.ipv4PrefixFor(UTIL.inetAddressFor(ipClass))._value.contains("/32"));
        assertTrue(UTIL.ipv4PrefixFor(ipClass)._value.contains("/32"));
        assertTrue(UTIL.ipv4PrefixFor(ipClass, 16)._value.contains("/16"));

        assertTrue(UTIL.ipv4PrefixForShort(UTIL.ipv4AddressBytes(ipClass), 0)._value.equals("0.0.0.0/0"));
        assertTrue(UTIL.ipv4PrefixForShort(UTIL.ipv4AddressBytes(ipClass), 32)._value.equals("1.2.3.4/32"));
        assertTrue(UTIL.ipv4PrefixForShort(UTIL.ipv4AddressBytes(ipClass), 0, 0)._value.equals("0.0.0.0/0"));
        assertTrue(UTIL.ipv4PrefixForShort(UTIL.ipv4AddressBytes(ipClass), 0, 32)._value.equals("1.2.3.4/32"));
        assertTrue(UTIL.ipv4PrefixForShort(new byte[] { 1, 2, 3, 4, 5 }, 1, 32)._value.equals("2.3.4.5/32"));

        assertTrue(UTIL.splitIpv4Prefix(new IpClass("1.2.3.4/16")).getKey()._value.equals("1.2.3.4"));
        assertTrue(UTIL.splitIpv4Prefix(new IpClass("1.2.3.4/16")).getValue().equals(16));
        assertArrayEquals(new byte[] { 1,2,3,4,16 }, UTIL.ipv4PrefixToBytes(new IpClass("1.2.3.4/16")));
    }

    @Test
    public void ipv6Tests() throws Exception {
        IpClass ipClass = new IpClass("::0/128");
        assertEquals("::0", UTIL.ipv6AddressFrom(ipClass)._value);
        ipClass = new IpClass("::0");
        assertTrue(UTIL.ipv6PrefixFor(UTIL.ipv6AddressBytes(ipClass))._value.contains("/128"));
        assertTrue(UTIL.ipv6PrefixFor(UTIL.inetAddressFor(ipClass))._value.contains("/128"));
        assertTrue(UTIL.ipv6PrefixFor(ipClass)._value.contains("/128"));
        assertTrue(UTIL.ipv6PrefixFor(ipClass, 16)._value.contains("/16"));

        assertTrue(UTIL.ipv6PrefixForShort(UTIL.ipv6AddressBytes(ipClass), 0)._value.equals("::0/0"));
        assertTrue(UTIL.ipv6PrefixForShort(UTIL.ipv6AddressBytes(ipClass), 64)._value.equals("::/64"));
        assertTrue(UTIL.ipv6PrefixForShort(UTIL.ipv6AddressBytes(ipClass), 0, 0)._value.equals("::0/0"));
        assertTrue(UTIL.ipv6PrefixForShort(UTIL.ipv6AddressBytes(ipClass), 0, 32)._value.equals("::/32"));

        assertTrue(UTIL.splitIpv6Prefix(new IpClass("::/32")).getKey()._value.equals("::"));
        assertTrue(UTIL.splitIpv6Prefix(new IpClass("::/32")).getValue().equals(32));
        assertArrayEquals(new byte[] { 0, 10, 0, 0, 0, 0, 0, 0, 0, 11, 0, 12, 0, 13, 0, 14, 64 },
                UTIL.ipv6PrefixToBytes(new IpClass("A::B:C:D:E/64")));
    }

    @Test
    public void prefixTest() throws Exception {
        assertTrue(UTIL.ipPrefixFor(UTIL.inetAddressFor(new IpClass("0.0.0.0")), 16)._value.equals("0.0.0.0/16"));
        assertTrue(UTIL.ipPrefixFor(UTIL.inetAddressFor(new IpClass("::")), 64)
                ._value.equals("::/64"));

        assertTrue(UTIL.ipPrefixFor(new byte[] { 0, 0, 0, 0 }, 16)._value.equals("0.0.0.0/16"));
        assertTrue(UTIL.ipPrefixFor(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, 64)
                ._value.equals("::/64"));
    }

    @Test
    public void inetAddressTest() throws Exception {
        assertTrue(UTIL.inetAddressFor(new IpClass("1.2.3.4")) instanceof Inet4Address);
        assertTrue(UTIL.inetAddressFor(new IpClass("FE80::2002:B3FF:FE1E:8329")) instanceof Inet6Address);
    }
}
