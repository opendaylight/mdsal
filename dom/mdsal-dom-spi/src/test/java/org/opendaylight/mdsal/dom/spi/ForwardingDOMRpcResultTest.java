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
import static org.mockito.MockitoAnnotations.initMocks;

import javax.annotation.Nonnull;
import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;

public class ForwardingDOMRpcResultTest extends ForwardingDOMRpcResult {

    @Mock(name = "domRpcResult")
    private DOMRpcResult domRpcResult;

    @Test
    public void basicTest() throws Exception {
        initMocks(this);

        doReturn(null).when(domRpcResult).getErrors();
        this.getErrors();
        verify(domRpcResult).getErrors();

        doReturn(null).when(domRpcResult).getResult();
        this.getResult();
        verify(domRpcResult).getResult();
    }

    @Nonnull
    @Override
    protected DOMRpcResult delegate() {
        return domRpcResult;
    }
}