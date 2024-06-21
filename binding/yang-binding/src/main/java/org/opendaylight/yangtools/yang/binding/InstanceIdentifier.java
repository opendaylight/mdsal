/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import com.google.common.annotations.Beta;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.ChildOf;
import org.opendaylight.yangtools.binding.ChoiceIn;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.binding.DataRoot;
import org.opendaylight.yangtools.binding.InstanceIdentifier.Builder;
import org.opendaylight.yangtools.binding.InstanceIdentifier.KeyedBuilder;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.KeyAware;

/**
 * Deprecated bridge towards {@link org.opendaylight.yangtools.binding.InstanceIdentifier}. Users are advised to migrate
 * to the methods exposed there.
 */
@Deprecated(since = "14.0.0", forRemoval = true)
public final class InstanceIdentifier {
    private InstanceIdentifier() {
        // Hidden on purpose
    }

    /**
     * Create a {@link Builder} for a specific type of InstanceIdentifier as specified by container.
     *
     * @param container Base container
     * @param <T> Type of the container
     * @return A new {@link Builder}
     * @throws NullPointerException if {@code container} is null
     */
    public static <T extends ChildOf<? extends DataRoot>> @NonNull Builder<T> builder(final Class<T> container) {
        return org.opendaylight.yangtools.binding.InstanceIdentifier.builder(container);
    }

    /**
     * Create a {@link Builder} for a specific type of InstanceIdentifier as specified by container in
     * a {@code grouping} used in the {@code case} statement.
     *
     * @param caze Choice case class
     * @param container Base container
     * @param <C> Case type
     * @param <T> Type of the container
     * @return A new {@link Builder}
     * @throws NullPointerException if any argument is null
     */
    public static <C extends ChoiceIn<? extends DataRoot> & DataObject, T extends ChildOf<? super C>>
            @NonNull Builder<T> builder(final Class<C> caze, final Class<T> container) {
        return org.opendaylight.yangtools.binding.InstanceIdentifier.builder(caze, container);
    }

    /**
     * Create a {@link Builder} for a specific type of InstanceIdentifier which represents an {@link KeyStep}.
     *
     * @param listItem list item class
     * @param listKey key value
     * @param <N> List type
     * @param <K> List key
     * @return A new {@link Builder}
     * @throws NullPointerException if any argument is null
     */
    public static <N extends KeyAware<K> & ChildOf<? extends DataRoot>,
            K extends Key<N>> @NonNull KeyedBuilder<N, K> builder(final Class<N> listItem, final K listKey) {
        return org.opendaylight.yangtools.binding.InstanceIdentifier.builder(listItem, listKey);
    }

    /**
     * Create a {@link Builder} for a specific type of InstanceIdentifier which represents an {@link KeyStep}
     * in a {@code grouping} used in the {@code case} statement.
     *
     * @param caze Choice case class
     * @param listItem list item class
     * @param listKey key value
     * @param <C> Case type
     * @param <N> List type
     * @param <K> List key
     * @return A new {@link Builder}
     * @throws NullPointerException if any argument is null
     */
    public static <C extends ChoiceIn<? extends DataRoot> & DataObject,
            N extends KeyAware<K> & ChildOf<? super C>, K extends Key<N>>
            @NonNull KeyedBuilder<N, K> builder(final Class<C> caze, final Class<N> listItem, final K listKey) {
        return org.opendaylight.yangtools.binding.InstanceIdentifier.builder(caze, listItem, listKey);
    }

    public static <R extends DataRoot & DataObject, T extends ChildOf<? super R>>
            @NonNull Builder<T> builderOfInherited(final Class<R> root, final Class<T> container) {
        return org.opendaylight.yangtools.binding.InstanceIdentifier.builderOfInherited(root, container);
    }

    public static <R extends DataRoot & DataObject, C extends ChoiceIn<? super R> & DataObject,
            T extends ChildOf<? super C>>
            @NonNull Builder<T> builderOfInherited(final Class<R> root,
                final Class<C> caze, final Class<T> container) {
        return org.opendaylight.yangtools.binding.InstanceIdentifier.builderOfInherited(root, caze, container);
    }

