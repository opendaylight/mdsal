package org.opendaylight.mdsal.dom.broker.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

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
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducer;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.mdsal.dom.broker.ShardedDOMDataTree;
import org.opendaylight.mdsal.dom.broker.test.util.TestModel;
import org.opendaylight.mdsal.dom.store.inmemory.DOMDataTreeShardProducer;
import org.opendaylight.mdsal.dom.store.inmemory.DOMDataTreeShardWriteTransaction;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMDataTreeShard;
import org.opendaylight.mdsal.dom.store.inmemory.WriteableDOMDataTreeShard;
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
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShardedDOMDataTreeProducerMultiShardTest {

    private static final Logger LOG = LoggerFactory.getLogger(ShardedDOMDataTreeProducerMultiShardTest.class);

    private static SchemaContext schemaContext = null;

    static {
        try {
            schemaContext = TestModel.createTestContext();
        } catch (final ReactorException e) {
            LOG.error("Unable to create schema context for TestModel", e);
        }
    }

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

        rootShard = InMemoryDOMDataTreeShard.create(ROOT_ID, executor, 1, 1);
        rootShard.onGlobalContextUpdated(schemaContext);

        final ShardedDOMDataTree dataTree = new ShardedDOMDataTree();
        final DOMDataTreeProducer shardRegProducer = dataTree.createProducer(Collections.singletonList(ROOT_ID));
        rootShardReg = dataTree.registerDataTreeShard(ROOT_ID, rootShard, shardRegProducer);
        shardRegProducer.close();

        dataTreeService = dataTree;
    }

    @Test(expected=IllegalStateException.class)
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

    @Test(expected=IllegalStateException.class)
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
        final DOMDataTreeListener mockedDataTreeListener = Mockito.mock(DOMDataTreeListener.class);
        doNothing().when(mockedDataTreeListener).onDataTreeChanged(anyCollection(), anyMap());

        dataTreeService.registerListener(mockedDataTreeListener, Collections.singletonList(INNER_CONTAINER_ID), true, Collections.emptyList());

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

        verify(mockedDataTreeListener, timeout(1000)).onDataTreeChanged(captorForChanges.capture(), captorForSubtrees.capture());
        final Collection<DataTreeCandidate> capturedValue = captorForChanges.getValue();
        assertTrue(capturedValue.size() == 1);

        final ContainerNode dataAfter = (ContainerNode) capturedValue.iterator().next().getRootNode().getDataAfter().get();
        assertEquals(innerContainer, dataAfter);
        verifyNoMoreInteractions(mockedDataTreeListener);
    }

    @Test
    public void testSingleShardListener() throws Exception {
        final DOMDataTreeListener mockedDataTreeListener = Mockito.mock(DOMDataTreeListener.class);
        doNothing().when(mockedDataTreeListener).onDataTreeChanged(anyCollection(), anyMap());

        dataTreeService.registerListener(mockedDataTreeListener, Collections.singletonList(INNER_CONTAINER_ID), true, Collections.emptyList());

        final DOMDataTreeShardProducer producer = rootShard.createProducer(Collections.singletonList(TEST_ID));
        final DOMDataTreeShardWriteTransaction transaction = producer.createTransaction();
        writeCrossShardContainer(transaction);

        verify(mockedDataTreeListener, timeout(1000)).onDataTreeChanged(captorForChanges.capture(), captorForSubtrees.capture());
        final Collection<DataTreeCandidate> capturedValue = captorForChanges.getValue();
        assertTrue(capturedValue.size() == 1);

        final ContainerNode dataAfter = (ContainerNode) capturedValue.iterator().next().getRootNode().getDataAfter().get();
        assertEquals(crossShardContainer.getChild(TestModel.INNER_CONTAINER_PATH.getLastPathArgument()).get(), dataAfter);

        final Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>> capturedSubtrees = captorForSubtrees.getValue();
        assertTrue(capturedSubtrees.size() == 1);
        assertTrue(capturedSubtrees.containsKey(INNER_CONTAINER_ID));
        assertEquals(crossShardContainer.getChild(TestModel.INNER_CONTAINER_PATH.getLastPathArgument()).get(), capturedSubtrees.get(INNER_CONTAINER_ID));

        verifyNoMoreInteractions(mockedDataTreeListener);
    }

    @Test
    public void testMultipleShards() throws Exception {
        final DOMDataTreeListener mockedDataTreeListener = Mockito.mock(DOMDataTreeListener.class);
        doNothing().when(mockedDataTreeListener).onDataTreeChanged(anyCollection(), anyMap());

        final InMemoryDOMDataTreeShard innerShard = InMemoryDOMDataTreeShard.create(INNER_CONTAINER_ID, executor, 1, 1);
        innerShard.onGlobalContextUpdated(schemaContext);
        final DOMDataTreeProducer shardRegProducer = dataTreeService.createProducer(Collections.singletonList(INNER_CONTAINER_ID));
        innerShardReg = dataTreeService.registerDataTreeShard(INNER_CONTAINER_ID, innerShard, shardRegProducer);
        shardRegProducer.close();

        dataTreeService.registerListener(mockedDataTreeListener, Collections.singletonList(TEST_ID), true, Collections.emptyList());

        final DOMDataTreeShardProducer producer = rootShard.createProducer(Collections.singletonList(TEST_ID));
        final DOMDataTreeShardWriteTransaction transaction = producer.createTransaction();
        writeCrossShardContainer(transaction);

        final ContainerNode testContainerVerificationNode = ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
                .build();

        //verify listeners have been notified
        verify(mockedDataTreeListener, timeout(1000).times(2)).onDataTreeChanged(captorForChanges.capture(), captorForSubtrees.capture());
        final List<Collection<DataTreeCandidate>> capturedChanges = captorForChanges.getAllValues();
        final List<Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>>> capturedSubtrees = captorForSubtrees.getAllValues();
        final DataTreeCandidate firstNotificationCandidate = capturedChanges.get(0).iterator().next();

        assertTrue(capturedSubtrees.get(0).size() == 1);
        assertEquals(testContainerVerificationNode, firstNotificationCandidate.getRootNode().getDataAfter().get());
        assertEquals(testContainerVerificationNode, capturedSubtrees.get(0).get(TEST_ID));

        final DataTreeCandidate secondNotificationCandidate = capturedChanges.get(1).iterator().next();
        assertTrue(capturedSubtrees.get(1).size() == 1);
        assertEquals(crossShardContainer, secondNotificationCandidate.getRootNode().getDataAfter().get());
        assertEquals(crossShardContainer, capturedSubtrees.get(1).get(TEST_ID));

        verifyNoMoreInteractions(mockedDataTreeListener);
    }

    @Test
    public void testMultipleWritesIntoSingleShard() throws Exception {
        final DOMDataTreeListener mockedDataTreeListener = Mockito.mock(DOMDataTreeListener.class);
        doNothing().when(mockedDataTreeListener).onDataTreeChanged(anyCollection(), anyMap());

        dataTreeService.registerListener(mockedDataTreeListener, Collections.singletonList(INNER_CONTAINER_ID), true, Collections.emptyList());

        final DOMDataTreeShardProducer producer = rootShard.createProducer(Collections.singletonList(TEST_ID));
        final DOMDataTreeShardWriteTransaction transaction = producer.createTransaction();
        writeCrossShardContainer(transaction);

        final DOMDataTreeShardWriteTransaction newTx = producer.createTransaction();
        final DOMDataTreeWriteCursor newCursor = newTx.createCursor(ROOT_ID);

        newCursor.delete(TestModel.TEST_PATH.getLastPathArgument());
    }

    @Test
    public void testMockedSubshards() throws Exception {
        final WriteableDOMDataTreeShard mockedInnerShard = Mockito.mock(WriteableDOMDataTreeShard.class);
        final DOMDataTreeProducer shardRegProducer = Mockito.mock(DOMDataTreeProducer.class);
        doReturn(Collections.singleton(INNER_CONTAINER_ID)).when(shardRegProducer).getSubtrees();

        dataTreeService.registerDataTreeShard(INNER_CONTAINER_ID, mockedInnerShard, shardRegProducer);
        final DOMDataTreeShardProducer mockedProducer = Mockito.mock(DOMDataTreeShardProducer.class);
        doReturn(mockedProducer).when(mockedInnerShard).createProducer(any(Collection.class));

        final DOMDataTreeShardProducer producer = rootShard.createProducer(Collections.singletonList(TEST_ID));

        final DOMDataTreeShardWriteTransaction transaction = producer.createTransaction();
        final DOMDataTreeWriteCursor cursor = transaction.createCursor(ROOT_ID);
        cursor.enter(TestModel.TEST_PATH.getLastPathArgument());

        final LeafNode<String> shardedValue1 =
                ImmutableLeafNodeBuilder.<String>create().withNodeIdentifier(new NodeIdentifier(TestModel.SHARDED_VALUE_1)).withValue("sharded value 1").build();
        final LeafNode<String> shardedValue2 =
                ImmutableLeafNodeBuilder.<String>create().withNodeIdentifier(new NodeIdentifier(TestModel.SHARDED_VALUE_2)).withValue("sharded value 2").build();

        final DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> containerNodeBuilder = ImmutableContainerNodeBuilder.create();
        final ContainerNode containerNode =
                containerNodeBuilder
                        .withNodeIdentifier(new NodeIdentifier(TestModel.INNER_CONTAINER))
                        .withChild(shardedValue1)
                        .withChild(shardedValue2)
                        .build();

        final DOMDataTreeShardWriteTransaction mockedTx = Mockito.mock(DOMDataTreeShardWriteTransaction.class);
        doReturn(mockedTx).when(mockedProducer).createTransaction();

        doNothing().when(mockedTx).ready();
        doReturn(Futures.immediateFuture(true)).when(mockedTx).validate();
        doReturn(Futures.immediateFuture(null)).when(mockedTx).prepare();
        doReturn(Futures.immediateFuture(null)).when(mockedTx).commit();

        final DOMDataTreeWriteCursor mockedCursor = Mockito.mock(DOMDataTreeWriteCursor.class);
        doNothing().when(mockedCursor).write(any(PathArgument.class), any(NormalizedNode.class));
        doNothing().when(mockedCursor).close();
        doReturn(mockedCursor).when(mockedTx).createCursor(any(DOMDataTreeIdentifier.class));

        cursor.write(TestModel.INNER_CONTAINER_PATH.getLastPathArgument(), containerNode);
        cursor.enter(TestModel.INNER_CONTAINER_PATH.getLastPathArgument());

        final ContainerNode lowerShardContainer = ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new NodeIdentifier(TestModel.ANOTHER_SHARD_CONTAINER))
                .withChild(ImmutableLeafNodeBuilder.create().withNodeIdentifier(new NodeIdentifier(TestModel.ANOTHER_SHARD_VALUE)).build())
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

    private ContainerNode createCrossShardContainer() {
        final LeafNode<String> shardedValue1 =
                ImmutableLeafNodeBuilder.<String>create().withNodeIdentifier(new NodeIdentifier(TestModel.SHARDED_VALUE_1)).withValue("sharded value 1").build();
        final LeafNode<String> shardedValue2 =
                ImmutableLeafNodeBuilder.<String>create().withNodeIdentifier(new NodeIdentifier(TestModel.SHARDED_VALUE_2)).withValue("sharded value 2").build();


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

    private void writeCrossShardContainer(final DOMDataTreeShardWriteTransaction transaction) throws Exception{
        final DOMDataTreeWriteCursor cursor = transaction.createCursor(ROOT_ID);

        cursor.write(TestModel.TEST_PATH.getLastPathArgument(), crossShardContainer);

        cursor.close();
        transaction.ready();
        transaction.submit().get();
    }
}
