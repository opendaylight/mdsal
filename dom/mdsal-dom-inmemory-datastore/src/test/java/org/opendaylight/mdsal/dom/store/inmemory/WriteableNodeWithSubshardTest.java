/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.NODE_IDENTIFIER;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.WRITEABLE_MODIFICATION_NODE;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.resetMocks;

import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Test;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

public class WriteableNodeWithSubshardTest {

    @Test
    public void basicTest() throws Exception {
        doReturn("test").when(WRITEABLE_MODIFICATION_NODE).toString();
        doNothing().when(WRITEABLE_MODIFICATION_NODE).markDeleted();

        final Map<PathArgument, WriteableModificationNode> children = new HashMap<>();
        children.put(NODE_IDENTIFIER, WRITEABLE_MODIFICATION_NODE);

        final WriteableNodeWithSubshard writeableNodeWithSubshard = new WriteableNodeWithSubshardImpl(children);

        assertEquals(writeableNodeWithSubshard.getChildrenWithSubshards(), children);

        final WriteableModificationNode TestWriteableModificationNode =
                writeableNodeWithSubshard.getChild(NODE_IDENTIFIER);
        assertNotNull(TestWriteableModificationNode);
        assertEquals(TestWriteableModificationNode, WRITEABLE_MODIFICATION_NODE);

        writeableNodeWithSubshard.markDeleted();
        verify(WRITEABLE_MODIFICATION_NODE).markDeleted();
    }

    @After
    public void reset() {
        resetMocks();
    }

    private class WriteableNodeWithSubshardImpl extends WriteableNodeWithSubshard {

        private WriteableNodeWithSubshardImpl(Map<PathArgument, WriteableModificationNode> children) {
            super(children);
        }

        @Override
        WriteCursorStrategy createOperation(DOMDataTreeWriteCursor parentCursor) {
            return null;
        }

        @Override
        public PathArgument getIdentifier() {
            return null;
        }
    }
}