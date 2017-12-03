/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.common.api.AsyncTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.TransactionChainListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCursorAwareTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeLoopException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducer;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducerException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeService;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;
import org.opendaylight.yangtools.concepts.ListenerRegistration;

public class ShardedDOMTransactionChainAdapter implements DOMTransactionChain {

    private final DOMDataTreeService dataTreeService;
    private final Object txChainIdentifier;
    private final AtomicLong txNum = new AtomicLong();
    private final TransactionChainListener txChainListener;
    private final CachedDataTreeService cachedDataTreeService;
    private TransactionChainWriteTransaction writeTx;
    private TransactionChainReadTransaction readTx;
    private ListenableFuture<Void> writeTxSubmitFuture;
    private boolean finished = false;

    public ShardedDOMTransactionChainAdapter(final Object txChainIdentifier,
                                             final DOMDataTreeService dataTreeService,
                                             final TransactionChainListener txChainListener) {
        this.dataTreeService = requireNonNull(dataTreeService);
        this.txChainIdentifier = requireNonNull(txChainIdentifier);

        // FIXME: nullable listener?
        this.txChainListener = txChainListener;
        this.cachedDataTreeService = new CachedDataTreeService(dataTreeService);
    }

    @Override
    public DOMDataTreeReadTransaction newReadOnlyTransaction() {
        checkRunning();
        checkReadTxClosed();
        checkWriteTxClosed();
        readTx = new TransactionChainReadTransaction(newTransactionIdentifier(),
                new ShardedDOMReadTransactionAdapter(newTransactionIdentifier(), dataTreeService),
                writeTxSubmitFuture, this);

        return readTx;
    }

    @Override
    public DOMDataTreeWriteTransaction newWriteOnlyTransaction() {
        checkRunning();
        checkWriteTxClosed();
        checkReadTxClosed();
        writeTx = new TransactionChainWriteTransaction(newTransactionIdentifier(),
                new ShardedDOMWriteTransactionAdapter(newTransactionIdentifier(),
                        cachedDataTreeService), this);

        return writeTx;
    }

    @Override
    public DOMDataTreeReadWriteTransaction newReadWriteTransaction() {
        checkRunning();
        checkWriteTxClosed();
        checkReadTxClosed();
        ShardedDOMReadWriteTransactionAdapter adapter = new ShardedDOMReadWriteTransactionAdapter(
                newTransactionIdentifier(), cachedDataTreeService);
        TransactionChainReadWriteTransaction readWriteTx = new TransactionChainReadWriteTransaction(
                newTransactionIdentifier(), adapter, adapter.getReadAdapter(), writeTxSubmitFuture, this);

        writeTx = readWriteTx;
        return readWriteTx;
    }

    @Override
    public void close() {
        if (finished) {
            // already closed, do nothing
            return;
        }

        checkReadTxClosed();
        checkWriteTxClosed();
        Futures.addCallback(writeTxSubmitFuture, new FutureCallback<Void>() {
            @Override
            public void onSuccess(@Nullable final Void result) {
                txChainListener.onTransactionChainSuccessful(ShardedDOMTransactionChainAdapter.this);
            }

            @Override
            public void onFailure(final Throwable throwable) {
                // We don't have to do nothing here,
                // tx should take car of it
            }
        }, MoreExecutors.directExecutor());

        cachedDataTreeService.closeProducers();
        finished = true;
    }

    public void closeReadTransaction() {
        readTx = null;
    }

    public void closeWriteTransaction(final ListenableFuture<Void> submitFuture) {
        writeTxSubmitFuture = submitFuture;
        writeTx = null;
    }

    private Object newTransactionIdentifier() {
        return "DOM-CHAIN-" + txChainIdentifier + "-" + txNum.getAndIncrement();
    }

    private void checkWriteTxClosed() {
        checkState(writeTx == null);
    }

    private void checkReadTxClosed() {
        checkState(readTx == null);
    }

    private void checkRunning() {
        checkState(!finished);
    }

    public void transactionFailed(final AsyncTransaction<?, ?> tx, final Throwable cause) {
        txChainListener.onTransactionChainFailed(this, tx, cause);
        if (writeTx != null) {
            writeTx.cancel();
        }
        if (readTx != null) {
            readTx.close();
        }
        cachedDataTreeService.closeProducers();
        finished = true;
    }

    static class CachedDataTreeService implements DOMDataTreeService {

        private final DOMDataTreeService delegateTreeService;
        private final Map<LogicalDatastoreType, NoopCloseDataProducer> producersMap =
                new EnumMap<>(LogicalDatastoreType.class);

        CachedDataTreeService(final DOMDataTreeService delegateTreeService) {
            this.delegateTreeService = delegateTreeService;
        }

        void closeProducers() {
            producersMap.values().forEach(NoopCloseDataProducer::closeDelegate);
        }

        @Nonnull
        @Override
        public <T extends DOMDataTreeListener> ListenerRegistration<T> registerListener(
                @Nonnull final T listener, @Nonnull final Collection<DOMDataTreeIdentifier> subtrees,
                         final boolean allowRxMerges, @Nonnull final Collection<DOMDataTreeProducer> producers)
                throws DOMDataTreeLoopException {
            return delegateTreeService.registerListener(listener, subtrees, allowRxMerges, producers);
        }

        @Override
        public DOMDataTreeProducer createProducer(@Nonnull final Collection<DOMDataTreeIdentifier> subtrees) {
            // FIXME: this needs to be documented or fixed.
            checkState(subtrees.size() == 1);

            final DOMDataTreeIdentifier treeId = subtrees.iterator().next();
            final NoopCloseDataProducer producer = new NoopCloseDataProducer(
                delegateTreeService.createProducer(subtrees));
            producersMap.putIfAbsent(treeId.getDatastoreType(), producer);
            return verifyNotNull(producer);
        }

        static class NoopCloseDataProducer implements DOMDataTreeProducer {

            private final DOMDataTreeProducer delegateTreeProducer;

            NoopCloseDataProducer(final DOMDataTreeProducer delegateTreeProducer) {
                this.delegateTreeProducer = delegateTreeProducer;
            }

            @Nonnull
            @Override
            public DOMDataTreeCursorAwareTransaction createTransaction(final boolean isolated) {
                return delegateTreeProducer.createTransaction(isolated);
            }

            @Nonnull
            @Override
            public DOMDataTreeProducer createProducer(@Nonnull final Collection<DOMDataTreeIdentifier> subtrees) {
                return delegateTreeProducer.createProducer(subtrees);
            }

            @Override
            public ListenableFuture<? extends Object> shutdown() {
                // noop
                return Futures.immediateFuture(null);
            }

            void closeDelegate() {
                try {
                    delegateTreeProducer.close();
                } catch (final DOMDataTreeProducerException | InterruptedException | ExecutionException e) {
                    throw new IllegalStateException("Trying to close DOMDataTreeProducer with open transaction", e);
                }
            }
        }
    }
}
