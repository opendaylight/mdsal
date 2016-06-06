/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;

public class ShardCanCommitCoordinationTaskTest implements TestUtils{

    @Test
    public void basicTest() throws Exception {
        doReturn(true).when(LISTENABLE_FUTURE).get();
        doReturn(LISTENABLE_FUTURE).when(DOM_STORE_THREE_PHASE_COMMIT_COHORT).canCommit();

        COHORTS.add(DOM_STORE_THREE_PHASE_COMMIT_COHORT);

        ShardCanCommitCoordinationTask shardCanCommitCoordinationTask =
                new ShardCanCommitCoordinationTask(DOM_DATA_TREE_IDENTIFIER, COHORTS);

        shardCanCommitCoordinationTask.call();
        verify(DOM_STORE_THREE_PHASE_COMMIT_COHORT).canCommit();
    }

    @Test(expected = TransactionCommitFailedException.class)
    public void exceptionCallTest() throws Exception {
        doThrow(new InterruptedException()).when(LISTENABLE_FUTURE).get();
        doReturn(LISTENABLE_FUTURE).when(DOM_STORE_THREE_PHASE_COMMIT_COHORT).canCommit();

        COHORTS.add(DOM_STORE_THREE_PHASE_COMMIT_COHORT);
        ShardCanCommitCoordinationTask shardCanCommitCoordinationTask =
                new ShardCanCommitCoordinationTask(DOM_DATA_TREE_IDENTIFIER, COHORTS);
        shardCanCommitCoordinationTask.call();
    }
}