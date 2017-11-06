/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.shard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.junit.After;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeSnapshotCursor;

public class DelegatingReadableCursorOperationTest extends DelegatingReadableCursorOperation {

    private static final DataTreeSnapshotCursor MOCK_CURSOR_SNAPSHOT = mock(DataTreeSnapshotCursor.class);

    @Test
    public void basicTest() throws Exception {
        final Optional<NormalizedNode<?, ?>> nodeOptional = Optional.empty();
        doReturn(nodeOptional).when(MOCK_CURSOR_SNAPSHOT).readNode(TestUtils.PATH_ARGUMENT);
        doNothing().when(MOCK_CURSOR_SNAPSHOT).exit();
        doNothing().when(MOCK_CURSOR_SNAPSHOT).enter(TestUtils.PATH_ARGUMENT);
        doReturn("test").when(TestUtils.PATH_ARGUMENT).toString();

        assertFalse(readNode(TestUtils.PATH_ARGUMENT).isPresent());
        verify(MOCK_CURSOR_SNAPSHOT).readNode(TestUtils.PATH_ARGUMENT);

        exit();
        verify(MOCK_CURSOR_SNAPSHOT).exit();

        assertEquals(this, enter(TestUtils.PATH_ARGUMENT));
        verify(MOCK_CURSOR_SNAPSHOT).enter(TestUtils.PATH_ARGUMENT);
    }

    @Override
    protected DataTreeSnapshotCursor delegate() {
        return MOCK_CURSOR_SNAPSHOT;
    }

    @After
    public void reset() {
        TestUtils.resetMocks();
    }
}