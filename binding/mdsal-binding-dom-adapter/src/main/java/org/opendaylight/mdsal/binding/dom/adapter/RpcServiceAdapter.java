/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.mdsal.binding.dom.adapter.RpcInvocationStrategy.ContentRouted;
import org.opendaylight.mdsal.binding.runtime.api.RpcRuntimeType;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.mdsal.dom.spi.ContentRoutedRpcContext;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;

final class RpcServiceAdapter extends AbstractRpcAdapter {
    private final ImmutableMap<Method, RpcInvocationStrategy> rpcNames;

    RpcServiceAdapter(final Class<? extends RpcService> type, final AdapterContext adapterContext,
            final DOMRpcService domService) {
        super(adapterContext, domService, type);

        // FIXME: This should be probably part of BindingRuntimeContext and RpcServices perhaps should have their own
        //        RuntimeType. At any rate, we are dancing around to reconstruct information RpcServiceRuntimeType would
        //        carry and the runtime context would bind to actual classes.
        final var serializer = adapterContext.currentSerializer();
        final var runtimeContext = serializer.getRuntimeContext();
        final var types = runtimeContext.getTypes();
        final var qnameModule = BindingReflections.getQNameModule(type);

        // We are dancing a bit here to reconstruct things a RpcServiceRuntimeType could easily hold
        final var module = runtimeContext.getEffectiveModelContext().findModuleStatement(qnameModule)
            .orElseThrow(() -> new IllegalStateException("No module found for " + qnameModule + " service " + type));
        rpcNames = module.streamEffectiveSubstatements(RpcEffectiveStatement.class)
            .map(rpc -> {
                final var rpcName = rpc.argument();
                final var inputClz = runtimeContext.getRpcInput(rpcName);
                final var methodName = BindingMapping.getRpcMethodName(rpcName);

                final Method method;
                try {
                    method = type.getMethod(methodName, inputClz);
                } catch (NoSuchMethodException e) {
                    throw new IllegalStateException("Cannot find RPC method for " + rpc, e);
                }

                final var runtimeType = types.schemaTreeChild(rpcName);
                if (!(runtimeType instanceof RpcRuntimeType rpcType)) {
                    throw new IllegalStateException("Unexpected run-time type " + runtimeType + " for " + rpcName);
                }

                final var contentContext = ContentRoutedRpcContext.forRpc(rpc);
                final RpcInvocationStrategy strategy;
                if (contentContext != null) {
                    final var extractor = serializer.findExtractor(rpcType.input());
                    strategy = extractor == null ? new RpcInvocationStrategy(this, rpcName)
                        : new ContentRouted(this, rpcName, contentContext.leaf(), extractor);
                } else {
                    strategy = new RpcInvocationStrategy(this, rpcName);
                }

                return Map.entry(method, strategy);
            })
            .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final var strategy = rpcNames.get(method);
        if (strategy != null) {
            if (args.length != 1) {
                throw new IllegalArgumentException("Input must be provided.");
            }
            return strategy.invoke((DataObject) requireNonNull(args[0]));
        }
        return defaultInvoke(proxy, method, args);
    }
}
