/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class IpAddressNoZoneBuilderTest {
    @Test
    void testGetDefaultInstance() {
        testIpv4("1.1.1.1");
        testIpv4("192.168.155.100");
        testIpv4("1.192.1.221");
        testIpv6("ABCD:EF01:2345:6789:ABCD:EF01:2345:6789");
        testIpv6("2001:DB8:0:0:8:800:200C:417A");
        testIpv6("0:0:0:0:0:0:0:0");
        testIpv6("::1.2.3.4");
        testIpv6("ABCD:EF01:2345:6789:ABCD:EF01:2345:6789");
        testIpv6("2001:DB8:0:0:8:800:200C:417A");
        testIpv6("2001:DB8::8:800:200C:417A");
        testIpv6("FF01:0:0:0:0:0:0:101");
        testIpv6("FF01::101");
        testIpv6("0:0:0:0:0:0:0:1");
        testIpv6("::1");
        testIpv6("::");
    }

    @Test
    void testIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> IetfInetUtil.ipAddressNoZoneFor("2001:0DB8::CD3/60"));
    }

    private static void testIpv4(final String ip) {
        assertEquals(new IpAddressNoZone(new Ipv4AddressNoZone(ip)), IetfInetUtil.ipAddressNoZoneFor(ip));
    }

    private static void testIpv6(final String ip) {
        assertEquals(new IpAddressNoZone(new Ipv6AddressNoZone(ip)), IetfInetUtil.ipAddressNoZoneFor(ip));
    }
}