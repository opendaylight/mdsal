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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.util.concurrent.MoreExecutors;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.adapter.test.util.BindingBrokerTestFactory;
import org.opendaylight.mdsal.binding.dom.adapter.test.util.BindingTestContext;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.rpcservice.rev140701.RockTheHouseInput;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;

public class LazySerializedContainerNodeTest {
    @Test
    public void basicTest() {
        final DataObject dataObject = mock(DataObject.class);
        final BindingNormalizedNodeSerializer codec = mock(BindingNormalizedNodeSerializer.class);
        final ContainerNode containerNode = mock(ContainerNode.class);
        doReturn(containerNode).when(codec).toNormalizedNodeRpcData(any());
        doReturn(null).when(containerNode).childByArg(any());

        final BindingBrokerTestFactory bindingBrokerTestFactory = new BindingBrokerTestFactory();
        bindingBrokerTestFactory.setExecutor(MoreExecutors.newDirectExecutorService());
        final BindingTestContext bindingTestContext = bindingBrokerTestFactory.getTestContext();
        bindingTestContext.start();

        final NodeIdentifier name = new NodeIdentifier(RockTheHouseInput.QNAME);
        final LeafNode<String> leafNode = ImmutableNodes.leafNode(QName.create("", "test"), "");
        final ContainerNode normalizedNode = LazySerializedContainerNode.create(name, dataObject, codec);
        assertNotNull(normalizedNode);
        final LazySerializedContainerNode lazySerializedContainerNode =
                (LazySerializedContainerNode) LazySerializedContainerNode.withContextRef(name, dataObject, leafNode,
                        codec);
        assertNotNull(lazySerializedContainerNode);
        assertEquals(leafNode, lazySerializedContainerNode.childByArg(leafNode.getIdentifier()));
        assertNull(lazySerializedContainerNode.childByArg(new NodeIdentifier(QName.create("", "mismatch"))));

        assertTrue(lazySerializedContainerNode.body().isEmpty());
        assertSame(name, lazySerializedContainerNode.getIdentifier());
        assertEquals(dataObject, lazySerializedContainerNode.getDataObject());
    }
}