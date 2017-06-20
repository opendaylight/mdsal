/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.extractor;

import com.google.common.annotations.Beta;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.lang.reflect.Method;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding.javav2.api.annotation.RoutingContext;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tree node reference extractor.
 */
@Beta
public abstract class ContextReferenceExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(ContextReferenceExtractor.class);
    private static final String GET_VALUE_NAME = "getValue";

    private static final ContextReferenceExtractor NULL_EXTRACTOR = new ContextReferenceExtractor() {

        @Nullable
        @Override
        public InstanceIdentifier<? extends TreeNode> extract(final TreeNode obj) {
            return null;
        }
    };

    private static final LoadingCache<Class<?>, ContextReferenceExtractor> EXTRACTORS =
            CacheBuilder.newBuilder().weakKeys().build(new CacheLoader<Class<?>, ContextReferenceExtractor>() {

                @Nonnull
                @Override
                public ContextReferenceExtractor load(@Nonnull final Class<?> key) throws Exception {
                    return create(key);
                }

                private ContextReferenceExtractor create(final Class<?> key) {
                    final Method contextGetter = getContextGetter(key);
                    if (contextGetter == null) {
                        return NULL_EXTRACTOR;
                    }
                    final Class<?> returnType = contextGetter.getReturnType();
                    try {
                        if (InstanceIdentifier.class.isAssignableFrom(returnType)) {
                            return DirectGetterRouteContextExtractor.create(contextGetter);
                        }
                        final Method getValueMethod = findGetValueMethod(returnType);
                        if (getValueMethod != null) {
                            return GetValueRouteContextExtractor.create(contextGetter, getValueMethod);
                        } else {
                            LOG.warn("Class {} can not be used to determine context, falling back to NULL_EXTRACTOR.",
                                    returnType);
                        }
                    } catch (final IllegalAccessException e) {
                        LOG.warn("Class {} does not conform to Binding Specification. Falling back to NULL_EXTRACTOR",
                                e);
                    }
                    return NULL_EXTRACTOR;
                }

                private Method getContextGetter(final Class<?> key) {
                    for (final Method method : key.getMethods()) {
                        if (method.getAnnotation(RoutingContext.class) != null) {
                            return method;
                        }
                    }
                    return null;
                }
            });

    /**
     * Extract context-reference (Instance Identifier) from Binding TreeNode.
     *
     * @param input
     *            - TreeNode from which context reference should be extracted.
     *
     * @return Instance Identifier representing context reference or null, if tree node does not contain
     *         context reference.
     */
    @Nullable
    public abstract InstanceIdentifier<? extends TreeNode> extract(TreeNode input);

    /**
     * Method for return specific extractor of input object.
     *
     * @param obj
     *            - object for get specific extractor
     * @return specific extractor
     */
    public static ContextReferenceExtractor from(final Class<?> obj) {
        return EXTRACTORS.getUnchecked(obj);
    }

    private static Method findGetValueMethod(final Class<?> type) {
        try {
            final Method method = type.getMethod(GET_VALUE_NAME);
            if (InstanceIdentifier.class.equals(method.getReturnType())) {
                return method;
            }
        } catch (final NoSuchMethodException e) {
            LOG.warn("Value class {} does not comform to Binding Specification.", type, e);
        }
        return null;
    }
}

