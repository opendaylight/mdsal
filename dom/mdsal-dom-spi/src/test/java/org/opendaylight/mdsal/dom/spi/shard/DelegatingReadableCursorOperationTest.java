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
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeSnapshotCursor;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DelegatingReadableCursorOperationTest {
    @Mock
    public DataTreeSnapshotCursor mockCursorSnapshot;

    @Test
    public void basicTest() throws Exception {
        doReturn(Optional.empty()).when(mockCursorSnapshot).readNode(TestUtils.PATH_ARGUMENT);
        doNothing().when(mockCursorSnapshot).exit();
        doNothing().when(mockCursorSnapshot).enter(TestUtils.PATH_ARGUMENT);
        doReturn("test").when(TestUtils.PATH_ARGUMENT).toString();

        final var op = new DelegatingReadableCursorOperation() {
            @Override
            protected DataTreeSnapshotCursor delegate() {
                return mockCursorSnapshot;
            }
        };

        assertFalse(op.readNode(TestUtils.PATH_ARGUMENT).isPresent());
        verify(mockCursorSnapshot).readNode(TestUtils.PATH_ARGUMENT);

        op.exit();
        verify(mockCursorSnapshot).exit();

        assertEquals(op, op.enter(TestUtils.PATH_ARGUMENT));
        verify(mockCursorSnapshot).enter(TestUtils.PATH_ARGUMENT);
    }

    @After
    public void reset() {
        TestUtils.resetMocks();
    }
}