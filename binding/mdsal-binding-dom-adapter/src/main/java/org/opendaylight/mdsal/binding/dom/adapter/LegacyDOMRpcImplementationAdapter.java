/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.mdsal.binding.dom.adapter.invoke.RpcMethodInvoker;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

@Deprecated(since = "11.0.0", forRemoval = true)
final class LegacyDOMRpcImplementationAdapter<T extends RpcService> extends AbstractDOMRpcImplementationAdapter {
    private final RpcMethodInvoker invoker;
    private final T delegate;

    LegacyDOMRpcImplementationAdapter(final AdapterContext adapterContext, final Class<T> type, final T delegate,
            final RpcMethodInvoker invoker) {
        // FIXME: do not use BindingReflections here
        super(adapterContext, YangConstants.operationInputQName(BindingReflections.getQNameModule(type)).intern());
        this.delegate = requireNonNull(delegate);
        this.invoker = requireNonNull(invoker);
    }

    @Override
    ListenableFuture<RpcResult<?>> invokeRpc(final CurrentAdapterSerializer serializer, final DOMRpcIdentifier rpc,
            final ContainerNode input) {
        return invoker.invokeOn(delegate, deserialize(serializer, rpc.getType(), input));
    }
}
