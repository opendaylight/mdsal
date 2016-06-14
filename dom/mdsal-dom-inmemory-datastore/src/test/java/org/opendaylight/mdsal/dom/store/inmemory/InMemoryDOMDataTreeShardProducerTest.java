/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.DOM_DATA_TREE_IDENTIFIER;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.resetMocks;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;

public class InMemoryDOMDataTreeShardProducerTest {

    @Test
    public void basicTest() throws Exception {
        final InMemoryDOMDataTreeShard inMemoryDOMDataTreeShard = mock(InMemoryDOMDataTreeShard.class);
        final InmemoryDOMDataTreeShardWriteTransaction inmemoryDOMDataTreeShardWriteTransaction =
                mock(InmemoryDOMDataTreeShardWriteTransaction.class);
        doReturn(inmemoryDOMDataTreeShardWriteTransaction).when(inMemoryDOMDataTreeShard)
                .createTransaction(anyCollectionOf((DOMDataTreeIdentifier.class)));

        final InMemoryDOMDataTreeShardProducer inMemoryDOMDataTreeShardProducer =
                new InMemoryDOMDataTreeShardProducer(inMemoryDOMDataTreeShard,
                        ImmutableSet.of(DOM_DATA_TREE_IDENTIFIER));

        assertNotNull(inMemoryDOMDataTreeShardProducer.createTransaction());
        verify(inMemoryDOMDataTreeShard).createTransaction(anyCollectionOf(DOMDataTreeIdentifier.class));
        resetMocks();
    }
}