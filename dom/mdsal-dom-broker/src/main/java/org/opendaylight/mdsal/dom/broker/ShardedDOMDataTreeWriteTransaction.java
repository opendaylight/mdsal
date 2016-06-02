/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.NotThreadSafe;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCursorAwareTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducer;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.mdsal.dom.store.inmemory.DOMDataTreeShardProducer;
import org.opendaylight.mdsal.dom.store.inmemory.DOMDataTreeShardWriteTransaction;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NotThreadSafe
final class ShardedDOMDataTreeWriteTransaction implements DOMDataTreeCursorAwareTransaction {
    private static final Logger LOG = LoggerFactory.getLogger(ShardedDOMDataTreeWriteTransaction.class);
    private static final AtomicLong COUNTER = new AtomicLong();
    private final Map<DOMDataTreeIdentifier, DOMDataTreeShardWriteTransaction> idToTransaction;
    private final ShardedDOMDataTreeProducer producer;
    private final String identifier;
    private final Set<YangInstanceIdentifier> childBoundaries = new HashSet<>();
    @GuardedBy("this")
    private boolean closed =  false;

    @GuardedBy("this")
    private DOMDataTreeWriteCursor openCursor;

    ShardedDOMDataTreeWriteTransaction(final ShardedDOMDataTreeProducer producer,
                                       final Map<DOMDataTreeIdentifier, DOMDataTreeShardProducer> idToProducer,
                                       final Map<DOMDataTreeIdentifier, DOMDataTreeProducer> childProducers) {
        this.producer = Preconditions.checkNotNull(producer);
        idToTransaction = new HashMap<>();
        Preconditions.checkNotNull(idToProducer).forEach((id, prod) -> idToTransaction.put(id, prod.createTransaction()));
        this.identifier = "SHARDED-DOM-" + COUNTER.getAndIncrement();
        childProducers.forEach((id, prod) -> childBoundaries.add(id.getRootIdentifier()));
    }

