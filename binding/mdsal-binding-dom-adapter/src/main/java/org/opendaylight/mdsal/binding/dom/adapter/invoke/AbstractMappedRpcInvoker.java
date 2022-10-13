/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.invoke;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.util.concurrent.ListenableFuture;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated(since = "11.0.0", forRemoval = true)
abstract class AbstractMappedRpcInvoker<T> extends RpcServiceInvoker {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractMappedRpcInvoker.class);

    @VisibleForTesting
    final Map<T, RpcMethodInvoker> map;

    protected AbstractMappedRpcInvoker(final Map<T, Method> map) {
        final Builder<T, RpcMethodInvoker> b = ImmutableMap.builder();

        for (Entry<T, Method> e : map.entrySet()) {
            if (BindingReflections.isRpcMethod(e.getValue())) {
                b.put(e.getKey(), RpcMethodInvoker.from(e.getValue()));
            } else {
                LOG.debug("Method {} is not an RPC method, ignoring it", e.getValue());
            }
        }

        this.map = b.build();
    }

    protected abstract T qnameToKey(QName qname);

    @Override
    public final ListenableFuture<RpcResult<?>> invokeRpc(final RpcService impl, final QName rpcName,
            final DataObject input) {
        requireNonNull(impl, "Implementation must be supplied");

        RpcMethodInvoker invoker = map.get(qnameToKey(rpcName));
        checkArgument(invoker != null, "Supplied RPC is not valid for implementation %s", impl);
        return invoker.invokeOn(impl, input);
    }
}
