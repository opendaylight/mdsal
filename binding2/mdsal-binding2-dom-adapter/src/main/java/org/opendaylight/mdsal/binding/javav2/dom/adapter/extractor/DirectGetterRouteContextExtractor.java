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
final class DirectGetterRouteContextExtractor extends ContextReferenceExtractor {

    private static final Lookup PUBLIC_LOOKUP = MethodHandles.publicLookup();

    private final MethodHandle handle;

    private DirectGetterRouteContextExtractor(final MethodHandle rawHandle) {
        handle = rawHandle.asType(MethodType.methodType(InstanceIdentifier.class, TreeNode.class));
    }

    static ContextReferenceExtractor create(final Method getterMethod) throws IllegalAccessException {
        final MethodHandle getterHandle = PUBLIC_LOOKUP.unreflect(getterMethod);
        return new DirectGetterRouteContextExtractor(getterHandle);
    }

    @SuppressWarnings("checkstyle:IllegalCatch")
    @Override
    public InstanceIdentifier<? extends TreeNode> extract(final TreeNode obj) {
        try {
            return (InstanceIdentifier<? extends TreeNode>) handle.invokeExact(obj);
        } catch (final Throwable e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }
}