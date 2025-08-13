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
import static org.junit.Assert.assertSame;
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
import org.mockito.Mock;
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

    @Mock
    public DOMSchemaService mockSchemaService;

    @Test
    public void registerRpcImplementation() {
        try (var rpcRouter = rpcsRouter()) {
            assertOperationKeys(rpcRouter);

            final var service = new RouterDOMRpcProviderService(rpcRouter);

            final var fooReg = service.registerRpcImplementation(getTestRpcImplementation(),
                DOMRpcIdentifier.create(Rpcs.FOO, null));
            assertOperationKeys(rpcRouter, Rpcs.FOO);

            final var barReg = service.registerRpcImplementation(getTestRpcImplementation(),
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

            final var service = new RouterDOMRpcProviderService(rpcRouter);

            final var fooReg = service.registerRpcImplementations(Map.of(
                DOMRpcIdentifier.create(Rpcs.FOO, null), getTestRpcImplementation()));
            assertOperationKeys(rpcRouter, Rpcs.FOO);

            final var barReg = service.registerRpcImplementations(Map.of(
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

            final var providerService = new RouterDOMRpcProviderService(rpcRouter);
            final var service = new RouterDOMRpcService(rpcRouter);

            try (var reg = providerService.registerRpcImplementation(
                    (rpc, unused) -> {
                        throw thrown;
                    }, DOMRpcIdentifier.create(Rpcs.FOO))) {

                final var future = service.invokeRpc(Rpcs.FOO, input);
                final var cause = assertThrows(ExecutionException.class, () -> Futures.getDone(future)).getCause();
                assertThat(cause, instanceOf(DefaultDOMRpcException.class));
                assertEquals("RPC implementation failed: java.lang.RuntimeException: mumble-mumble",
                    cause.getMessage());
                assertSame(thrown, cause.getCause());
            }

            final var future = service.invokeRpc(Rpcs.FOO, input);
            final var cause = assertThrows(ExecutionException.class, () -> Futures.getDone(future)).getCause();
            assertThat(cause, instanceOf(DOMRpcImplementationNotAvailableException.class));
            assertEquals("No implementation of RPC (rpcs)foo available", cause.getMessage());
        }
    }

    @Test
    public void testRpcListener() {
        try (var rpcRouter = new DOMRpcRouter(mockSchemaService)) {
            assertEquals(List.of(), rpcRouter.listeners());

            final var listener = mock(DOMRpcAvailabilityListener.class);
            doCallRealMethod().when(listener).acceptsImplementation(any());
            doNothing().when(listener).onRpcAvailable(any());
            doNothing().when(listener).onRpcUnavailable(any());

            final var providerService = new RouterDOMRpcProviderService(rpcRouter);
            final var service = new RouterDOMRpcService(rpcRouter);

            final var reg = service.registerRpcListener(listener);
            assertNotNull(reg);
            assertEquals(List.of(reg), rpcRouter.listeners());

            final var implReg = providerService.registerRpcImplementation(
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
        try (var rpcRouter = new DOMRpcRouter(mockSchemaService)) {
            assertEquals(List.of(), rpcRouter.actionListeners());

            final var service = new RouterDOMActionService(rpcRouter);
            final var listener = mock(AvailabilityListener.class);
            final var availability = service.extension(DOMActionAvailabilityExtension.class);
            assertNotNull(availability);
            final var reg = availability.registerAvailabilityListener(listener);
            assertNotNull(reg);
            assertEquals(List.of(reg), rpcRouter.actionListeners());

            // FIXME: register implementation and verify notification

            reg.close();
            assertEquals(List.of(), rpcRouter.actionListeners());
        }
    }

    @Test
    public void onGlobalContextUpdated() {
        try (DOMRpcRouter rpcRouter = new DOMRpcRouter(mockSchemaService)) {
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
                final var ex = assertThrows(ExecutionException.class, () -> Futures.getDone(future)).getCause();
                assertThat(ex, instanceOf(DOMRpcException.class));
                assertEquals("Action implementation failed: java.lang.RuntimeException: test-two-three",
                    ex.getMessage());
                assertSame(thrown, ex.getCause());
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
        final var ex = assertThrows(ExecutionException.class, () -> Futures.getDone(future));
        assertThat(ex.getCause(), instanceOf(DOMActionNotAvailableException.class));
    }

    private static ListenableFuture<? extends DOMRpcResult> invokeBaz(final DOMActionService actionService,
            final YangInstanceIdentifier path) {
        return actionService.invokeAction(Actions.BAZ_TYPE,
            DOMDataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL, path),
            ImmutableNodes.newContainerBuilder().withNodeIdentifier(new NodeIdentifier(Actions.INPUT)).build());
    }
}
