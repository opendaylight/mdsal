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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

public class DefaultDOMRpcResultTest {
    @Test
    public void basicTest() {
        RpcError rpcError = mock(RpcError.class);
        ContainerNode normalizedNode = mock(ContainerNode.class);
        DefaultDOMRpcResult defaultDOMRpcResult = new DefaultDOMRpcResult(normalizedNode, rpcError);
        assertEquals(normalizedNode, defaultDOMRpcResult.value());
        assertTrue(defaultDOMRpcResult.errors().contains(rpcError));
        assertTrue(new DefaultDOMRpcResult(normalizedNode).errors().isEmpty());
        assertTrue(new DefaultDOMRpcResult().errors().isEmpty());
        assertTrue(new DefaultDOMRpcResult(List.of()).errors().isEmpty());
        assertEquals(defaultDOMRpcResult.hashCode(), new DefaultDOMRpcResult(normalizedNode, rpcError).hashCode());
        assertTrue(new DefaultDOMRpcResult(normalizedNode, rpcError).equals(defaultDOMRpcResult));
        assertTrue(defaultDOMRpcResult.equals(defaultDOMRpcResult));
        assertFalse(defaultDOMRpcResult.equals("test"));
        assertFalse(defaultDOMRpcResult.equals(new DefaultDOMRpcResult()));
    }
}