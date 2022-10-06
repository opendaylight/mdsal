/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import java.lang.reflect.Method;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.binding.RpcInput;

final class RpcAdapter<T extends Rpc<?, ?>> extends AbstractRpcAdapter {
    private final RpcInvocationStrategy strategy;
    private final Method invokeMethod;

    RpcAdapter(final AdapterContext adapterContext, final DOMRpcService delegate, final Class<T> type) {
        super(adapterContext, delegate, type);

        final var desc = adapterContext.currentSerializer().getRpcInvokeDescription(type);
        invokeMethod = desc.getKey();
        strategy = RpcInvocationStrategy.of(this, invokeMethod, desc.getValue().asEffectiveStatement());
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        return invokeMethod.equals(method) ? strategy.invoke((RpcInput) args[0]) : defaultInvoke(proxy, method, args);
    }
}
