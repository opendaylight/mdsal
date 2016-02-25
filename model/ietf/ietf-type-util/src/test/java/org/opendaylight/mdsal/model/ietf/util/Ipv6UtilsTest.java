/*
 * Copyright (c) 2014 Brocade Communications Systems Inc and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.model.ietf.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Anton Ivanov aivanov@brocade.com
 */
public class Ipv6UtilsTest {
    /*
     * Test canonicalBinaryV6Address
     */
    @Test
    public void canonicalBinaryV6AddressTest() {
        byte [] ipv6binary = Ipv6Utils.canonicalBinaryV6Address("0000:0000:0000:0000:0000:0000:0000:0001");
        byte [] expected = {0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,1};

        Assert.assertArrayEquals(expected, ipv6binary);
    }
}
