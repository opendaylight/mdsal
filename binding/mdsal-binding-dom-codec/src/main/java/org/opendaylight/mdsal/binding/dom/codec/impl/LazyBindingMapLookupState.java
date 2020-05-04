/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;

/**
 * {@link LazyBindingMap.State} optimized for lookup access, mainly via {@link Map#values()}.
 *
 * @param <K> key type
 * @param <V> value type
 */
final class LazyBindingMapLookupState<K extends Identifier<V>, V extends DataObject & Identifiable<K>>
        extends LazyBindingMap.State<K, V> {
    private final ImmutableMap<K, V> delegate;

    LazyBindingMapLookupState(final LazyBindingMap<K, V> map) {
        // FIXME: MDSAL-539: Make this a lazily-populated map and adjust our implementations accordingly
        final Builder<K, V> builder = ImmutableMap.builderWithExpectedSize(map.size());
        for (MapEntryNode node : map.mapNode().getValue()) {
            final V entry = map.createValue(node);
            builder.put(entry.key(), entry);
        }
        delegate = builder.build();
    }

    @Override
    boolean containsKey(final Object key) {
        return delegate.containsKey(key);
    }

    @Override
    V get(final Object key) {
        return delegate.get(key);
    }

    @Override
    Set<K> keySet() {
        return delegate.keySet();
    }

    @Override
    Collection<V> values() {
        return delegate.values();
    }

    @Override
    Set<Entry<K, V>> entrySet() {
        return delegate.entrySet();
    }
}
