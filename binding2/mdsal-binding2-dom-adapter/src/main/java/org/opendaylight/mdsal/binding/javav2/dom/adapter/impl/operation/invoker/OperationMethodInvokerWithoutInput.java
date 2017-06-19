/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.operation.invoker;

import com.google.common.annotations.Beta;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.concurrent.Future;
import org.opendaylight.mdsal.binding.javav2.spec.base.Instantiable;
import org.opendaylight.mdsal.binding.javav2.spec.base.Rpc;
import org.opendaylight.yangtools.yang.common.RpcResult;

@Beta
class OperationMethodInvokerWithoutInput extends OperationMethodInvoker {

    private static final MethodType INVOCATION_SIGNATURE = MethodType.methodType(Future.class, Rpc.class);
    private final MethodHandle handle;

    OperationMethodInvokerWithoutInput(final MethodHandle methodHandle) {
        this.handle = methodHandle.asType(INVOCATION_SIGNATURE);
    }

    @Override
    public Future<RpcResult<?>> invokeOn(final Object impl, final Instantiable<?> input) {
        return invoking(handle, impl);
    }
}
