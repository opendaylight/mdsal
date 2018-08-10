/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.DOM_DATA_TREE_IDENTIFIER;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.resetMocks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.schema.tree.CursorAwareDataTreeSnapshot;

public class InMemoryDOMDataTreeShardProducerTest {

    @Test
    public void basicTest() throws Exception {
        final InMemoryDOMDataTreeShard inMemoryDOMDataTreeShard = mock(InMemoryDOMDataTreeShard.class);
        final InmemoryDOMDataTreeShardWriteTransaction inmemoryDOMDataTreeShardWriteTransaction =
                mock(InmemoryDOMDataTreeShardWriteTransaction.class);
        final CursorAwareDataTreeSnapshot snapshot = mock(CursorAwareDataTreeSnapshot.class);
        doReturn(snapshot).when(inMemoryDOMDataTreeShard).takeSnapshot();

        doReturn(inmemoryDOMDataTreeShardWriteTransaction).when(inMemoryDOMDataTreeShard)
                .createTransaction(any(String.class), any(InMemoryDOMDataTreeShardProducer.class),
                        any(CursorAwareDataTreeSnapshot.class));

        final InMemoryDOMDataTreeShardProducer inMemoryDOMDataTreeShardProducer =
                new InMemoryDOMDataTreeShardProducer(inMemoryDOMDataTreeShard,
                        ImmutableSet.of(DOM_DATA_TREE_IDENTIFIER),
                        new InMemoryShardDataModificationFactory(DOM_DATA_TREE_IDENTIFIER, ImmutableMap.of(),
                                ImmutableMap.of()));

        assertNotNull(inMemoryDOMDataTreeShardProducer.createTransaction());
        verify(inMemoryDOMDataTreeShard).createTransaction(
                any(String.class),
                any(InMemoryDOMDataTreeShardProducer.class),
                any(CursorAwareDataTreeSnapshot.class));
        resetMocks();
    }
}
