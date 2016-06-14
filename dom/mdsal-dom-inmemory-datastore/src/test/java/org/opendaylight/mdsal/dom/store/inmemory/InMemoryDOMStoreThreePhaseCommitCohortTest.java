/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.common.api.OptimisticLockFailedException;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.mdsal.dom.spi.store.SnapshotBackedTransactions;
import org.opendaylight.mdsal.dom.spi.store.SnapshotBackedWriteTransaction.TransactionReadyPrototype;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ConflictingModificationAppliedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeSnapshot;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;

public class InMemoryDOMStoreThreePhaseCommitCohortTest {

    private static  InMemoryDOMStoreThreePhaseCommitCohort inMemoryDOMStoreThreePhaseCommitCohort = null;
    private static final InMemoryDOMDataStore IN_MEMORY_DOM_DATA_STORE = mock(InMemoryDOMDataStore.class);
    private static final DataTreeCandidate DATA_TREE_CANDIDATE = mock(DataTreeCandidate.class);

    @Before
    public void setUp() throws Exception {
        reset(IN_MEMORY_DOM_DATA_STORE);
        DataTreeSnapshot dataTreeSnapshot = mock(DataTreeSnapshot.class);
        TransactionReadyPrototype transactionReadyPrototype = mock(TransactionReadyPrototype.class);
        DataTreeModification dataTreeModification = mock(DataTreeModification.class);
        doReturn(dataTreeModification).when(dataTreeSnapshot).newModification();
        doReturn("testModification").when(dataTreeModification).toString();

        inMemoryDOMStoreThreePhaseCommitCohort =
                new InMemoryDOMStoreThreePhaseCommitCohort(IN_MEMORY_DOM_DATA_STORE,
                        SnapshotBackedTransactions
                                .newWriteTransaction("test", false, dataTreeSnapshot, transactionReadyPrototype),
                        dataTreeModification);
    }

    @Test
    public void canCommitTest() throws Exception {
        doNothing().when(IN_MEMORY_DOM_DATA_STORE).validate(any());
        inMemoryDOMStoreThreePhaseCommitCohort.canCommit();
        verify(IN_MEMORY_DOM_DATA_STORE).validate(any());
    }

    @Test(expected = OptimisticLockFailedException.class)
    public void canCommitTestWithOptimisticLockFailedException() throws Throwable {
        doThrow(new ConflictingModificationAppliedException(YangInstanceIdentifier.EMPTY, "testException"))
                .when(IN_MEMORY_DOM_DATA_STORE).validate(any());
        try {
            inMemoryDOMStoreThreePhaseCommitCohort.canCommit().get();
            fail("Expected exception");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof OptimisticLockFailedException);
            throw e.getCause();
        }
    }

    @Test(expected = TransactionCommitFailedException.class)
    public void canCommitTestWithTransactionCommitFailedException() throws Throwable {
        doThrow(new DataValidationFailedException(YangInstanceIdentifier.EMPTY, "testException"))
                .when(IN_MEMORY_DOM_DATA_STORE).validate(any());
        try {
            inMemoryDOMStoreThreePhaseCommitCohort.canCommit().get();
            fail("Expected exception");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof TransactionCommitFailedException);
            throw e.getCause();
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void canCommitTestWithUnknownException() throws Throwable {
        doThrow(new UnsupportedOperationException("testException"))
                .when(IN_MEMORY_DOM_DATA_STORE).validate(any());
        try {
            inMemoryDOMStoreThreePhaseCommitCohort.canCommit().get();
            fail("Expected exception");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof UnsupportedOperationException);
            throw e.getCause();
        }
    }

    @Test
    public void preCommitTest() throws Exception {
        doReturn(DATA_TREE_CANDIDATE).when(IN_MEMORY_DOM_DATA_STORE).prepare(any());
        inMemoryDOMStoreThreePhaseCommitCohort.preCommit().get();
        verify(IN_MEMORY_DOM_DATA_STORE).prepare(any());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void preCommitTestWithUnknownException() throws Throwable {
        doThrow(new UnsupportedOperationException("testException"))
                .when(IN_MEMORY_DOM_DATA_STORE).prepare(any());
        try {
            inMemoryDOMStoreThreePhaseCommitCohort.preCommit().get();
            fail("Expected exception");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof UnsupportedOperationException);
            throw e.getCause();
        }
    }

    @Test
    public void abortTest() throws Exception {
        doReturn(DATA_TREE_CANDIDATE).when(IN_MEMORY_DOM_DATA_STORE).prepare(any());
        doReturn("testDataTreeCandidate").when(DATA_TREE_CANDIDATE).toString();
        final Field candidateField = InMemoryDOMStoreThreePhaseCommitCohort.class.getDeclaredField("candidate");
        candidateField.setAccessible(true);

        inMemoryDOMStoreThreePhaseCommitCohort.preCommit();
        DataTreeCandidate candidate =
                (DataTreeCandidate) candidateField.get(inMemoryDOMStoreThreePhaseCommitCohort);

        assertNotNull(candidate);
        inMemoryDOMStoreThreePhaseCommitCohort.abort();
        candidate = (DataTreeCandidate) candidateField.get(inMemoryDOMStoreThreePhaseCommitCohort);
        assertNull(candidate);
    }

    @Test
    public void commitTest() throws Exception {
        doNothing().when(IN_MEMORY_DOM_DATA_STORE).commit(any());
        doReturn(DATA_TREE_CANDIDATE).when(IN_MEMORY_DOM_DATA_STORE).prepare(any());
        inMemoryDOMStoreThreePhaseCommitCohort.preCommit();
        inMemoryDOMStoreThreePhaseCommitCohort.commit();
        verify(IN_MEMORY_DOM_DATA_STORE).commit(any());
    }
}