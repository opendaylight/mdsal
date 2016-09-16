/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.NotThreadSafe;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCursorAwareTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
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
    private static final TransactionCommitFailedExceptionMapper SUBMIT_FAILED_MAPPER =
            TransactionCommitFailedExceptionMapper.create("submit");
    private static final AtomicLong COUNTER = new AtomicLong();

    private final Map<DOMDataTreeIdentifier, DOMDataTreeShardWriteTransaction> idToTransaction;
    private final Collection<YangInstanceIdentifier> childBoundaries;
    private final ShardedDOMDataTreeProducer producer;
    private final String identifier;

    private final SettableFuture<Void> future = SettableFuture.create();
    private final CheckedFuture<Void, TransactionCommitFailedException> submitFuture =
            Futures.makeChecked(future, SUBMIT_FAILED_MAPPER);

    @GuardedBy("this")
    private boolean closed =  false;

    @GuardedBy("this")
    private DOMDataTreeWriteCursor openCursor;

    ShardedDOMDataTreeWriteTransaction(final ShardedDOMDataTreeProducer producer,
                                       final Map<DOMDataTreeIdentifier, DOMDataTreeShardProducer> idToProducer,
                                       final Set<YangInstanceIdentifier> childRoots) {
        this.producer = Preconditions.checkNotNull(producer);
        this.identifier = "SHARDED-DOM-" + COUNTER.getAndIncrement();
        idToTransaction = ImmutableMap.copyOf(Maps.transformValues(idToProducer,
            DOMDataTreeShardProducer::createTransaction));
        childBoundaries = Preconditions.checkNotNull(childRoots);
        LOG.debug("Created new transaction {}", identifier);
    }

    private DOMDataTreeShardWriteTransaction lookup(final DOMDataTreeIdentifier prefix) {
        for (final Entry<DOMDataTreeIdentifier, DOMDataTreeShardWriteTransaction> e : idToTransaction.entrySet()) {
            if (e.getKey().contains(prefix)) {
                Preconditions.checkArgument(!producer.isDelegatedToChild(prefix),
                    "Path %s is delegated to child producer.", prefix);
                return e.getValue();
            }
        }

        throw new IllegalArgumentException(String.format("Path %s is not accessible from transaction %s",
                prefix, this));
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
        for (final DOMDataTreeShardWriteTransaction tx : idToTransaction.values()) {
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

        producer.transactionSubmitted(this);
        return submitFuture;
    }

    CheckedFuture<Void, TransactionCommitFailedException> doSubmit(
            final Consumer<ShardedDOMDataTreeWriteTransaction> success,
            final BiConsumer<ShardedDOMDataTreeWriteTransaction, Throwable> failure) {

        final ListenableFuture<List<Void>> listListenableFuture = Futures.allAsList(
            idToTransaction.values().stream().map(tx -> {
                LOG.debug("Readying tx {}", identifier);
                tx.ready();
                return tx.submit();
            }).collect(Collectors.toList()));

        final SettableFuture<Void> ret = SettableFuture.create();
        Futures.addCallback(listListenableFuture, new FutureCallback<List<Void>>() {
            @Override
            public void onSuccess(final List<Void> result) {
                success.accept(ShardedDOMDataTreeWriteTransaction.this);
                ret.set(null);
            }

            @Override
            public void onFailure(final Throwable exp) {
                failure.accept(ShardedDOMDataTreeWriteTransaction.this, exp);
                ret.setException(exp);
            }
        });

        return Futures.makeChecked(ret, SUBMIT_FAILED_MAPPER);
    }

    void onTransactionSuccess(final Void result) {
        future.set(result);
    }

    void onTransactionFailure(final Throwable throwable) {
        future.setException(throwable);
    }

    synchronized void cursorClosed() {
        openCursor = null;
    }

    private class DelegatingCursor implements DOMDataTreeWriteCursor {

        private final DOMDataTreeWriteCursor delegate;
        private final DOMDataTreeIdentifier rootPosition;
        private final Deque<PathArgument> path = new LinkedList<>();

        DelegatingCursor(final DOMDataTreeWriteCursor delegate, final DOMDataTreeIdentifier rootPosition) {
            this.delegate = Preconditions.checkNotNull(delegate);
            this.rootPosition = Preconditions.checkNotNull(rootPosition);
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
            int depthEntered = path.size() - rootPosition.getRootIdentifier().getPathArguments().size();
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
            path.add(child);
            final YangInstanceIdentifier yid = YangInstanceIdentifier.create(path);
            childBoundaries.forEach(id -> {
                if (id.contains(yid)) {
                    path.removeLast();
                    throw new IllegalArgumentException("Path {" + yid + "} is not available to this cursor"
                            + " since it's already claimed by a child producer");
                }
            });
            path.removeLast();
        }
    }
}
