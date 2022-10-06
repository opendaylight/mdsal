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
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.lang.reflect.Method;
import java.util.Map.Entry;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
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
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

final class RpcServiceAdapter extends AbstractRpcAdapter {
    private final ImmutableMap<Method, RpcInvocationStrategy> rpcNames;

    RpcServiceAdapter(final Class<? extends RpcService> type, final AdapterContext adapterContext,
            final DOMRpcService delegate) {
        super(adapterContext, delegate, type);

        final ImmutableBiMap<Method, RpcDefinition> methods = currentSerializer().getRpcMethodToSchema(type);
        final var rpcBuilder = ImmutableMap.<Method, RpcInvocationStrategy>builderWithExpectedSize(methods.size());
        for (final Entry<Method, RpcDefinition> rpc : methods.entrySet()) {
            rpcBuilder.put(rpc.getKey(), createStrategy(rpc.getKey(), rpc.getValue()));
        }
        rpcNames = rpcBuilder.build();
    }

    private RpcInvocationStrategy createStrategy(final Method method, final RpcDefinition schema) {
        final QName rpcType = schema.getQName();
        final RpcRoutingStrategy strategy = RpcRoutingStrategy.from(schema);
        return strategy.isContextBasedRouted() ? new RoutedStrategy(rpcType, method, strategy.getLeaf())
                : new NonRoutedStrategy(rpcType);
    }

    @Override
    @SuppressWarnings("checkstyle:hiddenField")
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final RpcInvocationStrategy rpc = rpcNames.get(method);
        if (rpc != null) {
            if (args.length != 1) {
                throw new IllegalArgumentException("Input must be provided.");
            }
            return rpc.invoke((DataObject) requireNonNull(args[0]));
        }
        return defaultInvoke(proxy, method, args);
    }

    private abstract class RpcInvocationStrategy {
        private final @NonNull NodeIdentifier inputIdentifier;
        private final @NonNull Absolute outputPath;

        RpcInvocationStrategy(final QName rpcName) {
            final var namespace = rpcName.getModule();
            outputPath = Absolute.of(rpcName, YangConstants.operationOutputQName(namespace).intern()).intern();
            inputIdentifier = NodeIdentifier.create(YangConstants.operationInputQName(namespace.intern()));
        }

        final ListenableFuture<RpcResult<?>> invoke(final DataObject input) {
            return invoke0(serialize(input));
        }

        abstract ContainerNode serialize(DataObject input);

        final @NonNull NodeIdentifier inputIdentifier() {
            return inputIdentifier;
        }

        private ListenableFuture<RpcResult<?>> invoke0(final ContainerNode input) {
            final ListenableFuture<? extends DOMRpcResult> result =
                    delegate().invokeRpc(outputPath.firstNodeIdentifier(), input);
            if (ENABLE_CODEC_SHORTCUT && result instanceof BindingRpcFutureAware bindingAware) {
                return bindingAware.getBindingFuture();
            }

            return transformFuture(result, currentSerializer());
        }

        private ListenableFuture<RpcResult<?>> transformFuture(final ListenableFuture<? extends DOMRpcResult> domFuture,
                final BindingNormalizedNodeSerializer resultCodec) {
            return Futures.transform(domFuture, input -> {
                final NormalizedNode domData = input.value();
                final DataObject bindingResult;
                if (domData != null) {
                    bindingResult = resultCodec.fromNormalizedNodeRpcData(outputPath, (ContainerNode) domData);
                } else {
                    bindingResult = null;
                }

                return RpcResultUtil.rpcResultFromDOM(input.errors(), bindingResult);
            }, MoreExecutors.directExecutor());
        }
    }

    private final class NonRoutedStrategy extends RpcInvocationStrategy {
        NonRoutedStrategy(final QName rpcName) {
            super(rpcName);
        }

        @Override
        ContainerNode serialize(final DataObject input) {
            return LazySerializedContainerNode.create(inputIdentifier(), input, currentSerializer());
        }
    }

    private final class RoutedStrategy extends RpcInvocationStrategy {
        private final ContextReferenceExtractor refExtractor;
        private final NodeIdentifier contextName;

        RoutedStrategy(final QName rpcName, final Method rpcMethod, final QName leafName) {
            super(rpcName);
            final Optional<Class<? extends DataContainer>> maybeInputType =
                    BindingReflections.resolveRpcInputClass(rpcMethod);
            checkState(maybeInputType.isPresent(), "RPC method %s has no input", rpcMethod.getName());
            final Class<? extends DataContainer> inputType = maybeInputType.get();
            refExtractor = ContextReferenceExtractor.from(inputType);
            contextName = new NodeIdentifier(leafName);
        }

        @Override
        ContainerNode serialize(final DataObject input) {
            final InstanceIdentifier<?> bindingII = refExtractor.extract(input);
            final CurrentAdapterSerializer serializer = currentSerializer();

            if (bindingII != null) {
                final YangInstanceIdentifier yangII = serializer.toCachedYangInstanceIdentifier(bindingII);
                final LeafNode<?> contextRef = ImmutableNodes.leafNode(contextName, yangII);
                return LazySerializedContainerNode.withContextRef(inputIdentifier(), input, contextRef, serializer);
            }
            return LazySerializedContainerNode.create(inputIdentifier(), input, serializer);
        }
    }
}
