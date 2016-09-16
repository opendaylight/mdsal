/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.store.inmemory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeModification;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class InMemoryDOMDataTreeShardProducer implements DOMDataTreeShardProducer {

    private abstract static class State {
        /**
         * Allocate a new snapshot.
         *
         * @return A new snapshot
         */
        protected abstract DataTreeSnapshot getSnapshot(Object transactionId);
    }

    private static final class Idle extends State {
        private final InMemoryDOMDataTreeShardProducer producer;

        Idle(final InMemoryDOMDataTreeShardProducer producer) {
            this.producer = Preconditions.checkNotNull(producer);
        }

        @Override
        protected DataTreeSnapshot getSnapshot(Object transactionId) {
            return producer.takeSnapshot();
        }
    }

    /**
     * We have a transaction out there.
     */
    private static final class Allocated extends State {
        private static final AtomicReferenceFieldUpdater<Allocated, DataTreeSnapshot> SNAPSHOT_UPDATER =
                AtomicReferenceFieldUpdater.newUpdater(Allocated.class, DataTreeSnapshot.class, "snapshot");
        private final InmemoryDOMDataTreeShardWriteTransaction transaction;
        private volatile DataTreeSnapshot snapshot;

        Allocated(final InmemoryDOMDataTreeShardWriteTransaction transaction) {
            this.transaction = Preconditions.checkNotNull(transaction);
        }

        public InmemoryDOMDataTreeShardWriteTransaction getTransaction() {
            return transaction;
        }

        @Override
        protected DataTreeSnapshot getSnapshot(Object transactionId) {
            final DataTreeSnapshot ret = snapshot;
            Preconditions.checkState(ret != null,
                    "Could not get snapshot for transaction %s - previous transaction %s is not ready yet",
                    transactionId, transaction.getIdentifier());
            return ret;
        }

        void setSnapshot(final DataTreeSnapshot snapshot) {
            final boolean success = SNAPSHOT_UPDATER.compareAndSet(this, null, snapshot);
            Preconditions.checkState(success, "Transaction %s has already been marked as ready",
                    transaction.getIdentifier());
        }
    }

    /**
     * Producer is logically shut down, no further allocation allowed.
     */
    private static final class Shutdown extends State {
        private final String message;

        Shutdown(final String message) {
            this.message = Preconditions.checkNotNull(message);
        }

        @Override
        protected DataTreeSnapshot getSnapshot(Object transactionId) {
            throw new IllegalStateException(message);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(InMemoryDOMDataTreeShard.class);
    private static final AtomicLong COUNTER = new AtomicLong();

    private final InMemoryDOMDataTreeShard parentShard;
    private final Collection<DOMDataTreeIdentifier> prefixes;

    private static final AtomicReferenceFieldUpdater<InMemoryDOMDataTreeShardProducer, State> STATE_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(InMemoryDOMDataTreeShardProducer.class, State.class, "state");
    private final Idle idleState = new Idle(this);
    private volatile State state;

    InMemoryDOMDataTreeShardProducer(final InMemoryDOMDataTreeShard parentShard,
            final Collection<DOMDataTreeIdentifier> prefixes) {
        this.parentShard = Preconditions.checkNotNull(parentShard);
        this.prefixes = ImmutableSet.copyOf(prefixes);
        state = idleState;
    }

    @Override
    public synchronized InmemoryDOMDataTreeShardWriteTransaction createTransaction() {
//      Preconditions.checkState(currentTx == null || currentTx.isFinished(), "Previous transaction not finished yet.");
        Entry<State, DataTreeSnapshot> entry;
        InmemoryDOMDataTreeShardWriteTransaction ret;
        String transactionId = nextIdentifier();

        do {
            entry = getSnapshot(transactionId);
            ret = parentShard.createTransaction(transactionId, this, prefixes, entry.getValue());
        } while (!recordTransaction(entry.getKey(), ret));

        return ret;
    }

    synchronized void transactionReady(final InmemoryDOMDataTreeShardWriteTransaction tx,
                                       final DataTreeModification modification) {
        final State localState = state;
        LOG.debug("Transaction was readied {}, current state {}", tx.getIdentifier(), localState);

        if (localState instanceof Allocated) {
            final Allocated allocated = (Allocated) localState;
            final InmemoryDOMDataTreeShardWriteTransaction transaction = allocated.getTransaction();
            Preconditions.checkState(tx.equals(transaction),
                    "Mis-ordered ready transaction %s last allocated was %s", tx, transaction);
            allocated.setSnapshot(modification);
        } else {
            LOG.debug("Ignoring transaction {} readiness due to state {}", tx, localState);
        }
    }

    /**
     * Notify the base logic that a previously-submitted transaction has been committed successfully.
     *
     * @param transaction Transaction which completed successfully.
     */
    synchronized void onTransactionCommited(final InmemoryDOMDataTreeShardWriteTransaction transaction) {
        // If the committed transaction was the one we allocated last,
        // we clear it and the ready snapshot, so the next transaction
        // allocated refers to the data tree directly.
        final State localState = state;
        LOG.debug("Transaction {} commit done, current state {}", transaction.getIdentifier(), localState);

        if (!(localState instanceof Allocated)) {
            // This can legally happen if the chain is shut down before the transaction was committed
            // by the backend.
            LOG.debug("Ignoring successful transaction {} in state {}", transaction, localState);
            return;
        }

        final Allocated allocated = (Allocated) localState;
        final InmemoryDOMDataTreeShardWriteTransaction tx = allocated.getTransaction();
        if (!tx.equals(transaction)) {
            LOG.debug("Ignoring non-latest successful transaction {} in state {}", transaction, allocated);
            return;
        }

        if (!STATE_UPDATER.compareAndSet(this, localState, idleState)) {
            LOG.debug("Producer {} has already transitioned from {} to {}, not making it idle", this,
                    localState, state);
        }
    }

    private Entry<State, DataTreeSnapshot> getSnapshot(String transactionId) {
        final State localState = state;
        return new SimpleEntry<>(localState, localState.getSnapshot(transactionId));
    }

    private boolean recordTransaction(final State expected,
                                      final InmemoryDOMDataTreeShardWriteTransaction transaction) {
        final State state = new Allocated(transaction);
        return STATE_UPDATER.compareAndSet(this, expected, state);
    }

    private String nextIdentifier() {
        return "INMEMORY-SHARD-TX-" + COUNTER.getAndIncrement();

    }

    DataTreeSnapshot takeSnapshot() {
        return parentShard.takeSnapshot();
    }

    @Override
    public Collection<DOMDataTreeIdentifier> getPrefixes() {
        return prefixes;
    }
}
