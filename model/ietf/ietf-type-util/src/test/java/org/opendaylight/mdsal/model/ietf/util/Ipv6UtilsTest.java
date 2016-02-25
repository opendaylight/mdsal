/*
 * Copyright (c) 2014 Brocade Communications Systems Inc and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.model.ietf.util;

import static com.google.common.net.InetAddresses.forString;
import static org.junit.Assert.assertArrayEquals;
import static org.opendaylight.mdsal.model.ietf.util.Ipv6Utils.canonicalBinaryV6Address;
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

    /**
     * @author Anton Ivanov aivanov@brocade.com
     */
    @Test
    public void testFullQuads() {
        assertEqualResult("0000:0000:0000:0000:0000:0000:0000:0001");
    }

    @Test
    public void testZoneIndex() {
        assertArrayEquals(forString("::1").getAddress(), canonicalBinaryV6Address("::1%2"));
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

    // Utility for quick comparison with Guava
    private static void assertEqualResult(final String str) {
        assertArrayEquals(forString(str).getAddress(), canonicalBinaryV6Address(str));
    }
}
