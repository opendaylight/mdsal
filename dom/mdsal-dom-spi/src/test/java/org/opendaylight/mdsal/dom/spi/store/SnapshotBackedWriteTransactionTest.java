/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.common.base.MoreObjects;
import java.io.Serial;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.dom.spi.store.SnapshotBackedWriteTransaction.TransactionReadyPrototype;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeSnapshot;

public class SnapshotBackedWriteTransactionTest {

    private static final DataTreeSnapshot DATA_TREE_SNAPSHOT = mock(DataTreeSnapshot.class);
    private static final DataTreeModification DATA_TREE_MODIFICATION = mock(DataTreeModification.class);
    private static final TransactionReadyPrototype<Object> TRANSACTION_READY_PROTOTYPE =
            mock(TransactionReadyPrototype.class);
    private static final DOMStoreThreePhaseCommitCohort DOM_STORE_THREE_PHASE_COMMIT_COHORT =
            mock(DOMStoreThreePhaseCommitCohort.class);
    private static final ContainerNode NORMALIZED_NODE = mock(ContainerNode.class);
    private static final Optional<ContainerNode> NORMALIZED_NODE_OPTIONAL = Optional.of(NORMALIZED_NODE);
    private static SnapshotBackedWriteTransaction<Object> snapshotBackedWriteTransaction;

    @Before
    public void setUp() throws Exception {
        doReturn(DATA_TREE_MODIFICATION).when(DATA_TREE_SNAPSHOT).newModification();
        doNothing().when(DATA_TREE_MODIFICATION).ready();
        doNothing().when(DATA_TREE_MODIFICATION).write(any(), any());
        doNothing().when(DATA_TREE_MODIFICATION).merge(any(), any());
        doNothing().when(DATA_TREE_MODIFICATION).delete(any());
        doNothing().when(TRANSACTION_READY_PROTOTYPE).transactionAborted(any());
        doReturn("testDataTreeModification").when(DATA_TREE_MODIFICATION).toString();
        doReturn("testNormalizedNode").when(NORMALIZED_NODE).toString();
        doReturn(DOM_STORE_THREE_PHASE_COMMIT_COHORT)
                .when(TRANSACTION_READY_PROTOTYPE)
                .transactionReady(any(),any(), any());
        doReturn(NORMALIZED_NODE_OPTIONAL).when(DATA_TREE_MODIFICATION).readNode(YangInstanceIdentifier.of());
        snapshotBackedWriteTransaction = new SnapshotBackedWriteTransaction<>(new Object(), false, DATA_TREE_SNAPSHOT,
                TRANSACTION_READY_PROTOTYPE);
    }

    @Test
    public void basicTest() throws Exception {
        snapshotBackedWriteTransaction.write(YangInstanceIdentifier.of(), NORMALIZED_NODE);
        verify(DATA_TREE_MODIFICATION).write(any(), any());

        snapshotBackedWriteTransaction.merge(YangInstanceIdentifier.of(), NORMALIZED_NODE);
        verify(DATA_TREE_MODIFICATION).merge(any(), any());

        snapshotBackedWriteTransaction.delete(YangInstanceIdentifier.of());
        verify(DATA_TREE_MODIFICATION).delete(any());

        assertEquals(NORMALIZED_NODE_OPTIONAL,
                snapshotBackedWriteTransaction.readSnapshotNode(YangInstanceIdentifier.of()));
        verify(DATA_TREE_MODIFICATION).readNode(any());

        assertTrue(snapshotBackedWriteTransaction.addToStringAttributes(
                MoreObjects.toStringHelper(this).omitNullValues()).toString().contains("ready"));
        snapshotBackedWriteTransaction.close();
    }

    @Test
    public void readyTest() throws Exception {
        SnapshotBackedWriteTransaction<Object> tx = new SnapshotBackedWriteTransaction<>(new Object(), false,
                DATA_TREE_SNAPSHOT, TRANSACTION_READY_PROTOTYPE);
        Assert.assertNotNull(tx.ready());
        verify(TRANSACTION_READY_PROTOTYPE).transactionReady(any(), any(), eq(null));
        tx.close();
    }

    @Test
    public void readyWithException() {
        Exception thrown = new RuntimeException();
        doThrow(thrown).when(DATA_TREE_MODIFICATION).ready();
        Assert.assertNotNull(snapshotBackedWriteTransaction.ready());
        verify(TRANSACTION_READY_PROTOTYPE).transactionReady(any(), any(), same(thrown));
    }

    @Test(expected = IllegalArgumentException.class)
    public void writeWithException() throws Exception {
        doAnswer(inv -> {
            throw new TestException();
        }).when(DATA_TREE_MODIFICATION).write(any(), any());
        snapshotBackedWriteTransaction.write(YangInstanceIdentifier.of(), NORMALIZED_NODE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mergeWithException() throws Exception {
        doAnswer(inv -> {
            throw new TestException();
        }).when(DATA_TREE_MODIFICATION).merge(any(), any());
        snapshotBackedWriteTransaction.merge(YangInstanceIdentifier.of(), NORMALIZED_NODE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteWithException() throws Exception {
        doAnswer(inv -> {
            throw new TestException();
        }).when(DATA_TREE_MODIFICATION).delete(any());
        snapshotBackedWriteTransaction.delete(YangInstanceIdentifier.of());
    }

    private static final class TestException extends Exception {
        @Serial
        private static final long serialVersionUID = 1L;
    }
}