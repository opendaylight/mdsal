/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.store;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Constructor;
import org.junit.Test;
import org.opendaylight.mdsal.dom.spi.store.SnapshotBackedWriteTransaction.TransactionReadyPrototype;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeSnapshot;

public class SnapshotBackedTransactionsTest {

    @Test
    public void basicTest() throws Exception {
        final DataTreeSnapshot dataTreeSnapshot = mock(DataTreeSnapshot.class);
        final DataTreeModification dataTreeModification = mock(DataTreeModification.class);
        final TransactionReadyPrototype transactionReadyPrototype =  mock(TransactionReadyPrototype.class);
        doReturn(dataTreeModification).when(dataTreeSnapshot).newModification();

        assertNotNull(SnapshotBackedTransactions.newReadTransaction(new Object(), false, dataTreeSnapshot));
        assertNotNull(SnapshotBackedTransactions.newWriteTransaction(
                new Object(), false, dataTreeSnapshot, transactionReadyPrototype));
        assertNotNull(SnapshotBackedTransactions.newReadWriteTransaction(
                new Object(), false, dataTreeSnapshot, transactionReadyPrototype));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void constructorTest() throws Throwable {
        Constructor<SnapshotBackedTransactions> constructor = SnapshotBackedTransactions.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
            fail("Expected UnsupportedOperationException");
        } catch (Exception e) {
            throw e.getCause();
        }
    }
}