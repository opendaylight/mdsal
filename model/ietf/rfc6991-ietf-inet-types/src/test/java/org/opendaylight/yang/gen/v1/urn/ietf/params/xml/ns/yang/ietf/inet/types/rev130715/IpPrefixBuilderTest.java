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

public class IpPrefixBuilderTest {

    @Test
    public void testGetDefaultInstance() throws Exception {
        final Constructor<IpPrefixBuilder> constructor = IpPrefixBuilder.class.getDeclaredConstructor();
        assertFalse(constructor.isAccessible());
        constructor.setAccessible(true);
        final IpPrefixBuilder newInstance = constructor.newInstance();
        assertNotNull(newInstance);

        testIpv6("ff00::/8");
        testIpv4("192.0.2.1/24");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgumentException1() {
        IpPrefixBuilder.getDefaultInstance("badIp");
    }

    private static void testIpv4(final String ip) {
        final IpPrefix defaultInstance = IpPrefixBuilder.getDefaultInstance(ip);
        assertEquals(new IpPrefix(new Ipv4Prefix(ip)), defaultInstance);
    }

    private static void testIpv6(final String ip) {
        final IpPrefix defaultInstance = IpPrefixBuilder.getDefaultInstance(ip);
        assertEquals(new IpPrefix(new Ipv6Prefix(ip)), defaultInstance);
    }
}