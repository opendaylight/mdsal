/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.util.concurrent.Futures;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreThreePhaseCommitCohort;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;

public class CommitCoordinationTaskTest {
    private final DOMStoreThreePhaseCommitCohort cohort = mock(DOMStoreThreePhaseCommitCohort.class);

    private CommitCoordinationTask task;

    @Before
    public void setUp() throws Exception {
        final DOMDataTreeWriteTransaction tx = mock(DOMDataTreeWriteTransaction.class);
        task = new CommitCoordinationTask(tx, cohort);
        doReturn("test").when(tx).getIdentifier();
    }

    @Test(expected = TransactionCommitFailedException.class)
    public void canCommitBlockingWithFail() throws Exception {
        doReturn(FluentFutures.immediateNullFluentFuture()).when(cohort).abort();

        doReturn(FluentFutures.immediateFalseFluentFuture()).when(cohort).canCommit();
        task.call();
    }

    @Test(expected = TransactionCommitFailedException.class)
    public void canCommitBlockingWithFailException() throws Exception {
        doReturn(FluentFutures.immediateNullFluentFuture()).when(cohort).abort();

        doReturn(Futures.immediateFailedFuture(new InterruptedException())).when(cohort).canCommit();
        task.call();
    }

    @Test(expected = TransactionCommitFailedException.class)
    public void preCommitBlockingWithFail() throws Exception {
        doReturn(FluentFutures.immediateTrueFluentFuture()).when(cohort).canCommit();
        doReturn(FluentFutures.immediateNullFluentFuture()).when(cohort).abort();

        doReturn(Futures.immediateFailedFuture(new InterruptedException())).when(cohort).preCommit();
        task.call();
    }

    @Test(expected = TransactionCommitFailedException.class)
    public void commitBlockingWithFail() throws Exception {
        doReturn(FluentFutures.immediateTrueFluentFuture()).when(cohort).canCommit();
        doReturn(FluentFutures.immediateNullFluentFuture()).when(cohort).preCommit();
        doReturn(FluentFutures.immediateNullFluentFuture()).when(cohort).abort();

        doReturn(Futures.immediateFailedFuture(new InterruptedException())).when(cohort).commit();
        task.call();
    }
}