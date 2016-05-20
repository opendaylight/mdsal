/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.concurrent.RejectedExecutionException;
import org.junit.Test;
import org.opendaylight.mdsal.dom.api.DOMRpcAvailabilityListener;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.broker.test.util.TestModel;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class DOMRpcRouterTest extends TestUtils {

    @Test
    public void registerRpcImplementation() throws Exception {
        final DOMRpcRouter rpcRouter = new DOMRpcRouter();
        final Field routingTableField = DOMRpcRouter.class.getDeclaredField("routingTable");
        routingTableField.setAccessible(true);
        DOMRpcRoutingTable routingTable = (DOMRpcRoutingTable) routingTableField.get(rpcRouter);
        assertFalse(routingTable.getRpcs().containsKey(SchemaPath.ROOT));

        rpcRouter.registerRpcImplementation(getTestRpcImplementation(), DOMRpcIdentifier.create(SchemaPath.ROOT, null));
        routingTable = (DOMRpcRoutingTable) routingTableField.get(rpcRouter);
        assertTrue(routingTable.getRpcs().containsKey(SchemaPath.ROOT));

        rpcRouter.registerRpcImplementation(getTestRpcImplementation(), DOMRpcIdentifier.create(SchemaPath.SAME, null));
        routingTable = (DOMRpcRoutingTable) routingTableField.get(rpcRouter);
        assertTrue(routingTable.getRpcs().containsKey(SchemaPath.SAME));
    }

    @Test
    public void invokeRpc() throws Exception {
        final DOMRpcRouter rpcRouter = new DOMRpcRouter();
        assertNotNull(rpcRouter.invokeRpc(SchemaPath.create(false, TestModel.TEST_QNAME), null));
    }

    @Test
    public void registerRpcListener() throws Exception {
        final DOMRpcRouter rpcRouter = new DOMRpcRouter();
        final DOMRpcAvailabilityListener listener = mock(DOMRpcAvailabilityListener.class);

        final Field listenersField = DOMRpcRouter.class.getDeclaredField("listeners");
        listenersField.setAccessible(true);
        Collection<ListenerRegistration<? extends DOMRpcAvailabilityListener>> listenersOriginal =
                (Collection<ListenerRegistration<? extends DOMRpcAvailabilityListener>>) listenersField.get(rpcRouter);

        assertNotNull(rpcRouter.registerRpcListener(listener));

        Collection<ListenerRegistration<? extends DOMRpcAvailabilityListener>> listenersChanged =
                (Collection<ListenerRegistration<? extends DOMRpcAvailabilityListener>>) listenersField.get(rpcRouter);
        assertNotEquals(listenersOriginal, listenersChanged);
        assertTrue(listenersOriginal.isEmpty());
        assertFalse(listenersChanged.isEmpty());
    }

    @Test
    public void onGlobalContextUpdated() throws Exception {
        final DOMRpcRouter rpcRouter = new DOMRpcRouter();
        final Field routingTableField = DOMRpcRouter.class.getDeclaredField("routingTable");
        routingTableField.setAccessible(true);

        final DOMRpcRoutingTable routingTableOriginal = (DOMRpcRoutingTable) routingTableField.get(rpcRouter);

        rpcRouter.onGlobalContextUpdated(TestModel.createTestContext());

        final DOMRpcRoutingTable routingTableChanged = (DOMRpcRoutingTable) routingTableField.get(rpcRouter);
        assertNotEquals(routingTableOriginal, routingTableChanged);
    }

    @Test(expected = RejectedExecutionException.class)
    public void close() throws Exception {
        final DOMRpcRouter rpcRouter = new DOMRpcRouter();
        rpcRouter.close();
        rpcRouter.registerRpcImplementation(getTestRpcImplementation(), DOMRpcIdentifier.create(SchemaPath.ROOT, null));
    }
}