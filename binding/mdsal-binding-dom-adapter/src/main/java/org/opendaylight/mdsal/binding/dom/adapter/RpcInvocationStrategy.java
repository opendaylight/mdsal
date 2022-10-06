/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.mdsal.binding.dom.adapter.StaticConfiguration.ENABLE_CODEC_SHORTCUT;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.lang.reflect.Method;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.mdsal.dom.spi.ContentRoutedRpcContext;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

abstract sealed class RpcInvocationStrategy {
    private static final class NonRouted extends RpcInvocationStrategy {
        NonRouted(final AbstractRpcAdapter adapter, final QName rpcName) {
            super(adapter, rpcName);
        }

        @Override
        ContainerNode serialize(final CurrentAdapterSerializer serializer, final NodeIdentifier inputIdentifier,
                final DataObject input) {
            return LazySerializedContainerNode.create(inputIdentifier, input, serializer);
        }
    }

    private static final class Routed extends RpcInvocationStrategy {
        private final ContextReferenceExtractor refExtractor;
        private final NodeIdentifier contextName;

        Routed(final AbstractRpcAdapter adapter, final QName rpcName, final Method rpcMethod,
                final QName leafName) {
            super(adapter, rpcName);
            final Optional<Class<? extends DataContainer>> maybeInputType =
                    BindingReflections.resolveRpcInputClass(rpcMethod);
            checkState(maybeInputType.isPresent(), "RPC method %s has no input", rpcMethod.getName());
            final Class<? extends DataContainer> inputType = maybeInputType.get();
            refExtractor = ContextReferenceExtractor.from(inputType);
            contextName = new NodeIdentifier(leafName);
        }

        @Override
        ContainerNode serialize(final CurrentAdapterSerializer serializer, final NodeIdentifier inputIdentifier,
                final DataObject input) {
            final InstanceIdentifier<?> bindingII = refExtractor.extract(input);
            if (bindingII != null) {
                final YangInstanceIdentifier yangII = serializer.toCachedYangInstanceIdentifier(bindingII);
                final LeafNode<?> contextRef = ImmutableNodes.leafNode(contextName, yangII);
                return LazySerializedContainerNode.withContextRef(inputIdentifier, input, contextRef, serializer);
            }
            return LazySerializedContainerNode.create(inputIdentifier, input, serializer);
        }
    }

    private final @NonNull AbstractRpcAdapter adapter;
    private final @NonNull NodeIdentifier inputIdentifier;
    private final @NonNull Absolute outputPath;

    private RpcInvocationStrategy(final AbstractRpcAdapter adapter, final QName rpcName) {
        this.adapter = requireNonNull(adapter);
        final var namespace = rpcName.getModule();
        outputPath = Absolute.of(rpcName, YangConstants.operationOutputQName(namespace).intern()).intern();
        inputIdentifier = NodeIdentifier.create(YangConstants.operationInputQName(namespace.intern()));
    }

    static @NonNull RpcInvocationStrategy of(final AbstractRpcAdapter adapter, final Method method,
            final RpcDefinition schema) {
        final var rpcName = schema.getQName();
        final var contentContext = ContentRoutedRpcContext.forRpc(schema.asEffectiveStatement());
        return contentContext == null ? new NonRouted(adapter, rpcName)
            : new Routed(adapter, rpcName, method, contentContext.leaf());
    }

    final ListenableFuture<RpcResult<?>> invoke(final DataObject input) {
        return invoke0(serialize(adapter.currentSerializer(), inputIdentifier, input));
    }

    abstract ContainerNode serialize(@NonNull CurrentAdapterSerializer serializer,
        @NonNull NodeIdentifier inputIdentifier, DataObject input);

    private ListenableFuture<RpcResult<?>> invoke0(final ContainerNode input) {
        final var result = adapter.delegate().invokeRpc(outputPath.firstNodeIdentifier(), input);
        if (ENABLE_CODEC_SHORTCUT && result instanceof BindingRpcFutureAware bindingAware) {
            return bindingAware.getBindingFuture();
        }
        return transformFuture(result, adapter.currentSerializer());
    }

    private ListenableFuture<RpcResult<?>> transformFuture(final ListenableFuture<? extends DOMRpcResult> domFuture,
            final BindingNormalizedNodeSerializer resultCodec) {
        return Futures.transform(domFuture, input -> {
            final ContainerNode domData = input.value();
            final DataObject bindingResult;
            if (domData != null) {
                bindingResult = resultCodec.fromNormalizedNodeRpcData(outputPath, domData);
            } else {
                bindingResult = null;
            }

            return RpcResultUtil.rpcResultFromDOM(input.errors(), bindingResult);
        }, MoreExecutors.directExecutor());
    }
}