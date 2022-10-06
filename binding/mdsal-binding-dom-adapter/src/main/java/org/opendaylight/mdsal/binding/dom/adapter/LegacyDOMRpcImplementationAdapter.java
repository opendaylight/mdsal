/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Preconditions.checkArgument;
import static org.opendaylight.mdsal.binding.dom.adapter.StaticConfiguration.ENABLE_CODEC_SHORTCUT;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ListenableFuture;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.opendaylight.mdsal.binding.dom.adapter.invoke.RpcServiceInvoker;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingLazyContainerNode;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

final class LegacyDOMRpcImplementationAdapter<T extends RpcService> extends AbstractDOMRpcImplementationAdapter<T> {
    private static final Cache<Class<?>, RpcServiceInvoker> SERVICE_INVOKERS = CacheBuilder.newBuilder().weakKeys()
            .build();

    private final RpcServiceInvoker invoker;
    private final QName inputQname;

    LegacyDOMRpcImplementationAdapter(final AdapterContext adapterContext,
            final Class<T> type, final Map<QName, Method> localNameToMethod, final T delegate) {
        super(adapterContext, delegate);

        try {
            invoker = SERVICE_INVOKERS.get(type, () -> RpcServiceInvoker.from(localNameToMethod));
        } catch (ExecutionException e) {
            throw new IllegalArgumentException("Failed to create invokers for type " + type, e);
        }

        inputQname = YangConstants.operationInputQName(BindingReflections.getQNameModule(type)).intern();
    }

    @Override
    public ListenableFuture<DOMRpcResult> invokeRpc(final DOMRpcIdentifier rpc, final ContainerNode input) {
        final QName rpcType = rpc.getType();
        final CurrentAdapterSerializer serializer = currentSerializer();
        final DataObject bindingInput = input != null ? deserialize(serializer, rpcType, input) : null;
        final ListenableFuture<RpcResult<?>> bindingResult = invoke(rpcType, bindingInput);
        return LazyDOMRpcResultFuture.create(serializer, bindingResult);
    }

    private DataObject deserialize(final CurrentAdapterSerializer serializer, final QName rpcType,
            final ContainerNode input) {
        if (ENABLE_CODEC_SHORTCUT && input instanceof BindingLazyContainerNode) {
            return ((BindingLazyContainerNode<?>) input).getDataObject();
        }

        checkArgument(inputQname.equals(input.getIdentifier().getNodeType()),
            "Unexpected RPC %s input %s", rpcType, input);
        return serializer.fromNormalizedNodeRpcData(Absolute.of(rpcType, inputQname), input);
    }

    private ListenableFuture<RpcResult<?>> invoke(final QName rpcType, final DataObject input) {
        return invoker.invokeRpc(delegate(), rpcType, input);
    }
}
