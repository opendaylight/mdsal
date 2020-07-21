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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.dom.api.DOMRpcAvailabilityListener;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ForwardingDOMRpcServiceTest extends ForwardingDOMRpcService {
    @Mock(name = "domRpcService")
    private DOMRpcService domRpcService;

    @Test
    public void basicTest() throws Exception {
        final DOMRpcAvailabilityListener domRpcAvailabilityListener = mock(DOMRpcAvailabilityListener.class);
        final Absolute id = Absolute.of(QName.create("urn:foo", "foo"));

        doReturn(null).when(domRpcService).invokeRpc(id, null);
        this.invokeRpc(id, null);
        verify(domRpcService).invokeRpc(id, null);

        doReturn(null).when(domRpcService).registerRpcListener(domRpcAvailabilityListener);
        this.registerRpcListener(domRpcAvailabilityListener);
        verify(domRpcService).registerRpcListener(domRpcAvailabilityListener);
    }

    @Override
    protected DOMRpcService delegate() {
        return domRpcService;
    }
}