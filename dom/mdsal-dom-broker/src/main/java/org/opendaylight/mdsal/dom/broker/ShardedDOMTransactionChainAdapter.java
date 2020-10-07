/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCursorAwareTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducer;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducerException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeService;
import org.opendaylight.mdsal.dom.api.DOMDataTreeTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;
import org.opendaylight.mdsal.dom.api.DOMTransactionChainListener;
import org.opendaylight.mdsal.dom.spi.ForwardingDOMDataTreeService;

public class ShardedDOMTransactionChainAdapter implements DOMTransactionChain {

    private final DOMDataTreeService dataTreeService;
    private final Object txChainIdentifier;
    private final AtomicLong txNum = new AtomicLong();
    private final DOMTransactionChainListener txChainListener;
    private final CachedDataTreeService cachedDataTreeService;
    private TransactionChainWriteTransaction writeTx;
    private TransactionChainReadTransaction readTx;
    private FluentFuture<? extends CommitInfo> writeTxCommitFuture;
    private boolean finished = false;

    public ShardedDOMTransactionChainAdapter(final Object txChainIdentifier,
            final DOMDataTreeService dataTreeService, final DOMTransactionChainListener txChainListener) {
        this.dataTreeService = requireNonNull(dataTreeService);
        this.txChainIdentifier = requireNonNull(txChainIdentifier);
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
                writeTxCommitFuture, this);

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
                newTransactionIdentifier(), adapter, adapter.getReadAdapter(), writeTxCommitFuture, this);

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
        writeTxCommitFuture.addCallback(new FutureCallback<CommitInfo>() {
            @Override
            public void onSuccess(final CommitInfo result) {
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

    public void closeWriteTransaction(final FluentFuture<? extends CommitInfo> commitFuture) {
        writeTxCommitFuture = commitFuture;
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

    public void transactionFailed(final DOMDataTreeTransaction tx, final Throwable cause) {
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

    private static final class CachedDataTreeService extends ForwardingDOMDataTreeService {
        private final Map<LogicalDatastoreType, NoopCloseDataProducer> producersMap =
            new EnumMap<>(LogicalDatastoreType.class);
        private final DOMDataTreeService delegate;

        CachedDataTreeService(final DOMDataTreeService delegate) {
            this.delegate = requireNonNull(delegate);
        }

        @Override
        public DOMDataTreeProducer createProducer(final Collection<DOMDataTreeIdentifier> subtrees) {
            checkState(subtrees.size() == 1);
            DOMDataTreeIdentifier treeId = subtrees.iterator().next();
            NoopCloseDataProducer producer = new NoopCloseDataProducer(delegate.createProducer(
                Collections.singleton(treeId)));
            producersMap.putIfAbsent(treeId.getDatastoreType(), producer);
            return producer;
        }

        @Override
        protected DOMDataTreeService delegate() {
            return delegate;
        }

        void closeProducers() {
            producersMap.values().forEach(NoopCloseDataProducer::closeDelegate);
        }
    }

    private static final class NoopCloseDataProducer implements DOMDataTreeProducer {
        private final DOMDataTreeProducer delegateTreeProducer;

        NoopCloseDataProducer(final DOMDataTreeProducer delegateTreeProducer) {
            this.delegateTreeProducer = delegateTreeProducer;
        }

        @Override
        public DOMDataTreeCursorAwareTransaction createTransaction(final boolean isolated) {
            return delegateTreeProducer.createTransaction(isolated);
        }

        @Override
        public DOMDataTreeProducer createProducer(final Collection<DOMDataTreeIdentifier> subtrees) {
            return delegateTreeProducer.createProducer(subtrees);
        }

        @Override
        public void close() {
            // noop
        }

        public void closeDelegate() {
            try {
                delegateTreeProducer.close();
            } catch (final DOMDataTreeProducerException e) {
                throw new IllegalStateException("Trying to close DOMDataTreeProducer with open transaction", e);
            }
        }
    }
}
