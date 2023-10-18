/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

@ExtendWith(MockitoExtension.class)
class DefaultDOMRpcResultTest {
    @Mock
    private RpcError rpcError;
    @Mock
    private ContainerNode normalizedNode;

    @Test
    void basicTest() {
        final var defaultDOMRpcResult = new DefaultDOMRpcResult(normalizedNode, rpcError);
        assertEquals(normalizedNode, defaultDOMRpcResult.value());
        assertTrue(defaultDOMRpcResult.errors().contains(rpcError));
        assertTrue(new DefaultDOMRpcResult(normalizedNode).errors().isEmpty());
        assertTrue(new DefaultDOMRpcResult().errors().isEmpty());
        assertTrue(new DefaultDOMRpcResult(List.of()).errors().isEmpty());
        assertEquals(defaultDOMRpcResult.hashCode(), new DefaultDOMRpcResult(normalizedNode, rpcError).hashCode());
        assertEquals(new DefaultDOMRpcResult(normalizedNode, rpcError), defaultDOMRpcResult);
        assertEquals(defaultDOMRpcResult, defaultDOMRpcResult);
        assertNotEquals("test", defaultDOMRpcResult);
        assertNotEquals(defaultDOMRpcResult, new DefaultDOMRpcResult());
    }
}