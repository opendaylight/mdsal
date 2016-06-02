package org.opendaylight.mdsal.dom.broker.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

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
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCursorAwareTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducer;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.mdsal.dom.broker.ShardedDOMDataTree;
import org.opendaylight.mdsal.dom.broker.test.util.TestModel;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMDataTreeShard;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShardedDOMDataTreeTest {

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

    private static final DOMDataTreeIdentifier INNER_CONTAINER_ID =
            new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, TestModel.INNER_CONTAINER_PATH);

    private InMemoryDOMDataTreeShard rootShard;

    private ShardedDOMDataTree dataTreeService;
    private ListenerRegistration<InMemoryDOMDataTreeShard> rootShardReg;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Captor
    private ArgumentCaptor<Collection<DataTreeCandidate>> captorForChanges;
    @Captor
    private ArgumentCaptor<Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>>> captorForSubtrees;

    private final ContainerNode crossShardContainer = createCrossShardContainer();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        rootShard = InMemoryDOMDataTreeShard.create(ROOT_ID, executor, 5000);
        rootShard.onGlobalContextUpdated(schemaContext);

        final ShardedDOMDataTree dataTree = new ShardedDOMDataTree();
        rootShardReg = dataTree.registerDataTreeShard(ROOT_ID, rootShard);

        dataTreeService = dataTree;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProducerPathContention() throws Exception {
        final DOMDataTreeProducer p1 = dataTreeService.createProducer(Collections.singletonList(ROOT_ID));
        final DOMDataTreeProducer p2 = dataTreeService.createProducer(Collections.singletonList(TEST_ID));
    }

    @Test
    public void testSingleShardWrite() throws Exception {
        final DOMDataTreeListener mockedDataTreeListener = Mockito.mock(DOMDataTreeListener.class);
        doNothing().when(mockedDataTreeListener).onDataTreeChanged(anyCollection(), anyMap());

        dataTreeService.registerListener(mockedDataTreeListener, Collections.singletonList(INNER_CONTAINER_ID), true, Collections.emptyList());

        final DOMDataTreeProducer producer = dataTreeService.createProducer(Collections.singletonList(ROOT_ID));
        DOMDataTreeCursorAwareTransaction tx = producer.createTransaction(false);
        DOMDataTreeWriteCursor cursor = tx.createCursor(ROOT_ID);
        assertNotNull(cursor);

        cursor.write(TEST_ID.getRootIdentifier().getLastPathArgument(), crossShardContainer);

        try {
            tx.submit().checkedGet();
            fail("There's still an open cursor");
        } catch (final IllegalStateException e) {
            assertTrue(e.getMessage().contains("cursor open"));
        }

        cursor.close();
        tx.submit().checkedGet();

        tx = producer.createTransaction(false);
        cursor = tx.createCursor(TEST_ID);
        assertNotNull(cursor);

        cursor.delete(TestModel.INNER_CONTAINER_PATH.getLastPathArgument());
        cursor.close();
        tx.submit().checkedGet();

        verify(mockedDataTreeListener, timeout(1000).times(2)).onDataTreeChanged(captorForChanges.capture(), captorForSubtrees.capture());
        final List<Collection<DataTreeCandidate>> capturedValue = captorForChanges.getAllValues();
        assertTrue(capturedValue.size() == 2);

        final ContainerNode capturedChange = (ContainerNode) capturedValue.get(0).iterator().next().getRootNode().getDataAfter().get();
        final ContainerNode innerContainerVerify = (ContainerNode) crossShardContainer.getChild(TestModel.INNER_CONTAINER_PATH.getLastPathArgument()).get();
        assertEquals(innerContainerVerify, capturedChange);

        verifyNoMoreInteractions(mockedDataTreeListener);
    }

    @Test
    public void testMultipleProducerCursorCreation() throws Exception {

        final DOMDataTreeProducer rootProducer = dataTreeService.createProducer(Collections.singletonList(ROOT_ID));
        DOMDataTreeCursorAwareTransaction rootTx = rootProducer.createTransaction(false);
        //check if we can create cursor where the new producer will be
        DOMDataTreeWriteCursor rootTxCursor = rootTx.createCursor(INNER_CONTAINER_ID);
        assertNotNull(rootTxCursor);
        rootTxCursor.close();

        try {
            rootProducer.createProducer(Collections.singletonList(INNER_CONTAINER_ID));
            fail("Should've failed there is still a tx open");
        } catch (final IllegalStateException e) {
            assertTrue(e.getMessage().contains("open"));
        }

        assertTrue(rootTx.cancel());

        final DOMDataTreeProducer innerContainerProducer = rootProducer.createProducer(Collections.singletonList(INNER_CONTAINER_ID));

        rootTx = rootProducer.createTransaction(false);
        try {
            rootTx.createCursor(INNER_CONTAINER_ID);
            fail("Subtree should not be available to this producer");
        } catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("delegated to child producer"));
        }

        rootTxCursor = rootTx.createCursor(TEST_ID);
        assertNotNull(rootTxCursor);
        try {
            rootTxCursor.enter(INNER_CONTAINER_ID.getRootIdentifier().getLastPathArgument());
            fail("Cursor should not have this subtree available");
        } catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("not available to this cursor"));
        }

        try {
            rootTxCursor.write(TestModel.INNER_CONTAINER_PATH.getLastPathArgument(),
                    ImmutableContainerNodeBuilder.create()
                            .withNodeIdentifier(new NodeIdentifier(TestModel.INNER_CONTAINER))
                            .build());
            fail("Cursor should not have this subtree available");
        } catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("not available to this cursor"));
        }

        final DOMDataTreeCursorAwareTransaction innerShardTx = innerContainerProducer.createTransaction(false);
        final DOMDataTreeWriteCursor innerShardCursor = innerShardTx.createCursor(INNER_CONTAINER_ID);
        assertNotNull(innerShardCursor);
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
}
