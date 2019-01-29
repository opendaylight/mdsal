/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.opendaylight.yangtools.yang.binding.BindingObject;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.TypeObject;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

final class BindingNormalizedNodeCache extends CacheLoader<BindingObject, NormalizedNode<?, ?>> {

    private final LoadingCache<BindingObject, NormalizedNode<?, ?>> cache = CacheBuilder.newBuilder().weakValues()
            .build(this);
    final NodeCodecContext<?> subtreeRoot;
    final AbstractBindingNormalizedNodeCacheHolder cacheHolder;

    BindingNormalizedNodeCache(final AbstractBindingNormalizedNodeCacheHolder cacheHolder,
            final NodeCodecContext<?> subtreeRoot) {
        this.cacheHolder = requireNonNull(cacheHolder, "cacheHolder");
        this.subtreeRoot = requireNonNull(subtreeRoot, "subtreeRoot");
    }

    @Override
    public NormalizedNode<?, ?> load(final BindingObject key) {
        if (key instanceof DataObject) {
            Preconditions.checkArgument(subtreeRoot instanceof  DataContainerCodecContext);
            return CachingNormalizedNodeSerializer.serializeUsingStreamWriter(cacheHolder,
                    (DataContainerCodecContext) subtreeRoot, (DataObject) key);
        }

        Preconditions.checkArgument(subtreeRoot instanceof  LeafNodeCodecContext);
        Preconditions.checkArgument(key instanceof TypeObject);
        return CachingNormalizedNodeSerializer.serializeLeafNode((LeafNodeCodecContext) subtreeRoot, (TypeObject) key);
    }

    /**
     * Returns cached NormalizedNode representation of DataObject. If the representation is not cached, serializes
     * DataObject and updates cache with representation.
     *
     * @param obj Binding object to be deserialized
     * @return NormalizedNode representation of binding object.
     */
    NormalizedNode<?, ?> get(final BindingObject obj) {
        return cache.getUnchecked(obj);
    }
}
