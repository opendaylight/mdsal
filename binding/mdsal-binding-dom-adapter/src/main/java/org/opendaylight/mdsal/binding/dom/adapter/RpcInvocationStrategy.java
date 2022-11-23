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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.mdsal.dom.spi.ContentRoutedRpcContext;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

abstract class RpcInvocationStrategy {
    private static final class NonRoutedStrategy extends RpcInvocationStrategy {
        NonRoutedStrategy(final RpcServiceAdapter adapter, final QName rpcName) {
            super(adapter, rpcName);
        }

        @Override
        ContainerNode serialize(final NodeIdentifier inputIdentifier, final CurrentAdapterSerializer serializer,
                final DataObject input) {
            return LazySerializedContainerNode.create(inputIdentifier, input, serializer);
        }
    }

    private static final class RoutedStrategy extends RpcInvocationStrategy {
        private final ContextReferenceExtractor refExtractor;
        private final NodeIdentifier contextName;

        RoutedStrategy(final RpcServiceAdapter adapter, final QName rpcName, final Method rpcMethod,
                final QName leafName) {
            super(adapter, rpcName);
            final var maybeInputType = BindingReflections.resolveRpcInputClass(rpcMethod);
            checkState(maybeInputType.isPresent(), "RPC method %s has no input", rpcMethod.getName());
            refExtractor = ContextReferenceExtractor.from(maybeInputType.orElseThrow());
            contextName = new NodeIdentifier(leafName);
        }

        @Override
        ContainerNode serialize(final NodeIdentifier inputIdentifier, final CurrentAdapterSerializer serializer,
                final DataObject input) {
            final var bindingII = refExtractor.extract(input);
            if (bindingII != null) {
                final var yangII = serializer.toCachedYangInstanceIdentifier(bindingII);
                final var contextRef = ImmutableNodes.leafNode(contextName, yangII);
                return LazySerializedContainerNode.withContextRef(inputIdentifier, input, contextRef, serializer);
            }
            return LazySerializedContainerNode.create(inputIdentifier, input, serializer);
        }
    }

    private final @NonNull RpcServiceAdapter adapter;
    private final @NonNull NodeIdentifier inputIdentifier;
    private final @NonNull Absolute outputPath;

    private RpcInvocationStrategy(final RpcServiceAdapter adapter, final QName rpcName) {
        this.adapter = requireNonNull(adapter);
        final var namespace = rpcName.getModule();
        outputPath = Absolute.of(rpcName, YangConstants.operationOutputQName(namespace).intern()).intern();
        inputIdentifier = NodeIdentifier.create(YangConstants.operationInputQName(namespace.intern()));
    }

    static @NonNull RpcInvocationStrategy of(final RpcServiceAdapter adapter, final Method method,
            final RpcEffectiveStatement schema) {
        final var rpcName = schema.argument();
        final var contentContext = ContentRoutedRpcContext.forRpc(schema);
        return contentContext == null ? new NonRoutedStrategy(adapter, rpcName)
            : new RoutedStrategy(adapter, rpcName, method, contentContext.leaf());
    }

    final ListenableFuture<RpcResult<?>> invoke(final DataObject input) {
        return invoke(serialize(inputIdentifier, adapter.currentSerializer(), input));
    }

    private ListenableFuture<RpcResult<?>> invoke(final ContainerNode input) {
        final var domFuture = adapter.delegate().invokeRpc(outputPath.firstNodeIdentifier(), input);
        if (ENABLE_CODEC_SHORTCUT && domFuture instanceof BindingRpcFutureAware bindingAware) {
            return bindingAware.getBindingFuture();
        }
        return transformFuture(domFuture, adapter.currentSerializer());
    }

    abstract ContainerNode serialize(@NonNull NodeIdentifier inputIdentifier,
        @NonNull CurrentAdapterSerializer serializer, DataObject input);

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