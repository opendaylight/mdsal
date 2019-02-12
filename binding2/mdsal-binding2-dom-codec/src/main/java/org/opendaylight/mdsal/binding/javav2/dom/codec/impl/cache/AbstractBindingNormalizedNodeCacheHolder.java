/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.impl.cache;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base.DataContainerCodecContext;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;

/**
 * Abstract Holder of Binding to Normalized Node caches indexed by
 * {@link DataContainerCodecContext} to which cache is associated.
 *
 */
@Beta
public abstract class AbstractBindingNormalizedNodeCacheHolder {

    private final Set<Class<? extends TreeNode>> cachedValues;
    private final LoadingCache<DataContainerCodecContext<?, ?>, BindingNormalizedNodeCache> caches = CacheBuilder
            .newBuilder().build(new CacheLoader<DataContainerCodecContext<?, ?>, BindingNormalizedNodeCache>() {

                @Override
                public BindingNormalizedNodeCache load(@Nonnull final DataContainerCodecContext<?, ?> key)
                        throws Exception {
                    return new BindingNormalizedNodeCache(AbstractBindingNormalizedNodeCacheHolder.this, key);
                }

            });

    protected AbstractBindingNormalizedNodeCacheHolder(@Nonnull final Set<Class<? extends TreeNode>> cacheSpec) {
        cachedValues = requireNonNull(cacheSpec);
    }

    public BindingNormalizedNodeCache getCachingSerializer(final DataContainerCodecContext<?, ?> childCtx) {
        if (isCached(childCtx.getBindingClass())) {
            return caches.getUnchecked(childCtx);
        }
        return null;
    }

    /**
     * Check if specific type is cached.
     *
     * @param type
     *            - type for check
     * @return true if type is cached, false otherwise
     */
    public boolean isCached(final Class<? extends TreeNode> type) {
        return cachedValues.contains(type);
    }
}

