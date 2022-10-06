/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.Future;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.md.sal.knock.knock.rev180723.KnockKnock;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.md.sal.knock.knock.rev180723.KnockKnockInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.md.sal.knock.knock.rev180723.KnockKnockInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.md.sal.knock.knock.rev180723.KnockKnockOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.md.sal.knock.knock.rev180723.KnockKnockOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.md.sal.knock.knock.rev180723.OpendaylightKnockKnockRpcService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

public class BindingDOMRpcIntegrationTest {
    private static final InstanceIdentifier<TopLevelList> BA_NODE_ID = InstanceIdentifier.create(Top.class)
            .child(TopLevelList.class, new TopLevelListKey("a"));

    private static final QName KNOCK_KNOCK_QNAME = QName.create(KnockKnockOutput.QNAME, "knock-knock");

    private RpcProviderService baRpcProviderService;
    private RpcConsumerRegistry baRpcConsumerService;
    private DOMRpcProviderService biRpcProviderService;
    private BindingTestContext testContext;
    private DOMRpcService biRpcService;
    private final OpendaylightKnockKnockRpcServiceImpl knockRpcImpl = new OpendaylightKnockKnockRpcServiceImpl();

    @Before
    public void setup() throws Exception {
        BindingBrokerTestFactory testFactory = new BindingBrokerTestFactory();
        testFactory.setExecutor(MoreExecutors.newDirectExecutorService());
        testContext = testFactory.getTestContext();

        testContext.setSchemaModuleInfos(ImmutableSet.of(
                BindingReflections.getModuleInfo(OpendaylightKnockKnockRpcService.class),
                BindingReflections.getModuleInfo(Top.class)));
        testContext.start();
        baRpcProviderService = testContext.getBindingRpcProviderRegistry();
        baRpcConsumerService = testContext.getBindingRpcConsumerRegistry();
        biRpcProviderService = testContext.getDomRpcRegistry();
        biRpcService = testContext.getDomRpcInvoker();
    }

    @Test
    public void testBindingRegistrationWithDOMInvocation() throws Exception {
        knockRpcImpl.registerTo(baRpcProviderService, BA_NODE_ID).setKnockKnockResult(knockResult(true, "open"));

        final OpendaylightKnockKnockRpcService baKnockService =
                baRpcConsumerService.getRpcService(OpendaylightKnockKnockRpcService.class);
        assertNotSame(knockRpcImpl, baKnockService);

        KnockKnockInput baKnockKnockInput = knockKnock(BA_NODE_ID).setQuestion("who's there?").build();

        ContainerNode biKnockKnockInput = toDOMKnockKnockInput(baKnockKnockInput);
        DOMRpcResult domResult = Futures.getDone(biRpcService.invokeRpc(KNOCK_KNOCK_QNAME, biKnockKnockInput));
        assertNotNull(domResult);
        assertNotNull(domResult.value());
        assertTrue("Binding KnockKnock service was not invoked",
                knockRpcImpl.getReceivedKnocks().containsKey(BA_NODE_ID));
        assertEquals(baKnockKnockInput, knockRpcImpl.getReceivedKnocks().get(BA_NODE_ID).iterator().next());
    }

    @Test
    public void testDOMRegistrationWithBindingInvocation() throws Exception {
        KnockKnockOutput baKnockKnockOutput = new KnockKnockOutputBuilder().setAnswer("open").build();

        biRpcProviderService.registerRpcImplementation((rpc, input) ->
            FluentFutures.immediateFluentFuture(new DefaultDOMRpcResult(testContext.getCodec()
                    .currentSerializer().toNormalizedNodeRpcData(baKnockKnockOutput))),
            DOMRpcIdentifier.create(KNOCK_KNOCK_QNAME, testContext.getCodec().currentSerializer()
                .toYangInstanceIdentifier(BA_NODE_ID)));

        final OpendaylightKnockKnockRpcService baKnockService =
                baRpcConsumerService.getRpcService(OpendaylightKnockKnockRpcService.class);
        Future<RpcResult<KnockKnockOutput>> baResult = baKnockService.knockKnock(knockKnock(BA_NODE_ID)
            .setQuestion("Who's there?").build());
        assertNotNull(baResult);
        assertEquals(baKnockKnockOutput, Futures.getDone(baResult).getResult());
    }

