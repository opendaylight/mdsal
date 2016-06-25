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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.dom.spi.store.SnapshotBackedWriteTransaction.TransactionReadyPrototype;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeSnapshot;

public class SnapshotBackedWriteTransactionTest {

    private static final DataTreeSnapshot dataTreeSnapshot = mock(DataTreeSnapshot.class);
    private static final DataTreeModification dataTreeModification = mock(DataTreeModification.class);
    private static final TransactionReadyPrototype<Object> transactionReadyPrototype =
            mock(TransactionReadyPrototype.class);
    private static final DOMStoreThreePhaseCommitCohort domStoreThreePhaseCommitCohort =
            mock(DOMStoreThreePhaseCommitCohort.class);
    private static final NormalizedNode normalizedNode = mock(NormalizedNode.class);
    private static final Optional optional = Optional.of(normalizedNode);
    private SnapshotBackedWriteTransaction snapshotBackedWriteTransaction;

    @Before
    public void setUp() throws Exception {
        doReturn(dataTreeModification).when(dataTreeSnapshot).newModification();
        doNothing().when(dataTreeModification).ready();
        doNothing().when(dataTreeModification).write(any(), any());
        doNothing().when(dataTreeModification).merge(any(), any());
        doNothing().when(dataTreeModification).delete(any());
        doNothing().when(transactionReadyPrototype).transactionAborted(any());
        doReturn("testDataTreeModification").when(dataTreeModification).toString();
        doReturn("testNormalizedNode").when(normalizedNode).toString();
        doReturn(domStoreThreePhaseCommitCohort).when(transactionReadyPrototype).transactionReady(any(),any());
        doReturn(optional).when(dataTreeModification).readNode(YangInstanceIdentifier.EMPTY);
        snapshotBackedWriteTransaction = new SnapshotBackedWriteTransaction<>(new Object(), false, dataTreeSnapshot,
                transactionReadyPrototype);
    }

    @Test
    public void basicTest() throws Exception {
        snapshotBackedWriteTransaction.write(YangInstanceIdentifier.EMPTY, normalizedNode);
        verify(dataTreeModification).write(any(), any());

        snapshotBackedWriteTransaction.merge(YangInstanceIdentifier.EMPTY, normalizedNode);
        verify(dataTreeModification).merge(any(), any());

        snapshotBackedWriteTransaction.delete(YangInstanceIdentifier.EMPTY);
        verify(dataTreeModification).delete(any());

        assertEquals(optional, snapshotBackedWriteTransaction.readSnapshotNode(YangInstanceIdentifier.EMPTY));
        verify(dataTreeModification).readNode(any());

        assertTrue(snapshotBackedWriteTransaction.addToStringAttributes(
                MoreObjects.toStringHelper(this).omitNullValues()).toString().contains("ready"));
        snapshotBackedWriteTransaction.close();
    }

    @Test
    public void readyTest() throws Exception {
        SnapshotBackedWriteTransaction snapshotBackedWriteTransaction =
                new SnapshotBackedWriteTransaction<>(new Object(), false, dataTreeSnapshot, transactionReadyPrototype);
        Assert.assertNotNull(snapshotBackedWriteTransaction.ready());
        verify(transactionReadyPrototype).transactionReady(any(), any());
        snapshotBackedWriteTransaction.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void writeWithException() throws Exception {
        doThrow(TestException.class).when(dataTreeModification).write(any(), any());
        snapshotBackedWriteTransaction.write(YangInstanceIdentifier.EMPTY, normalizedNode);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mergeWithException() throws Exception {
        doThrow(TestException.class).when(dataTreeModification).merge(any(), any());
        snapshotBackedWriteTransaction.merge(YangInstanceIdentifier.EMPTY, normalizedNode);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteWithException() throws Exception {
        doThrow(TestException.class).when(dataTreeModification).delete(any());
        snapshotBackedWriteTransaction.delete(YangInstanceIdentifier.EMPTY);
    }

    private static final class TestException extends Exception {}
}