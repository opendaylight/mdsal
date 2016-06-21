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
import static org.mockito.MockitoAnnotations.initMocks;

import javax.annotation.Nonnull;
import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;

public class ForwardingDOMTransactionChainTest extends ForwardingDOMTransactionChain {

    @Mock(name = "domTransactionChain")
    private DOMTransactionChain domTransactionChain;

    @Test
    public void basicTest() throws Exception {
        initMocks(this);

        doReturn(null).when(domTransactionChain).newWriteOnlyTransaction();
        this.newWriteOnlyTransaction();
        verify(domTransactionChain).newWriteOnlyTransaction();

        doReturn(null).when(domTransactionChain).newReadOnlyTransaction();
        this.newReadOnlyTransaction();
        verify(domTransactionChain).newReadOnlyTransaction();

        doNothing().when(domTransactionChain).close();
        this.close();
        verify(domTransactionChain).close();
    }

    @Nonnull
    @Override
    protected DOMTransactionChain delegate() {
        return domTransactionChain;
    }
}