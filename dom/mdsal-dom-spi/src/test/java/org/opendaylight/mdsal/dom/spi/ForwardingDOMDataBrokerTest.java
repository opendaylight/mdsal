/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import javax.annotation.Nonnull;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;

public class ForwardingDOMDataBrokerTest extends ForwardingDOMDataBroker {

    @Mock(name = "domDataBroker")
    private DOMDataBroker domDataBroker;

    @Test
    public void basicTest() throws Exception {
        initMocks(this);

        Mockito.doReturn(null).when(domDataBroker).createTransactionChain(any());
        this.createTransactionChain(null);
        verify(domDataBroker).createTransactionChain(any());

        doReturn(null).when(domDataBroker).getSupportedExtensions();
        this.getSupportedExtensions();
        verify(domDataBroker).getSupportedExtensions();

        doReturn(null).when(domDataBroker).newReadOnlyTransaction();
        this.newReadOnlyTransaction();
        verify(domDataBroker).newReadOnlyTransaction();

        doReturn(null).when(domDataBroker).newWriteOnlyTransaction();
        this.newWriteOnlyTransaction();
        verify(domDataBroker).newWriteOnlyTransaction();
    }

    @Nonnull
    @Override
    protected DOMDataBroker delegate() {
        return domDataBroker;
    }
}