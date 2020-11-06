/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableClassToInstanceMap;
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
        this.createTransactionChain(null);
        verify(domDataBroker).createTransactionChain(any());

        doReturn(ImmutableClassToInstanceMap.of()).when(domDataBroker).getExtensions();
        this.getExtensions();
        verify(domDataBroker).getExtensions();

        doReturn(null).when(domDataBroker).newReadOnlyTransaction();
        this.newReadOnlyTransaction();
        verify(domDataBroker).newReadOnlyTransaction();

        doReturn(null).when(domDataBroker).newWriteOnlyTransaction();
        this.newWriteOnlyTransaction();
        verify(domDataBroker).newWriteOnlyTransaction();
    }

    @Override
    protected DOMDataBroker delegate() {
        return domDataBroker;
    }
}