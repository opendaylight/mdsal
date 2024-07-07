/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.CONFIGURATION;
import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.OPERATIONAL;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ForwardingExecutorService;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.TransactionCommitDeadlockException;
import org.opendaylight.mdsal.dom.api.DOMDataBroker.DataTreeChangeExtension;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.spi.AbstractDOMDataBroker;
import org.opendaylight.mdsal.dom.spi.store.DOMStore;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMDataStore;
import org.opendaylight.yangtools.util.concurrent.DeadlockDetectingListeningExecutorService;
import org.opendaylight.yangtools.util.concurrent.SpecialExecutors;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.ModificationType;

public class DOMDataTreeListenerTest extends AbstractDatastoreTest {

    private AbstractDOMDataBroker domBroker;
    private ListeningExecutorService executor;
    private ExecutorService futureExecutor;
    private CommitExecutorService commitExecutor;

    private static final MapNode OUTER_LIST = ImmutableNodes.newSystemMapBuilder()
        .withNodeIdentifier(new NodeIdentifier(TestModel.OUTER_LIST_QNAME))
        .withChild(TestUtils.mapEntry(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, 1))
        .build();

    private static final MapNode OUTER_LIST_2 = ImmutableNodes.newSystemMapBuilder()
        .withNodeIdentifier(new NodeIdentifier(TestModel.OUTER_LIST_QNAME))
        .withChild(TestUtils.mapEntry(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, 2))
        .build();

    private static final NormalizedNode TEST_CONTAINER = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .withChild(OUTER_LIST)
            .build();

    private static final NormalizedNode TEST_CONTAINER_2 = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .withChild(OUTER_LIST_2)
            .build();

    private static final DOMDataTreeIdentifier ROOT_DATA_TREE_ID =
        DOMDataTreeIdentifier.of(LogicalDatastoreType.CONFIGURATION, TestModel.TEST_PATH);

    private static final DOMDataTreeIdentifier OUTER_LIST_DATA_TREE_ID =
        DOMDataTreeIdentifier.of(LogicalDatastoreType.CONFIGURATION, TestModel.OUTER_LIST_PATH);

    @Before
    public void setupStore() {
        final InMemoryDOMDataStore operStore = new InMemoryDOMDataStore("OPER",
                MoreExecutors.newDirectExecutorService());
        final InMemoryDOMDataStore configStore = new InMemoryDOMDataStore("CFG",
                MoreExecutors.newDirectExecutorService());

        operStore.onModelContextUpdated(SCHEMA_CONTEXT);
        configStore.onModelContextUpdated(SCHEMA_CONTEXT);

        final ImmutableMap<LogicalDatastoreType, DOMStore> stores = ImmutableMap.<LogicalDatastoreType,
                DOMStore>builder()
                .put(CONFIGURATION, configStore)
                .put(OPERATIONAL, operStore)
                .build();

        commitExecutor = new CommitExecutorService(Executors.newSingleThreadExecutor());
        futureExecutor = SpecialExecutors.newBlockingBoundedCachedThreadPool(1, 5, "FCB",
                DOMDataTreeListenerTest.class);
        executor = new DeadlockDetectingListeningExecutorService(commitExecutor,
                TransactionCommitDeadlockException.DEADLOCK_EXCEPTION_SUPPLIER, futureExecutor);
        domBroker = new SerializedDOMDataBroker(stores, executor);
    }

    @After
    public void tearDown() {
        if (executor != null) {
            executor.shutdownNow();
        }

        if (futureExecutor != null) {
            futureExecutor.shutdownNow();
        }
    }

    @Test
    public void writeContainerEmptyTreeTest() throws InterruptedException {
        final var latch = new CountDownLatch(1);

        final var dataTreeChangeService = getDataTreeChangeExtension();
        assertNotNull("DOMDataTreeChangeService not found, cannot continue with test!",
                dataTreeChangeService);

        final var listener = new TestDataTreeListener(latch);
        final var listenerReg = dataTreeChangeService.registerTreeChangeListener(ROOT_DATA_TREE_ID, listener);

        final var writeTx = domBroker.newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, TestModel.TEST_PATH, TEST_CONTAINER);
        writeTx.commit();

