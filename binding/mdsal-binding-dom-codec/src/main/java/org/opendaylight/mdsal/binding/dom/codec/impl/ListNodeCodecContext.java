/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

class ListNodeCodecContext<D extends DataObject> extends DataObjectCodecContext<D,ListSchemaNode> {
    private final @Nullable List<?> defaultObject;

    ListNodeCodecContext(final DataContainerCodecPrototype<ListSchemaNode> prototype) {
        super(prototype);
        final Optional<ElementCountConstraint> optConstraint = prototype.getSchema().getElementCountConstraint();
        if (optConstraint.isPresent()) {
            final Integer minElements = optConstraint.get().getMinElements();
            defaultObject = minElements == null || minElements < 1 ? ImmutableList.of() : null;
        } else {
            defaultObject = ImmutableList.of();
        }
    }

    @Override
    public D deserialize(final NormalizedNode<?, ?> node) {
        if (node instanceof MapEntryNode) {
            return fromMapEntry((MapEntryNode) node);
        } else if (node instanceof UnkeyedListEntryNode) {
            return fromUnkeyedListEntry((UnkeyedListEntryNode) node);
        } else {
            throw new IllegalStateException("Unsupported data type " + node.getClass());
        }
    }

    @Override
    protected Object deserializeObject(final NormalizedNode<?, ?> node) {
        if (node instanceof MapNode) {
            return fromMap((MapNode) node);
        } else if (node instanceof MapEntryNode) {
            return fromMapEntry((MapEntryNode) node);
        } else if (node instanceof UnkeyedListNode) {
            return fromUnkeyedList((UnkeyedListNode) node);
        } else if (node instanceof UnkeyedListEntryNode) {
            return fromUnkeyedListEntry((UnkeyedListEntryNode) node);
        } else {
            throw new IllegalStateException("Unsupported data type " + node.getClass());
        }
    }

    final @Nullable Object defaultObject() {
        return defaultObject;
    }

    private List<D> fromMap(final MapNode nodes) {
        List<D> ret = new ArrayList<>(nodes.getValue().size());
        for (MapEntryNode node : nodes.getValue()) {
            ret.add(fromMapEntry(node));
        }
        return ret;
    }

    private D fromMapEntry(final MapEntryNode node) {
        return createBindingProxy(node);
    }

    private D fromUnkeyedListEntry(final UnkeyedListEntryNode node) {
        return createBindingProxy(node);
    }

    private List<D> fromUnkeyedList(final UnkeyedListNode nodes) {
        // FIXME: Could be this lazy transformed list?
        List<D> ret = new ArrayList<>(nodes.getValue().size());
        for (UnkeyedListEntryNode node : nodes.getValue()) {
            ret.add(fromUnkeyedListEntry(node));
        }
        return ret;
    }
}
