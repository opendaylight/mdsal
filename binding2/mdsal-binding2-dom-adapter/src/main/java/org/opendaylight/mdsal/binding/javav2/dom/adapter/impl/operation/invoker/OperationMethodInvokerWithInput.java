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
import org.opendaylight.mdsal.binding.javav2.spec.base.Action;
import org.opendaylight.mdsal.binding.javav2.spec.base.Instantiable;
import org.opendaylight.mdsal.binding.javav2.spec.base.ListAction;
import org.opendaylight.mdsal.binding.javav2.spec.base.Operation;
import org.opendaylight.mdsal.binding.javav2.spec.base.Rpc;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.yangtools.yang.common.RpcResult;

@Beta
class OperationMethodInvokerWithInput extends OperationMethodInvoker {

    private static final MethodType INVOCATION_SIGNATURE =
            MethodType.methodType(Future.class, Rpc.class, Action.class, ListAction.class, Instantiable.class);

    private final MethodHandle handle;

    OperationMethodInvokerWithInput(final MethodHandle methodHandle) {
        this.handle = methodHandle.asType(INVOCATION_SIGNATURE);
    }

    @Override
    public <T extends Operation> Future<RpcResult<?>> invokeOn(final T impl, final TreeNode input) {
        return invoking(handle, impl, input);
    }
}
