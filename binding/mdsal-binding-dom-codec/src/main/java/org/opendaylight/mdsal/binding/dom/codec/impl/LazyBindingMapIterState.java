/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.codec.impl.KeyedListNodeCodecContext.Unordered;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class LazyBindingMapIterState<K extends Identifier<V>, V extends DataObject & Identifiable<K>>
        extends LazyBindingMap.State<K, V> {
    private static final Logger LOG = LoggerFactory.getLogger(LazyBindingMapIterState.class);
    private static final VarHandle ENTRY_SET;
    private static final VarHandle KEY_SET;
    private static final VarHandle LOOKUP_MAP;

    static {
        final Lookup lookup = MethodHandles.lookup();
        try {
            ENTRY_SET = lookup.findVarHandle(LazyBindingMapIterState.class, "entrySet", EntrySet.class);
            KEY_SET = lookup.findVarHandle(LazyBindingMapIterState.class, "entrySet", EntrySet.class);
            LOOKUP_MAP = lookup.findVarHandle(LazyBindingMapIterState.class, "lookupMap", EntrySet.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    // Primary storage of transformed nodes. Other views are derived from this.
    private final @NonNull Values<K, V> values;

    // Secondary views derived from values
    private volatile KeySet<K, V> keySet;
    private volatile EntrySet<K, V> entrySet;

    // Lookup map, instantiated on demand
    private volatile ImmutableMap<K, V> lookupMap;

    LazyBindingMapIterState(final Unordered<K, V> codec, final MapNode mapNode) {
        values = new Values<>(codec, mapNode);
    }

    @Override
    boolean containsKey(final Object key) {
        return lookupMap().containsKey(key);
    }

    @Override
    V get(final Object key) {
        return lookupMap().get(key);
    }

    @Override
    Values<K, V> values() {
        return values;
    }

    @Override
    EntrySet<K, V> entrySet() {
        final EntrySet<K, V> ret;
        return (ret = (EntrySet<K, V>) ENTRY_SET.getAcquire(this)) != null ? ret : loadEntrySet();
    }

    @Override
    KeySet<K, V> keySet() {
        final KeySet<K, V> ret;
        return (ret = (KeySet<K, V>) KEY_SET.getAcquire(this)) != null ? ret : loadKeySet();
    }

    private @NonNull ImmutableMap<K, V> lookupMap() {
        final ImmutableMap<K, V> ret;
        return (ret = (ImmutableMap<K, V>) LOOKUP_MAP.getAcquire(this)) != null ? ret : loadLookupMap();
    }

    // TODO: this is not exactly efficient, as we are forcing full materialization. We also take a lock here, as we
    //       do not want multiple threads indexing the same thing
    private synchronized @NonNull ImmutableMap<K, V> loadLookupMap() {
        ImmutableMap<K, V> ret = (ImmutableMap<K, V>) LOOKUP_MAP.getAcquire(this);
        if (ret == null) {
            lookupMap = ret = ImmutableMap.copyOf(entrySet());
            if (LOG.isTraceEnabled()) {
                LOG.trace("Inefficient instantiation of lookup secondary", new Throwable());
            }
        }
        return ret;
    }

    private @NonNull EntrySet<K, V> loadEntrySet() {
        final EntrySet<K, V> created = new EntrySet<>(values);
        final Object witness;
        return (witness = ENTRY_SET.compareAndExchangeRelease(this, null, created)) == null ? created
                : (EntrySet<K, V>) witness;
    }

    private @NonNull KeySet<K, V> loadKeySet() {
        final KeySet<K, V> created = new KeySet<>(values);
        final Object witness;
        return (witness = KEY_SET.compareAndExchangeRelease(this, null, created)) == null ? created
                : (KeySet<K, V>) witness;
    }

    // TODO: improve performance of various methods by circling back to parent LazyBindingMap
    private static final class EntrySet<K extends Identifier<V>, V extends DataObject & Identifiable<K>>
            extends AbstractSet<Entry<K, V>> {
        private final Values<K, V> values;

        EntrySet(final Values<K, V> values) {
            this.values = requireNonNull(values);
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return Iterators.transform(values.iterator(), value -> Map.entry(value.key(), value));
        }

        @Override
        public int size() {
            return values.size();
        }
    }

    // TODO: improve performance of various methods by circling back to parent LazyBindingMap
    private static final class KeySet<K extends Identifier<V>, V extends DataObject & Identifiable<K>>
            extends AbstractSet<K> {
        private final Values<K, V> values;

        KeySet(final Values<K, V> values) {
            this.values = requireNonNull(values);
        }

        @Override
        public Iterator<K> iterator() {
            return Iterators.transform(values.iterator(), Identifiable::key);
        }

        @Override
        public int size() {
            return values.size();
        }
    }

    /*
     * Lazily-populated translation of DOM values to binding values. This class is not completely lazy, as we allocate
     * the array to hold all values upfront and populate it with MapEntry nodes.
     */
    // TODO: improve performance of various methods by circling back to parent LazyBindingMap
    private static final class Values<K extends Identifier<V>, V extends DataObject & Identifiable<K>>
            extends AbstractSet<V> {
        private static final VarHandle OBJECTS = MethodHandles.arrayElementVarHandle(Object[].class);

        private final Unordered<K, V> codec;
        private final Object[] objects;

        @SuppressWarnings("unchecked")
        Values(final Unordered<K, V> codec, final MapNode mapNode) {
            this.codec = requireNonNull(codec);
            objects = mapNode.getValue().toArray();
        }

        @Override
        public Iterator<V> iterator() {
            return new AbstractIterator<>() {
                private int nextOffset;

                @Override
                protected V computeNext() {
                    return nextOffset < objects.length ? objectAt(nextOffset++) : endOfData();
                }
            };
        }

        @Override
        public int size() {
            return objects.length;
        }

        private @NonNull V objectAt(final int offset) {
            final Object obj = OBJECTS.getAcquire(objects, offset);
            return obj instanceof MapEntryNode ? loadObjectAt(offset, (MapEntryNode) obj) : (V) obj;
        }

        private @NonNull V loadObjectAt(final int offset, final MapEntryNode obj) {
            final V ret = codec.fromMapEntry(obj);
            final Object witness;
            return (witness = OBJECTS.compareAndExchangeRelease(objects, offset, obj, ret)) == obj ? ret : (V) witness;
        }
    }
}
