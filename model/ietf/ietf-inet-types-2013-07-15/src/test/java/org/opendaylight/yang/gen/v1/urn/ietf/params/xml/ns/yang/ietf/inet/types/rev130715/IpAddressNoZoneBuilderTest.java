/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Constructor;
import org.junit.Test;

public class IpAddressNoZoneBuilderTest {

    @Test
    public void testGetDefaultInstance() throws Exception {
        final Constructor<IpAddressNoZoneBuilder> constructor = IpAddressNoZoneBuilder.class.getDeclaredConstructor();
        assertFalse(constructor.isAccessible());
        constructor.setAccessible(true);
        final IpAddressNoZoneBuilder newInstance = constructor.newInstance();
        assertNotNull(newInstance);

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

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgumentException() {
        IpAddressNoZoneBuilder.getDefaultInstance("2001:0DB8::CD3/60");
    }

    private static void testIpv4(final String ip) {
        final IpAddressNoZone defaultInstance = IpAddressNoZoneBuilder.getDefaultInstance(ip);
        assertEquals(new IpAddressNoZone(new Ipv4AddressNoZone(ip)), defaultInstance);
    }

    private static void testIpv6(final String ip) {
        final IpAddressNoZone defaultInstance = IpAddressNoZoneBuilder.getDefaultInstance(ip);
        assertEquals(new IpAddressNoZone(new Ipv6AddressNoZone(ip)), defaultInstance);
    }
}