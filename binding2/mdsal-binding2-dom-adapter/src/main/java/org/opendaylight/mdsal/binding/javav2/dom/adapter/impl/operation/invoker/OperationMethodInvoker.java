/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.operation.invoker;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import org.opendaylight.mdsal.binding.javav2.runtime.reflection.BindingReflections;
import org.opendaylight.mdsal.binding.javav2.spec.base.Instantiable;
import org.opendaylight.mdsal.binding.javav2.spec.base.Rpc;
import org.opendaylight.mdsal.binding.javav2.spec.base.RpcCallback;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;

@Beta
abstract class OperationMethodInvoker {

    private static final Lookup LOOKUP = MethodHandles.publicLookup();

    protected abstract void invokeOn(Rpc<?, ?> impl, TreeNode input, RpcCallback<?> callback);

    protected static OperationMethodInvoker from(final Method method) {
        final Optional<Class<? extends Instantiable<?>>> input = BindingReflections.resolveOperationInputClass(method);
        try {
            final MethodHandle methodHandle = LOOKUP.unreflect(method);
            if (input.isPresent()) {
                return new OperationMethodInvokerWithInput(methodHandle);
            }
            return new OperationMethodInvokerWithoutInput(methodHandle);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException("Lookup on public method failed.", e);
        }
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    protected void invoking(final MethodHandle handle, final Rpc<?, ?> impl, final TreeNode input,
            final RpcCallback<?> callback) {
        try {
            handle.invokeExact(impl, input, callback);
        } catch (final Throwable e) {
            throw Throwables.propagate(e);
        }
    }
}
