/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class IetfInetUtilTest {
    @Test
    public void testIpv4AddressFor() {
        final byte[] bytes = new byte[] { 1, 2, 3, 4 };

        Ipv4Address address = IetfInetUtil.ipv4AddressFor(bytes);
        assertEquals("1.2.3.4", address.getValue());
    }

    @Test
    public void testIpv4PrefixFor() {
        final byte[] bytes = new byte[] { 1, 2, 3, 4 };

        Ipv4Prefix address = IetfInetUtil.ipv4PrefixFor(bytes);
        assertEquals("1.2.3.4/32", address.getValue());
    }

    @Test
    public void testIpv4PrefixForMask() {
        final byte[] bytes = new byte[] { 1, 2, 3, 4 };

        Ipv4Prefix address = IetfInetUtil.ipv4PrefixFor(bytes, 24);
        assertEquals("1.2.3.4/24", address.getValue());
    }
}
