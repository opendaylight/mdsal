/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.shard;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableList;
import org.junit.After;
import org.junit.Test;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;

public class DelegatingWriteCursorStrategyTest extends DelegatingWriteCursorStrategy {

    @Test
    public void basicTest() throws Exception {
        doReturn("TestPathArgument").when(TestUtils.PATH_ARGUMENT).toString();
        assertEquals(this, this.childStrategy());

        doNothing().when(TestUtils.DOM_DATA_TREE_WRITE_CURSOR).enter(TestUtils.PATH_ARGUMENT);
        this.enter(TestUtils.PATH_ARGUMENT);
        verify(TestUtils.DOM_DATA_TREE_WRITE_CURSOR).enter(TestUtils.PATH_ARGUMENT);

        doNothing().when(TestUtils.DOM_DATA_TREE_WRITE_CURSOR).delete(TestUtils.PATH_ARGUMENT);
        this.delete(TestUtils.PATH_ARGUMENT);
        verify(TestUtils.DOM_DATA_TREE_WRITE_CURSOR).delete(TestUtils.PATH_ARGUMENT);

        doNothing().when(TestUtils.DOM_DATA_TREE_WRITE_CURSOR)
                .merge(TestUtils.PATH_ARGUMENT, TestUtils.NORMALIZED_NODE);
        this.merge(TestUtils.PATH_ARGUMENT, TestUtils.NORMALIZED_NODE);
        verify(TestUtils.DOM_DATA_TREE_WRITE_CURSOR).merge(TestUtils.PATH_ARGUMENT, TestUtils.NORMALIZED_NODE);

        doReturn(ImmutableList.of(TestUtils.NORMALIZED_NODE)).when(TestUtils.NORMALIZED_NODE_CONTAINER).getValue();
        doReturn(TestUtils.PATH_ARGUMENT).when(TestUtils.NORMALIZED_NODE).getIdentifier();
        this.mergeToCurrent(TestUtils.NORMALIZED_NODE_CONTAINER);
        verify(TestUtils.DOM_DATA_TREE_WRITE_CURSOR, times(2))
                .merge(TestUtils.PATH_ARGUMENT, TestUtils.NORMALIZED_NODE);

        doNothing().when(TestUtils.DOM_DATA_TREE_WRITE_CURSOR)
                .write(TestUtils.PATH_ARGUMENT, TestUtils.NORMALIZED_NODE);
        this.write(TestUtils.PATH_ARGUMENT, TestUtils.NORMALIZED_NODE);
        verify(TestUtils.DOM_DATA_TREE_WRITE_CURSOR).write(TestUtils.PATH_ARGUMENT, TestUtils.NORMALIZED_NODE);

        this.writeToCurrent(TestUtils.NORMALIZED_NODE_CONTAINER);
        verify(TestUtils.DOM_DATA_TREE_WRITE_CURSOR, times(2))
                .write(TestUtils.PATH_ARGUMENT, TestUtils.NORMALIZED_NODE);

        doNothing().when(TestUtils.DOM_DATA_TREE_WRITE_CURSOR).exit();
        this.exit();
        verify(TestUtils.DOM_DATA_TREE_WRITE_CURSOR).exit();
    }

    @Override
    protected DOMDataTreeWriteCursor delegate() {
        return TestUtils.DOM_DATA_TREE_WRITE_CURSOR;
    }

    @After
    public void reset() {
        TestUtils.resetMocks();
    }
}