/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.util.concurrent.MoreExecutors;
import java.lang.reflect.Method;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.adapter.test.util.BindingBrokerTestFactory;
import org.opendaylight.mdsal.binding.dom.adapter.test.util.BindingTestContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.rpcservice.rev140701.OpendaylightTestRpcServiceService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.rpcservice.rev140701.RockTheHouseInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.rpcservice.rev140701.RockTheHouseInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.rpc.routing.rev140701.OpendaylightTestRoutedRpcService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.rpc.routing.rev140701.RoutedSimpleRouteInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.rpc.routing.rev140701.RoutedSimpleRouteInputBuilder;
import org.opendaylight.yangtools.yang.binding.RpcService;

public class RpcServiceAdapterTest {

    @SuppressWarnings("checkstyle:IllegalThrows")
    @Test
    public void invoke() throws Throwable {
        final BindingBrokerTestFactory bindingBrokerTestFactory = new BindingBrokerTestFactory();
        bindingBrokerTestFactory.setExecutor(MoreExecutors.newDirectExecutorService());
        final BindingTestContext bindingTestContext = bindingBrokerTestFactory.getTestContext();
        bindingTestContext.start();

        RpcServiceAdapter rpcServiceAdapter = new RpcServiceAdapter(OpendaylightTestRpcServiceService.class,
                bindingTestContext.getCodec(), bindingTestContext.getDomRpcInvoker());

        Method method = TestRpcService.class.getMethod("equals", Object.class);
        assertTrue((boolean) rpcServiceAdapter.invoke(rpcServiceAdapter.facade(), method,
                new Object[]{ rpcServiceAdapter.facade() }));
        assertFalse((boolean) rpcServiceAdapter.invoke(rpcServiceAdapter.facade(), method,
                new Object[]{ new Object() }));

        method = TestRpcService.class.getMethod("hashCode");
        assertEquals(rpcServiceAdapter.facade().hashCode(), rpcServiceAdapter.invoke(rpcServiceAdapter.facade(),
                method, new Object[]{ }));

        method = TestRpcService.class.getMethod("toString");
        assertEquals(rpcServiceAdapter.facade().toString(), rpcServiceAdapter.invoke(rpcServiceAdapter.facade(),
                method, new Object[]{ }));

        method = OpendaylightTestRpcServiceService.class.getMethod("rockTheHouse", RockTheHouseInput.class);
        assertNotNull(rpcServiceAdapter.invoke(rpcServiceAdapter.facade(), method,
            new Object[]{ new RockTheHouseInputBuilder().build() }));

        rpcServiceAdapter = new RpcServiceAdapter(OpendaylightTestRoutedRpcService.class,
                bindingTestContext.getCodec(), bindingTestContext.getDomRpcInvoker());
        method = OpendaylightTestRoutedRpcService.class.getMethod("routedSimpleRoute", RoutedSimpleRouteInput.class);
        assertNotNull(rpcServiceAdapter.invoke(rpcServiceAdapter.facade(), method,
                new Object[]{ new RoutedSimpleRouteInputBuilder().build() }));
    }

    private interface TestRpcService extends RpcService {

        @Override
        String toString();

        @Override
        int hashCode();

        @Override
        boolean equals(Object object);
    }
}