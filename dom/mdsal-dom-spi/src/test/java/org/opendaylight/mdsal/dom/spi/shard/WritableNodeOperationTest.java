/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.shard;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.google.common.base.Optional;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

public class WritableNodeOperationTest extends WritableNodeOperation {

    @Test
    public void enterTest() throws Exception {
        Assert.assertEquals(TestUtils.DOM_DATA_TREE_WRITE_CURSOR, this.getCursor());

        returnNull();
        Assert.assertEquals(TestUtils.DOM_DATA_TREE_WRITE_CURSOR.toString(),
                this.enter(TestUtils.PATH_ARGUMENT).toString());
        verify(TestUtils.DOM_DATA_TREE_WRITE_CURSOR).enter(TestUtils.PATH_ARGUMENT);
        verify(TestUtils.WRITEABLE_MODIFICATION_NODE).getChild(TestUtils.PATH_ARGUMENT);

        returnNoNull();
        this.enter(TestUtils.PATH_ARGUMENT);
        verify(TestUtils.WRITEABLE_MODIFICATION_NODE).createOperation(TestUtils.DOM_DATA_TREE_WRITE_CURSOR);
    }

    @Test
    public void writeTest() throws Exception {
        returnNull();
        this.write(TestUtils.PATH_ARGUMENT, TestUtils.NORMALIZED_NODE);
        verify(TestUtils.DOM_DATA_TREE_WRITE_CURSOR).write(TestUtils.PATH_ARGUMENT, TestUtils.NORMALIZED_NODE);

        returnNoNull(TestUtils.NORMALIZED_NODE_CONTAINER);
        this.write(TestUtils.PATH_ARGUMENT, TestUtils.NORMALIZED_NODE_CONTAINER);
        verify(TestUtils.WRITE_CURSOR_STRATEGY).writeToCurrent(TestUtils.NORMALIZED_NODE_CONTAINER);
    }

    @Test
    public void writeToCurrentTest() throws Exception {
        returnNull();
        Map<PathArgument, WriteableModificationNode> childrenWithSubShards = new HashMap<>();
        childrenWithSubShards.put(TestUtils.PATH_ARGUMENT, TestUtils.WRITEABLE_MODIFICATION_NODE);
        doReturn(childrenWithSubShards).when(TestUtils.WRITEABLE_MODIFICATION_NODE).getChildrenWithSubshards();
        doReturn(Optional.of(TestUtils.NORMALIZED_NODE))
                .when(TestUtils.NORMALIZED_NODE_CONTAINER).getChild(TestUtils.PATH_ARGUMENT);
        this.writeToCurrent(TestUtils.NORMALIZED_NODE_CONTAINER);
        verify(TestUtils.DOM_DATA_TREE_WRITE_CURSOR).delete(TestUtils.PATH_ARGUMENT);
        verify(TestUtils.DOM_DATA_TREE_WRITE_CURSOR).exit();
    }

    @Test
    public void mergeTest() throws Exception {
        returnNull();
        this.merge(TestUtils.PATH_ARGUMENT, TestUtils.NORMALIZED_NODE_CONTAINER);
        verify(TestUtils.DOM_DATA_TREE_WRITE_CURSOR)
                .merge(TestUtils.PATH_ARGUMENT, TestUtils.NORMALIZED_NODE_CONTAINER);

        returnNoNull(TestUtils.NORMALIZED_NODE_CONTAINER);
        this.merge(TestUtils.PATH_ARGUMENT, TestUtils.NORMALIZED_NODE_CONTAINER);
        verify(TestUtils.WRITE_CURSOR_STRATEGY).mergeToCurrent(TestUtils.NORMALIZED_NODE_CONTAINER);
    }

