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
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of read-only transaction backed by {@link DataTreeSnapshot} which delegates most
 * of its calls to similar methods provided by underlying snapshot.
 *
 * @param <T> identifier type
 */
@Beta
public final class SnapshotBackedReadTransaction<T> extends
        AbstractDOMStoreTransaction<T> implements DOMStoreReadTransaction, SnapshotBackedTransaction {
    private static final Logger LOG = LoggerFactory.getLogger(SnapshotBackedReadTransaction.class);

    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<SnapshotBackedReadTransaction, TransactionClosePrototype>
            CLOSE_IMPL_UPDATER = AtomicReferenceFieldUpdater.newUpdater(SnapshotBackedReadTransaction.class,
                TransactionClosePrototype.class, "closeImpl");
    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<SnapshotBackedReadTransaction, DataTreeSnapshot> SNAPSHOT_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(SnapshotBackedReadTransaction.class, DataTreeSnapshot.class,
                "stableSnapshot");

    @SuppressWarnings("unused")
    private volatile TransactionClosePrototype<T> closeImpl;
    private volatile DataTreeSnapshot stableSnapshot;

    /**
     * Creates a new read-only transaction.
     *
     * @param identifier Transaction Identifier
     * @param debug Enable transaction debugging
     * @param snapshot Snapshot which will be modified.
     */
    SnapshotBackedReadTransaction(final T identifier, final boolean debug, final DataTreeSnapshot snapshot,
            final TransactionClosePrototype<T> closeImpl) {
        super(identifier, debug);
        this.stableSnapshot = requireNonNull(snapshot);
        this.closeImpl = closeImpl;
        LOG.debug("ReadOnly Tx: {} allocated with snapshot {}", identifier, snapshot);
    }

    @Override
    public void close() {
        final DataTreeSnapshot prev = SNAPSHOT_UPDATER.getAndSet(this, null);
        if (prev == null) {
            LOG.debug("Store transaction: {} : previously closed", getIdentifier());
            return;
        }

        LOG.debug("Store transaction: {} : Closed", getIdentifier());
        @SuppressWarnings("unchecked")
        final TransactionClosePrototype<T> impl = CLOSE_IMPL_UPDATER.getAndSet(this, null);
        if (impl != null) {
            impl.transactionClosed(this);
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @Override
    public FluentFuture<Optional<NormalizedNode<?,?>>> read(final YangInstanceIdentifier path) {
        LOG.debug("Tx: {} Read: {}", getIdentifier(), path);
        requireNonNull(path, "Path must not be null.");

        final DataTreeSnapshot snapshot = stableSnapshot;
        if (snapshot == null) {
            return FluentFutures.immediateFailedFluentFuture(new ReadFailedException("Transaction is closed"));
        }

        try {
            return FluentFutures.immediateFluentFuture(snapshot.readNode(path));
        } catch (Exception e) {
            LOG.error("Tx: {} Failed Read of {}", getIdentifier(), path, e);
            return FluentFutures.immediateFailedFluentFuture(new ReadFailedException("Read failed", e));
        }
    }

    @Override
    public FluentFuture<Boolean> exists(final YangInstanceIdentifier path) {
        LOG.debug("Tx: {} Exists: {}", getIdentifier(), path);
        requireNonNull(path, "Path must not be null.");

        return read(path).transform(Optional::isPresent, MoreExecutors.directExecutor());
    }

    @Override
    public Optional<DataTreeSnapshot> getSnapshot() {
        return Optional.ofNullable(stableSnapshot);
    }

    /**
     * Prototype implementation of {@link SnapshotBackedReadTransaction#close()}.
     *
     * <p>
     * This class is intended to be implemented by Transaction factories responsible for allocation
     * of {@link org.opendaylight.mdsal.dom.spi.store.SnapshotBackedReadTransaction} and
     * providing underlying logic for applying implementation.
     *
     * @param <T> identifier type
     */
    public interface TransactionClosePrototype<T> {
        /**
         * Called when a transaction is closed. This is not invoked at most once for every transaction.
         *
         * @param tx Transaction which got closed.
         */
        void transactionClosed(SnapshotBackedReadTransaction<T> tx);
    }
}
