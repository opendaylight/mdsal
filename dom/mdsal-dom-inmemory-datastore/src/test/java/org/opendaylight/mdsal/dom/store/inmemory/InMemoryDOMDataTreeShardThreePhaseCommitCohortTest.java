/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.DATA_TREE;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.resetMocks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;

public class InMemoryDOMDataTreeShardThreePhaseCommitCohortTest {

    private static final DataTreeCandidate DATA_TREE_CANDIDATE = mock(DataTreeCandidate.class);
    private static final DataTreeCandidateNode DATA_TREE_CANDIDATE_NODE = mock(DataTreeCandidateNode.class);
    private static final DataTreeModification DATA_TREE_MODIFICATION = mock(DataTreeModification.class);
    private static final InMemoryDOMDataTreeShardChangePublisher IN_MEMORY_DOM_DATA_TREE_SHARD_CHANGE_PUBLISHER =
            new InMemoryDOMDataTreeShardChangePublisher(MoreExecutors.newDirectExecutorService(), 1, DATA_TREE,
                    YangInstanceIdentifier.of(QName.create("test")), ImmutableMap.of());
    private static final InMemoryDOMDataTreeShardThreePhaseCommitCohort
            IN_MEMORY_DOM_DATA_TREE_SHARD_THREE_PHASE_COMMIT_COHORT =
                new InMemoryDOMDataTreeShardThreePhaseCommitCohort(DATA_TREE, DATA_TREE_MODIFICATION,
                    IN_MEMORY_DOM_DATA_TREE_SHARD_CHANGE_PUBLISHER);

    @Before
    public void setUp() throws Exception {
        doReturn(YangInstanceIdentifier.EMPTY).when(DATA_TREE_CANDIDATE).getRootPath();
        doReturn("testDataTreeCandidate").when(DATA_TREE_CANDIDATE).toString();
        doReturn(DATA_TREE_CANDIDATE_NODE).when(DATA_TREE_CANDIDATE).getRootNode();
        doReturn(DATA_TREE_CANDIDATE).when(DATA_TREE).prepare(any());

        doReturn(ModificationType.WRITE).when(DATA_TREE_CANDIDATE_NODE).getModificationType();
        doReturn(ImmutableSet.of()).when(DATA_TREE_CANDIDATE_NODE).getChildNodes();

        doNothing().when(DATA_TREE).validate(any());
        doNothing().when(DATA_TREE).commit(any());

        doReturn("testDataTreeModification").when(DATA_TREE_MODIFICATION).toString();
    }

    @Test
    public void basicTest() throws Exception {
        IN_MEMORY_DOM_DATA_TREE_SHARD_THREE_PHASE_COMMIT_COHORT.canCommit();
        verify(DATA_TREE).validate(any());
        IN_MEMORY_DOM_DATA_TREE_SHARD_THREE_PHASE_COMMIT_COHORT.preCommit();
        verify(DATA_TREE).prepare(any());
        IN_MEMORY_DOM_DATA_TREE_SHARD_THREE_PHASE_COMMIT_COHORT.commit();
        verify(DATA_TREE).commit(any());
        IN_MEMORY_DOM_DATA_TREE_SHARD_THREE_PHASE_COMMIT_COHORT.abort();
    }

    @Test(expected = IllegalStateException.class)
    public void abortWithExceptionTest() throws Exception {
        IN_MEMORY_DOM_DATA_TREE_SHARD_THREE_PHASE_COMMIT_COHORT.abort();
        IN_MEMORY_DOM_DATA_TREE_SHARD_THREE_PHASE_COMMIT_COHORT.commit();
    }

    @Test
    public void preCommitWithExceptionTest() throws Exception {
        doThrow(new RuntimeException("testException")).when(DATA_TREE).prepare(any());
        try {
            IN_MEMORY_DOM_DATA_TREE_SHARD_THREE_PHASE_COMMIT_COHORT.preCommit().get();
            Assert.fail("Expected Exception");
        } catch (ExecutionException e) {
            assertTrue(e.getCause().getMessage().contains("testException"));
        }
    }

    @Test
    public void canCommitWithDataValidationFailedExceptionTest() throws Exception {
        doThrow(new DataValidationFailedException(YangInstanceIdentifier.EMPTY, "testException"))
                .when(DATA_TREE).validate(any());
        try {
            IN_MEMORY_DOM_DATA_TREE_SHARD_THREE_PHASE_COMMIT_COHORT.canCommit().get();
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof TransactionCommitFailedException);
        }
    }

    @Test
    public void canCommitWithExceptionTest() throws Exception {
        doThrow(new RuntimeException("testException")).when(DATA_TREE).validate(any());
        try {
            IN_MEMORY_DOM_DATA_TREE_SHARD_THREE_PHASE_COMMIT_COHORT.canCommit().get();
            Assert.fail("Expected Exception");
        } catch (ExecutionException e) {
            assertTrue(e.getCause().getMessage().contains("testException"));
        }
    }

    @After
    public void reset() {
        resetMocks();
    }
}