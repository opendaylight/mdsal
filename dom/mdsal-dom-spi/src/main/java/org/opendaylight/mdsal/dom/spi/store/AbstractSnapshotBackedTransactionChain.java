/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.store;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import org.opendaylight.mdsal.dom.spi.store.SnapshotBackedReadTransaction.TransactionClosePrototype;
import org.opendaylight.mdsal.dom.spi.store.SnapshotBackedWriteTransaction.TransactionReadyPrototype;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of the {@link DOMStoreTransactionChain} interface relying on {@link DataTreeSnapshot}
 * supplier and backend commit coordinator.
 *
 * @param <T> transaction identifier type
 */
@Beta
public abstract class AbstractSnapshotBackedTransactionChain<T>
        extends TransactionReadyPrototype<T> implements DOMStoreTransactionChain, TransactionClosePrototype<T> {
    private abstract static sealed class State {
        /**
         * Allocate a new snapshot.
         *
         * @return A new snapshot
         */
        abstract DataTreeSnapshot getSnapshot();
    }

    private static final class Idle extends State {
        private final AbstractSnapshotBackedTransactionChain<?> chain;

        Idle(final AbstractSnapshotBackedTransactionChain<?> chain) {
            this.chain = requireNonNull(chain);
        }

        @Override
        DataTreeSnapshot getSnapshot() {
            return chain.takeSnapshot();
        }
    }

    /**
     * We have a transaction out there.
     */
    private static final class Allocated extends State {
        private static final VarHandle SNAPSHOT;

        static {
            try {
                SNAPSHOT = MethodHandles.lookup().findVarHandle(Allocated.class, "snapshot", DataTreeSnapshot.class);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        private final DOMStoreWriteTransaction transaction;
        private volatile DataTreeSnapshot snapshot;

        Allocated(final DOMStoreWriteTransaction transaction) {
            this.transaction = requireNonNull(transaction);
        }

        DOMStoreWriteTransaction getTransaction() {
            return transaction;
        }

        @Override
        DataTreeSnapshot getSnapshot() {
            final var ret = snapshot;
            if (ret == null) {
                throw new IllegalStateException(
                    "Previous transaction " + transaction.getIdentifier() + " is not ready yet");
            }
            return ret;
        }

        void setSnapshot(final DataTreeSnapshot snapshot) {
            if (!SNAPSHOT.compareAndSet(this, null, requireNonNull(snapshot))) {
                throw new IllegalStateException(
                    "Transaction " + transaction.getIdentifier() + " has already been marked as ready");
            }
        }
    }

    /**
     * Chain is logically shut down, no further allocation allowed.
     */
    private static final class Shutdown extends State {
        private final String message;

        Shutdown(final String message) {
            this.message = requireNonNull(message);
        }

        @Override
        DataTreeSnapshot getSnapshot() {
            throw new IllegalStateException(message);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSnapshotBackedTransactionChain.class);
    private static final Shutdown CLOSED = new Shutdown("Transaction chain is closed");
    private static final Shutdown FAILED = new Shutdown("Transaction chain has failed");
    private static final VarHandle STATE;

    static {
        try {
            STATE = MethodHandles.lookup().findVarHandle(AbstractSnapshotBackedTransactionChain.class, "state",
                State.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }


    private final Idle idleState;
    private volatile State state;

    protected AbstractSnapshotBackedTransactionChain() {
        state = idleState = new Idle(this);
    }

    private Entry<State, DataTreeSnapshot> getSnapshot() {
        final State localState = state;
        return new SimpleEntry<>(localState, localState.getSnapshot());
    }

    private boolean recordTransaction(final State expected, final DOMStoreWriteTransaction transaction) {
        return STATE.compareAndSet(this, expected, new Allocated(transaction));
    }

    @Override
    public final DOMStoreReadTransaction newReadOnlyTransaction() {
        return newReadOnlyTransaction(nextTransactionIdentifier());
    }

    protected DOMStoreReadTransaction newReadOnlyTransaction(final T transactionId) {
        final Entry<State, DataTreeSnapshot> entry = getSnapshot();
        return SnapshotBackedTransactions.newReadTransaction(transactionId,
                getDebugTransactions(), entry.getValue(), this);
    }

    @Override
    public void transactionClosed(final SnapshotBackedReadTransaction<T> tx) {
        // Defaults to no-op
    }

    @Override
    public final DOMStoreReadWriteTransaction newReadWriteTransaction() {
        return newReadWriteTransaction(nextTransactionIdentifier());
    }

    protected DOMStoreReadWriteTransaction newReadWriteTransaction(final T transactionId) {
        Entry<State, DataTreeSnapshot> entry;
        DOMStoreReadWriteTransaction ret;

        do {
            entry = getSnapshot();
            ret = new SnapshotBackedReadWriteTransaction<>(transactionId,
                    getDebugTransactions(), entry.getValue(), this);
        } while (!recordTransaction(entry.getKey(), ret));

        return ret;
    }

    @Override
    public final DOMStoreWriteTransaction newWriteOnlyTransaction() {
        return newWriteOnlyTransaction(nextTransactionIdentifier());
    }

    protected DOMStoreWriteTransaction newWriteOnlyTransaction(final T transactionId) {
        Entry<State, DataTreeSnapshot> entry;
        DOMStoreWriteTransaction ret;

        do {
            entry = getSnapshot();
            ret = new SnapshotBackedWriteTransaction<>(transactionId, getDebugTransactions(), entry.getValue(), this);
        } while (!recordTransaction(entry.getKey(), ret));

        return ret;
    }

    @Override
    protected final void transactionAborted(final SnapshotBackedWriteTransaction<T> tx) {
        final var localState = state;
        if (localState instanceof Allocated allocated && allocated.getTransaction().equals(tx)
            && !STATE.compareAndSet(this, allocated, idleState)) {
            LOG.warn("Transaction {} aborted, but chain {} state already transitioned from {} to {}, very strange",
                tx, this, allocated, state);
        }
    }

    @Override
    protected final DOMStoreThreePhaseCommitCohort transactionReady(final SnapshotBackedWriteTransaction<T> tx,
            final DataTreeModification tree,
            final Exception readyError) {
        final var localState = state;

        if (localState instanceof Allocated allocated) {
            final var transaction = allocated.getTransaction();
            if (tx.equals(transaction)) {
                allocated.setSnapshot(tree);
            } else {
                throw new IllegalStateException(
                    "Mis-ordered ready transaction " + tx + " last allocated was " + transaction);
            }
        } else {
            LOG.debug("Ignoring transaction {} readiness due to state {}", tx, localState);
        }

        return createCohort(tx, tree, readyError);
    }

    @Override
    public final void close() {
        var localState = state;

        while (true) {
            if (CLOSED.equals(localState)) {
                throw new IllegalStateException("Transaction chain " + this+ " has been closed");
            }
            if (FAILED.equals(localState)) {
                LOG.debug("Ignoring user close in failed state");
                return;
            }
            final var witness = (State) STATE.compareAndExchange(this, localState, CLOSED);
            if (witness == localState) {
                break;
            }
            localState = witness;
        }
    }

    /**
     * Notify the base logic that a previously-submitted transaction has been committed successfully.
     * @param transaction Transaction which completed successfully.
     */
    protected final void onTransactionCommited(final SnapshotBackedWriteTransaction<T> transaction) {
        // If the committed transaction was the one we allocated last,
        // we clear it and the ready snapshot, so the next transaction
        // allocated refers to the data tree directly.
        final var localState = state;

        if (!(localState instanceof Allocated allocated)) {
            // This can legally happen if the chain is shut down before the transaction was committed
            // by the backend.
            LOG.debug("Ignoring successful transaction {} in state {}", transaction, localState);
            return;
        }

        final var tx = allocated.getTransaction();
        if (!tx.equals(transaction)) {
            LOG.debug("Ignoring non-latest successful transaction {} in state {}", transaction, allocated);
            return;
        }

        if (!STATE.compareAndSet(this, allocated, idleState)) {
            LOG.debug("Transaction chain {} has already transitioned from {} to {}, not making it idle", this,
                allocated, state);
        }
    }

    /**
     * Notify the base logic that a previously-submitted transaction has failed.
     * @param transaction Transaction which failed.
     * @param cause Failure cause
     */
    protected final void onTransactionFailed(
            final SnapshotBackedWriteTransaction<T> transaction, final Throwable cause) {
        LOG.debug("Transaction chain {} failed on transaction {}", this, transaction, cause);
        state = FAILED;
    }

    /**
     * Return the next transaction identifier.
     *
     * @return transaction identifier.
     */
    protected abstract T nextTransactionIdentifier();

    /**
     * Inquire as to whether transactions should record their allocation context.
     * @return True if allocation context should be recorded.
     */
    protected abstract boolean getDebugTransactions();

    /**
     * Take a fresh {@link DataTreeSnapshot} from the backend.
     * @return A new snapshot.
     */
    protected abstract DataTreeSnapshot takeSnapshot();

    /**
     * Create a cohort for driving the transaction through the commit process.
     * @param transaction Transaction handle
     * @param modification {@link DataTreeModification} which needs to be applied to the backend
     * @param operationError Any previous error that could be reported through three phase commit
     * @return A {@link DOMStoreThreePhaseCommitCohort} cohort.
     */
    protected abstract DOMStoreThreePhaseCommitCohort createCohort(SnapshotBackedWriteTransaction<T> transaction,
                                                                   DataTreeModification modification,
                                                                   Exception operationError);
}
