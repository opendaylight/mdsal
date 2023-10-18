/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.store;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.opendaylight.mdsal.dom.spi.store.SnapshotBackedWriteTransaction.TransactionReadyPrototype;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeSnapshot;

class SnapshotBackedTransactionsTest {
    @Test
    void basicTest() throws Exception {
        final DataTreeSnapshot dataTreeSnapshot = mock(DataTreeSnapshot.class);
        final DataTreeModification dataTreeModification = mock(DataTreeModification.class);
        final TransactionReadyPrototype<Object> transactionReadyPrototype =  mock(TransactionReadyPrototype.class);
        doReturn(dataTreeModification).when(dataTreeSnapshot).newModification();

        assertNotNull(SnapshotBackedTransactions.newReadTransaction(new Object(), false, dataTreeSnapshot));
        assertNotNull(SnapshotBackedTransactions.newWriteTransaction(
                new Object(), false, dataTreeSnapshot, transactionReadyPrototype));
        assertNotNull(SnapshotBackedTransactions.newReadWriteTransaction(
                new Object(), false, dataTreeSnapshot, transactionReadyPrototype));
    }
}