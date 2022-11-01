/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreThreePhaseCommitCohort;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;

@ExtendWith(MockitoExtension.class)
public class CommitCoordinationTaskTest {
    @Mock
    private DOMStoreThreePhaseCommitCohort cohort;

    private CommitCoordinationTask task;

    @BeforeEach
    public void beforeEach() {
        final DOMDataTreeWriteTransaction tx = mock(DOMDataTreeWriteTransaction.class);
        task = new CommitCoordinationTask(tx, ImmutableList.of(cohort), null);
        doReturn("test").when(tx).getIdentifier();
    }

    @Test
    public void canCommitBlockingWithFail() throws Exception {
        doReturn(FluentFutures.immediateNullFluentFuture()).when(cohort).abort();

        doReturn(FluentFutures.immediateFalseFluentFuture()).when(cohort).canCommit();
        final var ex = assertThrows(TransactionCommitFailedException.class, task::call);
        assertEquals("Can Commit failed, no detailed cause available.", ex.getMessage());
    }

    @Test
    public void canCommitBlockingWithFailException() throws Exception {
        doReturn(FluentFutures.immediateNullFluentFuture()).when(cohort).abort();
        doReturn(Futures.immediateFailedFuture(new InterruptedException())).when(cohort).canCommit();
        final var ex = assertThrows(TransactionCommitFailedException.class, task::call);
        assertEquals("canCommit execution failed", ex.getMessage());
    }

    @Test
    public void preCommitBlockingWithFail() throws Exception {
        doReturn(FluentFutures.immediateTrueFluentFuture()).when(cohort).canCommit();
        doReturn(FluentFutures.immediateNullFluentFuture()).when(cohort).abort();
        doReturn(Futures.immediateFailedFuture(new InterruptedException())).when(cohort).preCommit();
        final var ex = assertThrows(TransactionCommitFailedException.class, task::call);
        assertEquals("preCommit execution failed", ex.getMessage());
    }

    @Test
    public void commitBlockingWithFail() throws Exception {
        doReturn(FluentFutures.immediateTrueFluentFuture()).when(cohort).canCommit();
        doReturn(FluentFutures.immediateNullFluentFuture()).when(cohort).preCommit();
        doReturn(FluentFutures.immediateNullFluentFuture()).when(cohort).abort();
        doReturn(Futures.immediateFailedFuture(new InterruptedException())).when(cohort).commit();
        final var ex = assertThrows(TransactionCommitFailedException.class, task::call);
        assertEquals("commit execution failed", ex.getMessage());
    }
}