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
import org.opendaylight.mdsal.binding.javav2.spec.base.Rpc;
import org.opendaylight.mdsal.binding.javav2.spec.base.RpcCallback;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;

@Beta
class OperationMethodInvokerWithInput extends OperationMethodInvoker {

    private static final MethodType INVOCATION_SIGNATURE =
            MethodType.methodType(void.class, Rpc.class, TreeNode.class, RpcCallback.class);

    private final MethodHandle handle;

    OperationMethodInvokerWithInput(final MethodHandle methodHandle) {
        this.handle = methodHandle.asType(INVOCATION_SIGNATURE);
    }

    @Override
    public void invokeOn(final Rpc<?, ?> impl, final TreeNode input, final RpcCallback<?> callback) {
        invoking(handle, impl, input, callback);
    }
}
