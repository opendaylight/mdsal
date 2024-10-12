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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.dom.spi.store.SnapshotBackedWriteTransaction.TransactionReadyPrototype;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeSnapshot;

@ExtendWith(MockitoExtension.class)
class SnapshotBackedTransactionsTest {
    @Mock
    private DataTreeSnapshot dataTreeSnapshot;
    @Mock
    private DataTreeModification dataTreeModification;
    @Mock
    private TransactionReadyPrototype<Object> transactionReadyPrototype;

    @Test
    void basicTest() {
        doReturn(dataTreeModification).when(dataTreeSnapshot).newModification();

        assertNotNull(SnapshotBackedTransactions.newReadTransaction(new Object(), false, dataTreeSnapshot));
        assertNotNull(SnapshotBackedTransactions.newWriteTransaction(
                new Object(), false, dataTreeSnapshot, transactionReadyPrototype));
        assertNotNull(SnapshotBackedTransactions.newReadWriteTransaction(
                new Object(), false, dataTreeSnapshot, transactionReadyPrototype));
    }
}