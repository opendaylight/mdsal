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
import java.util.concurrent.Future;
import org.opendaylight.mdsal.binding.javav2.runtime.reflection.BindingReflections;
import org.opendaylight.mdsal.binding.javav2.spec.base.Input;
import org.opendaylight.mdsal.binding.javav2.spec.base.Operation;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.yangtools.yang.common.RpcResult;

@Beta
abstract class OperationMethodInvoker {

    private static final Lookup LOOKUP = MethodHandles.publicLookup();

    protected abstract <T extends Operation> Future<RpcResult<?>> invokeOn(T impl, TreeNode input);

    protected static OperationMethodInvoker from(final Method method) {
        final Optional<Class<? extends Input<?>>> input = BindingReflections.resolveOperationInputClass(method);
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
    protected Future<RpcResult<?>> invoking(final MethodHandle handle, final Object... args) {
        try {
            return (Future<RpcResult<?>>) handle.invokeExact(args);
        } catch (final Throwable e) {
            throw Throwables.propagate(e);
        }
    }
}
