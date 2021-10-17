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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import org.junit.BeforeClass;
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
import org.opendaylight.mdsal.dom.broker.util.TestModel;
import org.opendaylight.mdsal.dom.spi.SimpleDOMActionResult;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DOMRpcRouterTest extends TestUtils {
    private static final QName FOO = QName.create("actions", "foo");
    private static final QName BAR = QName.create(FOO, "bar");
    private static final QName BAZ = QName.create(FOO, "baz");
    private static final QName INPUT = QName.create(FOO, "input");
    private static final QName OUTPUT = QName.create(FOO, "output");

    private static final Absolute BAZ_TYPE = Absolute.of(FOO, BAZ);
    private static final YangInstanceIdentifier BAZ_PATH_BAD = YangInstanceIdentifier.create(
        new NodeIdentifier(FOO), NodeIdentifierWithPredicates.of(FOO, BAR, "bad"));
    private static final YangInstanceIdentifier BAZ_PATH_GOOD = YangInstanceIdentifier.create(
        new NodeIdentifier(FOO), NodeIdentifierWithPredicates.of(FOO, BAR, "good"));

    private static final DOMActionImplementation IMPL =
        (type, path, input) -> Futures.immediateFuture(new SimpleDOMActionResult(
            Builders.containerBuilder().withNodeIdentifier(new NodeIdentifier(OUTPUT)).build()));

    private static EffectiveModelContext ACTIONS_CONTEXT;

    @BeforeClass
    public static void beforeClass() {
        ACTIONS_CONTEXT = YangParserTestUtils.parseYangResource("/actions.yang");
    }

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
    public void testFailedInvokeRpc() {
        try (DOMRpcRouter rpcRouter = new DOMRpcRouter()) {
            final ListenableFuture<?> future = rpcRouter.getRpcService().invokeRpc(TestModel.TEST_QNAME, null);
            final Throwable cause = assertThrows(ExecutionException.class, () -> Futures.getDone(future)).getCause();
            assertThat(cause, instanceOf(DOMRpcImplementationNotAvailableException.class));
            assertEquals("No implementation of RPC "
                + "(urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom:store:test?revision=2014-03-13)test "
                + "available", cause.getMessage());
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
                getTestRpcImplementation(), DOMRpcIdentifier.create(TestModel.TEST_QNAME, null));
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

    @Test
    public void testActionInstanceRouting() throws ExecutionException {
        try (DOMRpcRouter rpcRouter = new DOMRpcRouter()) {
            rpcRouter.onModelContextUpdated(ACTIONS_CONTEXT);

            final DOMActionProviderService actionProvider = rpcRouter.getActionProviderService();
            assertNotNull(actionProvider);
            final DOMActionService actionConsumer = rpcRouter.getActionService();
            assertNotNull(actionConsumer);

            try (ObjectRegistration<?> reg = actionProvider.registerActionImplementation(IMPL,
                DOMActionInstance.of(BAZ_TYPE, LogicalDatastoreType.OPERATIONAL, BAZ_PATH_GOOD))) {

                assertAvailable(actionConsumer, BAZ_PATH_GOOD);
                assertUnavailable(actionConsumer, BAZ_PATH_BAD);
            }

            assertUnavailable(actionConsumer, BAZ_PATH_BAD);
            assertUnavailable(actionConsumer, BAZ_PATH_GOOD);
        }
    }

    @Test
    public void testActionDatastoreRouting() throws ExecutionException {
        try (DOMRpcRouter rpcRouter = new DOMRpcRouter()) {
            rpcRouter.onModelContextUpdated(ACTIONS_CONTEXT);

            final DOMActionProviderService actionProvider = rpcRouter.getActionProviderService();
            assertNotNull(actionProvider);
            final DOMActionService actionConsumer = rpcRouter.getActionService();
            assertNotNull(actionConsumer);

            try (ObjectRegistration<?> reg = actionProvider.registerActionImplementation(IMPL,
                DOMActionInstance.of(BAZ_TYPE, LogicalDatastoreType.OPERATIONAL, YangInstanceIdentifier.empty()))) {

                assertAvailable(actionConsumer, BAZ_PATH_GOOD);
                assertAvailable(actionConsumer, BAZ_PATH_BAD);
            }

            assertUnavailable(actionConsumer, BAZ_PATH_BAD);
            assertUnavailable(actionConsumer, BAZ_PATH_GOOD);
        }
    }

    private static void assertAvailable(final DOMActionService actionService, final YangInstanceIdentifier path) {
        final DOMActionResult result;
        try {
            result = Futures.getDone(invokeBaz(actionService, path));
        } catch (ExecutionException e) {
            throw new AssertionError("Unexpected invocation failure", e);
        }
        assertEquals(List.of(), result.getErrors());
    }

    private static void assertUnavailable(final DOMActionService actionService, final YangInstanceIdentifier path) {
        final ListenableFuture<? extends DOMActionResult> future = invokeBaz(actionService, path);
        final ExecutionException ex = assertThrows(ExecutionException.class, () -> Futures.getDone(future));
        assertThat(ex.getCause(), instanceOf(DOMActionNotAvailableException.class));
    }

    private static ListenableFuture<? extends DOMActionResult> invokeBaz(final DOMActionService actionService,
            final YangInstanceIdentifier path) {
        return actionService.invokeAction(BAZ_TYPE, new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, path),
            Builders.containerBuilder().withNodeIdentifier(new NodeIdentifier(INPUT)).build());
    }
}
