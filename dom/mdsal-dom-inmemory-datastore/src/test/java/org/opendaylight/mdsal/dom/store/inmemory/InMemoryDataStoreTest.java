/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreReadTransaction;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreReadWriteTransaction;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreThreePhaseCommitCohort;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTransactionChain;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreWriteTransaction;
import org.opendaylight.mdsal.dom.spi.store.SnapshotBackedTransactions;
import org.opendaylight.mdsal.dom.spi.store.SnapshotBackedWriteTransaction.TransactionReadyPrototype;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeSnapshot;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

public class InMemoryDataStoreTest {
    private static EffectiveModelContext SCHEMA_CONTEXT;

    private InMemoryDOMDataStore domStore;

    @BeforeClass
    public static void beforeClass() {
        SCHEMA_CONTEXT = TestModel.createTestContext();
    }

    @AfterClass
    public static void afterClass() {
        SCHEMA_CONTEXT = null;
    }

    @Before
    public void setupStore() {
        domStore = new InMemoryDOMDataStore("TEST", MoreExecutors.newDirectExecutorService());
        domStore.onModelContextUpdated(SCHEMA_CONTEXT);
    }

    @Test
    public void testTransactionIsolation() throws Exception {
        DOMStoreReadTransaction readTx = domStore.newReadOnlyTransaction();
        assertNotNull(readTx);

        DOMStoreReadWriteTransaction writeTx = domStore.newReadWriteTransaction();
        assertNotNull(writeTx);

        /**
         * Writes /test in writeTx.
         */
        NormalizedNode testNode = ImmutableNodes.containerNode(TestModel.TEST_QNAME);
        writeTx.write(TestModel.TEST_PATH, testNode);

        /**
         * Reads /test from writeTx Read should return container.
         */
        assertEquals(Optional.of(testNode), Futures.getDone(writeTx.read(TestModel.TEST_PATH)));

        /**
         * Reads /test from readTx Read should return Absent.
         */
        assertEquals(Optional.empty(), Futures.getDone(readTx.read(TestModel.TEST_PATH)));
    }

    @Test
    public void testTransactionCommit() throws Exception {

        DOMStoreReadWriteTransaction writeTx = domStore.newReadWriteTransaction();
        assertNotNull(writeTx);

        /**
         * Writes /test in writeTx.
         */
        NormalizedNode testNode = ImmutableNodes.containerNode(TestModel.TEST_QNAME);
        writeTx.write(TestModel.TEST_PATH, testNode);

        /**
         * Reads /test from writeTx Read should return container.
         */
        assertEquals(Optional.of(testNode), Futures.getDone(writeTx.read(TestModel.TEST_PATH)));

        DOMStoreThreePhaseCommitCohort cohort = writeTx.ready();

        assertThreePhaseCommit(cohort);

        assertEquals(Optional.of(testNode),
            Futures.getDone(domStore.newReadOnlyTransaction().read(TestModel.TEST_PATH)));
    }

    @Test
    public void testDelete() throws Exception {

        DOMStoreWriteTransaction writeTx = domStore.newWriteOnlyTransaction();
        assertNotNull(writeTx);

        // Write /test and commit

        writeTx.write(TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME));

        assertThreePhaseCommit(writeTx.ready());

        assertTrue(Futures.getDone(domStore.newReadOnlyTransaction().read(TestModel.TEST_PATH)).isPresent());

        // Delete /test and verify

        writeTx = domStore.newWriteOnlyTransaction();

        writeTx.delete(TestModel.TEST_PATH);

        assertThreePhaseCommit(writeTx.ready());

