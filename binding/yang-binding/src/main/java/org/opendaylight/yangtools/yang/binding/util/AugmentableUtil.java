/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.util;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;

/**
 * Utilities for interrogating {@link Augmentable} objects.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public final class AugmentableUtil {

    /**
     * Stream augmentations available in target {@code augmentable}. If a particular augmentation type is present
     * in {@code types}, it is reported multiple times.
     *
     * @param augmentable Target augmentable
     * @param types Requested augmentations
     * @return A {@link Stream} of Class/instance entries. Entries can have null value, which indicates specified
     *         augmentation is not present.
     * @throws NullPointerException if any of the arguments is null
     */
    @SafeVarargs
    public static <T extends Augmentable<T>, A extends Augmentation<T>> Stream<Entry<Class<? extends A>, @Nullable A>>
            streamAugmentations(final T augmentable, final Class<? extends A>... types) {
        requireNonNull(augmentable);
        return Arrays.stream(types).map(type -> new SimpleImmutableEntry<>(type, augmentable.augmentation(type)));
    }

    /**
     * Stream augmentations available in target {@code augmentable}. If a particular augmentation type is present
     * in {@code types}, it is reported multiple times.
     *
     * @param augmentable Target augmentable
     * @param types Requested augmentations
     * @return A {@link Stream} of Class/instance entries. Entries can have null value, which indicates specified
     *         augmentation is not present.
     * @throws NullPointerException if any of the arguments is null
     */
    public static <T extends Augmentable<T>, A extends Augmentation<T>> Stream<Entry<Class<? extends A>, @Nullable A>>
            streamAugmentations(final T augmentable, final Collection<Class<? extends A>> types) {
        requireNonNull(augmentable);
        return types.stream().map(type -> new SimpleImmutableEntry<>(type, augmentable.augmentation(type)));
    }

    @SafeVarargs
    public static <T extends Augmentable<T>, A extends Augmentation<T>> Map<Class<? extends A>, A> indexAugmentations(
            final T augmentable, final Class<? extends A>... types) {
        requireNonNull(augmentable);
        final Builder<Class<? extends A>, A> b = ImmutableMap.builderWithExpectedSize(types.length);
        for (Class<? extends A> type : types) {
            final @Nullable A aug = augmentable.augmentation(type);
            if (aug != null) {
                b.put(type, aug);
            }
        }
        return b.build();
    }

    public static <T extends Augmentable<T>, A extends Augmentation<T>> Map<Class<? extends A>, A> indexAugmentations(
            final T augmentable, final Collection<Class<? extends A>> types) {
        requireNonNull(augmentable);
        final Builder<Class<? extends A>, A> b = ImmutableMap.builderWithExpectedSize(types.size());
        for (Class<? extends A> type : types) {
            final @Nullable A aug = augmentable.augmentation(type);
            if (aug != null) {
                b.put(type, aug);
            }
        }
        return b.build();
    }
}
