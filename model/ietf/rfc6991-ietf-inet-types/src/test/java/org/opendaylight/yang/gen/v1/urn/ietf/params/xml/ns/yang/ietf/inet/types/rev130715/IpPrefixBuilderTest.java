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

class IpPrefixBuilderTest {
    @Test
    void testGetDefaultInstance() {
        testIpv6("ff00::/8");
        testIpv4("192.0.2.1/24");
    }

    @Test
    void testIllegalArgumentException1() {
        assertThrows(IllegalArgumentException.class, () -> IetfInetUtil.ipPrefixFor("badIp"));
    }

    private static void testIpv4(final String ip) {
        assertEquals(new IpPrefix(new Ipv4Prefix(ip)), IetfInetUtil.ipPrefixFor(ip));
    }

    private static void testIpv6(final String ip) {
        assertEquals(new IpPrefix(new Ipv6Prefix(ip)), IetfInetUtil.ipPrefixFor(ip));
    }
}