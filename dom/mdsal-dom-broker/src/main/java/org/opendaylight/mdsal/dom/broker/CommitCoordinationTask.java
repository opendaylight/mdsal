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
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.spi.TransactionCommitFailedExceptionMapper;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreThreePhaseCommitCohort;
import org.opendaylight.yangtools.util.DurationStatisticsTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of blocking three-phase commit-coordination tasks without support of cancellation.
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

    private final DOMStoreThreePhaseCommitCohort cohort;
    private final DOMDataTreeWriteTransaction tx;

    CommitCoordinationTask(final DOMDataTreeWriteTransaction transaction, final DOMStoreThreePhaseCommitCohort cohort) {
        tx = requireNonNull(transaction, "transaction must not be null");
        this.cohort = requireNonNull(cohort, "cohort must not be null");
    }

    @Override
    public CommitInfo call() throws TransactionCommitFailedException {
        var phase = Phase.CAN_COMMIT;
        try {
            LOG.debug("Transaction {}: canCommit Started", tx.getIdentifier());
            canCommitBlocking();

            phase = Phase.PRE_COMMIT;
            LOG.debug("Transaction {}: preCommit Started", tx.getIdentifier());
            preCommitBlocking();

            phase = Phase.DO_COMMIT;
            LOG.debug("Transaction {}: doCommit Started", tx.getIdentifier());
            commitBlocking();

            LOG.debug("Transaction {}: doCommit completed", tx.getIdentifier());
            return CommitInfo.empty();
        } catch (final TransactionCommitFailedException e) {
            LOG.warn("Tx: {} Error during phase {}, starting Abort", tx.getIdentifier(), phase, e);
            abortBlocking(e);
            throw e;
        }
    }

    /**
     * Invokes canCommit on underlying cohort and blocks till the result is returned.
     *
     * <p>
     * Valid state transition is from SUBMITTED to CAN_COMMIT, if currentPhase is not SUBMITTED throws
     * IllegalStateException.
     *
     * @throws TransactionCommitFailedException If cohort fails Commit
     */
    private void canCommitBlocking() throws TransactionCommitFailedException {
        final var future = cohort.canCommit();
        final Boolean result;
        try {
            result = future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw TransactionCommitFailedExceptionMapper.CAN_COMMIT_ERROR_MAPPER.apply(e);
        }

        if (!Boolean.TRUE.equals(result)) {
            throw new TransactionCommitFailedException("Can Commit failed, no detailed cause available.");
        }
    }

    /**
     * Invokes preCommit on underlying cohort and blocks until the result is returned.
     *
     * <p>
     * Valid state transition is from CAN_COMMIT to PRE_COMMIT, if current state is not CAN_COMMIT throws
     * IllegalStateException.
     *
     * @throws TransactionCommitFailedException If cohort fails preCommit
     */
    private void preCommitBlocking() throws TransactionCommitFailedException {
        try {
            cohort.preCommit().get();
        } catch (InterruptedException | ExecutionException e) {
            throw TransactionCommitFailedExceptionMapper.PRE_COMMIT_MAPPER.apply(e);
        }
    }

    /**
     * Invokes commit on underlying cohort and blocks until result is returned.
     *
     * <p>
     * Valid state transition is from PRE_COMMIT to COMMIT, if not throws IllegalStateException.
     *
     * @throws TransactionCommitFailedException If cohort fails preCommit
     */
    private void commitBlocking() throws TransactionCommitFailedException {
        try {
            cohort.commit().get();
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
     * @throws IllegalStateException if abort failed.
     * @throws TransactionCommitFailedException on invocation of this method.
     */
    private void abortBlocking(final TransactionCommitFailedException originalCause)
            throws TransactionCommitFailedException {
        Exception cause = originalCause;
        try {
            cohort.abort().get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Tx: {} Error during Abort.", tx.getIdentifier(), e);
            cause = new IllegalStateException("Abort failed.", e);
            cause.addSuppressed(e);
        }
        Throwables.propagateIfPossible(cause, TransactionCommitFailedException.class);
    }
}
