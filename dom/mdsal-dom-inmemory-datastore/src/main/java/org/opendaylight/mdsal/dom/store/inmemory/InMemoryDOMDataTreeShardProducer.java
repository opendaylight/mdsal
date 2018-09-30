/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.spi.shard.DOMDataTreeShardProducer;
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
            this.producer = requireNonNull(producer);
        }

        @Override
        protected DataTreeSnapshot getSnapshot(final Object transactionId) {
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
            this.transaction = requireNonNull(transaction);
        }

        InmemoryDOMDataTreeShardWriteTransaction getTransaction() {
            return transaction;
        }

        @Override
        protected DataTreeSnapshot getSnapshot(final Object transactionId) {
            final DataTreeSnapshot ret = snapshot;
            checkState(ret != null,
                    "Could not get snapshot for transaction %s - previous transaction %s is not ready yet",
                    transactionId, transaction.getIdentifier());
            return ret;
        }

        void setSnapshot(final DataTreeSnapshot snapshot) {
            final boolean success = SNAPSHOT_UPDATER.compareAndSet(this, null, snapshot);
            checkState(success, "Transaction %s has already been marked as ready",
                    transaction.getIdentifier());
        }
    }

    /**
     * Producer is logically shut down, no further allocation allowed.
     */
    private static final class Shutdown extends State {
        private final String message;

        Shutdown(final String message) {
            this.message = requireNonNull(message);
        }

        @Override
        protected DataTreeSnapshot getSnapshot(final Object transactionId) {
            throw new IllegalStateException(message);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(InMemoryDOMDataTreeShardProducer.class);
    private static final AtomicLong COUNTER = new AtomicLong();

    private final InMemoryDOMDataTreeShard parentShard;
    private final Collection<DOMDataTreeIdentifier> prefixes;
    private final Idle idleState = new Idle(this);

    private static final AtomicReferenceFieldUpdater<InMemoryDOMDataTreeShardProducer, State> STATE_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(InMemoryDOMDataTreeShardProducer.class, State.class, "state");
    private volatile State state;

    private InMemoryShardDataModificationFactory modificationFactory;

    InMemoryDOMDataTreeShardProducer(final InMemoryDOMDataTreeShard parentShard,
            final Collection<DOMDataTreeIdentifier> prefixes,
            final InMemoryShardDataModificationFactory modificationFactory) {
        this.parentShard = requireNonNull(parentShard);
        this.prefixes = ImmutableSet.copyOf(prefixes);
        this.modificationFactory = requireNonNull(modificationFactory);
        state = idleState;
    }

    @Override
    public InmemoryDOMDataTreeShardWriteTransaction createTransaction() {
        final String transactionId = nextIdentifier();

        State localState;
        InmemoryDOMDataTreeShardWriteTransaction ret;
        do {
            localState = state;
            ret = parentShard.createTransaction(transactionId, this, localState.getSnapshot(transactionId));
        } while (!STATE_UPDATER.compareAndSet(this, localState, new Allocated(ret)));

        return ret;
    }

    @Override
    public void close() {
        final Shutdown shutdown = new Shutdown("Producer closed");
        if (!STATE_UPDATER.compareAndSet(this, idleState, shutdown)) {
            throw new IllegalStateException("Producer " + this + " in unexpected state " + state);
        }

        // FIXME: This call is ugly, it's better to clean up all by exposing only one entrance,
        // 'closeProducer' of shard or this 'close'.
        getParentShard().closeProducer(this);
        getModificationFactory().close();
    }

    void transactionReady(final InmemoryDOMDataTreeShardWriteTransaction tx, final DataTreeModification modification) {
        final State localState = state;
        LOG.debug("Transaction was readied {}, current state {}", tx.getIdentifier(), localState);

        if (localState instanceof Allocated) {
            final Allocated allocated = (Allocated) localState;
            final InmemoryDOMDataTreeShardWriteTransaction transaction = allocated.getTransaction();
            checkState(tx.equals(transaction), "Mis-ordered ready transaction %s last allocated was %s", tx,
                transaction);
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
    void onTransactionCommited(final InmemoryDOMDataTreeShardWriteTransaction transaction) {
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

    void transactionAborted(final InmemoryDOMDataTreeShardWriteTransaction tx) {
        final State localState = state;
        if (localState instanceof Allocated) {
            final Allocated allocated = (Allocated) localState;
            if (allocated.getTransaction().equals(tx)) {
                final boolean success = STATE_UPDATER.compareAndSet(this, localState, idleState);
                if (!success) {
                    LOG.warn("Transaction {} aborted, but producer {} state already transitioned from {} to {}",
                            tx, this, localState, state);
                }
            }
        }
    }

    private static String nextIdentifier() {
        return "INMEMORY-SHARD-TX-" + COUNTER.getAndIncrement();
    }

    DataTreeSnapshot takeSnapshot() {
        return parentShard.takeSnapshot();
    }

    @Override
    public Collection<DOMDataTreeIdentifier> getPrefixes() {
        return prefixes;
    }

    @NonNull InMemoryDOMDataTreeShard getParentShard() {
        return parentShard;
    }

    InMemoryShardDataModificationFactory getModificationFactory() {
        return modificationFactory;
    }

    void setModificationFactory(final InMemoryShardDataModificationFactory modificationFactory) {
        this.getModificationFactory().close();
        this.modificationFactory = requireNonNull(modificationFactory);
    }
}
