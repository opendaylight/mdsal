/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.google.common.base.Optional;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

public class WriteableNodeOperationTest extends WriteableNodeOperation implements TestUtils {

    @Test
    public void enterTest() throws Exception {
        assertEquals(DOM_DATA_TREE_WRITE_CURSOR, this.getCursor());

        returnNull();
        assertEquals(DOM_DATA_TREE_WRITE_CURSOR.toString(), this.enter(PATH_ARGUMENT).toString());
        verify(DOM_DATA_TREE_WRITE_CURSOR).enter(PATH_ARGUMENT);
        verify(WRITEABLE_MODIFICATION_NODE).getChild(PATH_ARGUMENT);

        returnNoNull();
        this.enter(PATH_ARGUMENT);
        verify(WRITEABLE_MODIFICATION_NODE).createOperation(DOM_DATA_TREE_WRITE_CURSOR);
    }

    @Test
    public void writeTest() throws Exception {
        returnNull();
        this.write(PATH_ARGUMENT, NORMALIZED_NODE);
        verify(DOM_DATA_TREE_WRITE_CURSOR).write(PATH_ARGUMENT, NORMALIZED_NODE);

        returnNoNull(NORMALIZED_NODE_CONTAINER);
        this.write(PATH_ARGUMENT, NORMALIZED_NODE_CONTAINER);
        verify(WRITE_CURSOR_STRATEGY).writeToCurrent(NORMALIZED_NODE_CONTAINER);
    }

    @Test
    public void writeToCurrentTest() throws Exception {
        returnNull();
        Map<PathArgument, WriteableModificationNode> childrenWithSubShards = new HashMap<>();
        childrenWithSubShards.put(PATH_ARGUMENT, WRITEABLE_MODIFICATION_NODE);
        doReturn(childrenWithSubShards).when(WRITEABLE_MODIFICATION_NODE).getChildrenWithSubshards();
        doReturn(Optional.of(NORMALIZED_NODE)).when(NORMALIZED_NODE_CONTAINER).getChild(PATH_ARGUMENT);
        this.writeToCurrent(NORMALIZED_NODE_CONTAINER);
        verify(DOM_DATA_TREE_WRITE_CURSOR).delete(PATH_ARGUMENT);
        verify(DOM_DATA_TREE_WRITE_CURSOR).exit();
    }

    @Test
    public void mergeTest() throws Exception {
        returnNull();
        this.merge(PATH_ARGUMENT, NORMALIZED_NODE_CONTAINER);
        verify(DOM_DATA_TREE_WRITE_CURSOR).merge(PATH_ARGUMENT, NORMALIZED_NODE_CONTAINER);

        returnNoNull(NORMALIZED_NODE_CONTAINER);
        this.merge(PATH_ARGUMENT, NORMALIZED_NODE_CONTAINER);
        verify(WRITE_CURSOR_STRATEGY).mergeToCurrent(NORMALIZED_NODE_CONTAINER);
    }

    @Test
    public void deleteTest() throws Exception {
        returnNoNull();
        this.delete(PATH_ARGUMENT);
        verify(DOM_DATA_TREE_WRITE_CURSOR).delete(PATH_ARGUMENT);
        verify(WRITEABLE_MODIFICATION_NODE).markDeleted();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void mergeToCurrentTestWithException() throws Exception {
        returnNoNull();
        this.mergeToCurrent(NORMALIZED_NODE_CONTAINER);
    }

    @Test
    public void mergeToCurrentTest() throws Exception {
        returnNull();
        this.mergeToCurrent(NORMALIZED_NODE_CONTAINER);
        verify(DOM_DATA_TREE_WRITE_CURSOR).merge(PATH_ARGUMENT, NORMALIZED_NODE);
    }

    private static void returnNull() {
        doReturn(null).when(WRITEABLE_MODIFICATION_NODE).getChild(PATH_ARGUMENT);
        doReturn(null).when(WRITEABLE_MODIFICATION_NODE).createOperation(DOM_DATA_TREE_WRITE_CURSOR);
    }

    private static void returnNoNull() {
        returnNoNull(null);
    }

    private static void returnNoNull(NormalizedNodeContainer normalizedNode) {
        if (normalizedNode != null) {
            doNothing().when(WRITE_CURSOR_STRATEGY).writeToCurrent(normalizedNode);
            doNothing().when(WRITE_CURSOR_STRATEGY).mergeToCurrent(normalizedNode);
            doReturn(WRITE_CURSOR_STRATEGY)
                    .when(WRITEABLE_MODIFICATION_NODE).createOperation(DOM_DATA_TREE_WRITE_CURSOR);
        }

        doReturn(WRITEABLE_MODIFICATION_NODE).when(WRITEABLE_MODIFICATION_NODE).getChild(PATH_ARGUMENT);
        doNothing().when(WRITEABLE_MODIFICATION_NODE).markDeleted();
    }

    @Before
    public void setUp() {
        final Collection<NormalizedNode> collectionNodes = new HashSet<>();
        doReturn("testArgument").when(PATH_ARGUMENT).toString();
        doReturn("testCursor").when(DOM_DATA_TREE_WRITE_CURSOR).toString();
        doReturn("testNode").when(NORMALIZED_NODE).toString();
        doReturn("testNodeContainer").when(NORMALIZED_NODE_CONTAINER).toString();
        doNothing().when(DOM_DATA_TREE_WRITE_CURSOR).enter(PATH_ARGUMENT);
        doNothing().when(DOM_DATA_TREE_WRITE_CURSOR).write(PATH_ARGUMENT, NORMALIZED_NODE);
        doNothing().when(DOM_DATA_TREE_WRITE_CURSOR).write(PATH_ARGUMENT, NORMALIZED_NODE_CONTAINER);
        doNothing().when(DOM_DATA_TREE_WRITE_CURSOR).merge(PATH_ARGUMENT, NORMALIZED_NODE);
        doNothing().when(DOM_DATA_TREE_WRITE_CURSOR).merge(PATH_ARGUMENT, NORMALIZED_NODE_CONTAINER);
        doNothing().when(DOM_DATA_TREE_WRITE_CURSOR).delete(PATH_ARGUMENT);
        doNothing().when(DOM_DATA_TREE_WRITE_CURSOR).exit();

        collectionNodes.add(NORMALIZED_NODE);
        doReturn(collectionNodes).when(NORMALIZED_NODE_CONTAINER).getValue();
        doReturn(PATH_ARGUMENT).when(NORMALIZED_NODE_CONTAINER).getIdentifier();
        doReturn(PATH_ARGUMENT).when(NORMALIZED_NODE).getIdentifier();
    }

    @After
    public void resetMocks() {
        TestUtils.resetMocks();
    }

    public WriteableNodeOperationTest(){
        super(WRITEABLE_MODIFICATION_NODE, DOM_DATA_TREE_WRITE_CURSOR);
    }

    @Override
    public void exit() {
        // NOOP
    }
}