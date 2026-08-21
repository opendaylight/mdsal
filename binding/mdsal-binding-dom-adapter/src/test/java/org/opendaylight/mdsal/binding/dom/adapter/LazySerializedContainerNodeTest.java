/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.util.concurrent.MoreExecutors;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.adapter.test.util.BindingBrokerTestFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.rpcservice.rev140701.RockTheHouseInput;
import org.opendaylight.yangtools.binding.ContainerObject;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;

public class LazySerializedContainerNodeTest {
    @Test
    public void basicTest() {
        final var dataObject = mock(ContainerObject.class);
        final var codec = mock(BindingNormalizedNodeSerializer.class);
        final var containerNode = mock(ContainerNode.class);
        doReturn(containerNode).when(codec).toNormalizedNodeRpcData(any());
        doReturn(null).when(containerNode).childByArg(any());

        final var bindingBrokerTestFactory = new BindingBrokerTestFactory();
        bindingBrokerTestFactory.setExecutor(MoreExecutors.newDirectExecutorService());
        final var bindingTestContext = bindingBrokerTestFactory.getTestContext();
        bindingTestContext.start();

        final var name = new NodeIdentifier(RockTheHouseInput.QNAME);
        final var leafNode = ImmutableNodes.leafNode(QName.create("", "test"), "");
        final var normalizedNode = LazySerializedContainerNode.create(name, dataObject, codec);
        assertNotNull(normalizedNode);
        final var lazySerializedContainerNode = assertInstanceOf(LazySerializedContainerNode.class,
            LazySerializedContainerNode.withContextRef(name, dataObject, leafNode, codec));
        assertNotNull(lazySerializedContainerNode);
        assertEquals(leafNode, lazySerializedContainerNode.childByArg(leafNode.name()));
        assertNull(lazySerializedContainerNode.childByArg(new NodeIdentifier(QName.create("", "mismatch"))));

        assertTrue(lazySerializedContainerNode.body().isEmpty());
        assertSame(name, lazySerializedContainerNode.name());
        assertEquals(dataObject, lazySerializedContainerNode.getDataObject());
    }
}
