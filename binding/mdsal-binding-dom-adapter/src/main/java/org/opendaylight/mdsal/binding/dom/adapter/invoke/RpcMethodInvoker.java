/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.invoke;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.ListenableFuture;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Optional;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.RpcResult;

@Beta
public abstract class RpcMethodInvoker {

    RpcMethodInvoker() {
        // Hidden on purpose
    }

    static RpcMethodInvoker from(final Method method) {
        final MethodHandle methodHandle;
        try {
            methodHandle = MethodHandles.publicLookup().unreflect(method);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Lookup on public method failed.", e);
        }

        final Optional<Class<? extends DataContainer>> input = BindingReflections.resolveRpcInputClass(method);
        if (input.isPresent()) {
            return new RpcMethodInvokerWithInput(methodHandle);
        }
        return new RpcMethodInvokerWithoutInput(methodHandle);
    }

    public abstract ListenableFuture<RpcResult<?>> invokeOn(RpcService impl, DataObject input);
}
