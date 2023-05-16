/*
 * Copyright (c) 2014 Brocade Communications Systems Inc and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.model.ietf.type.util;

import static com.google.common.net.InetAddresses.forString;
import static org.junit.Assert.assertArrayEquals;
import static org.opendaylight.mdsal.model.ietf.type.util.Ipv6Utils.fillIpv6Bytes;

import org.junit.Test;

public class Ipv6UtilsTest {

    @Test
    public void testDiscards() {
        assertEqualResult("2001:0000:3238:DFE1:63:0000:0000:FEFB");
        assertEqualResult("2001:0000:3238:DFE1:63::FEFB");
        assertEqualResult("2001:0:3238:DFE1:63::FEFB");
        assertEqualResult("::1");
        assertEqualResult("::");
    }

    @Test
    public void testFullQuads() {
        assertEqualResult("0000:0000:0000:0000:0000:0000:0000:0001");
    }

    @Test
    public void testRfc4291() {
        assertEqualResult("ABCD:EF01:2345:6789:ABCD:EF01:2345:6789");
        assertEqualResult("2001:DB8:0:0:8:800:200C:417A");
        assertEqualResult("2001:DB8::8:800:200C:417A");
        assertEqualResult("FF01:0:0:0:0:0:0:101");
        assertEqualResult("FF01::101");
        assertEqualResult("0:0:0:0:0:0:0:1");
        assertEqualResult("::1");
        assertEqualResult("0:0:0:0:0:0:0:0");
        assertEqualResult("::");

        final byte[] test1 = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 1, 68, 3 };
        assertArrayEquals(test1, bytesForString("0:0:0:0:0:0:13.1.68.3"));
        assertArrayEquals(test1, bytesForString("::13.1.68.3"));

        final byte[] test2 = new byte[] {
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, (byte)255, (byte)255, (byte)129, (byte)144, 52, 38
        };
        assertArrayEquals(test2, bytesForString("0:0:0:0:0:FFFF:129.144.52.38"));
        assertArrayEquals(test2, bytesForString("::FFFF:129.144.52.38"));
    }

    @Test
    public void testRfc5952leadingZeroes() {
        assertEqualResult("2001:db8:aaaa:bbbb:cccc:dddd:eeee:0001");
        assertEqualResult("2001:db8:aaaa:bbbb:cccc:dddd:eeee:001");
        assertEqualResult("2001:db8:aaaa:bbbb:cccc:dddd:eeee:01");
        assertEqualResult("2001:db8:aaaa:bbbb:cccc:dddd:eeee:1");
    }

    @Test
    public void testRfc5952zeroCompression() {
        assertEqualResult("2001:db8:aaaa:bbbb:cccc:dddd::1");
        assertEqualResult("2001:db8:aaaa:bbbb:cccc:dddd:0:1");
        assertEqualResult("2001:db8:0:0:0::1");
        assertEqualResult("2001:db8:0:0::1");
        assertEqualResult("2001:db8:0::1");
        assertEqualResult("2001:db8::1");
        assertEqualResult("2001:db8::aaaa:0:0:1");
        assertEqualResult("2001:db8:0:0:aaaa::1");
    }

    @Test
    public void testRfc5952upperLowerCase() {
        assertEqualResult("2001:db8:aaaa:bbbb:cccc:dddd:eeee:aaaa");
        assertEqualResult("2001:db8:aaaa:bbbb:cccc:dddd:eeee:AAAA");
        assertEqualResult("2001:db8:aaaa:bbbb:cccc:dddd:eeee:AaAa");
    }

    @Test
    public void testRfc5952specials() {
        // Can't use Guava for these, as it will return an IPv4 address
        assertArrayEquals(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xff, (byte) 0xff, (byte)192, 0, 2, 1 },
            bytesForString("::ffff:192.0.2.1"));
        assertArrayEquals(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xff, (byte) 0xff, (byte)192, 0, 2, 1 },
            bytesForString("0:0:0:0:0:ffff:192.0.2.1"));
    }

    @Test
    public void testRfc6052() {
        assertEqualResult("2001:db8:c000:221::");
        assertEqualResult("2001:db8:1c0:2:21::");
        assertEqualResult("2001:db8:122:c000:2:2100::");
        assertEqualResult("2001:db8:122:3c0:0:221::");
        assertEqualResult("2001:db8:122:344:c0:2:2100::");
        assertEqualResult("2001:db8:122:344::192.0.2.33");

        assertEqualResult("64:ff9b::192.0.2.33");
    }

    private static byte[] bytesForString(final String str) {
        final byte[] bytes = new byte[16];
        fillIpv6Bytes(bytes, str, str.length());
        return bytes;
    }

    // Utility for quick comparison with Guava
    private static void assertEqualResult(final String str) {
        assertArrayEquals(forString(str).getAddress(), bytesForString(str));
    }
}
