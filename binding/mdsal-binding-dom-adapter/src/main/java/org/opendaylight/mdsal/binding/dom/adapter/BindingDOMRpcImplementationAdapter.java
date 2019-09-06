/*
 * Copyright (c) 2020 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.util.concurrent.ListenableFuture;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class BindingDOMRpcImplementationAdapter implements DOMRpcImplementation {
    private final RpcService delegate;
    private final RpcServiceMethodContext context;

    // Default implementations are 0, we need to perform some translation, hence we have a slightly higher cost
    private static final int COST = 1;

    public BindingDOMRpcImplementationAdapter(final RpcService delegate,
            final RpcServiceMethodContext context) {
        this.delegate = delegate;
        this.context = context;
    }


    @Override
    public @NonNull ListenableFuture<? extends DOMRpcResult> invokeRpc(final @NonNull DOMRpcIdentifier rpc,
            final @NonNull NormalizedNode<?, ?> input) {
        final ListenableFuture<RpcResult<?>> bindingResult = context.invokeRpc(delegate, input);
        return LazyDOMRpcResultFuture.create(context.getOutputCodec(), bindingResult);
    }

    @Override
    public long invocationCost() {
        return COST;
    }

}
