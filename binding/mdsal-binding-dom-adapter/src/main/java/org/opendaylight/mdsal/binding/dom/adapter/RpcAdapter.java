/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.base.VerifyException;
import java.lang.reflect.Method;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.yangtools.yang.binding.RpcInput;

final class RpcAdapter extends AbstractRpcAdapter {
    RpcAdapter(final AdapterContext adapterContext, final DOMRpcService delegate, final Class<?> type) {
        super(adapterContext, delegate, type);
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if ("invoke".equals(method.getName()) && method.getParameterCount() == 1) {
            final var input = args[0];
            if (input instanceof RpcInput rpcInput) {
                return invoke(rpcInput);
            } else if (input == null) {
                return invoke();
            } else {
                throw new VerifyException("Unexpected input " + args[0]);
            }
        }
        return defaultInvoke(proxy, method, args);
    }

    private Object invoke() {
        throw new UnsupportedOperationException();
    }

    private Object invoke(final RpcInput input) {
        throw new UnsupportedOperationException();
    }
}
