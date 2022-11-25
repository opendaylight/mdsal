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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcInput;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

sealed class RpcInvocationStrategy {
    static final class ContentRouted extends RpcInvocationStrategy {
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
                final RpcInput input) {
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

    RpcInvocationStrategy(final AbstractRpcAdapter adapter, final QName rpcName) {
        this.adapter = requireNonNull(adapter);
        final var namespace = rpcName.getModule();
        outputPath = Absolute.of(rpcName, YangConstants.operationOutputQName(namespace).intern()).intern();
        inputIdentifier = NodeIdentifier.create(YangConstants.operationInputQName(namespace.intern()));
    }

    final ListenableFuture<RpcResult<?>> invoke(final RpcInput input) {
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
            final @NonNull CurrentAdapterSerializer serializer, final RpcInput input) {
        return LazySerializedContainerNode.create(inputIdentifier, input, serializer);
    }

    private ListenableFuture<RpcResult<?>> transformFuture(final ListenableFuture<? extends DOMRpcResult> domFuture,
            BindingCodecTree codecTree) {
        return Futures.transform(domFuture, input -> {
            final ContainerNode domData = input.value();
            final DataObject bindingResult;
            if (domData != null) {
                final BindingDataObjectCodecTreeNode codecTreeNode = (BindingDataObjectCodecTreeNode)codecTree
                        .getSubtreeCodec(outputPath);
                bindingResult = (DataObject) codecTreeNode.deserialize(domData);
            } else {
                bindingResult = null;
            }

            return RpcResultUtil.rpcResultFromDOM(input.errors(), bindingResult);
        }, MoreExecutors.directExecutor());
    }
}