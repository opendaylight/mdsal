/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.store;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Throwables;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Optional;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeModification;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of Write transaction which is backed by {@link DataTreeSnapshot} and executed according to
 * {@link TransactionReadyPrototype}.
 *
 * @param <T> Identifier type
 */
@Beta
public class SnapshotBackedWriteTransaction<T> extends AbstractDOMStoreTransaction<T>
        implements DOMStoreWriteTransaction, SnapshotBackedTransaction {
    private static final Logger LOG = LoggerFactory.getLogger(SnapshotBackedWriteTransaction.class);
    private static final VarHandle MUTABLE_TREE;
    private static final VarHandle READY_IMPL;

    static {
        final var lookup = MethodHandles.lookup();
        try {
            READY_IMPL = lookup.findVarHandle(SnapshotBackedWriteTransaction.class, "readyImpl",
                TransactionReadyPrototype.class);
            MUTABLE_TREE = lookup.findVarHandle(SnapshotBackedWriteTransaction.class, "mutableTree",
                DataTreeModification.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    // non-null when not ready
    private volatile TransactionReadyPrototype<T> readyImpl;
    // non-null when not committed/closed
    private volatile DataTreeModification mutableTree;

    SnapshotBackedWriteTransaction(final T identifier, final boolean debug,
            final DataTreeSnapshot snapshot, final TransactionReadyPrototype<T> readyImpl) {
        super(identifier, debug);
        this.readyImpl = requireNonNull(readyImpl, "readyImpl must not be null.");
        mutableTree = snapshot.newModification();
        LOG.debug("Write Tx: {} allocated with snapshot {}", identifier, snapshot);
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @Override
    public void write(final YangInstanceIdentifier path, final NormalizedNode data) {
        checkNotReady();

        final var tree = mutableTree;
        LOG.debug("Tx: {} Write: {}:{}", getIdentifier(), path, data);

        try {
            tree.write(path, data);
            // FIXME: Add checked exception
        } catch (Exception e) {
            LOG.error("Tx: {}, failed to write {}:{} in {}", getIdentifier(), path, data, tree, e);
            // Rethrow original ones if they are subclasses of RuntimeException or Error
            Throwables.throwIfUnchecked(e);
            // FIXME: Introduce proper checked exception
            throw new IllegalArgumentException("Illegal input data.", e);
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @Override
    public void merge(final YangInstanceIdentifier path, final NormalizedNode data) {
        checkNotReady();

        final var tree = mutableTree;
        LOG.debug("Tx: {} Merge: {}:{}", getIdentifier(), path, data);

        try {
            tree.merge(path, data);
            // FIXME: Add checked exception
        } catch (Exception e) {
            LOG.error("Tx: {}, failed to merge {}:{} in {}", getIdentifier(), path, data, tree, e);
            // Rethrow original ones if they are subclasses of RuntimeException or Error
            Throwables.throwIfUnchecked(e);
            // FIXME: Introduce proper checked exception
            throw new IllegalArgumentException("Illegal input data.", e);
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @Override
    public void delete(final YangInstanceIdentifier path) {
        checkNotReady();

        final var tree = mutableTree;
        LOG.debug("Tx: {} Delete: {}", getIdentifier(), path);

        try {
            tree.delete(path);
            // FIXME: Add checked exception
        } catch (Exception e) {
            LOG.error("Tx: {}, failed to delete {} in {}", getIdentifier(), path, tree, e);
            // Rethrow original ones if they are subclasses of RuntimeException or Error
            Throwables.throwIfUnchecked(e);
            // FIXME: Introduce proper checked exception
            throw new IllegalArgumentException("Illegal path to delete.", e);
        }
    }

    /**
     * Exposed for {@link SnapshotBackedReadWriteTransaction}'s sake only. The contract does
     * not allow data access after the transaction has been closed or readied.
     *
     * @param path Path to read
     * @return null if the the transaction has been closed;
     */
    final Optional<NormalizedNode> readSnapshotNode(final YangInstanceIdentifier path) {
        return readyImpl == null ? null : mutableTree.readNode(path);
    }

    private void checkNotReady() {
        checkState(readyImpl != null,
                "Transaction %s is no longer open. No further modifications allowed.", getIdentifier());
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @Override
    public DOMStoreThreePhaseCommitCohort ready() {
        final var ready = acquireReadyImpl();
        if (ready == null) {
            throw new IllegalStateException("Transaction " + getIdentifier() + " is no longer open");
        }
        LOG.debug("Store transaction: {} : Ready", getIdentifier());

        final var tree = mutableTree;
        releaseMutableTree();
        try {
            tree.ready();
            return ready.transactionReady(this, tree, null);
        } catch (RuntimeException e) {
            LOG.debug("Store transaction: {}: unexpected failure when readying", getIdentifier(), e);
            return ready.transactionReady(this, tree, e);
        }
    }

    @Override
    public void close() {
        final var ready = acquireReadyImpl();
        if (ready != null) {
            LOG.debug("Store transaction: {} : Closed", getIdentifier());
            releaseMutableTree();
            ready.transactionAborted(this);
        } else {
            LOG.debug("Store transaction: {} : Closed after submit", getIdentifier());
        }
    }

    private @Nullable TransactionReadyPrototype<T> acquireReadyImpl() {
        return (TransactionReadyPrototype<T>) READY_IMPL.getAndSet(this, null);
    }

    private void releaseMutableTree() {
        MUTABLE_TREE.setRelease(this, null);
    }

    @Override
    public Optional<DataTreeSnapshot> getSnapshot() {
        return readyImpl == null ? Optional.empty() : Optional.ofNullable(mutableTree);
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("ready", readyImpl == null);
    }

    /**
     * Prototype implementation of {@link SnapshotBackedWriteTransaction#ready()}.
     *
     * <p>
     * This class is intended to be implemented by Transaction factories responsible for allocation
     * of {@link org.opendaylight.mdsal.dom.spi.store.SnapshotBackedWriteTransaction} and
     * providing underlying logic for applying implementation.
     *
     * @param <T> identifier type
     */
    public abstract static class TransactionReadyPrototype<T> {
        /**
         * Called when a transaction is closed without being readied. This is not invoked for
         * transactions which are ready.
         *
         * @param tx Transaction which got aborted.
         */
        protected abstract void transactionAborted(SnapshotBackedWriteTransaction<T> tx);

        /**
         * Returns a commit coordinator associated with supplied transactions.
         * This call must not fail.
         *
         * @param tx
         *            Transaction on which ready was invoked.
         * @param tree
         *            Modified data tree which has been constructed.
         * @param readyError
         *            Any error that has already happened when readying.
         * @return DOMStoreThreePhaseCommitCohort associated with transaction
         */
        protected abstract DOMStoreThreePhaseCommitCohort transactionReady(SnapshotBackedWriteTransaction<T> tx,
                                                                           DataTreeModification tree,
                                                                           @Nullable Exception readyError);
    }
}
