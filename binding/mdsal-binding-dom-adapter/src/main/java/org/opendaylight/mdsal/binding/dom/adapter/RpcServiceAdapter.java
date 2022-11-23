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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;

class RpcServiceAdapter implements InvocationHandler {
    private final ImmutableMap<Method, RpcInvocationStrategy> rpcNames;
    private final @NonNull Class<? extends RpcService> type;
    private final @NonNull AdapterContext adapterContext;
    private final @NonNull DOMRpcService delegate;
    private final @NonNull RpcService facade;

    RpcServiceAdapter(final Class<? extends RpcService> type, final AdapterContext adapterContext,
            final DOMRpcService domService) {
        this.type = requireNonNull(type);
        this.adapterContext = requireNonNull(adapterContext);
        delegate = requireNonNull(domService);

        final var methods = adapterContext.currentSerializer().getRpcMethodToSchema(type);
        final var rpcBuilder = ImmutableMap.<Method, RpcInvocationStrategy>builderWithExpectedSize(methods.size());
        for (final Entry<Method, RpcDefinition> rpc : methods.entrySet()) {
            rpcBuilder.put(rpc.getKey(),
                RpcInvocationStrategy.of(this, rpc.getKey(), rpc.getValue().asEffectiveStatement()));
        }
        rpcNames = rpcBuilder.build();
        facade = (RpcService) Proxy.newProxyInstance(type.getClassLoader(), new Class[] {type}, this);
    }

    final @NonNull CurrentAdapterSerializer currentSerializer() {
        return adapterContext.currentSerializer();
    }

    final @NonNull DOMRpcService delegate() {
        return delegate;
    }

    final @NonNull RpcService facade() {
        return facade;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) {
        final var strategy = rpcNames.get(method);
        if (strategy != null) {
            if (args.length != 1) {
                throw new IllegalArgumentException("Input must be provided.");
            }
            return strategy.invoke((DataObject) requireNonNull(args[0]));
        }

        switch (method.getName()) {
            case "toString":
                if (method.getReturnType().equals(String.class) && method.getParameterCount() == 0) {
                    return type.getName() + "$Adapter{delegate=" + delegate.toString() + "}";
                }
                break;
            case "hashCode":
                if (method.getReturnType().equals(int.class) && method.getParameterCount() == 0) {
                    return System.identityHashCode(proxy);
                }
                break;
            case "equals":
                if (method.getReturnType().equals(boolean.class) && method.getParameterCount() == 1
                        && method.getParameterTypes()[0] == Object.class) {
                    return proxy == args[0];
                }
                break;
            default:
                break;
        }

        throw new UnsupportedOperationException("Method " + method.toString() + "is unsupported.");
    }
}
