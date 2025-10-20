/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doReturn;

import com.google.common.util.concurrent.Futures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

@ExtendWith(MockitoExtension.class)
class ForwardingDOMRpcImplementationTest {
    @Mock
    private DOMRpcImplementation domRpcImplementation;
    @Mock
    private DOMRpcIdentifier domRpcIdentifier;
    @Mock
    private ContainerNode input;
    @Mock
    private DOMRpcResult result;

    @Test
    void basicTest() {
        var impl = new ForwardingDOMRpcImplementation() {
            @Override
            protected DOMRpcImplementation delegate() {
                return domRpcImplementation;
            }
        };

        var future = Futures.immediateFuture(result);
        doReturn(future).when(domRpcImplementation).invokeRpc(domRpcIdentifier, input);
        assertSame(future, impl.invokeRpc(domRpcIdentifier, input));
    }
}