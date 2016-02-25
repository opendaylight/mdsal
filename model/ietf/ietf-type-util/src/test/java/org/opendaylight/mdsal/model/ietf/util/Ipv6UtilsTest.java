/*
 * Copyright (c) 2014 Brocade Communications Systems Inc and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.model.ietf.util;

import com.google.common.net.InetAddresses;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Anton Ivanov aivanov@brocade.com
 */
public class Ipv6UtilsTest {
    @Test
    public void testFullQuads() {
        byte [] ipv6binary = Ipv6Utils.bytesForString("0000:0000:0000:0000:0000:0000:0000:0001");
        Assert.assertArrayEquals(InetAddresses.forString("::1").getAddress(), ipv6binary);
    }
}
