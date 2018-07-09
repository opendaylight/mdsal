/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.ListenableFuture;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.dom.api.DOMRpcException;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.binding.util.RpcServiceInvoker;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class BindingDOMRpcImplementationAdapter implements DOMRpcImplementation {

    private static final Cache<Class<?>, RpcServiceInvoker> SERVICE_INVOKERS
            = CacheBuilder.newBuilder().weakKeys().build();
    // Default implementations are 0, we need to perform some translation, hence we have a slightly higher cost
    private static final int COST = 1;

    private final BindingNormalizedNodeCodecRegistry codec;
    private final RpcServiceInvoker invoker;
    private final RpcService delegate;
    private final QName inputQname;

    <T extends RpcService> BindingDOMRpcImplementationAdapter(final BindingNormalizedNodeCodecRegistry codec,
            final Class<T> type, final Map<SchemaPath, Method> localNameToMethod, final T delegate) {
        try {
            this.invoker = SERVICE_INVOKERS.get(type, () -> {
                final Map<QName, Method> map = new HashMap<>();
                for (Entry<SchemaPath, Method> e : localNameToMethod.entrySet()) {
                    map.put(e.getKey().getLastComponent(), e.getValue());
                }

                return RpcServiceInvoker.from(map);
            });
        } catch (ExecutionException e) {
            throw new IllegalArgumentException("Failed to create invokers for type " + type, e);
        }

        this.codec = Preconditions.checkNotNull(codec);
        this.delegate = Preconditions.checkNotNull(delegate);
        inputQname = YangConstants.operationInputQName(BindingReflections.getQNameModule(type)).intern();
    }

    @Nonnull
    @Override
    public CheckedFuture<DOMRpcResult, DOMRpcException> invokeRpc(@Nonnull final DOMRpcIdentifier rpc,
            final NormalizedNode<?, ?> input) {

        final SchemaPath schemaPath = rpc.getType();
        final DataObject bindingInput = input != null ? deserialize(rpc.getType(), input) : null;
        final ListenableFuture<RpcResult<?>> bindingResult = invoke(schemaPath, bindingInput);
        return transformResult(bindingResult);
    }

    @Override
    public long invocationCost() {
        return COST;
    }

    private DataObject deserialize(final SchemaPath rpcPath, final NormalizedNode<?, ?> input) {
        if (input instanceof BindingDataAware) {
            return ((BindingDataAware) input).bindingData();
        }
        final SchemaPath inputSchemaPath = rpcPath.createChild(inputQname);
        return codec.fromNormalizedNodeRpcData(inputSchemaPath, (ContainerNode) input);
    }

    private ListenableFuture<RpcResult<?>> invoke(final SchemaPath schemaPath, final DataObject input) {
        return invoker.invokeRpc(delegate, schemaPath.getLastComponent(), input);
    }

    private CheckedFuture<DOMRpcResult, DOMRpcException> transformResult(
            final ListenableFuture<RpcResult<?>> bindingResult) {
        return LazyDOMRpcResultFuture.create(codec, bindingResult);
    }
}
