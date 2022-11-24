/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.ListenableFuture;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.binding.RpcInput;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;

final class BindingDOMRpcImplementationAdapter extends AbstractDOMRpcImplementationAdapter {
    @SuppressWarnings("rawtypes")
    private final @NonNull Rpc delegate;

    BindingDOMRpcImplementationAdapter(final AdapterContext adapterContext, final QName rpcName,
            final Rpc<?, ?> delegate) {
        super(adapterContext, rpcName);
        this.delegate = requireNonNull(delegate);
    }

    @Override
    @SuppressWarnings("unchecked")
    ListenableFuture<RpcResult<?>> invokeRpc(final RpcInput input) {
        return delegate.invoke(input);
    }
}
