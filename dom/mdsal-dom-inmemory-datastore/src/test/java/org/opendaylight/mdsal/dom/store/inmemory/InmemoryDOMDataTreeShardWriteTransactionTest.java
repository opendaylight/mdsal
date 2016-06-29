/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.DATA_TREE;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.DOM_DATA_TREE_SHARD_PRODUCER;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.resetMocks;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.MoreExecutors;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.Executors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModificationCursor;
import org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType;

public class InmemoryDOMDataTreeShardWriteTransactionTest {

    private static InmemoryDOMDataTreeShardWriteTransaction inmemoryDOMDataTreeShardWriteTransaction;
    private static ShardDataModification shardDataModification;
    private static final ShardRootModificationContext SHARD_ROOT_MODIFICATION_CONTEXT =
            mock(ShardRootModificationContext.class);
    private static final YangInstanceIdentifier YANG_INSTANCE_IDENTIFIER =
            YangInstanceIdentifier.of(QName.create("test"));
    private static final DOMDataTreeIdentifier DOM_DATA_TREE_IDENTIFIER =
            new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, YANG_INSTANCE_IDENTIFIER);
    private static final ForeignShardModificationContext FOREIGN_SHARD_MODIFICATION_CONTEXT =
            new ForeignShardModificationContext(DOM_DATA_TREE_IDENTIFIER, DOM_DATA_TREE_SHARD_PRODUCER);
    private static final ReadableWriteableDOMDataTreeShard READABLE_WRITEABLE_DOM_DATA_TREE_SHARD =
            mock(ReadableWriteableDOMDataTreeShard.class);
    private static final ChildShardContext CHILD_SHARD_CONTEXT =
            new ChildShardContext(DOM_DATA_TREE_IDENTIFIER, READABLE_WRITEABLE_DOM_DATA_TREE_SHARD);
    private static final Map<DOMDataTreeIdentifier, ChildShardContext> CHILD_SHARDS =
            ImmutableMap.of(DOM_DATA_TREE_IDENTIFIER, CHILD_SHARD_CONTEXT);

    @Before
    public void setUp() throws Exception {
        final DataTreeModification dataTreeModification = mock(DataTreeModification.class);
        doReturn("testDataTreeModification").when(dataTreeModification).toString();
        doReturn(dataTreeModification).when(SHARD_ROOT_MODIFICATION_CONTEXT).ready();
        doReturn(DOM_DATA_TREE_IDENTIFIER).when(SHARD_ROOT_MODIFICATION_CONTEXT).getIdentifier();
        shardDataModification = ShardDataModification.from(SHARD_ROOT_MODIFICATION_CONTEXT,
                ImmutableMap.of(YANG_INSTANCE_IDENTIFIER, FOREIGN_SHARD_MODIFICATION_CONTEXT));
        final DataTreeModificationCursor dataTreeModificationCursor = mock(DataTreeModificationCursor.class);
        doReturn(DataTreeModificationCursorAdaptor.of( dataTreeModificationCursor))
                .when(SHARD_ROOT_MODIFICATION_CONTEXT).cursor();
        final DataTreeCandidate dataTreeCandidate = mock(DataTreeCandidate.class);
        final DataTreeCandidateNode dataTreeCandidateNode = mock(DataTreeCandidateNode.class);
        doReturn(dataTreeCandidateNode).when(dataTreeCandidate).getRootNode();
        doReturn(ModificationType.WRITE).when(dataTreeCandidateNode).getModificationType();
        doReturn(YANG_INSTANCE_IDENTIFIER).when(dataTreeCandidate).getRootPath();
        doReturn("testDataTreeCandidate").when(dataTreeCandidate).toString();
        doReturn(dataTreeCandidate).when(DATA_TREE).prepare(any());
        final InMemoryDOMDataTreeShardChangePublisher inMemoryDOMDataTreeShardChangePublisher =
                new InMemoryDOMDataTreeShardChangePublisher(MoreExecutors.newDirectExecutorService(), 1, DATA_TREE,
                        YANG_INSTANCE_IDENTIFIER, CHILD_SHARDS);

        inmemoryDOMDataTreeShardWriteTransaction =
                new InmemoryDOMDataTreeShardWriteTransaction(shardDataModification, DATA_TREE,
                        inMemoryDOMDataTreeShardChangePublisher, MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor()));
    }

    @Test
    public void close() throws Exception {
        inmemoryDOMDataTreeShardWriteTransaction.createCursor(DOM_DATA_TREE_IDENTIFIER);
        inmemoryDOMDataTreeShardWriteTransaction.close();
        assertTrue(inmemoryDOMDataTreeShardWriteTransaction.isFinished());
    }

    @Test
    public void cursorClosed() throws Exception {
        final Field cursorField = InmemoryDOMDataTreeShardWriteTransaction.class.getDeclaredField("cursor");
        cursorField.setAccessible(true);
        DOMDataTreeWriteCursor cursor;

        inmemoryDOMDataTreeShardWriteTransaction.createCursor(DOM_DATA_TREE_IDENTIFIER);
        cursor = (DOMDataTreeWriteCursor) cursorField.get(inmemoryDOMDataTreeShardWriteTransaction);
        assertNotNull(cursor);

        inmemoryDOMDataTreeShardWriteTransaction.cursorClosed();
        cursor = (DOMDataTreeWriteCursor) cursorField.get(inmemoryDOMDataTreeShardWriteTransaction);
        assertNull(cursor);
    }

    @Test
    public void isFinished() throws Exception {
        assertFalse(inmemoryDOMDataTreeShardWriteTransaction.isFinished());
        inmemoryDOMDataTreeShardWriteTransaction.ready();
        assertTrue(inmemoryDOMDataTreeShardWriteTransaction.isFinished());
    }

    @Test
    public void ready() throws Exception {
        final Field childShardsField = ShardDataModification.class.getDeclaredField("childShards");
        childShardsField.setAccessible(true);
        childShardsField.set(shardDataModification,
                ImmutableMap.of(DOM_DATA_TREE_IDENTIFIER, FOREIGN_SHARD_MODIFICATION_CONTEXT));

        inmemoryDOMDataTreeShardWriteTransaction.ready();
        verify(SHARD_ROOT_MODIFICATION_CONTEXT).ready();
    }

    @Test
    public void submit() throws Exception {
        doNothing().when(DATA_TREE).validate(any());
        doNothing().when(DATA_TREE).commit(any());
        inmemoryDOMDataTreeShardWriteTransaction.ready();
        assertNull(inmemoryDOMDataTreeShardWriteTransaction.submit().get());
        verify(DATA_TREE).commit(any());
        verify(DATA_TREE).validate(any());
    }

    @Test
    public void validate() throws Exception {
        inmemoryDOMDataTreeShardWriteTransaction.ready();
        doNothing().when(DATA_TREE).validate(any());
        assertTrue(inmemoryDOMDataTreeShardWriteTransaction.validate().get());
        verify(DATA_TREE).validate(any());
    }

    @Test
    public void prepare() throws Exception {
        inmemoryDOMDataTreeShardWriteTransaction.ready();
        assertNull(inmemoryDOMDataTreeShardWriteTransaction.prepare().get());
        verify(DATA_TREE).prepare(any());
    }

    @Test
    public void commit() throws Exception {
        assertNull(inmemoryDOMDataTreeShardWriteTransaction.commit().get());
    }

    @Test
    public void createCursor() throws Exception {
        assertNotNull(inmemoryDOMDataTreeShardWriteTransaction.createCursor(DOM_DATA_TREE_IDENTIFIER));
    }

    @After
    public void mocksReset() {
        resetMocks();
        reset(SHARD_ROOT_MODIFICATION_CONTEXT);
    }
}