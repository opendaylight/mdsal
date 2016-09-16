/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.DOM_DATA_TREE_IDENTIFIER;

import java.lang.reflect.Field;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;

public class ShardSubmitCoordinationTaskTest {

    private final InmemoryDOMDataTreeShardWriteTransaction tx = mock(InmemoryDOMDataTreeShardWriteTransaction.class);

    @Before
    public void setUp() throws Exception {
        doReturn("TestTx").when(tx).getIdentifier();
        doReturn("TestTx").when(tx).toString();
        doNothing().when(tx).transactionCommited(any());
    }

    @Test
    public void basicTest() throws Exception {
        final ShardSubmitCoordinationTask shardSubmitCoordinationTask =
                new ShardSubmitCoordinationTask(DOM_DATA_TREE_IDENTIFIER, Collections.EMPTY_SET, tx);

        final ShardCanCommitCoordinationTask canCommitCoordinationTask = mock(ShardCanCommitCoordinationTask.class);
        doNothing().when(canCommitCoordinationTask).canCommitBlocking();
        final ShardPreCommitCoordinationTask preCommitCoordinationTask = mock(ShardPreCommitCoordinationTask.class);
        doNothing().when(preCommitCoordinationTask).preCommitBlocking();
        final ShardCommitCoordinationTask commitCoordinationTask = mock(ShardCommitCoordinationTask.class);
        doNothing().when(commitCoordinationTask).commitBlocking();

        Field canCommitCoordinationTaskField =
                ShardSubmitCoordinationTask.class.getDeclaredField("canCommitCoordinationTask");
        Field preCommitCoordinationTaskField =
                ShardSubmitCoordinationTask.class.getDeclaredField("preCommitCoordinationTask");
        Field commitCoordinationTaskField =
                ShardSubmitCoordinationTask.class.getDeclaredField("commitCoordinationTask");

        canCommitCoordinationTaskField.setAccessible(true);
        preCommitCoordinationTaskField.setAccessible(true);
        commitCoordinationTaskField.setAccessible(true);

        canCommitCoordinationTaskField.set(shardSubmitCoordinationTask, canCommitCoordinationTask);
        preCommitCoordinationTaskField.set(shardSubmitCoordinationTask, preCommitCoordinationTask);
        commitCoordinationTaskField.set(shardSubmitCoordinationTask, commitCoordinationTask);

        shardSubmitCoordinationTask.call();

        verify(canCommitCoordinationTask).canCommitBlocking();
        verify(preCommitCoordinationTask).preCommitBlocking();
        verify(commitCoordinationTask).commitBlocking();
    }
}