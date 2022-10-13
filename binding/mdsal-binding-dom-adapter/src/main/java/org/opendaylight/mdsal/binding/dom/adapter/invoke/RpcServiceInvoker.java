/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.invoke;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.util.concurrent.ListenableFuture;
import java.lang.reflect.Method;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides single method invocation of RPCs on supplied instance.
 *
 * <p>
 * RPC Service invoker provides common invocation interface for any subtype of {@link RpcService} via
 * {@link #invokeRpc(RpcService, QName, DataObject)} method.
 */
@Deprecated(since = "11.0.0", forRemoval = true)
public abstract class RpcServiceInvoker {
    private static final Logger LOG = LoggerFactory.getLogger(RpcServiceInvoker.class);

    /**
     * Creates an RPCServiceInvoker for specified QName-&lt;Method mapping.
     *
     * @param qnameToMethod translation mapping, must not be null nor empty.
     * @return An {@link RpcServiceInvoker} instance.
     */
    public static RpcServiceInvoker from(final Map<QName, Method> qnameToMethod) {
        checkArgument(!qnameToMethod.isEmpty());
        QNameModule module = null;

        for (QName qname : qnameToMethod.keySet()) {
            if (module != null) {
                if (!module.equals(qname.getModule())) {
                    LOG.debug("QNames from different modules {} and {}, falling back to QName map", module,
                        qname.getModule());
                    return QNameRpcServiceInvoker.instanceFor(qnameToMethod);
                }
            } else {
                module = qname.getModule();
            }
        }

        // All module are equal, which means we can use localName only
        return LocalNameRpcServiceInvoker.instanceFor(module, qnameToMethod);
    }

    /**
     * Invokes supplied RPC on provided implementation of RPC Service.
     *
     * @param impl Imlementation on which RPC should be invoked.
     * @param rpcName Name of RPC to be invoked.
     * @param input Input data for RPC.
     * @return Future which will complete once rpc procesing is finished.
     */
    public abstract ListenableFuture<RpcResult<?>> invokeRpc(@NonNull RpcService impl, @NonNull QName rpcName,
            @Nullable DataObject input);
}
