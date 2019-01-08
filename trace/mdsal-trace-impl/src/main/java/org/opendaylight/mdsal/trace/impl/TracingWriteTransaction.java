/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.trace.impl;

import com.google.common.util.concurrent.FluentFuture;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;

class TracingWriteTransaction extends AbstractTracingWriteTransaction
        implements CloseTracked<TracingWriteTransaction> {

    private final CloseTrackedTrait<TracingWriteTransaction> closeTracker;

    TracingWriteTransaction(DOMDataTreeWriteTransaction delegate, TracingBroker tracingBroker,
            CloseTrackedRegistry<TracingWriteTransaction> writeTransactionsRegistry) {
        super(delegate, tracingBroker);
        this.closeTracker = new CloseTrackedTrait<>(writeTransactionsRegistry, this);
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
    public CloseTracked<TracingWriteTransaction> getRealCloseTracked() {
        return this;
    }
}
