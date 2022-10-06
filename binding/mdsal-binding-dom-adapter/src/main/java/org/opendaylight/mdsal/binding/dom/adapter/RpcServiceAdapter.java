/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.lang.reflect.Method;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;

final class RpcServiceAdapter extends AbstractRpcAdapter {
    private final ImmutableMap<Method, RpcInvocationStrategy> rpcNames;

    RpcServiceAdapter(final Class<? extends RpcService> type, final AdapterContext adapterContext,
            final DOMRpcService delegate) {
        super(adapterContext, delegate, type);

        final var methods = currentSerializer().getRpcMethodToSchema(type);
        final var rpcBuilder = ImmutableMap.<Method, RpcInvocationStrategy>builderWithExpectedSize(methods.size());
        for (var rpc : methods.entrySet()) {
            final var method = rpc.getKey();
            rpcBuilder.put(method, RpcInvocationStrategy.of(this, method, rpc.getValue().asEffectiveStatement()));
        }
        rpcNames = rpcBuilder.build();
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final var strategy = rpcNames.get(method);
        if (strategy != null) {
            if (args.length != 1) {
                throw new IllegalArgumentException("Input must be provided.");
            }
            return strategy.invoke((DataObject) requireNonNull(args[0]));
        }
        return defaultInvoke(proxy, method, args);
    }
}
