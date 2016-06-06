/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableList;
import java.lang.reflect.Field;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModificationCursor;

public class ShardDataModificationCursorTest implements TestUtils {

    @Test
    public void basicTest() throws Exception {
        final DataTreeModificationCursor dataTreeModificationCursor = mock(DataTreeModificationCursor.class);
        final DataTreeModificationCursorAdaptor dataTreeModificationCursorAdaptor =
                DataTreeModificationCursorAdaptor.of(dataTreeModificationCursor);
        final ShardRootModificationContext shardRootModificationContext = mock(ShardRootModificationContext.class);
        final Map<PathArgument, WriteableModificationNode> children = new HashMap<>();
        children.put(PATH_ARGUMENT, WRITEABLE_MODIFICATION_NODE);
        final ShardDataModification root =
                new ShardDataModificationBuilder(shardRootModificationContext).build(children);
        doReturn(dataTreeModificationCursorAdaptor).when(shardRootModificationContext).cursor();
        InmemoryDOMDataTreeShardWriteTransaction inmemoryDOMDataTreeShardWriteTransaction =
                mock(InmemoryDOMDataTreeShardWriteTransaction.class);
        ShardDataModificationCursor shardDataModificationCursor =
                new ShardDataModificationCursor(root, inmemoryDOMDataTreeShardWriteTransaction);

        final Field stackField = ShardDataModificationCursor.class.getDeclaredField("stack");
        stackField.setAccessible(true);

        final Deque<WriteCursorStrategy> stack =
                (Deque<WriteCursorStrategy>) stackField.get(shardDataModificationCursor);
        stack.clear();
        stack.push(WRITE_CURSOR_STRATEGY);
        stackField.set(shardDataModificationCursor, stack);

        doNothing().when(WRITE_CURSOR_STRATEGY).delete(PATH_ARGUMENT);
        doNothing().when(WRITE_CURSOR_STRATEGY).merge(PATH_ARGUMENT, NORMALIZED_NODE);
        doNothing().when(WRITE_CURSOR_STRATEGY).write(PATH_ARGUMENT, NORMALIZED_NODE);
        doReturn("testPathArgument").when(PATH_ARGUMENT).toString();

        shardDataModificationCursor.delete(PATH_ARGUMENT);
        verify(WRITE_CURSOR_STRATEGY).delete(PATH_ARGUMENT);
        shardDataModificationCursor.merge(PATH_ARGUMENT, NORMALIZED_NODE);
        verify(WRITE_CURSOR_STRATEGY).merge(PATH_ARGUMENT, NORMALIZED_NODE);
        shardDataModificationCursor.write(PATH_ARGUMENT, NORMALIZED_NODE);
        verify(WRITE_CURSOR_STRATEGY).write(PATH_ARGUMENT, NORMALIZED_NODE);

        doReturn(WRITE_CURSOR_STRATEGY).when(WRITE_CURSOR_STRATEGY).enter(PATH_ARGUMENT);
        shardDataModificationCursor.enter(ImmutableList.of(PATH_ARGUMENT));
        shardDataModificationCursor.enter(PATH_ARGUMENT, PATH_ARGUMENT);
        verify(WRITE_CURSOR_STRATEGY, times(3)).enter(PATH_ARGUMENT);

        doNothing().when(inmemoryDOMDataTreeShardWriteTransaction).cursorClosed();
        shardDataModificationCursor.close();
        verify(inmemoryDOMDataTreeShardWriteTransaction).cursorClosed();

        doNothing().when(WRITE_CURSOR_STRATEGY).exit();
        shardDataModificationCursor.exit(1);
        verify(WRITE_CURSOR_STRATEGY).exit();
    }

    @After
    public void resetMocks() {
        TestUtils.resetMocks();
    }
}