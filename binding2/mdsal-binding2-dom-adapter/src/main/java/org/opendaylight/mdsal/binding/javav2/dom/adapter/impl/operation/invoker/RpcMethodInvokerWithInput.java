/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.operation.invoker;

import com.google.common.annotations.Beta;
import com.google.common.base.Throwables;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import org.opendaylight.mdsal.binding.javav2.spec.base.Operation;
import org.opendaylight.mdsal.binding.javav2.spec.base.RpcCallback;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;

@Beta
class RpcMethodInvokerWithInput extends OperationMethodInvoker {

    private static final MethodType INVOCATION_SIGNATURE =
            MethodType.methodType(void.class, Operation.class, TreeNode.class, RpcCallback.class);
    private final MethodHandle handle;

    RpcMethodInvokerWithInput(final MethodHandle methodHandle) {
        this.handle = methodHandle.asType(INVOCATION_SIGNATURE);
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    <T extends Operation> void invokeOn(final T impl, final TreeNode input,
            final RpcCallback<?> callback) {
        try {
            handle.invokeExact(impl, input, callback);
        } catch (Throwable error) {
            Throwables.throwIfUnchecked(error);
            throw new RuntimeException(error);
        }
    }
}
