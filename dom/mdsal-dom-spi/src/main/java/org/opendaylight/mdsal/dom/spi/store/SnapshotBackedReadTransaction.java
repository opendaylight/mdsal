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
import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import org.opendaylight.mdsal.common.api.ReadFailedException;
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
    private volatile DataTreeSnapshot stableSnapshot;

    /**
     * Creates a new read-only transaction.
     *
     * @param identifier Transaction Identifier
     * @param debug Enable transaction debugging
     * @param snapshot Snapshot which will be modified.
     */
    SnapshotBackedReadTransaction(final T identifier, final boolean debug, final DataTreeSnapshot snapshot) {
        super(identifier, debug);
        this.stableSnapshot = requireNonNull(snapshot);
        LOG.debug("ReadOnly Tx: {} allocated with snapshot {}", identifier, snapshot);
    }

    @Override
    public void close() {
        LOG.debug("Store transaction: {} : Closed", getIdentifier());
        stableSnapshot = null;
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @Override
    public CheckedFuture<Optional<NormalizedNode<?,?>>, ReadFailedException> read(final YangInstanceIdentifier path) {
        LOG.debug("Tx: {} Read: {}", getIdentifier(), path);
        requireNonNull(path, "Path must not be null.");

        final DataTreeSnapshot snapshot = stableSnapshot;
        if (snapshot == null) {
            return Futures.immediateFailedCheckedFuture(new ReadFailedException("Transaction is closed"));
        }

        try {
            return Futures.immediateCheckedFuture(Optional.fromJavaUtil(snapshot.readNode(path)));
        } catch (Exception e) {
            LOG.error("Tx: {} Failed Read of {}", getIdentifier(), path, e);
            return Futures.immediateFailedCheckedFuture(new ReadFailedException("Read failed", e));
        }
    }

    @Override
    public CheckedFuture<Boolean, ReadFailedException> exists(final YangInstanceIdentifier path) {
        LOG.debug("Tx: {} Exists: {}", getIdentifier(), path);
        requireNonNull(path, "Path must not be null.");

        try {
            return Futures.immediateCheckedFuture(read(path).checkedGet().isPresent());
        } catch (ReadFailedException e) {
            return Futures.immediateFailedCheckedFuture(e);
        }
    }

    @Override
    public java.util.Optional<DataTreeSnapshot> getSnapshot() {
        return java.util.Optional.ofNullable(stableSnapshot);
    }
}
