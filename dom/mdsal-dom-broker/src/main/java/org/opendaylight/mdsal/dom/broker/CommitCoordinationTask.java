/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Throwables;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreThreePhaseCommitCohort;
import org.opendaylight.yangtools.util.DurationStatisticsTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of blocking
 * <a href="https://en.wikipedia.org/wiki/Three-phase_commit_protocol">three-phase commit protocol</a> coordination
 * tasks without support of cancellation. We are assuming the coordinator is the local VM, hence if the coordinator
 * dies, there is no replacement coordinator and the tasks will just timeout from {@link Phase#PRE_COMMIT}.
 */
sealed class CommitCoordinationTask implements Callable<CommitInfo> {
    static final class WithTracker extends CommitCoordinationTask {
        private final DurationStatisticsTracker commitStatTracker;

        WithTracker(final DOMDataTreeWriteTransaction transaction, final DOMStoreThreePhaseCommitCohort cohort,
                final DurationStatisticsTracker commitStatTracker) {
            super(transaction, cohort);
            this.commitStatTracker = requireNonNull(commitStatTracker);
        }

        @Override
        public CommitInfo call() throws TransactionCommitFailedException {
            final long startTime = System.nanoTime();

            try {
                return super.call();
            } finally {
                commitStatTracker.addDuration(System.nanoTime() - startTime);
            }
        }
    }

    private enum Phase {
        CAN_COMMIT,
        PRE_COMMIT,
        DO_COMMIT
    }

    private static final Logger LOG = LoggerFactory.getLogger(CommitCoordinationTask.class);
    private final DOMDataTreeWriteTransaction transaction;
    private final DOMStoreThreePhaseCommitCohort cohort;

    CommitCoordinationTask(final DOMDataTreeWriteTransaction transaction, final DOMStoreThreePhaseCommitCohort cohort) {
        this.transaction = requireNonNull(transaction, "transaction must not be null");
        this.cohort = requireNonNull(cohort, "cohort must not be null");
    }

    @Override
    public CommitInfo call() throws TransactionCommitFailedException {
        var phase = Phase.CAN_COMMIT;
        try {
            LOG.debug("Transaction {}: canCommit Started", transaction.getIdentifier());
            canCommitBlocking();

            phase = Phase.PRE_COMMIT;
            LOG.debug("Transaction {}: preCommit Started", transaction.getIdentifier());
            preCommitBlocking();

            phase = Phase.DO_COMMIT;
            LOG.debug("Transaction {}: doCommit Started", transaction.getIdentifier());
            final var info = commitBlocking();
            LOG.debug("Transaction {}: doCommit completed", transaction.getIdentifier());
            return info;
        } catch (TransactionCommitFailedException e) {
            LOG.warn("Tx: {} Error during phase {}, starting Abort", transaction.getIdentifier(), phase, e);
            abortBlocking(e);
            throw e;
        }
    }

    /**
     * Invokes canCommit on underlying cohort and blocks till the result is returned. Valid state transition to
     * {@link Phase#CAN_COMMIT}.
     *
     * @throws TransactionCommitFailedException If cohort fails Commit
     * @throws IllegalStateException if current phase not {@code SUBMITTED}
     */
    private void canCommitBlocking() throws TransactionCommitFailedException {
        final var future = cohort.canCommit();
        final Boolean result;
        try {
            result = future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw TransactionCommitFailedExceptionMapper.CAN_COMMIT_ERROR_MAPPER.apply(e);
        }

        if (result == null || !result) {
            throw new TransactionCommitFailedException("Can Commit failed, no detailed cause available.");
        }
    }

    /**
     * Invokes preCommit on underlying cohort and blocks until the result is returned. Valid state transition is from
     * {@link Phase#CAN_COMMIT} to {@link Phase#PRE_COMMIT}.
     *
     * @throws IllegalStateException if current state is not {@link Phase#CAN_COMMIT}
     * @throws TransactionCommitFailedException If cohort fails preCommit
     */
    private void preCommitBlocking() throws TransactionCommitFailedException {
        final var future = cohort.preCommit();
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw TransactionCommitFailedExceptionMapper.PRE_COMMIT_MAPPER.apply(e);
        }
    }

    /**
     * Invokes commit on underlying cohort and blocks until result is returned.
     *
     * @return Returns the commit information
     * @throws IllegalStateException if current state is not {@link Phase#PRE_COMMIT}
     * @throws TransactionCommitFailedException If cohort fails preCommit
     */
    private @NonNull CommitInfo commitBlocking() throws TransactionCommitFailedException {
        final var future = cohort.commit();
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw TransactionCommitFailedExceptionMapper.COMMIT_ERROR_MAPPER.apply(e);
        }
    }

    /**
     * Aborts transaction.
     *
     * <p>
     * Invokes {@link DOMStoreThreePhaseCommitCohort#abort()} on underlying cohort, blocks the results. If
     * abort failed throws IllegalStateException, which will contains originalCause as suppressed Exception.
     *
     * <p>
     * If abort was successful throws supplied exception
     *
     * @param originalCause Exception which should be used to fail transaction for consumers of transaction future
     *                      and listeners of transaction failure.
     * @throws TransactionCommitFailedException on invocation of this method.
     * @throws IllegalStateException            if abort failed.
     */
    private void abortBlocking(final TransactionCommitFailedException originalCause)
            throws TransactionCommitFailedException {
        final var future = cohort.abort();

        Exception cause = originalCause;
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Tx: {} Error during Abort.", transaction.getIdentifier(), e);
            cause = new IllegalStateException("Abort failed.", e);
            cause.addSuppressed(e);
        }
        Throwables.propagateIfPossible(cause, TransactionCommitFailedException.class);
    }
}
