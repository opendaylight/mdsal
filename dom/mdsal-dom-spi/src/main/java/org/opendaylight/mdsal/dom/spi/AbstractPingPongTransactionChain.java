/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.mdsal.dom.spi.PingPongTransactionChain.LOG;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.MoreExecutors;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.function.Function;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.checkerframework.checker.lock.qual.Holding;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;
import org.opendaylight.mdsal.dom.api.DOMTransactionChainListener;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * The actual implementation of {@link PingPongTransactionChain}. Split out to allow deeper testing while keeping the
 * externally-visible implementation final.
 */
abstract class AbstractPingPongTransactionChain implements DOMTransactionChain {
    private final DOMTransactionChainListener listener;
    private final DOMTransactionChain delegate;

    @GuardedBy("this")
    @VisibleForTesting
    boolean failed;
    @GuardedBy("this")
    private PingPongTransaction shutdownTx;
    @GuardedBy("this")
    @VisibleForTesting
    Entry<PingPongTransaction, Throwable> deadTx;

    //  This VarHandle is used to manipulate the "ready" transaction. We perform only atomic get-and-set on it.
    private static final VarHandle READY_TX;
    @SuppressWarnings("unused")
    private volatile PingPongTransaction readyTx;

    /*
     * This VarHandle is used to manipulate the "locked" transaction. A locked transaction means we know that the user
     * still holds a transaction and should at some point call us. We perform on compare-and-swap to ensure we properly
     * detect when a user is attempting to allocated multiple transactions concurrently.
     */
    private static final VarHandle LOCKED_TX;
    private volatile PingPongTransaction lockedTx;

    /*
     * This updater is used to manipulate the "inflight" transaction. There can be at most one of these at any given
     * time. We perform only compare-and-swap on these.
     */
    private static final VarHandle INFLIGHT_TX;
    @VisibleForTesting
    volatile PingPongTransaction inflightTx;

