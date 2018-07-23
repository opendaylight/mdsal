/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.invoke;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.ListenableFuture;
import java.lang.reflect.Method;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Invocation strategy for RPCs. This class is visible for migration purposes only.
 */
@Beta
public final class RpcServiceInvoker {
    private final org.opendaylight.yangtools.yang.binding.util.RpcServiceInvoker delegate;

    private RpcServiceInvoker(final org.opendaylight.yangtools.yang.binding.util.RpcServiceInvoker delegate) {
        this.delegate = requireNonNull(delegate);
    }

    /**
     * Creates an RPCServiceInvoker for specified QName-&lt;Method mapping.
     *
     * @param qnameToMethod translation mapping, must not be null nor empty.
     * @return An {@link RpcServiceInvoker} instance.
     */
    public static RpcServiceInvoker from(final Map<QName, Method> qnameToMethod) {
        return new RpcServiceInvoker(
            org.opendaylight.yangtools.yang.binding.util.RpcServiceInvoker.from(qnameToMethod));
    }

    /**
     * Invokes supplied RPC on provided implementation of RPC Service.
     *
     * @param impl Implementation on which RPC should be invoked.
     * @param rpcName Name of RPC to be invoked.
     * @param input Input data for RPC.
     * @return Future which will complete once rpc processing is finished.
     */
    public ListenableFuture<RpcResult<?>> invokeRpc(@Nonnull final RpcService impl, @Nonnull final QName rpcName,
            @Nullable final DataObject input) {
        return delegate.invokeRpc(impl, rpcName, input);
    }
}
