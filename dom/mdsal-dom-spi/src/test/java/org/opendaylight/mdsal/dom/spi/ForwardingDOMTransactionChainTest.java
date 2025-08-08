/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;

@ExtendWith(MockitoExtension.class)
class ForwardingDOMTransactionChainTest {
    @Mock
    private DOMTransactionChain delegate;
    @Mock
    private DOMDataTreeReadTransaction readTx;
    @Mock
    private DOMDataTreeWriteTransaction writeTx;
    @Spy
    private ForwardingDOMTransactionChain chain;

    @BeforeEach
    void beforeEach() {
        doReturn(delegate).when(chain).delegate();
    }

    @Test
    void writeOnlyForwards() {
        doReturn(writeTx).when(delegate).newWriteOnlyTransaction();
        assertSame(writeTx, chain.newWriteOnlyTransaction());
    }

    @Test
    void readOnlyForwards() {
        doReturn(readTx).when(delegate).newReadOnlyTransaction();
        assertSame(readTx, chain.newReadOnlyTransaction());
    }

    @Test
    void closeForwards() {
        doNothing().when(delegate).close();
        chain.close();
        verify(delegate).close();
    }
}
