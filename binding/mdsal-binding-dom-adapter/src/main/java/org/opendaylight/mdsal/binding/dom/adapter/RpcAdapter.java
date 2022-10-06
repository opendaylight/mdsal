/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import java.lang.reflect.Method;
import org.opendaylight.mdsal.binding.dom.adapter.RpcInvocationStrategy.ContentRouted;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.mdsal.dom.spi.ContentRoutedRpcContext;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.binding.RpcInput;

final class RpcAdapter<T extends Rpc<?, ?>> extends AbstractRpcAdapter {
    private final RpcInvocationStrategy strategy;
    private final Method invokeMethod;

    RpcAdapter(final AdapterContext adapterContext, final DOMRpcService delegate, final Class<T> type) {
        super(adapterContext, delegate, type);

        final var serializer = adapterContext.currentSerializer();
        final var rpcType = serializer.getRuntimeContext().getRpcDefinition(type);
        if (rpcType == null) {
            throw new IllegalStateException("Failed to find runtime type for " + type);
        }

        try {
            invokeMethod = type.getMethod("invoke", RpcInput.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Failed to find invoke method in " + type, e);
        }

        final var rpc = rpcType.statement();
        final var contentContext = ContentRoutedRpcContext.forRpc(rpc);
        if (contentContext != null) {
            final var extractor = serializer.findExtractor(rpcType.input());
            strategy = extractor == null ? new RpcInvocationStrategy(this, rpc.argument())
                : new ContentRouted(this, rpc.argument(), contentContext.leaf(), extractor);
        } else {
            strategy = new RpcInvocationStrategy(this, rpc.argument());
        }
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        return invokeMethod.equals(method) ? strategy.invoke((RpcInput) args[0]) : defaultInvoke(proxy, method, args);
    }
}
