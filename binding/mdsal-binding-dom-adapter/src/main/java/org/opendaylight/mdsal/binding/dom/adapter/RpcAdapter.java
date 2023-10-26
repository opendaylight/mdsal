/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.adapter.RpcInvocationStrategy.ContentRouted;
import org.opendaylight.mdsal.binding.runtime.api.RpcRuntimeType;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.mdsal.dom.spi.ContentRoutedRpcContext;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.binding.RpcInput;
import org.opendaylight.yangtools.yang.binding.contract.Naming;

final class RpcAdapter<T extends Rpc<?, ?>> implements InvocationHandler {
    private final @NonNull AdapterContext adapterContext;
    private final @NonNull DOMRpcService delegate;
    private final @NonNull T facade;
    private final String name;

    private final RpcInvocationStrategy strategy;
    private final Method invokeMethod;

    RpcAdapter(final AdapterContext adapterContext, final DOMRpcService delegate, final Class<T> type) {
        this.adapterContext = requireNonNull(adapterContext);
        this.delegate = requireNonNull(delegate);

        final var serializer = adapterContext.currentSerializer();
        final var rpcType = serializer.getRuntimeContext().getRpcDefinition(type);
        if (rpcType == null) {
            throw new IllegalStateException("Failed to find runtime type for " + type);
        }

        try {
            invokeMethod = type.getMethod(Naming.RPC_INVOKE_NAME, RpcInput.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Failed to find invoke method in " + type, e);
        }

        facade = type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, this));
        name = type.getName();

        strategy = createStrategy(serializer, rpcType);
    }

    private @NonNull RpcInvocationStrategy createStrategy(final CurrentAdapterSerializer serializer,
            final RpcRuntimeType rpcType) {
        final var rpc = rpcType.statement();
        final var contentContext = ContentRoutedRpcContext.forRpc(rpc);
        if (contentContext != null) {
            final var extractor = serializer.findExtractor(rpcType.input());
            if (extractor != null) {
                return new ContentRouted(this, rpc.argument(), contentContext.leaf(), extractor);
            }
        }
        return new RpcInvocationStrategy(this, rpc.argument());
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (invokeMethod.equals(method)) {
            return strategy.invoke((RpcInput) requireNonNull(args[0]));
        }

        switch (method.getName()) {
            case "toString":
                if (method.getReturnType().equals(String.class) && method.getParameterCount() == 0) {
                    return name + "$Adapter{delegate=" + delegate + "}";
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

        if (method.isDefault()) {
            return InvocationHandler.invokeDefault(proxy, method, args);
        }
        throw new UnsupportedOperationException("Method " + method.toString() + " is not supported");
    }

    @NonNull CurrentAdapterSerializer currentSerializer() {
        return adapterContext.currentSerializer();
    }

    @NonNull DOMRpcService delegate() {
        return delegate;
    }

    @NonNull T facade() {
        return facade;
    }
}
