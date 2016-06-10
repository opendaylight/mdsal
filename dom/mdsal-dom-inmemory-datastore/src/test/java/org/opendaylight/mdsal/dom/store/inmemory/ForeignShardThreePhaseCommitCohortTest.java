/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.DOM_DATA_TREE_IDENTIFIER;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.DOM_DATA_TREE_SHARD_PRODUCER;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.DOM_DATA_TREE_SHARD_WRITE_TRANSACTION;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.DOM_DATA_TREE_WRITE_CURSOR;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.resetMocks;

import org.junit.After;
import org.junit.Test;

public class ForeignShardThreePhaseCommitCohortTest {

    @Test
    public void basicTest() throws Exception {
        final ForeignShardModificationContext foreignShardModificationContext =
                new ForeignShardModificationContext(DOM_DATA_TREE_IDENTIFIER, DOM_DATA_TREE_SHARD_PRODUCER);
        doReturn(DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).when(DOM_DATA_TREE_SHARD_PRODUCER).createTransaction();
        doReturn(DOM_DATA_TREE_WRITE_CURSOR)
                .when(DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).createCursor(DOM_DATA_TREE_IDENTIFIER);
        foreignShardModificationContext.getCursor();

        final ForeignShardThreePhaseCommitCohort foreignShardThreePhaseCommitCohort =
                new ForeignShardThreePhaseCommitCohort(DOM_DATA_TREE_IDENTIFIER, foreignShardModificationContext);

        doReturn(null).when(DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).prepare();
        foreignShardThreePhaseCommitCohort.preCommit();
        verify(DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).prepare();

        doReturn(null).when(DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).validate();
        foreignShardThreePhaseCommitCohort.canCommit();
        verify(DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).validate();

        doReturn(null).when(DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).commit();
        foreignShardThreePhaseCommitCohort.commit();
        verify(DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).commit();

        assertEquals(null, foreignShardThreePhaseCommitCohort.abort().get());
    }

    @After
    public void reset() {
        resetMocks();
    }
}