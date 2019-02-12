/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.operation;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Optional;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.extractor.ContextReferenceExtractor;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.serialized.LazySerializedContainerNode;
import org.opendaylight.mdsal.binding.javav2.runtime.reflection.BindingReflections;
import org.opendaylight.mdsal.binding.javav2.spec.base.Input;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.Instantiable;
import org.opendaylight.mdsal.binding.javav2.spec.base.Output;
import org.opendaylight.mdsal.binding.javav2.spec.base.Rpc;
import org.opendaylight.mdsal.binding.javav2.spec.base.RpcCallback;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.mdsal.dom.spi.RpcRoutingStrategy;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorSeverity;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

@Beta
class RpcServiceAdapter implements InvocationHandler {

    private final RpcInvocationStrategy strategy;
    private final Class<? extends Rpc<?, ?>> type;
    private final BindingToNormalizedNodeCodec codec;
    private final DOMRpcService delegate;
    private final Rpc<?, ?> proxy;

    RpcServiceAdapter(final Class<? extends Rpc<?, ?>> type, final BindingToNormalizedNodeCodec codec,
            final DOMRpcService domService) {
        this.type = requireNonNull(type);
        this.codec = requireNonNull(codec);
        this.delegate = requireNonNull(domService);
        strategy = createStrategy(type);
        proxy = (Rpc<?, ?>) Proxy.newProxyInstance(type.getClassLoader(), new Class[] { type }, this);
    }

    private RpcInvocationStrategy createStrategy(final Class<? extends Rpc<?, ?>> rpcInterface) {
        final RpcDefinition rpc = codec.getRpcDefinition(rpcInterface);
        final RpcRoutingStrategy domStrategy = RpcRoutingStrategy.from(rpc);
        if (domStrategy.isContextBasedRouted()) {
            try {
                return new RoutedStrategy(rpc.getPath(),
                    rpcInterface.getMethod("invoke", Input.class, RpcCallback.class), domStrategy.getLeaf());
            } catch (final NoSuchMethodException e) {
                throw new IllegalStateException("Can not find 'invoke' method", e);
            }
        }
        return new NonRoutedStrategy(rpc.getPath());
    }

    Rpc<?, ?> getProxy() {
        return proxy;
    }

    @Override
    @SuppressWarnings("checkstyle:hiddenField")
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        switch (method.getName()) {
            case "toString":
                return type.getName() + "$Adapter{delegate=" + delegate.toString() + "}";
            case "hashCode":
                return System.identityHashCode(proxy);
            case "equals":
                return proxy == args[0];
            case "invoke":
                if (args.length == 2) {
                    final Input<?> input = (Input<?>) requireNonNull(args[0]);
                    final RpcCallback<Output> callback = (RpcCallback<Output>) requireNonNull(args[1]);
                    ListenableFuture<RpcResult<?>> future =  strategy.invoke((TreeNode) input);
                    Futures.addCallback(future, new FutureCallback<RpcResult<?>>() {

                        @Override
                        public void onSuccess(final RpcResult<?> result) {
                            if (result.getErrors().isEmpty()) {
                                callback.onSuccess((Output) result.getResult());
                            } else {
                                result.getErrors().forEach(e -> callback.onFailure(e.getCause()));
                            }
                        }

                        @Override
                        public void onFailure(final Throwable throwable) {
                            callback.onFailure(throwable);
                        }
                    }, MoreExecutors.directExecutor());
                }
                return 0;
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
            if (listenInPoolThread instanceof LazyDOMRpcResultFuture) {
                return ((LazyDOMRpcResultFuture) listenInPoolThread).getBindingFuture();
            }

            return transformFuture(schemaPath, listenInPoolThread, codec.getCodecFactory());
        }

        private ListenableFuture<RpcResult<?>> transformFuture(final SchemaPath rpc,
                final ListenableFuture<DOMRpcResult> domFuture, final BindingNormalizedNodeCodecRegistry resultCodec) {
            return Futures.transform(domFuture, input -> {
                final NormalizedNode<?, ?> domData = input.getResult();
                final TreeNode bindingResult;
                if (domData != null) {
                    final SchemaPath rpcOutput = rpc.createChild(QName.create(rpc.getLastComponent(), "output"));
                    bindingResult = resultCodec.fromNormalizedNodeOperationData(rpcOutput, (ContainerNode) domData);
                } else {
                    bindingResult = null;
                }

                // DOMRpcResult does not have a notion of success, hence we have to reverse-engineer it by looking
                // at reported errors and checking whether they are just warnings.
                final Collection<? extends RpcError> errors = input.getErrors();
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
