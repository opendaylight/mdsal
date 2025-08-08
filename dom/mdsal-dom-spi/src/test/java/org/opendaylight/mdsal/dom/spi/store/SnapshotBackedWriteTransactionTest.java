/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.google.common.base.MoreObjects;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.opendaylight.mdsal.dom.spi.store.SnapshotBackedWriteTransaction.TransactionReadyPrototype;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeSnapshot;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SnapshotBackedWriteTransactionTest {
    @Mock
    private DataTreeSnapshot dataTreeSnapshot;
    @Mock
    private DataTreeModification dataTreeModification;
    @Mock
    private TransactionReadyPrototype<Object> transactionReadyPrototype;
    @Mock
    private DOMStoreThreePhaseCommitCohort domStoreThreePhaseCommitCohort;
    @Mock
    private ContainerNode normalizedNode;
    private SnapshotBackedWriteTransaction<Object> snapshotBackedWriteTransaction;

    @BeforeEach
    void beforeEach() {
        doReturn(dataTreeModification).when(dataTreeSnapshot).newModification();
        doNothing().when(dataTreeModification).ready();
        doNothing().when(dataTreeModification).write(any(), any());
        doNothing().when(dataTreeModification).merge(any(), any());
        doNothing().when(dataTreeModification).delete(any());
        doNothing().when(transactionReadyPrototype).transactionAborted(any());
        doReturn("testDataTreeModification").when(dataTreeModification).toString();
        doReturn("testNormalizedNode").when(normalizedNode).toString();
        doReturn(domStoreThreePhaseCommitCohort).when(transactionReadyPrototype).transactionReady(any(),any(), any());
        doReturn(Optional.of(normalizedNode)).when(dataTreeModification).readNode(YangInstanceIdentifier.of());
        snapshotBackedWriteTransaction = new SnapshotBackedWriteTransaction<>(new Object(), false, dataTreeSnapshot,
                transactionReadyPrototype);
    }

    @Test
    void basicTest() {
        snapshotBackedWriteTransaction.write(YangInstanceIdentifier.of(), normalizedNode);
        verify(dataTreeModification).write(any(), any());

        snapshotBackedWriteTransaction.merge(YangInstanceIdentifier.of(), normalizedNode);
        verify(dataTreeModification).merge(any(), any());

        snapshotBackedWriteTransaction.delete(YangInstanceIdentifier.of());
        verify(dataTreeModification).delete(any());

        assertEquals(Optional.of(normalizedNode),
                snapshotBackedWriteTransaction.readSnapshotNode(YangInstanceIdentifier.of()));
        verify(dataTreeModification).readNode(any());

        assertTrue(snapshotBackedWriteTransaction.addToStringAttributes(
                MoreObjects.toStringHelper(this).omitNullValues()).toString().contains("ready"));
        snapshotBackedWriteTransaction.close();
    }

    @Test
    void readyTest() {
        try (var tx = new SnapshotBackedWriteTransaction<>(new Object(), false, dataTreeSnapshot,
            transactionReadyPrototype)) {
            assertNotNull(tx.ready());
            verify(transactionReadyPrototype).transactionReady(any(), any(), eq(null));
        }
    }

    @Test
    void readyWithException() {
        Exception thrown = new RuntimeException();
        doThrow(thrown).when(dataTreeModification).ready();
        assertNotNull(snapshotBackedWriteTransaction.ready());
        verify(transactionReadyPrototype).transactionReady(any(), any(), same(thrown));
    }

    @Test
    void writeWithException() {
        doAnswer(inv -> {
            throw new TestException();
        }).when(dataTreeModification).write(any(), any());
        assertThrows(IllegalArgumentException.class,
            () -> snapshotBackedWriteTransaction.write(YangInstanceIdentifier.of(), normalizedNode));
    }

    @Test
    void mergeWithException() {
        doAnswer(inv -> {
            throw new TestException();
        }).when(dataTreeModification).merge(any(), any());
        assertThrows(IllegalArgumentException.class,
            () -> snapshotBackedWriteTransaction.merge(YangInstanceIdentifier.of(), normalizedNode));
    }

    @Test
    void deleteWithException() {
        doAnswer(inv -> {
            throw new TestException();
        }).when(dataTreeModification).delete(any());
        assertThrows(IllegalArgumentException.class,
            () -> snapshotBackedWriteTransaction.delete(YangInstanceIdentifier.of()));
    }

    private static final class TestException extends Exception {
        @java.io.Serial
        private static final long serialVersionUID = 1L;
    }
}