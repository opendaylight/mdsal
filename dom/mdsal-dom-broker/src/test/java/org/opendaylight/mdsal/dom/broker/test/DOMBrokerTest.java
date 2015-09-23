/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.broker.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.CONFIGURATION;
import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.OPERATIONAL;

import org.opendaylight.mdsal.dom.broker.test.util.TestModel;

import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMDataStore;
import org.opendaylight.mdsal.dom.spi.store.DOMStore;

import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.TransactionCommitDeadlockException;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.mdsal.dom.broker.AbstractDOMDataBroker;
import org.opendaylight.mdsal.dom.broker.SerializedDOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ForwardingExecutorService;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Collections;
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
import org.opendaylight.yangtools.util.concurrent.DeadlockDetectingListeningExecutorService;
import org.opendaylight.yangtools.util.concurrent.SpecialExecutors;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class DOMBrokerTest {

    private SchemaContext schemaContext;
    private AbstractDOMDataBroker domBroker;
    private ListeningExecutorService executor;
    private ExecutorService futureExecutor;
    private CommitExecutorService commitExecutor;

    @Before
    public void setupStore() {

        InMemoryDOMDataStore operStore = new InMemoryDOMDataStore("OPER",
                MoreExecutors.newDirectExecutorService());
        InMemoryDOMDataStore configStore = new InMemoryDOMDataStore("CFG",
                MoreExecutors.newDirectExecutorService());
        schemaContext = TestModel.createTestContext();

        operStore.onGlobalContextUpdated(schemaContext);
        configStore.onGlobalContextUpdated(schemaContext);

        ImmutableMap<LogicalDatastoreType, DOMStore> stores = ImmutableMap.<LogicalDatastoreType, DOMStore> builder() //
                .put(CONFIGURATION, configStore) //
                .put(OPERATIONAL, operStore) //
                .build();

        commitExecutor = new CommitExecutorService(Executors.newSingleThreadExecutor());
        futureExecutor = SpecialExecutors.newBlockingBoundedCachedThreadPool(1, 5, "FCB");
        executor = new DeadlockDetectingListeningExecutorService(commitExecutor,
                TransactionCommitDeadlockException.DEADLOCK_EXCEPTION_SUPPLIER, futureExecutor);
        domBroker = new SerializedDOMDataBroker(stores, executor);
    }

    @After
    public void tearDown() {
        if( executor != null ) {
            executor.shutdownNow();
        }

        if(futureExecutor != null) {
            futureExecutor.shutdownNow();
        }
    }

    @Test(timeout=10000)
    public void testTransactionIsolation() throws InterruptedException, ExecutionException {

        assertNotNull(domBroker);

        DOMDataTreeReadTransaction readTx = domBroker.newReadOnlyTransaction();
        assertNotNull(readTx);

        DOMDataTreeWriteTransaction writeTx = domBroker.newWriteOnlyTransaction();
        assertNotNull(writeTx);
        /**
         *
         * Writes /test in writeTx
         *
         */
        writeTx.put(OPERATIONAL, TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME));


        /**
         *
         * Reads /test from readTx Read should return Absent.
         *
         */
        ListenableFuture<Optional<NormalizedNode<?, ?>>> readTxContainer = readTx
                .read(OPERATIONAL, TestModel.TEST_PATH);
        assertFalse(readTxContainer.get().isPresent());
    }

    @Test(timeout=10000)
    public void testTransactionCommit() throws InterruptedException, ExecutionException {

        DOMDataTreeWriteTransaction writeTx = domBroker.newWriteOnlyTransaction();
        assertNotNull(writeTx);
        /**
         *
         * Writes /test in writeTx
         *
         */
        writeTx.put(OPERATIONAL, TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME));

        writeTx.submit().get();

        Optional<NormalizedNode<?, ?>> afterCommitRead = domBroker.newReadOnlyTransaction()
                .read(OPERATIONAL, TestModel.TEST_PATH).get();
        assertTrue(afterCommitRead.isPresent());
    }

    @Test(expected=TransactionCommitFailedException.class)
    public void testRejectedCommit() throws Exception {

        commitExecutor.delegate = Mockito.mock( ExecutorService.class );
        Mockito.doThrow( new RejectedExecutionException( "mock" ) )
            .when( commitExecutor.delegate ).execute( Mockito.any( Runnable.class ) );
        Mockito.doNothing().when( commitExecutor.delegate ).shutdown();
        Mockito.doReturn( Collections.emptyList() ).when( commitExecutor.delegate ).shutdownNow();
        Mockito.doReturn( "" ).when( commitExecutor.delegate ).toString();
        Mockito.doReturn( true ).when( commitExecutor.delegate )
            .awaitTermination( Mockito.anyLong(), Mockito.any( TimeUnit.class ) );

        DOMDataTreeWriteTransaction writeTx = domBroker.newWriteOnlyTransaction();
        writeTx.put( OPERATIONAL, TestModel.TEST_PATH, ImmutableNodes.containerNode(TestModel.TEST_QNAME) );

        writeTx.submit().checkedGet( 5, TimeUnit.SECONDS );
    }

    AtomicReference<Throwable> submitTxAsync( final DOMDataTreeWriteTransaction writeTx ) {
        final AtomicReference<Throwable> caughtEx = new AtomicReference<>();
        new Thread() {
            @Override
            public void run() {

                try {
                    writeTx.submit();
                } catch( Throwable e ) {
                    caughtEx.set( e );
                }
            }

        }.start();

        return caughtEx;
    }

    static class CommitExecutorService extends ForwardingExecutorService {

        ExecutorService delegate;

        public CommitExecutorService( final ExecutorService delegate ) {
            this.delegate = delegate;
        }

        @Override
        protected ExecutorService delegate() {
            return delegate;
        }
    }
}
