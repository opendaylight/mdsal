/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.DOM_DATA_TREE_IDENTIFIER;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.DOM_DATA_TREE_SHARD_PRODUCER;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.DOM_DATA_TREE_SHARD_WRITE_TRANSACTION;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.DOM_DATA_TREE_WRITE_CURSOR;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.resetMocks;

import com.google.common.collect.ImmutableMap;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModificationCursor;

public class ShardDataModificationTest {
    private static final ShardRootModificationContext SHARD_ROOT_MODIFICATION_CONTEXT =
            mock(ShardRootModificationContext.class);
    private static final ForeignShardModificationContext FOREIGN_SHARD_MODIFICATION_CONTEXT =
            new ForeignShardModificationContext(DOM_DATA_TREE_IDENTIFIER, DOM_DATA_TREE_SHARD_PRODUCER);

    private static ShardDataModification shardDataModification = null;

    @Before
    public void setUp() throws Exception {
        doReturn(DOM_DATA_TREE_IDENTIFIER).when(SHARD_ROOT_MODIFICATION_CONTEXT).getIdentifier();
        shardDataModification = ShardDataModification.from(SHARD_ROOT_MODIFICATION_CONTEXT,
                ImmutableMap.of(YangInstanceIdentifier.of(QName.create("test")), FOREIGN_SHARD_MODIFICATION_CONTEXT));

        final Map<DOMDataTreeIdentifier, ForeignShardModificationContext> children =
                ImmutableMap.of(DOM_DATA_TREE_IDENTIFIER, FOREIGN_SHARD_MODIFICATION_CONTEXT);

        final Field childShardsField = ShardDataModification.class.getDeclaredField("childShards");
        childShardsField.setAccessible(true);
        childShardsField.set(shardDataModification, children);
    }

    @Test
    public void basicTest() throws Exception {
        assertEquals(DOM_DATA_TREE_IDENTIFIER.getRootIdentifier().getLastPathArgument(),
                shardDataModification.getIdentifier());

        assertEquals(DOM_DATA_TREE_IDENTIFIER, shardDataModification.getPrefix());

        DataTreeModification dataTreeModification = mock(DataTreeModification.class);
        doReturn(dataTreeModification).when(SHARD_ROOT_MODIFICATION_CONTEXT).ready();
        doReturn(DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).when(DOM_DATA_TREE_SHARD_PRODUCER).createTransaction();
        doReturn(DOM_DATA_TREE_WRITE_CURSOR).when(DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).createCursor(any());
        FOREIGN_SHARD_MODIFICATION_CONTEXT.getCursor();
        doNothing().when(DOM_DATA_TREE_WRITE_CURSOR).close();
        doNothing().when(DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).close();
        doNothing().when(DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).ready();

        shardDataModification.seal();
        verify(SHARD_ROOT_MODIFICATION_CONTEXT).ready();
        verify(DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).ready();
        verify(DOM_DATA_TREE_WRITE_CURSOR).close();

        shardDataModification.closeTransactions();
        verify(DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).close();
    }

    @Test(expected = IllegalStateException.class)
    public void createWithException() throws Exception {
        final DataTreeModificationCursor dataTreeModificationCursor = mock(DataTreeModificationCursor.class);
        final DataTreeModificationCursorAdaptor dataTreeModificationCursorAdaptor =
                DataTreeModificationCursorAdaptor.of(dataTreeModificationCursor);
        doReturn(dataTreeModificationCursorAdaptor).when(SHARD_ROOT_MODIFICATION_CONTEXT).cursor();
        shardDataModification.createOperation(null).exit();
    }

    @After
    public void reset() {
        resetMocks();
    }
}