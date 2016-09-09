/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableBiMap.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCursorAwareTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducer;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducerBusyException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducerException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeShard;
import org.opendaylight.mdsal.dom.store.inmemory.DOMDataTreeShardProducer;
import org.opendaylight.mdsal.dom.store.inmemory.WriteableDOMDataTreeShard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ShardedDOMDataTreeProducer implements DOMDataTreeProducer {
    private static final Logger LOG = LoggerFactory.getLogger(ShardedDOMDataTreeProducer.class);
    private final Set<DOMDataTreeIdentifier> subtrees;
    private final ShardedDOMDataTree dataTree;

    private BiMap<DOMDataTreeIdentifier, DOMDataTreeShardProducer> idToProducer = ImmutableBiMap.of();
    private Map<DOMDataTreeIdentifier, DOMDataTreeShard> idToShard;


    private static final AtomicReferenceFieldUpdater<ShardedDOMDataTreeProducer,
            ShardedDOMDataTreeWriteTransaction> OPEN_UPDATER =
            AtomicReferenceFieldUpdater
                    .newUpdater(ShardedDOMDataTreeProducer.class, ShardedDOMDataTreeWriteTransaction.class, "openTx");
    private volatile ShardedDOMDataTreeWriteTransaction openTx;

    private static final AtomicReferenceFieldUpdater<ShardedDOMDataTreeProducer,
            ShardedDOMDataTreeWriteTransaction> INFLIGHT_UPDATER =
            AtomicReferenceFieldUpdater
                    .newUpdater(ShardedDOMDataTreeProducer.class,
                            ShardedDOMDataTreeWriteTransaction.class, "inFlightTx");
    private volatile ShardedDOMDataTreeWriteTransaction inFlightTx;

    @GuardedBy("this")
    private volatile Deque<ShardedDOMDataTreeWriteTransaction> txQueue = new ConcurrentLinkedDeque<>();

    @GuardedBy("this")
    private Map<DOMDataTreeIdentifier, DOMDataTreeProducer> children = Collections.emptyMap();
    @GuardedBy("this")
    private boolean closed;

    @GuardedBy("this")
    private ShardedDOMDataTreeListenerContext<?> attachedListener;

    ShardedDOMDataTreeProducer(final ShardedDOMDataTree dataTree,
                               final Collection<DOMDataTreeIdentifier> subtrees,
                               final Map<DOMDataTreeIdentifier, DOMDataTreeShard> shardMap,
                               final Multimap<DOMDataTreeShard, DOMDataTreeIdentifier> shardToId) {
        this.dataTree = Preconditions.checkNotNull(dataTree);
        if (!shardToId.isEmpty()) {
            this.idToProducer = mapIdsToProducer(shardToId);
        }
        idToShard = ImmutableMap.copyOf(shardMap);
        this.subtrees = ImmutableSet.copyOf(subtrees);
    }

    static DOMDataTreeProducer create(final ShardedDOMDataTree dataTree,
                                      final Collection<DOMDataTreeIdentifier> subtrees,
                                      final Map<DOMDataTreeIdentifier, DOMDataTreeShard> shardMap) {
        final Multimap<DOMDataTreeShard, DOMDataTreeIdentifier> shardToIdentifiers = ArrayListMultimap.create();
        // map which identifier belongs to which shard
        for (final Entry<DOMDataTreeIdentifier, DOMDataTreeShard> entry : shardMap.entrySet()) {
            shardToIdentifiers.put(entry.getValue(), entry.getKey());
        }

        return new ShardedDOMDataTreeProducer(dataTree, subtrees, shardMap, shardToIdentifiers);
    }

    void subshardAdded(final Map<DOMDataTreeIdentifier, DOMDataTreeShard> shardMap) {
        Preconditions.checkState(openTx == null, "Transaction %s is still open", openTx);
        final Multimap<DOMDataTreeShard, DOMDataTreeIdentifier> shardToIdentifiers = ArrayListMultimap.create();
        // map which identifier belongs to which shard
        for (final Entry<DOMDataTreeIdentifier, DOMDataTreeShard> entry : shardMap.entrySet()) {
            shardToIdentifiers.put(entry.getValue(), entry.getKey());
        }
        this.idToProducer = mapIdsToProducer(shardToIdentifiers);
        idToShard = ImmutableMap.copyOf(shardMap);
    }

    private BiMap<DOMDataTreeIdentifier, DOMDataTreeShardProducer> mapIdsToProducer(final Multimap<DOMDataTreeShard,
            DOMDataTreeIdentifier> shardToId) {
        final Builder<DOMDataTreeIdentifier, DOMDataTreeShardProducer> idToProducerBuilder = ImmutableBiMap.builder();
        for (final Entry<DOMDataTreeShard, Collection<DOMDataTreeIdentifier>> entry : shardToId.asMap().entrySet()) {
            if (entry.getKey() instanceof WriteableDOMDataTreeShard) {
                //create a single producer for all prefixes in a single shard
                final DOMDataTreeShardProducer producer = ((WriteableDOMDataTreeShard) entry.getKey())
                        .createProducer(entry.getValue());
                // id mapped to producers
                for (final DOMDataTreeIdentifier id : entry.getValue()) {
                    idToProducerBuilder.put(id, producer);
                }
            } else {
                LOG.error("Unable to create a producer for shard that's not a WriteableDOMDataTreeShard");
            }
        }

        return idToProducerBuilder.build();
    }

    @Override
    public synchronized DOMDataTreeCursorAwareTransaction createTransaction(final boolean isolated) {
        Preconditions.checkState(!closed, "Producer is already closed");
        Preconditions.checkState(openTx == null, "Transaction %s is still open", openTx);

        LOG.debug("Creating transaction from producer");
        if (isolated) {
            OPEN_UPDATER.compareAndSet(this, null,
                    new ShardedDOMDataTreeWriteTransaction(this, idToProducer, children, isolated));
            return openTx;
        }

        if (!txQueue.isEmpty()) {
            final ShardedDOMDataTreeWriteTransaction last = txQueue.removeLast();
            if (last.isIsolated()) {
                OPEN_UPDATER.compareAndSet(this, null,
                        new ShardedDOMDataTreeWriteTransaction(this, idToProducer, children, isolated));
                return openTx;
            }

            if (INFLIGHT_UPDATER.get(this) == null) {
                // we have a tx in queue and no inflight tx, process the last one and create new one for user
                processTransaction(last);

                OPEN_UPDATER.compareAndSet(this, null,
                        new ShardedDOMDataTreeWriteTransaction(this, idToProducer, children, isolated));
            } else {
                LOG.debug("Reusing previous transaction{} since there is still a transaction inflight, queue size: {}",
                        last.getIdentifier(), txQueue.size());
                OPEN_UPDATER.compareAndSet(this, null, last);
            }
        } else {
            OPEN_UPDATER.compareAndSet(this, null,
                    new ShardedDOMDataTreeWriteTransaction(this, idToProducer, children, isolated));
        }

        return openTx;
    }

    @GuardedBy("this")
    private boolean haveSubtree(final DOMDataTreeIdentifier subtree) {
        for (final DOMDataTreeIdentifier i : idToShard.keySet()) {
            if (i.contains(subtree)) {
                return true;
            }
        }

        return false;
    }

    @GuardedBy("this")
    private DOMDataTreeProducer lookupChild(final DOMDataTreeIdentifier domDataTreeIdentifier) {
        for (final Entry<DOMDataTreeIdentifier, DOMDataTreeProducer> e : children.entrySet()) {
            if (e.getKey().contains(domDataTreeIdentifier)) {
                return e.getValue();
            }
        }

        return null;
    }

    @Override
    public synchronized DOMDataTreeProducer createProducer(final Collection<DOMDataTreeIdentifier> subtrees) {
        Preconditions.checkState(!closed, "Producer is already closed");
        Preconditions.checkState(OPEN_UPDATER.get(this) == null, "Transaction %s is still open", openTx);

        for (final DOMDataTreeIdentifier s : subtrees) {
            // Check if the subtree was visible at any time
            Preconditions.checkArgument(haveSubtree(s), "Subtree %s was never available in producer %s", s, this);
            // Check if the subtree has not been delegated to a child
            final DOMDataTreeProducer child = lookupChild(s);
            Preconditions.checkArgument(child == null, "Subtree %s is delegated to child producer %s", s, child);

            // Check if part of the requested subtree is not delegated to a child.
            for (final DOMDataTreeIdentifier c : children.keySet()) {
                if (s.contains(c)) {
                    throw new IllegalArgumentException(String.format("Subtree %s cannot be delegated as it is"
                            + " superset of already-delegated %s", s, c));
                }
            }
        }

        final DOMDataTreeProducer ret = dataTree.createProducer(this, subtrees);
        final ImmutableMap.Builder<DOMDataTreeIdentifier, DOMDataTreeProducer> cb = ImmutableMap.builder();
        cb.putAll(children);
        for (final DOMDataTreeIdentifier s : subtrees) {
            cb.put(s, ret);
        }

        children = cb.build();
        return ret;
    }

    boolean isDelegatedToChild(final DOMDataTreeIdentifier path) {
        for (final DOMDataTreeIdentifier c : children.keySet()) {
            if (c.contains(path)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public synchronized void close() throws DOMDataTreeProducerException {
        if (!closed) {
            if (OPEN_UPDATER.get(this) != null) {
                throw new DOMDataTreeProducerBusyException(String.format("Transaction %s is still open", openTx));
            }

            closed = true;
            dataTree.destroyProducer(this);
        }
    }

    protected Set<DOMDataTreeIdentifier> getSubtrees() {
        return subtrees;
    }

    synchronized void cancelTransaction(final ShardedDOMDataTreeWriteTransaction transaction) {
        if (!openTx.equals(transaction)) {
            LOG.warn("Transaction {} is not open in producer {}", transaction, this);
            return;
        }

        LOG.debug("Transaction {} cancelled", transaction);
        openTx = null;
    }

    synchronized void processTransaction(final ShardedDOMDataTreeWriteTransaction transaction) {
        if (OPEN_UPDATER.get(this) != null && OPEN_UPDATER.get(this).equals(transaction)) {
            OPEN_UPDATER.set(this, null);
        }

        if (INFLIGHT_UPDATER.compareAndSet(this, null, transaction)) {
            Futures.addCallback(inFlightTx.doSubmit(), new FutureCallback<Void>() {
                @Override
                public void onSuccess(final Void result) {
                    transactionSuccessful(inFlightTx, result);
                }

                @Override
                public void onFailure(final Throwable throwable) {
                    transactionFailed(inFlightTx, throwable);
                }
            });
        } else {
            if (txQueue.isEmpty() || !txQueue.getLast().equals(transaction)) {
                LOG.debug("Added tx{} into queue, queue size: {}", transaction.getIdentifier(), txQueue.size());
                txQueue.add(transaction);
            }
        }
    }

    void transactionSuccessful(final ShardedDOMDataTreeWriteTransaction tx, final Void result) {
        LOG.debug("Transaction {} completed successfully", tx.getIdentifier());
        Preconditions.checkState(INFLIGHT_UPDATER.compareAndSet(this, tx, null));

        tx.onTransactionSuccess(result);
        processNextTransaction(tx);
    }

    void transactionFailed(final ShardedDOMDataTreeWriteTransaction tx, final Throwable throwable) {
        LOG.debug("Transaction {} failed", tx.getIdentifier(), throwable);
        Preconditions.checkState(INFLIGHT_UPDATER.compareAndSet(this, tx, null));

        tx.onTransactionFailure(throwable);
        processNextTransaction(tx);
    }

    private synchronized void processNextTransaction(final ShardedDOMDataTreeWriteTransaction tx) {

        if (txQueue.isEmpty()) {
            LOG.debug("txQueue empty cannot process next, queue size{}", txQueue.size());
            return;
        } else if (txQueue.peek().equals(openTx)) {
            LOG.debug("last tx is currently locked by user, not procesing next, queue size{}", txQueue.size());
            return;
        }

        processTransaction(txQueue.remove());
    }

    synchronized void boundToListener(final ShardedDOMDataTreeListenerContext<?> listener) {
        // FIXME: Add option to dettach
        Preconditions.checkState(this.attachedListener == null,
                "Producer %s is already attached to other listener.",
                listener.getListener());
        this.attachedListener = listener;
    }
}