    @Test
    public void deleteTest() throws Exception {
        returnNoNull();
        this.delete(TestUtils.PATH_ARGUMENT);
        verify(TestUtils.DOM_DATA_TREE_WRITE_CURSOR).delete(TestUtils.PATH_ARGUMENT);
        verify(TestUtils.WRITEABLE_MODIFICATION_NODE).markDeleted();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void mergeToCurrentTestWithException() throws Exception {
        returnNoNull();
        this.mergeToCurrent(TestUtils.NORMALIZED_NODE_CONTAINER);
    }

    @Test
    public void mergeToCurrentTest() throws Exception {
        returnNull();
        this.mergeToCurrent(TestUtils.NORMALIZED_NODE_CONTAINER);
        verify(TestUtils.DOM_DATA_TREE_WRITE_CURSOR).merge(TestUtils.PATH_ARGUMENT, TestUtils.NORMALIZED_NODE);
    }

    private static void returnNull() {
        doReturn(null).when(TestUtils.WRITEABLE_MODIFICATION_NODE).getChild(TestUtils.PATH_ARGUMENT);
        doReturn(null).when(TestUtils.WRITEABLE_MODIFICATION_NODE)
                .createOperation(TestUtils.DOM_DATA_TREE_WRITE_CURSOR);
    }

    private static void returnNoNull() {
        returnNoNull(null);
    }

    private static void returnNoNull(final NormalizedNodeContainer<?, ?, ?> normalizedNode) {
        if (normalizedNode != null) {
            doNothing().when(TestUtils.WRITE_CURSOR_STRATEGY).writeToCurrent(normalizedNode);
            doNothing().when(TestUtils.WRITE_CURSOR_STRATEGY).mergeToCurrent(normalizedNode);
            doReturn(TestUtils.WRITE_CURSOR_STRATEGY)
                    .when(TestUtils.WRITEABLE_MODIFICATION_NODE).createOperation(TestUtils.DOM_DATA_TREE_WRITE_CURSOR);
        }

        doReturn(TestUtils.WRITEABLE_MODIFICATION_NODE)
                .when(TestUtils.WRITEABLE_MODIFICATION_NODE).getChild(TestUtils.PATH_ARGUMENT);
        doNothing().when(TestUtils.WRITEABLE_MODIFICATION_NODE).markDeleted();
    }

    @Before
    public void setUp() {
        final Collection<NormalizedNode<?, ?>> collectionNodes = new HashSet<>();
        doReturn("testArgument").when(TestUtils.PATH_ARGUMENT).toString();
        doReturn("testCursor").when(TestUtils.DOM_DATA_TREE_WRITE_CURSOR).toString();
        doReturn("testNode").when(TestUtils.NORMALIZED_NODE).toString();
        doReturn("testNodeContainer").when(TestUtils.NORMALIZED_NODE_CONTAINER).toString();
        doNothing().when(TestUtils.DOM_DATA_TREE_WRITE_CURSOR).enter(TestUtils.PATH_ARGUMENT);
        doNothing().when(TestUtils.DOM_DATA_TREE_WRITE_CURSOR)
                .write(TestUtils.PATH_ARGUMENT, TestUtils.NORMALIZED_NODE);
        doNothing().when(TestUtils.DOM_DATA_TREE_WRITE_CURSOR)
                .write(TestUtils.PATH_ARGUMENT, TestUtils.NORMALIZED_NODE_CONTAINER);
        doNothing().when(TestUtils.DOM_DATA_TREE_WRITE_CURSOR)
                .merge(TestUtils.PATH_ARGUMENT, TestUtils.NORMALIZED_NODE);
        doNothing().when(TestUtils.DOM_DATA_TREE_WRITE_CURSOR)
                .merge(TestUtils.PATH_ARGUMENT, TestUtils.NORMALIZED_NODE_CONTAINER);
        doNothing().when(TestUtils.DOM_DATA_TREE_WRITE_CURSOR).delete(TestUtils.PATH_ARGUMENT);
        doNothing().when(TestUtils.DOM_DATA_TREE_WRITE_CURSOR).exit();

        collectionNodes.add(TestUtils.NORMALIZED_NODE);
        doReturn(collectionNodes).when(TestUtils.NORMALIZED_NODE_CONTAINER).getValue();
        doReturn(TestUtils.PATH_ARGUMENT).when(TestUtils.NORMALIZED_NODE_CONTAINER).getIdentifier();
        doReturn(TestUtils.PATH_ARGUMENT).when(TestUtils.NORMALIZED_NODE).getIdentifier();
    }

    @After
    public void reset() {
        TestUtils.resetMocks();
    }

    public WritableNodeOperationTest() {
        super(TestUtils.WRITEABLE_MODIFICATION_NODE, TestUtils.DOM_DATA_TREE_WRITE_CURSOR);
    }

    @Override
    public void exit() {
        // NOOP
    }
}