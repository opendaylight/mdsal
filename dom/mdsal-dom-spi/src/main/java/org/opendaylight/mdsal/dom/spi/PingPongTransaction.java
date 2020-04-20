/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.SettableFuture;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadWriteTransaction;

/**
 * Transaction context. Tracks the relationship with the backend transaction.
 * We never leak this class to the user and have it implement the {@link FutureCallback}
 * interface so we have a simple way of propagating the result.
 */
final class PingPongTransaction implements FutureCallback<CommitInfo> {
    private final @NonNull SettableFuture<CommitInfo> future = SettableFuture.create();
    private final @NonNull FluentFuture<CommitInfo> fluent =
        FluentFuture.from(new UncancellableListenableFuture<>(future));
    private final @NonNull DOMDataTreeReadWriteTransaction delegate;

    private @Nullable DOMDataTreeReadWriteTransaction frontendTransaction;

    PingPongTransaction(final DOMDataTreeReadWriteTransaction delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @NonNull DOMDataTreeReadWriteTransaction getTransaction() {
        return delegate;
    }

    DOMDataTreeReadWriteTransaction getFrontendTransaction() {
        return frontendTransaction;
    }

    @NonNull FluentFuture<? extends CommitInfo> completionFuture() {
        return fluent;
    }

    @Override
    public void onSuccess(final CommitInfo result) {
        future.set(result);
    }

    @Override
    public void onFailure(final Throwable throwable) {
        future.setException(throwable);
    }

    void recordFrontendTransaction(final DOMDataTreeReadWriteTransaction tx) {
        if (frontendTransaction == null) {
            frontendTransaction = requireNonNull(tx);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("delegate", delegate).toString();
    }
}
