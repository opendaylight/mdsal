/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.binding.dom.adapter.test.util.BindingBrokerTestFactory;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.rpcservice.rev140701.RockTheHouse;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.rpcservice.rev140701.RockTheHouseInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.rpcservice.rev140701.RockTheHouseInputBuilder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;

@ExtendWith(MockitoExtension.class)
class Mdsal739Test {
    private final ListeningExecutorService executorService = MoreExecutors.newDirectExecutorService();

    @Mock
    private DOMRpcService rpcService;
    @Captor
    private ArgumentCaptor<ContainerNode> captor;

    private AdapterContext adapterContext;

    @BeforeEach
    void before() {
        final var bindingBrokerTestFactory = new BindingBrokerTestFactory();
        bindingBrokerTestFactory.setExecutor(executorService);
        final var bindingTestContext = bindingBrokerTestFactory.getTestContext();
        bindingTestContext.start();

        adapterContext = bindingTestContext.getCodec();
    }

    @AfterEach
    void after() {
        executorService.shutdownNow();
    }

    @Test
    void testRpcInputName() {
        doReturn(Futures.immediateFailedFuture(new Throwable())).when(rpcService).invokeRpc(any(), captor.capture());
        final var adapter = (RockTheHouse) new RpcAdapter(adapterContext, rpcService, RockTheHouse.class).facade();

        final var result = adapter.invoke(new RockTheHouseInputBuilder().setZipCode("12345").build());
        assertThrows(ExecutionException.class, () -> Futures.getDone(result));
        final var input = captor.getValue();
        assertSame(NodeIdentifier.create(RockTheHouseInput.QNAME), input.name());
        final var body = input.body();
        assertEquals(1, body.size());
        assertEquals(ImmutableNodes.leafNode(QName.create(RockTheHouseInput.QNAME, "zip-code"), "12345"),
            body.iterator().next());
    }
}
