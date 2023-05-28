/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.util.concurrent.ExecutionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.common.api.OptimisticLockFailedException;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreThreePhaseCommitCohort;
import org.opendaylight.mdsal.dom.spi.store.SnapshotBackedTransactions;
import org.opendaylight.mdsal.dom.spi.store.SnapshotBackedWriteTransaction.TransactionReadyPrototype;
import org.opendaylight.yangtools.yang.common.ErrorSeverity;
import org.opendaylight.yangtools.yang.common.ErrorTag;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.tree.api.ConflictingModificationAppliedException;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeSnapshot;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class InMemoryDOMStoreThreePhaseCommitCohortTest {
    @Mock
    private InMemoryDOMDataStore dataStore;
    @Mock
    private DataTreeCandidate candidate;
    @Mock
    private TransactionReadyPrototype<String> prototype;
    @Mock
    private DataTreeSnapshot snapshot;
    @Mock
    private DataTreeModification modification;

    @Test
    public void canCommitTest() throws Exception {
        doNothing().when(dataStore).validate(any());
        prepareSimpleCohort().canCommit();
        verify(dataStore).validate(any());
    }

    @Test
    public void canCommitWithOperationError() throws Exception {
        doReturn(modification).when(snapshot).newModification();
        final var operationError = new RuntimeException();
        final var cohort = new InMemoryDOMStoreThreePhaseCommitCohort(dataStore,
            SnapshotBackedTransactions.newWriteTransaction("test", false, snapshot, prototype), modification,
            operationError);

        assertSame(operationError, assertFailsCanCommit(cohort));
    }

    @Test
    public void canCommitTestWithOptimisticLockFailedException() throws Exception {
        final var cause = new ConflictingModificationAppliedException(YangInstanceIdentifier.of(), "testException");
        doThrow(cause).when(dataStore).validate(any());

        final var ex = assertFailsCanCommit(prepareSimpleCohort());
        assertThat(ex, instanceOf(OptimisticLockFailedException.class));
        assertSame(cause, ex.getCause());
        final var errors = ((OptimisticLockFailedException) ex).getErrorList();
        assertEquals(1, errors.size());
        final var error = errors.get(0);
        assertEquals(ErrorSeverity.ERROR, error.getSeverity());
        assertEquals(ErrorType.APPLICATION, error.getErrorType());
        assertEquals(ErrorTag.RESOURCE_DENIED, error.getTag());
    }

    @Test
    public void canCommitTestWithTransactionCommitFailedException() throws Exception {
        final var cause = new DataValidationFailedException(YangInstanceIdentifier.of(), "testException");
        doThrow(cause).when(dataStore).validate(any());

        final var ex = assertFailsCanCommit(prepareSimpleCohort());
        assertThat(ex, instanceOf(TransactionCommitFailedException.class));
        assertSame(cause, ex.getCause());
        final var errors = ((TransactionCommitFailedException) ex).getErrorList();
        assertEquals(1, errors.size());
        final var error = errors.get(0);
        assertEquals(ErrorSeverity.ERROR, error.getSeverity());
        assertEquals(ErrorType.APPLICATION, error.getErrorType());
        assertEquals(ErrorTag.OPERATION_FAILED, error.getTag());
    }

    @Test
    public void canCommitTestWithUnknownException() throws Exception {
        final var cause = new UnsupportedOperationException("testException");
        doThrow(cause).when(dataStore).validate(any());

        assertSame(cause, assertFailsCanCommit(prepareSimpleCohort()));
    }

    @Test
    public void preCommitTest() throws Exception {
        doReturn(candidate).when(dataStore).prepare(any());
        prepareSimpleCohort().preCommit().get();
        verify(dataStore).prepare(any());
    }

    @Test
    public void preCommitTestWithUnknownException() throws Exception {
        final var cause = new UnsupportedOperationException("testException");
        doThrow(cause).when(dataStore).prepare(any());

        final var future = prepareSimpleCohort().preCommit();
        final var ex = assertThrows(ExecutionException.class, future::get).getCause();
        assertSame(cause, ex);
    }

    @Test
    public void abortTest() throws Exception {
        doReturn(candidate).when(dataStore).prepare(any());

        final var cohort = prepareSimpleCohort();
        cohort.preCommit();
        assertNotNull(cohort.candidate);

        cohort.abort();
        assertNull(cohort.candidate);
    }

    @Test
    public void commitTest() throws Exception {
        doNothing().when(dataStore).commit(any());
        doReturn(candidate).when(dataStore).prepare(any());

        final var cohort = prepareSimpleCohort();
        cohort.preCommit();
        cohort.commit();
        verify(dataStore).commit(any());
    }

    private InMemoryDOMStoreThreePhaseCommitCohort prepareSimpleCohort() {
        doReturn(modification).when(snapshot).newModification();
        return new InMemoryDOMStoreThreePhaseCommitCohort(dataStore,
            SnapshotBackedTransactions.newWriteTransaction("test", false, snapshot, prototype),
            modification, null);
    }

    private static Throwable assertFailsCanCommit(final DOMStoreThreePhaseCommitCohort cohort) {
        final var future = cohort.canCommit();
        return assertThrows(ExecutionException.class, future::get).getCause();
    }
}
