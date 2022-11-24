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
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

final class BindingDOMRpcImplementationAdapter extends AbstractDOMRpcImplementationAdapter {
    private final @NonNull Rpc<?, ?> delegate;
    private final @NonNull QName rpcName;

    BindingDOMRpcImplementationAdapter(final AdapterContext adapterContext, final Rpc<?, ?> delegate,
            final QName rpcName) {
        super(adapterContext, YangConstants.operationInputQName(rpcName.getModule()).intern());
        this.delegate = requireNonNull(delegate);
        this.rpcName = requireNonNull(rpcName);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    ListenableFuture<RpcResult<?>> invokeRpc(final CurrentAdapterSerializer serializer, final DOMRpcIdentifier rpc,
            final ContainerNode input) {
        return ((Rpc) delegate).invoke(deserialize(serializer, rpcName, input));
    }
}
