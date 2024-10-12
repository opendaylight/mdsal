/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;

@ExtendWith(MockitoExtension.class)
class ForwardingDOMRpcProviderServiceTest {
    @Mock
    private DOMRpcProviderService delegate;
    @Mock
    private DOMRpcImplementation domRpcImplementation;

    private ForwardingDOMRpcProviderService service;

    @BeforeEach
    void beforeEach() {
        service = new ForwardingDOMRpcProviderService() {
            @Override
            protected DOMRpcProviderService delegate() {
                return delegate;
            }
        };
    }

    @Test
    void basicTest() {
        doReturn(null).when(delegate).registerRpcImplementation(domRpcImplementation);
        service.registerRpcImplementation(domRpcImplementation);
        verify(delegate).registerRpcImplementation(domRpcImplementation);

        doReturn(null).when(delegate).registerRpcImplementation(domRpcImplementation, Set.of());
        service.registerRpcImplementation(domRpcImplementation, Set.of());
        verify(delegate).registerRpcImplementation(domRpcImplementation, Set.of());
    }
}