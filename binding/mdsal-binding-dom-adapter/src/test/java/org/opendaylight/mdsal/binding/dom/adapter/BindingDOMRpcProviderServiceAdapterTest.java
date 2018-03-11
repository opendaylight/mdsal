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

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Executors;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.adapter.test.util.BindingBrokerTestFactory;
import org.opendaylight.mdsal.binding.dom.adapter.test.util.BindingTestContext;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.rpcservice.rev140701.OpendaylightTestRpcServiceService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.rpcservice.rev140701.RockTheHouseInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.rpcservice.rev140701.RockTheHouseOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class BindingDOMRpcProviderServiceAdapterTest {

    @Test
    public void basicTest() throws Exception {
        final DOMRpcProviderService providerService = mock(DOMRpcProviderService.class);
        final BindingBrokerTestFactory testFactory = new BindingBrokerTestFactory();
        testFactory.setExecutor(Executors.newCachedThreadPool());

        final BindingTestContext testContext = testFactory.getTestContext();
        testContext.start();

        final BindingDOMRpcProviderServiceAdapter adapter =
                new BindingDOMRpcProviderServiceAdapter(providerService, testContext.getCodec());

        assertNotNull(adapter.registerRpcImplementation(OpendaylightTestRpcServiceService.class, new TestImpl()));
        assertNotNull(adapter.registerRpcImplementation(OpendaylightTestRpcServiceService.class, new TestImpl(),
                ImmutableSet.of()));
    }

    private class TestImpl implements OpendaylightTestRpcServiceService {

        @Override
        public ListenableFuture<RpcResult<RockTheHouseOutput>> rockTheHouse(final RockTheHouseInput input) {
            return null;
        }
    }
}