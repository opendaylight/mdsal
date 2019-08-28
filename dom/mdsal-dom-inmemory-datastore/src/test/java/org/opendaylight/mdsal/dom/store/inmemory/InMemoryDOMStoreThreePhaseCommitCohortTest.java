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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
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

    private static InMemoryDOMStoreThreePhaseCommitCohort inMemoryDOMStoreThreePhaseCommitCohort = null;

    @Mock
    private static InMemoryDOMDataStore IN_MEMORY_DOM_DATA_STORE;

    @Mock
    private static DataTreeCandidate DATA_TREE_CANDIDATE;

    @Mock
    private static TransactionReadyPrototype<String> TRANSACTION_READY_PROTOTYPE;

    @Mock
    private static DataTreeSnapshot DATA_TREE_SNAPSHOT;

    @Mock
    private static DataTreeModification DATA_TREE_MODIFICATION;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        doReturn(DATA_TREE_MODIFICATION).when(DATA_TREE_SNAPSHOT).newModification();
        doReturn("testModification").when(DATA_TREE_MODIFICATION).toString();
        inMemoryDOMStoreThreePhaseCommitCohort =
                new InMemoryDOMStoreThreePhaseCommitCohort(IN_MEMORY_DOM_DATA_STORE,
                        SnapshotBackedTransactions.newWriteTransaction(
                                "test", false, DATA_TREE_SNAPSHOT, TRANSACTION_READY_PROTOTYPE),
                        DATA_TREE_MODIFICATION,
                        null);
    }

    @Test
    public void canCommitTest() throws Exception {
        doNothing().when(IN_MEMORY_DOM_DATA_STORE).validate(any());
        inMemoryDOMStoreThreePhaseCommitCohort.canCommit();
        verify(IN_MEMORY_DOM_DATA_STORE).validate(any());
    }

    @Test
    public void canCommitWithOperationError() throws Exception {
        RuntimeException operationError = new RuntimeException();
        inMemoryDOMStoreThreePhaseCommitCohort =
                new InMemoryDOMStoreThreePhaseCommitCohort(IN_MEMORY_DOM_DATA_STORE,
                        SnapshotBackedTransactions.newWriteTransaction(
                                "test", false, DATA_TREE_SNAPSHOT, TRANSACTION_READY_PROTOTYPE),
                        DATA_TREE_MODIFICATION,
                        operationError);
        doNothing().when(IN_MEMORY_DOM_DATA_STORE).validate(any());
        try {
            inMemoryDOMStoreThreePhaseCommitCohort.canCommit().get();
            fail("Expected exception");
        } catch (ExecutionException e) {
            assertTrue(e.getCause() == operationError);
        }
    }

    @SuppressWarnings({ "checkstyle:IllegalThrows", "checkstyle:avoidHidingCauseException" })
    @Test(expected = OptimisticLockFailedException.class)
    public void canCommitTestWithOptimisticLockFailedException() throws Throwable {
        doThrow(new ConflictingModificationAppliedException(YangInstanceIdentifier.empty(), "testException"))
                .when(IN_MEMORY_DOM_DATA_STORE).validate(any());
        try {
            inMemoryDOMStoreThreePhaseCommitCohort.canCommit().get();
            fail("Expected exception");
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof OptimisticLockFailedException);
            throw e.getCause();
        }
    }

    @SuppressWarnings({ "checkstyle:IllegalThrows", "checkstyle:avoidHidingCauseException" })
    @Test(expected = TransactionCommitFailedException.class)
    public void canCommitTestWithTransactionCommitFailedException() throws Throwable {
        doThrow(new DataValidationFailedException(YangInstanceIdentifier.empty(), "testException"))
                .when(IN_MEMORY_DOM_DATA_STORE).validate(any());
        try {
            inMemoryDOMStoreThreePhaseCommitCohort.canCommit().get();
            fail("Expected exception");
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof TransactionCommitFailedException);
            throw e.getCause();
        }
    }

    @SuppressWarnings({ "checkstyle:IllegalThrows", "checkstyle:avoidHidingCauseException" })
    @Test(expected = UnsupportedOperationException.class)
    public void canCommitTestWithUnknownException() throws Throwable {
        doThrow(new UnsupportedOperationException("testException"))
                .when(IN_MEMORY_DOM_DATA_STORE).validate(any());
        try {
            inMemoryDOMStoreThreePhaseCommitCohort.canCommit().get();
            fail("Expected exception");
        } catch (ExecutionException e) {
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

    @SuppressWarnings({ "checkstyle:IllegalThrows", "checkstyle:avoidHidingCauseException" })
    @Test(expected = UnsupportedOperationException.class)
    public void preCommitTestWithUnknownException() throws Throwable {
        doThrow(new UnsupportedOperationException("testException"))
                .when(IN_MEMORY_DOM_DATA_STORE).prepare(any());
        try {
            inMemoryDOMStoreThreePhaseCommitCohort.preCommit().get();
            fail("Expected exception");
        } catch (ExecutionException e) {
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