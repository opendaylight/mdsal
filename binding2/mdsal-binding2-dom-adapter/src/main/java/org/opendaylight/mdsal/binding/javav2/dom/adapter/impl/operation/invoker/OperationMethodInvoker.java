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
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import org.opendaylight.mdsal.binding.javav2.runtime.reflection.BindingReflections;
import org.opendaylight.mdsal.binding.javav2.spec.base.Input;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.KeyedInstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;

@Beta
abstract class OperationMethodInvoker {

    private static final Lookup LOOKUP = MethodHandles.publicLookup();

    protected static OperationMethodInvoker from(final Method method) {
        final Optional<Class<? extends Input<?>>> input = BindingReflections.resolveOperationInputClass(method);
        final Optional<Class<InstanceIdentifier<? extends TreeNode>>> ii =
            BindingReflections.resolveOperationIIClass(method);
        final Optional<Class<KeyedInstanceIdentifier<? extends TreeNode, ?>>> kii =
            BindingReflections.resolveOperationKeyedIIClass(method);
        try {
            final MethodHandle methodHandle = LOOKUP.unreflect(method);
            if (input.isPresent()) {
                if (ii.isPresent()) {
                    return new ActionMethodInvokerWithInput(methodHandle);
                } else if (kii.isPresent()) {
                    return new ActionMethodInvokerWithInput(methodHandle);
                } else {
                    return new RpcMethodInvokerWithInput(methodHandle);
                }
            }
            return new OperationMethodInvokerWithoutInput(methodHandle);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException("Lookup on public method failed.", e);
        }
    }
}
