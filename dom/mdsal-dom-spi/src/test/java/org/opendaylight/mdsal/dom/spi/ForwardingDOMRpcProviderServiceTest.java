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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;

@ExtendWith(MockitoExtension.class)
class ForwardingDOMRpcProviderServiceTest extends ForwardingDOMRpcProviderService {
    @Mock(name = "domRpcProviderService")
    private DOMRpcProviderService domRpcProviderService;
    @Mock
    private  DOMRpcImplementation domRpcImplementation;

    @Test
    void basicTest() {
        doReturn(null).when(domRpcProviderService).registerRpcImplementation(domRpcImplementation);
        registerRpcImplementation(domRpcImplementation);
        verify(domRpcProviderService).registerRpcImplementation(domRpcImplementation);

        doReturn(null).when(domRpcProviderService).registerRpcImplementation(domRpcImplementation, Set.of());
        registerRpcImplementation(domRpcImplementation, Set.of());
        verify(domRpcProviderService).registerRpcImplementation(domRpcImplementation, Set.of());
    }

    @Override
    protected DOMRpcProviderService delegate() {
        return domRpcProviderService;
    }
}