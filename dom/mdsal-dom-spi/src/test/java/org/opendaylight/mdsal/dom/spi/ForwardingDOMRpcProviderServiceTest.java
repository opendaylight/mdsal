/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ForwardingDOMRpcProviderServiceTest extends ForwardingDOMRpcProviderService {

    @Mock(name = "domRpcProviderService")
    public DOMRpcProviderService domRpcProviderService;

    @Test
    public void basicTest() throws Exception {
        final DOMRpcImplementation domRpcImplementation = mock(DOMRpcImplementation.class);

        doReturn(null).when(domRpcProviderService).registerRpcImplementation(domRpcImplementation);
        this.registerRpcImplementation(domRpcImplementation);
        verify(domRpcProviderService).registerRpcImplementation(domRpcImplementation);

        doReturn(null).when(domRpcProviderService).registerRpcImplementation(domRpcImplementation,
                Collections.emptySet());
        this.registerRpcImplementation(domRpcImplementation, Collections.emptySet());
        verify(domRpcProviderService).registerRpcImplementation(domRpcImplementation, Collections.emptySet());
    }

    @Override
    protected DOMRpcProviderService delegate() {
        return domRpcProviderService;
    }
}