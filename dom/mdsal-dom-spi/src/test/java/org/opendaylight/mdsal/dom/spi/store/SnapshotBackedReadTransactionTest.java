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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.util.Optional;
import org.junit.Test;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeSnapshot;

@SuppressWarnings("checkstyle:IllegalCatch")
public class SnapshotBackedReadTransactionTest {

    private static final DataTreeSnapshot DATA_TREE_SNAPSHOT = mock(DataTreeSnapshot.class);

    private SnapshotBackedReadTransaction<Object> snapshotBackedReadTransaction =
            new SnapshotBackedReadTransaction<>(new Object(), false, DATA_TREE_SNAPSHOT, null);

    @Test
    public void basicTest() throws Exception {
        final NormalizedNode<?, ?> testNode = mock(NormalizedNode.class);
        final Optional<NormalizedNode<?, ?>> optional = Optional.of(testNode);
        doReturn("testNode").when(testNode).toString();
        doReturn(Optional.of(testNode)).when(DATA_TREE_SNAPSHOT).readNode(YangInstanceIdentifier.empty());
        assertTrue(snapshotBackedReadTransaction.exists(YangInstanceIdentifier.empty()).get());

        assertEquals(optional, snapshotBackedReadTransaction.read(YangInstanceIdentifier.empty()).get());
        final Field stableSnapshotField = SnapshotBackedReadTransaction.class.getDeclaredField("stableSnapshot");
        stableSnapshotField.setAccessible(true);

        DataTreeSnapshot stableSnapshot = (DataTreeSnapshot) stableSnapshotField.get(snapshotBackedReadTransaction);
        assertNotNull(stableSnapshot);
        snapshotBackedReadTransaction.close();
        stableSnapshot = (DataTreeSnapshot) stableSnapshotField.get(snapshotBackedReadTransaction);
        assertNull(stableSnapshot);
    }

    @SuppressWarnings({ "checkstyle:IllegalThrows", "checkstyle:avoidHidingCauseException" })
    @Test(expected = ReadFailedException.class)
    public void readTestWithException() throws Throwable {
        snapshotBackedReadTransaction.close();
        try {
            snapshotBackedReadTransaction.read(YangInstanceIdentifier.empty()).get();
            fail("Expected ReadFailedException");
        } catch (Exception e) {
            throw e.getCause();
        }
    }

    @SuppressWarnings({ "checkstyle:IllegalThrows", "checkstyle:avoidHidingCauseException" })
    @Test(expected = ReadFailedException.class)
    public void readNodeTestWithException() throws Throwable {
        doThrow(new NullPointerException("no Node")).when(DATA_TREE_SNAPSHOT).readNode(any());
        snapshotBackedReadTransaction = new SnapshotBackedReadTransaction<>(new Object(), false, DATA_TREE_SNAPSHOT,
                null);
        try {
            snapshotBackedReadTransaction.read(YangInstanceIdentifier.empty()).get();
            fail("Expected ReadFailedException");
        } catch (Exception e) {
            throw e.getCause();
        }
    }

    @SuppressWarnings({ "checkstyle:IllegalThrows", "checkstyle:avoidHidingCauseException" })
    @Test(expected = ReadFailedException.class)
    public void existsTestWithException() throws Throwable  {
        doThrow(new NullPointerException("no Node")).when(DATA_TREE_SNAPSHOT).readNode(any());

        try {
            snapshotBackedReadTransaction.exists(YangInstanceIdentifier.empty()).get();
            fail("Expected ReadFailedException");
        } catch (Exception e) {
            throw e.getCause();
        }
    }
}
