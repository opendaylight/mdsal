/*
 * Copyright (c) 2016, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.opendaylight.mdsal.dom.broker.TestUtils.getTestRpcImplementation;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMActionAvailabilityExtension;
import org.opendaylight.mdsal.dom.api.DOMActionAvailabilityExtension.AvailabilityListener;
import org.opendaylight.mdsal.dom.api.DOMActionImplementation;
import org.opendaylight.mdsal.dom.api.DOMActionInstance;
import org.opendaylight.mdsal.dom.api.DOMActionNotAvailableException;
import org.opendaylight.mdsal.dom.api.DOMActionProviderService;
import org.opendaylight.mdsal.dom.api.DOMActionResult;
import org.opendaylight.mdsal.dom.api.DOMActionService;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcAvailabilityListener;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementationNotAvailableException;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.spi.SimpleDOMActionResult;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextListener;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DOMRpcRouterTest {
    private static final YangInstanceIdentifier BAZ_PATH_BAD = YangInstanceIdentifier.create(
        new NodeIdentifier(Actions.FOO), NodeIdentifierWithPredicates.of(Actions.FOO, Actions.BAR, "bad"));
    private static final YangInstanceIdentifier BAZ_PATH_GOOD = YangInstanceIdentifier.create(
        new NodeIdentifier(Actions.FOO), NodeIdentifierWithPredicates.of(Actions.FOO, Actions.BAR, "good"));

    private static final DOMActionImplementation IMPL =
        (type, path, input) -> Futures.immediateFuture(new SimpleDOMActionResult(
            Builders.containerBuilder().withNodeIdentifier(new NodeIdentifier(Actions.OUTPUT)).build()));

    @Test
    public void registerRpcImplementation() {
        try (DOMRpcRouter rpcRouter = rpcsRouter()) {
            assertOperationKeys(rpcRouter);

            final Registration fooReg = rpcRouter.getRpcProviderService().registerRpcImplementation(
                getTestRpcImplementation(), DOMRpcIdentifier.create(Rpcs.FOO, null));
            assertOperationKeys(rpcRouter, Rpcs.FOO);

            final Registration barReg = rpcRouter.getRpcProviderService().registerRpcImplementation(
                getTestRpcImplementation(), DOMRpcIdentifier.create(Rpcs.BAR, null));
            assertOperationKeys(rpcRouter, Rpcs.FOO, Rpcs.BAR);

            fooReg.close();
            assertOperationKeys(rpcRouter, Rpcs.BAR);
            barReg.close();
            assertOperationKeys(rpcRouter);
        }
    }

    @Test
    public void registerRpcImplementations() {
        try (DOMRpcRouter rpcRouter = rpcsRouter()) {
            assertOperationKeys(rpcRouter);

            final Registration fooReg = rpcRouter.getRpcProviderService().registerRpcImplementations(
                Map.of(DOMRpcIdentifier.create(Rpcs.FOO, null), getTestRpcImplementation()));
            assertOperationKeys(rpcRouter, Rpcs.FOO);

            final Registration barReg = rpcRouter.getRpcProviderService().registerRpcImplementations(
                Map.of(
                    DOMRpcIdentifier.create(Rpcs.BAR, null), getTestRpcImplementation(),
                    DOMRpcIdentifier.create(Rpcs.BAZ, null), getTestRpcImplementation()));
            assertOperationKeys(rpcRouter, Rpcs.FOO, Rpcs.BAR, Rpcs.BAZ);

            fooReg.close();
            assertOperationKeys(rpcRouter, Rpcs.BAR, Rpcs.BAZ);
            barReg.close();
            assertOperationKeys(rpcRouter);
        }
    }


    private static void assertOperationKeys(final DOMRpcRouter router, final QName... keys) {
        assertEquals(Set.of(keys), router.routingTable().getOperations().keySet());
    }

    @Test
    public void testFailedInvokeRpc() {
        try (DOMRpcRouter rpcRouter = rpcsRouter()) {
            final ListenableFuture<?> future = rpcRouter.getRpcService().invokeRpc(Rpcs.FOO, null);
            final Throwable cause = assertThrows(ExecutionException.class, () -> Futures.getDone(future)).getCause();
            assertThat(cause, instanceOf(DOMRpcImplementationNotAvailableException.class));
            assertEquals("No implementation of RPC (rpcs)foo available", cause.getMessage());
        }
    }

    @Test
    public void testRpcListener() {
        try (DOMRpcRouter rpcRouter = new DOMRpcRouter()) {
            assertEquals(List.of(), rpcRouter.listeners());

            final DOMRpcAvailabilityListener listener = mock(DOMRpcAvailabilityListener.class);
            doCallRealMethod().when(listener).acceptsImplementation(any());
            doNothing().when(listener).onRpcAvailable(any());
            doNothing().when(listener).onRpcUnavailable(any());

            final Registration reg = rpcRouter.getRpcService().registerRpcListener(listener);
            assertNotNull(reg);
            assertEquals(List.of(reg), rpcRouter.listeners());

            final Registration implReg = rpcRouter.getRpcProviderService().registerRpcImplementation(
                getTestRpcImplementation(), DOMRpcIdentifier.create(Rpcs.FOO, null));
            verify(listener, timeout(1000)).onRpcAvailable(any());

            implReg.close();
            verify(listener, timeout(1000)).onRpcUnavailable(any());

            reg.close();
            assertEquals(List.of(), rpcRouter.listeners());
        }
    }

    @Test
    public void testActionListener() {
        try (DOMRpcRouter rpcRouter = new DOMRpcRouter()) {
            assertEquals(List.of(), rpcRouter.actionListeners());

            final AvailabilityListener listener = mock(AvailabilityListener.class);
            final Registration reg = rpcRouter.getActionService().getExtensions()
                .getInstance(DOMActionAvailabilityExtension.class).registerAvailabilityListener(listener);
            assertNotNull(reg);
            assertEquals(List.of(reg), rpcRouter.actionListeners());

            // FIXME: register implementation and verify notification

            reg.close();
            assertEquals(List.of(), rpcRouter.actionListeners());
        }
    }

    @Test
    public void onGlobalContextUpdated() {
        try (DOMRpcRouter rpcRouter = new DOMRpcRouter()) {
            final DOMRpcRoutingTable routingTableOriginal = rpcRouter.routingTable();
            rpcRouter.onModelContextUpdated(TestModel.createTestContext());
            assertNotEquals(routingTableOriginal, rpcRouter.routingTable());
        }
    }

    @Test
    public void testClose() {
        final ListenerRegistration<EffectiveModelContextListener> reg = mock(ListenerRegistration.class);
        doNothing().when(reg).close();
        final DOMSchemaService schema = mock(DOMSchemaService.class);
        doReturn(reg).when(schema).registerSchemaContextListener(any());

        final DOMRpcRouter rpcRouter = new DOMRpcRouter(schema);
        rpcRouter.close();

        final DOMRpcProviderService svc = rpcRouter.getRpcProviderService();
        assertThrows(RejectedExecutionException.class, () -> svc.registerRpcImplementation(getTestRpcImplementation(),
            DOMRpcIdentifier.create(Rpcs.FOO, null)));
    }

    @Test
    public void testActionInstanceRouting() throws ExecutionException {
        try (DOMRpcRouter rpcRouter = actionsRouter()) {
            final DOMActionProviderService actionProvider = rpcRouter.getActionProviderService();
            assertNotNull(actionProvider);
            final DOMActionService actionConsumer = rpcRouter.getActionService();
            assertNotNull(actionConsumer);

            try (ObjectRegistration<?> reg = actionProvider.registerActionImplementation(IMPL,
                DOMActionInstance.of(Actions.BAZ_TYPE, LogicalDatastoreType.OPERATIONAL, BAZ_PATH_GOOD))) {

                assertAvailable(actionConsumer, BAZ_PATH_GOOD);
                assertUnavailable(actionConsumer, BAZ_PATH_BAD);
            }

            assertUnavailable(actionConsumer, BAZ_PATH_BAD);
            assertUnavailable(actionConsumer, BAZ_PATH_GOOD);
        }
    }

    @Test
    public void testActionDatastoreRouting() throws ExecutionException {
        try (DOMRpcRouter rpcRouter = actionsRouter()) {
            final DOMActionProviderService actionProvider = rpcRouter.getActionProviderService();
            assertNotNull(actionProvider);
            final DOMActionService actionConsumer = rpcRouter.getActionService();
            assertNotNull(actionConsumer);

            try (ObjectRegistration<?> reg = actionProvider.registerActionImplementation(IMPL,
                DOMActionInstance.of(Actions.BAZ_TYPE, LogicalDatastoreType.OPERATIONAL,
                    YangInstanceIdentifier.empty()))) {

                assertAvailable(actionConsumer, BAZ_PATH_GOOD);
                assertAvailable(actionConsumer, BAZ_PATH_BAD);
            }

            assertUnavailable(actionConsumer, BAZ_PATH_BAD);
            assertUnavailable(actionConsumer, BAZ_PATH_GOOD);
        }
    }

    private static DOMRpcRouter actionsRouter() {
        final DOMRpcRouter router = new DOMRpcRouter();
        router.onModelContextUpdated(Actions.CONTEXT);
        return router;
    }

    private static DOMRpcRouter rpcsRouter() {
        final DOMRpcRouter router = new DOMRpcRouter();
        router.onModelContextUpdated(Rpcs.CONTEXT);
        return router;
    }

    private static void assertAvailable(final DOMActionService actionService, final YangInstanceIdentifier path)
            throws ExecutionException {
        final DOMActionResult result = Futures.getDone(invokeBaz(actionService, path));
        assertEquals(List.of(), result.getErrors());
    }

    private static void assertUnavailable(final DOMActionService actionService, final YangInstanceIdentifier path) {
        final ListenableFuture<? extends DOMActionResult> future = invokeBaz(actionService, path);
        final ExecutionException ex = assertThrows(ExecutionException.class, () -> Futures.getDone(future));
        assertThat(ex.getCause(), instanceOf(DOMActionNotAvailableException.class));
    }

    private static ListenableFuture<? extends DOMActionResult> invokeBaz(final DOMActionService actionService,
            final YangInstanceIdentifier path) {
        return actionService.invokeAction(Actions.BAZ_TYPE,
            new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, path),
            Builders.containerBuilder().withNodeIdentifier(new NodeIdentifier(Actions.INPUT)).build());
    }
}
