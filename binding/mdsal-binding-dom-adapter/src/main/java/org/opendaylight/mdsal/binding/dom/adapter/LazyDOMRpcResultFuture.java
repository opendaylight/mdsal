/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.dom.api.DOMRpcException;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.mdsal.dom.api.DefaultDOMRpcException;
import org.opendaylight.mdsal.dom.spi.DefaultDOMRpcResult;
import org.opendaylight.yangtools.util.concurrent.ExceptionMapper;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.RpcOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

final class LazyDOMRpcResultFuture<O extends RpcOutput> extends AbstractFuture<DOMRpcResult>
        implements BindingRpcFutureAware<O> {
    private static final ExceptionMapper<DOMRpcException> DOM_RPC_EX_MAPPER = new ExceptionMapper<>("rpc",
            DOMRpcException.class) {
        @Override
        protected DOMRpcException newWithCause(final String message, final Throwable cause) {
            return cause instanceof DOMRpcException ? (DOMRpcException)cause
                    : new DefaultDOMRpcException("RPC failed", cause);
        }
    };

    private final @NonNull ListenableFuture<RpcResult<O>> bindingFuture;
    private final BindingNormalizedNodeSerializer codec;

    private volatile DOMRpcResult result;

    private LazyDOMRpcResultFuture(final ListenableFuture<RpcResult<O>> delegate,
            final BindingNormalizedNodeSerializer codec) {
        bindingFuture = requireNonNull(delegate, "delegate");
        this.codec = requireNonNull(codec, "codec");
    }

    static <O extends RpcOutput> @NonNull LazyDOMRpcResultFuture<O> create(final BindingNormalizedNodeSerializer codec,
            final ListenableFuture<RpcResult<O>> bindingResult) {
        return new LazyDOMRpcResultFuture<>(bindingResult, codec);
    }

    @Override
    public ListenableFuture<RpcResult<O>> getBindingFuture() {
        return bindingFuture;
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        return bindingFuture.cancel(mayInterruptIfRunning);
    }

    @Override
    public void addListener(final Runnable listener, final Executor executor) {
        bindingFuture.addListener(listener, executor);
    }

    @Override
    public DOMRpcResult get() throws InterruptedException, ExecutionException {
        if (result != null) {
            return result;
        }

        try {
            return transformIfNecessary(bindingFuture.get());
        } catch (ExecutionException e) {
            throw new ExecutionException(e.getMessage(), DOM_RPC_EX_MAPPER.apply(e));
        }
    }

    @Override
    public DOMRpcResult get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException,
            TimeoutException {
        if (result != null) {
            return result;
        }

        try {
            return transformIfNecessary(bindingFuture.get(timeout, unit));
        } catch (ExecutionException e) {
            throw new ExecutionException(e.getMessage(), DOM_RPC_EX_MAPPER.apply(e));
        }
    }

    @Override
    public boolean isCancelled() {
        return bindingFuture.isCancelled();
    }

    @Override
    public boolean isDone() {
        return bindingFuture.isDone();
    }

    private synchronized DOMRpcResult transformIfNecessary(final RpcResult<?> input) {
        if (result == null) {
            result = transform(input);
        }
        return result;
    }

    private DOMRpcResult transform(final RpcResult<?> input) {
        if (input.isSuccessful()) {
            final var value = input.getResult() instanceof DataContainer container
                ? codec.toNormalizedNodeRpcData(container) : null;
            return new DefaultDOMRpcResult(value);
        }
        return new DefaultDOMRpcResult(input.getErrors());
    }
}
