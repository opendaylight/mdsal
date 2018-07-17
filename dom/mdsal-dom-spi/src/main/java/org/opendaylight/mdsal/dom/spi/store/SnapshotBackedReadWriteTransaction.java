/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.store;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import org.opendaylight.mdsal.common.api.ReadFailedException;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of Read-Write transaction which is backed by {@link DataTreeSnapshot} and executed according
 * to {@link SnapshotBackedWriteTransaction.TransactionReadyPrototype}.
 *
 * @param <T> identifier type
 */
@Beta
public final class SnapshotBackedReadWriteTransaction<T> extends
        SnapshotBackedWriteTransaction<T> implements DOMStoreReadWriteTransaction {
    private static final Logger LOG = LoggerFactory.getLogger(SnapshotBackedReadWriteTransaction.class);

    SnapshotBackedReadWriteTransaction(final T identifier, final boolean debug,
            final DataTreeSnapshot snapshot, final TransactionReadyPrototype<T> readyImpl) {
        super(identifier, debug, snapshot, readyImpl);
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @Override
    public FluentFuture<Optional<NormalizedNode<?,?>>> read(final YangInstanceIdentifier path) {
        LOG.debug("Tx: {} Read: {}", getIdentifier(), path);
        checkNotNull(path, "Path must not be null.");

        final Optional<NormalizedNode<?, ?>> result;

        try {
            result = Optional.fromJavaUtil(readSnapshotNode(path));
        } catch (Exception e) {
            LOG.error("Tx: {} Failed Read of {}", getIdentifier(), path, e);
            return FluentFuture.from(Futures.immediateFailedFuture(new ReadFailedException("Read failed", e)));
        }

        if (result == null) {
            return FluentFuture.from(Futures.immediateFailedFuture(new ReadFailedException("Transaction is closed")));
        }

        return FluentFuture.from(Futures.immediateFuture(result));
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @Override
    public FluentFuture<Boolean> exists(final YangInstanceIdentifier path) {
        return read(path).transform(input -> input.isPresent(), MoreExecutors.directExecutor());
    }
}