        assertEquals(Optional.empty(), Futures.getDone(domStore.newReadOnlyTransaction().read(TestModel.TEST_PATH)));
    }

    @Test
    public void testMerge() throws Exception {

        DOMStoreWriteTransaction writeTx = domStore.newWriteOnlyTransaction();
        assertNotNull(writeTx);

        ContainerNode containerNode = Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
                .addChild(ImmutableNodes.mapNodeBuilder(TestModel.OUTER_LIST_QNAME)
                        .addChild(ImmutableNodes.mapEntry(TestModel.OUTER_LIST_QNAME,
                                                            TestModel.ID_QNAME, 1)).build()).build();

        writeTx.merge(TestModel.TEST_PATH, containerNode);

        assertThreePhaseCommit(writeTx.ready());

        assertEquals(Optional.of(containerNode),
            Futures.getDone(domStore.newReadOnlyTransaction().read(TestModel.TEST_PATH)));

        // Merge a new list entry node

        writeTx = domStore.newWriteOnlyTransaction();
        assertNotNull(writeTx);

        containerNode = Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .addChild(ImmutableNodes.mapNodeBuilder(TestModel.OUTER_LIST_QNAME)
                .addChild(ImmutableNodes.mapEntry(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, 1))
                .addChild(ImmutableNodes.mapEntry(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, 2))
                .build())
            .build();

        writeTx.merge(TestModel.TEST_PATH, containerNode);

        assertThreePhaseCommit(writeTx.ready());

        assertEquals(Optional.of(containerNode),
            Futures.getDone(domStore.newReadOnlyTransaction().read(TestModel.TEST_PATH)));
    }

    @Test
    public void testExistsForExistingData() throws Exception {

        DOMStoreReadWriteTransaction writeTx = domStore.newReadWriteTransaction();
        assertNotNull(writeTx);

        ContainerNode containerNode = Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .addChild(ImmutableNodes.mapNodeBuilder(TestModel.OUTER_LIST_QNAME)
                .addChild(ImmutableNodes.mapEntry(TestModel.OUTER_LIST_QNAME,
                    TestModel.ID_QNAME, 1)).build()).build();

        writeTx.merge(TestModel.TEST_PATH, containerNode);

        assertEquals(Boolean.TRUE, Futures.getDone(writeTx.exists(TestModel.TEST_PATH)));

        DOMStoreThreePhaseCommitCohort ready = writeTx.ready();

        ready.preCommit().get();

        ready.commit().get();

        DOMStoreReadTransaction readTx = domStore.newReadOnlyTransaction();
        assertNotNull(readTx);

        assertEquals(Boolean.TRUE, Futures.getDone(readTx.exists(TestModel.TEST_PATH)));
    }

    @Test
    public void testExistsForNonExistingData() throws Exception {

        DOMStoreReadWriteTransaction writeTx = domStore.newReadWriteTransaction();
        assertNotNull(writeTx);

        var exists = writeTx.exists(TestModel.TEST_PATH);

        assertEquals(Boolean.FALSE, exists.get());

        var readTx = domStore.newReadOnlyTransaction();
        assertNotNull(readTx);

        exists = readTx.exists(TestModel.TEST_PATH);

        assertEquals(Boolean.FALSE, exists.get());
    }

    @Test
    public void testExistsThrowsReadFailedException() {
        var readTx = domStore.newReadOnlyTransaction();
        assertNotNull(readTx);

        readTx.close();

        final var future = readTx.exists(TestModel.TEST_PATH);

        final var ex = assertThrows(ExecutionException.class, future::get).getCause();
        assertThat(ex, instanceOf(ReadFailedException.class));
    }


    @Test
    public void testReadWithReadOnlyTransactionClosed() {
        DOMStoreReadTransaction readTx = domStore.newReadOnlyTransaction();
        assertNotNull(readTx);

        readTx.close();

        assertReadThrows(readTx);
    }

    @Test
    public void testReadWithReadOnlyTransactionFailure() {
        DataTreeSnapshot mockSnapshot = mock(DataTreeSnapshot.class);
        doThrow(new RuntimeException("mock ex")).when(mockSnapshot)
            .readNode(any(YangInstanceIdentifier.class));

        DOMStoreReadTransaction readTx = SnapshotBackedTransactions.newReadTransaction("1", true, mockSnapshot);

        assertReadThrows(readTx);
    }

    @Test
    public void testReadWithReadWriteTransactionClosed() {

        DOMStoreReadTransaction readTx = domStore.newReadWriteTransaction();
        assertNotNull(readTx);

        readTx.close();

        assertReadThrows(readTx);
    }

    @Test
    public void testReadWithReadWriteTransactionFailure() {
        DataTreeSnapshot mockSnapshot = mock(DataTreeSnapshot.class);
        DataTreeModification mockModification = mock(DataTreeModification.class);
        doThrow(new RuntimeException("mock ex")).when(mockModification)
            .readNode(any(YangInstanceIdentifier.class));
        doReturn(mockModification).when(mockSnapshot).newModification();
        @SuppressWarnings("unchecked")
        TransactionReadyPrototype<String> mockReady = mock(TransactionReadyPrototype.class);
        DOMStoreReadTransaction readTx = SnapshotBackedTransactions.newReadWriteTransaction(
                "1", false, mockSnapshot, mockReady);

        assertReadThrows(readTx);
    }

    private static void assertReadThrows(final DOMStoreReadTransaction readTx) {
        final var future = readTx.read(TestModel.TEST_PATH);
        final var cause = assertThrows(ExecutionException.class, future::get).getCause();
        assertThat(cause, instanceOf(ReadFailedException.class));
    }

    @Test
    public void testWriteWithTransactionReady() {
        var writeTx = domStore.newWriteOnlyTransaction();
        writeTx.ready();

        // Should throw ex
        assertThrows(IllegalStateException.class,
            () -> writeTx.write(TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME)));
    }

    @Test
    public void testReadyWithTransactionAlreadyReady() {
        var writeTx = domStore.newWriteOnlyTransaction();

        writeTx.ready();

        // Should throw ex
        assertThrows(IllegalStateException.class, writeTx::ready);
    }

    @Test
    public void testReadyWithMissingMandatoryData() throws Exception {
        DOMStoreWriteTransaction writeTx = domStore.newWriteOnlyTransaction();
        NormalizedNode testNode = Builders.containerBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.MANDATORY_DATA_TEST_QNAME))
                .addChild(ImmutableNodes.leafNode(TestModel.OPTIONAL_QNAME, "data"))
                .build();
        writeTx.write(TestModel.MANDATORY_DATA_TEST_PATH, testNode);
        DOMStoreThreePhaseCommitCohort ready = writeTx.ready();

        final var future = ready.canCommit();
        final var cause = assertThrows(ExecutionException.class, future::get).getCause();
        assertThat(cause, instanceOf(IllegalArgumentException.class));
        assertThat(cause.getMessage(), containsString("mandatory-data-test is missing mandatory descendant"));
    }

    @Test
    public void testTransactionAbort() throws Exception {

        DOMStoreReadWriteTransaction writeTx = domStore.newReadWriteTransaction();
        assertNotNull(writeTx);

        assertTestContainerWrite(writeTx);

        DOMStoreThreePhaseCommitCohort cohort = writeTx.ready();

        assertTrue(cohort.canCommit().get());
        cohort.preCommit().get();
        cohort.abort().get();

        assertEquals(Optional.empty(), domStore.newReadOnlyTransaction().read(TestModel.TEST_PATH).get());
    }

    @Test
    public void testTransactionChain() throws Exception {
        DOMStoreTransactionChain txChain = domStore.createTransactionChain();
        assertNotNull(txChain);

        /**
         * We alocate new read-write transaction and write /test.
         */
        DOMStoreReadWriteTransaction firstTx = txChain.newReadWriteTransaction();
        assertTestContainerWrite(firstTx);

        /**
         * First transaction is marked as ready, we are able to allocate chained
         * transactions.
         */
        final DOMStoreThreePhaseCommitCohort firstWriteTxCohort = firstTx.ready();

        /**
         * We alocate chained transaction - read transaction, note first one is
         * still not commited to datastore.
         */
        DOMStoreReadTransaction secondReadTx = txChain.newReadOnlyTransaction();

        /**
         * We test if we are able to read data from tx, read should not fail
         * since we are using chained transaction.
         */
        assertTestContainerExists(secondReadTx);

        /**
         * We alocate next transaction, which is still based on first one, but
         * is read-write.
         */
        DOMStoreReadWriteTransaction thirdDeleteTx = txChain.newReadWriteTransaction();

        /**
         * We test existence of /test in third transaction container should
         * still be visible from first one (which is still uncommmited).
         */
        assertTestContainerExists(thirdDeleteTx);

        /**
         * We delete node in third transaction.
         */
        thirdDeleteTx.delete(TestModel.TEST_PATH);

        /**
         * third transaction is sealed.
         */
        DOMStoreThreePhaseCommitCohort thirdDeleteTxCohort = thirdDeleteTx.ready();

        /**
         * We commit first transaction.
         *
         */
        assertThreePhaseCommit(firstWriteTxCohort);

        // Alocates store transacion
        DOMStoreReadTransaction storeReadTx = domStore.newReadOnlyTransaction();
        /**
         * We verify transaction is commited to store, container should exists
         * in datastore.
         */
        assertTestContainerExists(storeReadTx);
        /**
         * We commit third transaction
         *
         */
        assertThreePhaseCommit(thirdDeleteTxCohort);
    }

    @Test
    @Ignore
    public void testTransactionConflict() throws Exception {
        DOMStoreReadWriteTransaction txOne = domStore.newReadWriteTransaction();
        DOMStoreReadWriteTransaction txTwo = domStore.newReadWriteTransaction();
        assertTestContainerWrite(txOne);
        assertTestContainerWrite(txTwo);

        /**
         * Commits transaction
         */
        assertThreePhaseCommit(txOne.ready());

        /**
         * Asserts that txTwo could not be commited
         */
        assertFalse(txTwo.ready().canCommit().get());
    }

    private static void assertThreePhaseCommit(final DOMStoreThreePhaseCommitCohort cohort) throws Exception {
        assertTrue(cohort.canCommit().get());
        cohort.preCommit().get();
        cohort.commit().get();
    }

    private static Optional<NormalizedNode> assertTestContainerWrite(final DOMStoreReadWriteTransaction writeTx)
            throws Exception {
        /**
         *
         * Writes /test in writeTx
         *
         */
        writeTx.write(TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME));

        return assertTestContainerExists(writeTx);
    }

    /**
     * Reads /test from readTx Read should return container.
     */
    private static Optional<NormalizedNode> assertTestContainerExists(final DOMStoreReadTransaction readTx)
            throws Exception {

        ListenableFuture<Optional<NormalizedNode>> writeTxContainer = readTx.read(TestModel.TEST_PATH);
        assertTrue(writeTxContainer.get().isPresent());
        return writeTxContainer.get();
    }
}
