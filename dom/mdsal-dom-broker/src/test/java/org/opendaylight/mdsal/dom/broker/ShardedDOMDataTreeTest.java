/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
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
import org.mockito.MockitoAnnotations;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCursorAwareTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducer;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.mdsal.dom.broker.util.TestModel;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMDataTreeShard;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapNodeBuilder;
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
            new DOMDataTreeIdentifier(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.EMPTY);
    private static final DOMDataTreeIdentifier TEST_ID =
            new DOMDataTreeIdentifier(LogicalDatastoreType.CONFIGURATION, TestModel.TEST_PATH);

    private static final DOMDataTreeIdentifier INNER_CONTAINER_ID =
            new DOMDataTreeIdentifier(LogicalDatastoreType.CONFIGURATION, TestModel.INNER_CONTAINER_PATH);

    private static final YangInstanceIdentifier OUTER_LIST_YID = TestModel.OUTER_LIST_PATH.node(
            new NodeIdentifierWithPredicates(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, 1));
    private static final DOMDataTreeIdentifier OUTER_LIST_ID =
            new DOMDataTreeIdentifier(LogicalDatastoreType.CONFIGURATION, OUTER_LIST_YID);

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

        rootShard = InMemoryDOMDataTreeShard.create(ROOT_ID, executor, 1);
        rootShard.onGlobalContextUpdated(schemaContext);

        final ShardedDOMDataTree dataTree = new ShardedDOMDataTree();
        final DOMDataTreeProducer shardRegProducer = dataTree.createProducer(Collections.singletonList(ROOT_ID));
        rootShardReg = dataTree.registerDataTreeShard(ROOT_ID, rootShard, shardRegProducer);
        shardRegProducer.close();

        dataTreeService = dataTree;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProducerPathContention() throws Exception {
        dataTreeService.createProducer(Collections.singletonList(ROOT_ID));
        dataTreeService.createProducer(Collections.singletonList(TEST_ID));
    }

    @Test
    public void testShardRegistrationClose() throws Exception {
        rootShardReg.close();

        final InMemoryDOMDataTreeShard newRootShard = InMemoryDOMDataTreeShard.create(ROOT_ID, executor, 1);
        newRootShard.onGlobalContextUpdated(schemaContext);
        final DOMDataTreeProducer shardRegProducer = dataTreeService.createProducer(Collections.singletonList(ROOT_ID));

        final ListenerRegistration<InMemoryDOMDataTreeShard> newRootShardReg =
                dataTreeService.registerDataTreeShard(ROOT_ID, rootShard, shardRegProducer);
        shardRegProducer.close();

        final InMemoryDOMDataTreeShard innerShard = InMemoryDOMDataTreeShard.create(INNER_CONTAINER_ID, executor, 1);
        innerShard.onGlobalContextUpdated(schemaContext);
        final DOMDataTreeProducer shardRegProducer2 =
                dataTreeService.createProducer(Collections.singletonList(INNER_CONTAINER_ID));
        ListenerRegistration<InMemoryDOMDataTreeShard> innerShardReg =
                dataTreeService.registerDataTreeShard(INNER_CONTAINER_ID, innerShard, shardRegProducer2);

        innerShardReg.close();
        // try to register the shard again
        innerShardReg = dataTreeService.registerDataTreeShard(INNER_CONTAINER_ID, innerShard, shardRegProducer2);
        final DOMDataTreeCursorAwareTransaction tx = shardRegProducer2.createTransaction(false);
        final DOMDataTreeWriteCursor cursor = tx.createCursor(INNER_CONTAINER_ID);
        assertNotNull(cursor);

        cursor.close();
        tx.cancel();
        shardRegProducer2.close();

        innerShardReg.close();
        newRootShardReg.close();
    }

    @Test(expected = IllegalStateException.class)
    public void testEmptyShardMapProducer() throws Exception {
        final ShardedDOMDataTree dataTree = new ShardedDOMDataTree();
        final DOMDataTreeProducer producer = dataTree.createProducer(Collections.singletonList(ROOT_ID));
        producer.createTransaction(false);
    }

    @Test
    public void testSingleShardWrite() throws Exception {
        final DOMDataTreeListener mockedDataTreeListener = mock(DOMDataTreeListener.class);
        doNothing().when(mockedDataTreeListener).onDataTreeChanged(anyCollection(), anyMap());

        dataTreeService.registerListener(mockedDataTreeListener, Collections.singletonList(INNER_CONTAINER_ID),
                true, Collections.emptyList());

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
        tx.submit().get();

        tx = producer.createTransaction(false);
        cursor = tx.createCursor(TEST_ID);
        assertNotNull(cursor);

        cursor.delete(TestModel.INNER_CONTAINER_PATH.getLastPathArgument());
        cursor.close();
        tx.submit().get();

        verify(mockedDataTreeListener, timeout(1000).times(3)).onDataTreeChanged(captorForChanges.capture(),
                captorForSubtrees.capture());
        final List<Collection<DataTreeCandidate>> capturedValue = captorForChanges.getAllValues();
        assertTrue(capturedValue.size() == 3);

        final ContainerNode capturedChange =
                (ContainerNode) capturedValue.get(1).iterator().next().getRootNode().getDataAfter().get();
        final ContainerNode innerContainerVerify = (ContainerNode) crossShardContainer.getChild(
                TestModel.INNER_CONTAINER_PATH.getLastPathArgument()).get();
        assertEquals(innerContainerVerify, capturedChange);

        verifyNoMoreInteractions(mockedDataTreeListener);
    }

    @Test
    // TODO extract common logic from testSingleSubshardWrite and
    // testSingleShardWrite tests
    public void testSingleSubshardWrite() throws Exception {
        final DOMDataTreeListener mockedDataTreeListener = mock(DOMDataTreeListener.class);
        doNothing().when(mockedDataTreeListener).onDataTreeChanged(anyCollection(), anyMap());

        InMemoryDOMDataTreeShard testShard = InMemoryDOMDataTreeShard.create(TEST_ID, executor, 1);
        testShard.onGlobalContextUpdated(schemaContext);

        final DOMDataTreeProducer regProducer = dataTreeService.createProducer(Collections.singleton(TEST_ID));
        dataTreeService.registerDataTreeShard(TEST_ID, testShard, regProducer);
        regProducer.close();

        dataTreeService.registerListener(mockedDataTreeListener, Collections.singletonList(TEST_ID),
                true, Collections.emptyList());

        final DOMDataTreeProducer producer = dataTreeService.createProducer(Collections.singletonList(ROOT_ID));
        DOMDataTreeCursorAwareTransaction tx = producer.createTransaction(false);
        DOMDataTreeWriteCursor cursor = tx.createCursor(ROOT_ID);
        assertNotNull(cursor);

        cursor.write(TEST_ID.getRootIdentifier().getLastPathArgument(), crossShardContainer);

        cursor.close();
        tx.submit().get();

        tx = producer.createTransaction(false);
        cursor = tx.createCursor(TEST_ID);
        assertNotNull(cursor);

        cursor.delete(TestModel.INNER_CONTAINER_PATH.getLastPathArgument());
        cursor.close();
        tx.submit().get();

        verify(mockedDataTreeListener, timeout(5000).times(3)).onDataTreeChanged(captorForChanges.capture(),
                captorForSubtrees.capture());

        final List<Collection<DataTreeCandidate>> capturedValue = captorForChanges.getAllValues();
        final ContainerNode capturedChange =
                (ContainerNode) capturedValue.get(1).iterator().next().getRootNode().getDataAfter().get();
        final ContainerNode innerContainerVerify = crossShardContainer;
        assertEquals(innerContainerVerify, capturedChange);
    }

    @Test
    public void testMultipleWritesIntoSingleMapEntry() throws Exception {

        final YangInstanceIdentifier oid1 = TestModel.OUTER_LIST_PATH.node(new NodeIdentifierWithPredicates(
                TestModel.OUTER_LIST_QNAME, QName.create(TestModel.OUTER_LIST_QNAME, "id"), 0));
        final DOMDataTreeIdentifier outerListPath = new DOMDataTreeIdentifier(LogicalDatastoreType.CONFIGURATION, oid1);

        final DOMDataTreeProducer shardProducer = dataTreeService.createProducer(
                Collections.singletonList(outerListPath));
        final InMemoryDOMDataTreeShard outerListShard = InMemoryDOMDataTreeShard.create(outerListPath, executor, 1000);
        outerListShard.onGlobalContextUpdated(schemaContext);

        final ListenerRegistration<InMemoryDOMDataTreeShard> oid1ShardRegistration =
                dataTreeService.registerDataTreeShard(outerListPath, outerListShard, shardProducer);

        final DOMDataTreeCursorAwareTransaction tx = shardProducer.createTransaction(false);
        final DOMDataTreeWriteCursor cursor =
                tx.createCursor(new DOMDataTreeIdentifier(LogicalDatastoreType.CONFIGURATION, oid1));
        assertNotNull(cursor);

        MapNode innerList = ImmutableMapNodeBuilder
                .create()
                .withNodeIdentifier(new NodeIdentifier(TestModel.INNER_LIST_QNAME))
                .build();

        cursor.write(new NodeIdentifier(TestModel.INNER_LIST_QNAME), innerList);
        cursor.close();
        tx.submit().get();

        final ArrayList<ListenableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            final Collection<MapEntryNode> innerListMapEntries = createInnerListMapEntries(1000, "run-" + i);
            for (final MapEntryNode innerListMapEntry : innerListMapEntries) {
                final DOMDataTreeCursorAwareTransaction tx1 = shardProducer.createTransaction(false);
                final DOMDataTreeWriteCursor cursor1 = tx1.createCursor(
                        new DOMDataTreeIdentifier(LogicalDatastoreType.CONFIGURATION,
                                oid1.node(new NodeIdentifier(TestModel.INNER_LIST_QNAME))));
                cursor1.write(innerListMapEntry.getIdentifier(), innerListMapEntry);
                cursor1.close();
                futures.add(tx1.submit());
            }
        }

        futures.get(futures.size() - 1).get();

    }

    private static Collection<MapEntryNode> createInnerListMapEntries(final int amount, final String valuePrefix) {
        final Collection<MapEntryNode> ret = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            ret.add(ImmutableNodes.mapEntryBuilder()
                    .withNodeIdentifier(new NodeIdentifierWithPredicates(TestModel.INNER_LIST_QNAME,
                            QName.create(TestModel.OUTER_LIST_QNAME, "name"), Integer.toString(i)))
                    .withChild(ImmutableNodes
                            .leafNode(QName.create(TestModel.INNER_LIST_QNAME, "name"), Integer.toString(i)))
                    .withChild(ImmutableNodes
                            .leafNode(QName.create(TestModel.INNER_LIST_QNAME, "value"), valuePrefix + "-" + i))
                    .build());
        }

        return ret;
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

        final DOMDataTreeProducer innerContainerProducer = rootProducer.createProducer(
                Collections.singletonList(INNER_CONTAINER_ID));

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

    private static ContainerNode createCrossShardContainer() {
        final LeafNode<String> shardedValue1 =
                ImmutableLeafNodeBuilder.<String>create().withNodeIdentifier(new NodeIdentifier(
                        TestModel.SHARDED_VALUE_1)).withValue("sharded value 1").build();
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

    //test multiple vertical levels between the shards
    @Test
    public void testLargerSubshardSpace() throws Exception {

        final InMemoryDOMDataTreeShard outerListShard = InMemoryDOMDataTreeShard.create(OUTER_LIST_ID, executor, 1, 1);
        outerListShard.onGlobalContextUpdated(schemaContext);

        try (DOMDataTreeProducer producer =
                     dataTreeService.createProducer(Collections.singletonList(OUTER_LIST_ID))) {
            dataTreeService.registerDataTreeShard(OUTER_LIST_ID, outerListShard, producer);
        }

        final DOMDataTreeProducer producer = dataTreeService.createProducer(Collections.singletonList(ROOT_ID));
        final DOMDataTreeCursorAwareTransaction tx = producer.createTransaction(false);
        final DOMDataTreeWriteCursor cursor = tx.createCursor(ROOT_ID);

        assertNotNull(cursor);
        cursor.write(TestModel.TEST_PATH.getLastPathArgument(), createCrossShardContainer2());
        cursor.close();

        tx.submit().get();

        final DOMDataTreeListener listener = mock(DOMDataTreeListener.class);
        doNothing().when(listener).onDataTreeChanged(any(), any());
        dataTreeService.registerListener(listener,
                Collections.singletonList(
                        new DOMDataTreeIdentifier(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.EMPTY)),
                false, Collections.emptyList());

        verify(listener, times(2)).onDataTreeChanged(any(), any());


    }

    private static ContainerNode createCrossShardContainer2() {

        final MapEntryNode
                innerListEntry1 = ImmutableNodes
                .mapEntryBuilder(TestModel.INNER_LIST_QNAME, TestModel.NAME_QNAME, "name-1")
                .withChild(ImmutableNodes.leafNode(TestModel.NAME_QNAME, "name-1"))
                .withChild(ImmutableNodes.leafNode(TestModel.VALUE_QNAME, "value-1"))
                .build();
        final MapEntryNode innerListEntry2 = ImmutableNodes
                .mapEntryBuilder(TestModel.INNER_LIST_QNAME, TestModel.NAME_QNAME, "name-2")
                .withChild(ImmutableNodes.leafNode(TestModel.NAME_QNAME, "name-2"))
                .withChild(ImmutableNodes.leafNode(TestModel.VALUE_QNAME, "value-2"))
                .build();

        final MapNode innerList1 = ImmutableNodes
                .mapNodeBuilder(TestModel.INNER_LIST_QNAME).withChild(innerListEntry1).build();
        final MapNode innerList2 = ImmutableNodes
                .mapNodeBuilder(TestModel.INNER_LIST_QNAME).withChild(innerListEntry2).build();

        final MapEntryNode outerListEntry1 = ImmutableNodes
                .mapEntryBuilder(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, 1)
                .withChild(innerList1)
                .build();
        final MapEntryNode outerListEntry2 = ImmutableNodes
                .mapEntryBuilder(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, 2)
                .withChild(innerList2)
                .build();

        final MapNode outerList = ImmutableNodes.mapNodeBuilder(TestModel.OUTER_LIST_QNAME)
                .withChild(outerListEntry1)
                .withChild(outerListEntry2)
                .build();

        final ContainerNode testContainer = ImmutableContainerNodeBuilder.create()
                .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
                .withChild(outerList)
                .build();

        return testContainer;
    }
}
