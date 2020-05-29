/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.shard;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.opendaylight.mdsal.dom.spi.shard.TestUtils.DOM_DATA_TREE_IDENTIFIER;
import static org.opendaylight.mdsal.dom.spi.shard.TestUtils.DOM_DATA_TREE_SHARD_PRODUCER;
import static org.opendaylight.mdsal.dom.spi.shard.TestUtils.DOM_DATA_TREE_SHARD_WRITE_TRANSACTION;
import static org.opendaylight.mdsal.dom.spi.shard.TestUtils.DOM_DATA_TREE_WRITE_CURSOR;
import static org.opendaylight.mdsal.dom.spi.shard.TestUtils.resetMocks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ForeignShardThreePhaseCommitCohortTest {
    @Before
    public void setUp() throws Exception {
        doNothing().when(DOM_DATA_TREE_WRITE_CURSOR).close();
    }

    @Test
    public void basicTest() throws Exception {
        final ForeignShardModificationContext foreignShardModificationContext =
                new ForeignShardModificationContext(DOM_DATA_TREE_IDENTIFIER, DOM_DATA_TREE_SHARD_PRODUCER);
        doReturn(DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).when(DOM_DATA_TREE_SHARD_PRODUCER).createTransaction();
        doReturn(DOM_DATA_TREE_WRITE_CURSOR)
                .when(DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).createCursor(DOM_DATA_TREE_IDENTIFIER);
        doNothing().when(DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).ready();

        foreignShardModificationContext.getCursor();
        foreignShardModificationContext.ready();
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

        assertNull(foreignShardThreePhaseCommitCohort.abort().get());
    }

    @After
    public void reset() {
        resetMocks();
    }
}