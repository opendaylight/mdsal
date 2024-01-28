/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A {@link ListenableFuture} specialization for {@link DOMRpcService} which can either:
 * <ul>
 *   <li>produce a {@link DOMRpcResult}, or</li>
 *   <li>fail with a {@link DOMRpcException}</li>
 * </ul>
 */
public sealed class DOMRpcFuture extends AbstractFuture<@NonNull DOMRpcResult> permits SettableDOMRpcFuture {
    private final @Nullable ListenableFuture<? extends DOMRpcResult> sourceFuture;

    DOMRpcFuture() {
        sourceFuture = null;
    }

    public static @NonNull DOMRpcFuture of(final DOMRpcResult result) {
        final var future = new DOMRpcFuture();
        future.set(requireNonNull(result));
        return future;
    }

    public static @NonNull DOMRpcFuture ofFuture(final ListenableFuture<? extends DOMRpcResult> sourceFuture) {
        final var future = new DOMRpcFuture();
        future.setFuture(Futures.catchingAsync(sourceFuture, Throwable.class,
            failure -> Futures.immediateFailedFuture(mapFailure(failure)), MoreExecutors.directExecutor()));
        return future;
    }

    public static @NonNull DOMRpcFuture failed(final DOMRpcException cause) {
        final var future = new DOMRpcFuture();
        future.setException(requireNonNull(cause));
        return future;
    }

    @Override
    public final boolean cancel(final boolean mayInterruptIfRunning) {
        return super.cancel(false);
    }

    /**
     * Add a callback to invoke when this future completes, or immediately if it is already complete.
     *
     * @param callback Callback to invoke
     * @return This future
     * @throws NullPointerException if {@code callback} is {@code null}
     */
    public final @NonNull DOMRpcFuture addCallback(final DOMRpcCallback callback) {
        Futures.addCallback(this, callback, MoreExecutors.directExecutor());
        return this;
    }

    /**
     * Get the {@link DOMRpcResult}.
     *
     * @return The result
     * @throws DOMRpcException if this future failed or this call is interrupted.
     */
    public final @NonNull DOMRpcResult getOrThrow() throws DOMRpcException {
        try {
            return get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DefaultDOMRpcException("Interrupted while waiting", e);
        } catch (ExecutionException e) {
            Throwables.throwIfInstanceOf(e.getCause(), DOMRpcException.class);
            throw new DefaultDOMRpcException("Operation failed", e);
        }
    }

    @Override
    public final DOMRpcResult get() throws InterruptedException, ExecutionException {
        final var source = sourceFuture;
        if (source == null) {
            return super.get();
        }

        try {
            return source.get();
        } catch (ExecutionException e) {
            throw new ExecutionException( mapFailure(e.getCause()));
        }
    }

    @Override
    public final DOMRpcResult get(final long timeout, final TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        final var source = sourceFuture;
        if (source == null) {
            return super.get(timeout, unit);
        }

        try {
            return source.get(timeout, unit);
        } catch (ExecutionException e) {
            throw new ExecutionException(mapFailure(e.getCause()));
        }
    }

    private static @NonNull DOMRpcException mapFailure(final Throwable failure) {
        return failure instanceof DOMRpcException dre ? dre : new DefaultDOMRpcException("Unexpected failure", failure);
    }

    /**
     * Return the argument supplied to {@link #ofFuture(ListenableFuture)}, if this future was instantiated from there.
     * Otherwise returns {@code null}
     *
     * @return Source future, or {@code null} if not applicable
     */
    public final @Nullable ListenableFuture<? extends DOMRpcResult> sourceFuture() {
        return sourceFuture;
    }
}
