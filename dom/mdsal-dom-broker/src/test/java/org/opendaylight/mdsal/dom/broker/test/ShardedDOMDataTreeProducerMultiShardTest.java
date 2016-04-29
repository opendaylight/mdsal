package org.opendaylight.mdsal.dom.broker.test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.google.common.util.concurrent.Futures;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.Nonnull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListeningException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShardedDOMDataTreeProducerMultiShardTest {

    private static final Logger LOG = LoggerFactory.getLogger(ShardedDOMDataTreeProducerMultiShardTest.class);

    private final SchemaContext schemaContext = TestModel.createTestContext();

    private static final DOMDataTreeIdentifier ROOT_ID = new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL,
            YangInstanceIdentifier.EMPTY);
    private static final DOMDataTreeIdentifier TEST_ID = new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL,
            TestModel.TEST_PATH);

    private static final DOMDataTreeIdentifier TEST2_ID = new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL,
            TestModel.TEST2_PATH);

    private static final DOMDataTreeIdentifier INNER_CONTAINER_ID = new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, TestModel.INNER_CONTAINER_PATH);
    private static final DOMDataTreeIdentifier ANOTHER_SHARD_ID = new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, TestModel.ANOTHER_SHARD_PATH);

    private InMemoryDOMDataTreeShard rootShard;

    private InMemoryDOMDataTreeShard anotherInnerShard;


    private ShardedDOMDataTree dataTreeService;
    private ListenerRegistration<InMemoryDOMDataTreeShard> rootShardReg;
    private ListenerRegistration<InMemoryDOMDataTreeShard> innerShardReg;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Before
    public void setUp() throws Exception {

        rootShard = InMemoryDOMDataTreeShard.create(ROOT_ID, executor, 5000);
        rootShard.onGlobalContextUpdated(schemaContext);

        ShardedDOMDataTree dataTree = new ShardedDOMDataTree();
        rootShardReg = dataTree.registerDataTreeShard(ROOT_ID, rootShard);

        dataTreeService = dataTree;
    }

    @Test
    public void testSingleShardListener() throws Exception {
        //FIXME after listeners are implemented add them here and test those

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

        verify(mockedDataTreeListener, timeout(100)).onDataTreeChanged(anyCollection(), anyMap());
        verifyNoMoreInteractions(mockedDataTreeListener);
    }

    @Test
    public void testMultipleShards() throws Exception {
        //FIXME after listeners are implemented add them here and test those

//        final DOMDataTreeListener mockedDataTreeListener = new DOMDataTreeListener() {
//            @Override
//            public void onDataTreeChanged(@Nonnull Collection<DataTreeCandidate> changes, @Nonnull Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>> subtrees) {
//                LOG.warn("Received onDataTreeChanged callback changes {} , subtrees {}", changes, subtrees);
//            }
//
//            @Override
//            public void onDataTreeFailed(@Nonnull Collection<DOMDataTreeListeningException> causes) {
//                LOG.warn("Received onDataTreeFailed callback causes {}", causes);
//            }
//        };

        final DOMDataTreeListener mockedDataTreeListener = Mockito.mock(DOMDataTreeListener.class);
        doNothing().when(mockedDataTreeListener).onDataTreeChanged(anyCollection(), anyMap());

        final InMemoryDOMDataTreeShard innerShard = InMemoryDOMDataTreeShard.create(INNER_CONTAINER_ID, executor, 5000);
        innerShard.onGlobalContextUpdated(schemaContext);
        innerShardReg = dataTreeService.registerDataTreeShard(INNER_CONTAINER_ID, innerShard);

        dataTreeService.registerListener(mockedDataTreeListener, Collections.singletonList(TEST_ID), true, Collections.emptyList());

        final DOMDataTreeShardProducer producer = rootShard.createProducer(Collections.singletonList(TEST_ID));
        final DOMDataTreeShardWriteTransaction transaction = producer.createTransaction();
        final DOMDataTreeWriteCursor cursor = transaction.createCursor(ROOT_ID);

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

        final ContainerNode testContainer = ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
                .withChild(containerNode)
                .build();
        cursor.write(TestModel.TEST_PATH.getLastPathArgument(), testContainer);

        cursor.enter(TestModel.TEST_PATH.getLastPathArgument());
        cursor.enter(TestModel.INNER_CONTAINER_PATH.getLastPathArgument());

        final ContainerNode lowerShardContainer = ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new NodeIdentifier(TestModel.ANOTHER_SHARD_CONTAINER))
                .withChild(ImmutableLeafNodeBuilder.create().withNodeIdentifier(new NodeIdentifier(TestModel.ANOTHER_SHARD_VALUE)).build())
                .build();

        cursor.write(TestModel.ANOTHER_SHARD_PATH.getLastPathArgument(), lowerShardContainer);
        cursor.close();
        transaction.ready();
        transaction.submit().get();

        //verify listeners have been notified

        verify(mockedDataTreeListener, timeout(1000000).times(2)).onDataTreeChanged(anyCollection(), anyMap());
        verifyNoMoreInteractions(mockedDataTreeListener);
    }

    @Test
    public void testMockedSubshards() throws Exception {
        final WriteableDOMDataTreeShard mockedInnerShard = Mockito.mock(WriteableDOMDataTreeShard.class);
        dataTreeService.registerDataTreeShard(INNER_CONTAINER_ID, mockedInnerShard);
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
}
