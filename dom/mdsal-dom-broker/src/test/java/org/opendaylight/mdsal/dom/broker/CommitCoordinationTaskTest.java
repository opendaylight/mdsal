/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doReturn;

import com.google.common.util.concurrent.Futures;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreThreePhaseCommitCohort;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class CommitCoordinationTaskTest {
    @Mock
    private DOMStoreThreePhaseCommitCohort cohort;
    @Mock
    private DOMDataTreeWriteTransaction tx;

    private CommitCoordinationTask task;

    @Before
    public void setUp() {
        task = new CommitCoordinationTask(tx, cohort);
        doReturn("test").when(tx).getIdentifier();
    }

    @Test
    public void canCommitBlockingWithFail() {
        doReturn(FluentFutures.immediateNullFluentFuture()).when(cohort).abort();

        doReturn(FluentFutures.immediateFalseFluentFuture()).when(cohort).canCommit();
        assertThrows(TransactionCommitFailedException.class, task::call);
    }

    @Test
    public void canCommitBlockingWithFailException() {
        doReturn(FluentFutures.immediateNullFluentFuture()).when(cohort).abort();

        doReturn(Futures.immediateFailedFuture(new InterruptedException())).when(cohort).canCommit();
        assertThrows(TransactionCommitFailedException.class, task::call);
    }

    @Test
    public void preCommitBlockingWithFail() {
        doReturn(FluentFutures.immediateTrueFluentFuture()).when(cohort).canCommit();
        doReturn(FluentFutures.immediateNullFluentFuture()).when(cohort).abort();

        doReturn(Futures.immediateFailedFuture(new InterruptedException())).when(cohort).preCommit();
        assertThrows(TransactionCommitFailedException.class, task::call);
    }

    @Test
    public void commitBlockingWithFail() {
        doReturn(FluentFutures.immediateTrueFluentFuture()).when(cohort).canCommit();
        doReturn(FluentFutures.immediateNullFluentFuture()).when(cohort).preCommit();
        doReturn(FluentFutures.immediateNullFluentFuture()).when(cohort).abort();

        doReturn(Futures.immediateFailedFuture(new InterruptedException())).when(cohort).commit();
        assertThrows(TransactionCommitFailedException.class, task::call);
    }
}