    // FIXME: use atomic operations
    @GuardedBy("this")
    private DOMDataTreeShardWriteTransaction lookup(final DOMDataTreeIdentifier prefix) {
        for (final Entry<DOMDataTreeIdentifier, DOMDataTreeShardWriteTransaction> e : idToTransaction.entrySet()) {
            if (e.getKey().contains(prefix)) {
                Preconditions.checkArgument(!producer.isDelegatedToChild(prefix),
                        "Path %s is delegated to child producer.",
                        prefix);
                return e.getValue();
            }
        }
        throw new IllegalArgumentException(String.format("Path %s is not accessible from transaction %s", prefix, this));
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public synchronized boolean cancel() {
        if (closed) {
            return false;
        }

        LOG.debug("Cancelling transaction {}", identifier);
        if (openCursor != null) {
            openCursor.close();
        }
        for (final DOMDataTreeShardWriteTransaction tx : ImmutableSet.copyOf(idToTransaction.values())) {
            tx.close();
        }

        closed = true;
        producer.cancelTransaction(this);
        return true;
    }

    @Override
    public synchronized DOMDataTreeWriteCursor createCursor(final DOMDataTreeIdentifier prefix) {
        Preconditions.checkState(!closed, "Transaction is closed already");
        Preconditions.checkState(openCursor == null, "There is still a cursor open");
        final DOMDataTreeShardWriteTransaction lookup = lookup(prefix);
        openCursor = new DelegatingCursor(lookup.createCursor(prefix), prefix);
        return openCursor;
    }

    @Override
    public synchronized CheckedFuture<Void, TransactionCommitFailedException> submit() {
        Preconditions.checkState(!closed, "Transaction %s is already closed", identifier);
        Preconditions.checkState(openCursor == null, "Cannot submit transaction while there is a cursor open");

        final Set<DOMDataTreeShardWriteTransaction> txns = ImmutableSet.copyOf(idToTransaction.values());
        for (final DOMDataTreeShardWriteTransaction tx : txns) {
            tx.ready();
        }
        producer.transactionSubmitted(this);
        try {
            return Futures.immediateCheckedFuture(new SubmitCoordinationTask(identifier, txns).call());
        } catch (final TransactionCommitFailedException e) {
            return Futures.immediateFailedCheckedFuture(e);
        }
    }

    synchronized void cursorClosed() {
        openCursor = null;
    }

    private class DelegatingCursor implements DOMDataTreeWriteCursor {

        private final DOMDataTreeWriteCursor delegate;
        private final Deque<PathArgument> path = new LinkedList<>();

        public DelegatingCursor(final DOMDataTreeWriteCursor delegate, final DOMDataTreeIdentifier rootPosition) {
            this.delegate = delegate;
            path.addAll(rootPosition.getRootIdentifier().getPathArguments());
        }

        @Override
        public void enter(@Nonnull final PathArgument child) {
            checkAvailable(child);
            path.push(child);
            delegate.enter(child);
        }

        @Override
        public void enter(@Nonnull final PathArgument... path) {
            for (final PathArgument pathArgument : path) {
                enter(pathArgument);
            }
        }

        @Override
        public void enter(@Nonnull final Iterable<PathArgument> path) {
            for (final PathArgument pathArgument : path) {
                enter(pathArgument);
            }
        }

        @Override
        public void exit() {
            path.pop();
            delegate.exit();
        }

        @Override
        public void exit(final int depth) {
            for (int i = 0; i < depth; i++) {
                path.pop();
            }
            delegate.exit(depth);
        }

        @Override
        public void close() {
            delegate.close();
            cursorClosed();
        }

        @Override
        public void delete(final PathArgument child) {
            checkAvailable(child);
            delegate.delete(child);
        }

        @Override
        public void merge(final PathArgument child, final NormalizedNode<?, ?> data) {
            checkAvailable(child);
            delegate.merge(child, data);
        }

        @Override
        public void write(final PathArgument child, final NormalizedNode<?, ?> data) {
            checkAvailable(child);
            delegate.write(child, data);
        }

        void checkAvailable(final PathArgument child) {
            path.add(child);
            final YangInstanceIdentifier yid = YangInstanceIdentifier.create(path);
            childBoundaries.forEach(id -> {
                if (id.contains(yid)) {
                    path.removeLast();
                    throw new IllegalArgumentException("Path {" + yid + "} is not available to this cursor since it's already claimed by a child producer");
                }
            });
            path.removeLast();
        }
    }

    private static class SubmitCoordinationTask implements Callable<Void> {

        private static final Logger LOG = LoggerFactory.getLogger(SubmitCoordinationTask.class);

        private final String identifier;
        private final Collection<DOMDataTreeShardWriteTransaction> transactions;

        SubmitCoordinationTask(final String identifier,
                                    final Collection<DOMDataTreeShardWriteTransaction> transactions) {
            this.identifier = identifier;
            this.transactions = transactions;
        }

        @Override
        public Void call() throws TransactionCommitFailedException {

            try {
                LOG.debug("Producer {}, submit started", identifier);
                submitBlocking();

                return null;
            } catch (final TransactionCommitFailedException e) {
                LOG.warn("Failure while submitting transaction for producer {}", identifier, e);
                //FIXME abort here
                throw e;
            }
        }

        void submitBlocking() throws TransactionCommitFailedException {
            for (final ListenableFuture<?> commit : submitAll()) {
                try {
                    commit.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new TransactionCommitFailedException("Submit failed", e);
                }
            }
        }

        private ListenableFuture<?>[] submitAll() {
            final ListenableFuture<?>[] ops = new ListenableFuture<?>[transactions.size()];
            int i = 0;
            for (final DOMDataTreeShardWriteTransaction tx : transactions) {
                ops[i++] = tx.submit();
            }
            return ops;
        }
    }
}
