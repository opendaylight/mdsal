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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModificationCursor;

public class DataTreeModificationCursorAdaptorTest implements TestUtils {

    @Test
    public void basicTest() throws Exception {
        final DataTreeModificationCursor dataTreeModificationCursor = mock(DataTreeModificationCursor.class);
        final DataTreeModificationCursorAdaptor dataTreeModificationCursorAdaptor =
                DataTreeModificationCursorAdaptor.of(dataTreeModificationCursor);
        final Iterable iterable = mock(Iterable.class);
        doReturn("test").when(PATH_ARGUMENT).toString();

        assertEquals(dataTreeModificationCursorAdaptor.delegate(), dataTreeModificationCursor);

        doNothing().when(dataTreeModificationCursor).delete(PATH_ARGUMENT);
        dataTreeModificationCursorAdaptor.delete(PATH_ARGUMENT);
        verify(dataTreeModificationCursor).delete(PATH_ARGUMENT);

        doNothing().when(dataTreeModificationCursor).enter(PATH_ARGUMENT);
        dataTreeModificationCursorAdaptor.enter(PATH_ARGUMENT);
        verify(dataTreeModificationCursor).enter(PATH_ARGUMENT);

        doNothing().when(dataTreeModificationCursor).enter(PATH_ARGUMENT, PATH_ARGUMENT);
        dataTreeModificationCursorAdaptor.enter(PATH_ARGUMENT, PATH_ARGUMENT);
        verify(dataTreeModificationCursor).enter(PATH_ARGUMENT, PATH_ARGUMENT);

        doNothing().when(dataTreeModificationCursor).enter(iterable);
        dataTreeModificationCursorAdaptor.enter(iterable);
        verify(dataTreeModificationCursor).enter(iterable);

        doNothing().when(dataTreeModificationCursor).merge(PATH_ARGUMENT, null);
        dataTreeModificationCursorAdaptor.merge(PATH_ARGUMENT,null);
        verify(dataTreeModificationCursor).merge(PATH_ARGUMENT, null);

        doNothing().when(dataTreeModificationCursor).write(PATH_ARGUMENT, null);
        dataTreeModificationCursorAdaptor.write(PATH_ARGUMENT, null);
        verify(dataTreeModificationCursor).write(PATH_ARGUMENT, null);

        doNothing().when(dataTreeModificationCursor).exit(1);
        dataTreeModificationCursorAdaptor.exit(1);
        verify(dataTreeModificationCursor).exit(1);

        doNothing().when(dataTreeModificationCursor).exit();
        dataTreeModificationCursorAdaptor.exit();
        verify(dataTreeModificationCursor).exit();

        doReturn(null).when(dataTreeModificationCursor).readNode(PATH_ARGUMENT);
        dataTreeModificationCursorAdaptor.readNode(PATH_ARGUMENT);
        verify(dataTreeModificationCursor).readNode(PATH_ARGUMENT);

        doNothing().when(dataTreeModificationCursor).close();
        dataTreeModificationCursorAdaptor.close();
        verify(dataTreeModificationCursor).close();
    }

    @After
    public void resetMocks() {
        TestUtils.resetMocks();
    }
}