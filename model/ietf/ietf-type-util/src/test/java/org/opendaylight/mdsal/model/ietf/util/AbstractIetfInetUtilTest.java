/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.model.ietf.util;

import static org.junit.Assert.assertArrayEquals;
import com.google.common.net.InetAddresses;
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
    public void testIpv4ToBytes() {
        assertV4Equals("1.2.3.4");
        assertV4Equals("12.23.34.45");
        assertV4Equals("255.254.253.252");
        assertV4Equals("128.16.0.127");

        assertV4Equals("1.2.3.4", "%5");
        assertV4Equals("12.23.34.45", "%5");
        assertV4Equals("255.254.253.252", "%5");
        assertV4Equals("128.16.0.127", "%5");
    }
}
