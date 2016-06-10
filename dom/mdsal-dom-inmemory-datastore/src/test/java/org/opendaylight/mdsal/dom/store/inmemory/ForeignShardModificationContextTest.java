/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.DOM_DATA_TREE_IDENTIFIER;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.DOM_DATA_TREE_SHARD_PRODUCER;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.DOM_DATA_TREE_SHARD_WRITE_TRANSACTION;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.DOM_DATA_TREE_WRITE_CURSOR;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.resetMocks;

import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

public class ForeignShardModificationContextTest {

    @Test
    public void basicTest() throws Exception {
        final ForeignShardModificationContext foreignShardModificationContext =
                new ForeignShardModificationContext(DOM_DATA_TREE_IDENTIFIER, DOM_DATA_TREE_SHARD_PRODUCER);
        doReturn("testTransaction").when(DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).toString();
        assertFalse(foreignShardModificationContext.isModified());
        doReturn(DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).when(DOM_DATA_TREE_SHARD_PRODUCER).createTransaction();
        doReturn(DOM_DATA_TREE_WRITE_CURSOR)
                .when(DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).createCursor(DOM_DATA_TREE_IDENTIFIER);
        foreignShardModificationContext.getCursor();
        verify(DOM_DATA_TREE_SHARD_PRODUCER).createTransaction();
        verify(DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).createCursor(DOM_DATA_TREE_IDENTIFIER);
        assertTrue(foreignShardModificationContext.isModified());

        doNothing().when(DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).ready();
        doNothing().when(DOM_DATA_TREE_WRITE_CURSOR).close();
        foreignShardModificationContext.ready();
        verify(DOM_DATA_TREE_WRITE_CURSOR, only()).close();
        verify(DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).ready();

        doReturn(null).when(DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).validate();
        foreignShardModificationContext.validate();
        verify(DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).validate();

        doReturn(null).when(DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).prepare();
        foreignShardModificationContext.prepare();
        verify(DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).prepare();

        doReturn(null).when(DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).commit();
        foreignShardModificationContext.submit();
        verify(DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).commit();

        Mockito.reset(DOM_DATA_TREE_WRITE_CURSOR);
        doNothing().when(DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).close();
        doNothing().when(DOM_DATA_TREE_WRITE_CURSOR).close();
        foreignShardModificationContext.getCursor();
        foreignShardModificationContext.closeForeignTransaction();
        verify(DOM_DATA_TREE_WRITE_CURSOR).close();
        verify(DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).close();
    }

    @After
    public void reset() {
        resetMocks();
    }
}