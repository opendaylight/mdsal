/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.CONFIGURATION;
import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.OPERATIONAL;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ForwardingExecutorService;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.mdsal.common.api.TransactionCommitDeadlockException;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.spi.store.DOMStore;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMDataStore;
import org.opendaylight.yangtools.util.concurrent.DeadlockDetectingListeningExecutorService;
import org.opendaylight.yangtools.util.concurrent.SpecialExecutors;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;

public class DOMBrokerTest extends AbstractDatastoreTest {

    private AbstractDOMDataBroker domBroker;
    private ListeningExecutorService executor;
    private ExecutorService futureExecutor;
    private CommitExecutorService commitExecutor;

    @Before
    public void setupStore() {
        final InMemoryDOMDataStore operStore = new InMemoryDOMDataStore("OPER",
                MoreExecutors.newDirectExecutorService());
        final InMemoryDOMDataStore configStore = new InMemoryDOMDataStore("CFG",
                MoreExecutors.newDirectExecutorService());

        operStore.onModelContextUpdated(SCHEMA_CONTEXT);
        configStore.onModelContextUpdated(SCHEMA_CONTEXT);

        final ImmutableMap<LogicalDatastoreType, DOMStore> stores =
                ImmutableMap.<LogicalDatastoreType, DOMStore>builder()
                .put(CONFIGURATION, configStore)
                .put(OPERATIONAL, operStore)
                .build();

        commitExecutor = new CommitExecutorService(Executors.newSingleThreadExecutor());
        futureExecutor = SpecialExecutors.newBlockingBoundedCachedThreadPool(1, 5, "FCB", DOMBrokerTest.class);
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
        writeTx.put(OPERATIONAL, TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME));

        /**
         * Reads /test from readTx Read should return Absent.
         *
         */
        final ListenableFuture<Optional<NormalizedNode>> readTxContainer = readTx.read(OPERATIONAL,
            TestModel.TEST_PATH);
        assertFalse(readTxContainer.get().isPresent());
    }

    @Test(timeout = 10000)
    public void testTransactionCommit() throws InterruptedException, ExecutionException {
        final DOMDataTreeWriteTransaction writeTx = domBroker.newWriteOnlyTransaction();
        assertNotNull(writeTx);
        /**
         * Writes /test in writeTx
         *
         */
        writeTx.put(OPERATIONAL, TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME));

        writeTx.commit().get();

        final Optional<NormalizedNode> afterCommitRead = domBroker.newReadOnlyTransaction()
                .read(OPERATIONAL, TestModel.TEST_PATH).get();
        assertTrue(afterCommitRead.isPresent());
    }

    @Test(expected = TransactionCommitFailedException.class)
    @SuppressWarnings({"checkstyle:AvoidHidingCauseException", "checkstyle:IllegalThrows"})
    public void testRejectedCommit() throws Throwable {
        commitExecutor.delegate = Mockito.mock(ExecutorService.class);
        Mockito.doThrow(new RejectedExecutionException("mock"))
            .when(commitExecutor.delegate).execute(Mockito.any(Runnable.class));
        Mockito.doNothing().when(commitExecutor.delegate).shutdown();
        Mockito.doReturn(Collections.emptyList()).when(commitExecutor.delegate).shutdownNow();
        Mockito.doReturn("").when(commitExecutor.delegate).toString();
        Mockito.doReturn(Boolean.TRUE).when(commitExecutor.delegate)
            .awaitTermination(Mockito.anyLong(), Mockito.any(TimeUnit.class));

        final DOMDataTreeWriteTransaction writeTx = domBroker.newWriteOnlyTransaction();
        writeTx.put(OPERATIONAL, TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME));

        try {
            writeTx.commit().get(5, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    AtomicReference<Throwable> submitTxAsync(final DOMDataTreeWriteTransaction writeTx) {
        final AtomicReference<Throwable> caughtEx = new AtomicReference<>();
        new Thread(() -> {
            try {
                writeTx.commit();
            } catch (final Throwable e) {
                caughtEx.set(e);
            }
        }).start();

        return caughtEx;
    }

    @Test(expected = ReadFailedException.class)
    @SuppressWarnings({"checkstyle:IllegalThrows", "checkstyle:AvoidHidingCauseException"})
    public void basicTests() throws Throwable {
        final DataContainerChild outerList = ImmutableNodes.mapNodeBuilder(TestModel.OUTER_LIST_QNAME)
                .withChild(ImmutableNodes.mapEntry(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, 1))
                .build();
        final NormalizedNode testContainer = Builders.containerBuilder()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(TestModel.TEST_QNAME))
                .withChild(outerList)
                .build();

        DOMDataTreeWriteTransaction writeTx = domBroker.newWriteOnlyTransaction();
        final DOMDataTreeReadTransaction readRx = domBroker.newReadOnlyTransaction();
        assertNotNull(writeTx);
        assertNotNull(readRx);
        assertNotNull(((SerializedDOMDataBroker) domBroker).getCommitStatsTracker());

        writeTx.put(OPERATIONAL, TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME));
        writeTx.commit().get();
        assertFalse(writeTx.cancel());

        assertEquals(false, domBroker.newReadOnlyTransaction().exists(CONFIGURATION, TestModel.TEST_PATH).get());
        assertEquals(true, domBroker.newReadOnlyTransaction().exists(OPERATIONAL, TestModel.TEST_PATH).get());
        assertEquals(false, domBroker.newReadOnlyTransaction().exists(OPERATIONAL, TestModel.TEST2_PATH).get());

        writeTx = domBroker.newWriteOnlyTransaction();
        writeTx.put(OPERATIONAL, TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME));
        writeTx.delete(OPERATIONAL, TestModel.TEST_PATH);
        writeTx.commit().get();
        assertEquals(false, domBroker.newReadOnlyTransaction().exists(OPERATIONAL, TestModel.TEST_PATH).get());
        assertTrue(domBroker.newWriteOnlyTransaction().cancel());

        writeTx = domBroker.newWriteOnlyTransaction();
        writeTx.put(OPERATIONAL, TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME));
        writeTx.merge(OPERATIONAL, TestModel.TEST_PATH, testContainer);
        writeTx.commit().get();
        assertEquals(Boolean.TRUE, domBroker.newReadOnlyTransaction().exists(OPERATIONAL, TestModel.TEST_PATH).get());
        assertEquals(Boolean.TRUE, domBroker.newReadOnlyTransaction().read(OPERATIONAL, TestModel.TEST_PATH).get()
                 .orElseThrow().toString().contains(testContainer.toString()));

        readRx.close();

        //Expected exception after close call

        try {
            readRx.read(OPERATIONAL, TestModel.TEST_PATH).get();
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @SuppressWarnings({"checkstyle:IllegalThrows", "checkstyle:IllegalCatch"})
    @Test
    public void closeTest() throws Exception {
        final String testException = "TestException";

        domBroker.setCloseable(() -> {
            throw new Exception(testException);
        });

        try {
            domBroker.close();
        } catch (final Exception e) {
            assertTrue(e.getMessage().contains(testException));
        }
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
}
