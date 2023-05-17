/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ForwardingDOMDataBrokerTest extends ForwardingDOMDataBroker {
    @Mock(name = "domDataBroker")
    public DOMDataBroker domDataBroker;

    @Test
    public void basicTest() throws Exception {
        doReturn(null).when(domDataBroker).createTransactionChain(any());
        createTransactionChain(null);
        verify(domDataBroker).createTransactionChain(any());

        doCallRealMethod().when(domDataBroker).supportedExtensions();
        supportedExtensions();
        verify(domDataBroker).supportedExtensions();

        doReturn(null).when(domDataBroker).newReadOnlyTransaction();
        newReadOnlyTransaction();
        verify(domDataBroker).newReadOnlyTransaction();

        doReturn(null).when(domDataBroker).newWriteOnlyTransaction();
        newWriteOnlyTransaction();
        verify(domDataBroker).newWriteOnlyTransaction();
    }

    @Override
    protected DOMDataBroker delegate() {
        return domDataBroker;
    }
}