/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.DOM_DATA_TREE_IDENTIFIER;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.DOM_DATA_TREE_SHARD_PRODUCER;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.DOM_DATA_TREE_SHARD_WRITE_TRANSACTION;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.DOM_DATA_TREE_WRITE_CURSOR;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.resetMocks;

import java.util.Collections;
import org.junit.After;
import org.junit.Test;

public class WriteableSubshardBoundaryNodeTest {

    private static final ForeignShardModificationContext FOREIGN_SHARD_MODIFICATION_CONTEXT =
            new ForeignShardModificationContext(DOM_DATA_TREE_IDENTIFIER, DOM_DATA_TREE_SHARD_PRODUCER);

    @Test
    public void createOperation() throws Exception {
        final WriteableSubshardBoundaryNode writeableSubshardBoundaryNode =
                WriteableSubshardBoundaryNode.from(FOREIGN_SHARD_MODIFICATION_CONTEXT);

        doNothing().when(DOM_DATA_TREE_WRITE_CURSOR).exit();
        doReturn(DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).when(DOM_DATA_TREE_SHARD_PRODUCER).createTransaction();
        doReturn(DOM_DATA_TREE_WRITE_CURSOR)
                .when(DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).createCursor(DOM_DATA_TREE_IDENTIFIER);
        doNothing().when(DOM_DATA_TREE_WRITE_CURSOR)
                .enter(DOM_DATA_TREE_IDENTIFIER.getRootIdentifier().getLastPathArgument());
        doNothing().when(DOM_DATA_TREE_WRITE_CURSOR).exit();

        writeableSubshardBoundaryNode.createOperation(DOM_DATA_TREE_WRITE_CURSOR).exit();
        verify(DOM_DATA_TREE_WRITE_CURSOR).exit();

        WriteCursorStrategy writeCursorStrategy =
                writeableSubshardBoundaryNode.createOperation(DOM_DATA_TREE_WRITE_CURSOR);
        assertNotNull(writeCursorStrategy);

        WriteCursorStrategy childWriteCursorStrategy =
                writeCursorStrategy.enter(DOM_DATA_TREE_IDENTIFIER.getRootIdentifier().getLastPathArgument());
        childWriteCursorStrategy.exit();
        verify(DOM_DATA_TREE_WRITE_CURSOR, times(2)).exit();
    }

    @Test
    public void getChildrenWithSubshards() throws Exception {
        final WriteableSubshardBoundaryNode writeableSubshardBoundaryNode =
                WriteableSubshardBoundaryNode.from(FOREIGN_SHARD_MODIFICATION_CONTEXT);

        assertNotNull(writeableSubshardBoundaryNode.getChildrenWithSubshards());
        assertSame(Collections.emptyMap(),writeableSubshardBoundaryNode.getChildrenWithSubshards());
        assertEquals(DOM_DATA_TREE_IDENTIFIER.getRootIdentifier().getLastPathArgument(),
                writeableSubshardBoundaryNode.getIdentifier());
        assertNull(writeableSubshardBoundaryNode.getChild(null));
    }

    @After
    public void reset() {
        resetMocks();
    }
}