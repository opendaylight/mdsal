/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.UncheckedExecutionException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import org.eclipse.jdt.annotation.NonNull;

/**
 * An {@link OnCommitCallback} compatible with {@link FutureCallback}. Provides ease of integration with
 * {@link ListenableFuture}. Utility {@link ForwardingOnCommitFutureCallback} exists to provide forwarding the result
 * to a delegate.
 */
public interface OnCommitFutureCallback extends OnCommitCallback, FutureCallback<@NonNull CommitInfo> {
    @Override
    default void onFailure(final Throwable cause) {
        onFailure(map(cause));
    }

    private static @NonNull TransactionCommitFailedException map(final Throwable cause) {
        return switch (cause) {
            case TransactionCommitFailedException tcfe -> tcfe;
            case CancellationException ce -> newWithCause("was cancelled.", ce);
            case InterruptedException ie -> newWithCause("was interupted.", ie);
            case ExecutionException ee -> map(ee.getCause());
            case UncheckedExecutionException uee -> map(uee.getCause());
            default -> newWithCause("encountered an unexpected failure", cause);
        };
    }

    private static @NonNull TransactionCommitFailedException newWithCause(final String what, final Throwable cause) {
        return new TransactionCommitFailedException("commit " + what, cause);
    }
}
