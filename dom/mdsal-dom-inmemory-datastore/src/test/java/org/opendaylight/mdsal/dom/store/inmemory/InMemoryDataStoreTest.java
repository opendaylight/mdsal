/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreReadTransaction;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreReadWriteTransaction;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreThreePhaseCommitCohort;
import org.opendaylight.mdsal.dom.spi.store.SnapshotBackedTransactions;
import org.opendaylight.mdsal.dom.spi.store.SnapshotBackedWriteTransaction.TransactionReadyPrototype;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeSnapshot;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

@ExtendWith(MockitoExtension.class)
class InMemoryDataStoreTest {
    private static EffectiveModelContext SCHEMA_CONTEXT;

    @Mock
    private DataTreeSnapshot mockSnapshot;
    @Mock
    private DataTreeModification mockModification;
    @Mock
    private TransactionReadyPrototype<String> mockReady;

    private InMemoryDOMDataStore domStore;

    @BeforeAll
    static void beforeAll() {
        SCHEMA_CONTEXT = TestModel.createTestContext();
    }

    @AfterAll
    static void afterAll() {
        SCHEMA_CONTEXT = null;
    }

    @BeforeEach
    void beforeEach() {
        domStore = new InMemoryDOMDataStore("TEST", MoreExecutors.newDirectExecutorService());
        domStore.onModelContextUpdated(SCHEMA_CONTEXT);
    }

    @AfterEach
    void afterEach() {
        domStore.close();
    }

    @Test
    void testTransactionIsolation() throws Exception {
        final var readTx = domStore.newReadOnlyTransaction();
        assertNotNull(readTx);

        final var writeTx = domStore.newReadWriteTransaction();
        assertNotNull(writeTx);

        /**
         * Writes /test in writeTx.
         */
        final var testNode = testContainer();
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

    private static ContainerNode testContainer() {
        return ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .build();
    }

    @Test
    void testTransactionCommit() throws Exception {
        final var writeTx = domStore.newReadWriteTransaction();
        assertNotNull(writeTx);

        /**
         * Writes /test in writeTx.
         */
        var testNode = testContainer();
        writeTx.write(TestModel.TEST_PATH, testNode);

        /**
         * Reads /test from writeTx Read should return container.
         */
        assertEquals(Optional.of(testNode), Futures.getDone(writeTx.read(TestModel.TEST_PATH)));

        final var cohort = writeTx.ready();

        assertThreePhaseCommit(cohort);

        assertEquals(Optional.of(testNode),
            Futures.getDone(domStore.newReadOnlyTransaction().read(TestModel.TEST_PATH)));
    }

    @Test
    void testDelete() throws Exception {
        var writeTx = domStore.newWriteOnlyTransaction();
        assertNotNull(writeTx);

        // Write /test and commit

        writeTx.write(TestModel.TEST_PATH, testContainer());

        assertThreePhaseCommit(writeTx.ready());

        assertTrue(Futures.getDone(domStore.newReadOnlyTransaction().read(TestModel.TEST_PATH)).isPresent());

        // Delete /test and verify

        writeTx = domStore.newWriteOnlyTransaction();

        writeTx.delete(TestModel.TEST_PATH);

        assertThreePhaseCommit(writeTx.ready());

        assertEquals(Optional.empty(), Futures.getDone(domStore.newReadOnlyTransaction().read(TestModel.TEST_PATH)));
    }

    @Test
    void testMerge() throws Exception {
        var writeTx = domStore.newWriteOnlyTransaction();
        assertNotNull(writeTx);

        var containerNode = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .addChild(ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.OUTER_LIST_QNAME))
                .addChild(ImmutableNodes.newMapEntryBuilder()
                    .withNodeIdentifier(NodeIdentifierWithPredicates.of(TestModel.OUTER_LIST_QNAME,
                        TestModel.ID_QNAME, 1))
                    .withChild(ImmutableNodes.leafNode(TestModel.ID_QNAME, 1))
                    .build())
                .build())
            .build();

        writeTx.merge(TestModel.TEST_PATH, containerNode);

        assertThreePhaseCommit(writeTx.ready());

