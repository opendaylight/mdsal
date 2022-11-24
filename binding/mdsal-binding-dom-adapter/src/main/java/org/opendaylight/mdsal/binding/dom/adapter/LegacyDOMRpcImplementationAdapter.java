/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ListenableFuture;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.opendaylight.mdsal.binding.dom.adapter.invoke.RpcServiceInvoker;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

@Deprecated(since = "11.0.0", forRemoval = true)
final class LegacyDOMRpcImplementationAdapter<T extends RpcService> extends AbstractDOMRpcImplementationAdapter {
    private static final Cache<Class<?>, RpcServiceInvoker> SERVICE_INVOKERS = CacheBuilder.newBuilder().weakKeys()
            .build();

    private final RpcServiceInvoker invoker;
    private final T delegate;

    LegacyDOMRpcImplementationAdapter(final AdapterContext adapterContext, final Class<T> type,
            final Map<QName, Method> localNameToMethod, final T delegate) {
        // FIXME: do not use BindingReflections here
        super(adapterContext, YangConstants.operationInputQName(BindingReflections.getQNameModule(type)).intern());

        try {
            invoker = SERVICE_INVOKERS.get(type, () -> RpcServiceInvoker.from(localNameToMethod));
        } catch (ExecutionException e) {
            throw new IllegalArgumentException("Failed to create invokers for type " + type, e);
        }

        this.delegate = requireNonNull(delegate);
    }

    @Override
    ListenableFuture<RpcResult<?>> invokeRpc(final CurrentAdapterSerializer serializer, final DOMRpcIdentifier rpc,
            final ContainerNode input) {
        final QName rpcType = rpc.getType();
        final var bindingInput = input != null ? deserialize(serializer, rpcType, input) : null;
        return invoker.invokeRpc(delegate, rpcType, bindingInput);
    }
}
