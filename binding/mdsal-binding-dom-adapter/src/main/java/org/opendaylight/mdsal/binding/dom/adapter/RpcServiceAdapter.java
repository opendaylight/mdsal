/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.mdsal.binding.dom.adapter.StaticConfiguration.ENABLE_CODEC_SHORTCUT;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map.Entry;
import java.util.Optional;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.mdsal.dom.spi.RpcRoutingStrategy;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

class RpcServiceAdapter implements InvocationHandler {
    private final ImmutableMap<Method, RpcInvocationStrategy> rpcNames;
    private final Class<? extends RpcService> type;
    private final AdapterContext adapterContext;
    private final DOMRpcService delegate;
    private final RpcService proxy;

    RpcServiceAdapter(final Class<? extends RpcService> type, final AdapterContext adapterContext,
            final DOMRpcService domService) {
        this.type = requireNonNull(type);
        this.adapterContext = requireNonNull(adapterContext);
        this.delegate = requireNonNull(domService);

        final ImmutableBiMap<Method, RpcDefinition> methods = adapterContext.currentSerializer()
                .getRpcMethodToSchema(type);
        final Builder<Method, RpcInvocationStrategy> rpcBuilder = ImmutableMap.builderWithExpectedSize(methods.size());
        for (final Entry<Method, RpcDefinition> rpc : methods.entrySet()) {
            rpcBuilder.put(rpc.getKey(), createStrategy(rpc.getKey(), rpc.getValue()));
        }
        rpcNames = rpcBuilder.build();
        proxy = (RpcService) Proxy.newProxyInstance(type.getClassLoader(), new Class[] {type}, this);
    }

    private RpcInvocationStrategy createStrategy(final Method method, final RpcDefinition schema) {
        final RpcRoutingStrategy strategy = RpcRoutingStrategy.from(schema);
        if (strategy.isContextBasedRouted()) {
            return new RoutedStrategy(schema.getPath(), method, strategy.getLeaf());
        }
        return new NonRoutedStrategy(schema.getPath());
    }

    RpcService getProxy() {
        return proxy;
    }

    @Override
    @SuppressWarnings("checkstyle:hiddenField")
    public Object invoke(final Object proxy, final Method method, final Object[] args) {
        final RpcInvocationStrategy rpc = rpcNames.get(method);
        if (rpc != null) {
            if (args.length != 1) {
                throw new IllegalArgumentException("Input must be provided.");
            }
            return rpc.invoke((DataObject) requireNonNull(args[0]));
        }

        switch (method.getName()) {
            case "toString":
                if (method.getReturnType().equals(String.class) && method.getParameterCount() == 0) {
                    return type.getName() + "$Adapter{delegate=" + delegate.toString() + "}";
                }
                break;
            case "hashCode":
                if (method.getReturnType().equals(int.class) && method.getParameterCount() == 0) {
                    return System.identityHashCode(proxy);
                }
                break;
            case "equals":
                if (method.getReturnType().equals(boolean.class) && method.getParameterCount() == 1
                        && method.getParameterTypes()[0] == Object.class) {
                    return proxy == args[0];
                }
                break;
            default:
                break;
        }

        throw new UnsupportedOperationException("Method " + method.toString() + "is unsupported.");
    }

    private abstract class RpcInvocationStrategy {
        private final SchemaPath rpcName;

        RpcInvocationStrategy(final SchemaPath path) {
            rpcName = path;
        }

        final ListenableFuture<RpcResult<?>> invoke(final DataObject input) {
            return invoke0(rpcName, serialize(input));
        }

        abstract ContainerNode serialize(DataObject input);

        final SchemaPath getRpcName() {
            return rpcName;
        }

        ListenableFuture<RpcResult<?>> invoke0(final SchemaPath schemaPath, final ContainerNode input) {
            final ListenableFuture<? extends DOMRpcResult> result = delegate.invokeRpc(schemaPath, input);
            if (ENABLE_CODEC_SHORTCUT && result instanceof BindingRpcFutureAware) {
                return ((BindingRpcFutureAware) result).getBindingFuture();
            }

            return transformFuture(schemaPath, result, adapterContext.currentSerializer());
        }

        private ListenableFuture<RpcResult<?>> transformFuture(final SchemaPath rpc,
                final ListenableFuture<? extends DOMRpcResult> domFuture,
                final BindingNormalizedNodeSerializer resultCodec) {
            return Futures.transform(domFuture, input -> {
                final NormalizedNode<?, ?> domData = input.getResult();
                final DataObject bindingResult;
                if (domData != null) {
                    final SchemaPath rpcOutput = rpc.createChild(YangConstants.operationOutputQName(
                        rpc.getLastComponent().getModule()));
                    bindingResult = resultCodec.fromNormalizedNodeRpcData(rpcOutput, (ContainerNode) domData);
                } else {
                    bindingResult = null;
                }

                return RpcResultUtil.rpcResultFromDOM(input.getErrors(), bindingResult);
            }, MoreExecutors.directExecutor());
        }
    }

    private final class NonRoutedStrategy extends RpcInvocationStrategy {
        NonRoutedStrategy(final SchemaPath path) {
            super(path);
        }

        @Override
        ContainerNode serialize(final DataObject input) {
            return LazySerializedContainerNode.create(getRpcName(), input, adapterContext.currentSerializer());
        }
    }

    private final class RoutedStrategy extends RpcInvocationStrategy {
        private final ContextReferenceExtractor refExtractor;
        private final NodeIdentifier contextName;

        RoutedStrategy(final SchemaPath path, final Method rpcMethod, final QName leafName) {
            super(path);
            final Optional<Class<? extends DataContainer>> maybeInputType =
                    BindingReflections.resolveRpcInputClass(rpcMethod);
            checkState(maybeInputType.isPresent(), "RPC method %s has no input", rpcMethod.getName());
            final Class<? extends DataContainer> inputType = maybeInputType.get();
            refExtractor = ContextReferenceExtractor.from(inputType);
            this.contextName = new NodeIdentifier(leafName);
        }

        @Override
        ContainerNode serialize(final DataObject input) {
            final InstanceIdentifier<?> bindingII = refExtractor.extract(input);
            final CurrentAdapterSerializer serializer = adapterContext.currentSerializer();

            if (bindingII != null) {
                final YangInstanceIdentifier yangII = serializer.toCachedYangInstanceIdentifier(bindingII);
                final LeafNode<?> contextRef = ImmutableNodes.leafNode(contextName, yangII);
                return LazySerializedContainerNode.withContextRef(getRpcName(), input, contextRef, serializer);
            }
            return LazySerializedContainerNode.create(getRpcName(), input, serializer);
        }

    }
}
