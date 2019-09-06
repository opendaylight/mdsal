/*
 * Copyright (c) 2020 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.opendaylight.mdsal.binding.dom.adapter.invoke.RpcMethodInvoker;
import org.opendaylight.mdsal.binding.dom.adapter.invoke.RpcServiceInvoker;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeCodec;
import org.opendaylight.yangtools.yang.binding.RpcOutput;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

final class RpcServiceClassContext {
    private static final Cache<Class<?>, RpcServiceInvoker> SERVICE_INVOKERS = CacheBuilder.newBuilder().weakKeys()
            .build();
    private final ImmutableMap<SchemaNodeIdentifier, RpcServiceMethodContext> methods;
    private final RpcServiceInvoker invoker;

    <T extends RpcService>  RpcServiceClassContext(final AdapterContext adapterContext, final Class<T> type,
                                                   final Map<QName, Method> localNameToMethod) {
        try {
            this.invoker = SERVICE_INVOKERS.get(type, () -> {
                final Map<QName, Method> map = new HashMap<>();
                for (Map.Entry<QName, Method> e : localNameToMethod.entrySet()) {
                    map.put(e.getKey(), e.getValue());
                }

                return RpcServiceInvoker.from(map);
            });
        } catch (ExecutionException e) {
            throw new IllegalArgumentException("Failed to create invokers for type " + type, e);
        }
        final  Map<SchemaNodeIdentifier, RpcServiceMethodContext> methodsBuilder = new HashMap<>();
        for (Map.Entry<QName, Method> e : localNameToMethod.entrySet()) {
            final QName rpcQName = e.getKey();
            final Absolute inputPath = Absolute.of(rpcQName, YangConstants.operationInputQName(rpcQName.getModule()));
            final Absolute outputPath = Absolute.of(rpcQName, YangConstants.operationOutputQName(rpcQName.getModule()));
            final BindingNormalizedNodeCodec<?> inputCodec = adapterContext.currentSerializer()
                    .getRpcInputCodec(inputPath);
            final BindingNormalizedNodeCodec<RpcOutput> outputCodec = (BindingNormalizedNodeCodec<RpcOutput>)
                    adapterContext.currentSerializer().getRpcInputCodec(outputPath);
            final RpcMethodInvoker methodInvoker = this.invoker.getMethodInvoker(rpcQName);
            final RpcServiceMethodContext methodContext = new RpcServiceMethodContext(inputCodec, outputCodec,
                    methodInvoker);
            methodsBuilder.put(Absolute.of(rpcQName), methodContext);
        }
        methods = ImmutableMap.copyOf(methodsBuilder);
    }

    public RpcServiceMethodContext getMethodContext(final QName rpcQName) {
        return methods.get(SchemaNodeIdentifier.Absolute.of(rpcQName));
    }
}
