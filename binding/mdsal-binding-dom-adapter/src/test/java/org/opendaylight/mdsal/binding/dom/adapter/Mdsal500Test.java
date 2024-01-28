/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.binding.api.RpcService;
import org.opendaylight.mdsal.binding.dom.adapter.test.util.BindingBrokerTestFactory;
import org.opendaylight.mdsal.binding.dom.adapter.test.util.BindingTestContext;
import org.opendaylight.mdsal.dom.api.DOMRpcFuture;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.mdsal.dom.spi.DefaultDOMRpcResult;
import org.opendaylight.yang.gen.v1.rpc.norev.Switch;
import org.opendaylight.yang.gen.v1.rpc.norev.SwitchInput;
import org.opendaylight.yang.gen.v1.rpc.norev.SwitchInputBuilder;
import org.opendaylight.yang.gen.v1.rpc.norev.SwitchOutput;
import org.opendaylight.yang.gen.v1.rpc.norev.SwitchOutputBuilder;
import org.opendaylight.yang.svc.v1.rpc.norev.YangModuleInfoImpl;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

public class Mdsal500Test {
    private static final String FOO = "foo";

    private static final QName SWITCH_QNAME = QName.create(SwitchOutput.QNAME, "switch");

    private RpcProviderService baRpcProviderService;
    private RpcService baRpcConsumerService;
    private DOMRpcProviderService biRpcProviderService;
    private BindingTestContext testContext;
    private DOMRpcService biRpcService;
    private final SwitchImpl switchRpcImpl = new SwitchImpl();

    @Before
    public void setup() throws Exception {
        BindingBrokerTestFactory testFactory = new BindingBrokerTestFactory();
        testFactory.setExecutor(MoreExecutors.newDirectExecutorService());
        testContext = testFactory.getTestContext();

        testContext.setSchemaModuleInfos(Set.of(YangModuleInfoImpl.getInstance()));
        testContext.start();
        baRpcProviderService = testContext.getBindingRpcProviderRegistry();
        baRpcConsumerService = testContext.getBindingRpcService();
        biRpcProviderService = testContext.getDomRpcRegistry();
        biRpcService = testContext.getDomRpcInvoker();
    }

    @Test
    public void testBindingRegistrationWithDOMInvocation() throws Exception {
        switchRpcImpl.registerTo(baRpcProviderService).setSwitchResult(switchResult(true));

        final var baSwitchService = baRpcConsumerService.getRpc(Switch.class);
        assertNotSame(switchRpcImpl, baSwitchService);

        SwitchInput baSwitchInput = switchBuilder(FOO).build();

        ContainerNode biSwitchInput = toDOMSwitchInput(baSwitchInput);
        DOMRpcResult domResult = biRpcService.invokeRpc(SWITCH_QNAME, biSwitchInput).get(5, TimeUnit.SECONDS);
        assertNotNull(domResult);
        assertNotNull(domResult.value());
        assertTrue("Binding KnockKnock service was not invoked",
                switchRpcImpl.getReceivedSwitch().containsKey(FOO));
        assertEquals(baSwitchInput, switchRpcImpl.getReceivedSwitch().get(FOO).iterator().next());
    }

    @Test
    public void testDOMRegistrationWithBindingInvocation()
            throws InterruptedException, ExecutionException, TimeoutException {
        SwitchOutput baOutput = new SwitchOutputBuilder().build();

        biRpcProviderService.registerRpcImplementation((rpc, input) -> DOMRpcFuture.of(
            new DefaultDOMRpcResult(testContext.getCodec().currentSerializer().toNormalizedNodeRpcData(baOutput))),
            DOMRpcIdentifier.create(SWITCH_QNAME));

        final var baSwitchService = baRpcConsumerService.getRpc(Switch.class);
        final var baResult = baSwitchService.invoke(switchBuilder(FOO).build());
        assertNotNull(baResult);
        assertEquals(baOutput, baResult.get(5, TimeUnit.SECONDS).getResult());
    }

    @Test
    public void testBindingRpcShortcut() throws InterruptedException, ExecutionException, TimeoutException {
        final var baSwitchResult = switchResult(true);
        switchRpcImpl.registerTo(baRpcProviderService).setSwitchResult(baSwitchResult);

        final var baSwitchService = baRpcConsumerService.getRpc(Switch.class);

        final var baSwitchInput = switchBuilder(FOO).build();
        final var future = baSwitchService.invoke(baSwitchInput);

        final var rpcResult = future.get(5, TimeUnit.SECONDS);

        assertEquals(baSwitchResult.get().getResult().getClass(), rpcResult.getResult().getClass());
        assertSame(baSwitchResult.get().getResult(), rpcResult.getResult());
        assertSame(baSwitchInput, switchRpcImpl.getReceivedSwitch().get(FOO).iterator().next());
    }

    private static ListenableFuture<RpcResult<SwitchOutput>> switchResult(final boolean success) {
        return Futures.immediateFuture(RpcResultBuilder.<SwitchOutput>status(success)
            .withResult(new SwitchOutputBuilder().build())
            .build());
    }

    private static SwitchInputBuilder switchBuilder(final String foo) {
        return new SwitchInputBuilder().setFoo(foo);
    }

    private ContainerNode toDOMSwitchInput(final SwitchInput from) {
        return testContext.getCodec().currentSerializer().toNormalizedNodeRpcData(from);
    }

    private static final class SwitchImpl implements Switch {
        private final Multimap<String, SwitchInput> receivedSwitch = HashMultimap.create();
        private ListenableFuture<RpcResult<SwitchOutput>> switchResult;

        SwitchImpl setSwitchResult(final ListenableFuture<RpcResult<SwitchOutput>> switchOutput) {
            switchResult = switchOutput;
            return this;
        }

        Multimap<String, SwitchInput> getReceivedSwitch() {
            return receivedSwitch;
        }

        SwitchImpl registerTo(final RpcProviderService registry) {
            final var registration = registry.registerRpcImplementation(this);
            assertNotNull(registration);
            return this;
        }

        @Override
        public ListenableFuture<RpcResult<SwitchOutput>> invoke(final SwitchInput switchInput) {
            receivedSwitch.put(switchInput.getFoo(), switchInput);
            return switchResult;
        }
    }
}
