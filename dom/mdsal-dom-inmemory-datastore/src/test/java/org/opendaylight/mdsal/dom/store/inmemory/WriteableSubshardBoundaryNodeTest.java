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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import org.junit.Test;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

public class WriteableSubshardBoundaryNodeTest {
    private static final DOMDataTreeIdentifier DOM_DATA_TREE_IDENTIFIER =
            new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, YangInstanceIdentifier.EMPTY);
    private static final DOMDataTreeShardProducer DOM_DATA_TREE_SHARD_PRODUCER = mock(DOMDataTreeShardProducer.class);
    private static final DOMDataTreeWriteCursor DATA_TREE_WRITE_CURSOR = mock(DOMDataTreeWriteCursor.class);
    private static final ForeignShardModificationContext FOREIGN_SHARD_MODIFICATION_CONTEXT =
            new ForeignShardModificationContext(DOM_DATA_TREE_IDENTIFIER, DOM_DATA_TREE_SHARD_PRODUCER);

    @Test
    public void createOperation() throws Exception {
        final WriteableSubshardBoundaryNode writeableSubshardBoundaryNode =
                WriteableSubshardBoundaryNode.from(FOREIGN_SHARD_MODIFICATION_CONTEXT);
        final DOMDataTreeShardWriteTransaction domDataTreeShardWriteTransaction =
                mock(DOMDataTreeShardWriteTransaction.class);
        final DOMDataTreeWriteCursor domDataTreeWriteCursor = mock(DOMDataTreeWriteCursor.class);

        doNothing().when(DATA_TREE_WRITE_CURSOR).exit();
        doReturn(domDataTreeShardWriteTransaction).when(DOM_DATA_TREE_SHARD_PRODUCER).createTransaction();
        doReturn(domDataTreeWriteCursor).when(domDataTreeShardWriteTransaction).createCursor(DOM_DATA_TREE_IDENTIFIER);
        doNothing().when(domDataTreeWriteCursor).enter(DOM_DATA_TREE_IDENTIFIER.getRootIdentifier().getLastPathArgument());
        doNothing().when(domDataTreeWriteCursor).exit();

        writeableSubshardBoundaryNode.createOperation(DATA_TREE_WRITE_CURSOR).exit();
        verify(DATA_TREE_WRITE_CURSOR).exit();

        WriteCursorStrategy writeCursorStrategy = writeableSubshardBoundaryNode.createOperation(DATA_TREE_WRITE_CURSOR);
        assertNotNull(writeCursorStrategy);

        WriteCursorStrategy childWriteCursorStrategy =
                writeCursorStrategy.enter(DOM_DATA_TREE_IDENTIFIER.getRootIdentifier().getLastPathArgument());
        childWriteCursorStrategy.exit();
        verify(domDataTreeWriteCursor).exit();
    }

    @Test
    public void getChildrenWithSubshards() throws Exception {
        WriteableSubshardBoundaryNode writeableSubshardBoundaryNode =
                WriteableSubshardBoundaryNode.from(FOREIGN_SHARD_MODIFICATION_CONTEXT);

        assertNotNull(writeableSubshardBoundaryNode.getChildrenWithSubshards());
        assertSame(Collections.emptyMap(),writeableSubshardBoundaryNode.getChildrenWithSubshards());
        assertEquals(DOM_DATA_TREE_IDENTIFIER.getRootIdentifier().getLastPathArgument(),
                writeableSubshardBoundaryNode.getIdentifier());
        assertNull(writeableSubshardBoundaryNode.getChild(null));
    }
}