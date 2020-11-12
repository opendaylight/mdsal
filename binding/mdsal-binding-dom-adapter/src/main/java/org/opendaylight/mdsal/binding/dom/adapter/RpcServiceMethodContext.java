/*
 * Copyright (c) 2020 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.opendaylight.mdsal.binding.dom.adapter.StaticConfiguration.ENABLE_CODEC_SHORTCUT;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.mdsal.binding.dom.adapter.invoke.RpcMethodInvoker;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingLazyContainerNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeCodec;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcOutput;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

final class RpcServiceMethodContext {
    final BindingNormalizedNodeCodec<?> inputCodec;
    final BindingNormalizedNodeCodec<RpcOutput> outputCodec;
    final RpcMethodInvoker methodInvoker;

    RpcServiceMethodContext(final BindingNormalizedNodeCodec<?> inputCodec,
                            final BindingNormalizedNodeCodec<RpcOutput> outputCodec,
                            final RpcMethodInvoker methodInvoker) {
        this.inputCodec = inputCodec;
        this.outputCodec = outputCodec;
        this.methodInvoker = methodInvoker;
    }


    public ListenableFuture<RpcResult<?>> invokeRpc(RpcService delegate, NormalizedNode<?, ?> input) {
        final DataObject deserialized;
        if (ENABLE_CODEC_SHORTCUT && input instanceof BindingLazyContainerNode) {
            deserialized = ((BindingLazyContainerNode<?>) input).getDataObject();
        } else {
            deserialized = (DataObject) inputCodec.deserialize(input);
        }
        return methodInvoker.invokeOn(delegate, deserialized);
    }

    public BindingNormalizedNodeCodec<RpcOutput> getOutputCodec() {
        return outputCodec;
    }
}
