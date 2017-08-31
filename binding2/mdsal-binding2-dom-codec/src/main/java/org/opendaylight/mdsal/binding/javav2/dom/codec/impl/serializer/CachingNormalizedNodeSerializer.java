/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.impl.serializer;

import com.google.common.annotations.Beta;
import com.google.common.base.Throwables;
import java.io.IOException;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.cache.AbstractBindingNormalizedNodeCacheHolder;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.cache.BindingNormalizedNodeCache;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base.DataContainerCodecContext;
import org.opendaylight.mdsal.binding.javav2.spec.base.Instantiable;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingSerializer;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingStreamEventWriter;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.TreeNodeSerializer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;

/**
 * Serializer of Binding objects to Normalized Node which uses
 * {@link BindingNormalizedNodeCache} to cache already serialized values.
 *
 * This serializer implements {@link BindingStreamEventWriter} along with
 * {@link BindingSerializer}.
 *
 * {@link BindingSerializer} interface is used by generated implementations of
 * {@link TreeNodeSerializer} to provide Binding object for inspection and to
 * prevent streaming of already serialized object.
 */
@Beta
public final class CachingNormalizedNodeSerializer extends ForwardingBindingStreamEventWriter
        implements BindingSerializer<Object, TreeNode> {

    private final NormalizedNodeResult domResult;
    private final NormalizedNodeWithAddChildWriter domWriter;
    private final BindingToNormalizedStreamWriter delegate;
    private final AbstractBindingNormalizedNodeCacheHolder cacheHolder;

    private CachingNormalizedNodeSerializer(final AbstractBindingNormalizedNodeCacheHolder cacheHolder,
            final DataContainerCodecContext<?, ?> subtreeRoot) {
        this.cacheHolder = cacheHolder;
        this.domResult = new NormalizedNodeResult();
        this.domWriter = new NormalizedNodeWithAddChildWriter(domResult);
        this.delegate = BindingToNormalizedStreamWriter.create(subtreeRoot, domWriter);
    }

    @Override
    protected BindingStreamEventWriter delegate() {
        return delegate;
    }

    private NormalizedNode<?, ?> build() {
        return domResult.getResult();
    }

    /**
     * Serializes input if it is cached, returns null otherwise.
     *
     * If input is cached it uses
     * {@link NormalizedNodeWithAddChildWriter#addChild(NormalizedNode)} to
     * provide already serialized value to underlying NormalizedNodeWriter in
     * order to reuse value instead of creating new one using Normalized Node
     * stream APIs.
     *
     * Note that this optional is serialization of child node invoked from
     * {@link TreeNodeSerializer}, which may opt-out from streaming of data when
     * non-null result is returned.
     */
    @Override
    public NormalizedNode<?, ?> serialize(final TreeNode input) {
        final BindingNormalizedNodeCache cachingSerializer = getCacheSerializer(((Instantiable<?>) input).implementedInterface());
        if (cachingSerializer != null) {
            final NormalizedNode<?, ?> domData = cachingSerializer.get(input);
            domWriter.addChild(domData);
            return domData;
        }
        return null;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private BindingNormalizedNodeCache getCacheSerializer(final Class type) {
        if (cacheHolder.isCached(type)) {
            final DataContainerCodecContext<?, ?> currentCtx = (DataContainerCodecContext<?, ?>) delegate.current();
            if (type.equals(currentCtx.getBindingClass())) {
                return cacheHolder.getCachingSerializer(currentCtx);
            }
            return cacheHolder.getCachingSerializer(currentCtx.streamChild(type));
        }
        return null;
    }

    /**
     * Serializes supplied data using stream writer with child cache enabled or
     * using cache directly if cache is avalaible also for supplied Codec node.
     *
     * @param cacheHolder
     *            - Binding to Normalized Node Cache holder
     * @param subtreeRoot
     *            - codec Node for provided data object
     * @param data
     *            - data to be serialized
     * @return Normalized Node representation of data
     */
    public static NormalizedNode<?, ?> serialize(final AbstractBindingNormalizedNodeCacheHolder cacheHolder,
            final DataContainerCodecContext<?, ?> subtreeRoot, final TreeNode data) {
        final BindingNormalizedNodeCache cache = cacheHolder.getCachingSerializer(subtreeRoot);
        if (cache != null) {
            return cache.get(data);
        }
        return serializeUsingStreamWriter(cacheHolder, subtreeRoot, data);
    }

    /**
     * Serializes supplied data using stream writer with child cache enabled.
     *
     * @param cacheHolder
     *            - binding to Normalized Node Cache holder
     * @param subtreeRoot
     *            - codec Node for provided data object
     * @param data
     *            - data to be serialized
     * @return Normalized Node representation of data
     */
    public static NormalizedNode<?, ?> serializeUsingStreamWriter(
            final AbstractBindingNormalizedNodeCacheHolder cacheHolder,
            final DataContainerCodecContext<?, ?> subtreeRoot, final TreeNode data) {
        final CachingNormalizedNodeSerializer writer = new CachingNormalizedNodeSerializer(cacheHolder, subtreeRoot);
        try {
            subtreeRoot.eventStreamSerializer().serialize(data, writer);
            return writer.build();
        } catch (final IOException e) {
            throw Throwables.propagate(e);
        }
    }
}