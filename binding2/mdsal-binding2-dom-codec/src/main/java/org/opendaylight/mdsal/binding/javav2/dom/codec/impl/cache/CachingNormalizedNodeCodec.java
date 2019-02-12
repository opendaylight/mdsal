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
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.dom.codec.api.BindingNormalizedNodeCachingCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base.DataContainerCodecContext;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.serializer.CachingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Caching codec.
 *
 * @param <D>
 *            - type of tree node
 */
@Beta
public class CachingNormalizedNodeCodec<D extends TreeNode> extends AbstractBindingNormalizedNodeCacheHolder
        implements BindingNormalizedNodeCachingCodec<D> {

    private final DataContainerCodecContext<D, ?> context;

    public CachingNormalizedNodeCodec(final DataContainerCodecContext<D, ?> subtreeRoot,
            final Set<Class<? extends TreeNode>> cacheSpec) {
        super(cacheSpec);
        this.context = requireNonNull(subtreeRoot);
    }

    @Nonnull
    @Override
    public D deserialize(@Nonnull final NormalizedNode<?, ?> data) {
        return context.deserialize(data);
    }

    @Nonnull
    @Override
    public NormalizedNode<?, ?> serialize(@Nonnull final D data) {
        return CachingNormalizedNodeSerializer.serialize(this, context, data);
    }

    @Override
    public void close() {
    }
}