    static {
        final var lookup = MethodHandles.lookup();
        try {
            INFLIGHT_TX = lookup.findVarHandle(AbstractPingPongTransactionChain.class, "inflightTx",
                PingPongTransaction.class);
            LOCKED_TX = lookup.findVarHandle(AbstractPingPongTransactionChain.class, "lockedTx",
                PingPongTransaction.class);
            READY_TX = lookup.findVarHandle(AbstractPingPongTransactionChain.class, "readyTx",
                PingPongTransaction.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    AbstractPingPongTransactionChain(final Function<DOMTransactionChainListener, DOMTransactionChain> delegateFactory,
            final DOMTransactionChainListener listener) {
        this.listener = requireNonNull(listener);
        delegate = delegateFactory.apply(new DOMTransactionChainListener() {
            @Override
            public void onTransactionChainFailed(final DOMTransactionChain chain,
                    final DOMDataTreeTransaction transaction, final Throwable cause) {
                LOG.debug("Transaction chain {} reported failure in {}", chain, transaction, cause);
                delegateFailed(chain, cause);
            }

            @Override
            public void onTransactionChainSuccessful(final DOMTransactionChain chain) {
                delegateSuccessful(chain);
            }
        });
    }

    private void delegateSuccessful(final DOMTransactionChain chain) {
        final Entry<PingPongTransaction, Throwable> canceled;
        synchronized (this) {
            // This looks weird, but we need not hold the lock while invoking callbacks
            canceled = deadTx;
        }

        if (canceled == null) {
            listener.onTransactionChainSuccessful(this);
            return;
        }

        // Backend shutdown successful, but we have a batch of transactions we have to report as dead due to the
        // user calling cancel().
        final PingPongTransaction tx = canceled.getKey();
        final Throwable cause = canceled.getValue();
        LOG.debug("Transaction chain {} successful, failing cancelled transaction {}", chain, tx, cause);

        listener.onTransactionChainFailed(this, tx.getFrontendTransaction(), cause);
        tx.onFailure(cause);
    }

    private void delegateFailed(final DOMTransactionChain chain, final Throwable cause) {
        final DOMDataTreeReadWriteTransaction frontend;
        final PingPongTransaction tx = inflightTx;
        if (tx == null) {
            LOG.warn("Transaction chain {} failed with no pending transactions", chain);
            frontend = null;
        } else {
            frontend = tx.getFrontendTransaction();
        }

        listener.onTransactionChainFailed(this, frontend, cause);

        synchronized (this) {
            failed = true;

            /*
             * If we do not have a locked transaction, we need to ensure that the backend transaction is cancelled.
             * Otherwise we can defer until the user calls us.
             */
            if (lockedTx == null) {
                processIfReady();
            }
        }
    }

    private synchronized @NonNull PingPongTransaction slowAllocateTransaction() {
        checkState(shutdownTx == null, "Transaction chain %s has been shut down", this);

        if (deadTx != null) {
            throw new IllegalStateException(String.format(
                "Transaction chain %s has failed due to transaction %s being canceled", this, deadTx.getKey()),
                deadTx.getValue());
        }

        final DOMDataTreeReadWriteTransaction delegateTx = delegate.newReadWriteTransaction();
        final PingPongTransaction newTx = new PingPongTransaction(delegateTx);

        final Object witness = LOCKED_TX.compareAndExchange(this, null, newTx);
        if (witness != null) {
            delegateTx.cancel();
            throw new IllegalStateException(
                    String.format("New transaction %s raced with transaction %s", newTx, witness));
        }

        return newTx;
    }

    private @Nullable PingPongTransaction acquireReadyTx() {
        return (PingPongTransaction) READY_TX.getAndSet(this, null);
    }

    private @NonNull PingPongTransaction allocateTransaction() {
        // Step 1: acquire current state
        final PingPongTransaction oldTx = acquireReadyTx();

        // Slow path: allocate a delegate transaction
        if (oldTx == null) {
            return slowAllocateTransaction();
        }

        // Fast path: reuse current transaction. We will check failures and similar on commit().
        final Object witness = LOCKED_TX.compareAndExchange(this, null, oldTx);
        if (witness != null) {
            // Ouch. Delegate chain has not detected a duplicate transaction allocation. This is the best we can do.
            oldTx.getTransaction().cancel();
            throw new IllegalStateException(String.format("Reusable transaction %s raced with transaction %s", oldTx,
                witness));
        }

        return oldTx;
    }

    /**
     * This forces allocateTransaction() on a slow path, which has to happen after this method has completed executing.
     * Also inflightTx may be updated outside the lock, hence we need to re-check.
     */
    @Holding("this")
    private void processIfReady() {
        if (inflightTx == null) {
            final PingPongTransaction tx = acquireReadyTx();
            if (tx != null) {
                processTransaction(tx);
            }
        }
    }

    /**
     * Process a ready transaction. The caller needs to ensure that each transaction is seen only once by this method.
     *
     * @param tx Transaction which needs processing.
     */
    @Holding("this")
    private void processTransaction(final @NonNull PingPongTransaction tx) {
        if (failed) {
            LOG.debug("Cancelling transaction {}", tx);
            tx.getTransaction().cancel();
            return;
        }

        LOG.debug("Submitting transaction {}", tx);
        final Object witness = INFLIGHT_TX.compareAndExchange(this, null, tx);
        if (witness != null) {
            LOG.warn("Submitting transaction {} while {} is still running", tx, witness);
        }

        tx.getTransaction().commit().addCallback(new FutureCallback<CommitInfo>() {
            @Override
            public void onSuccess(final CommitInfo result) {
                transactionSuccessful(tx, result);
            }

            @Override
            public void onFailure(final Throwable throwable) {
                transactionFailed(tx, throwable);
            }
        }, MoreExecutors.directExecutor());
    }

    /*
     * We got invoked from the data store thread. We need to do two things:
     * 1) release the in-flight transaction
     * 2) process the potential next transaction
     *
     * We have to perform 2) under lock. We could perform 1) without locking, but that means the CAS result may
     * not be accurate, as a user thread may submit the ready transaction before we acquire the lock -- and checking
     * for next transaction is not enough, as that may have also be allocated (as a result of a quick
     * submit/allocate/submit between 1) and 2)). Hence we'd end up doing the following:
     * 1) CAS of inflightTx
     * 2) take lock
     * 3) volatile read of inflightTx
     *
     * Rather than doing that, we keep this method synchronized, hence performing only:
     * 1) take lock
     * 2) CAS of inflightTx
     *
     * Since the user thread is barred from submitting the transaction (in processIfReady), we can then proceed with
     * the knowledge that inflightTx is null -- processTransaction() will still do a CAS, but that is only for
     * correctness.
     */
    private synchronized void processNextTransaction(final PingPongTransaction tx) {
        final Object witness = INFLIGHT_TX.compareAndExchange(this, tx, null);
        checkState(witness == tx, "Completed transaction %s while %s was submitted", tx, witness);

        final PingPongTransaction nextTx = acquireReadyTx();
        if (nextTx == null) {
            final PingPongTransaction local = shutdownTx;
            if (local != null) {
                processTransaction(local);
                delegate.close();
                shutdownTx = null;
            }
        } else {
            processTransaction(nextTx);
        }
    }

    private void transactionSuccessful(final PingPongTransaction tx, final CommitInfo result) {
        LOG.debug("Transaction {} completed successfully", tx);

        tx.onSuccess(result);
        processNextTransaction(tx);
    }

    private void transactionFailed(final PingPongTransaction tx, final Throwable throwable) {
        LOG.debug("Transaction {} failed", tx, throwable);

        tx.onFailure(throwable);
        processNextTransaction(tx);
    }

    private void readyTransaction(final @NonNull PingPongTransaction tx) {
        // First mark the transaction as not locked.
        final Object lockedWitness = LOCKED_TX.compareAndExchange(this, tx, null);
        checkState(lockedWitness == tx, "Attempted to submit transaction %s while we have %s", tx, lockedWitness);
        LOG.debug("Transaction {} unlocked", tx);

        /*
         * The transaction is ready. It will then be picked up by either next allocation,
         * or a background transaction completion callback.
         */
        final Object readyWitness = READY_TX.compareAndExchange(this, null, tx);
        checkState(readyWitness == null, "Transaction %s collided on ready state with %s", tx, readyWitness);
        LOG.debug("Transaction {} readied", tx);

        /*
         * We do not see a transaction being in-flight, so we need to take care of dispatching
         * the transaction to the backend. We are in the ready case, we cannot short-cut
         * the checking of readyTx, as an in-flight transaction may have completed between us
         * setting the field above and us checking.
         */
        if (inflightTx == null) {
            synchronized (this) {
                processIfReady();
            }
        }
    }

    /**
     * Transaction cancellation is a heavyweight operation. We only support cancelation of a locked transaction
     * and return false for everything else. Cancelling such a transaction will result in all transactions in the
     * batch to be cancelled.
     *
     * @param tx Backend shared transaction
     * @param frontendTx transaction
     * @return {@code true} if the transaction was cancelled successfully
     */
    private synchronized boolean cancelTransaction(final PingPongTransaction tx,
            final DOMDataTreeReadWriteTransaction frontendTx) {
        // Attempt to unlock the operation.
        final Object witness = LOCKED_TX.compareAndExchange(this, tx, null);
        verify(witness == tx, "Cancelling transaction %s collided with locked transaction %s", tx, witness);

        // Cancel the backend transaction, so we do not end up leaking it.
        final boolean backendCancelled = tx.getTransaction().cancel();

        if (failed) {
            // The transaction has failed, this is probably the user just clearing up the transaction they had. We have
            // already cancelled the transaction anyway,
            return true;
        }

        // We have dealt with cancelling the backend transaction and have unlocked the transaction. Since we are still
        // inside the synchronized block, any allocations are blocking on the slow path. Now we have to decide the fate
        // of this transaction chain.
        //
        // If there are no other frontend transactions in this batch we are aligned with backend state and we can
        // continue processing.
        if (frontendTx.equals(tx.getFrontendTransaction())) {
            if (backendCancelled) {
                LOG.debug("Cancelled transaction {} was head of the batch, resuming processing", tx);
                return true;
            }

            // Backend refused to cancel the transaction. Reinstate it to locked state.
            final Object reinstateWitness = LOCKED_TX.compareAndExchange(this, null, tx);
            verify(reinstateWitness == null, "Reinstating transaction %s collided with locked transaction %s", tx,
                reinstateWitness);
            return false;
        }

        if (!backendCancelled) {
            LOG.warn("Backend transaction cannot be cancelled during cancellation of {}, attempting to continue", tx);
        }

        // There are multiple frontend transactions in this batch. We have to report them as failed, which dooms this
        // transaction chain, too. Since we just came off of a locked transaction, we do not have a ready transaction
        // at the moment, but there may be some transaction in-flight. So we proceed to shutdown the backend chain
        // and mark the fact that we should be turning its completion into a failure.
        deadTx = Map.entry(tx, new CancellationException("Transaction " + frontendTx + " canceled").fillInStackTrace());
        delegate.close();
        return true;
    }

    @Override
    public final synchronized void close() {
        final PingPongTransaction notLocked = lockedTx;
        checkState(notLocked == null, "Attempted to close chain with outstanding transaction %s", notLocked);

        // This is not reliable, but if we observe it to be null and the process has already completed,
        // the backend transaction chain will throw the appropriate error.
        checkState(shutdownTx == null, "Attempted to close an already-closed chain");

        // This may be a reaction to our failure callback, in that case the backend is already shutdown
        if (deadTx != null) {
            LOG.debug("Delegate {} is already closed due to failure {}", delegate, deadTx);
            return;
        }

        // Force allocations on slow path, picking up a potentially-outstanding transaction
        final PingPongTransaction tx = acquireReadyTx();

        if (tx != null) {
            // We have one more transaction, which needs to be processed somewhere. If we do not
            // a transaction in-flight, we need to push it down ourselves.
            // If there is an in-flight transaction we will schedule this last one into a dedicated
            // slot. Allocation slow path will check its presence and fail, the in-flight path will
            // pick it up, submit and immediately close the chain.
            if (inflightTx == null) {
                processTransaction(tx);
                delegate.close();
            } else {
                shutdownTx = tx;
            }
        } else {
            // Nothing outstanding, we can safely shutdown
            delegate.close();
        }
    }

    @Override
    public final DOMDataTreeReadTransaction newReadOnlyTransaction() {
        return new PingPongReadTransaction(allocateTransaction());
    }

    @Override
    public final DOMDataTreeReadWriteTransaction newReadWriteTransaction() {
        final PingPongTransaction tx = allocateTransaction();
        final DOMDataTreeReadWriteTransaction ret = new PingPongReadWriteTransaction(tx);
        tx.recordFrontendTransaction(ret);
        return ret;
    }

    @Override
    public final DOMDataTreeWriteTransaction newWriteOnlyTransaction() {
        return newReadWriteTransaction();
    }

    private final class PingPongReadTransaction implements DOMDataTreeReadTransaction {
        private final @NonNull PingPongTransaction tx;

        PingPongReadTransaction(final PingPongTransaction tx) {
            this.tx = requireNonNull(tx);
        }

        @Override
        public FluentFuture<Optional<NormalizedNode>> read(final LogicalDatastoreType store,
                final YangInstanceIdentifier path) {
            return tx.getTransaction().read(store, path);
        }

        @Override
        public FluentFuture<Boolean> exists(final LogicalDatastoreType store, final YangInstanceIdentifier path) {
            return tx.getTransaction().exists(store, path);
        }

        @Override
        public Object getIdentifier() {
            return tx.getTransaction().getIdentifier();
        }

        @Override
        public void close() {
            readyTransaction(tx);
        }
    }

    private final class PingPongReadWriteTransaction extends ForwardingDOMDataReadWriteTransaction {
        private final @NonNull PingPongTransaction tx;

        private boolean isOpen = true;

        PingPongReadWriteTransaction(final PingPongTransaction tx) {
            this.tx = requireNonNull(tx);
        }

        @Override
        public FluentFuture<? extends CommitInfo> commit() {
            readyTransaction(tx);
            isOpen = false;
            return tx.getCommitFuture().transform(ignored -> CommitInfo.empty(), MoreExecutors.directExecutor());
        }

        @Override
        public boolean cancel() {
            if (isOpen && cancelTransaction(tx, this)) {
                isOpen = false;
                return true;
            }
            return false;
        }

        @Override
        protected DOMDataTreeReadWriteTransaction delegate() {
            return tx.getTransaction();
        }
    }
}
