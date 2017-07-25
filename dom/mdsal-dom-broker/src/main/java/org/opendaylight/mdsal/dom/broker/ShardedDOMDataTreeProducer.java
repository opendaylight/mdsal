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
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCursorAwareTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducer;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducerBusyException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducerException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeShard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ShardedDOMDataTreeProducer implements DOMDataTreeProducer {
    private static final Logger LOG = LoggerFactory.getLogger(ShardedDOMDataTreeProducer.class);

    private final Set<DOMDataTreeIdentifier> subtrees;
    private final ShardedDOMDataTree dataTree;

    private static final AtomicReferenceFieldUpdater<ShardedDOMDataTreeProducer, ShardedDOMDataTreeWriteTransaction>
        CURRENT_UPDATER = AtomicReferenceFieldUpdater.newUpdater(ShardedDOMDataTreeProducer.class,
            ShardedDOMDataTreeWriteTransaction.class, "currentTx");
    private volatile ShardedDOMDataTreeWriteTransaction currentTx;

    private static final AtomicReferenceFieldUpdater<ShardedDOMDataTreeProducer, ShardedDOMDataTreeWriteTransaction>
        OPEN_UPDATER = AtomicReferenceFieldUpdater.newUpdater(ShardedDOMDataTreeProducer.class,
            ShardedDOMDataTreeWriteTransaction.class, "openTx");
    private volatile ShardedDOMDataTreeWriteTransaction openTx;

    private static final AtomicReferenceFieldUpdater<ShardedDOMDataTreeProducer, ShardedDOMDataTreeWriteTransaction>
        LAST_UPDATER = AtomicReferenceFieldUpdater.newUpdater(ShardedDOMDataTreeProducer.class,
            ShardedDOMDataTreeWriteTransaction.class, "lastTx");
    private volatile ShardedDOMDataTreeWriteTransaction lastTx;

    private static final AtomicIntegerFieldUpdater<ShardedDOMDataTreeProducer> CLOSED_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(ShardedDOMDataTreeProducer.class, "closed");
    private volatile int closed;

    private volatile DOMDataTreeListener attachedListener;
    private volatile ProducerLayout layout;

    private ShardedDOMDataTreeProducer(final ShardedDOMDataTree dataTree,
                               final Collection<DOMDataTreeIdentifier> subtrees,
                               final Map<DOMDataTreeIdentifier, DOMDataTreeShard> shardMap) {
        this.dataTree = Preconditions.checkNotNull(dataTree);
        this.subtrees = ImmutableSet.copyOf(subtrees);
        this.layout = ProducerLayout.create(shardMap);
    }

    static DOMDataTreeProducer create(final ShardedDOMDataTree dataTree,
                                      final Collection<DOMDataTreeIdentifier> subtrees,
                                      final Map<DOMDataTreeIdentifier, DOMDataTreeShard> shardMap) {
        return new ShardedDOMDataTreeProducer(dataTree, subtrees, shardMap);
    }

    private void checkNotClosed() {
        Preconditions.checkState(closed == 0, "Producer is already closed");
    }

    private void checkIdle() {
        Preconditions.checkState(openTx == null, "Transaction %s is still open", openTx);
    }

    void subshardAdded(final Map<DOMDataTreeIdentifier, DOMDataTreeShard> shardMap) {
        checkIdle();

        layout = layout.reshard(shardMap);
    }

    @Override
    public DOMDataTreeCursorAwareTransaction createTransaction(final boolean isolated) {
        checkNotClosed();
        checkIdle();

        LOG.debug("Creating transaction from producer {}", this);

        final ShardedDOMDataTreeWriteTransaction current = CURRENT_UPDATER.getAndSet(this, null);
        final ShardedDOMDataTreeWriteTransaction ret;
        if (isolated) {
            ret = createIsolatedTransaction(layout, current);
        } else {
            ret = createReusedTransaction(layout, current);
        }

        final boolean success = OPEN_UPDATER.compareAndSet(this, null, ret);
        Preconditions.checkState(success, "Illegal concurrent access to producer %s detected", this);
        return ret;
    }

    // This may look weird, but this has side-effects on local's producers, hence it needs to be properly synchronized
    // so that it happens-after submitTransaction() which may have been stolen by a callback.
    @GuardedBy("this")
    private ShardedDOMDataTreeWriteTransaction createTransaction(final ProducerLayout local) {
        return new ShardedDOMDataTreeWriteTransaction(this, local.createTransactions(), local);

    }

    // Isolated case. If we have a previous transaction, submit it before returning this one.
    private synchronized ShardedDOMDataTreeWriteTransaction createIsolatedTransaction(
            final ProducerLayout local, final ShardedDOMDataTreeWriteTransaction current) {
        if (current != null) {
            submitTransaction(current);
        }

        return createTransaction(local);
    }

    private ShardedDOMDataTreeWriteTransaction createReusedTransaction(final ProducerLayout local,
            final ShardedDOMDataTreeWriteTransaction current) {
        if (current != null) {
            // Lock-free fast path
            if (local.equals(current.getLayout())) {
                LOG.debug("Reusing previous transaction {} since there is still a transaction inflight",
                    current.getIdentifier());
                return current;
            }

            synchronized (this) {
                submitTransaction(current);
                return createTransaction(local);
            }
        }

        // Null indicates we have not seen a previous transaction -- which does not mean it is ready, as it may have
        // been stolen and in is process of being submitted.
        synchronized (this) {
            return createTransaction(local);
        }
    }

    @GuardedBy("this")
    private void submitTransaction(final ShardedDOMDataTreeWriteTransaction tx) {
        lastTx = tx;
        tx.doSubmit(this::transactionSuccessful, this::transactionFailed);
    }

    @Override
    public DOMDataTreeProducer createProducer(final Collection<DOMDataTreeIdentifier> subtrees) {
        checkNotClosed();
        checkIdle();

        final ProducerLayout local = layout;

        for (final DOMDataTreeIdentifier s : subtrees) {
            // Check if the subtree was visible at any time
            Preconditions.checkArgument(local.haveSubtree(s), "Subtree %s was never available in producer %s", s, this);
            // Check if the subtree has not been delegated to a child
            final DOMDataTreeProducer child = local.lookupChild(s);
            Preconditions.checkArgument(child == null, "Subtree %s is delegated to child producer %s", s, child);

            // Check if part of the requested subtree is not delegated to a child.
            for (final DOMDataTreeIdentifier c : local.getChildTrees()) {
                Preconditions.checkArgument(!s.contains(c),
                    "Subtree %s cannot be delegated as it is a superset of already-delegated %s", s, c);
            }
        }


        final DOMDataTreeProducer ret;
        synchronized (this) {
            ret = dataTree.createProducer(this, subtrees);
        }

        layout = local.addChild(ret, subtrees);
        return ret;
    }

    boolean isDelegatedToChild(final DOMDataTreeIdentifier path) {
        return layout.lookupChild(path) != null;
    }

    @Override
    public void close() throws DOMDataTreeProducerException {
        if (openTx != null) {
            throw new DOMDataTreeProducerBusyException(String.format("Transaction %s is still open", openTx));
        }

        if (CLOSED_UPDATER.compareAndSet(this, 0, 1)) {
            synchronized (this) {
                dataTree.destroyProducer(this);
            }
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
        final ShardedDOMDataTreeWriteTransaction current = currentTx;
        if (current != null) {
            synchronized (this) {
                if (CURRENT_UPDATER.compareAndSet(this, current, null)) {
                    submitTransaction(current);
                }
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
        // FIXME: transaction failure should result in a hard error
        transactionCompleted(tx);
    }

    private void transactionCompleted(final ShardedDOMDataTreeWriteTransaction tx) {
        final boolean wasLast = LAST_UPDATER.compareAndSet(this, tx, null);
        if (wasLast) {
            submitCurrentTransaction();
        }
    }

    void bindToListener(final DOMDataTreeListener listener) {
        final DOMDataTreeListener local = attachedListener;
        Preconditions.checkState(local == null, "Producer %s is already attached to listener %s", this, local);
        this.attachedListener = Preconditions.checkNotNull(listener);
    }
}
