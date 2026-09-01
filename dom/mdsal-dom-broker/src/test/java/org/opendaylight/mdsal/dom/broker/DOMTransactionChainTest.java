/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.CONFIGURATION;
import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.OPERATIONAL;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;
import org.opendaylight.mdsal.dom.spi.AbstractDOMDataBroker;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMStore;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;

public class DOMTransactionChainTest extends AbstractDatastoreTest {
    private InMemoryDOMStore configStore;
    private InMemoryDOMStore operStore;
    private AbstractDOMDataBroker domBroker;
    private ExecutorService executor;

    @Before
    public void before() {
        configStore = newDOMStore(CONFIGURATION);
        operStore = newDOMStore(OPERATIONAL);
        executor = Executors.newSingleThreadExecutor();
        domBroker = new SerializedDOMDataBroker(Map.of(CONFIGURATION, configStore, OPERATIONAL, operStore), executor);
    }

    @After
    public void after() {
        if (executor != null) {
            executor.shutdownNow();
        }
        if (operStore != null) {
            operStore.close();
        }
        if (configStore != null) {
            configStore.close();
        }
    }

    @Test
    public void testTransactionChainNoConflict() throws Exception {
        final DOMTransactionChain txChain = domBroker.createTransactionChain();
        assertNotNull(txChain);

        /**
         * We allocate new read-write transaction and write /test.
         */
        final DOMDataTreeWriteTransaction firstTx = allocateAndWrite(txChain);

        /**
         * First transaction is marked as ready, we are able to allocate chained
         * transactions.
         */
        final ListenableFuture<? extends CommitInfo> firstWriteTxFuture = firstTx.commit();

        /**
         * We allocate chained transaction - read transaction.
         */
        final DOMDataTreeReadTransaction secondReadTx = txChain.newReadOnlyTransaction();

        /**
         *
         * We test if we are able to read data from tx, read should not fail
         * since we are using chained transaction.
         *
         *
         */
        assertTestContainerExists(secondReadTx);

        /**
         * We allocate next transaction, which is still based on first one, but
         * is read-write.
         *
         */
        final DOMDataTreeWriteTransaction thirdDeleteTx = allocateAndDelete(txChain);

        /**
         * We commit first transaction
         *
         */
        assertCommitSuccessful(firstWriteTxFuture);

        /**
         * Allocates transaction from data store.
         *
         */
        final DOMDataTreeReadTransaction storeReadTx = domBroker.newReadOnlyTransaction();

        /**
         * We verify transaction is committed to store, container should exist
         * in datastore.
         */
        assertTestContainerExists(storeReadTx);

        /**
         * third transaction is sealed and committed.
         */
        assertCommitSuccessful(thirdDeleteTx.commit());

        /**
         * We close transaction chain.
         */
        txChain.close();

        txChain.future().get(1, TimeUnit.SECONDS);
    }

    @Test
    public void testTransactionChainNotSealed() {
        final var txChain = domBroker.createTransactionChain();
        assertNotNull(txChain);

        /**
         * We allocate new read-write transaction and write /test
         */
        allocateAndWrite(txChain);

        /**
         * We allocate chained transaction - read transaction, note first one is
         * still not committed to datastore, so this allocation should fail with
         * IllegalStateException.
         */
        // actual backing tx allocation happens on put
        final var ex = assertThrows(IllegalStateException.class, () -> allocateAndWrite(txChain));
        assertEquals("Previous transaction OPER-0 is not ready yet", ex.getMessage());
    }

    private static DOMDataTreeWriteTransaction allocateAndDelete(final DOMTransactionChain txChain) {
        final DOMDataTreeWriteTransaction tx = txChain.newWriteOnlyTransaction();
        /**
         * We delete node in third transaction
         */
        tx.delete(LogicalDatastoreType.OPERATIONAL, TestModel.TEST_PATH);
        return tx;
    }

    private static DOMDataTreeWriteTransaction allocateAndWrite(final DOMTransactionChain txChain) {
        final var tx = txChain.newWriteOnlyTransaction();
        writeTestContainer(tx);
        return tx;
    }

    private static void assertCommitSuccessful(final ListenableFuture<? extends CommitInfo> firstWriteTxFuture) {
        try {
            firstWriteTxFuture.get(1, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new AssertionError(e);
        }
    }

    private static void assertTestContainerExists(final DOMDataTreeReadTransaction readTx) {
        final Optional<NormalizedNode> readedData;
        try {
            readedData = readTx.read(OPERATIONAL, TestModel.TEST_PATH).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new AssertionError(e);
        }
        assertTrue(readedData.isPresent());
    }

    private static void writeTestContainer(final DOMDataTreeWriteTransaction tx) {
        tx.put(OPERATIONAL, TestModel.TEST_PATH, ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .build());
    }
}
