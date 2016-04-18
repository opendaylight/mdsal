/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.util;

import static com.sun.xml.internal.ws.policy.sourcemodel.wspolicy.XmlToken.Optional;

import com.google.common.base.Optional;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.util.concurrent.Future;
import org.opendaylight.yangtools.yang.binding.InterfaceTyped;
import org.opendaylight.yangtools.yang.binding.TreeNode;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.RpcResult;

abstract class RpcMethodInvoker {

    private static final Lookup LOOKUP = MethodHandles.publicLookup();

    protected abstract Future<RpcResult<?>> invokeOn(RpcService impl, TreeNode input);

    protected static RpcMethodInvoker from(final Method method) {
        Optional<Class<? extends InterfaceTyped>> input = BindingReflections.resolveRpcInputClass(method);
        try {
            MethodHandle methodHandle = LOOKUP.unreflect(method);
            if (input.isPresent()) {
                return new RpcMethodInvokerWithInput(methodHandle);
            }
            return new RpcMethodInvokerWithoutInput(methodHandle);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Lookup on public method failed.",e);
        }

    }
}
