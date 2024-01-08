/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;

@ExtendWith(MockitoExtension.class)
class ForwardingDOMDataBrokerTest {
    private interface Extension extends DOMDataBroker.Extension {
        // Nothing else
    }

    @Mock
    private DOMTransactionChain chain;
    @Mock
    private Extension extension;
    @Mock
    private DOMDataBroker domDataBroker;
    @Mock
    private DOMDataTreeReadTransaction readTx;
    @Mock
    private DOMDataTreeWriteTransaction writeTx;

    @Test
    void basicTest() throws Exception {
        final var impl = new ForwardingDOMDataBroker() {
            @Override
            protected DOMDataBroker delegate() {
                return domDataBroker;
            }
        };

        doReturn(chain).when(domDataBroker).createTransactionChain();
        assertSame(chain, impl.createTransactionChain());

        doReturn(List.of(extension)).when(domDataBroker).supportedExtensions();
        assertSame(extension, impl.extension(Extension.class));

        doReturn(readTx).when(domDataBroker).newReadOnlyTransaction();
        assertSame(readTx, impl.newReadOnlyTransaction());

        doReturn(writeTx).when(domDataBroker).newWriteOnlyTransaction();
        assertSame(writeTx, impl.newWriteOnlyTransaction());
    }
}