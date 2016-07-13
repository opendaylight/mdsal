/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.util.concurrent.MoreExecutors;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.adapter.test.util.BindingBrokerTestFactory;
import org.opendaylight.mdsal.binding.dom.adapter.test.util.BindingTestContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.test.bi.ba.rpcservice.rev140701.OpendaylightTestRpcServiceService;
import org.opendaylight.yangtools.binding.data.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.RpcEffectiveStatementImpl;

public class LazySerializedContainerNodeTest {

    @Test
    public void basicTest() throws Exception {
        final SchemaPath rpcName;
        final DataObject dataObject = mock(DataObject.class);
        final BindingNormalizedNodeCodecRegistry codec = mock(BindingNormalizedNodeCodecRegistry.class);
        final ContainerNode containerNode = mock(ContainerNode.class);
        doReturn(containerNode).when(codec).toNormalizedNodeRpcData(any());
        doReturn(Optional.absent()).when(containerNode).getChild(any());

        final BindingBrokerTestFactory bindingBrokerTestFactory = new BindingBrokerTestFactory();
        bindingBrokerTestFactory.setExecutor(MoreExecutors.newDirectExecutorService());
        final BindingTestContext bindingTestContext = bindingBrokerTestFactory.getTestContext();
        bindingTestContext.start();

        final ImmutableBiMap biMap =
                bindingTestContext.getCodec().getRpcMethodToSchema(OpendaylightTestRpcServiceService.class);
        rpcName = ((RpcEffectiveStatementImpl) biMap.values().iterator().next()).getPath();
        final LeafNode leafNode = ImmutableLeafNodeBuilder.create().withNodeIdentifier(NodeIdentifier
                .create(QName.create("test"))).build();
        final NormalizedNode normalizedNode = LazySerializedContainerNode.create(rpcName, dataObject, codec);
        assertNotNull(normalizedNode);
        final LazySerializedContainerNode lazySerializedContainerNode =
                (LazySerializedContainerNode) LazySerializedContainerNode.withContextRef(rpcName, dataObject, leafNode,
                        codec);
        assertNotNull(lazySerializedContainerNode);
        assertEquals(leafNode, lazySerializedContainerNode.getChild(leafNode.getIdentifier()).get());
        assertFalse(lazySerializedContainerNode.getChild(mock(PathArgument.class)).isPresent());
        assertTrue(lazySerializedContainerNode.getAttributes().isEmpty());

        assertTrue(lazySerializedContainerNode.getValue().isEmpty());
        assertEquals(lazySerializedContainerNode.getIdentifier().getNodeType(), lazySerializedContainerNode.getNodeType());
        assertEquals(rpcName.getLastComponent(), lazySerializedContainerNode.getIdentifier().getNodeType());
        assertNull(lazySerializedContainerNode.getAttributeValue(null));
        assertEquals(dataObject, lazySerializedContainerNode.bindingData());
    }
}