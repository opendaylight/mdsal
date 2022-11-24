/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.invoke;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ListenableFuture;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.RpcResult;

@Deprecated(since = "11.0.0", forRemoval = true)
public final class RpcMethodInvoker {
    private static final MethodType INVOCATION_SIGNATURE = MethodType.methodType(ListenableFuture.class,
        RpcService.class, DataObject.class);

    private final MethodHandle handle;

    @VisibleForTesting
    RpcMethodInvoker(final MethodHandle handle) {
        this.handle = handle.asType(INVOCATION_SIGNATURE);
    }

    public static @NonNull RpcMethodInvoker from(final Method method) {
        try {
            return new RpcMethodInvoker(MethodHandles.publicLookup().unreflect(method));
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Lookup on public method failed.", e);
        }
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    public @NonNull ListenableFuture<RpcResult<?>> invokeOn(final RpcService impl, final DataObject input) {
        try {
            return (ListenableFuture<RpcResult<?>>) handle.invokeExact(impl, input);
        } catch (Throwable e) {
            Throwables.throwIfUnchecked(e);
            throw new IllegalStateException(e);
        }
    }
}
