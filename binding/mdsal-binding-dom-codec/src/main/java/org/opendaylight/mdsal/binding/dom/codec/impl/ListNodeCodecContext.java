/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ListNodeCodecContext<D extends DataObject> extends DataObjectCodecContext<D, ListSchemaNode> {
    private static final Logger LOG = LoggerFactory.getLogger(ListNodeCodecContext.class);
    private static final String LAZY_CUTOFF_PROPERTY =
            "org.opendaylight.mdsal.binding.dom.codec.impl.ListNodeCodecContext.LAZY_CUTOFF";
    private static final int DEFAULT_LAZY_CUTOFF = 32;
    private static final int LAZY_CUTOFF;

    static {
        final int value = Integer.getInteger(LAZY_CUTOFF_PROPERTY, DEFAULT_LAZY_CUTOFF);
        if (value < 0) {
            LOG.info("Using lazy population of lists disabled");
            LAZY_CUTOFF = Integer.MAX_VALUE;
        } else {
            LOG.info("Using lazy population of lists larger than {} elements", value);
            LAZY_CUTOFF = value;
        }
    }

    ListNodeCodecContext(final DataContainerCodecPrototype<ListSchemaNode> prototype) {
        super(prototype);
    }

    ListNodeCodecContext(final DataContainerCodecPrototype<ListSchemaNode> prototype,
            final Method keyMethod) {
        super(prototype, keyMethod);
    }

    @Override
    public D deserialize(final NormalizedNode<?, ?> node) {
        if (node instanceof MapEntryNode) {
            return createBindingProxy((MapEntryNode) node);
        } else if (node instanceof UnkeyedListEntryNode) {
            return createBindingProxy((UnkeyedListEntryNode) node);
        } else {
            throw new IllegalStateException("Unsupported data type " + node.getClass());
        }
    }

    @Override
    protected Object deserializeObject(final NormalizedNode<?, ?> node) {
        if (node instanceof MapNode) {
            return fromMap((MapNode) node);
        } else if (node instanceof MapEntryNode) {
            return createBindingProxy((MapEntryNode) node);
        } else if (node instanceof UnkeyedListNode) {
            return fromUnkeyedList((UnkeyedListNode) node);
        } else if (node instanceof UnkeyedListEntryNode) {
            return createBindingProxy((UnkeyedListEntryNode) node);
        } else {
            throw new IllegalStateException("Unsupported data type " + node.getClass());
        }
    }

    @NonNull Object fromMap(final MapNode map, final int size) {
        return createList(map.getValue(), size);
    }

    private Object fromMap(final MapNode map) {
        final int size;
        // This should never happen, but we do need to ensure users never see an empty Map
        return (size = map.size()) == 0 ? null : fromMap(map, size);
    }

    private List<D> fromUnkeyedList(final UnkeyedListNode node) {
        final int size;
        // This should never happen, but we do need to ensure users never see an empty List
        return (size = node.getSize()) == 0 ? null : createList(node.getValue(), size);
    }

    private @NonNull List<D> createList(final Collection<? extends NormalizedNodeContainer<?, ?, ?>> value,
            final int size) {
        if (size == 1) {
            // Do not bother with lazy instantiation for singletons
            return List.of(createBindingProxy(value.iterator().next()));
        }
        if (size > LAZY_CUTOFF) {
            return new LazyBindingList<>(this, value);
        }

        final Builder<D> builder = ImmutableList.builderWithExpectedSize(size);
        for (NormalizedNodeContainer<?, ?, ?> node : value) {
            builder.add(createBindingProxy(node));
        }
        return builder.build();
    }
}
