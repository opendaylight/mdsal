/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.broker;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.junit.Test;
import org.opendaylight.mdsal.dom.api.DOMRpcException;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementationNotAvailableException;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.mdsal.dom.broker.test.util.TestModel;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;


public class GlobalDOMRpcRoutingTableEntryTest {
    private static final DataContainerChild<?, ?> OUTER_LIST = ImmutableNodes.mapNodeBuilder(TestModel.OUTER_LIST_QNAME)
            .withChild(ImmutableNodes.mapEntry(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, 1))
            .build();

    private static final NormalizedNode<?, ?> TEST_CONTAINER = Builders.containerBuilder()
            .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(TestModel.TEST_QNAME))
            .withChild(OUTER_LIST)
            .build();

    private static final String EXCEPTION_TEXT = "testException";

    @Test
    public void basicTest() throws Exception {
        final RpcDefinition rpcDefinition = mock(RpcDefinition.class);
        final Map<YangInstanceIdentifier, List<DOMRpcImplementation>> rpcImplementations = new HashMap<>();
        final List<DOMRpcImplementation> rpcImplementation = new ArrayList<>();
        final YangInstanceIdentifier yangInstanceIdentifier = YangInstanceIdentifier.builder().build();

        doReturn(SchemaPath.ROOT).when(rpcDefinition).getPath();

        rpcImplementation.add(new TestRpcImplementation());
        rpcImplementations.put(yangInstanceIdentifier, rpcImplementation);

        final GlobalDOMRpcRoutingTableEntry globalDOMRpcRoutingTableEntry =
                new GlobalDOMRpcRoutingTableEntry(rpcDefinition, new HashMap<>());

        assertTrue(globalDOMRpcRoutingTableEntry.getSchemaPath().equals(SchemaPath.ROOT));
        assertTrue(globalDOMRpcRoutingTableEntry.getImplementations().isEmpty());
        assertFalse(globalDOMRpcRoutingTableEntry
                .newInstance(rpcImplementations).getImplementations().isEmpty());
        assertTrue(globalDOMRpcRoutingTableEntry
                .newInstance(rpcImplementations).getImplementations().containsValue(rpcImplementation));

        try{
            globalDOMRpcRoutingTableEntry.newInstance(rpcImplementations)
                    .invokeRpc(TEST_CONTAINER).checkedGet();
        }catch(DOMRpcImplementationNotAvailableException e){
            assertTrue(e.getMessage().contains(EXCEPTION_TEXT));
        }
    }

    private static final class TestRpcImplementation implements DOMRpcImplementation {
        private final CheckedFuture<DOMRpcResult, DOMRpcException> unknownRpc;

        private TestRpcImplementation() {
            unknownRpc = Futures.immediateFailedCheckedFuture(
                    new DOMRpcImplementationNotAvailableException(EXCEPTION_TEXT));
        }

        @Nonnull
        public CheckedFuture<DOMRpcResult, DOMRpcException> invokeRpc(
                @Nonnull DOMRpcIdentifier rpc, @Nullable NormalizedNode<?, ?> input) {
            return unknownRpc;
        }
    }
}