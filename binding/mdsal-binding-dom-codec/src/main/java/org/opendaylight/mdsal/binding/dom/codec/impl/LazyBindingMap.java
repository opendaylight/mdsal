/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.codec.impl.KeyedListNodeCodecContext.Unordered;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;

/**
 * Lazily-populated Map of binding DTOs. This implementation acts as the main entry point, so that we can decide on the
 * translation strategy we are going to use. We make that decision based on the first method that touches the mappings
 * (or materializes a view).
 *
 * @param <K> key type
 * @param <V> value type
 */
final class LazyBindingMap<K extends Identifier<V>, V extends DataObject & Identifiable<K>>
        extends AbstractMap<K, V> implements Immutable {
    private static final VarHandle DELEGATE;

    static {
        try {
            DELEGATE = MethodHandles.lookup().findVarHandle(LazyBindingMap.class, "delegate",
                LazyBindingMapState.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final @NonNull Unordered<V> codec;
    private final @NonNull MapNode mapNode;

    // Used via VarHandle above
    @SuppressWarnings("unused")
    private volatile LazyBindingMapState<K, V> delegate;

    LazyBindingMap(final Unordered codec, final MapNode mapNode) {
        this.codec = requireNonNull(codec);
        this.mapNode = requireNonNull(mapNode);
    }

    @Override
    public int size() {
        return mapNode.size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public V remove(final Object key) {
        throw uoe();
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        throw uoe();
    }

    @Override
    public void clear() {
        throw uoe();
    }

    @Override
    public boolean containsKey(final Object key) {
        return lookupState().containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        /*
         * This implementation relies on the relationship specified by Identifiable/Identifier and its use in binding
         * objects. The key is a wrapper object composed of a subset (or all) properties in the value, i.e. we have
         * a partial index.
         *
         * Instead of performing an O(N) search, we extract the key from the value, look the for the corresponding
         * mapping. If we find a mapping we check if the mapped value equals the the value being looked up.
         *
         * Note we prefer throwing ClassCastException/NullPointerException when presented with null or an object which
         * cannot possibly be contained in this map.
         */
        final V cast = codec.getBindingClass().cast(requireNonNull(value));
        final V found = get(cast.key());
        return found != null && cast.equals(found);
    }

    @Override
    public V get(final Object key) {
        return lookupState().get(key);
    }

    @Override
    public Set<K> keySet() {
        return iterState().keySet();
    }

    @Override
    public Collection<V> values() {
        return iterState().values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return iterState().entrySet();
    }

    private @NonNull LazyBindingMapState<K, V> lookupState() {
        final LazyBindingMapState<K, V> local;
        return (local = (LazyBindingMapState<K, V>) DELEGATE.getAcquire(this)) != null ? local : loadLookup();
    }

    private @NonNull LazyBindingMapState<K, V> iterState() {
        final LazyBindingMapState<K, V> local;
        return (local = (LazyBindingMapState<K, V>) DELEGATE.getAcquire(this)) != null ? local : loadIter();
    }

    @SuppressWarnings("unchecked")
    private @NonNull LazyBindingMapState<K, V> loadLookup() {
        final LazyBindingMapState<K, V> created = new LazyBindingMapState.LookupState<>(codec, mapNode);
        final Object witness;
        return (witness = DELEGATE.compareAndExchangeRelease(this, null, created)) == null ? created
                : (LazyBindingMapState<K, V>) witness;
    }

    @SuppressWarnings("unchecked")
    private @NonNull LazyBindingMapState<K, V> loadIter() {
        final LazyBindingMapState<K, V> created = new LazyBindingMapState.IterState<>(codec, mapNode);
        final Object witness;
        return (witness = DELEGATE.compareAndExchangeRelease(this, null, created)) == null ? created
                : (LazyBindingMapState<K, V>) witness;
    }

    private static UnsupportedOperationException uoe() {
        return new UnsupportedOperationException("Modification is not supported");
    }
}
