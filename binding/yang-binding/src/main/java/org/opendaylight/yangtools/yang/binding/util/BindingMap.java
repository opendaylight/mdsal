/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.util;

import com.google.common.annotations.Beta;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;

/**
 * Utility class for instantiating Maps containing {@link Identifiable} values. Unlike normal Map instantiation
 * utilities, methods in this class index values via their identifier, hence providing a more convenient API, amenable
 * to fluent builders.
 *
 * <p>
 * A typical example of use with generated DataObjects looks like this:
 * <pre>
 *   <code>
 *     Foo foo = new FooBuilder()
 *          .setBar(BindingMap.of(
 *              new BarBuilder().setName("one").build(),
 *              new BarBuilder().setName("two").build()))
 *          .build();
 *   </code>
 * </pre>
 *
 * <p>
 * Another alternative is to use builders:
 * <pre>
 *   <code>
 *     Foo foo = new FooBuilder()
 *          .setBar(BindingMap.&#60BarKey, Bar&#62builder()
 *              .add(new BarBuilder().setName("one").build())
 *              .add(new BarBuilder().setName("two").build())
 *              .build())
 *          .build();
 *   </code>
 * </pre>
 *
 * <p>
 * This class allows for two modes of operation:
 * <ul>
 *   <li>Unordered, available through {@link #of()}/{@link #builder()} family of functions. Maps instantiated through
 *       this, preferred, interface will have their iteration order randomized, as explain in Java 9+ unmodifiable
 *       collections.</li>
 *   <li>Ordered, available through {@link #ordered(Identifiable...)}/{@link #orderedBuilder()} family of functions.
 *       Maps instantiated through this interface have a predictable iteration order, as per {@link ImmutableMap}
 *       class design. The use of this interface is generally discouraged, as it may lead to code relying on map
 *       iteration order. Nevertheless it may prove useful where the goal is to have predictable outcomes and hence
 *       is provided for completeness.</li>
 * </ul>
 */
@Beta
public final class BindingMap {
    private BindingMap() {
        // Hidden on purpose
    }

    /**
     * Returns an unmodifiable map containing a single mapping.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param v1 the mapping's value
     * @return a {@code Map} containing the specified value
     * @throws NullPointerException if the value is {@code null}
     */
    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> of(final V v1) {
        return Map.of(v1.key(), v1);
    }

    /**
     * Returns an unmodifiable map containing two mappings.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param v1 the first mapping's value
     * @param v2 the second mapping's value
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if the values contain duplicate keys
     * @throws NullPointerException if any value is {@code null}
     */
    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> of(final V v1, final V v2) {
        return Map.of(v1.key(), v1, v2.key(), v2);
    }

    /**
     * Returns an unmodifiable map containing three mappings.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param v1 the first mapping's value
     * @param v2 the second mapping's value
     * @param v3 the third mapping's value
     * @return a {@code Map} containing the specified mappings
     * @throws IllegalArgumentException if the values contain duplicate keys
     * @throws NullPointerException if any value is {@code null}
     */
    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> of(final V v1, final V v2,
            final V v3) {
        return Map.of(v1.key(), v1, v2.key(), v2, v3.key(), v3);
    }

    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> of(final V v1, final V v2,
            final V v3, final V v4) {
        return Map.of(v1.key(), v1, v2.key(), v2, v3.key(), v3, v4.key(), v4);
    }

    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> of(final V v1, final V v2,
            final V v3, final V v4, final V v5) {
        return Map.of(v1.key(), v1, v2.key(), v2, v3.key(), v3, v4.key(), v4, v5.key(), v5);
    }

    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> of(final V v1, final V v2,
            final V v3, final V v4, final V v5, final V v6) {
        return Map.of(v1.key(), v1, v2.key(), v2, v3.key(), v3, v4.key(), v4, v5.key(), v5, v6.key(), v6);
    }

    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> of(final V v1, final V v2,
            final V v3, final V v4, final V v5, final V v6, final V v7) {
        return Map.of(v1.key(), v1, v2.key(), v2, v3.key(), v3, v4.key(), v4, v5.key(), v5, v6.key(), v6, v7.key(), v7);
    }

    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> of(final V v1, final V v2,
            final V v3, final V v4, final V v5, final V v6, final V v7, final V v8) {
        return Map.of(v1.key(), v1, v2.key(), v2, v3.key(), v3, v4.key(), v4, v5.key(), v5, v6.key(), v6, v7.key(), v7,
            v8.key(), v8);
    }

    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> of(final V v1, final V v2,
            final V v3, final V v4, final V v5, final V v6, final V v7, final V v8, final V v9) {
        return Map.of(v1.key(), v1, v2.key(), v2, v3.key(), v3, v4.key(), v4, v5.key(), v5, v6.key(), v6, v7.key(), v7,
            v8.key(), v8, v9.key(), v9);
    }

    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> of(final V v1, final V v2,
            final V v3, final V v4, final V v5, final V v6, final V v7, final V v8, final V v9, final V v10) {
        return Map.of(v1.key(), v1, v2.key(), v2, v3.key(), v3, v4.key(), v4, v5.key(), v5, v6.key(), v6, v7.key(), v7,
            v8.key(), v8, v9.key(), v9, v10.key(), v10);
    }

    /**
     * Returns an unmodifiable map containing given values.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param values values from which the map is populated
     * @return a {@code Map} containing the specified values
     * @throws IllegalArgumentException if there are any duplicate keys in the provided values
     * @throws NullPointerException if any value is {@code null}, or if the {@code values} array is {@code null}
     */
    @SafeVarargs
    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> of(final V... values) {
        return of(Arrays.asList(values));
    }

