/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.mdsal.binding.dom.adapter.StaticConfiguration.ENABLE_CODEC_SHORTCUT;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ListenableFuture;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.adapter.invoke.RpcServiceInvoker;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class BindingDOMRpcImplementationAdapter implements DOMRpcImplementation {
    private static final class ClassContext implements Immutable {
        final RpcServiceInvoker invoker;
        final @NonNull QName inputQname;

        ClassContext(final Class<? extends RpcService> type, final Map<SchemaPath, Method> localNameToMethod) {
            inputQname = YangConstants.operationInputQName(BindingReflections.getQNameModule(type)).intern();

            final Map<QName, Method> map = new HashMap<>();
            for (Entry<SchemaPath, Method> e : localNameToMethod.entrySet()) {
                map.put(e.getKey().getLastComponent(), e.getValue());
            }

            this.invoker = RpcServiceInvoker.from(map);
        }
    }

    private static final Cache<Class<?>, ClassContext> CLASS_CONTEXTS = CacheBuilder.newBuilder().weakKeys().build();

    // Default implementations are 0, we need to perform some translation, hence we have a slightly higher cost
    private static final int COST = 1;

    private final BindingNormalizedNodeCodecRegistry codec;
    private final RpcServiceInvoker invoker;
    private final RpcService delegate;
    private final QName inputQname;

    <T extends RpcService> BindingDOMRpcImplementationAdapter(final BindingNormalizedNodeCodecRegistry codec,
            final Class<T> type, final Map<SchemaPath, Method> localNameToMethod, final T delegate) {
        this.codec = requireNonNull(codec);
        this.delegate = requireNonNull(delegate);

        final ClassContext context;
        try {
            context = CLASS_CONTEXTS.get(type, () -> new ClassContext(type, localNameToMethod));
        } catch (ExecutionException e) {
            throw new IllegalArgumentException("Failed to create invokers for type " + type, e);
        }

        this.invoker = context.invoker;
        this.inputQname = context.inputQname;
    }

    @Override
    public ListenableFuture<DOMRpcResult> invokeRpc(final DOMRpcIdentifier rpc, final NormalizedNode<?, ?> input) {
        final SchemaPath schemaPath = rpc.getType();
        final DataObject bindingInput = input != null ? deserialize(rpc.getType(), input) : null;
        final ListenableFuture<RpcResult<?>> bindingResult = invoke(schemaPath, bindingInput);
        return LazyDOMRpcResultFuture.create(codec, bindingResult);
    }

    @Override
    public long invocationCost() {
        return COST;
    }

    private DataObject deserialize(final SchemaPath rpcPath, final NormalizedNode<?, ?> input) {
        if (ENABLE_CODEC_SHORTCUT && input instanceof BindingDataAware) {
            return ((BindingDataAware) input).bindingData();
        }
        final SchemaPath inputSchemaPath = rpcPath.createChild(inputQname);
        return codec.fromNormalizedNodeRpcData(inputSchemaPath, (ContainerNode) input);
    }

    private ListenableFuture<RpcResult<?>> invoke(final SchemaPath schemaPath, final DataObject input) {
        return invoker.invokeRpc(delegate, schemaPath.getLastComponent(), input);
    }
}
