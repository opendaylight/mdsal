/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCursorAwareTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteCursor;
import org.opendaylight.mdsal.dom.spi.shard.DOMDataTreeShardWriteTransaction;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ShardedDOMDataTreeWriteTransaction implements DOMDataTreeCursorAwareTransaction {
    private static final Logger LOG = LoggerFactory.getLogger(ShardedDOMDataTreeWriteTransaction.class);
    private static final AtomicLong COUNTER = new AtomicLong();

    private final Map<DOMDataTreeIdentifier, DOMDataTreeShardWriteTransaction> transactions;
    private final ShardedDOMDataTreeProducer producer;
    private final ProducerLayout layout;
    private final String identifier;

    private final SettableFuture<CommitInfo> future = SettableFuture.create();

    @GuardedBy("this")
    private boolean closed =  false;

    @GuardedBy("this")
    private DOMDataTreeWriteCursor openCursor;

    ShardedDOMDataTreeWriteTransaction(final ShardedDOMDataTreeProducer producer,
        final Map<DOMDataTreeIdentifier, DOMDataTreeShardWriteTransaction> transactions, final ProducerLayout layout) {
        this.producer = requireNonNull(producer);
        this.transactions = ImmutableMap.copyOf(transactions);
        this.layout = requireNonNull(layout);
        this.identifier = "SHARDED-DOM-" + COUNTER.getAndIncrement();
        LOG.debug("Created new transaction {}", identifier);
    }

    private DOMDataTreeShardWriteTransaction lookup(final DOMDataTreeIdentifier prefix) {
        final DOMDataTreeShardWriteTransaction fast = transactions.get(prefix);
        if (fast != null) {
            return fast;
        }

        LOG.debug("Prefix {} not found in available subtrees {}, fallback to slow path", prefix, transactions.keySet());
        for (final Entry<DOMDataTreeIdentifier, DOMDataTreeShardWriteTransaction> e : transactions.entrySet()) {
            if (e.getKey().contains(prefix)) {
                return e.getValue();
            }
        }

        return null;
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
        for (final DOMDataTreeShardWriteTransaction tx : transactions.values()) {
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
        Preconditions.checkArgument(!producer.isDelegatedToChild(prefix), "Path %s is delegated to child producer.",
            prefix);

        final DOMDataTreeShardWriteTransaction lookup = lookup(prefix);
        Preconditions.checkArgument(lookup != null, "Path %s is not accessible from transaction %s", prefix, this);

        openCursor = new DelegatingCursor(lookup.createCursor(prefix), prefix);
        return openCursor;
    }

    @Override
    public synchronized FluentFuture<? extends @NonNull CommitInfo> commit() {
        Preconditions.checkState(!closed, "Transaction %s is already closed", identifier);
        Preconditions.checkState(openCursor == null, "Cannot submit transaction while there is a cursor open");

        producer.transactionSubmitted(this);
        return FluentFuture.from(future);
    }

    void doSubmit(final Consumer<ShardedDOMDataTreeWriteTransaction> success,
            final BiConsumer<ShardedDOMDataTreeWriteTransaction, Throwable> failure) {
        LOG.debug("Readying tx {}", identifier);

        final ListenableFuture<?> internalFuture;
        switch (transactions.size()) {
            case 0:
                success.accept(this);
                return;
            case 1: {
                final DOMDataTreeShardWriteTransaction tx = transactions.values().iterator().next();
                tx.ready();
                internalFuture = tx.submit();
                break;
            }
            default:
                internalFuture = Futures.allAsList(transactions.values().stream().map(tx -> {
                    tx.ready();
                    return tx.submit();
                }).collect(Collectors.toList()));
        }

        Futures.addCallback(internalFuture, new FutureCallback<Object>() {
            @Override
            public void onSuccess(final Object result) {
                success.accept(ShardedDOMDataTreeWriteTransaction.this);
            }

            @Override
            public void onFailure(final Throwable exp) {
                failure.accept(ShardedDOMDataTreeWriteTransaction.this, exp);
            }
        }, MoreExecutors.directExecutor());
    }

    void onTransactionSuccess(final CommitInfo commitInfo) {
        future.set(commitInfo);
    }

    void onTransactionFailure(final Throwable throwable) {
        future.setException(throwable);
    }

    synchronized void cursorClosed() {
        openCursor = null;
    }

    private class DelegatingCursor implements DOMDataTreeWriteCursor {
        private final Deque<PathArgument> currentArgs = new ArrayDeque<>();
        private final DOMDataTreeWriteCursor delegate;
        private final DOMDataTreeIdentifier rootPosition;

        DelegatingCursor(final DOMDataTreeWriteCursor delegate, final DOMDataTreeIdentifier rootPosition) {
            this.delegate = requireNonNull(delegate);
            this.rootPosition = requireNonNull(rootPosition);
            currentArgs.addAll(rootPosition.getRootIdentifier().getPathArguments());
        }

        @Override
        public void enter(final PathArgument child) {
            checkAvailable(child);
            delegate.enter(child);
            currentArgs.push(child);
        }

        @Override
        public void enter(final PathArgument... path) {
            for (final PathArgument pathArgument : path) {
                enter(pathArgument);
            }
        }

        @Override
        public void enter(final Iterable<PathArgument> path) {
            for (final PathArgument pathArgument : path) {
                enter(pathArgument);
            }
        }

        @Override
        public void exit() {
            delegate.exit();
            currentArgs.pop();
        }

        @Override
        public void exit(final int depth) {
            delegate.exit(depth);
            for (int i = 0; i < depth; i++) {
                currentArgs.pop();
            }
        }

        @Override
        public void close() {
            int depthEntered = currentArgs.size() - rootPosition.getRootIdentifier().getPathArguments().size();
            if (depthEntered > 0) {
                // clean up existing modification cursor in case this tx will be reused for batching
                delegate.exit(depthEntered);
            }

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
            layout.checkAvailable(currentArgs, child);
        }
    }

    ProducerLayout getLayout() {
        return layout;
    }
}