    /**
     * Returns an unmodifiable map containing given values.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param values values from which the map is populated
     * @return a {@code Map} containing the specified values
     * @throws IllegalArgumentException if there are any duplicate keys in the provided values
     * @throws NullPointerException if any value is {@code null}, or if the {@code values} array is {@code null}
     */
    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> of(
            final Collection<V> values) {
        return values.stream().collect(toMap());
    }

    public static <K extends Identifier<V>, V extends Identifiable<K>>
            @NonNull Collector<V, ?, ? extends Map<K, V>> toMap() {
        return Collectors.toUnmodifiableMap(Identifiable::key, v -> v);
    }

    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Builder<K, V> builder() {
        return new UnorderedBuilder<>(Builder.DEFAULT_INITIAL_CAPACITY);
    }

    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Builder<K, V> builder(
            final int expectedSize) {
        return new UnorderedBuilder<>(expectedSize);
    }

    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> ordered(final V v1,
            final V v2) {
        return ImmutableMap.of(v1.key(), v1, v2.key(), v2);
    }

    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> ordered(final V v1,
            final V v2, final V v3) {
        return ImmutableMap.of(v1.key(), v1, v2.key(), v2, v3.key(), v3);
    }

    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> ordered(final V v1,
            final V v2, final V v3, final V v4) {
        return ImmutableMap.of(v1.key(), v1, v2.key(), v2, v3.key(), v3, v4.key(), v4);
    }

    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> ordered(final V v1,
            final V v2, final V v3, final V v4, final V v5) {
        return ImmutableMap.of(v1.key(), v1, v2.key(), v2, v3.key(), v3, v4.key(), v4, v5.key(), v5);
    }

    /**
     * Returns an unmodifiable map containing given values. Resulting {@code Map} will retain the iteration order
     * values.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param values values from which the map is populated
     * @return a {@code Map} containing the specified values
     * @throws IllegalArgumentException if there are any duplicate keys in the provided values
     * @throws NullPointerException if any value is {@code null}, or if the {@code values} array is {@code null}
     */
    @SafeVarargs
    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> ordered(final V... values) {
        return ordered(Arrays.asList(values));
    }

    /**
     * Returns an unmodifiable map containing given values. Resulting {@code Map} will retain the iteration order
     * values.
     *
     * @param <K> the {@code Map}'s key type
     * @param <V> the {@code Map}'s value type
     * @param values values from which the map is populated
     * @return a {@code Map} containing the specified values
     * @throws IllegalArgumentException if there are any duplicate keys in the provided values
     * @throws NullPointerException if any value is {@code null}, or if the {@code values} array is {@code null}
     */
    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Map<K, V> ordered(
            final Collection<V> values) {
        return values.stream().collect(toOrderedMap());
    }

    public static <K extends Identifier<V>, V extends Identifiable<K>>
            @NonNull Collector<V, ?, ? extends Map<K, V>> toOrderedMap() {
        return ImmutableMap.<V, K, V>toImmutableMap(Identifiable::key, v -> v);
    }

    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Builder<K, V> orderedBuilder() {
        return new OrderedBuilder<>(Builder.DEFAULT_INITIAL_CAPACITY);
    }

    public static <K extends Identifier<V>, V extends Identifiable<K>> @NonNull Builder<K, V> orderedBuilder(
            final int expectedSize) {
        return new OrderedBuilder<>(expectedSize);
    }

    public abstract static class Builder<K extends Identifier<V>, V extends Identifiable<K>>
            implements org.opendaylight.yangtools.concepts.Builder<Map<K, V>> {
        static final int DEFAULT_INITIAL_CAPACITY = 4;

        Builder() {
            // Hidden on purpose
        }

        public final @NonNull Builder<K, V> add(final V value) {
            addEntry(value.key(), value);
            return this;
        }

        @SafeVarargs
        public final @NonNull Builder<K, V> addAll(final V... values) {
            return addAll(Arrays.asList(values));
        }

        public final @NonNull Builder<K, V> addAll(final Collection<V> values) {
            addEntries(Collections2.transform(values, value -> Map.entry(value.key(), value)));
            return this;
        }

        abstract void addEntry(K key, V value);

        abstract void addEntries(Collection<Entry<K, V>> entries);
    }

    private static final class OrderedBuilder<K extends Identifier<V>, V extends Identifiable<K>>
            extends Builder<K, V> {
        private final ImmutableMap.Builder<K, V> delegate;

        OrderedBuilder(final int expectedSize) {
            delegate = ImmutableMap.builderWithExpectedSize(expectedSize);
        }

        @Override
        public Map<K, V> build() {
            return delegate.build();
        }

        @Override
        void addEntry(final K key, final V value) {
            delegate.put(key, value);
        }

        @Override
        void addEntries(final Collection<Entry<K, V>> entries) {
            delegate.putAll(entries);
        }
    }

    private static final class UnorderedBuilder<K extends Identifier<V>, V extends Identifiable<K>>
            extends Builder<K, V> {
        private final ArrayList<Entry<K, V>> buffer;

        UnorderedBuilder(final int expectedSize) {
            buffer = new ArrayList<>(expectedSize);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Map<K, V> build() {
            return Map.ofEntries(buffer.toArray(new Entry[0]));
        }

        @Override
        void addEntry(final K key, final V value) {
            buffer.add(Map.entry(key, value));
        }

        @Override
        void addEntries(final Collection<Entry<K, V>> entries) {
            buffer.addAll(entries);
        }
    }
}
