/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.CONFIGURATION;
import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.OPERATIONAL;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.mdsal.common.api.TransactionCommitDeadlockException;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.spi.AbstractDOMDataBroker;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMDataStore;
import org.opendaylight.yangtools.util.concurrent.DeadlockDetectingListeningExecutorService;
import org.opendaylight.yangtools.util.concurrent.SpecialExecutors;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;

public class DOMBrokerTest extends AbstractDatastoreTest {
    private InMemoryDOMDataStore configStore;
    private InMemoryDOMDataStore operStore;
    private AbstractDOMDataBroker domBroker;
    private ExecutorService executor;
    private ExecutorService futureExecutor;
    private CommitExecutorService commitExecutor;

    @Before
    public void before() {
        configStore = newDOMStore(CONFIGURATION);
        operStore = newDOMStore(OPERATIONAL);
        commitExecutor = new CommitExecutorService();
        futureExecutor = SpecialExecutors.newBlockingBoundedCachedThreadPool(1, 5, "FCB", DOMBrokerTest.class);
        executor = new DeadlockDetectingListeningExecutorService(commitExecutor,
                TransactionCommitDeadlockException.DEADLOCK_EXCEPTION_SUPPLIER, futureExecutor);
        domBroker = new SerializedDOMDataBroker(Map.of(CONFIGURATION, configStore, OPERATIONAL, operStore), executor);
    }

    @After
    public void after() {
        if (executor != null) {
            executor.shutdownNow();
        }
        if (futureExecutor != null) {
            futureExecutor.shutdownNow();
        }
        if (operStore != null) {
            operStore.close();
        }
        if (configStore != null) {
            configStore.close();
        }
    }

    @Test(timeout = 10000)
    public void testTransactionIsolation() throws InterruptedException, ExecutionException {
        assertNotNull(domBroker);

        final DOMDataTreeReadTransaction readTx = domBroker.newReadOnlyTransaction();
        assertNotNull(readTx);

        final DOMDataTreeWriteTransaction writeTx = domBroker.newWriteOnlyTransaction();
        assertNotNull(writeTx);

        /**
         * Writes /test in writeTx.
         *
         */
        writeTx.put(OPERATIONAL, TestModel.TEST_PATH, ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .build());

        /**
         * Reads /test from readTx Read should return Absent.
         *
         */
        final var readTxContainer = readTx.read(OPERATIONAL, TestModel.TEST_PATH);
        assertEquals(Optional.empty(), readTxContainer.get());
    }

    @Test(timeout = 10000)
    public void testTransactionCommit() throws InterruptedException, ExecutionException {
        final DOMDataTreeWriteTransaction writeTx = domBroker.newWriteOnlyTransaction();
        assertNotNull(writeTx);
        /**
         * Writes /test in writeTx
         *
         */
        writeTx.put(OPERATIONAL, TestModel.TEST_PATH, ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .build());

        writeTx.commit().get();

        final var afterCommitRead = domBroker.newReadOnlyTransaction().read(OPERATIONAL, TestModel.TEST_PATH).get();
        assertTrue(afterCommitRead.isPresent());
    }

    // FIXME: split this test into its own class
    @Test
    public void testRejectedCommit() throws Exception {
        commitExecutor.delegate = mock(ExecutorService.class);
        final var thrown = new RejectedExecutionException("mock");

        doThrow(thrown).when(commitExecutor.delegate).execute(any(Runnable.class));
        doNothing().when(commitExecutor.delegate).shutdown();
        doReturn(List.of()).when(commitExecutor.delegate).shutdownNow();
        doReturn("").when(commitExecutor.delegate).toString();
        doReturn(Boolean.TRUE).when(commitExecutor.delegate).awaitTermination(anyLong(), any(TimeUnit.class));

        final var writeTx = domBroker.newWriteOnlyTransaction();
        writeTx.put(OPERATIONAL, TestModel.TEST_PATH, ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .build());

        final var ee = assertThrows(ExecutionException.class, () -> writeTx.commit().get(5, TimeUnit.SECONDS));
        final var ex = assertInstanceOf(TransactionCommitFailedException.class, ee.getCause());
        assertEquals("Could not submit the commit task - the commit queue capacity has been exceeded.",
            ex.getMessage());
        assertSame(thrown, ex.getCause());
    }

    @Test
    public void basicTests() throws Exception {
        final DataContainerChild outerList = ImmutableNodes.newSystemMapBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.OUTER_LIST_QNAME))
            .withChild(TestUtils.mapEntry(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, 1))
            .build();
        final ContainerNode testContainer = ImmutableNodes.newContainerBuilder()
                .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
                .withChild(outerList)
                .build();

        DOMDataTreeWriteTransaction writeTx = domBroker.newWriteOnlyTransaction();
        final DOMDataTreeReadTransaction readRx = domBroker.newReadOnlyTransaction();
        assertNotNull(writeTx);
        assertNotNull(readRx);
        assertNotNull(((SerializedDOMDataBroker) domBroker).getCommitStatsTracker());

        writeTx.put(OPERATIONAL, TestModel.TEST_PATH, ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .build());
        writeTx.commit().get();
        assertFalse(writeTx.cancel());

        assertFalse(domBroker.newReadOnlyTransaction().exists(CONFIGURATION, TestModel.TEST_PATH).get());
        assertTrue(domBroker.newReadOnlyTransaction().exists(OPERATIONAL, TestModel.TEST_PATH).get());
        assertFalse(domBroker.newReadOnlyTransaction().exists(OPERATIONAL, TestModel.TEST2_PATH).get());

        writeTx = domBroker.newWriteOnlyTransaction();
        writeTx.put(OPERATIONAL, TestModel.TEST_PATH, ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .build());
        writeTx.delete(OPERATIONAL, TestModel.TEST_PATH);
        writeTx.commit().get();
        assertFalse(domBroker.newReadOnlyTransaction().exists(OPERATIONAL, TestModel.TEST_PATH).get());
        assertTrue(domBroker.newWriteOnlyTransaction().cancel());

        writeTx = domBroker.newWriteOnlyTransaction();
        writeTx.put(OPERATIONAL, TestModel.TEST_PATH, ImmutableNodes.newContainerBuilder()
            .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
            .build());
        writeTx.merge(OPERATIONAL, TestModel.TEST_PATH, testContainer);
        writeTx.commit().get();
        assertTrue(domBroker.newReadOnlyTransaction().exists(OPERATIONAL, TestModel.TEST_PATH).get());
        assertTrue(domBroker.newReadOnlyTransaction().read(OPERATIONAL, TestModel.TEST_PATH).get()
                 .orElseThrow().toString().contains(testContainer.toString()));

        readRx.read(OPERATIONAL, TestModel.TEST_PATH).get(); // init backing tx before close
        readRx.close();

        final var ee = assertThrows(ExecutionException.class,
            () -> readRx.read(OPERATIONAL, TestModel.TEST_PATH).get());
        final var cause = assertInstanceOf(ReadFailedException.class, ee.getCause());
        assertEquals("Transaction is closed", cause.getMessage());
    }
}
