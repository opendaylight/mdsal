/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.shard;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Test;

public class ForeignShardModificationContextTest {

    @Test
    public void basicTest() throws Exception {
        final ForeignShardModificationContext foreignShardModificationContext =
                new ForeignShardModificationContext(TestUtils.DOM_DATA_TREE_IDENTIFIER,
                        TestUtils.DOM_DATA_TREE_SHARD_PRODUCER);
        doReturn("testTransaction").when(TestUtils.DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).toString();
        assertFalse(foreignShardModificationContext.isModified());
        doReturn(TestUtils.DOM_DATA_TREE_SHARD_WRITE_TRANSACTION)
                .when(TestUtils.DOM_DATA_TREE_SHARD_PRODUCER).createTransaction();
        doReturn(TestUtils.DOM_DATA_TREE_WRITE_CURSOR)
                .when(TestUtils.DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).createCursor(TestUtils.DOM_DATA_TREE_IDENTIFIER);
        foreignShardModificationContext.getCursor();
        verify(TestUtils.DOM_DATA_TREE_SHARD_PRODUCER).createTransaction();
        verify(TestUtils.DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).createCursor(TestUtils.DOM_DATA_TREE_IDENTIFIER);
        assertTrue(foreignShardModificationContext.isModified());

        doNothing().when(TestUtils.DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).ready();
        doNothing().when(TestUtils.DOM_DATA_TREE_WRITE_CURSOR).close();
        foreignShardModificationContext.ready();
        verify(TestUtils.DOM_DATA_TREE_WRITE_CURSOR, only()).close();
        verify(TestUtils.DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).ready();

        doReturn(null).when(TestUtils.DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).validate();
        foreignShardModificationContext.validate();
        verify(TestUtils.DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).validate();

        doReturn(null).when(TestUtils.DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).prepare();
        foreignShardModificationContext.prepare();
        verify(TestUtils.DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).prepare();

        doReturn(null).when(TestUtils.DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).commit();
        foreignShardModificationContext.submit();
        verify(TestUtils.DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).commit();
    }

    @Test
    public void basicTestClose() throws Exception {
        final ForeignShardModificationContext foreignShardModificationContext =
                new ForeignShardModificationContext(TestUtils.DOM_DATA_TREE_IDENTIFIER,
                        TestUtils.DOM_DATA_TREE_SHARD_PRODUCER);
        doReturn(TestUtils.DOM_DATA_TREE_SHARD_WRITE_TRANSACTION)
                .when(TestUtils.DOM_DATA_TREE_SHARD_PRODUCER).createTransaction();
        doReturn(TestUtils.DOM_DATA_TREE_WRITE_CURSOR)
                .when(TestUtils.DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).createCursor(TestUtils.DOM_DATA_TREE_IDENTIFIER);
        doNothing().when(TestUtils.DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).close();
        doNothing().when(TestUtils.DOM_DATA_TREE_WRITE_CURSOR).close();
        foreignShardModificationContext.getCursor();
        foreignShardModificationContext.closeForeignTransaction();
        verify(TestUtils.DOM_DATA_TREE_WRITE_CURSOR).close();
        verify(TestUtils.DOM_DATA_TREE_SHARD_WRITE_TRANSACTION).close();
    }

    @After
    public void reset() {
        TestUtils.resetMocks();
    }
}
