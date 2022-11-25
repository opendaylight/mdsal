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
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.ref.WeakReference;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingLazyContainerNode;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.yangtools.yang.binding.RpcInput;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

abstract sealed class AbstractDOMRpcImplementationAdapter implements DOMRpcImplementation
        permits BindingDOMRpcImplementationAdapter, LegacyDOMRpcImplementationAdapter {
    private static final class RpcInputCodec extends WeakReference<BindingCodecTree> {
        final @NonNull BindingDataObjectCodecTreeNode<RpcInput> codec;

        RpcInputCodec(final BindingCodecTree referent, final QName rpcName, final QName inputName) {
            super(referent);

            final var path = Absolute.of(rpcName, inputName);
            @SuppressWarnings("unchecked")
            final var found = (BindingDataObjectCodecTreeNode<RpcInput>) referent.getSubtreeCodec(path);
            if (found == null) {
                throw new IllegalStateException("Cannot findl codec for " + path);
            }
            codec = found;
        }
    }

    private static final VarHandle CODEC;

    static {
        try {
            CODEC = MethodHandles.lookup()
                .findVarHandle(AbstractDOMRpcImplementationAdapter.class, "codec", RpcInputCodec.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final AdapterContext adapterContext;
    private final @NonNull QName inputName;
    private final @NonNull QName rpcName;

    @SuppressWarnings("unused")
    private volatile RpcInputCodec codec;

    AbstractDOMRpcImplementationAdapter(final AdapterContext adapterContext, final BindingCodecTree codecTree,
            final QName rpcName) {
        this.adapterContext = requireNonNull(adapterContext);
        this.rpcName = requireNonNull(rpcName);
        inputName = YangConstants.operationInputQName(rpcName.getModule()).intern();
        codec = new RpcInputCodec(codecTree, rpcName, inputName);
    }

    @Override
    public final long invocationCost() {
        // Default implementations are 0, we need to perform some translation, hence we have a slightly higher cost
        return 1;
    }

    @Override
    public final ListenableFuture<DOMRpcResult> invokeRpc(final DOMRpcIdentifier rpc, final ContainerNode input) {
        final var serializer = adapterContext.currentSerializer();
        return LazyDOMRpcResultFuture.create(serializer, invokeRpc(deserialize(serializer, input)));
    }

    abstract @NonNull ListenableFuture<RpcResult<?>> invokeRpc(@NonNull RpcInput input);

    private @NonNull RpcInput deserialize(final @NonNull CurrentAdapterSerializer serializer,
            final @NonNull ContainerNode input) {
        if (ENABLE_CODEC_SHORTCUT && input instanceof BindingLazyContainerNode<?> lazy) {
            return (RpcInput) lazy.getDataObject();
        }

        checkArgument(inputName.equals(input.getIdentifier().getNodeType()),
            "Unexpected RPC %s input %s", rpcName, input);

        return getCodec(serializer).deserialize(input);
    }

    private @NonNull BindingDataObjectCodecTreeNode<RpcInput> getCodec(@NonNull BindingCodecTree codecTree) {
        RpcInputCodec local = (RpcInputCodec) CODEC.getAcquire(this);
        while (true) {
            final var cached = local.get();
            if (codecTree == cached) {
                return local.codec;
            }

            final var path = Absolute.of(rpcName, inputName);
            @SuppressWarnings("unchecked")
            final var found = (BindingDataObjectCodecTreeNode<RpcInput>) codecTree.getSubtreeCodec(path);
            if (found == null) {
                throw new IllegalStateException("Cannot findl codec for " + path);
            }

            final var updated = new RpcInputCodec(codecTree, rpcName, inputName);
            final var witness = (RpcInputCodec) CODEC.compareAndExchangeRelease(this, local, updated);
            if (witness == local) {
                return found;
            }
            local = witness;
        }
    }
}
