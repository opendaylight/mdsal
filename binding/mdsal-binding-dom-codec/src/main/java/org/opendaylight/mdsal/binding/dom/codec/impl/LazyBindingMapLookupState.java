/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.codec.impl.KeyedListNodeCodecContext.Unordered;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;

// FIXME: here we want to eagerly instantiate lookup map, as we will be accessing that. It is furthermore likely
//        there will not be other accesses
// FIXME: iterator-order values would be instantiated on first access
final class LazyBindingMapLookupState<K extends Identifier<V>, V extends DataObject & Identifiable<K>>
        extends LazyBindingMap.State<K, V> {

    LazyBindingMapLookupState(final Unordered<K, V> codec, final MapNode mapNode) {

    }

    @Override
    boolean containsKey(@NonNull final Object key) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    V get(@NonNull final Object key) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    @NonNull
    Set<K> keySet() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    @NonNull
    Collection<V> values() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    @NonNull
    Set<Entry<K, V>> entrySet() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }
}
