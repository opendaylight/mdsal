/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Test;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

public class TransactionChainReadTransactionTest {

    @Test
    public void basicTest() throws Exception {
        final String identifier = "testIdent";
        final DOMDataTreeReadTransaction readTransaction = mock(DOMDataTreeReadTransaction.class);
        final ShardedDOMTransactionChainAdapter chainAdapter = mock(ShardedDOMTransactionChainAdapter.class);
        ListenableFuture<Void> previousWriteTxFuture = Futures.immediateFuture(null);

        TransactionChainReadTransaction transactionChainReadTransaction =
                new TransactionChainReadTransaction(identifier, readTransaction, previousWriteTxFuture, chainAdapter);

        assertEquals(identifier, transactionChainReadTransaction.getIdentifier());

        doNothing().when(readTransaction).close();
        doNothing().when(chainAdapter).closeReadTransaction();
        transactionChainReadTransaction.close();
        verify(readTransaction).close();
        verify(chainAdapter).closeReadTransaction();

        doReturn(Futures.immediateCheckedFuture(null)).when(readTransaction).read(any(), any());
        assertNotNull(transactionChainReadTransaction.exists(
                LogicalDatastoreType.OPERATIONAL, YangInstanceIdentifier.EMPTY));
        transactionChainReadTransaction.read(LogicalDatastoreType.OPERATIONAL, YangInstanceIdentifier.EMPTY);
        verify(readTransaction, atLeastOnce()).read(LogicalDatastoreType.OPERATIONAL, YangInstanceIdentifier.EMPTY);

        doReturn(Futures.immediateFailedCheckedFuture(
                new NullPointerException())).when(readTransaction).read(any(), any());
        doNothing().when(chainAdapter).transactionFailed(any(), any());
        assertNotNull(transactionChainReadTransaction.read(
                LogicalDatastoreType.OPERATIONAL, YangInstanceIdentifier.EMPTY));
        previousWriteTxFuture = Futures.immediateFailedFuture(new NullPointerException());
        transactionChainReadTransaction =
                new TransactionChainReadTransaction(identifier, readTransaction, previousWriteTxFuture, chainAdapter);
        assertNotNull(transactionChainReadTransaction.read(
                LogicalDatastoreType.OPERATIONAL, YangInstanceIdentifier.EMPTY));
    }
}