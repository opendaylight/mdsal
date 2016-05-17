package org.opendaylight.mdsal.dom.broker.test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.google.common.util.concurrent.Futures;
import java.util.Collection;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
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
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeAttrBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class ShardedDOMDataTreeProducerMultiShardTest {

    private SchemaContext schemaContext;

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

    @Before
    public void setUp() throws Exception {
        schemaContext = TestModel.createTestContext();

        rootShard = InMemoryDOMDataTreeShard.create(ROOT_ID);
        rootShard.onGlobalContextUpdated(schemaContext);

        ShardedDOMDataTree dataTree = new ShardedDOMDataTree();
        rootShardReg = dataTree.registerDataTreeShard(ROOT_ID, rootShard);

        dataTreeService = dataTree;
    }

    @Test
    public void testMultipleShards() throws Exception {
        //FIXME after listeners are implemented add them here and test those

        final InMemoryDOMDataTreeShard innerShard = InMemoryDOMDataTreeShard.create(INNER_CONTAINER_ID);
        innerShard.onGlobalContextUpdated(schemaContext);
        innerShardReg = dataTreeService.registerDataTreeShard(INNER_CONTAINER_ID, innerShard);

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

        //verify listeners have been notified
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

        verify(mockedTx).ready();
        verify(mockedTx).validate();
        verify(mockedTx).prepare();
        verify(mockedTx).commit();

    }
}