    public static <R extends DataRoot & DataObject, N extends KeyAware<K> & ChildOf<? super R>,
            K extends Key<N>>
            @NonNull KeyedBuilder<N, K> builderOfInherited(final Class<R> root,
                final Class<N> listItem, final K listKey) {
        return org.opendaylight.yangtools.binding.InstanceIdentifier.builderOfInherited(root, listItem, listKey);
    }

    public static <R extends DataRoot & DataObject, C extends ChoiceIn<? super R> & DataObject,
            N extends KeyAware<K> & ChildOf<? super C>, K extends Key<N>>
            @NonNull KeyedBuilder<N, K> builderOfInherited(final Class<R> root,
                final Class<C> caze, final Class<N> listItem, final K listKey) {
        return org.opendaylight.yangtools.binding.InstanceIdentifier.builderOfInherited(root, caze, listItem, listKey);
    }

    @Beta
    public static <T extends DataObject, C extends ChoiceIn<?> & DataObject>
            org.opendaylight.yangtools.binding.@NonNull DataObjectStep<T> createStep(
                final Class<C> caze, final Class<T> type) {
        return org.opendaylight.yangtools.binding.InstanceIdentifier.createStep(caze, type);
    }

    @Beta
    public static <T extends DataObject> org.opendaylight.yangtools.binding.@NonNull DataObjectStep<T> createStep(
            final Class<T> type) {
        return createStep(null, type);
    }

    /**
     * Create an instance identifier for a sequence of {@link DataObjectStep} steps. The steps are required to be formed
     * of classes extending either {@link ChildOf} or {@link Augmentation} contracts. This method does not check whether
     * or not the sequence is structurally sound, for example that an {@link Augmentation} follows an
     * {@link Augmentable} step. Furthermore the compile-time indicated generic type of the returned object does not
     * necessarily match the contained state.
     *
     * <p>
     * Failure to observe precautions to validate the list's contents may yield an object which mey be rejected at
     * run-time or lead to undefined behaviour.
     *
     * @param pathArguments The path to a specific node in the data tree
     * @return InstanceIdentifier instance
     * @throws NullPointerException if {@code pathArguments} is, or contains an item which is, {@code null}
     * @throws IllegalArgumentException if {@code pathArguments} is empty or contains an item which does not represent
     *                                  a valid addressing step.
     */
    public static <T extends DataObject> org.opendaylight.yangtools.binding.@NonNull InstanceIdentifier<T> unsafeOf(
            final List<? extends org.opendaylight.yangtools.binding.DataObjectStep<?>> pathArguments) {
        return org.opendaylight.yangtools.binding.InstanceIdentifier.unsafeOf(pathArguments);
    }

    /**
     * Create an instance identifier for a very specific object type.
     *
     * <p>
     * For example
     * <pre>
     *      new InstanceIdentifier(Nodes.class)
     * </pre>
     * would create an InstanceIdentifier for an object of type Nodes
     *
     * @param type The type of the object which this instance identifier represents
     * @return InstanceIdentifier instance
     */
    public static <T extends ChildOf<? extends DataRoot>>
            org.opendaylight.yangtools.binding.@NonNull InstanceIdentifier<T> create(final Class<@NonNull T> type) {
        return org.opendaylight.yangtools.binding.InstanceIdentifier.create(type);
    }

    /**
     * Return the key associated with the last component of the specified identifier.
     *
     * @param id instance identifier
     * @return key associated with the last component
     * @throws IllegalArgumentException if the supplied identifier type cannot have a key.
     * @throws NullPointerException if id is null.
     */
    public static <N extends KeyAware<K> & DataObject, K extends Key<N>> K keyOf(
            final org.opendaylight.yangtools.binding.InstanceIdentifier<N> id) {
        return  org.opendaylight.yangtools.binding.InstanceIdentifier.keyOf(id);
    }
}
