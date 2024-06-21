/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.mdsal.binding.dom.adapter.StaticConfiguration.ENABLE_CODEC_SHORTCUT;

import com.google.common.util.concurrent.ListenableFuture;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.yangtools.binding.Rpc;
import org.opendaylight.yangtools.binding.RpcInput;
import org.opendaylight.yangtools.binding.data.codec.api.BindingLazyContainerNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

final class BindingDOMRpcImplementationAdapter implements DOMRpcImplementation {
    private final @NonNull AdapterContext adapterContext;
    private final @NonNull QName rpcName;
    @SuppressWarnings("rawtypes")
    private final @NonNull Rpc delegate;

    BindingDOMRpcImplementationAdapter(final AdapterContext adapterContext, final QName rpcName,
            final Rpc<?, ?> delegate) {
        this.adapterContext = requireNonNull(adapterContext);
        this.rpcName = requireNonNull(rpcName);
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public long invocationCost() {
        // Default implementations are 0, we need to perform some translation, hence we have a slightly higher cost
        return 1;
    }

    @Override
    public ListenableFuture<DOMRpcResult> invokeRpc(final DOMRpcIdentifier rpc, final ContainerNode input) {
        final var serializer = adapterContext.currentSerializer();
        @SuppressWarnings("unchecked")
        final var future = delegate.invoke(deserialize(serializer, input));
        return LazyDOMRpcResultFuture.create(serializer, future);
    }

    private @NonNull RpcInput deserialize(final @NonNull CurrentAdapterSerializer serializer,
            final @NonNull ContainerNode input) {
        if (ENABLE_CODEC_SHORTCUT && input instanceof BindingLazyContainerNode<?> lazy) {
            return (RpcInput) lazy.getDataObject();
        }

        final var inputName = input.name().getNodeType();
        if (!"input".equals(inputName.getLocalName()) || !rpcName.getModule().equals(inputName.getModule())) {
            throw new IllegalArgumentException("Unexpected RPC " + rpcName + " input " + input.prettyTree());
        }

        // TODO: this is a bit inefficient: typically we get the same CurrentAdapterSerializer and the path is also
        //       constant, hence we should be able to cache this lookup and just have the appropriate
        //       BindingDataObjectCodecTreeNode and reuse it directly
        // FIXME: should be a guaranteed return, as input is @NonNull
        return verifyNotNull((RpcInput) serializer.fromNormalizedNodeRpcData(Absolute.of(rpcName, inputName), input));
    }
}
