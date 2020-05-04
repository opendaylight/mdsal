/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.codec.impl.KeyedListNodeCodecContext.Unordered;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;

/**
 * Internal state attached to {@link LazyBindingMap}.
 */
abstract class LazyBindingMapState<K extends Identifier<V>, V extends DataObject & Identifiable<K>> {
    static final class IterState<K extends Identifier<V>, V extends DataObject & Identifiable<K>>
            extends LazyBindingMapState<K, V> {

        // FIXME: here we want to eagerly instantiate iterator-order translation of values, as we will be accessing
        //        it. Population of those values should be lazy, though.

        // FIXME: lookup map would be populated on first access with the contents of the iterator-order values,
        //        and then performed. Once a mixed-mode access is done ... we would end up populating both

        IterState(final Unordered<V> codec, final MapNode mapNode) {
            super(codec, mapNode);
        }
    }

    static final class LookupState<K extends Identifier<V>, V extends DataObject & Identifiable<K>>
            extends LazyBindingMapState<K, V> {

        // FIXME: here we want to eagerly instantiate lookup map, as we will be accessing that. It is furthermore likely
        //        there will not be other accesses

        // FIXME: iterator-order values would be instantiated on first access

        LookupState(final Unordered<V> codec, final MapNode mapNode) {
            super(codec, mapNode);
        }
    }

    private final Unordered<V> codec;
    private final MapNode mapNode;

    LazyBindingMapState(final Unordered<V> codec, final MapNode mapNode) {
        this.codec = requireNonNull(codec);
        this.mapNode = requireNonNull(mapNode);
    }

    private @NonNull Map<K, V> createDelegate() {
        final Builder<K, V> builder = ImmutableMap.builderWithExpectedSize(mapNode.size());
        for (MapEntryNode node : mapNode.getValue()) {
            final V entry = codec.fromMapEntry(node);
            builder.put(entry.key(), entry);
        }
        return builder.build();
    }

    final boolean containsKey(final Object key) {
        // TODO Auto-generated method stub
        return false;
    }

    final V get(final Object key) {
        // TODO Auto-generated method stub
        return null;
    }

    // TODO: we really can derive this from values() through the use of Identifiable::key
    final @NonNull Set<K> keySet() {
        // TODO Auto-generated method stub
        return null;
    }

    // TODO: we really can promise a Set<V> here
    final @NonNull Collection<V> values() {
        // TODO Auto-generated method stub
        return null;
    }

    // TODO: we really can derive this from values() through the use of
    //         value -> Map.entry(value.key(), value)
    final @NonNull Set<Entry<K, V>> entrySet() {
        // TODO Auto-generated method stub
        return null;
    }
}
