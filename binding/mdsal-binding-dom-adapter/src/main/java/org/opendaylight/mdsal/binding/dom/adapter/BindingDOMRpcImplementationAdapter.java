/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.mdsal.binding.dom.adapter.StaticConfiguration.ENABLE_CODEC_SHORTCUT;

import com.google.common.util.concurrent.ListenableFuture;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingLazyContainerNode;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.binding.RpcInput;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

final class BindingDOMRpcImplementationAdapter extends AbstractDOMRpcImplementationAdapter {
    private final @NonNull Rpc<?, ?> delegate;
    private final @NonNull QName rpcName;
    private final @NonNull QName inputName;

    BindingDOMRpcImplementationAdapter(final AdapterContext adapterContext, final Rpc<?, ?> delegate,
            final QName rpcName) {
        super(adapterContext);
        this.delegate = requireNonNull(delegate);
        this.rpcName = requireNonNull(rpcName);
        inputName = YangConstants.operationInputQName(rpcName.getModule()).intern();
    }

    @Override
    ListenableFuture<RpcResult<?>> invokeRpc(final CurrentAdapterSerializer serializer, final DOMRpcIdentifier rpc,
            final ContainerNode input) {
        final var bindingInput = input != null ? deserialize(serializer, input) : null;
        return ((Rpc) delegate).invoke((RpcInput) bindingInput);
    }

    private DataObject deserialize(final CurrentAdapterSerializer serializer, final ContainerNode input) {
        if (ENABLE_CODEC_SHORTCUT && input instanceof BindingLazyContainerNode<?> lazy) {
            return lazy.getDataObject();
        }

        // TODO: this is a bit inefficient: typically we get the same CurrentAdapterSerializer and the path is also
        //       constant, hence we should be able to cache this lookup and just have the appropriate
        //       BindingDataObjectCodecTreeNode and reuse it directly
        checkArgument(inputName.equals(input.getIdentifier().getNodeType()),
            "Unexpected RPC %s input %s", rpcName, input);
        return serializer.fromNormalizedNodeRpcData(Absolute.of(rpcName, inputName), input);
    }
}
