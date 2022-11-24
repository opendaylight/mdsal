/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.Map;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.codec.spi.BindingDOMCodecServices;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.rpcservice.rev140701.OpendaylightTestRpcServiceService;
import org.opendaylight.yangtools.yang.common.QName;

@Deprecated(since = "11.0.0", forRemoval = true)
public class LegacyDOMRpcImplementationAdapterTest {
    @Test
    public void basicTest() throws Exception {
        final var codecServices = mock(BindingDOMCodecServices.class);
        final var testMethod = getClass().getDeclaredMethod("testMethod");
        final var rpcType = QName.create("tst", "test");
        final var adapter = new LegacyDOMRpcImplementationAdapter<>(new ConstantAdapterContext(codecServices),
            OpendaylightTestRpcServiceService.class, Map.of(rpcType, testMethod),
            mock(OpendaylightTestRpcServiceService.class));
        assertNotNull(adapter);
    }

    private void testMethod() {
        //NOOP
    }
}