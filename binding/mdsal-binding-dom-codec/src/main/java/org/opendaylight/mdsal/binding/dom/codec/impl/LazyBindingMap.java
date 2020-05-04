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
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.codec.impl.KeyedListNodeCodecContext.Unordered;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;

final class LazyBindingMap<K extends Identifier<V>, V extends DataObject & Identifiable<K>> extends AbstractMap<K, V> {
    private static final VarHandle DELEGATE;

    static {
        try {
            DELEGATE = MethodHandles.lookup().findVarHandle(LazyBindingMap.class, "delegate", Map.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final @NonNull Unordered<V> codec;
    private final @NonNull MapNode mapNode;

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
        return lookupDelegate().containsKey(key);
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
        return lookupDelegate().get(key);
    }

    @Override
    public Set<K> keySet() {
        // TODO: we really can derive this from values() through the use of Identifiable::key
        return iterDelegate().keySet();
    }

    @Override
    public Collection<V> values() {
        // TODO: we really can promise a Set<V> here
        return iterDelegate().values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        // TODO: we really can derive this from values() through the use of
        //         value -> Map.entry(value.key(), value)
        return iterDelegate().entrySet();
    }

    private @NonNull Map<K, V> lookupDelegate() {
        final Map<K, V> local;
        return (local = (Map<K, V>) DELEGATE.getAcquire(this)) != null ? local : loadKeyDelegate();
    }

    private @NonNull Map<K, V> iterDelegate() {
        final Map<K, V> local;
        return (local = (Map<K, V>) DELEGATE.getAcquire(this)) != null ? local : loadMassDelegate();
    }

    @SuppressWarnings("unchecked")
    private @NonNull Map<K, V> loadKeyDelegate() {
        final Map<K, V> created = createKeyDelegate();
        final Object witness;
        return (witness = DELEGATE.compareAndExchangeRelease(this, null, created)) == null ? created
                : (Map<K, V>) witness;
    }

    @SuppressWarnings("unchecked")
    private @NonNull Map<K, V> loadMassDelegate() {
        final Map<K, V> created = createMassDelegate();
        final Object witness;
        return (witness = DELEGATE.compareAndExchangeRelease(this, null, created)) == null ? created
                : (Map<K, V>) witness;
    }

    private @NonNull Map<K, V> createKeyDelegate() {
        // FIXME: MDSAL-539: add a specialized strategy based on mapNode.getChild()
        return createDelegate();
    }

    private @NonNull Map<K, V> createMassDelegate() {
        // FIXME: MDSAL-539: add a specialized strategy based on mapNode.getValue()
        return createDelegate();
    }

    private @NonNull Map<K, V> createDelegate() {
        final Builder<K, V> builder = ImmutableMap.builderWithExpectedSize(mapNode.size());
        for (MapEntryNode node : mapNode.getValue()) {
            final V entry = codec.fromMapEntry(node);
            builder.put(entry.key(), entry);
        }
        return builder.build();
    }

    private static UnsupportedOperationException uoe() {
        return new UnsupportedOperationException("Modification is not supported");
    }
}
