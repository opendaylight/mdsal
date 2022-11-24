/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ListenableFuture;
import java.lang.invoke.MethodHandle;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

@Deprecated(since = "11.0.0", forRemoval = true)
final class LegacyDOMRpcImplementationAdapter extends AbstractDOMRpcImplementationAdapter {
    private final MethodHandle handle;

    LegacyDOMRpcImplementationAdapter(final AdapterContext adapterContext, final QName inputName,
            final RpcService delegate, final MethodHandle handle) {
        super(adapterContext, inputName);
        this.handle = handle.bindTo(requireNonNull(delegate));
    }

    @Override
    @SuppressWarnings("checkstyle:illegalCatch")
    ListenableFuture<RpcResult<?>> invokeRpc(final CurrentAdapterSerializer serializer, final DOMRpcIdentifier rpc,
            final ContainerNode input) {
        try {
            return (ListenableFuture<RpcResult<?>>) handle.invokeExact(deserialize(serializer, rpc.getType(), input));
        } catch (Throwable e) {
            Throwables.throwIfUnchecked(e);
            throw new IllegalStateException(e);
        }
    }
}
