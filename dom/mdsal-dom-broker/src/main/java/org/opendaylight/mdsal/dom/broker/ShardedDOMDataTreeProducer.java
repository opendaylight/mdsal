/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableBiMap.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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

    private static final AtomicReferenceFieldUpdater<ShardedDOMDataTreeProducer, ShardedDOMDataTreeWriteTransaction>
        CURRENT_UPDATER = AtomicReferenceFieldUpdater.newUpdater(ShardedDOMDataTreeProducer.class,
            ShardedDOMDataTreeWriteTransaction.class, "currentTx");
    @SuppressWarnings("unused")
    private volatile ShardedDOMDataTreeWriteTransaction currentTx;

    private static final AtomicReferenceFieldUpdater<ShardedDOMDataTreeProducer, ShardedDOMDataTreeWriteTransaction>
        OPEN_UPDATER = AtomicReferenceFieldUpdater.newUpdater(ShardedDOMDataTreeProducer.class,
            ShardedDOMDataTreeWriteTransaction.class, "openTx");
    private volatile ShardedDOMDataTreeWriteTransaction openTx;

    private static final AtomicReferenceFieldUpdater<ShardedDOMDataTreeProducer, ShardedDOMDataTreeWriteTransaction>
        LAST_UPDATER = AtomicReferenceFieldUpdater.newUpdater(ShardedDOMDataTreeProducer.class,
            ShardedDOMDataTreeWriteTransaction.class, "lastTx");
    private volatile ShardedDOMDataTreeWriteTransaction lastTx;

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

    private static BiMap<DOMDataTreeIdentifier, DOMDataTreeShardProducer> mapIdsToProducer(
            final Multimap<DOMDataTreeShard, DOMDataTreeIdentifier> shardToId) {
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
        final ShardedDOMDataTreeWriteTransaction current = CURRENT_UPDATER.getAndSet(this, null);
        final ShardedDOMDataTreeWriteTransaction ret;
        if (isolated) {
            // Isolated case. If we have a previous transaction, submit it before returning this one.
            if (current != null) {
                submitTransaction(current);
            }
            ret = new ShardedDOMDataTreeWriteTransaction(this, idToProducer, children, true);
        } else {
            // Non-isolated case, see if we can reuse the transaction
            if (current != null) {
                LOG.debug("Reusing previous transaction {} since there is still a transaction inflight",
                        current.getIdentifier());
                ret = current;
            } else {
                ret = new ShardedDOMDataTreeWriteTransaction(this, idToProducer, children, false);
            }
        }

        final boolean success = OPEN_UPDATER.compareAndSet(this, null, ret);
        Verify.verify(success);
        return ret;
    }

    @GuardedBy("this")
    private void submitTransaction(final ShardedDOMDataTreeWriteTransaction current) {
        lastTx = current;
        current.doSubmit(this::transactionSuccessful, this::transactionFailed);
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
        Preconditions.checkState(openTx == null, "Transaction %s is still open", openTx);

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
            if (openTx != null) {
                throw new DOMDataTreeProducerBusyException(String.format("Transaction %s is still open", openTx));
            }

            closed = true;
            dataTree.destroyProducer(this);
        }
    }

    protected Set<DOMDataTreeIdentifier> getSubtrees() {
        return subtrees;
    }

    void cancelTransaction(final ShardedDOMDataTreeWriteTransaction transaction) {
        final boolean success = OPEN_UPDATER.compareAndSet(this, transaction, null);
        if (success) {
            LOG.debug("Transaction {} cancelled", transaction);
        } else {
            LOG.warn("Transaction {} is not open in producer {}", transaction, this);
        }
    }

    // Called when the user submits a transaction
    void transactionSubmitted(final ShardedDOMDataTreeWriteTransaction transaction) {
        final boolean wasOpen = OPEN_UPDATER.compareAndSet(this, transaction, null);
        Preconditions.checkState(wasOpen, "Attempted to submit non-open transaction %s", transaction);

        if (lastTx == null) {
            // No transaction outstanding, we need to submit it now
            synchronized (this) {
                submitTransaction(transaction);
            }

            return;
        }

        // There is a potentially-racing submitted transaction. Publish the new one, which may end up being
        // picked up by processNextTransaction.
        final boolean success = CURRENT_UPDATER.compareAndSet(this, null, transaction);
        Verify.verify(success);

        // Now a quick check: if the racing transaction completed in between, it may have missed the current
        // transaction, hence we need to re-check
        if (lastTx == null) {
            submitCurrentTransaction();
        }
    }

    private void submitCurrentTransaction() {
        final ShardedDOMDataTreeWriteTransaction current = CURRENT_UPDATER.getAndSet(this, null);
        if (current != null) {
            synchronized (this) {
                submitTransaction(current);
            }
        }
    }

    private void transactionSuccessful(final ShardedDOMDataTreeWriteTransaction tx) {
        LOG.debug("Transaction {} completed successfully", tx.getIdentifier());

        tx.onTransactionSuccess(null);
        transactionCompleted(tx);
    }

    private void transactionFailed(final ShardedDOMDataTreeWriteTransaction tx, final Throwable throwable) {
        LOG.debug("Transaction {} failed", tx.getIdentifier(), throwable);

        tx.onTransactionFailure(throwable);
        transactionCompleted(tx);
    }

    private void transactionCompleted(final ShardedDOMDataTreeWriteTransaction tx) {
        final boolean wasLast = LAST_UPDATER.compareAndSet(this, tx, null);
        if (wasLast) {
            submitCurrentTransaction();
        }
    }

    synchronized void boundToListener(final ShardedDOMDataTreeListenerContext<?> listener) {
        // FIXME: Add option to detach
        Preconditions.checkState(this.attachedListener == null, "Producer %s is already attached to other listener.",
                listener.getListener());
        this.attachedListener = listener;
    }
}