        assertEquals(Optional.of(containerNode),
            Futures.getDone(domStore.newReadOnlyTransaction().read(TestModel.TEST_PATH)));

        // Merge a new list entry node

        writeTx = domStore.newWriteOnlyTransaction();
        assertNotNull(writeTx);

        containerNode = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .addChild(ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.OUTER_LIST_QNAME))
                .addChild(ImmutableNodes.newMapEntryBuilder()
                    .withNodeIdentifier(NodeIdentifierWithPredicates.of(TestModel.OUTER_LIST_QNAME,
                        TestModel.ID_QNAME, 1))
                    .withChild(ImmutableNodes.leafNode(TestModel.ID_QNAME, 1))
                    .build())
                .addChild(ImmutableNodes.newMapEntryBuilder()
                    .withNodeIdentifier(NodeIdentifierWithPredicates.of(TestModel.OUTER_LIST_QNAME,
                        TestModel.ID_QNAME, 2))
                    .withChild(ImmutableNodes.leafNode(TestModel.ID_QNAME, 2))
                    .build())
                .build())
            .build();

        writeTx.merge(TestModel.TEST_PATH, containerNode);

        assertThreePhaseCommit(writeTx.ready());

        assertEquals(Optional.of(containerNode),
            Futures.getDone(domStore.newReadOnlyTransaction().read(TestModel.TEST_PATH)));
    }

    @Test
    void testExistsForExistingData() throws Exception {
        final var writeTx = domStore.newReadWriteTransaction();
        assertNotNull(writeTx);

        final var containerNode = ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .addChild(ImmutableNodes.newSystemMapBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.OUTER_LIST_QNAME))
                .addChild(ImmutableNodes.newMapEntryBuilder()
                    .withNodeIdentifier(NodeIdentifierWithPredicates.of(TestModel.OUTER_LIST_QNAME,
                        TestModel.ID_QNAME, 1))
                    .withChild(ImmutableNodes.leafNode(TestModel.ID_QNAME, 1))
                    .build())
                .build())
            .build();

        writeTx.merge(TestModel.TEST_PATH, containerNode);

        assertEquals(Boolean.TRUE, Futures.getDone(writeTx.exists(TestModel.TEST_PATH)));

        final var ready = writeTx.ready();

        ready.preCommit().get();

        ready.commit().get();

        final var readTx = domStore.newReadOnlyTransaction();
        assertNotNull(readTx);

        assertEquals(Boolean.TRUE, Futures.getDone(readTx.exists(TestModel.TEST_PATH)));
    }

    @Test
    void testExistsForNonExistingData() throws Exception {
        final var writeTx = domStore.newReadWriteTransaction();
        assertNotNull(writeTx);

        var exists = writeTx.exists(TestModel.TEST_PATH);

        assertEquals(Boolean.FALSE, exists.get());

        final var readTx = domStore.newReadOnlyTransaction();
        assertNotNull(readTx);

        exists = readTx.exists(TestModel.TEST_PATH);

        assertEquals(Boolean.FALSE, exists.get());
    }

    @Test
    void testExistsThrowsReadFailedException() {
        final var readTx = domStore.newReadOnlyTransaction();
        assertNotNull(readTx);

        readTx.close();

        final var future = readTx.exists(TestModel.TEST_PATH);

        final var ee = assertThrows(ExecutionException.class, () -> Futures.getDone(future));
        final var rfe = assertInstanceOf(ReadFailedException.class, ee.getCause());
        assertEquals("", rfe.getMessage());
    }


    @Test
    void testReadWithReadOnlyTransactionClosed() {
        final var readTx = domStore.newReadOnlyTransaction();
        assertNotNull(readTx);

        readTx.close();

        assertReadThrows(readTx);
    }

    @Test
    void testReadWithReadOnlyTransactionFailure() {
        doThrow(new RuntimeException("mock ex")).when(mockSnapshot).readNode(any(YangInstanceIdentifier.class));

        assertReadThrows(SnapshotBackedTransactions.newReadTransaction("1", true, mockSnapshot));
    }

    @Test
    void testReadWithReadWriteTransactionClosed() {
        final var readTx = domStore.newReadWriteTransaction();
        assertNotNull(readTx);

        readTx.close();

        assertReadThrows(readTx);
    }

    @Test
    void testReadWithReadWriteTransactionFailure() {
        doThrow(new RuntimeException("mock ex")).when(mockModification).readNode(any(YangInstanceIdentifier.class));
        doReturn(mockModification).when(mockSnapshot).newModification();
        final var readTx = SnapshotBackedTransactions.newReadWriteTransaction("1", false, mockSnapshot, mockReady);
        assertReadThrows(readTx);
    }

    private static void assertReadThrows(final DOMStoreReadTransaction readTx) {
        final var future = readTx.read(TestModel.TEST_PATH);
        final var ee = assertThrows(ExecutionException.class, () -> Futures.getDone(future));
        assertInstanceOf(ReadFailedException.class, ee.getCause());
    }

    @Test
    public void testWriteWithTransactionReady() {
        var writeTx = domStore.newWriteOnlyTransaction();
        writeTx.ready();

        // Should throw ex
        assertThrows(IllegalStateException.class, () -> writeTx.write(TestModel.TEST_PATH, testContainer()));
    }

    @Test
    void testReadyWithTransactionAlreadyReady() {
        var writeTx = domStore.newWriteOnlyTransaction();

        writeTx.ready();

        // Should throw ex
        assertThrows(IllegalStateException.class, writeTx::ready);
    }

    @Test
    void testReadyWithMissingMandatoryData() {
        final var writeTx = domStore.newWriteOnlyTransaction();
        var testNode = ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.MANDATORY_DATA_TEST_QNAME))
                .addChild(ImmutableNodes.leafNode(TestModel.OPTIONAL_QNAME, "data"))
                .build();
        writeTx.write(TestModel.MANDATORY_DATA_TEST_PATH, testNode);
        final var ready = writeTx.ready();

        final var future = ready.canCommit();
        final var ee = assertThrows(ExecutionException.class, () -> Futures.getDone(future));
        final var cause = assertInstanceOf(IllegalArgumentException.class, ee.getCause());
        assertThat(cause.getMessage()).contains("mandatory-data-test is missing mandatory descendant");
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
    void testTransactionChain() throws Exception {
        final var txChain = domStore.createTransactionChain();
        assertNotNull(txChain);

        /**
         * We alocate new read-write transaction and write /test.
         */
        final var firstTx = txChain.newReadWriteTransaction();
        assertTestContainerWrite(firstTx);

        /**
         * First transaction is marked as ready, we are able to allocate chained
         * transactions.
         */
        final var firstWriteTxCohort = firstTx.ready();

        /**
         * We alocate chained transaction - read transaction, note first one is
         * still not commited to datastore.
         */
        final var secondReadTx = txChain.newReadOnlyTransaction();

        /**
         * We test if we are able to read data from tx, read should not fail
         * since we are using chained transaction.
         */
        assertTestContainerExists(secondReadTx);

        /**
         * We alocate next transaction, which is still based on first one, but
         * is read-write.
         */
        final var thirdDeleteTx = txChain.newReadWriteTransaction();

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
        final var thirdDeleteTxCohort = thirdDeleteTx.ready();

        /**
         * We commit first transaction.
         *
         */
        assertThreePhaseCommit(firstWriteTxCohort);

        // Allocates store transaction
        final var storeReadTx = domStore.newReadOnlyTransaction();
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
    @Disabled
    void testTransactionConflict() throws Exception {
        final var txOne = domStore.newReadWriteTransaction();
        final var txTwo = domStore.newReadWriteTransaction();
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
        writeTx.write(TestModel.TEST_PATH, testContainer());

        return assertTestContainerExists(writeTx);
    }

    /**
     * Reads /test from readTx Read should return container.
     */
    private static Optional<NormalizedNode> assertTestContainerExists(final DOMStoreReadTransaction readTx)
            throws Exception {
        final var writeTxContainer = readTx.read(TestModel.TEST_PATH);
        assertTrue(writeTxContainer.get().isPresent());
        return writeTxContainer.get();
    }
}
