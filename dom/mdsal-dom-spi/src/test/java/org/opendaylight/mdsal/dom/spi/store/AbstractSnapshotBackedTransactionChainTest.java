/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.store;

import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeSnapshot;

@ExtendWith(MockitoExtension.class)
class AbstractSnapshotBackedTransactionChainTest {
    @Mock
    private DataTreeSnapshot dataTreeSnapshot;
    @Mock
    private DOMStoreThreePhaseCommitCohort domStoreThreePhaseCommitCohort;
    @Mock
    private DataTreeModification dataTreeModification;
    @Mock
    private SnapshotBackedWriteTransaction<Object> snapshotBackedWriteTransaction;

    @Test
    void basicTest() {
        doReturn(dataTreeModification).when(dataTreeSnapshot).newModification();

        final var chain = new AbstractSnapshotBackedTransactionChain<>() {
            @Override
            protected Object nextTransactionIdentifier() {
                return new Object();
            }

            @Override
            protected boolean getDebugTransactions() {
                return false;
            }

            @Override
            protected DataTreeSnapshot takeSnapshot() {
                return dataTreeSnapshot;
            }

            @Override
            protected DOMStoreThreePhaseCommitCohort createCohort(
                    final SnapshotBackedWriteTransaction<Object> transaction, final DataTreeModification modification,
                    final Exception operationError) {
                return domStoreThreePhaseCommitCohort;
            }
        };

        chain.newReadOnlyTransaction().close();
        chain.newWriteOnlyTransaction().close();
        chain.newReadWriteTransaction().close();

        chain.transactionReady(snapshotBackedWriteTransaction, dataTreeModification, null);

        chain.transactionAborted(snapshotBackedWriteTransaction);
        chain.close();

        chain.onTransactionCommited(snapshotBackedWriteTransaction);
        chain.onTransactionFailed(snapshotBackedWriteTransaction, null);
    }
}