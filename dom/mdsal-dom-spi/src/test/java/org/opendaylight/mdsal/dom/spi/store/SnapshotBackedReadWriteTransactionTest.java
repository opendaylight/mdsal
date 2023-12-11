/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.store;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.google.common.util.concurrent.Futures;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.mdsal.dom.spi.store.SnapshotBackedWriteTransaction.TransactionReadyPrototype;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeSnapshot;

public class SnapshotBackedReadWriteTransactionTest {

    private static final DataTreeSnapshot DATA_TREE_SNAPSHOT = mock(DataTreeSnapshot.class);
    private static final DataTreeModification DATA_TREE_MODIFICATION = mock(DataTreeModification.class);
    private static final TransactionReadyPrototype<Object> TRANSACTION_READY_PROTOTYPE =
            mock(TransactionReadyPrototype.class);
    private SnapshotBackedReadWriteTransaction<Object> snapshotBackedReadWriteTransaction;

    @Before
    public void setUp() {
        doReturn(DATA_TREE_MODIFICATION).when(DATA_TREE_SNAPSHOT).newModification();
        snapshotBackedReadWriteTransaction = new SnapshotBackedReadWriteTransaction<>(new Object(), false,
                DATA_TREE_SNAPSHOT, TRANSACTION_READY_PROTOTYPE);
    }

    @Test
    public void basicTest() throws Exception {
        final var testNode = mock(ContainerNode.class);
        final var optional = Optional.of(testNode);
        doReturn("testNode").when(testNode).toString();
        doReturn(Optional.of(testNode)).when(DATA_TREE_MODIFICATION).readNode(YangInstanceIdentifier.of());
        assertTrue(snapshotBackedReadWriteTransaction.exists(YangInstanceIdentifier.of()).get());
        assertEquals(optional, snapshotBackedReadWriteTransaction.read(YangInstanceIdentifier.of()).get());
    }

    @Test
    public void readTestWithNullException() {
        doReturn(null).when(DATA_TREE_MODIFICATION).readNode(YangInstanceIdentifier.of());

        final var future = snapshotBackedReadWriteTransaction.read(YangInstanceIdentifier.of());
        final var cause = assertThrows(ExecutionException.class, () -> Futures.getDone(future)).getCause();
        assertThat(cause, instanceOf(ReadFailedException.class));
        assertEquals("Transaction is closed", cause.getMessage());
    }

    @Test
    public void readNodeTestWithException() {
        final var thrown = new NullPointerException("no Node");
        doThrow(thrown).when(DATA_TREE_MODIFICATION).readNode(any());

        final var future = snapshotBackedReadWriteTransaction.read(YangInstanceIdentifier.of());
        final var cause = assertThrows(ExecutionException.class, () -> Futures.getDone(future)).getCause();
        assertThat(cause, instanceOf(ReadFailedException.class));
        assertEquals("Read failed", cause.getMessage());
        assertSame(thrown, cause.getCause());
    }

    @Test
    public void existsTestWithException() {
        final var thrown = new NullPointerException("no Node");
        doThrow(thrown).when(DATA_TREE_MODIFICATION).readNode(any());

        final var future = snapshotBackedReadWriteTransaction.exists(YangInstanceIdentifier.of());
        final var cause = assertThrows(ExecutionException.class, () -> Futures.getDone(future)).getCause();
        assertThat(cause, instanceOf(ReadFailedException.class));
        assertEquals("Read failed", cause.getMessage());
        assertSame(thrown, cause.getCause());
    }
}
