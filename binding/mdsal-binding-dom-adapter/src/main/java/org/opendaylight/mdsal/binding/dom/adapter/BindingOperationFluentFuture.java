/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.ExecutionException;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.dom.api.DOMActionResult;
import org.opendaylight.mdsal.dom.spi.SimpleDOMActionResult;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.RpcOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

final class BindingOperationFluentFuture<O extends RpcOutput> extends AbstractFuture<DOMActionResult>
        implements BindingRpcFutureAware {
    private final ListenableFuture<RpcResult<O>> userFuture;
    private final Class<? extends Action<?, ?, O>> action;
    private final NodeIdentifier identifier;

    private BindingNormalizedNodeSerializer codec;

    BindingOperationFluentFuture(final FluentFuture<RpcResult<O>> userFuture,
            final Class<? extends Action<?, ?, O>> action, final NodeIdentifier identifier,
            final BindingNormalizedNodeSerializer codec) {
        this.userFuture = requireNonNull(userFuture);
        this.action = requireNonNull(action);
        this.identifier = requireNonNull(identifier);
        this.codec = requireNonNull(codec);
        userFuture.addListener(this::userFutureCompleted, MoreExecutors.directExecutor());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public ListenableFuture<RpcResult<?>> getBindingFuture() {
        return (ListenableFuture) userFuture;
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    private void userFutureCompleted() {
        final DOMActionResult domResult;

        try {
            final RpcResult<O> bindingResult = Futures.getDone(userFuture);
            if (bindingResult.getResult() != null) {
                domResult = new SimpleDOMActionResult(
                        codec.toLazyNormalizedNodeActionOutput(action, identifier, bindingResult.getResult()),
                        bindingResult.getErrors());
            } else {
                domResult = new SimpleDOMActionResult(bindingResult.getErrors());
            }
        } catch (ExecutionException e) {
            codec = null;
            setException(e.getCause());
            return;
        } catch (RuntimeException | Error e) {
            codec = null;
            setException(e);
            return;
        }

        codec = null;
        set(domResult);
    }
}
