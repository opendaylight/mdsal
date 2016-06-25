/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.store;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.MockitoAnnotations.initMocks;

import com.google.common.base.MoreObjects;
import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeSnapshot;

public class AbstractSnapshotBackedTransactionChainTest extends AbstractSnapshotBackedTransactionChain {

    @Mock
    private static DataTreeSnapshot dataTreeSnapshot;

    @Mock
    private static DOMStoreThreePhaseCommitCohort domStoreThreePhaseCommitCohort;

    @Test
    public void basicTest() throws Exception {
        initMocks(this);
        SnapshotBackedWriteTransaction snapshotBackedWriteTransaction = mock(SnapshotBackedWriteTransaction.class);
        DataTreeModification dataTreeModification = mock(DataTreeModification.class);
        doReturn(dataTreeModification).when(dataTreeSnapshot).newModification();
        doReturn(MoreObjects.toStringHelper(this)).when(snapshotBackedWriteTransaction).addToStringAttributes(any());

        this.newReadOnlyTransaction().close();
        this.newWriteOnlyTransaction().close();
        this.newReadWriteTransaction().close();

        this.transactionReady(snapshotBackedWriteTransaction, dataTreeModification);


        this.transactionAborted(snapshotBackedWriteTransaction);
        this.close();

        this.onTransactionCommited(snapshotBackedWriteTransaction);
        this.onTransactionFailed(snapshotBackedWriteTransaction, null);

    }

    @Override
    protected Object nextTransactionIdentifier() {
        System.out.println("nextTransactionIdentifier Invoked");
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
    protected DOMStoreThreePhaseCommitCohort createCohort(SnapshotBackedWriteTransaction transaction,
                                                          DataTreeModification modification) {
        return domStoreThreePhaseCommitCohort;
    }
}