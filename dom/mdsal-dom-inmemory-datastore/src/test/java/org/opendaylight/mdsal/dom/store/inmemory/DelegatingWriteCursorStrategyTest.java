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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableList;
import org.junit.After;
import org.junit.Test;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;

public class DelegatingWriteCursorStrategyTest extends DelegatingWriteCursorStrategy implements TestUtils {

    @Test
    public void basicTest() throws Exception {
        doReturn("TestPathArgument").when(PATH_ARGUMENT).toString();
        assertEquals(this, this.childStrategy());

        doNothing().when(DOM_DATA_TREE_WRITE_CURSOR).enter(PATH_ARGUMENT);
        this.enter(PATH_ARGUMENT);
        verify(DOM_DATA_TREE_WRITE_CURSOR).enter(PATH_ARGUMENT);

        doNothing().when(DOM_DATA_TREE_WRITE_CURSOR).delete(PATH_ARGUMENT);
        this.delete(PATH_ARGUMENT);
        verify(DOM_DATA_TREE_WRITE_CURSOR).delete(PATH_ARGUMENT);

        doNothing().when(DOM_DATA_TREE_WRITE_CURSOR).merge(PATH_ARGUMENT, NORMALIZED_NODE);
        this.merge(PATH_ARGUMENT, NORMALIZED_NODE);
        verify(DOM_DATA_TREE_WRITE_CURSOR).merge(PATH_ARGUMENT, NORMALIZED_NODE);

        doReturn(ImmutableList.of(NORMALIZED_NODE)).when(NORMALIZED_NODE_CONTAINER).getValue();
        doReturn(PATH_ARGUMENT).when(NORMALIZED_NODE).getIdentifier();
        this.mergeToCurrent(NORMALIZED_NODE_CONTAINER);
        verify(DOM_DATA_TREE_WRITE_CURSOR, times(2)).merge(PATH_ARGUMENT, NORMALIZED_NODE);

        doNothing().when(DOM_DATA_TREE_WRITE_CURSOR).write(PATH_ARGUMENT, NORMALIZED_NODE);
        this.write(PATH_ARGUMENT, NORMALIZED_NODE);
        verify(DOM_DATA_TREE_WRITE_CURSOR).write(PATH_ARGUMENT, NORMALIZED_NODE);

        this.writeToCurrent(NORMALIZED_NODE_CONTAINER);
        verify(DOM_DATA_TREE_WRITE_CURSOR, times(2)).write(PATH_ARGUMENT, NORMALIZED_NODE);

        doNothing().when(DOM_DATA_TREE_WRITE_CURSOR).exit();
        this.exit();
        verify(DOM_DATA_TREE_WRITE_CURSOR).exit();
    }

    @Override
    protected DOMDataTreeWriteCursor delegate() {
        return DOM_DATA_TREE_WRITE_CURSOR;
    }

    @After
    public void resetMocks() {
        TestUtils.resetMocks();
    }
}