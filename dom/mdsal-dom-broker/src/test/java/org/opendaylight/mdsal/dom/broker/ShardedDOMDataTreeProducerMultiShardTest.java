/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.Futures;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.MockitoAnnotations;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducer;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.mdsal.dom.broker.util.TestModel;
import org.opendaylight.mdsal.dom.spi.shard.DOMDataTreeShardProducer;
import org.opendaylight.mdsal.dom.spi.shard.DOMDataTreeShardWriteTransaction;
import org.opendaylight.mdsal.dom.spi.shard.WriteableDOMDataTreeShard;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMDataTreeShard;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeAttrBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class ShardedDOMDataTreeProducerMultiShardTest {

    private static final SchemaContext SCHEMA_CONTEXT = TestModel.createTestContext();

    private static final DOMDataTreeIdentifier ROOT_ID =
            new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, YangInstanceIdentifier.EMPTY);
    private static final DOMDataTreeIdentifier TEST_ID =
            new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, TestModel.TEST_PATH);

    private static final DOMDataTreeIdentifier TEST2_ID =
            new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, TestModel.TEST2_PATH);

    private static final DOMDataTreeIdentifier INNER_CONTAINER_ID =
            new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, TestModel.INNER_CONTAINER_PATH);
    private static final DOMDataTreeIdentifier ANOTHER_SHARD_ID =
            new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, TestModel.ANOTHER_SHARD_PATH);

    private InMemoryDOMDataTreeShard rootShard;
    private InMemoryDOMDataTreeShard anotherInnerShard;

    private ShardedDOMDataTree dataTreeService;
    private ListenerRegistration<InMemoryDOMDataTreeShard> rootShardReg;
    private ListenerRegistration<InMemoryDOMDataTreeShard> innerShardReg;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Captor
    private ArgumentCaptor<Collection<DataTreeCandidate>> captorForChanges;
    @Captor
    private ArgumentCaptor<Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>>> captorForSubtrees;

    private final ContainerNode crossShardContainer = createCrossShardContainer();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        rootShard = InMemoryDOMDataTreeShard.create(ROOT_ID, executor, 1);
        rootShard.onGlobalContextUpdated(SCHEMA_CONTEXT);

        final ShardedDOMDataTree dataTree = new ShardedDOMDataTree();
        final DOMDataTreeProducer shardRegProducer = dataTree.createProducer(Collections.singletonList(ROOT_ID));
        rootShardReg = dataTree.registerDataTreeShard(ROOT_ID, rootShard, shardRegProducer);
        shardRegProducer.close();

        dataTreeService = dataTree;
    }

    @Test(expected = IllegalStateException.class)
    public void testTxReadyMultiples() throws Exception {
        final DOMDataTreeShardProducer producer = rootShard.createProducer(Collections.singletonList(TEST_ID));
        final DOMDataTreeShardWriteTransaction transaction = producer.createTransaction();
        final DOMDataTreeWriteCursor cursor = transaction.createCursor(ROOT_ID);

        final ContainerNode testContainer = ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
                .build();
        cursor.write(TestModel.TEST_PATH.getLastPathArgument(), testContainer);
        transaction.ready();

        transaction.ready();
    }

    @Test(expected = IllegalStateException.class)
    public void testSubmitUnclosedCursor() throws Exception {
        final DOMDataTreeShardProducer producer = rootShard.createProducer(Collections.singletonList(TEST_ID));
        final DOMDataTreeShardWriteTransaction transaction = producer.createTransaction();
        final DOMDataTreeWriteCursor cursor = transaction.createCursor(ROOT_ID);

        final ContainerNode testContainer = ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
                .build();

        cursor.write(TestModel.TEST_PATH.getLastPathArgument(), testContainer);
        transaction.ready();
    }

    @Test
    public void testMultipleCursorsFromOneTx() throws Exception {
        final DOMDataTreeListener mockedDataTreeListener = mock(DOMDataTreeListener.class);
        doNothing().when(mockedDataTreeListener).onDataTreeChanged(anyCollection(), anyMap());

        dataTreeService.registerListener(mockedDataTreeListener, Collections.singletonList(INNER_CONTAINER_ID),
                true, Collections.emptyList());

        final DOMDataTreeShardProducer producer = rootShard.createProducer(Collections.singletonList(TEST_ID));
        final DOMDataTreeShardWriteTransaction transaction = producer.createTransaction();
        final DOMDataTreeWriteCursor cursor = transaction.createCursor(ROOT_ID);

        final ContainerNode testContainer = ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
                .build();

        cursor.write(TestModel.TEST_PATH.getLastPathArgument(), testContainer);
        cursor.close();

        final DOMDataTreeWriteCursor newCursor = transaction.createCursor(ROOT_ID);
        newCursor.enter(TestModel.TEST_PATH.getLastPathArgument());
        final ContainerNode innerContainer = ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new NodeIdentifier(TestModel.INNER_CONTAINER))
                .withChild(ImmutableLeafNodeBuilder.<String>create()
                        .withNodeIdentifier(new NodeIdentifier(TestModel.SHARDED_VALUE_1))
                        .withValue("inner-value")
                        .build())
                .build();

        newCursor.write(TestModel.INNER_CONTAINER_PATH.getLastPathArgument(), innerContainer);
        newCursor.close();
        transaction.ready();
        transaction.submit();

        verify(mockedDataTreeListener, timeout(1000).times(2)).onDataTreeChanged(
                captorForChanges.capture(), captorForSubtrees.capture());
        final Collection<DataTreeCandidate> capturedValue = captorForChanges.getValue();
        assertTrue(capturedValue.size() == 1);

        final ContainerNode dataAfter =
                (ContainerNode) capturedValue.iterator().next().getRootNode().getDataAfter().get();
        assertEquals(innerContainer, dataAfter);
        verifyNoMoreInteractions(mockedDataTreeListener);
    }

    @Test
    public void testSingleShardListener() throws Exception {
        final DOMDataTreeListener mockedDataTreeListener = mock(DOMDataTreeListener.class);
        doNothing().when(mockedDataTreeListener).onDataTreeChanged(anyCollection(), anyMap());

        dataTreeService.registerListener(mockedDataTreeListener, Collections.singletonList(INNER_CONTAINER_ID), true,
                Collections.emptyList());

        final DOMDataTreeShardProducer producer = rootShard.createProducer(Collections.singletonList(TEST_ID));
        final DOMDataTreeShardWriteTransaction transaction = producer.createTransaction();
        writeCrossShardContainer(transaction);

        verify(mockedDataTreeListener, timeout(1000).times(2)).onDataTreeChanged(
                captorForChanges.capture(), captorForSubtrees.capture());
        final Collection<DataTreeCandidate> capturedValue = captorForChanges.getValue();
        assertTrue(capturedValue.size() == 1);

        final ContainerNode dataAfter =
                (ContainerNode) capturedValue.iterator().next().getRootNode().getDataAfter().get();
        assertEquals(crossShardContainer.getChild(
                TestModel.INNER_CONTAINER_PATH.getLastPathArgument()).get(), dataAfter);

        final Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>> capturedSubtrees = captorForSubtrees.getValue();
        assertTrue(capturedSubtrees.size() == 1);
        assertTrue(capturedSubtrees.containsKey(INNER_CONTAINER_ID));
        assertEquals(crossShardContainer.getChild(TestModel.INNER_CONTAINER_PATH.getLastPathArgument()).get(),
                capturedSubtrees.get(INNER_CONTAINER_ID));

        verifyNoMoreInteractions(mockedDataTreeListener);
    }

    @Test
    public void testMultipleShards() throws Exception {
        final DOMDataTreeListener mockedDataTreeListener = mock(DOMDataTreeListener.class);
        doNothing().when(mockedDataTreeListener).onDataTreeChanged(anyCollection(), anyMap());

        final InMemoryDOMDataTreeShard innerShard = InMemoryDOMDataTreeShard.create(INNER_CONTAINER_ID, executor, 1);
        innerShard.onGlobalContextUpdated(SCHEMA_CONTEXT);
        final DOMDataTreeProducer shardRegProducer =
                dataTreeService.createProducer(Collections.singletonList(INNER_CONTAINER_ID));
        innerShardReg = dataTreeService.registerDataTreeShard(INNER_CONTAINER_ID, innerShard, shardRegProducer);
        shardRegProducer.close();

        dataTreeService.registerListener(mockedDataTreeListener, Collections.singletonList(TEST_ID),
                true, Collections.emptyList());

        final DOMDataTreeShardProducer producer = rootShard.createProducer(Collections.singletonList(TEST_ID));
        final DOMDataTreeShardWriteTransaction transaction = producer.createTransaction();
        writeCrossShardContainer(transaction);

        final ContainerNode testContainerVerificationNode = ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
                .build();

        //verify listeners have been notified
        verify(mockedDataTreeListener, timeout(1000).times(4)).onDataTreeChanged(
                captorForChanges.capture(), captorForSubtrees.capture());
        final List<Collection<DataTreeCandidate>> capturedChanges = captorForChanges.getAllValues();
        final List<Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>>> capturedSubtrees =
                captorForSubtrees.getAllValues();
        final DataTreeCandidate firstNotificationCandidate = capturedChanges.get(2).iterator().next();

        assertTrue(capturedSubtrees.get(2).size() == 1);
        assertEquals(testContainerVerificationNode, firstNotificationCandidate.getRootNode().getDataAfter().get());
        assertEquals(testContainerVerificationNode, capturedSubtrees.get(2).get(TEST_ID));

        final DataTreeCandidate secondNotificationCandidate = capturedChanges.get(3).iterator().next();
        assertTrue(capturedSubtrees.get(3).size() == 1);
        assertEquals(crossShardContainer, secondNotificationCandidate.getRootNode().getDataAfter().get());
        assertEquals(crossShardContainer, capturedSubtrees.get(3).get(TEST_ID));

        verifyNoMoreInteractions(mockedDataTreeListener);
    }

    @Test
    public void testMultipleShardsProducerClose() throws Exception {
        final InMemoryDOMDataTreeShard innerShard = InMemoryDOMDataTreeShard.create(INNER_CONTAINER_ID, executor, 1);
        innerShard.onGlobalContextUpdated(SCHEMA_CONTEXT);

        assertTrue(rootShard.getProducers().isEmpty());

        final DOMDataTreeProducer innerShardRegProducer =
                dataTreeService.createProducer(Collections.singletonList(INNER_CONTAINER_ID));
        assertTrue(rootShard.getProducers().size() == 1);
        final DOMDataTreeShardProducer rootShardProducer = Iterables.getOnlyElement(rootShard.getProducers());
        assertEquals(rootShardProducer.getPrefixes().toString(),
                Collections.singletonList(INNER_CONTAINER_ID).toString());

        dataTreeService.registerDataTreeShard(INNER_CONTAINER_ID, innerShard, innerShardRegProducer);

        assertTrue(rootShard.getProducers().isEmpty());
        assertTrue(innerShard.getProducers().size() == 1);
        final DOMDataTreeShardProducer innerShardProducer = Iterables.getOnlyElement(innerShard.getProducers());
        assertEquals(innerShardProducer.getPrefixes().toString(),
                Collections.singletonList(INNER_CONTAINER_ID).toString());

        innerShardRegProducer.close();
        assertTrue(rootShard.getProducers().isEmpty());
        assertTrue(innerShard.getProducers().isEmpty());

        final DOMDataTreeProducer testProducer =
                dataTreeService.createProducer(Collections.singletonList(TEST_ID));
        assertTrue(rootShard.getProducers().size() == 1);
        final DOMDataTreeShardProducer rootShardProducer2 = Iterables.getOnlyElement(rootShard.getProducers());
        assertEquals(rootShardProducer2.getPrefixes().toString(),
                Collections.singletonList(TEST_ID).toString());

        assertTrue(innerShard.getProducers().size() == 1);
        final DOMDataTreeShardProducer innerShardProducer2 = Iterables.getOnlyElement(innerShard.getProducers());
        assertEquals(innerShardProducer2.getPrefixes().toString(),
                Collections.singletonList(INNER_CONTAINER_ID).toString());

        testProducer.close();
        assertTrue(rootShard.getProducers().isEmpty());
        assertTrue(innerShard.getProducers().isEmpty());
    }

    @Test
    public void testMultipleShardsChildProducerClose() throws Exception {
        final InMemoryDOMDataTreeShard innerShard = InMemoryDOMDataTreeShard.create(INNER_CONTAINER_ID, executor, 1);
        innerShard.onGlobalContextUpdated(SCHEMA_CONTEXT);

        final DOMDataTreeProducer innerShardRegProducer =
                dataTreeService.createProducer(Collections.singletonList(INNER_CONTAINER_ID));
        dataTreeService.registerDataTreeShard(INNER_CONTAINER_ID, innerShard, innerShardRegProducer);
        innerShardRegProducer.close();
        assertTrue(rootShard.getProducers().isEmpty());
        assertTrue(innerShard.getProducers().isEmpty());

        final DOMDataTreeProducer testProducer =
                dataTreeService.createProducer(Collections.singletonList(TEST_ID));
        final DOMDataTreeProducer testChildProducer = testProducer.createProducer(
                Collections.singletonList(INNER_CONTAINER_ID));
        assertTrue(rootShard.getProducers().size() == 1);
        assertTrue(innerShard.getProducers().size() == 2);

        final DOMDataTreeShardProducer rootShardProducer = Iterables.getOnlyElement(rootShard.getProducers());
        assertEquals(rootShardProducer.getPrefixes().toString(),
                Collections.singletonList(TEST_ID).toString());

        for (DOMDataTreeShardProducer producer : innerShard.getProducers()) {
            assertEquals(producer.getPrefixes().toString(),
                    Collections.singletonList(INNER_CONTAINER_ID).toString());
        }

        testProducer.close();
        assertTrue(rootShard.getProducers().isEmpty());
        assertTrue(innerShard.getProducers().size() == 1);
        final DOMDataTreeShardProducer innerShardProducer = Iterables.getOnlyElement(innerShard.getProducers());
        assertEquals(innerShardProducer.getPrefixes().toString(),
                Collections.singletonList(INNER_CONTAINER_ID).toString());

        testChildProducer.close();
        assertTrue(rootShard.getProducers().isEmpty());
        assertTrue(innerShard.getProducers().isEmpty());
    }

    @Test
    public void testMultipleShardsProducerCloseForSubshardAttached() throws Exception {
        final InMemoryDOMDataTreeShard innerShard = InMemoryDOMDataTreeShard.create(INNER_CONTAINER_ID, executor, 1);
        innerShard.onGlobalContextUpdated(SCHEMA_CONTEXT);

        final DOMDataTreeProducer innerShardRegProducer =
                dataTreeService.createProducer(Collections.singletonList(INNER_CONTAINER_ID));
        dataTreeService.registerDataTreeShard(INNER_CONTAINER_ID, innerShard, innerShardRegProducer);
        innerShardRegProducer.close();
        assertTrue(rootShard.getProducers().isEmpty());
        assertTrue(innerShard.getProducers().isEmpty());

        final DOMDataTreeProducer testProducer =
                dataTreeService.createProducer(Collections.singletonList(TEST_ID));
        assertTrue(rootShard.getProducers().size() == 1);
        assertTrue(innerShard.getProducers().size() == 1);

        final DOMDataTreeShardProducer rootShardProducer = Iterables.getOnlyElement(rootShard.getProducers());
        assertEquals(rootShardProducer.getPrefixes().toString(),
                Collections.singletonList(TEST_ID).toString());

        final DOMDataTreeShardProducer innerShardProducer = Iterables.getOnlyElement(innerShard.getProducers());
        assertEquals(innerShardProducer.getPrefixes().toString(),
                Collections.singletonList(INNER_CONTAINER_ID).toString());

        final InMemoryDOMDataTreeShard test2Shard = InMemoryDOMDataTreeShard.create(TEST2_ID, executor, 1);
        innerShard.onGlobalContextUpdated(SCHEMA_CONTEXT);

        final DOMDataTreeProducer test2ShardRegProducer =
                dataTreeService.createProducer(Collections.singletonList(TEST2_ID));
        dataTreeService.registerDataTreeShard(TEST2_ID, test2Shard, test2ShardRegProducer);
        test2ShardRegProducer.close();

        assertTrue(rootShard.getProducers().size() == 1);
        assertTrue(innerShard.getProducers().size() == 1);

        final DOMDataTreeShardProducer rootShardProducer2 = Iterables.getOnlyElement(rootShard.getProducers());
        assertEquals(rootShardProducer2.getPrefixes().toString(),
                Collections.singletonList(TEST_ID).toString());

        final DOMDataTreeShardProducer innerShardProducer2 = Iterables.getOnlyElement(innerShard.getProducers());
        assertEquals(innerShardProducer2.getPrefixes().toString(),
                Collections.singletonList(INNER_CONTAINER_ID).toString());
    }

    @Test
    public void testMultipleWritesIntoSingleShard() throws Exception {
        final DOMDataTreeListener mockedDataTreeListener = mock(DOMDataTreeListener.class);
        doNothing().when(mockedDataTreeListener).onDataTreeChanged(anyCollection(), anyMap());

        dataTreeService.registerListener(mockedDataTreeListener, Collections.singletonList(INNER_CONTAINER_ID),
                true, Collections.emptyList());

        final DOMDataTreeShardProducer producer = rootShard.createProducer(Collections.singletonList(TEST_ID));
        final DOMDataTreeShardWriteTransaction transaction = producer.createTransaction();
        writeCrossShardContainer(transaction);

        final DOMDataTreeShardWriteTransaction newTx = producer.createTransaction();
        final DOMDataTreeWriteCursor newCursor = newTx.createCursor(ROOT_ID);

        newCursor.delete(TestModel.TEST_PATH.getLastPathArgument());
    }

    @Test
    public void testMockedSubshards() throws Exception {
        final WriteableDOMDataTreeShard mockedInnerShard = mock(WriteableDOMDataTreeShard.class);
        final DOMDataTreeShardProducer mockedProducer = mock(DOMDataTreeShardProducer.class);
        doReturn(mockedProducer).when(mockedInnerShard).createProducer(anyCollection());
        final ShardedDOMDataTreeProducer shardRegProducer = mock(ShardedDOMDataTreeProducer.class);
        doReturn(Collections.singleton(INNER_CONTAINER_ID)).when(shardRegProducer).getSubtrees();
        doNothing().when(shardRegProducer).subshardAdded(anyMap());

        dataTreeService.registerDataTreeShard(INNER_CONTAINER_ID, mockedInnerShard, shardRegProducer);

        final DOMDataTreeShardProducer producer = rootShard.createProducer(Collections.singletonList(TEST_ID));

        final DOMDataTreeShardWriteTransaction transaction = producer.createTransaction();
        final DOMDataTreeWriteCursor cursor = transaction.createCursor(ROOT_ID);
        cursor.enter(TestModel.TEST_PATH.getLastPathArgument());

        final LeafNode<String> shardedValue1 =
                ImmutableLeafNodeBuilder.<String>create().withNodeIdentifier(
                        new NodeIdentifier(TestModel.SHARDED_VALUE_1)).withValue("sharded value 1").build();
        final LeafNode<String> shardedValue2 =
                ImmutableLeafNodeBuilder.<String>create().withNodeIdentifier(
                        new NodeIdentifier(TestModel.SHARDED_VALUE_2)).withValue("sharded value 2").build();

        final DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> containerNodeBuilder =
                ImmutableContainerNodeBuilder.create();
        final ContainerNode containerNode =
                containerNodeBuilder
                        .withNodeIdentifier(new NodeIdentifier(TestModel.INNER_CONTAINER))
                        .withChild(shardedValue1)
                        .withChild(shardedValue2)
                        .build();

        final DOMDataTreeShardWriteTransaction mockedTx = mock(DOMDataTreeShardWriteTransaction.class);
        doReturn(mockedTx).when(mockedProducer).createTransaction();

        doNothing().when(mockedTx).ready();
        doReturn(Futures.immediateFuture(true)).when(mockedTx).validate();
        doReturn(Futures.immediateFuture(null)).when(mockedTx).prepare();
        doReturn(Futures.immediateFuture(null)).when(mockedTx).commit();

        final DOMDataTreeWriteCursor mockedCursor = mock(DOMDataTreeWriteCursor.class);
        doNothing().when(mockedCursor).write(any(PathArgument.class), any(NormalizedNode.class));
        doNothing().when(mockedCursor).close();
        doReturn(mockedCursor).when(mockedTx).createCursor(any(DOMDataTreeIdentifier.class));

        cursor.write(TestModel.INNER_CONTAINER_PATH.getLastPathArgument(), containerNode);
        cursor.enter(TestModel.INNER_CONTAINER_PATH.getLastPathArgument());

        final ContainerNode lowerShardContainer = ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new NodeIdentifier(TestModel.ANOTHER_SHARD_CONTAINER))
                .withChild(ImmutableLeafNodeBuilder.create().withNodeIdentifier(
                        new NodeIdentifier(TestModel.ANOTHER_SHARD_VALUE)).withValue("").build())
                .build();

        cursor.write(TestModel.ANOTHER_SHARD_PATH.getLastPathArgument(), lowerShardContainer);
        cursor.close();
        transaction.ready();
        transaction.submit().get();

        final InOrder inOrder = inOrder(mockedTx);
        inOrder.verify(mockedTx).ready();
        inOrder.verify(mockedTx).validate();
        inOrder.verify(mockedTx).prepare();
        inOrder.verify(mockedTx).commit();

    }

    private static ContainerNode createCrossShardContainer() {
        final LeafNode<String> shardedValue1 =
                ImmutableLeafNodeBuilder.<String>create().withNodeIdentifier(
                        new NodeIdentifier(TestModel.SHARDED_VALUE_1)).withValue("sharded value 1").build();
        final LeafNode<String> shardedValue2 =
                ImmutableLeafNodeBuilder.<String>create().withNodeIdentifier(new NodeIdentifier(
                        TestModel.SHARDED_VALUE_2)).withValue("sharded value 2").build();


        final ContainerNode lowerShardContainer = ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new NodeIdentifier(TestModel.ANOTHER_SHARD_CONTAINER))
                .withChild(ImmutableLeafNodeBuilder.create()
                        .withNodeIdentifier(new NodeIdentifier(TestModel.ANOTHER_SHARD_VALUE))
                        .withValue("testing-value")
                        .build())
                .build();

        final ContainerNode containerNode =
                ImmutableContainerNodeBuilder.create()
                        .withNodeIdentifier(new NodeIdentifier(TestModel.INNER_CONTAINER))
                        .withChild(shardedValue1)
                        .withChild(shardedValue2)
                        .withChild(lowerShardContainer)
                        .build();

        final ContainerNode testContainer = ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
                .withChild(containerNode)
                .build();

        return testContainer;
    }

    private void writeCrossShardContainer(final DOMDataTreeShardWriteTransaction transaction) throws Exception {
        final DOMDataTreeWriteCursor cursor = transaction.createCursor(ROOT_ID);

        cursor.write(TestModel.TEST_PATH.getLastPathArgument(), crossShardContainer);

        cursor.close();
        transaction.ready();
        transaction.submit().get();
    }
}
