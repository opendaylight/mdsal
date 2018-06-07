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

public class IetfInetUtilTest {

    @Test
    public void testIetfInetUtilConstructor() throws Exception {
        final Constructor<IetfInetUtil> constructor = IetfInetUtil.class.getDeclaredConstructor();
        assertFalse(constructor.isAccessible());
        constructor.setAccessible(true);

        final IetfInetUtil newInstance = constructor.newInstance();
        assertNotNull(newInstance);
    }

    @Test
    public void testIpv4Address() {
        final Ipv4Address ipv4Address = new Ipv4Address("192.168.1.1");
        final Ipv4Prefix ipv4Prefix = new Ipv4Prefix("192.0.2.1/24");
        final IpAddress ipAddress = IetfInetUtil.INSTANCE.ipv4Address(ipv4Address);
        final String ipv4AddressString = IetfInetUtil.INSTANCE.ipv4AddressString(ipv4Address);
        final Ipv4Address maybeIpv4Address = IetfInetUtil.INSTANCE.maybeIpv4Address(new IpAddress(ipv4Address));
        final IpPrefix ipPrefix = IetfInetUtil.INSTANCE.ipv4Prefix(ipv4Prefix);
        final String ipv4PrefixString = IetfInetUtil.INSTANCE.ipv4PrefixString(ipv4Prefix);

        assertEquals(ipv4PrefixString, ipPrefix.getIpv4Prefix().getValue());
        assertNotNull(maybeIpv4Address);
        assertEquals(ipv4AddressString, maybeIpv4Address.getValue());
        assertEquals(ipAddress, new IpAddress(ipv4Address));
    }

    @Test
    public void testIpv6Address() {
        final Ipv6Address ipv6Address = new Ipv6Address("ABCD:EF01:2345:6789:ABCD:EF01:2345:6789");
        final Ipv6Prefix ipv6Prefix = new Ipv6Prefix("ff00::/8");
        final IpAddress ipAddress = IetfInetUtil.INSTANCE.ipv6Address(ipv6Address);
        final String ipv6AddressString = IetfInetUtil.INSTANCE.ipv6AddressString(ipv6Address);
        final Ipv6Address maybeIpv6Address = IetfInetUtil.INSTANCE.maybeIpv6Address(new IpAddress(ipv6Address));
        final IpPrefix ipPrefix = IetfInetUtil.INSTANCE.ipv6Prefix(ipv6Prefix);
        final String ipv6PrefixString = IetfInetUtil.INSTANCE.ipv6PrefixString(ipv6Prefix);

        assertEquals(ipv6PrefixString, ipPrefix.getIpv6Prefix().getValue());
        assertNotNull(maybeIpv6Address);
        assertEquals(ipv6AddressString, maybeIpv6Address.getValue());
        assertEquals(ipAddress, new IpAddress(ipv6Address));
    }
}