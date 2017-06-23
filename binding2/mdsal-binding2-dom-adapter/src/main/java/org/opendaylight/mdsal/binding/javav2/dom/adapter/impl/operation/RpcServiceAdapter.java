/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.operation;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map.Entry;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.extractor.ContextReferenceExtractor;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.serialized.LazySerializedContainerNode;
import org.opendaylight.mdsal.binding.javav2.runtime.reflection.BindingReflections;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.Instantiable;
import org.opendaylight.mdsal.binding.javav2.spec.base.Rpc;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.mdsal.dom.spi.RpcRoutingStrategy;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.OperationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

@Beta
class RpcServiceAdapter implements InvocationHandler {

    private final ImmutableMap<Method, RpcInvocationStrategy> rpcNames;
    private final Class<? extends Rpc<?, ?>> type;
    private final BindingToNormalizedNodeCodec codec;
    private final DOMRpcService delegate;
    private final Rpc<?, ?> proxy;

    RpcServiceAdapter(final Class<? extends Rpc<?, ?>> type, final BindingToNormalizedNodeCodec codec,
            final DOMRpcService domService) {
        this.type = Preconditions.checkNotNull(type);
        this.codec = Preconditions.checkNotNull(codec);
        this.delegate = Preconditions.checkNotNull(domService);
        final ImmutableMap.Builder<Method, RpcInvocationStrategy> rpcBuilder = ImmutableMap.builder();
        for (final Entry<Method, OperationDefinition> rpc : codec.getRPCMethodToSchema(type).entrySet()) {
            rpcBuilder.put(rpc.getKey(), createStrategy(rpc.getKey(), (RpcDefinition) rpc.getValue()));
        }
        rpcNames = rpcBuilder.build();
        proxy = (Rpc<?, ?>) Proxy.newProxyInstance(type.getClassLoader(), new Class[] { type }, this);
    }

    private RpcInvocationStrategy createStrategy(final Method method, final RpcDefinition schema) {
        final RpcRoutingStrategy strategy = RpcRoutingStrategy.from(schema);
        if (strategy.isContextBasedRouted()) {
            return new RoutedStrategy(schema.getPath(), method, strategy.getLeaf());
        }
        return new NonRoutedStrategy(schema.getPath());
    }

    Rpc<?, ?> getProxy() {
        return proxy;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

        final RpcInvocationStrategy rpc = rpcNames.get(method);
        if (rpc != null) {
            if (method.getParameterTypes().length == 0) {
                return rpc.invokeEmpty();
            }
            if (args.length != 1) {
                throw new IllegalArgumentException("Input must be provided.");
            }
            return rpc.invoke((TreeNode) args[0]);
        }

        if (isObjectMethod(method)) {
            return callObjectMethod(proxy, method, args);
        }
        throw new UnsupportedOperationException("Method " + method.toString() + "is unsupported.");
    }

    private static boolean isObjectMethod(final Method method) {
        switch (method.getName()) {
            case "toString":
                return method.getReturnType().equals(String.class) && method.getParameterTypes().length == 0;
            case "hashCode":
                return method.getReturnType().equals(int.class) && method.getParameterTypes().length == 0;
            case "equals":
                return method.getReturnType().equals(boolean.class) && method.getParameterTypes().length == 1
                        && method.getParameterTypes()[0] == Object.class;
            default:
                return false;
        }
    }

    private Object callObjectMethod(final Object self, final Method method, final Object[] args) {
        switch (method.getName()) {
            case "toString":
                return type.getName() + "$Adapter{delegate=" + delegate.toString() + "}";
            case "hashCode":
                return System.identityHashCode(self);
            case "equals":
                return self == args[0];
            default:
                return null;
        }
    }

    private abstract class RpcInvocationStrategy {

        private final SchemaPath rpcName;

        protected RpcInvocationStrategy(final SchemaPath path) {
            rpcName = path;
        }

        final ListenableFuture<RpcResult<?>> invoke(final TreeNode input) {
            return invoke0(rpcName, serialize(input));
        }

        abstract NormalizedNode<?, ?> serialize(TreeNode input);

        final ListenableFuture<RpcResult<?>> invokeEmpty() {
            return invoke0(rpcName, null);
        }

        final SchemaPath getRpcName() {
            return rpcName;
        }

        private ListenableFuture<RpcResult<?>> invoke0(final SchemaPath schemaPath, final NormalizedNode<?, ?> input) {
            final ListenableFuture<DOMRpcResult> listenInPoolThread =
                    JdkFutureAdapters.listenInPoolThread(delegate.invokeRpc(schemaPath, input));
            if (listenInPoolThread instanceof LazyDOMOperationResultFuture) {
                return ((LazyDOMOperationResultFuture) listenInPoolThread).getBindingFuture();
            }

            return transformFuture(schemaPath, listenInPoolThread, codec.getCodecFactory());
        }

        private ListenableFuture<RpcResult<?>> transformFuture(final SchemaPath rpc,
                final ListenableFuture<DOMRpcResult> domFuture, final BindingNormalizedNodeCodecRegistry codec) {
            return Futures.transform(domFuture, (Function<DOMRpcResult, RpcResult<?>>) input -> {
                final NormalizedNode<?, ?> domData = input.getResult();
                final TreeNode bindingResult;
                if (domData != null) {
                    final SchemaPath rpcOutput = rpc.createChild(QName.create(rpc.getLastComponent(), "output"));
                    bindingResult = codec.fromNormalizedNodeOperationData(rpcOutput, (ContainerNode) domData);
                } else {
                    bindingResult = null;
                }
                return RpcResult.class.cast(RpcResultBuilder.success(bindingResult).build());
            }, MoreExecutors.directExecutor());
        }

    }

    private final class NonRoutedStrategy extends RpcInvocationStrategy {

        protected NonRoutedStrategy(final SchemaPath path) {
            super(path);
        }

        @Override
        NormalizedNode<?, ?> serialize(final TreeNode input) {
            return LazySerializedContainerNode.create(getRpcName(), input, codec.getCodecRegistry());
        }

    }

    private final class RoutedStrategy extends RpcInvocationStrategy {

        private final ContextReferenceExtractor refExtractor;
        private final NodeIdentifier contextName;

        protected RoutedStrategy(final SchemaPath path, final Method rpcMethod, final QName leafName) {
            super(path);
            final Optional<Class<? extends Instantiable<?>>> maybeInputType =
                    BindingReflections.resolveOperationInputClass(rpcMethod);
            Preconditions.checkState(maybeInputType.isPresent(), "RPC method %s has no input", rpcMethod.getName());
            final Class<? extends Instantiable<?>> inputType = maybeInputType.get();
            refExtractor = ContextReferenceExtractor.from(inputType);
            this.contextName = new NodeIdentifier(leafName);
        }

        @SuppressWarnings("rawtypes")
        @Override
        NormalizedNode<?, ?> serialize(final TreeNode input) {
            final InstanceIdentifier<?> bindingII = refExtractor.extract(input);
            if (bindingII != null) {
                final YangInstanceIdentifier yangII = codec.toYangInstanceIdentifierCached(bindingII);
                final LeafNode contextRef = ImmutableNodes.leafNode(contextName, yangII);
                return LazySerializedContainerNode.withContextRef(getRpcName(), input, contextRef,
                        codec.getCodecRegistry());
            }
            return LazySerializedContainerNode.create(getRpcName(), input, codec.getCodecRegistry());
        }

    }
}