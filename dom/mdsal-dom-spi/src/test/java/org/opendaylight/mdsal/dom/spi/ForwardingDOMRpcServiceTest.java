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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.dom.api.DOMRpcAvailabilityListener;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

@ExtendWith(MockitoExtension.class)
class ForwardingDOMRpcServiceTest {
    @Mock
    private DOMRpcService delegate;
    @Mock
    private DOMRpcAvailabilityListener listener;
    @Mock
    private ContainerNode input;
    @Mock
    private DOMRpcResult result;
    @Mock
    private Registration registration;
    @Spy
    private ForwardingDOMRpcService service;

    @BeforeEach
    void beforeEach() {
        doReturn(delegate).when(service).delegate();
    }

    @Test
    void invokeRpcForwards() {
        final var id = QName.create("urn:foo", "foo");

        final var future = Futures.immediateFuture(result);
        doReturn(future).when(delegate).invokeRpc(id, input);
        assertSame(future, service.invokeRpc(id, input));
    }

    @Test
    void registerListenerForwards() {
        doReturn(registration).when(delegate).registerRpcListener(listener);
        assertSame(registration, service.registerRpcListener(listener));
    }
}