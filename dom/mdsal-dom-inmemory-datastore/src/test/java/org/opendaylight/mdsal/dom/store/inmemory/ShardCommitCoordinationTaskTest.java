/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.COHORTS;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.DOM_DATA_TREE_IDENTIFIER;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.DOM_STORE_THREE_PHASE_COMMIT_COHORT;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.LISTENABLE_FUTURE;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.resetMocks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;

public class ShardCommitCoordinationTaskTest {

    final InmemoryDOMDataTreeShardWriteTransaction mockTx = mock(InmemoryDOMDataTreeShardWriteTransaction.class);

    @Before
    public void setUp() throws Exception {
        doReturn("MockedTx").when(mockTx).toString();
        doNothing().when(mockTx).transactionCommited(any());
    }

    @Test
    public void basicTest() throws Exception {
        doReturn(Void.TYPE).when(LISTENABLE_FUTURE).get();
        doReturn(LISTENABLE_FUTURE).when(DOM_STORE_THREE_PHASE_COMMIT_COHORT).commit();

        COHORTS.add(DOM_STORE_THREE_PHASE_COMMIT_COHORT);

        ShardCommitCoordinationTask shardCommitCoordinationTask =
                new ShardCommitCoordinationTask(DOM_DATA_TREE_IDENTIFIER, COHORTS, mockTx);

        shardCommitCoordinationTask.call();
        verify(DOM_STORE_THREE_PHASE_COMMIT_COHORT).commit();
    }

    @Test(expected = TransactionCommitFailedException.class)
    public void exceptionCallTest() throws Exception {
        doThrow(new InterruptedException()).when(LISTENABLE_FUTURE).get();
        doReturn(LISTENABLE_FUTURE).when(DOM_STORE_THREE_PHASE_COMMIT_COHORT).commit();

        COHORTS.add(DOM_STORE_THREE_PHASE_COMMIT_COHORT);
        ShardCommitCoordinationTask shardCommitCoordinationTask =
                new ShardCommitCoordinationTask(DOM_DATA_TREE_IDENTIFIER, COHORTS, mockTx);
        shardCommitCoordinationTask.call();
    }

    @After
    public void reset() {
        resetMocks();
    }
}