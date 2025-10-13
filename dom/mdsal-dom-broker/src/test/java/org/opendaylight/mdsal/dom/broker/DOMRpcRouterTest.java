/*
 * Copyright (c) 2016, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.opendaylight.mdsal.dom.api.DOMActionService;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcAvailabilityListener;
import org.opendaylight.mdsal.dom.api.DOMRpcException;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementationNotAvailableException;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.api.DefaultDOMRpcException;
import org.opendaylight.mdsal.dom.spi.DefaultDOMRpcResult;
import org.opendaylight.mdsal.dom.spi.FixedDOMSchemaService;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DOMRpcRouterTest {
    private static final YangInstanceIdentifier BAZ_PATH_BAD = YangInstanceIdentifier.of(
        new NodeIdentifier(Actions.FOO), NodeIdentifierWithPredicates.of(Actions.FOO, Actions.BAR, "bad"));
    private static final YangInstanceIdentifier BAZ_PATH_GOOD = YangInstanceIdentifier.of(
        new NodeIdentifier(Actions.FOO), NodeIdentifierWithPredicates.of(Actions.FOO, Actions.BAR, "good"));

    private static final DOMActionImplementation IMPL =
        (type, path, input) -> Futures.immediateFuture(new DefaultDOMRpcResult(
            ImmutableNodes.newContainerBuilder().withNodeIdentifier(new NodeIdentifier(Actions.OUTPUT)).build()));

    @Test
    public void registerRpcImplementation() {
        try (var rpcRouter = rpcsRouter()) {
            assertOperationKeys(rpcRouter);

            final var svc = new RouterDOMRpcProviderService(rpcRouter);

            final var fooReg = svc.registerRpcImplementation(getTestRpcImplementation(),
                DOMRpcIdentifier.create(Rpcs.FOO, null));
            assertOperationKeys(rpcRouter, Rpcs.FOO);

            final var barReg = svc.registerRpcImplementation(getTestRpcImplementation(),
                DOMRpcIdentifier.create(Rpcs.BAR, null));
            assertOperationKeys(rpcRouter, Rpcs.FOO, Rpcs.BAR);

            fooReg.close();
            assertOperationKeys(rpcRouter, Rpcs.BAR);
            barReg.close();
            assertOperationKeys(rpcRouter);
        }
    }

    @Test
    public void registerRpcImplementations() {
        try (var rpcRouter = rpcsRouter()) {
            assertOperationKeys(rpcRouter);

            final var svc = new RouterDOMRpcProviderService(rpcRouter);

            final var fooReg = svc.registerRpcImplementations(
                Map.of(DOMRpcIdentifier.create(Rpcs.FOO, null), getTestRpcImplementation()));
            assertOperationKeys(rpcRouter, Rpcs.FOO);

            final var barReg = svc.registerRpcImplementations(Map.of(
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
        try (var rpcRouter = rpcsRouter()) {
            final var input = ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(Actions.INPUT))
                .build();
            final var thrown = new RuntimeException("mumble-mumble");

            final var rpcService = new RouterDOMRpcService(rpcRouter);
            final var rpcProviderService = new RouterDOMRpcProviderService(rpcRouter);

            try (var reg = rpcProviderService.registerRpcImplementation(
                    (rpc, unused) -> {
                        throw thrown;
                    }, DOMRpcIdentifier.create(Rpcs.FOO))) {

                final var future = rpcService.invokeRpc(Rpcs.FOO, input);
                final var ee = assertThrows(ExecutionException.class, () -> Futures.getDone(future));
                final var cause = assertInstanceOf(DefaultDOMRpcException.class, ee.getCause());
                assertEquals("RPC implementation failed: java.lang.RuntimeException: mumble-mumble",
                    cause.getMessage());
                assertSame(thrown, cause.getCause());
            }

            final var future = rpcService.invokeRpc(Rpcs.FOO, input);
            final var ee = assertThrows(ExecutionException.class, () -> Futures.getDone(future));
            final var cause = assertInstanceOf(DOMRpcImplementationNotAvailableException.class, ee.getCause());
            assertEquals("No implementation of RPC (rpcs)foo available", cause.getMessage());
        }
    }

    @Test
    public void testRpcListener() {
        try (var rpcRouter = rpcsRouter()) {
            assertEquals(List.of(), rpcRouter.listeners());

            final var rpcService = new RouterDOMRpcService(rpcRouter);
            final var rpcProviderService = new RouterDOMRpcProviderService(rpcRouter);

            final var listener = mock(DOMRpcAvailabilityListener.class);
            doCallRealMethod().when(listener).acceptsImplementation(any());
            doNothing().when(listener).onRpcAvailable(any());
            doNothing().when(listener).onRpcUnavailable(any());

            final var reg = rpcService.registerRpcListener(listener);
            assertNotNull(reg);
            assertEquals(List.of(reg), rpcRouter.listeners());

            final var implReg = rpcProviderService.registerRpcImplementation(getTestRpcImplementation(),
                DOMRpcIdentifier.create(Rpcs.FOO, null));
            verify(listener, timeout(1000)).onRpcAvailable(any());

            implReg.close();
            verify(listener, timeout(1000)).onRpcUnavailable(any());

            reg.close();
            assertEquals(List.of(), rpcRouter.listeners());
        }
    }

    @Test
    public void testActionListener() {
        try (var rpcRouter = actionsRouter()) {
            assertEquals(List.of(), rpcRouter.actionListeners());
            final var actionService = new RouterDOMActionService(rpcRouter);

            final var listener = mock(AvailabilityListener.class);
            final var availability = actionService.extension(DOMActionAvailabilityExtension.class);
            assertNotNull(availability);

            try (var reg = availability.registerAvailabilityListener(listener)) {
                assertNotNull(reg);
                assertEquals(List.of(reg), rpcRouter.actionListeners());

                // FIXME: register implementation and verify notification

            }
            assertEquals(List.of(), rpcRouter.actionListeners());
        }
    }

    @Test
    public void onGlobalContextUpdated() {
        try (var rpcRouter = rpcsRouter()) {
            final DOMRpcRoutingTable routingTableOriginal = rpcRouter.routingTable();
            rpcRouter.onModelContextUpdated(TestModel.createTestContext());
            assertNotEquals(routingTableOriginal, rpcRouter.routingTable());
        }
    }

    @Test
    public void testClose() {
        final var reg = mock(Registration.class);
        doNothing().when(reg).close();
        final var schema = mock(DOMSchemaService.class);
        doReturn(reg).when(schema).registerSchemaContextListener(any());

        final var rpcRouter = new DOMRpcRouter(schema);
        rpcRouter.close();

        final var svc = new RouterDOMRpcProviderService(rpcRouter);
        assertThrows(RejectedExecutionException.class, () -> svc.registerRpcImplementation(getTestRpcImplementation(),
            DOMRpcIdentifier.create(Rpcs.FOO, null)));
    }

    @Test
    public void testActionInstanceRouting() throws ExecutionException {
        try (var rpcRouter = actionsRouter()) {
            final var actionProvider = new RouterDOMActionProviderService(rpcRouter);
            final var actionConsumer = new RouterDOMActionService(rpcRouter);

            try (var reg = actionProvider.registerActionImplementation(IMPL,
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
        try (var rpcRouter = actionsRouter()) {
            final var actionProvider = new RouterDOMActionProviderService(rpcRouter);
            final var actionConsumer = new RouterDOMActionService(rpcRouter);

            try (var reg = actionProvider.registerActionImplementation(IMPL,
                DOMActionInstance.of(Actions.BAZ_TYPE, LogicalDatastoreType.OPERATIONAL,
                    YangInstanceIdentifier.of()))) {

                assertAvailable(actionConsumer, BAZ_PATH_GOOD);
                assertAvailable(actionConsumer, BAZ_PATH_BAD);
            }

            assertUnavailable(actionConsumer, BAZ_PATH_BAD);
            assertUnavailable(actionConsumer, BAZ_PATH_GOOD);
        }
    }

    @Test
    public void testActionInstanceThrowing() throws ExecutionException {
        try (var rpcRouter = actionsRouter()) {
            final var actionProvider = new RouterDOMActionProviderService(rpcRouter);
            final var actionConsumer = new RouterDOMActionService(rpcRouter);

            final var thrown = new RuntimeException("test-two-three");

            try (var reg = actionProvider.registerActionImplementation(
                (type, path, input) -> {
                    throw thrown;
                }, DOMActionInstance.of(Actions.BAZ_TYPE, LogicalDatastoreType.OPERATIONAL, BAZ_PATH_GOOD))) {

                final var future = invokeBaz(actionConsumer, BAZ_PATH_GOOD);
                final var ee = assertThrows(ExecutionException.class, () -> Futures.getDone(future));
                final var cause = assertInstanceOf(DOMRpcException.class, ee.getCause());
                assertEquals("Action implementation failed: java.lang.RuntimeException: test-two-three",
                    cause.getMessage());
                assertSame(thrown, cause.getCause());
            }
        }
    }

    private static DOMRpcRouter actionsRouter() {
        return new DOMRpcRouter(new FixedDOMSchemaService(Actions.CONTEXT));
    }

    private static DOMRpcRouter rpcsRouter() {
        return new DOMRpcRouter(new FixedDOMSchemaService(Rpcs.CONTEXT));
    }

    private static void assertAvailable(final DOMActionService actionService, final YangInstanceIdentifier path)
            throws ExecutionException {
        final var result = Futures.getDone(invokeBaz(actionService, path));
        assertEquals(List.of(), result.errors());
    }

    private static void assertUnavailable(final DOMActionService actionService, final YangInstanceIdentifier path) {
        final var future = invokeBaz(actionService, path);
        final var ee = assertThrows(ExecutionException.class, () -> Futures.getDone(future));
        assertInstanceOf(DOMActionNotAvailableException.class, ee.getCause());
    }

    private static ListenableFuture<? extends DOMRpcResult> invokeBaz(final DOMActionService actionService,
            final YangInstanceIdentifier path) {
        return actionService.invokeAction(Actions.BAZ_TYPE,
            DOMDataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL, path),
            ImmutableNodes.newContainerBuilder().withNodeIdentifier(new NodeIdentifier(Actions.INPUT)).build());
    }
}
