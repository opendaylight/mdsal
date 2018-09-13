/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.OPERATIONAL;

import org.junit.Test;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCursorAwareTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducer;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeService;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMTransactionChainListener;
import org.opendaylight.mdsal.dom.broker.util.TestModel;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;

public class ShardedDOMTransactionChainAdapterTest {

    @Test
    public void basicTest() throws Exception {
        String identifier = "TestIdent";
        DOMDataTreeService dataTreeService = mock(DOMDataTreeService.class);
        DOMDataTreeProducer producer = mock(DOMDataTreeProducer.class);
        DOMDataTreeCursorAwareTransaction transaction = mock(DOMDataTreeCursorAwareTransaction.class);
        DOMDataTreeWriteCursor cursor = mock(DOMDataTreeWriteCursor.class);
        doReturn(producer).when(dataTreeService).createProducer(any());
        doReturn(transaction).when(producer).createTransaction(true);
        doReturn(cursor).when(transaction).createCursor(any());
        doNothing().when(producer).close();


        DOMTransactionChainListener chainListener = new BlockingTransactionChainListener();
        ShardedDOMTransactionChainAdapter transactionChainAdapter =
                new ShardedDOMTransactionChainAdapter(identifier, dataTreeService, chainListener);

        DOMDataTreeWriteTransaction writeTransaction = transactionChainAdapter.newWriteOnlyTransaction();
        assertNotNull(writeTransaction);
        assertNotNull(writeTransaction.getIdentifier());

        doNothing().when(cursor).write(any(), any());
        writeTransaction.put(OPERATIONAL, TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME));
        verify(cursor).write(any(), any());

        doNothing().when(cursor).merge(any(), any());
        writeTransaction.merge(OPERATIONAL, TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME));
        verify(cursor).merge(any(), any());

        doNothing().when(cursor).delete(any());
        writeTransaction.delete(OPERATIONAL, TestModel.TEST_PATH);
        verify(cursor).delete(any());

        doNothing().when(cursor).close();
        doReturn(CommitInfo.emptyFluentFuture()).when(transaction).commit();
        doReturn(true).when(transaction).cancel();
        assertTrue(writeTransaction.cancel());
        transactionChainAdapter.closeWriteTransaction(FluentFutures.immediateNullFluentFuture());

        transactionChainAdapter = new ShardedDOMTransactionChainAdapter(identifier, dataTreeService, chainListener);
        writeTransaction = transactionChainAdapter.newWriteOnlyTransaction();
        writeTransaction.put(OPERATIONAL, TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME));
        assertNotNull(writeTransaction.commit());
        assertFalse(writeTransaction.cancel());
        transactionChainAdapter.closeWriteTransaction(FluentFutures.immediateNullFluentFuture());

        assertNotNull(transactionChainAdapter.newWriteOnlyTransaction().commit());

        DOMDataTreeReadTransaction readTransaction = transactionChainAdapter.newReadOnlyTransaction();
        assertNotNull(readTransaction);
        transactionChainAdapter.closeReadTransaction();
        transactionChainAdapter.close();
    }
}
