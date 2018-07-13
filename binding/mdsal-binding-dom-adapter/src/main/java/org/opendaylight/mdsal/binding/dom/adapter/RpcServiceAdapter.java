/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Map.Entry;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.mdsal.dom.spi.RpcRoutingStrategy;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorSeverity;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
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
    private final BindingToNormalizedNodeCodec codec;
    private final DOMRpcService delegate;
    private final RpcService proxy;

    RpcServiceAdapter(final Class<? extends RpcService> type, final BindingToNormalizedNodeCodec codec,
            final DOMRpcService domService) {
        this.type = Preconditions.checkNotNull(type);
        this.codec = Preconditions.checkNotNull(codec);
        this.delegate = Preconditions.checkNotNull(domService);
        final ImmutableMap.Builder<Method, RpcInvocationStrategy> rpcBuilder = ImmutableMap.builder();
        for (final Entry<Method, RpcDefinition> rpc : codec.getRpcMethodToSchema(type).entrySet()) {
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
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

        final RpcInvocationStrategy rpc = rpcNames.get(method);
        if (rpc != null) {
            if (method.getParameterTypes().length == 0) {
                return rpc.invokeEmpty();
            }
            if (args.length != 1) {
                throw new IllegalArgumentException("Input must be provided.");
            }
            return rpc.invoke((DataObject) args[0]);
        }

        switch (method.getName()) {
            case "toString":
                if (method.getReturnType().equals(String.class) && method.getParameterTypes().length == 0) {
                    return type.getName() + "$Adapter{delegate=" + delegate.toString() + "}";
                }
                break;
            case "hashCode":
                if (method.getReturnType().equals(int.class) && method.getParameterTypes().length == 0) {
                    return System.identityHashCode(proxy);
                }
                break;
            case "equals":
                if (method.getReturnType().equals(boolean.class) && method.getParameterTypes().length == 1
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

        protected RpcInvocationStrategy(final SchemaPath path) {
            rpcName = path;
        }

        final ListenableFuture<RpcResult<?>> invoke(final DataObject input) {
            return invoke0(rpcName, serialize(input));
        }

        abstract ContainerNode serialize(DataObject input);

        final ListenableFuture<RpcResult<?>> invokeEmpty() {
            return invoke0(rpcName, null);
        }

        final SchemaPath getRpcName() {
            return rpcName;
        }

        ListenableFuture<RpcResult<?>> invoke0(final SchemaPath schemaPath, final ContainerNode input) {
            final ListenableFuture<DOMRpcResult> result = delegate.invokeRpc(schemaPath, input);
            if (result instanceof BindingRpcFutureAware) {
                return ((BindingRpcFutureAware) result).getBindingFuture();
            }

            return transformFuture(schemaPath, result, codec.getCodecFactory());
        }

        private ListenableFuture<RpcResult<?>> transformFuture(final SchemaPath rpc,
                final ListenableFuture<DOMRpcResult> domFuture, final BindingNormalizedNodeCodecRegistry resultCodec) {
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

                // DOMRpcResult does not have a notion of success, hence we have to reverse-engineer it by looking
                // at reported errors and checking whether they are just warnings.
                final Collection<RpcError> errors = input.getErrors();
                return RpcResult.class.cast(RpcResultBuilder.status(errors.stream()
                    .noneMatch(error -> error.getSeverity() == ErrorSeverity.ERROR))
                    .withResult(bindingResult).withRpcErrors(errors).build());
            }, MoreExecutors.directExecutor());
        }
    }

    private final class NonRoutedStrategy extends RpcInvocationStrategy {

        protected NonRoutedStrategy(final SchemaPath path) {
            super(path);
        }

        @Override
        ContainerNode serialize(final DataObject input) {
            return LazySerializedContainerNode.create(getRpcName(), input, codec.getCodecRegistry());
        }

    }

    private final class RoutedStrategy extends RpcInvocationStrategy {

        private final ContextReferenceExtractor refExtractor;
        private final NodeIdentifier contextName;

        protected RoutedStrategy(final SchemaPath path, final Method rpcMethod, final QName leafName) {
            super(path);
            final Optional<Class<? extends DataContainer>> maybeInputType =
                    BindingReflections.resolveRpcInputClass(rpcMethod);
            Preconditions.checkState(maybeInputType.isPresent(), "RPC method %s has no input", rpcMethod.getName());
            final Class<? extends DataContainer> inputType = maybeInputType.get();
            refExtractor = ContextReferenceExtractor.from(inputType);
            this.contextName = new NodeIdentifier(leafName);
        }

        @Override
        ContainerNode serialize(final DataObject input) {
            final InstanceIdentifier<?> bindingII = refExtractor.extract(input);
            if (bindingII != null) {
                final YangInstanceIdentifier yangII = codec.toYangInstanceIdentifierCached(bindingII);
                final LeafNode<?> contextRef = ImmutableNodes.leafNode(contextName, yangII);
                return LazySerializedContainerNode.withContextRef(getRpcName(), input, contextRef,
                        codec.getCodecRegistry());
            }
            return LazySerializedContainerNode.create(getRpcName(), input, codec.getCodecRegistry());
        }

    }
}
