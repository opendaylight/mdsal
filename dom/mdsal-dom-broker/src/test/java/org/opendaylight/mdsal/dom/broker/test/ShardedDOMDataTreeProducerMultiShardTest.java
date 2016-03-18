package org.opendaylight.mdsal.dom.broker.test;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeService;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.mdsal.dom.broker.ShardedDOMDataTree;
import org.opendaylight.mdsal.dom.broker.test.util.TestModel;
import org.opendaylight.mdsal.dom.store.inmemory.DOMDataTreeShardProducer;
import org.opendaylight.mdsal.dom.store.inmemory.DOMDataTreeShardWriteTransaction;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMDataTreeShard;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeAttrBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class ShardedDOMDataTreeProducerMultiShardTest {

    private final SchemaContext schemaContext = TestModel.createTestContext();

    private static final DOMDataTreeIdentifier ROOT_ID = new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL,
            YangInstanceIdentifier.EMPTY);
    private static final DOMDataTreeIdentifier TEST_ID = new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL,
            TestModel.TEST_PATH);

    private static final DOMDataTreeIdentifier TEST2_ID = new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL,
            TestModel.TEST2_PATH);

    private static final DOMDataTreeIdentifier INNER_CONTAINER_ID = new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, TestModel.INNER_CONTAINER_PATH);
    private static final DOMDataTreeIdentifier ANOTHER_SHARD_ID = new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, TestModel.ANOTHER_SHARD_PATH);

    private final InMemoryDOMDataTreeShard rootShard = InMemoryDOMDataTreeShard.create(ROOT_ID);

    private final InMemoryDOMDataTreeShard innerShard = InMemoryDOMDataTreeShard.create(INNER_CONTAINER_ID);

    private final InMemoryDOMDataTreeShard anotherInnerShard = InMemoryDOMDataTreeShard.create(ANOTHER_SHARD_ID);


    private DOMDataTreeService dataTreeService;
    private ListenerRegistration<InMemoryDOMDataTreeShard> rootShardReg;
    private ListenerRegistration<InMemoryDOMDataTreeShard> innerShardReg;

    @Before
    public void setUp() throws Exception {

        rootShard.onGlobalContextUpdated(schemaContext);
        innerShard.onGlobalContextUpdated(schemaContext);
        anotherInnerShard.onGlobalContextUpdated(schemaContext);

        ShardedDOMDataTree dataTree = new ShardedDOMDataTree();
        rootShardReg = dataTree.registerDataTreeShard(ROOT_ID, rootShard);
        innerShardReg = dataTree.registerDataTreeShard(INNER_CONTAINER_ID, innerShard);

        dataTreeService = dataTree;
    }

    @Test
    public void testMultipleShards() throws Exception {
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
        cursor.close();


    }

}
