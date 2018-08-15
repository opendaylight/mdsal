/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.operation;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.dom.api.DOMActionException;
import org.opendaylight.mdsal.dom.api.DOMActionNotAvailableException;
import org.opendaylight.mdsal.dom.api.DOMActionResult;
import org.opendaylight.mdsal.dom.spi.SimpleDOMActionResult;
import org.opendaylight.yangtools.util.concurrent.ExceptionMapper;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * DOM operation result from Binding.
 */
@Beta
final class LazyDOMActionResultFuture extends AbstractFuture<DOMActionResult> {
    private static final ExceptionMapper<DOMActionException> DOM_ACTION_EX_MAPPER =
            new ExceptionMapper<DOMActionException>("action", DOMActionException.class) {
        @Override
        protected DOMActionException newWithCause(String message, Throwable cause) {
            return cause instanceof DOMActionException ? (DOMActionException)cause
                    : new DOMActionNotAvailableException("Action failed", cause);
        }
    };

    private final ListenableFuture<RpcResult<?>> bindingFuture;
    private final BindingNormalizedNodeCodecRegistry codec;
    private volatile DOMActionResult result;

    private LazyDOMActionResultFuture(final ListenableFuture<RpcResult<?>> delegate,
                                      final BindingNormalizedNodeCodecRegistry codec) {
        this.bindingFuture = requireNonNull(delegate, "delegate");
        this.codec = requireNonNull(codec, "codec");
    }

    static FluentFuture<DOMActionResult> create(final BindingNormalizedNodeCodecRegistry codec,
            final ListenableFuture<RpcResult<?>> bindingResult) {
        return new LazyDOMActionResultFuture(bindingResult, codec);
    }

    ListenableFuture<RpcResult<?>> getBindingFuture() {
        return bindingFuture;
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        return bindingFuture.cancel(mayInterruptIfRunning);
    }

    @Override
    public void addListener(@Nonnull final Runnable listener, @Nonnull final Executor executor) {
        bindingFuture.addListener(listener, executor);
    }

    @Override
    public DOMActionResult get() throws InterruptedException, ExecutionException {
        if (result != null) {
            return result;
        }

        try {
            return transformIfNecessary(bindingFuture.get());
        } catch (ExecutionException e) {
            throw new ExecutionException(e.getMessage(), DOM_ACTION_EX_MAPPER.apply(e));
        }
    }

    @Override
    public DOMActionResult get(@Nonnull final long timeout, final TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        if (result != null) {
            return result;
        }

        try {
            return transformIfNecessary(bindingFuture.get(timeout, unit));
        } catch (ExecutionException e) {
            throw new ExecutionException(e.getMessage(), DOM_ACTION_EX_MAPPER.apply(e));
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

    private synchronized DOMActionResult transformIfNecessary(final RpcResult<?> input) {
        if (result == null) {
            result = transform(input);
        }
        return result;
    }

    private DOMActionResult transform(final RpcResult<?> input) {
        if (input.isSuccessful()) {
            final Object inputData = input.getResult();
            if (inputData instanceof DataContainer) {
                return new SimpleDOMActionResult(codec.toNormalizedNodeOperationData((TreeNode) inputData),
                    ImmutableList.of());
            } else {
                return new SimpleDOMActionResult(ImmutableList.of());
            }
        }
        return new SimpleDOMActionResult(input.getErrors());
    }

}
