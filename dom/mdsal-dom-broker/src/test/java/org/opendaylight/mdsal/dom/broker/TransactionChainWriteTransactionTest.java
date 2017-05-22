/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import org.junit.Test;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;

public class TransactionChainWriteTransactionTest {

    @Test
    public void basicTest() throws Exception {
        final String identifier = "testIdent";
        final DOMDataTreeWriteTransaction writeTransaction = mock(DOMDataTreeWriteTransaction.class);
        final ShardedDOMTransactionChainAdapter chainAdapter = mock(ShardedDOMTransactionChainAdapter.class);

        final TransactionChainWriteTransaction transactionChainWriteTransaction =
                new TransactionChainWriteTransaction(identifier, writeTransaction, chainAdapter);

        assertEquals(identifier, transactionChainWriteTransaction.getIdentifier());

        doNothing().when(writeTransaction).put(any(), any(), any());
        transactionChainWriteTransaction.put(any(), any(), any());
        verify(writeTransaction).put(any(), any(), any());

        doNothing().when(writeTransaction).merge(any(), any(), any());
        transactionChainWriteTransaction.merge(any(), any(), any());
        verify(writeTransaction).merge(any(), any(), any());

        doReturn(false).when(writeTransaction).cancel();
        doNothing().when(chainAdapter).closeWriteTransaction(any());
        transactionChainWriteTransaction.cancel();
        verify(writeTransaction).cancel();

        doNothing().when(writeTransaction).delete(any(), any());
        transactionChainWriteTransaction.delete(any(), any());
        verify(writeTransaction).delete(any(), any());

        CheckedFuture<Void, TransactionCommitFailedException> writeResult = Futures.immediateCheckedFuture(null);
        doReturn(writeResult).when(writeTransaction).submit();
        assertEquals(writeResult, transactionChainWriteTransaction.submit());

        writeResult = Futures.immediateFailedCheckedFuture(mock(TransactionCommitFailedException.class));
        doNothing().when(chainAdapter).transactionFailed(any(), any());
        doReturn(writeResult).when(writeTransaction).submit();
        assertEquals(writeResult, transactionChainWriteTransaction.submit());
    }
}