/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

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

sealed class RpcInvocationStrategy {
    private static final class ContentRouted extends RpcInvocationStrategy {
        private final ContextReferenceExtractor refExtractor;
        private final NodeIdentifier contextName;

        ContentRouted(final AbstractRpcAdapter adapter, final QName rpcName, final QName leafName,
                final ContextReferenceExtractor refExtractor) {
            super(adapter, rpcName);
            contextName = NodeIdentifier.create(leafName);
            this.refExtractor = requireNonNull(refExtractor);
        }

        @Override
        ContainerNode serialize(final NodeIdentifier inputIdentifier, final CurrentAdapterSerializer serializer,
                final DataObject input) {
            final var bindingII = refExtractor.extract(input);
            if (bindingII == null) {
                return super.serialize(inputIdentifier, serializer, input);
            }

            final var yangII = serializer.toCachedYangInstanceIdentifier(bindingII);
            final var contextRef = ImmutableNodes.leafNode(contextName, yangII);
            return LazySerializedContainerNode.withContextRef(inputIdentifier, input, contextRef, serializer);
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
            final RpcEffectiveStatement schema) {
        final var contentContext = ContentRoutedRpcContext.forRpc(schema);
        if (contentContext == null) {
            return new RpcInvocationStrategy(adapter, schema.argument());
        }

        return new ContentRouted(adapter, schema.argument(), contentContext.leaf(), ContextReferenceExtractor.from(
            // FIXME: do not use BindingReflections here
            BindingReflections.resolveRpcInputClass(method).orElseThrow(
                () -> new IllegalArgumentException("RPC method " + method.getName() + " has no input"))));
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

    ContainerNode serialize(final @NonNull NodeIdentifier identifier,
            final @NonNull CurrentAdapterSerializer serializer, final DataObject input) {
        return LazySerializedContainerNode.create(inputIdentifier, input, serializer);
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