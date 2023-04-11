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
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.ExecutionException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMActionResult;
import org.opendaylight.mdsal.dom.spi.SimpleDOMActionResult;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.RpcOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

final class BindingOperationFluentFuture<O extends RpcOutput> extends AbstractFuture<DOMActionResult>
        implements BindingRpcFutureAware<O> {
    private final @NonNull ListenableFuture<RpcResult<O>> userFuture;
    private final Class<? extends Action<?, ?, O>> action;
    private final NodeIdentifier identifier;

    private AdapterContext adapterContext;

    BindingOperationFluentFuture(final ListenableFuture<RpcResult<O>> userFuture,
            final Class<? extends Action<?, ?, O>> action, final NodeIdentifier identifier,
            final AdapterContext adapterContext) {
        this.userFuture = requireNonNull(userFuture);
        this.action = requireNonNull(action);
        this.identifier = requireNonNull(identifier);
        this.adapterContext = requireNonNull(adapterContext);
        userFuture.addListener(this::userFutureCompleted, MoreExecutors.directExecutor());
    }

    @Override
    public ListenableFuture<RpcResult<O>> getBindingFuture() {
        return userFuture;
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    private void userFutureCompleted() {
        final DOMActionResult domResult;

        try {
            final RpcResult<O> bindingResult = Futures.getDone(userFuture);
            if (bindingResult.getResult() != null) {
                domResult = new SimpleDOMActionResult(adapterContext.currentSerializer()
                    .toLazyNormalizedNodeActionOutput(action, identifier, bindingResult.getResult()),
                    bindingResult.getErrors());
            } else {
                domResult = new SimpleDOMActionResult(bindingResult.getErrors());
            }
        } catch (ExecutionException e) {
            adapterContext = null;
            setException(e.getCause());
            return;
        } catch (RuntimeException | Error e) {
            adapterContext = null;
            setException(e);
            return;
        }

        adapterContext = null;
        set(domResult);
    }
}
