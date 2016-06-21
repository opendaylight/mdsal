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

import javax.annotation.Nonnull;
import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.mdsal.dom.api.DOMRpcAvailabilityListener;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class ForwardingDOMRpcServiceTest extends ForwardingDOMRpcService {

    @Mock(name = "domRpcService")
    private DOMRpcService domRpcService;

    @Test
    public void basicTest() throws Exception {
        initMocks(this);

        final DOMRpcAvailabilityListener domRpcAvailabilityListener = mock(DOMRpcAvailabilityListener.class);

        doReturn(null).when(domRpcService).invokeRpc(SchemaPath.SAME, null);
        this.invokeRpc(SchemaPath.SAME, null);
        verify(domRpcService).invokeRpc(SchemaPath.SAME, null);

        doReturn(null).when(domRpcService).registerRpcListener(domRpcAvailabilityListener);
        this.registerRpcListener(domRpcAvailabilityListener);
        verify(domRpcService).registerRpcListener(domRpcAvailabilityListener);
    }

    @Nonnull
    @Override
    protected DOMRpcService delegate() {
        return domRpcService;
    }
}