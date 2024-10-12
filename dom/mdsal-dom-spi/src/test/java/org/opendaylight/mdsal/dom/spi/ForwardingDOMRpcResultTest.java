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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;

@ExtendWith(MockitoExtension.class)
class ForwardingDOMRpcResultTest {
    @Mock
    private DOMRpcResult delegate;

    private ForwardingDOMRpcResult result;

    @BeforeEach
    void beforeEach() {
        result = new ForwardingDOMRpcResult() {
            @Override
            protected DOMRpcResult delegate() {
                return delegate;
            }
        };
    }

    @Test
    void basicTest() {
        doReturn(null).when(delegate).errors();
        result.errors();
        verify(delegate).errors();

        doReturn(null).when(delegate).value();
        result.value();
        verify(delegate).value();
    }
}