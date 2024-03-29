/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.store;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.spi.store.SnapshotBackedReadTransaction.TransactionClosePrototype;
import org.opendaylight.mdsal.dom.spi.store.SnapshotBackedWriteTransaction.TransactionReadyPrototype;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeSnapshot;

/**
 * Public utility class for instantiating snapshot-backed transactions.
 */
@Beta
public final class SnapshotBackedTransactions {
    private SnapshotBackedTransactions() {
        // Hidden on purpose
    }

    /**
     * Creates a new read-only transaction.
     *
     * @param identifier Transaction Identifier
     * @param debug Enable transaction debugging
     * @param snapshot Snapshot which will be modified.
     * @return A new read-only transaction
     */
    public static <T> @NonNull SnapshotBackedReadTransaction<T> newReadTransaction(final T identifier,
            final boolean debug, final DataTreeSnapshot snapshot) {
        return new SnapshotBackedReadTransaction<>(identifier, debug, snapshot, null);
    }

    /**
     * Creates a new read-only transaction.
     *
     * @param identifier Transaction Identifier
     * @param debug Enable transaction debugging
     * @param snapshot Snapshot which will be modified.
     * @param closeImpl Implementation of close method
     * @return A new read-only transaction
     */
    public static <T> @NonNull SnapshotBackedReadTransaction<T> newReadTransaction(final T identifier,
            final boolean debug, final DataTreeSnapshot snapshot, final TransactionClosePrototype<T> closeImpl) {
        return new SnapshotBackedReadTransaction<>(identifier, debug, snapshot, requireNonNull(closeImpl));
    }

    /**
     * Creates a new read-write transaction.
     *
     * @param identifier transaction Identifier
     * @param debug Enable transaction debugging
     * @param snapshot Snapshot which will be modified.
     * @param readyImpl Implementation of ready method.
     * @return A new read-write transaction
     */
    public static <T> @NonNull SnapshotBackedReadWriteTransaction<T> newReadWriteTransaction(final T identifier,
            final boolean debug, final DataTreeSnapshot snapshot, final TransactionReadyPrototype<T> readyImpl) {
        return new SnapshotBackedReadWriteTransaction<>(identifier, debug, snapshot, readyImpl);
    }

    /**
     * Creates a new write-only transaction.
     *
     * @param identifier transaction Identifier
     * @param debug Enable transaction debugging
     * @param snapshot Snapshot which will be modified.
     * @param readyImpl Implementation of ready method.
     * @return A new write transaction
     */
    public static <T> @NonNull SnapshotBackedWriteTransaction<T> newWriteTransaction(final T identifier,
            final boolean debug, final DataTreeSnapshot snapshot, final TransactionReadyPrototype<T> readyImpl) {
        return new SnapshotBackedWriteTransaction<>(identifier, debug, snapshot, readyImpl);
    }
}
