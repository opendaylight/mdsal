/*
 * Copyright (c) 2016 Red Hat, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.trace.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.FluentFuture;
import java.util.Optional;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadWriteTransaction;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

class TracingReadWriteTransaction
    extends AbstractTracingWriteTransaction
        implements DOMDataTreeReadWriteTransaction, CloseTracked<TracingReadWriteTransaction> {

    private final CloseTrackedTrait<TracingReadWriteTransaction> closeTracker;
    private final DOMDataTreeReadWriteTransaction delegate;

    TracingReadWriteTransaction(final DOMDataTreeReadWriteTransaction delegate, final TracingBroker tracingBroker,
            final CloseTrackedRegistry<TracingReadWriteTransaction> readWriteTransactionsRegistry) {
        super(delegate, tracingBroker);
        this.closeTracker = new CloseTrackedTrait<>(readWriteTransactionsRegistry, this);
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public FluentFuture<Optional<NormalizedNode>> read(final LogicalDatastoreType store,
            final YangInstanceIdentifier yiid) {
        return delegate.read(store, yiid);
    }

    @Override
    public FluentFuture<Boolean> exists(final LogicalDatastoreType store, final YangInstanceIdentifier yiid) {
        return delegate.exists(store, yiid);
    }

    @Override
    public FluentFuture<? extends CommitInfo> commit() {
        closeTracker.removeFromTrackedRegistry();
        return super.commit();
    }

    @Override
    public boolean cancel() {
        closeTracker.removeFromTrackedRegistry();
        return super.cancel();
    }

    @Override
    public @Nullable StackTraceElement[] getAllocationContextStackTrace() {
        return closeTracker.getAllocationContextStackTrace();
    }

    @Override
    public CloseTracked<TracingReadWriteTransaction> getRealCloseTracked() {
        return this;
    }
}
