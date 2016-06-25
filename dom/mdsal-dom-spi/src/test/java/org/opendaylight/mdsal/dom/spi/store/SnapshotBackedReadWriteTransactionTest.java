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
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.mdsal.dom.spi.store.SnapshotBackedWriteTransaction.TransactionReadyPrototype;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeSnapshot;

public class SnapshotBackedReadWriteTransactionTest {

    private static final DataTreeSnapshot DATA_TREE_SNAPSHOT = mock(DataTreeSnapshot.class);
    private static final DataTreeModification DATA_TREE_MODIFICATION = mock(DataTreeModification.class);
    private static final TransactionReadyPrototype TRANSACTION_READY_PROTOTYPE =  mock(TransactionReadyPrototype.class);
    private SnapshotBackedReadWriteTransaction snapshotBackedReadWriteTransaction;

    @Before
    public void setUp() throws Exception {
        doReturn(DATA_TREE_MODIFICATION).when(DATA_TREE_SNAPSHOT).newModification();
        snapshotBackedReadWriteTransaction = new SnapshotBackedReadWriteTransaction(new Object(), false,
                DATA_TREE_SNAPSHOT, TRANSACTION_READY_PROTOTYPE);
    }

    @Test
    public void basicTest() throws Exception {
        final NormalizedNode<?, ?> testNode = mock(NormalizedNode.class);
        final Optional<NormalizedNode> optional = Optional.of(testNode);
        doReturn("testNode").when(testNode).toString();
        doReturn(optional).when(DATA_TREE_MODIFICATION).readNode(YangInstanceIdentifier.EMPTY);
        assertTrue((Boolean) snapshotBackedReadWriteTransaction.exists(YangInstanceIdentifier.EMPTY).get());
        assertEquals(optional, snapshotBackedReadWriteTransaction.read(YangInstanceIdentifier.EMPTY).get());
    }

    @Test(expected = ReadFailedException.class)
    public void readTestWithNullException() throws Throwable {
        doReturn(null).when(DATA_TREE_MODIFICATION).readNode(YangInstanceIdentifier.EMPTY);
        try {
            snapshotBackedReadWriteTransaction.read(YangInstanceIdentifier.EMPTY).get();
            fail("Expected ReadFailedException");
        } catch (Exception e) {
            throw e.getCause();
        }
    }

    @Test(expected = ReadFailedException.class)
    public void readNodeTestWithException() throws Throwable {
        doThrow(new NullPointerException("no Node")).when(DATA_TREE_MODIFICATION).readNode(any());

        try {
            snapshotBackedReadWriteTransaction.read(YangInstanceIdentifier.EMPTY).get();
            fail("Expected ReadFailedException");
        } catch (Exception e) {
            throw e.getCause();
        }
    }

    @Test(expected = ReadFailedException.class)
    public void existsTestWithException() throws Throwable {
        doThrow(new NullPointerException("no Node")).when(DATA_TREE_MODIFICATION).readNode(any());
        try {
            snapshotBackedReadWriteTransaction.exists(YangInstanceIdentifier.EMPTY).get();
            fail("Expected ReadFailedException");
        } catch (Exception e) {
            throw e.getCause();
        }
    }
}