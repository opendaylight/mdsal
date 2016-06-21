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

import java.util.Collections;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class DefaultDOMRpcResultTest {

    @Test
    public void basicTest() throws Exception {
        RpcError rpcError = mock(RpcError.class);
        NormalizedNode normalizedNode = mock(NormalizedNode.class);
        DefaultDOMRpcResult defaultDOMRpcResult = new DefaultDOMRpcResult(normalizedNode, rpcError);
        assertEquals(normalizedNode, defaultDOMRpcResult.getResult());
        assertTrue(defaultDOMRpcResult.getErrors().contains(rpcError));
        assertTrue(new DefaultDOMRpcResult(normalizedNode).getErrors().isEmpty());
        assertTrue(new DefaultDOMRpcResult().getErrors().isEmpty());
        assertTrue(new DefaultDOMRpcResult(Collections.EMPTY_LIST).getErrors().isEmpty());
        assertEquals(defaultDOMRpcResult.hashCode(), new DefaultDOMRpcResult(normalizedNode, rpcError).hashCode());
        assertTrue(new DefaultDOMRpcResult(normalizedNode, rpcError).equals(defaultDOMRpcResult));
        assertTrue(defaultDOMRpcResult.equals(defaultDOMRpcResult));
        assertFalse(defaultDOMRpcResult.equals("test"));
        assertFalse(defaultDOMRpcResult.equals(new DefaultDOMRpcResult()));
    }
}