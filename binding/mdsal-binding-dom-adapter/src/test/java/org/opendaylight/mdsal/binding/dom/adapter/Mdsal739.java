/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.dom.adapter.test.util.BindingBrokerTestFactory;
import org.opendaylight.mdsal.binding.dom.adapter.test.util.BindingTestContext;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.rpcservice.rev140701.OpendaylightTestRpcServiceService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.rpcservice.rev140701.RockTheHouseInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.rpcservice.rev140701.RockTheHouseInputBuilder;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class Mdsal739 {
    private ListeningExecutorService executorService;
    private AdapterContext adapterContext;

    @Before
    public void before() {
        executorService = MoreExecutors.newDirectExecutorService();

        final BindingBrokerTestFactory bindingBrokerTestFactory = new BindingBrokerTestFactory();
        bindingBrokerTestFactory.setExecutor(executorService);
        final BindingTestContext bindingTestContext = bindingBrokerTestFactory.getTestContext();
        bindingTestContext.start();

        adapterContext = bindingTestContext.getCodec();
    }

    @After
    public void after() {
        executorService.shutdownNow();
    }

    @Test
    public void testRpcInputName() {
        final var rpcService = mock(DOMRpcService.class);

        final var captor = ArgumentCaptor.forClass(NormalizedNode.class);
        doReturn(Futures.immediateFailedFuture(new Throwable())).when(rpcService).invokeRpc(any(), captor.capture());
        final var adapter = (OpendaylightTestRpcServiceService) new RpcServiceAdapter(
            OpendaylightTestRpcServiceService.class, adapterContext, rpcService).getProxy();

        final var result = adapter.rockTheHouse(new RockTheHouseInputBuilder().build());
        assertThrows(ExecutionException.class, () -> Futures.getDone(result));
        final var input = captor.getValue();
        assertThat(input, instanceOf(ContainerNode.class));
        assertEquals(new NodeIdentifier(RockTheHouseInput.QNAME), input.getIdentifier());
    }
}
