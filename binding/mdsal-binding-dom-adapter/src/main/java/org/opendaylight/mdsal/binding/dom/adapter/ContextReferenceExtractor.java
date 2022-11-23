/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import java.lang.reflect.Method;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.annotations.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class ContextReferenceExtractor {
    private static final Logger LOG = LoggerFactory.getLogger(ContextReferenceExtractor.class);
    // FIXME: this should be somewhere in BindingMapping or similar -- it works with ScalarTypeObject after all, which
    //        is not obvious here
    private static final String GET_VALUE_NAME = "getValue";

    static @Nullable ContextReferenceExtractor of(final Class<?> type) {
        final var contextGetter = getContextGetter(type);
        if (contextGetter == null) {
            return null;
        }

        final var returnType = contextGetter.getReturnType();
        try {
            if (InstanceIdentifier.class.isAssignableFrom(returnType)) {
                return DirectGetterRouteContextExtractor.create(contextGetter);
            }
            final var getValueMethod = findGetValueMethod(returnType, InstanceIdentifier.class);
            if (getValueMethod != null) {
                return GetValueRouteContextExtractor.create(contextGetter, getValueMethod);
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
    abstract @Nullable InstanceIdentifier<?> extract(DataObject obj);

    private static @Nullable Method findGetValueMethod(final Class<?> type, final Class<?> returnType) {
        final Method method;
        try {
            method = type.getMethod(GET_VALUE_NAME);
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
