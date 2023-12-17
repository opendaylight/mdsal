/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Support for indexing a set of non-null objects and efficiently look them up. This essentially boils down to:
 * <ul>
 *   <li>{@link #index(Stream)}ing, which results in an opaque non-null state</li>
 *   <li>{@link #lookup(Object, Comparable)}ing up an object in provided state</li>
 *   <li>turning provided state {@link #toList(Class, Object)} of objects of expected type</li>
 * </ul>
 */
abstract class ArrayIndex<K, V> {
    private final Class<V[]> arrayType;
    private final Class<V> valueType;

    ArrayIndex(final Class<V> valueType, final Class<V[]> arrayType) {
        this.valueType = requireNonNull(valueType);
        this.arrayType = requireNonNull(arrayType);
    }

    final @NonNull Object index(final Stream<? extends V> values) {
        final var tmp = values
            .map(Objects::requireNonNull)
            .toArray(length -> length == 0 ? emptyArray() : newArray(length));
        return switch (tmp.length) {
            case 0 -> tmp;
            case 1 -> requireNonNull(tmp[0]);
            default -> {
                Arrays.sort(tmp, (o1, o2) -> {
                    final int cmp = compareValues(o1, o2);
                    if (cmp == 0) {
                        throw new IllegalArgumentException("Type " + o1 + " conflicts with " + o2);
                    }
                    return cmp;
                });

                yield tmp;
            }
        };
    }

    final @Nullable V lookup(final @NonNull Object values, final K key) {
        final var k = requireNonNull(key);
        if (arrayType.isInstance(values)) {
            return lookup(arrayType.cast(values), k);
        }
        final var value = requireNonNull(valueType.cast(values));
        return compareValue(value, k) == 0 ? value : null;
    }

    private @Nullable V lookup(final V[] array, final K key) {
        final var k = requireNonNull(key);
        if (array.length == 0) {
            return null;
        }
        // Here we are assuming that Arrays.binarySearch() accepts a null object, so as to help CHA by not introducing
        // a fake RuntimeType class and the corresponding instanceof checks to side-step the statement lookup (which
        // would need more faking).
        // We make a slight assumption that o2 is the what we specify as a key, but can recover if it is the other way
        // around.
        final var offset = Arrays.binarySearch(array, null, (o1, o2) -> compareValue(requireNonNullElse(o1, o2), k));
        return offset < 0 ? null : array[offset];
    }

    final <T extends V> @NonNull List<T> toList(final Class<T> type, final @NonNull Object values) {
        return arrayType.isInstance(values) ? toList(type, arrayType.cast(values))
            : List.of(type.cast(values));
    }

    // Unchecked cast is a false positive: we check all items
    @SuppressWarnings("unchecked")
    private <T extends V> @NonNull List<T> toList(final Class<T> type, final V[] array) {
        if (array.length == 0) {
            return List.of();
        }
        for (var item : array) {
            type.cast(requireNonNull(item));
        }
        return (List<T>) Collections.unmodifiableList(Arrays.asList(array));
    }

    abstract V[] emptyArray();

    abstract V[] newArray(int length);

    abstract int compareValue(@NonNull V obj, @NonNull K key);

    abstract int compareValues(@NonNull V o1, @NonNull V o2);
}
