/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.shard;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

public class WriteableSubshardBoundaryNodeTest {

    private static final ForeignShardModificationContext FOREIGN_SHARD_MODIFICATION_CONTEXT =
            new ForeignShardModificationContext(
                    TestUtils.DOM_DATA_TREE_IDENTIFIER, TestUtils.DOM_DATA_TREE_SHARD_PRODUCER);

    @Test
    public void createOperation() throws Exception {
        final WriteableSubshardBoundaryNode writeableSubshardBoundaryNode =
                WriteableSubshardBoundaryNode.from(FOREIGN_SHARD_MODIFICATION_CONTEXT);

        doNothing().when(TestUtils.DOM_DATA_TREE_WRITE_CURSOR).exit();
        doReturn(TestUtils.DOM_DATA_TREE_SHARD_WRITE_TRANSACTION)
                .when(TestUtils.DOM_DATA_TREE_SHARD_PRODUCER).createTransaction();
        doReturn(TestUtils.DOM_DATA_TREE_WRITE_CURSOR)
                .when(TestUtils.DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).createCursor(TestUtils.DOM_DATA_TREE_IDENTIFIER);
        doNothing().when(TestUtils.DOM_DATA_TREE_WRITE_CURSOR)
                .enter(TestUtils.DOM_DATA_TREE_IDENTIFIER.getRootIdentifier().getLastPathArgument());
        doNothing().when(TestUtils.DOM_DATA_TREE_WRITE_CURSOR).exit();

        writeableSubshardBoundaryNode.createOperation(TestUtils.DOM_DATA_TREE_WRITE_CURSOR).exit();
        verify(TestUtils.DOM_DATA_TREE_WRITE_CURSOR).exit();

        WriteCursorStrategy writeCursorStrategy =
                writeableSubshardBoundaryNode.createOperation(TestUtils.DOM_DATA_TREE_WRITE_CURSOR);
        assertNotNull(writeCursorStrategy);

        WriteCursorStrategy childWriteCursorStrategy =
                writeCursorStrategy.enter(TestUtils.DOM_DATA_TREE_IDENTIFIER.getRootIdentifier().getLastPathArgument());
        childWriteCursorStrategy.exit();
        verify(TestUtils.DOM_DATA_TREE_WRITE_CURSOR, times(2)).exit();
    }

    @Test
    public void getChildrenWithSubshards() throws Exception {
        final WriteableSubshardBoundaryNode writeableSubshardBoundaryNode =
                WriteableSubshardBoundaryNode.from(FOREIGN_SHARD_MODIFICATION_CONTEXT);

        assertNotNull(writeableSubshardBoundaryNode.getChildrenWithSubshards());
        assertSame(ImmutableMap.of(), writeableSubshardBoundaryNode.getChildrenWithSubshards());
        Assert.assertEquals(TestUtils.DOM_DATA_TREE_IDENTIFIER.getRootIdentifier().getLastPathArgument(),
                writeableSubshardBoundaryNode.getIdentifier());
        assertNull(writeableSubshardBoundaryNode.getChild(mock(PathArgument.class)));
    }

    @After
    public void reset() {
        TestUtils.resetMocks();
    }
}