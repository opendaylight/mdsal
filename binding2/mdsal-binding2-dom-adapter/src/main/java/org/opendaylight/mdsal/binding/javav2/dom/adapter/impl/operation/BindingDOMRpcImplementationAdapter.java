/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.operation;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.javav2.spec.base.Input;
import org.opendaylight.mdsal.binding.javav2.spec.base.Output;
import org.opendaylight.mdsal.binding.javav2.spec.base.Rpc;
import org.opendaylight.mdsal.binding.javav2.spec.base.RpcCallback;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Rpc implementation adapter.
 */
@Beta
public class BindingDOMRpcImplementationAdapter extends AbstractBindingDOMImplementationAdapter<Rpc>
        implements DOMRpcImplementation {

    BindingDOMRpcImplementationAdapter(final BindingNormalizedNodeCodecRegistry codec,
           final Class<? extends Rpc> clazz, final Rpc delegate) {
       super(codec, clazz, delegate);
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    @Nonnull
    @Override
    public FluentFuture<DOMRpcResult> invokeRpc(@Nonnull final DOMRpcIdentifier rpc,
            @Nullable final NormalizedNode<?, ?> input) {
        final TreeNode bindingInput = input != null ? deserialize(rpc.getType(), input) : null;
        final SettableFuture<RpcResult<?>> bindingResult = SettableFuture.create();
        CompletableFuture.runAsync(() -> getDelegate().invoke((Input<?>) bindingInput, new RpcCallback<Output<?>>() {
                public void onSuccess(Output<?> output) {
                    bindingResult.set(RpcResultBuilder.success(output).build());
                }

                public void onFailure(Throwable error) {
                    bindingResult.set(RpcResultBuilder.failed().withError(ErrorType.APPLICATION,
                        error.getMessage()).build());
                }
            })
        );
        return transformResult(bindingResult);
    }
}
