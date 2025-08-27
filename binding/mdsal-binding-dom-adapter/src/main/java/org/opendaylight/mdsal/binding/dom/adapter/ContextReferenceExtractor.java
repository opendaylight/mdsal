/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.BindingInstanceIdentifier;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.annotations.RoutingContext;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract sealed class ContextReferenceExtractor {
    @VisibleForTesting
    static final class Direct extends ContextReferenceExtractor {
        private final MethodHandle handle;

        private Direct(final MethodHandle rawHandle) {
            handle = rawHandle.asType(MethodType.methodType(BindingInstanceIdentifier.class, DataObject.class));
        }

        @VisibleForTesting
        static ContextReferenceExtractor create(final Method getterMethod) throws IllegalAccessException {
            return new Direct(MethodHandles.publicLookup().unreflect(getterMethod));
        }

        @Override
        BindingInstanceIdentifier extractImpl(final DataObject obj) throws Throwable {
            return (BindingInstanceIdentifier) handle.invokeExact(obj);
        }
    }

    @VisibleForTesting
    static final class GetValue extends ContextReferenceExtractor {
        private final MethodHandle contextHandle;
        private final MethodHandle valueHandle;

        private GetValue(final MethodHandle rawContextHandle, final MethodHandle rawValueHandle) {
            contextHandle = rawContextHandle.asType(MethodType.methodType(Object.class, DataObject.class));
            valueHandle = rawValueHandle.asType(MethodType.methodType(BindingInstanceIdentifier.class, Object.class));
        }

        private static ContextReferenceExtractor create(final Method contextGetter, final Method getValueMethod)
                throws IllegalAccessException {
            final var lookup = MethodHandles.publicLookup();
            return new GetValue(lookup.unreflect(contextGetter), lookup.unreflect(getValueMethod));
        }

        @Override
        BindingInstanceIdentifier extractImpl(final DataObject obj) throws Throwable {
            final var ctx = contextHandle.invokeExact(obj);
            return ctx == null ? null : (BindingInstanceIdentifier) valueHandle.invokeExact(ctx);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(ContextReferenceExtractor.class);

    static @Nullable ContextReferenceExtractor of(final Class<?> type) {
        final var contextGetter = getContextGetter(type);
        if (contextGetter == null) {
            return null;
        }

        final var returnType = contextGetter.getReturnType();
        try {
            if (BindingInstanceIdentifier.class.isAssignableFrom(returnType)) {
                return Direct.create(contextGetter);
            }
            final var getValueMethod = findGetValueMethod(returnType, BindingInstanceIdentifier.class);
            if (getValueMethod != null) {
                return GetValue.create(contextGetter, getValueMethod);
            } else {
                LOG.warn("Class {} can not be used to determine context, falling back to NULL_EXTRACTOR.", returnType);
            }
        } catch (final IllegalAccessException e) {
            LOG.warn("Class {} does not conform to Binding Specification v1. Falling back to NULL_EXTRACTOR",
                returnType, e);
        }
        return null;
    }

    /**
     * Extract context-reference (Instance Identifier) from a Binding DataObject.
     *
     * @param obj DataObject from which context reference should be extracted.
     *
     * @return Instance Identifier representing context reference or null, if data object does not contain a context
     *         reference.
     */
    @SuppressWarnings("checkstyle:IllegalCatch")
    final @Nullable BindingInstanceIdentifier extract(final DataObject obj) {
        try {
            return extractImpl(obj);
        } catch (Throwable e) {
            Throwables.throwIfUnchecked(e);
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("checkstyle:IllegalThrows")
    @SuppressFBWarnings(value = "THROWS_METHOD_THROWS_CLAUSE_THROWABLE",
        justification = "https://github.com/spotbugs/spotbugs/issues/3644")
    abstract @Nullable BindingInstanceIdentifier extractImpl(DataObject obj) throws Throwable;

    private static @Nullable Method findGetValueMethod(final Class<?> type, final Class<?> returnType) {
        final Method method;
        try {
            method = type.getMethod(Naming.SCALAR_TYPE_OBJECT_GET_VALUE_NAME);
        } catch (NoSuchMethodException e) {
            LOG.warn("Value class {} does not comform to Binding Specification v1.", type, e);
            return null;
        }

        if (returnType.equals(method.getReturnType())) {
            return method;
        }
        return null;
    }

    private static Method getContextGetter(final Class<?> type) {
        for (var method : type.getMethods()) {
            if (method.getAnnotation(RoutingContext.class) != null) {
                return method;
            }
        }
        return null;
    }
}