        latch.await(5, TimeUnit.SECONDS);

        assertEquals(1, listener.getReceivedChanges().size());
        final var changes = listener.getReceivedChanges().get(0);
        assertEquals(1, changes.size());

        final var candidate = changes.get(0);
        assertNotNull(candidate);
        final var candidateRoot = candidate.getRootNode();
        checkChange(null, TEST_CONTAINER, ModificationType.WRITE, candidateRoot);
        listenerReg.close();
    }

    @Test
    public void replaceContainerContainerInTreeTest() throws ExecutionException, InterruptedException {
        final var latch = new CountDownLatch(2);

        final var dataTreeChangeService = getDataTreeChangeExtension();
        assertNotNull("DOMDataTreeChangeService not found, cannot continue with test!",
                dataTreeChangeService);

        var writeTx = domBroker.newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, TestModel.TEST_PATH, TEST_CONTAINER);
        writeTx.commit().get();

        final var listener = new TestDataTreeListener(latch);
        final var listenerReg = dataTreeChangeService.registerTreeChangeListener(ROOT_DATA_TREE_ID, listener);
        writeTx = domBroker.newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, TestModel.TEST_PATH, TEST_CONTAINER_2);
        writeTx.commit();

        latch.await(5, TimeUnit.SECONDS);

        assertEquals(2, listener.getReceivedChanges().size());
        var changes = listener.getReceivedChanges().get(0);
        assertEquals(1, changes.size());

        var candidate = changes.get(0);
        assertNotNull(candidate);
        var candidateRoot = candidate.getRootNode();
        checkChange(null, TEST_CONTAINER, ModificationType.WRITE, candidateRoot);

        changes = listener.getReceivedChanges().get(1);
        assertEquals(1, changes.size());

        candidate = changes.get(0);
        assertNotNull(candidate);
        candidateRoot = candidate.getRootNode();
        checkChange(TEST_CONTAINER, TEST_CONTAINER_2, ModificationType.WRITE, candidateRoot);
        listenerReg.close();
    }

    @Test
    public void deleteContainerContainerInTreeTest() throws ExecutionException, InterruptedException {
        final var latch = new CountDownLatch(2);

        final var dataTreeChangeService = getDataTreeChangeExtension();
        assertNotNull("DOMDataTreeChangeService not found, cannot continue with test!", dataTreeChangeService);

        var writeTx = domBroker.newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, TestModel.TEST_PATH, TEST_CONTAINER);
        writeTx.commit().get();

        final var listener = new TestDataTreeListener(latch);
        final var listenerReg = dataTreeChangeService.registerTreeChangeListener(ROOT_DATA_TREE_ID, listener);

        writeTx = domBroker.newWriteOnlyTransaction();
        writeTx.delete(LogicalDatastoreType.CONFIGURATION, TestModel.TEST_PATH);
        writeTx.commit();

        latch.await(5, TimeUnit.SECONDS);

        assertEquals(2, listener.getReceivedChanges().size());
        var changes = listener.getReceivedChanges().get(0);
        assertEquals(1, changes.size());

        var candidate = changes.get(0);
        assertNotNull(candidate);
        var candidateRoot = candidate.getRootNode();
        checkChange(null, TEST_CONTAINER, ModificationType.WRITE, candidateRoot);

        changes = listener.getReceivedChanges().get(1);
        assertEquals(1, changes.size());

        candidate = changes.get(0);
        assertNotNull(candidate);
        candidateRoot = candidate.getRootNode();
        checkChange(TEST_CONTAINER, null, ModificationType.DELETE, candidateRoot);
        listenerReg.close();
    }

    @Test
    public void replaceChildListContainerInTreeTest() throws ExecutionException, InterruptedException {
        final var latch = new CountDownLatch(2);

        final var dataTreeChangeService = getDataTreeChangeExtension();
        assertNotNull("DOMDataTreeChangeService not found, cannot continue with test!", dataTreeChangeService);

        var writeTx = domBroker.newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, TestModel.TEST_PATH, TEST_CONTAINER);
        writeTx.commit().get();

        final var listener = new TestDataTreeListener(latch);
        final var listenerReg = dataTreeChangeService.registerTreeChangeListener(ROOT_DATA_TREE_ID, listener);

        writeTx = domBroker.newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, TestModel.OUTER_LIST_PATH, OUTER_LIST_2);
        writeTx.commit();

        latch.await(5, TimeUnit.SECONDS);

        assertEquals(2, listener.getReceivedChanges().size());
        var changes = listener.getReceivedChanges().get(0);
        assertEquals(1, changes.size());

        var candidate = changes.get(0);
        assertNotNull(candidate);
        var candidateRoot = candidate.getRootNode();
        checkChange(null, TEST_CONTAINER, ModificationType.WRITE, candidateRoot);

        changes = listener.getReceivedChanges().get(1);
        assertEquals(1, changes.size());

        candidate = changes.get(0);
        assertNotNull(candidate);
        candidateRoot = candidate.getRootNode();
        checkChange(TEST_CONTAINER, TEST_CONTAINER_2, ModificationType.SUBTREE_MODIFIED, candidateRoot);
        final var modifiedChild = candidateRoot.getModifiedChild(new NodeIdentifier(TestModel.OUTER_LIST_QNAME));
        checkChange(OUTER_LIST, OUTER_LIST_2, ModificationType.WRITE, modifiedChild);
        listenerReg.close();
    }

    @Test
    public void rootModificationChildListenerTest() throws ExecutionException, InterruptedException {
        final var latch = new CountDownLatch(2);

        final var dataTreeChangeService = getDataTreeChangeExtension();
        assertNotNull("DOMDataTreeChangeService not found, cannot continue with test!",
                dataTreeChangeService);

        var writeTx = domBroker.newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, TestModel.TEST_PATH, TEST_CONTAINER);
        writeTx.commit().get();

        final var listener = new TestDataTreeListener(latch);
        final var listenerReg = dataTreeChangeService.registerTreeChangeListener(OUTER_LIST_DATA_TREE_ID, listener);

        writeTx = domBroker.newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, TestModel.TEST_PATH, TEST_CONTAINER_2);
        writeTx.commit().get();

        latch.await(1, TimeUnit.SECONDS);

        assertEquals(2, listener.getReceivedChanges().size());
        var changes = listener.getReceivedChanges().get(0);
        assertEquals(1, changes.size());

        var candidate = changes.get(0);
        assertNotNull(candidate);
        var candidateRoot = candidate.getRootNode();
        checkChange(null, OUTER_LIST, ModificationType.WRITE, candidateRoot);

        changes = listener.getReceivedChanges().get(1);
        assertEquals(1, changes.size());

        candidate = changes.get(0);
        assertNotNull(candidate);
        candidateRoot = candidate.getRootNode();
        checkChange(OUTER_LIST, OUTER_LIST_2, ModificationType.WRITE, candidateRoot);
        listenerReg.close();
    }

    @Test
    public void listEntryChangeNonRootRegistrationTest() throws ExecutionException, InterruptedException {
        final var latch = new CountDownLatch(2);

        final var dataTreeChangeService = getDataTreeChangeExtension();
        assertNotNull("DOMDataTreeChangeService not found, cannot continue with test!", dataTreeChangeService);

        var writeTx = domBroker.newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, TestModel.TEST_PATH, TEST_CONTAINER);
        writeTx.commit().get();

        final var listener = new TestDataTreeListener(latch);
        final var listenerReg = dataTreeChangeService.registerTreeChangeListener(OUTER_LIST_DATA_TREE_ID, listener);

        final var outerListEntryId1 =
                NodeIdentifierWithPredicates.of(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, 1);
        final var outerListEntryId2 =
                NodeIdentifierWithPredicates.of(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, 2);
        final var outerListEntryId3 =
                NodeIdentifierWithPredicates.of(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, 3);

        final var outerListEntry1 = TestUtils.mapEntry(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, 1);
        final var outerListEntry2 = TestUtils.mapEntry(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, 2);
        final var outerListEntry3 = TestUtils.mapEntry(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, 3);

        final var listAfter = ImmutableNodes.newSystemMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.OUTER_LIST_QNAME))
            .withChild(outerListEntry2)
            .withChild(outerListEntry3)
            .build();

        writeTx = domBroker.newWriteOnlyTransaction();
        writeTx.delete(LogicalDatastoreType.CONFIGURATION, TestModel.OUTER_LIST_PATH.node(outerListEntryId1));
        writeTx.put(LogicalDatastoreType.CONFIGURATION, TestModel.OUTER_LIST_PATH.node(outerListEntryId2),
                outerListEntry2);
        writeTx.put(LogicalDatastoreType.CONFIGURATION, TestModel.OUTER_LIST_PATH.node(outerListEntryId3),
                outerListEntry3);
        writeTx.commit();

        latch.await(5, TimeUnit.SECONDS);

        assertEquals(2, listener.getReceivedChanges().size());
        var changes = listener.getReceivedChanges().get(0);
        assertEquals(1, changes.size());

        var candidate = changes.get(0);
        assertNotNull(candidate);
        var candidateRoot = candidate.getRootNode();
        checkChange(null, OUTER_LIST, ModificationType.WRITE, candidateRoot);

        changes = listener.getReceivedChanges().get(1);
        assertEquals(1, changes.size());

        candidate = changes.get(0);
        assertNotNull(candidate);
        candidateRoot = candidate.getRootNode();
        checkChange(OUTER_LIST, listAfter, ModificationType.SUBTREE_MODIFIED, candidateRoot);
        final var entry1Canditate = candidateRoot.getModifiedChild(outerListEntryId1);
        checkChange(outerListEntry1, null, ModificationType.DELETE, entry1Canditate);
        final var entry2Canditate = candidateRoot.getModifiedChild(outerListEntryId2);
        checkChange(null, outerListEntry2, ModificationType.WRITE, entry2Canditate);
        final var entry3Canditate = candidateRoot.getModifiedChild(outerListEntryId3);
        checkChange(null, outerListEntry3, ModificationType.WRITE, entry3Canditate);
        listenerReg.close();
    }

    private static void checkChange(final NormalizedNode expectedBefore, final NormalizedNode expectedAfter,
                                    final ModificationType expectedMod, final DataTreeCandidateNode candidateNode) {
        assertEquals(expectedBefore, candidateNode.dataBefore());
        assertEquals(expectedAfter, candidateNode.dataAfter());
        assertEquals(expectedMod, candidateNode.modificationType());
    }

    private DataTreeChangeExtension getDataTreeChangeExtension() {
        return domBroker.extension(DataTreeChangeExtension.class);
    }

    static class CommitExecutorService extends ForwardingExecutorService {

        ExecutorService delegate;

        CommitExecutorService(final ExecutorService delegate) {
            this.delegate = delegate;
        }

        @Override
        protected ExecutorService delegate() {
            return delegate;
        }
    }

    static class TestDataTreeListener implements DOMDataTreeChangeListener {
        private final List<List<DataTreeCandidate>> receivedChanges = new ArrayList<>();
        private final CountDownLatch latch;

        TestDataTreeListener(final CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onDataTreeChanged(final List<DataTreeCandidate> changes) {
            receivedChanges.add(changes);
            latch.countDown();
        }

        @Override
        public void onInitialData() {
            // noop
        }

        List<List<DataTreeCandidate>> getReceivedChanges() {
            return receivedChanges;
        }
    }
}
