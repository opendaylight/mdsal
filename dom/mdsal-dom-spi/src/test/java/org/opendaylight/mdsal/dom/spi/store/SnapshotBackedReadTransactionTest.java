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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.google.common.util.concurrent.Futures;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeSnapshot;

class SnapshotBackedReadTransactionTest {

    private static final DataTreeSnapshot DATA_TREE_SNAPSHOT = mock(DataTreeSnapshot.class);

    private SnapshotBackedReadTransaction<Object> snapshotBackedReadTransaction =
            new SnapshotBackedReadTransaction<>(new Object(), false, DATA_TREE_SNAPSHOT, null);

    @Test
    void basicTest() throws Exception {
        final NormalizedNode testNode = mock(NormalizedNode.class);
        final Optional<NormalizedNode> optional = Optional.of(testNode);
        doReturn("testNode").when(testNode).toString();
        doReturn(Optional.of(testNode)).when(DATA_TREE_SNAPSHOT).readNode(YangInstanceIdentifier.of());
        assertTrue(snapshotBackedReadTransaction.exists(YangInstanceIdentifier.of()).get());

        assertEquals(optional, snapshotBackedReadTransaction.read(YangInstanceIdentifier.of()).get());
        final Field stableSnapshotField = SnapshotBackedReadTransaction.class.getDeclaredField("stableSnapshot");
        stableSnapshotField.setAccessible(true);

        DataTreeSnapshot stableSnapshot = (DataTreeSnapshot) stableSnapshotField.get(snapshotBackedReadTransaction);
        assertNotNull(stableSnapshot);
        snapshotBackedReadTransaction.close();
        stableSnapshot = (DataTreeSnapshot) stableSnapshotField.get(snapshotBackedReadTransaction);
        assertNull(stableSnapshot);
    }

    @Test
    void readTestWithException() {
        snapshotBackedReadTransaction.close();
        final var future = snapshotBackedReadTransaction.read(YangInstanceIdentifier.of());
        final var cause = assertThrows(ExecutionException.class, () -> Futures.getDone(future)).getCause();
        assertThat(cause, instanceOf(ReadFailedException.class));
        assertEquals("Transaction is closed", cause.getMessage());
    }

    @Test
    void readNodeTestWithException() {
        final var thrown = new NullPointerException("no Node");
        doThrow(thrown).when(DATA_TREE_SNAPSHOT).readNode(any());
        snapshotBackedReadTransaction = new SnapshotBackedReadTransaction<>(new Object(), false, DATA_TREE_SNAPSHOT,
                null);

        final var future = snapshotBackedReadTransaction.read(YangInstanceIdentifier.of());
        final var cause = assertThrows(ExecutionException.class, () -> Futures.getDone(future)).getCause();
        assertThat(cause, instanceOf(ReadFailedException.class));
        assertEquals("Read failed", cause.getMessage());
        assertSame(thrown, cause.getCause());
    }

    @Test
    void existsTestWithException() {
        final var thrown = new NullPointerException("no Node");
        doThrow(thrown).when(DATA_TREE_SNAPSHOT).readNode(any());

        final var future = snapshotBackedReadTransaction.exists(YangInstanceIdentifier.of());
        final var cause = assertThrows(ExecutionException.class, () -> Futures.getDone(future)).getCause();
        assertThat(cause, instanceOf(ReadFailedException.class));
        assertEquals("Read failed", cause.getMessage());
        assertSame(thrown, cause.getCause());
    }
}
