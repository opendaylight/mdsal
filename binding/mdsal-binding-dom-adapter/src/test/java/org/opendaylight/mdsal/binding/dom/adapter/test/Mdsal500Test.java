/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.RpcConsumerRegistry;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.binding.dom.adapter.test.util.BindingBrokerTestFactory;
import org.opendaylight.mdsal.binding.dom.adapter.test.util.BindingTestContext;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.mdsal.dom.spi.DefaultDOMRpcResult;
import org.opendaylight.yang.gen.v1.rpc.norev.Mdsal500Service;
import org.opendaylight.yang.gen.v1.rpc.norev.SwitchInput;
import org.opendaylight.yang.gen.v1.rpc.norev.SwitchInputBuilder;
import org.opendaylight.yang.gen.v1.rpc.norev.SwitchOutput;
import org.opendaylight.yang.gen.v1.rpc.norev.SwitchOutputBuilder;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class Mdsal500Test {
    private static final String FOO = "foo";

    private static final QName SWITCH_QNAME = QName.create(SwitchOutput.QNAME, "switch");
    private static final SchemaPath SWITCH_PATH = SchemaPath.create(true, SWITCH_QNAME);

    private RpcProviderService baRpcProviderService;
    private RpcConsumerRegistry baRpcConsumerService;
    private DOMRpcProviderService biRpcProviderService;
    private BindingTestContext testContext;
    private DOMRpcService biRpcService;
    private final Mdsal500ServiceImpl switchRpcImpl = new Mdsal500ServiceImpl();

    @Before
    public void setup() throws Exception {
        BindingBrokerTestFactory testFactory = new BindingBrokerTestFactory();
        testFactory.setExecutor(MoreExecutors.newDirectExecutorService());
        testContext = testFactory.getTestContext();

        testContext.setSchemaModuleInfos(ImmutableSet.of(
                BindingReflections.getModuleInfo(Mdsal500Service.class)));
        testContext.start();
        baRpcProviderService = testContext.getBindingRpcProviderRegistry();
        baRpcConsumerService = testContext.getBindingRpcConsumerRegistry();
        biRpcProviderService = testContext.getDomRpcRegistry();
        biRpcService = testContext.getDomRpcInvoker();
    }

    @Test
    public void testBindingRegistrationWithDOMInvocation() throws Exception {
        switchRpcImpl.registerTo(baRpcProviderService).setSwitchResult(switchResult(true));

        final Mdsal500Service baSwitchService = baRpcConsumerService.getRpcService(Mdsal500Service.class);
        assertNotSame(switchRpcImpl, baSwitchService);

        SwitchInput baSwitchInput = switchBuilder(FOO).build();

        ContainerNode biSwitchInput = toDOMSwitchInput(baSwitchInput);
        DOMRpcResult domResult = biRpcService.invokeRpc(SWITCH_PATH, biSwitchInput).get(5, TimeUnit.SECONDS);
        assertNotNull(domResult);
        assertNotNull(domResult.getResult());
        assertTrue("Binding KnockKnock service was not invoked",
                switchRpcImpl.getReceivedSwitch().containsKey(FOO));
        assertEquals(baSwitchInput, switchRpcImpl.getReceivedSwitch().get(FOO).iterator().next());
    }

    @Test
    public void testDOMRegistrationWithBindingInvocation()
            throws InterruptedException, ExecutionException, TimeoutException {
        SwitchOutput baSwitchOutput = new SwitchOutputBuilder().build();

        biRpcProviderService.registerRpcImplementation((rpc, input) ->
            FluentFutures.immediateFluentFuture(new DefaultDOMRpcResult(testContext.getCodec()
                    .getCodecFactory().toNormalizedNodeRpcData(baSwitchOutput))),
            DOMRpcIdentifier.create(SWITCH_PATH));

        final Mdsal500Service baSwitchService =
                baRpcConsumerService.getRpcService(Mdsal500Service.class);
        Future<RpcResult<SwitchOutput>> baResult = baSwitchService.switch$(switchBuilder(FOO)
            .build());
        assertNotNull(baResult);
        assertEquals(baSwitchOutput, baResult.get(5, TimeUnit.SECONDS).getResult());
    }

    @Test
    public void testBindingRpcShortcut() throws InterruptedException, ExecutionException, TimeoutException {
        final ListenableFuture<RpcResult<SwitchOutput>> baSwitchResult = switchResult(true);
        switchRpcImpl.registerTo(baRpcProviderService).setSwitchResult(baSwitchResult);

        final Mdsal500Service baSwitchService = baRpcConsumerService.getRpcService(Mdsal500Service.class);

        SwitchInput baSwitchInput = switchBuilder(FOO).build();
        ListenableFuture<RpcResult<SwitchOutput>> future = baSwitchService.switch$(baSwitchInput);

        final RpcResult<SwitchOutput> rpcResult = future.get(5, TimeUnit.SECONDS);

        assertEquals(baSwitchResult.get().getResult().getClass(), rpcResult.getResult().getClass());
        assertSame(baSwitchResult.get().getResult(), rpcResult.getResult());
        assertSame(baSwitchInput, switchRpcImpl.getReceivedSwitch().get(FOO).iterator().next());
    }

    private static ListenableFuture<RpcResult<SwitchOutput>> switchResult(final boolean success) {
        SwitchOutput output = new SwitchOutputBuilder().build();
        RpcResult<SwitchOutput> result = RpcResultBuilder.<SwitchOutput>status(success).withResult(output)
                .build();
        return Futures.immediateFuture(result);
    }

    private static SwitchInputBuilder switchBuilder(final String foo) {
        SwitchInputBuilder builder = new SwitchInputBuilder();
        builder.setFoo(foo);
        return builder;
    }

    private ContainerNode toDOMSwitchInput(final SwitchInput from) {
        return testContext.getCodec().getCodecFactory().toNormalizedNodeRpcData(from);
    }

    private static class Mdsal500ServiceImpl implements Mdsal500Service {
        private ListenableFuture<RpcResult<SwitchOutput>> switchResult;
        private final Multimap<String, SwitchInput> receivedSwitch = HashMultimap.create();

        Mdsal500ServiceImpl setSwitchResult(final ListenableFuture<RpcResult<SwitchOutput>> switchOutput) {
            this.switchResult = switchOutput;
            return this;
        }

        Multimap<String, SwitchInput> getReceivedSwitch() {
            return receivedSwitch;
        }

        Mdsal500ServiceImpl registerTo(final RpcProviderService registry) {
            final ObjectRegistration<Mdsal500ServiceImpl> registration =
                    registry.registerRpcImplementation(Mdsal500Service.class, this);
            assertNotNull(registration);
            return this;
        }

        @Override
        public ListenableFuture<RpcResult<SwitchOutput>> switch$(SwitchInput switchInput) {
            receivedSwitch.put(switchInput.getFoo(), switchInput);
            return switchResult;
        }
    }

}
