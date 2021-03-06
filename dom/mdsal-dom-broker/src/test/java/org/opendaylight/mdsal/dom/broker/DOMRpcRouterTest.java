/*
 * Copyright (c) 2016, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.concurrent.RejectedExecutionException;
import org.junit.Test;
import org.opendaylight.mdsal.dom.api.DOMRpcAvailabilityListener;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.mdsal.dom.broker.util.TestModel;

public class DOMRpcRouterTest extends TestUtils {

    @Test
    public void registerRpcImplementation() {
        try (DOMRpcRouter rpcRouter = new DOMRpcRouter()) {
            DOMRpcRoutingTable routingTable = rpcRouter.routingTable();
            assertFalse(routingTable.getOperations().containsKey(TestModel.TEST_QNAME));

            rpcRouter.getRpcProviderService().registerRpcImplementation(getTestRpcImplementation(),
                DOMRpcIdentifier.create(TestModel.TEST_QNAME, null));
            routingTable = rpcRouter.routingTable();
            assertTrue(routingTable.getOperations().containsKey(TestModel.TEST_QNAME));

            rpcRouter.getRpcProviderService().registerRpcImplementation(getTestRpcImplementation(),
                DOMRpcIdentifier.create(TestModel.TEST2_QNAME, null));
            routingTable = rpcRouter.routingTable();
            assertTrue(routingTable.getOperations().containsKey(TestModel.TEST2_QNAME));
        }
    }

    @Test
    public void invokeRpc() {
        try (DOMRpcRouter rpcRouter = new DOMRpcRouter()) {
            assertNotNull(rpcRouter.getRpcService().invokeRpc(TestModel.TEST_QNAME, null));
        }
    }

    @Test
    public void registerRpcListener() {
        try (DOMRpcRouter rpcRouter = new DOMRpcRouter()) {
            final DOMRpcAvailabilityListener listener = mock(DOMRpcAvailabilityListener.class);

            final Collection<?> listenersOriginal = rpcRouter.listeners();

            assertNotNull(rpcRouter.getRpcService().registerRpcListener(listener));

            final Collection<?> listenersChanged = rpcRouter.listeners();
            assertNotEquals(listenersOriginal, listenersChanged);
            assertTrue(listenersOriginal.isEmpty());
            assertFalse(listenersChanged.isEmpty());
        }
    }

    @Test
    public void onGlobalContextUpdated() {
        try (DOMRpcRouter rpcRouter = new DOMRpcRouter()) {

            final DOMRpcRoutingTable routingTableOriginal = rpcRouter.routingTable();

            rpcRouter.onModelContextUpdated(TestModel.createTestContext());

            final DOMRpcRoutingTable routingTableChanged = rpcRouter.routingTable();
            assertNotEquals(routingTableOriginal, routingTableChanged);
        }
    }

    @Test
    public void close() {
        final DOMRpcRouter rpcRouter = new DOMRpcRouter();
        rpcRouter.close();

        final DOMRpcProviderService svc = rpcRouter.getRpcProviderService();
        assertThrows(RejectedExecutionException.class, () -> svc.registerRpcImplementation(getTestRpcImplementation(),
            DOMRpcIdentifier.create(TestModel.TEST_QNAME, null)));
    }
}
