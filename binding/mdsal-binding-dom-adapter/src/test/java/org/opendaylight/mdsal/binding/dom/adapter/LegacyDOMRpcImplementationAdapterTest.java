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

import com.google.common.collect.ImmutableMap;
import java.lang.reflect.Method;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.codec.spi.BindingDOMCodecServices;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.rpcservice.rev140701.OpendaylightTestRpcServiceService;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;

public class LegacyDOMRpcImplementationAdapterTest {
    @Test
    public void basicTest() throws Exception {
        final BindingDOMCodecServices registry = mock(BindingDOMCodecServices.class);
        final Method testMethod = getClass().getDeclaredMethod("testMethod");
        final QName rpcType = QName.create(QNameModule.create(XMLNamespace.of("tst")), "test");
        final LegacyDOMRpcImplementationAdapter adapter = new LegacyDOMRpcImplementationAdapter(
            new ConstantAdapterContext(registry), OpendaylightTestRpcServiceService.class,
            ImmutableMap.of(rpcType, testMethod), mock(OpendaylightTestRpcServiceService.class));
        assertNotNull(adapter);
    }

    private void testMethod() {
        //NOOP
    }
}