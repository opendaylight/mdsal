/*
 * Copyright (c) 2018 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.operation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.test.BindingBrokerTestFactory;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.test.BindingTestContext;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.RpcCallback;
import org.opendaylight.mdsal.dom.api.DOMRpcException;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementationNotAvailableException;
import org.opendaylight.mdsal.gen.javav2.urn.test.operation.rev170621.TestOperationServiceInContAction;
import org.opendaylight.mdsal.gen.javav2.urn.test.operation.rev170621.TestOperationServiceMyRpcRpc;
import org.opendaylight.mdsal.gen.javav2.urn.test.operation.rev170621.data.MyCont;
import org.opendaylight.mdsal.gen.javav2.urn.test.operation.rev170621.data.my_cont.in_cont.InContInput;
import org.opendaylight.mdsal.gen.javav2.urn.test.operation.rev170621.data.my_cont.in_cont.InContOutput;
import org.opendaylight.mdsal.gen.javav2.urn.test.operation.rev170621.data.my_rpc.MyRpcInput;
import org.opendaylight.mdsal.gen.javav2.urn.test.operation.rev170621.data.my_rpc.MyRpcOutput;
import org.opendaylight.mdsal.gen.javav2.urn.test.operation.rev170621.dto.my_cont.in_cont.InContInputBuilder;
import org.opendaylight.mdsal.gen.javav2.urn.test.operation.rev170621.dto.my_cont.in_cont.InContOutputBuilder;
import org.opendaylight.mdsal.gen.javav2.urn.test.operation.rev170621.dto.my_rpc.MyRpcInputBuilder;
import org.opendaylight.mdsal.gen.javav2.urn.test.operation.rev170621.dto.my_rpc.MyRpcOutputBuilder;


public class BindingDOMOperationServiceAdapterTest {
    private static final MyRpcOutput RPC_OUTPUT = new MyRpcOutputBuilder().setRpcOutputLeaf("rpc_output_leaf").build();
    private static final InContOutput ACTION_OUTPUT =
        new InContOutputBuilder().setOutputLeaf("action_output_leaf").build();

    @Test
    public void actionTest() throws Exception {
        final BindingBrokerTestFactory testFactory = new BindingBrokerTestFactory();
        testFactory.setExecutor(Executors.newCachedThreadPool());

        final BindingTestContext testContext = testFactory.getTestContext();
        testContext.start();

        final BindingDOMOperationProviderServiceAdapter adapter =
            new BindingDOMOperationProviderServiceAdapter(testContext.getDomOperationRegistry(),
                testContext.getCodec());

        final BindingDOMOperationServiceAdapter serviceAdapter =
            new BindingDOMOperationServiceAdapter(testContext.getDomOperationInvoker(), testContext.getCodec());

        final CountDownLatch invokeLatch = new CountDownLatch(1);

        assertNotNull(adapter.registerActionImplementation(TestOperationServiceInContAction.class,
            InstanceIdentifier.create(MyCont.class),new TestActionImpl()));
        final TestOperationServiceInContAction action =
            serviceAdapter.getActionService(TestOperationServiceInContAction.class);
        assertNotNull(serviceAdapter.getActionService(TestOperationServiceInContAction.class));

        action.invoke(new InContInputBuilder().setInputLeaf("in").build(),
            InstanceIdentifier.create(MyCont.class),
            new RpcCallback<InContOutput>() {
                @Override
                public void onSuccess(InContOutput output) {
                    assertEquals(ACTION_OUTPUT, output);
                    invokeLatch.countDown();
                }

                @Override
                public void onFailure(Throwable cause) {
                    assertNotNull(cause);
                }
            });
        assertEquals("Invoke complete", true, invokeLatch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void actionFailedTest() throws Exception {
        final BindingBrokerTestFactory testFactory = new BindingBrokerTestFactory();
        testFactory.setExecutor(Executors.newCachedThreadPool());

        final BindingTestContext testContext = testFactory.getTestContext();
        testContext.start();

        final BindingDOMOperationProviderServiceAdapter adapter =
            new BindingDOMOperationProviderServiceAdapter(testContext.getDomOperationRegistry(),
                testContext.getCodec());

        final BindingDOMOperationServiceAdapter serviceAdapter =
            new BindingDOMOperationServiceAdapter(testContext.getDomOperationInvoker(), testContext.getCodec());

        final CountDownLatch invokeLatch = new CountDownLatch(1);

        assertNotNull(adapter.registerActionImplementation(TestOperationServiceInContAction.class,
            InstanceIdentifier.create(MyCont.class),new TestActionFailedImpl()));
        final TestOperationServiceInContAction action =
            serviceAdapter.getActionService(TestOperationServiceInContAction.class);
        assertNotNull(serviceAdapter.getActionService(TestOperationServiceInContAction.class));

        action.invoke(new InContInputBuilder().setInputLeaf("in").build(),
            InstanceIdentifier.create(MyCont.class),
            new RpcCallback<InContOutput>() {
                @Override
                public void onSuccess(InContOutput output) {

                }

                @Override
                public void onFailure(Throwable cause) {
                    assertTrue(cause instanceof DOMRpcException);
                    assertTrue(cause.getCause() instanceof RuntimeException);
                    assertEquals(cause.getCause().getMessage(), "TestActionFailedImpl invoke failed.");
                    invokeLatch.countDown();
                }
            });
        assertEquals("Invoke complete", true, invokeLatch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void actionNotAvaliableTest() throws Exception {
        final BindingBrokerTestFactory testFactory = new BindingBrokerTestFactory();
        testFactory.setExecutor(Executors.newCachedThreadPool());

        final BindingTestContext testContext = testFactory.getTestContext();
        testContext.start();

        final BindingDOMOperationServiceAdapter serviceAdapter =
            new BindingDOMOperationServiceAdapter(testContext.getDomOperationInvoker(), testContext.getCodec());

        final CountDownLatch invokeLatch = new CountDownLatch(1);

        final TestOperationServiceInContAction action =
            serviceAdapter.getActionService(TestOperationServiceInContAction.class);
        assertNotNull(serviceAdapter.getActionService(TestOperationServiceInContAction.class));

        action.invoke(new InContInputBuilder().setInputLeaf("in").build(),
            InstanceIdentifier.create(MyCont.class),
            new RpcCallback<InContOutput>() {
                @Override
                public void onSuccess(InContOutput output) {

                }

                @Override
                public void onFailure(Throwable cause) {
                    assertTrue(cause instanceof DOMRpcImplementationNotAvailableException);
                    invokeLatch.countDown();
                }
            });
        assertEquals("Invoke complete", true, invokeLatch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void rpcTest() throws Exception {
        final BindingBrokerTestFactory testFactory = new BindingBrokerTestFactory();
        testFactory.setExecutor(Executors.newCachedThreadPool());

        final BindingTestContext testContext = testFactory.getTestContext();
        testContext.start();

        final BindingDOMOperationProviderServiceAdapter adapter =
            new BindingDOMOperationProviderServiceAdapter(testContext.getDomOperationRegistry(),
                testContext.getCodec());

        final BindingDOMOperationServiceAdapter serviceAdapter =
            new BindingDOMOperationServiceAdapter(testContext.getDomOperationInvoker(), testContext.getCodec());

        final CountDownLatch invokeLatch = new CountDownLatch(1);

        assertNotNull(adapter.registerRpcImplementation(TestOperationServiceMyRpcRpc.class,
            new TestRpcImpl()));
        final TestOperationServiceMyRpcRpc rpc =
            serviceAdapter.getRpcService(TestOperationServiceMyRpcRpc.class);
        assertNotNull(rpc);

        rpc.invoke(new MyRpcInputBuilder().setRpcInputLeaf("123").build(),
                new RpcCallback<MyRpcOutput>() {
                    @Override
                    public void onSuccess(MyRpcOutput output) {
                        assertEquals(RPC_OUTPUT, output);
                        invokeLatch.countDown();
                    }

                    @Override
                    public void onFailure(Throwable cause) {
                    }
                });
        assertEquals("Invoke complete", true, invokeLatch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void rpcFailedTest() throws Exception {
        final BindingBrokerTestFactory testFactory = new BindingBrokerTestFactory();
        testFactory.setExecutor(Executors.newCachedThreadPool());

        final BindingTestContext testContext = testFactory.getTestContext();
        testContext.start();

        final BindingDOMOperationProviderServiceAdapter adapter =
            new BindingDOMOperationProviderServiceAdapter(testContext.getDomOperationRegistry(),
                testContext.getCodec());

        final BindingDOMOperationServiceAdapter serviceAdapter =
            new BindingDOMOperationServiceAdapter(testContext.getDomOperationInvoker(), testContext.getCodec());

        final CountDownLatch invokeLatch = new CountDownLatch(1);

        assertNotNull(adapter.registerRpcImplementation(TestOperationServiceMyRpcRpc.class,
            new TestRpcFailedImpl()));
        final TestOperationServiceMyRpcRpc rpc =
            serviceAdapter.getRpcService(TestOperationServiceMyRpcRpc.class);
        assertNotNull(rpc);

        rpc.invoke(new MyRpcInputBuilder().setRpcInputLeaf("123").build(),
            new RpcCallback<MyRpcOutput>() {
                @Override
                public void onSuccess(MyRpcOutput output) {

                }

                @Override
                public void onFailure(Throwable cause) {
                    assertTrue(cause instanceof DOMRpcException);
                    assertTrue(cause.getCause() instanceof RuntimeException);
                    assertEquals(cause.getCause().getMessage(), "TestRpcFailedImpl invoke failed.");
                    invokeLatch.countDown();
                }
            });
        assertEquals("Invoke complete", true, invokeLatch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void rpcNotAvaliableTest() throws Exception {
        final BindingBrokerTestFactory testFactory = new BindingBrokerTestFactory();
        testFactory.setExecutor(Executors.newCachedThreadPool());

        final BindingTestContext testContext = testFactory.getTestContext();
        testContext.start();

        final BindingDOMOperationServiceAdapter serviceAdapter =
            new BindingDOMOperationServiceAdapter(testContext.getDomOperationInvoker(), testContext.getCodec());

        final CountDownLatch invokeLatch = new CountDownLatch(1);

        final TestOperationServiceMyRpcRpc rpc =
            serviceAdapter.getRpcService(TestOperationServiceMyRpcRpc.class);
        assertNotNull(rpc);

        rpc.invoke(new MyRpcInputBuilder().setRpcInputLeaf("123").build(),
            new RpcCallback<MyRpcOutput>() {
                @Override
                public void onSuccess(MyRpcOutput output) {

                }

                @Override
                public void onFailure(Throwable cause) {
                    assertTrue(cause instanceof DOMRpcImplementationNotAvailableException);
                    invokeLatch.countDown();
                }
            });
        assertEquals("Invoke complete", true, invokeLatch.await(5, TimeUnit.SECONDS));
    }

    private class TestRpcImpl implements TestOperationServiceMyRpcRpc {

        @Override
        public void invoke(MyRpcInput input, RpcCallback<MyRpcOutput> callback) {
            callback.onSuccess(RPC_OUTPUT);
        }
    }

    private class TestRpcFailedImpl implements TestOperationServiceMyRpcRpc {

        @Override
        public void invoke(MyRpcInput input, RpcCallback<MyRpcOutput> callback) {
            callback.onFailure(new RuntimeException("TestRpcFailedImpl invoke failed."));
        }
    }

    private class TestActionImpl implements TestOperationServiceInContAction {

        @Override
        public void invoke(InContInput input, InstanceIdentifier<MyCont> ii, RpcCallback<InContOutput> callback) {
            callback.onSuccess(ACTION_OUTPUT);
        }
    }

    private class TestActionFailedImpl implements TestOperationServiceInContAction {

        @Override
        public void invoke(InContInput input, InstanceIdentifier<MyCont> ii, RpcCallback<InContOutput> callback) {
            callback.onFailure(new RuntimeException("TestActionFailedImpl invoke failed."));
        }
    }
}