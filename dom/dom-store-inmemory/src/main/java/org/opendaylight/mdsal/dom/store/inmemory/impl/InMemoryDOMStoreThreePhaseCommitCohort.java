/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.store.inmemory.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.OptimisticLockFailedException;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.mdsal.dom.spi.store.AbstractDOMStoreTransaction;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreThreePhaseCommitCohort;
import org.opendaylight.mdsal.dom.spi.store.SnapshotBackedWriteTransaction;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.data.tree.api.ConflictingModificationAppliedException;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class InMemoryDOMStoreThreePhaseCommitCohort implements DOMStoreThreePhaseCommitCohort {
    private static final Logger LOG = LoggerFactory.getLogger(InMemoryDOMStoreThreePhaseCommitCohort.class);
    private static final ListenableFuture<Boolean> CAN_COMMIT_FUTURE = Futures.immediateFuture(Boolean.TRUE);

    private final SnapshotBackedWriteTransaction<String> transaction;
    private final DataTreeModification modification;
    private final Exception operationError;
    private final InMemoryTree tree;

    @VisibleForTesting
    DataTreeCandidate candidate;

    InMemoryDOMStoreThreePhaseCommitCohort(final InMemoryTree tree,
            final SnapshotBackedWriteTransaction<String> transaction, final DataTreeModification modification,
            final Exception operationError) {
        this.transaction = requireNonNull(transaction);
        this.modification = requireNonNull(modification);
        this.tree = requireNonNull(tree);
        this.operationError = operationError;
    }

    private static void warnDebugContext(final AbstractDOMStoreTransaction<?> transaction) {
        final var ctx = transaction.getDebugContext();
        if (ctx != null) {
            LOG.warn("Transaction {} has been allocated in the following context", transaction.getIdentifier(), ctx);
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @Override
    public final ListenableFuture<Boolean> canCommit() {
        if (operationError != null) {
            return Futures.immediateFailedFuture(operationError);
        }

        try {
            tree.validate(modification);
            LOG.debug("Store Transaction: {} can be committed", getTransaction().getIdentifier());
            return CAN_COMMIT_FUTURE;
        } catch (ConflictingModificationAppliedException e) {
            LOG.warn("Store Tx: {} Conflicting modification for {}.", getTransaction().getIdentifier(),
                    e.getPath());
            warnDebugContext(getTransaction());
            return Futures.immediateFailedFuture(new OptimisticLockFailedException("Optimistic lock failed.", e));
        } catch (DataValidationFailedException e) {
            LOG.warn("Store Tx: {} Data Precondition failed for {}.", getTransaction().getIdentifier(),
                    e.getPath(), e);
            warnDebugContext(getTransaction());

            // For debugging purposes, allow dumping of the modification. Coupled with the above
            // precondition log, it should allow us to understand what went on.
            LOG.trace("Store Tx: {} modifications: {} tree: {}", getTransaction().getIdentifier(), modification, tree);

            return Futures.immediateFailedFuture(
                    new TransactionCommitFailedException("Data did not pass validation.", e));
        } catch (Exception e) {
            LOG.warn("Unexpected failure in validation phase", e);
            return Futures.immediateFailedFuture(e);
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @Override
    public final ListenableFuture<Empty> preCommit() {
        try {
            candidate = tree.prepare(modification);
            return Empty.immediateFuture();
        } catch (Exception e) {
            LOG.warn("Unexpected failure in pre-commit phase", e);
            return Futures.immediateFailedFuture(e);
        }
    }

    @Override
    public final ListenableFuture<Empty> abort() {
        candidate = null;
        return Empty.immediateFuture();
    }

    protected final SnapshotBackedWriteTransaction<String> getTransaction() {
        return transaction;
    }

    @Override
    public ListenableFuture<CommitInfo> commit() {
        final var local = candidate;
        if (local == null) {
            throw new IllegalStateException("Proposed subtree must be computed");
        }

        // The commit has to occur atomically with regard to listener registrations.
        tree.commit(local);
        return CommitInfo.emptyFluentFuture();
    }
}
