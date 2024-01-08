/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ForwardingDOMTransactionChainTest extends ForwardingDOMTransactionChain {
    @Mock(name = "domTransactionChain")
    public DOMTransactionChain domTransactionChain;

    @Test
    public void basicTest() throws Exception {
        doReturn(null).when(domTransactionChain).newWriteOnlyTransaction();
        newWriteOnlyTransaction();
        verify(domTransactionChain).newWriteOnlyTransaction();

        doReturn(null).when(domTransactionChain).newReadOnlyTransaction();
        newReadOnlyTransaction();
        verify(domTransactionChain).newReadOnlyTransaction();

        doNothing().when(domTransactionChain).close();
        close();
        verify(domTransactionChain).close();
    }

    @Override
    protected DOMTransactionChain delegate() {
        return domTransactionChain;
    }
}
