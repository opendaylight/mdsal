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
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Collections;
import javax.annotation.Nonnull;
import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;

public class ForwardingDOMRpcProviderServiceTest extends ForwardingDOMRpcProviderService {

    @Mock(name = "domRpcProviderService")
    private DOMRpcProviderService domRpcProviderService;

    @Test
    public void basicTest() throws Exception {
        initMocks(this);

        final DOMRpcImplementation domRpcImplementation = mock(DOMRpcImplementation.class);

        doReturn(null).when(domRpcProviderService).registerRpcImplementation(domRpcImplementation);
        this.registerRpcImplementation(domRpcImplementation);
        verify(domRpcProviderService).registerRpcImplementation(domRpcImplementation);

        doReturn(null).when(domRpcProviderService).registerRpcImplementation(domRpcImplementation,
                Collections.EMPTY_SET);
        this.registerRpcImplementation(domRpcImplementation, Collections.EMPTY_SET);
        verify(domRpcProviderService).registerRpcImplementation(domRpcImplementation, Collections.EMPTY_SET);
    }

    @Nonnull
    @Override
    protected DOMRpcProviderService delegate() {
        return domRpcProviderService;
    }
}