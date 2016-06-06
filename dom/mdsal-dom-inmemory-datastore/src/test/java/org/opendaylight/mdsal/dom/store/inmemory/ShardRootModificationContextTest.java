/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.CursorAwareDataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.CursorAwareDataTreeSnapshot;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModificationCursor;

public class ShardRootModificationContextTest implements TestUtils{

    @Test
    public void basicTest() throws Exception {
        final CursorAwareDataTreeSnapshot cursorAwareDataTreeSnapshot = mock(CursorAwareDataTreeSnapshot.class);
        final CursorAwareDataTreeModification cursorAwareDataTreeModification =
                mock(CursorAwareDataTreeModification.class);
        final DataTreeModificationCursor dataTreeModificationCursor = mock(DataTreeModificationCursor.class);
        doReturn(cursorAwareDataTreeModification).when(cursorAwareDataTreeSnapshot).newModification();
        doNothing().when(cursorAwareDataTreeModification).ready();
        doReturn(dataTreeModificationCursor)
                .when(cursorAwareDataTreeModification).createCursor(YangInstanceIdentifier.EMPTY);
        doNothing().when(dataTreeModificationCursor).close();

        final ShardRootModificationContext shardRootModificationContext =
                new ShardRootModificationContext(DOM_DATA_TREE_IDENTIFIER, cursorAwareDataTreeSnapshot);
        assertEquals(DOM_DATA_TREE_IDENTIFIER, shardRootModificationContext.getIdentifier());
        assertFalse(shardRootModificationContext.isModified());

        final DataTreeModificationCursorAdaptor dataTreeModificationCursorAdaptor =
                shardRootModificationContext.cursor();
        assertNotNull(dataTreeModificationCursorAdaptor);
        assertTrue(shardRootModificationContext.isModified());
        verify(cursorAwareDataTreeSnapshot).newModification();
        verify(cursorAwareDataTreeModification).createCursor(YangInstanceIdentifier.EMPTY);

        shardRootModificationContext.ready();
        verify(cursorAwareDataTreeModification).ready();
        verify(dataTreeModificationCursor).close();
    }
}