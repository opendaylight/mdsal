/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.lang.reflect.Field;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

public class RpcRoutingStrategyTest {

    private static RpcRoutingStrategy rpcRoutingStrategy;
    private static final QName Q_NAME = QName.create("testQname");
    private static final RpcDefinition RPC_DEFINITION = mock(RpcDefinition.class);
    private static final DataSchemaNode DATA_SCHEMA_NODE = mock(DataSchemaNode.class);
    private static final UnknownSchemaNode UNKNOWN_SCHEMA_NODE = mock(UnknownSchemaNode.class);

    @Before
    public void setUp() throws Exception {
        final ContainerSchemaNode containerSchemaNode = mock(ContainerSchemaNode.class);

        doReturn(containerSchemaNode).when(RPC_DEFINITION).getInput();
        doReturn(ImmutableSet.of(DATA_SCHEMA_NODE)).when(containerSchemaNode).getChildNodes();
        doReturn(ImmutableList.of(UNKNOWN_SCHEMA_NODE)).when(DATA_SCHEMA_NODE).getUnknownSchemaNodes();
        doReturn(QName.create("testNode")).when(UNKNOWN_SCHEMA_NODE).getNodeType();
        doReturn(Q_NAME).when(RPC_DEFINITION).getQName();

        rpcRoutingStrategy = RpcRoutingStrategy.from(RPC_DEFINITION);
        assertNotNull(rpcRoutingStrategy);

        assertEquals(Q_NAME, rpcRoutingStrategy.getIdentifier());
        assertFalse(rpcRoutingStrategy.isContextBasedRouted());
    }

    @Test()
    public void routedRpcStrategyTest() throws Exception {
        final Field contextReferenceField = RpcRoutingStrategy.class.getDeclaredField("CONTEXT_REFERENCE");
        contextReferenceField.setAccessible(true);

        final QName contextReference = (QName) contextReferenceField.get(rpcRoutingStrategy);

        reset(UNKNOWN_SCHEMA_NODE);
        doReturn(contextReference).when(UNKNOWN_SCHEMA_NODE).getNodeType();
        doReturn(Q_NAME).when(UNKNOWN_SCHEMA_NODE).getQName();
        doReturn(Q_NAME).when(DATA_SCHEMA_NODE).getQName();
        rpcRoutingStrategy = RpcRoutingStrategy.from(RPC_DEFINITION);

        assertNotNull(rpcRoutingStrategy);

        assertTrue(rpcRoutingStrategy.isContextBasedRouted());
        assertEquals(Q_NAME, rpcRoutingStrategy.getContext());
        assertEquals(Q_NAME, rpcRoutingStrategy.getLeaf());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getLeafTest() throws Exception {
        rpcRoutingStrategy.getLeaf();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getContextTest() throws Exception {
        rpcRoutingStrategy.getContext();
    }
}