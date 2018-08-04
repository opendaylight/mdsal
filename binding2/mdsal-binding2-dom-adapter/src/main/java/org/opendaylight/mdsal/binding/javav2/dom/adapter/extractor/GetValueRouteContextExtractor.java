/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.extractor;

import com.google.common.annotations.Beta;
import com.google.common.base.Throwables;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;

@Beta
final class GetValueRouteContextExtractor extends ContextReferenceExtractor {

    private static final Lookup PUBLIC_LOOKUP = MethodHandles.publicLookup();

    private final MethodHandle contextHandle;
    private final MethodHandle valueHandle;

    private GetValueRouteContextExtractor(final MethodHandle rawContextHandle, final MethodHandle rawValueHandle) {
        contextHandle = rawContextHandle.asType(MethodType.methodType(Object.class, TreeNode.class));
        valueHandle = rawValueHandle.asType(MethodType.methodType(InstanceIdentifier.class, Object.class));
    }

    static ContextReferenceExtractor create(final Method contextGetter, final Method getValueMethod)
            throws IllegalAccessException {
        final MethodHandle rawContextHandle = PUBLIC_LOOKUP.unreflect(contextGetter);
        final MethodHandle rawValueHandle = PUBLIC_LOOKUP.unreflect(getValueMethod);
        return new GetValueRouteContextExtractor(rawContextHandle, rawValueHandle);
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @Override
    public InstanceIdentifier<? extends TreeNode> extract(final TreeNode obj) {
        try {
            final Object ctx = contextHandle.invokeExact(obj);
            if (ctx != null) {
                return (InstanceIdentifier<? extends TreeNode>) valueHandle.invokeExact(ctx);
            }
            return null;
        } catch (final Throwable e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }
}

