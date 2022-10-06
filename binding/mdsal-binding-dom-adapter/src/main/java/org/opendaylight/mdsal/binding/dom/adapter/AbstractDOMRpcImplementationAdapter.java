/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.ListenableFuture;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

abstract sealed class AbstractDOMRpcImplementationAdapter implements DOMRpcImplementation
        permits BindingDOMRpcImplementationAdapter, LegacyDOMRpcImplementationAdapter {
    private final AdapterContext adapterContext;

    AbstractDOMRpcImplementationAdapter(final AdapterContext adapterContext) {
        this.adapterContext = requireNonNull(adapterContext);
    }

    @Override
    public final long invocationCost() {
        // Default implementations are 0, we need to perform some translation, hence we have a slightly higher cost
        return 1;
    }

    @Override
    public final ListenableFuture<DOMRpcResult> invokeRpc(final DOMRpcIdentifier rpc, final ContainerNode input) {
        final var serializer = adapterContext.currentSerializer();
        return LazyDOMRpcResultFuture.create(serializer, invokeRpc(serializer, rpc, input));
    }

    abstract @NonNull ListenableFuture<RpcResult<?>> invokeRpc(CurrentAdapterSerializer serializer,
        DOMRpcIdentifier rpc, ContainerNode input);
}
