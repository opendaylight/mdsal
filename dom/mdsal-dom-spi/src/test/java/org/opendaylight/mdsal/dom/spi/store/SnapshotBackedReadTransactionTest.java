/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.google.common.util.concurrent.Futures;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeSnapshot;

@ExtendWith(MockitoExtension.class)
class SnapshotBackedReadTransactionTest {
    @Mock
    private DataTreeSnapshot dataTreeSnapshot;

    private SnapshotBackedReadTransaction<Object> snapshotBackedReadTransaction;

    @BeforeEach
    void beforeEach() {
        snapshotBackedReadTransaction = new SnapshotBackedReadTransaction<>(new Object(), false, dataTreeSnapshot,
            null);
    }

    @Test
    void basicTest() throws Exception {
        final var testNode = mock(ContainerNode.class);
        final var optional = Optional.of(testNode);
        doReturn(Optional.of(testNode)).when(dataTreeSnapshot).readNode(YangInstanceIdentifier.of());
        assertTrue(snapshotBackedReadTransaction.exists(YangInstanceIdentifier.of()).get());

        assertEquals(optional, snapshotBackedReadTransaction.read(YangInstanceIdentifier.of()).get());
        final var stableSnapshotField = SnapshotBackedReadTransaction.class.getDeclaredField("stableSnapshot");
        stableSnapshotField.setAccessible(true);

        var stableSnapshot = assertInstanceOf(DataTreeSnapshot.class,
            stableSnapshotField.get(snapshotBackedReadTransaction));
        snapshotBackedReadTransaction.close();
        stableSnapshot = (DataTreeSnapshot) stableSnapshotField.get(snapshotBackedReadTransaction);
        assertNull(stableSnapshot);
    }

    @Test
    void readTestWithException() {
        snapshotBackedReadTransaction.close();
        final var future = snapshotBackedReadTransaction.read(YangInstanceIdentifier.of());
        final var ex = assertThrows(ExecutionException.class, () -> Futures.getDone(future));
        final var cause = assertInstanceOf(ReadFailedException.class, ex.getCause());
        assertEquals("Transaction is closed", cause.getMessage());
    }

    @Test
    void readNodeTestWithException() {
        final var thrown = new NullPointerException("no Node");
        doThrow(thrown).when(dataTreeSnapshot).readNode(any());
        snapshotBackedReadTransaction = new SnapshotBackedReadTransaction<>(new Object(), false, dataTreeSnapshot,
                null);

        final var future = snapshotBackedReadTransaction.read(YangInstanceIdentifier.of());
        final var ex = assertThrows(ExecutionException.class, () -> Futures.getDone(future));
        final var cause = assertInstanceOf(ReadFailedException.class, ex.getCause());
        assertEquals("Read failed", cause.getMessage());
        assertSame(thrown, cause.getCause());
    }

    @Test
    void existsTestWithException() {
        final var thrown = new NullPointerException("no Node");
        doThrow(thrown).when(dataTreeSnapshot).readNode(any());

        final var future = snapshotBackedReadTransaction.exists(YangInstanceIdentifier.of());
        final var ex = assertThrows(ExecutionException.class, () -> Futures.getDone(future));
        final var cause = assertInstanceOf(ReadFailedException.class, ex.getCause());
        assertEquals("Read failed", cause.getMessage());
        assertSame(thrown, cause.getCause());
    }
}
