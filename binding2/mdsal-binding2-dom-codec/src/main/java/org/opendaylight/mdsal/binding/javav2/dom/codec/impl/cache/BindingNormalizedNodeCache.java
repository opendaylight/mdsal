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
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base.DataContainerCodecContext;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.serializer.CachingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Cached NormalizedNode representation of TreeNode.
 */
@Beta
public class BindingNormalizedNodeCache extends CacheLoader<TreeNode, NormalizedNode<?, ?>> {

    @SuppressWarnings("rawtypes")
    private final DataContainerCodecContext subtreeRoot;
    private final AbstractBindingNormalizedNodeCacheHolder cacheHolder;
    private final LoadingCache<TreeNode, NormalizedNode<?, ?>> cache =
            CacheBuilder.newBuilder().weakValues().build(this);

    public BindingNormalizedNodeCache(@Nonnull final AbstractBindingNormalizedNodeCacheHolder cacheHolder,
            @Nonnull final DataContainerCodecContext<?, ?> subtreeRoot) {
        this.cacheHolder = requireNonNull(cacheHolder, "cacheHolder");
        this.subtreeRoot = requireNonNull(subtreeRoot, "subtreeRoot");
    }

    @Override
    public NormalizedNode<?, ?> load(@Nonnull final TreeNode key) throws Exception {
        return CachingNormalizedNodeSerializer.serializeUsingStreamWriter(cacheHolder, subtreeRoot, key);
    }

    /**
     * Returns cached NormalizedNode representation of TreeNode.
     *
     * <p>
     * If representation is not cached, serializes TreeNode and updates cache
     * with representation.
     *
     * @param obj
     *            - binding object to be deserialized
     * @return NormalizedNode representation of binding object
     */
    public NormalizedNode<?, ?> get(final TreeNode obj) {
        return cache.getUnchecked(obj);
    }
}
