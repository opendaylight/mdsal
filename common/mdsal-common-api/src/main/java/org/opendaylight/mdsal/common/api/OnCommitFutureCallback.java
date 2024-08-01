/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A {@link FutureCallback} delegating to an {@link OnCommitCallback}, dealing with exceptions in an uniform manner.
 */
public record OnCommitFutureCallback(@NonNull OnCommitCallback delegate)
        implements FutureCallback<@NonNull CommitInfo> {
    public OnCommitFutureCallback {
        requireNonNull(delegate);
    }

    public static void addTo(final @NonNull OnCommitCallback delegate, final @NonNull Executor executor,
            final @NonNull ListenableFuture<? extends @NonNull CommitInfo> future) {
        Futures.addCallback(future, new OnCommitFutureCallback(delegate) , executor);
    }

    @Override
    public void onSuccess(final CommitInfo result) {
        delegate.onSuccess(result);
    }

    @Override
    public void onFailure(final Throwable cause) {
        delegate.onFailure(mapException(cause));
    }

    private static @NonNull TransactionCommitFailedException mapException(final Throwable ex) {
        return switch (ex) {
            case CancellationException ce -> newWithCause("was cancelled.", ce);
            case ExecutionException ee -> mapException(ee.getCause());
            case InterruptedException ie -> newWithCause("was interupted.", ie);
            case TransactionCommitFailedException tcfe -> tcfe;
            default -> newWithCause(" encountered an unexpected failure", ex);
        };
    }

    private static @NonNull TransactionCommitFailedException newWithCause(final String what, final Throwable cause) {
        return new TransactionCommitFailedException("commit " + what, cause);
    }
}
