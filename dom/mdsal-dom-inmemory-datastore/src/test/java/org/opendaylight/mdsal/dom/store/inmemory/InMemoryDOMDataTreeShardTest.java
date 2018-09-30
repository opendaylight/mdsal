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
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.opendaylight.controller.md.sal.dom.store.impl.TestModel.createTestContext;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.DOM_DATA_TREE_IDENTIFIER;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.DOM_DATA_TREE_SHARD_PRODUCER;
import static org.opendaylight.mdsal.dom.store.inmemory.TestUtils.resetMocks;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Collection;
import org.junit.After;
import org.junit.Test;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.CursorAwareDataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.CursorAwareDataTreeSnapshot;

public class InMemoryDOMDataTreeShardTest {

    @Test
    public void basicTest() {
        final InMemoryDOMDataTreeShard inMemoryDOMDataTreeShard =
                InMemoryDOMDataTreeShard.create(DOM_DATA_TREE_IDENTIFIER,
                        MoreExecutors.directExecutor(), 1);

        final DOMDataTreeIdentifier domDataTreeIdentifier =
                new DOMDataTreeIdentifier(LogicalDatastoreType.CONFIGURATION,
                        YangInstanceIdentifier.of(QName.create("", "Test")));

        final InMemoryDOMDataTreeShard domDataTreeShard = mock(InMemoryDOMDataTreeShard.class);
        doReturn("testReadableWriteableDOMDataTreeShard").when(domDataTreeShard).toString();
        doReturn(DOM_DATA_TREE_SHARD_PRODUCER).when(domDataTreeShard).createProducer(any());
        doReturn(domDataTreeShard).when(DOM_DATA_TREE_SHARD_PRODUCER).getParentShard();
        doNothing().when(DOM_DATA_TREE_SHARD_PRODUCER).close();

        assertFalse(inMemoryDOMDataTreeShard.getChildShards().containsValue(domDataTreeShard));
        inMemoryDOMDataTreeShard.onChildAttached(DOM_DATA_TREE_IDENTIFIER, domDataTreeShard);
        assertTrue(inMemoryDOMDataTreeShard.getChildShards().containsValue(domDataTreeShard));
        inMemoryDOMDataTreeShard.onChildAttached(domDataTreeIdentifier, domDataTreeShard);

        final Collection<DOMDataTreeIdentifier> prefixes = ImmutableList.of(DOM_DATA_TREE_IDENTIFIER);
        assertEquals(prefixes.toString(), inMemoryDOMDataTreeShard.createProducer(prefixes).getPrefixes().toString());

        final InMemoryDOMDataTreeShardProducer mockProducer = mock(InMemoryDOMDataTreeShardProducer.class);
        doReturn(prefixes).when(mockProducer).getPrefixes();
        doReturn(inMemoryDOMDataTreeShard.createModificationFactory(prefixes))
                .when(mockProducer).getModificationFactory();

        inMemoryDOMDataTreeShard.onGlobalContextUpdated(createTestContext());
        inMemoryDOMDataTreeShard.createTransaction("", mockProducer, mock(CursorAwareDataTreeSnapshot.class));

        final DOMDataTreeChangeListener domDataTreeChangeListener = mock(DOMDataTreeChangeListener.class);
        final ListenerRegistration<?> listenerRegistration = mock(ListenerRegistration.class);
        doReturn(listenerRegistration).when(domDataTreeShard).registerTreeChangeListener(any(), any());
        doNothing().when(domDataTreeChangeListener).onDataTreeChanged(any());
        inMemoryDOMDataTreeShard.registerTreeChangeListener(YangInstanceIdentifier.EMPTY, domDataTreeChangeListener);
        verify(domDataTreeShard, atLeastOnce()).registerTreeChangeListener(any(), any());

        inMemoryDOMDataTreeShard.onChildDetached(DOM_DATA_TREE_IDENTIFIER, domDataTreeShard);
        assertFalse(inMemoryDOMDataTreeShard.getChildShards().containsKey(DOM_DATA_TREE_IDENTIFIER));
    }

    @Test
    public void createTransactionWithException() {
        final DOMDataTreeIdentifier domDataTreeIdentifier =
                new DOMDataTreeIdentifier(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.EMPTY);

        final InMemoryDOMDataTreeShard inMemoryDOMDataTreeShard =
                InMemoryDOMDataTreeShard.create(domDataTreeIdentifier,
                        MoreExecutors.newDirectExecutorService(), 1);
        final CursorAwareDataTreeModification dataTreeModification = mock(CursorAwareDataTreeModification.class);

        final InmemoryDOMDataTreeShardWriteTransaction inmemoryDOMDataTreeShardWriteTransaction =
                mock(InmemoryDOMDataTreeShardWriteTransaction.class);
        doReturn(dataTreeModification).when(inmemoryDOMDataTreeShardWriteTransaction).getRootModification();
        final Collection<DOMDataTreeIdentifier> prefixes = ImmutableList.of(DOM_DATA_TREE_IDENTIFIER);
        final InMemoryDOMDataTreeShardProducer mockProducer = mock(InMemoryDOMDataTreeShardProducer.class);
        doReturn(prefixes).when(mockProducer).getPrefixes();
        doReturn(inMemoryDOMDataTreeShard.createModificationFactory(prefixes))
            .when(mockProducer).getModificationFactory();

        inMemoryDOMDataTreeShard.createTransaction("", mockProducer, mock(CursorAwareDataTreeSnapshot.class));
    }

    @After
    public void reset() {
        resetMocks();
    }
}