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
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.binding.api.RpcService;
import org.opendaylight.mdsal.binding.dom.adapter.test.util.BindingBrokerTestFactory;
import org.opendaylight.mdsal.binding.dom.adapter.test.util.BindingTestContext;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.mdsal.dom.spi.DefaultDOMRpcResult;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.md.sal.knock.knock.rev180723.KnockKnock;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.md.sal.knock.knock.rev180723.KnockKnockInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.md.sal.knock.knock.rev180723.KnockKnockInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.md.sal.knock.knock.rev180723.KnockKnockOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.md.sal.knock.knock.rev180723.KnockKnockOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yangtools.binding.BindingInstanceIdentifier;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

public class BindingDOMRpcIntegrationTest {
    private static final DataObjectIdentifier<TopLevelList> BA_NODE_ID = InstanceIdentifier.create(Top.class)
            .child(TopLevelList.class, new TopLevelListKey("a"))
            .toIdentifier();

    private static final QName KNOCK_KNOCK_QNAME = QName.create(KnockKnockOutput.QNAME, "knock-knock");

    private RpcProviderService baRpcProviderService;
    private RpcService baRpcService;
    private DOMRpcProviderService biRpcProviderService;
    private BindingTestContext testContext;
    private DOMRpcService biRpcService;
    private final KnockKnockImpl knockRpcImpl = new KnockKnockImpl();

    @Before
    public void setup() {
        BindingBrokerTestFactory testFactory = new BindingBrokerTestFactory();
        testFactory.setExecutor(MoreExecutors.newDirectExecutorService());
        testContext = testFactory.getTestContext();

        testContext.setSchemaModuleInfos(ImmutableSet.of(
            BindingRuntimeHelpers.getYangModuleInfo(KnockKnock.class),
            BindingRuntimeHelpers.getYangModuleInfo(Top.class)));
        testContext.start();
        baRpcProviderService = testContext.getBindingRpcProviderRegistry();
        baRpcService = testContext.getBindingRpcService();
        biRpcProviderService = testContext.getDomRpcRegistry();
        biRpcService = testContext.getDomRpcInvoker();
    }

    @Test
    public void testBindingRegistrationWithDOMInvocation() throws Exception {
        knockRpcImpl.registerTo(baRpcProviderService, BA_NODE_ID).setKnockKnockResult(knockResult(true, "open"));

        final var baKnockService = baRpcService.getRpc(KnockKnock.class);
        assertNotSame(knockRpcImpl, baKnockService);

        final var baKnockKnockInput = knockKnock(BA_NODE_ID).setQuestion("who's there?").build();

        final var biKnockKnockInput = toDOMKnockKnockInput(baKnockKnockInput);
        final var domResult = Futures.getDone(biRpcService.invokeRpc(KNOCK_KNOCK_QNAME, biKnockKnockInput));
        assertNotNull(domResult);
        assertNotNull(domResult.value());
        assertTrue("Binding KnockKnock service was not invoked",
                knockRpcImpl.getReceivedKnocks().containsKey(BA_NODE_ID));
        assertEquals(baKnockKnockInput, knockRpcImpl.getReceivedKnocks().get(BA_NODE_ID).iterator().next());
    }

    @Test
    public void testDOMRegistrationWithBindingInvocation() throws Exception {
        final var baKnockKnockOutput = new KnockKnockOutputBuilder().setAnswer("open").build();

        biRpcProviderService.registerRpcImplementation((rpc, input) ->
            FluentFutures.immediateFluentFuture(new DefaultDOMRpcResult(testContext.getCodec()
                    .currentSerializer().toNormalizedNodeRpcData(baKnockKnockOutput))),
            DOMRpcIdentifier.create(KNOCK_KNOCK_QNAME, testContext.getCodec().currentSerializer()
                .toYangInstanceIdentifier(BA_NODE_ID)));

        final var baKnockService = baRpcService.getRpc(KnockKnock.class);
        final var baResult = baKnockService.invoke(knockKnock(BA_NODE_ID).setQuestion("Who's there?").build());
        assertNotNull(baResult);
        assertEquals(baKnockKnockOutput, Futures.getDone(baResult).getResult());
    }

    @Test
    public void testBindingRpcShortcut() throws Exception {
        final var baKnockResult = knockResult(true, "open");
        knockRpcImpl.registerTo(baRpcProviderService, BA_NODE_ID).setKnockKnockResult(baKnockResult);

        final var baKnockService = baRpcService.getRpc(KnockKnock.class);

        final var baKnockKnockInput = knockKnock(BA_NODE_ID).setQuestion("who's there?").build();

        final var rpcResult = Futures.getDone(baKnockService.invoke(baKnockKnockInput));

        assertEquals(baKnockResult.get().getResult().getClass(), rpcResult.getResult().getClass());
        assertSame(baKnockResult.get().getResult(), rpcResult.getResult());
        assertSame(baKnockKnockInput, knockRpcImpl.getReceivedKnocks().get(BA_NODE_ID).iterator().next());
    }

    @Test
    public void testSimpleRpc() throws Exception {
        baRpcProviderService.registerRpcImplementation((KnockKnock) input -> knockResult(true, "open"));

        final var baKnockService = baRpcService.getRpc(KnockKnock.class);
        final var rpcResult = Futures.getDone(
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

    private static KnockKnockInputBuilder knockKnock(final DataObjectIdentifier<TopLevelList> listId) {
        return new KnockKnockInputBuilder().setKnockerId(listId);
    }

    private ContainerNode toDOMKnockKnockInput(final KnockKnockInput from) {
        return testContext.getCodec().currentSerializer().toNormalizedNodeRpcData(from);
    }

    private static final class KnockKnockImpl implements KnockKnock {
        private final Multimap<BindingInstanceIdentifier, KnockKnockInput> receivedKnocks = HashMultimap.create();
        private ListenableFuture<RpcResult<KnockKnockOutput>> knockKnockResult;
        private Registration registration;

        KnockKnockImpl setKnockKnockResult(
                final ListenableFuture<RpcResult<KnockKnockOutput>> kkOutput) {
            knockKnockResult = kkOutput;
            return this;
        }

        Multimap<BindingInstanceIdentifier, KnockKnockInput> getReceivedKnocks() {
            return receivedKnocks;
        }

        KnockKnockImpl registerTo(final RpcProviderService registry, final DataObjectIdentifier<?>... paths) {
            registration = registry.registerRpcImplementation(this, ImmutableSet.copyOf(paths));
            assertNotNull(registration);
            return this;
        }

        @Override
        public ListenableFuture<RpcResult<KnockKnockOutput>> invoke(final KnockKnockInput input) {
            receivedKnocks.put(input.getKnockerId(), input);
            return knockKnockResult;
        }
    }
}
