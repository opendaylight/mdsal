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
import org.opendaylight.mdsal.dom.api.DOMRpcService;

abstract sealed class AbstractRpcAdapter implements InvocationHandler permits RpcAdapter, RpcServiceAdapter {
    private final @NonNull DOMRpcService delegate;
    private final @NonNull AdapterContext adapterContext;
    private final @NonNull Object facade;
    private final String name;

    AbstractRpcAdapter(final AdapterContext adapterContext, final DOMRpcService delegate, final Class<?> type) {
        this.adapterContext = requireNonNull(adapterContext);
        this.delegate = requireNonNull(delegate);
        facade = Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, this);
        name = type.getName();
    }

    final @NonNull CurrentAdapterSerializer currentSerializer() {
        return adapterContext.currentSerializer();
    }

    final @NonNull DOMRpcService delegate() {
        return delegate;
    }

    final @NonNull Object facade() {
        return facade;
    }

    @SuppressWarnings("checkstyle:illegalThrows")
    final Object defaultInvoke(final Object proxy, final Method method, final Object [] args) throws Throwable {
        switch (method.getName()) {
            case "toString":
                if (method.getReturnType().equals(String.class) && method.getParameterCount() == 0) {
                    return name + "$Adapter{delegate=" + delegate() + "}";
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
}