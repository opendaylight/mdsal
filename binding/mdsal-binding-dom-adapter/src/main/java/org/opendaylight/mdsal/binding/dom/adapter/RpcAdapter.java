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

final class RpcAdapter extends AbstractRpcAdapter {
    RpcAdapter(final AdapterContext adapterContext, final DOMRpcService delegate, final Class<?> type) {
        super(adapterContext, delegate, type);
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if ("invoke".equals(method.getName()) && method.getParameterCount() == 1) {
            // FIXME: finish this
            throw new UnsupportedOperationException();
        }
        return defaultInvoke(proxy, method, args);
    }
}