    @Test
    public void testBindingRpcShortcut() throws Exception {
        final ListenableFuture<RpcResult<KnockKnockOutput>> baKnockResult = knockResult(true, "open");
        knockRpcImpl.registerTo(baRpcProviderService, BA_NODE_ID).setKnockKnockResult(baKnockResult);

        final OpendaylightKnockKnockRpcService baKnockService =
                baRpcConsumerService.getRpcService(OpendaylightKnockKnockRpcService.class);

        KnockKnockInput baKnockKnockInput = knockKnock(BA_NODE_ID).setQuestion("who's there?").build();

        final RpcResult<KnockKnockOutput> rpcResult = Futures.getDone(baKnockService.knockKnock(baKnockKnockInput));

        assertEquals(baKnockResult.get().getResult().getClass(), rpcResult.getResult().getClass());
        assertSame(baKnockResult.get().getResult(), rpcResult.getResult());
        assertSame(baKnockKnockInput, knockRpcImpl.getReceivedKnocks().get(BA_NODE_ID).iterator().next());
    }

    @Test
    public void testSimpleRpc() throws Exception {
        baRpcProviderService.registerRpcImplementation((KnockKnock) input -> knockResult(true, "open"));

        final KnockKnock baKnockService = baRpcConsumerService.getRpc(KnockKnock.class);
        final RpcResult<KnockKnockOutput> rpcResult = Futures.getDone(
            baKnockService.invoke(knockKnock(BA_NODE_ID).setQuestion("who's there?").build()));

        assertEquals(rpcResult.getResult().getClass(), rpcResult.getResult().getClass());
        assertSame(rpcResult.getResult(), rpcResult.getResult());
    }

    private static ListenableFuture<RpcResult<KnockKnockOutput>> knockResult(final boolean success,
            final String answer) {
        return RpcResultBuilder.<KnockKnockOutput>status(success)
            .withResult(new KnockKnockOutputBuilder().setAnswer(answer).build())
            .buildFuture();
    }

    private static KnockKnockInputBuilder knockKnock(final InstanceIdentifier<TopLevelList> listId) {
        KnockKnockInputBuilder builder = new KnockKnockInputBuilder();
        builder.setKnockerId(listId);
        return builder;
    }

    private ContainerNode toDOMKnockKnockInput(final KnockKnockInput from) {
        return testContext.getCodec().currentSerializer().toNormalizedNodeRpcData(from);
    }

    private static class OpendaylightKnockKnockRpcServiceImpl implements OpendaylightKnockKnockRpcService {
        private ListenableFuture<RpcResult<KnockKnockOutput>> knockKnockResult;
        private final Multimap<InstanceIdentifier<?>, KnockKnockInput> receivedKnocks = HashMultimap.create();
        private ObjectRegistration<OpendaylightKnockKnockRpcServiceImpl> registration;

        OpendaylightKnockKnockRpcServiceImpl setKnockKnockResult(
                final ListenableFuture<RpcResult<KnockKnockOutput>> kkOutput) {
            knockKnockResult = kkOutput;
            return this;
        }

        Multimap<InstanceIdentifier<?>, KnockKnockInput> getReceivedKnocks() {
            return receivedKnocks;
        }

        OpendaylightKnockKnockRpcServiceImpl registerTo(final RpcProviderService registry,
                final InstanceIdentifier<?>... paths) {
            registration = registry.registerRpcImplementation(OpendaylightKnockKnockRpcService.class, this,
                    ImmutableSet.copyOf(paths));
            assertNotNull(registration);
            return this;
        }

        @Override
        public ListenableFuture<RpcResult<KnockKnockOutput>> knockKnock(final KnockKnockInput input) {
            receivedKnocks.put(input.getKnockerId(), input);
            return knockKnockResult;
        }
    }
}
