/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import com.google.common.base.Preconditions;
import java.io.ObjectStreamException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.InstanceWildcard.WildcardBuilder;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.ChoiceIn;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Key;
import org.opendaylight.yangtools.yang.binding.KeyAware;

/**
 * An {@link InstanceIdentifier}, which has a list key attached at its last path element.
 *
 * @param <T> Target data type
 * @param <K> Target key type
 */
public class KeyedInstanceIdentifier<T extends KeyAware<K> & DataObject, K extends Key<T>>
        extends InstanceIdentifier<T> {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final K key;

    KeyedInstanceIdentifier(final Class<@NonNull T> type, final Iterable<PathArgument> pathArguments, final int hash,
            final K key) {
        super(type, pathArguments, hash);
        this.key = key;
    }

    /**
     * Return the key attached to this identifier. This method is equivalent to calling
     * {@link InstanceIdentifier#keyOf(InstanceIdentifier)}.
     *
     * @return Key associated with this instance identifier.
     */
    public final K getKey() {
        return key;
    }

    @Override
    public final @NonNull KeyedInstanceIdentifierBuilderImpl<T, K> builder() {
        return new KeyedInstanceIdentifierBuilderImpl<>(IdentifiableItem.of(getTargetType(), key), pathArguments,
                hashCode());
    }

    @Override
    protected boolean fastNonEqual(final InstanceIdentifier<?> other) {
        final KeyedInstanceIdentifier<?, ?> kii = (KeyedInstanceIdentifier<?, ?>) other;

        /*
         * We could do an equals() here, but that may actually be expensive.
         * equals() in superclass falls back to a full compare, which will
         * end up running that equals anyway, so do not bother here.
         */
        return key == null != (kii.key == null);
    }

    @java.io.Serial
    private Object writeReplace() throws ObjectStreamException {
        return new KII1<>(this);
    }

    public static <T extends KeyAware<K> & DataObject, K extends Key<T>> KeyedInstanceIdentifier<T, K> ofLegacy(
            final org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier<T, K> kii) {
        Preconditions.checkArgument(!kii.isWildcarded(), """
                Legacy KeyedInstanceIdentifier must not be wildcarded. Use KeyedInstanceWildcard.ofLegacy() instead.
                """);
        return new KeyedInstanceIdentifier<>(kii.getTargetType(), InstanceIdentifier.ofLegacyPathArguments(
                kii.getPathArguments()), kii.hashCode(), kii.getKey());
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    @Override
    public org.opendaylight.yangtools.yang.binding.@NonNull KeyedInstanceIdentifier<T,K> toLegacy() {
        final org.opendaylight.yangtools.yang.binding.InstanceIdentifier result =
                org.opendaylight.yangtools.yang.binding.InstanceIdentifier.unsafeOf(InstanceIdentifier
                        .toLegacyPathArguments(pathArguments));
        if (result instanceof org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier<?,?> keyedResult) {
            return (org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier<T, K>) keyedResult;
        }
        throw new IllegalArgumentException();
    }

    public interface KeyedInstanceIdentifierBuilder<T extends DataObject & KeyAware<K>, K extends Key<T>> {
        /**
         * Append the specified container as a child of the current InstanceIdentifier referenced by the builder. This
         * method should be used when you want to build an instance identifier by appending top-level elements, for
         * example
         * <pre>
         *     InstanceIdentifier.builder().child(Nodes.class).build();
         * </pre>
         *
         * <p>
         * NOTE :- The above example is only for illustration purposes InstanceIdentifier.builder() has been deprecated
         * and should not be used. Use InstanceIdentifier.builder(Nodes.class) instead
         *
         * @param container Container to append
         * @param <N> Container type
         * @return this builder
         * @throws NullPointerException if {@code container} is null
         */
        <N extends ChildOf<? super T>> @NonNull InstanceIdentifierBuilder<N> child(Class<N> container);

        /**
         * Append the specified container as a child of the current InstanceIdentifier referenced by the builder. This
         * method should be used when you want to build an instance identifier by appending a container node to the
         * identifier and the {@code container} is defined in a {@code grouping} used in a {@code case} statement.
         *
         * @param caze Choice case class
         * @param container Container to append
         * @param <C> Case type
         * @param <N> Container type
         * @return this builder
         * @throws NullPointerException if {@code container} is null
         */
        <C extends ChoiceIn<? super T> & DataObject, N extends ChildOf<? super C>>
            @NonNull InstanceIdentifierBuilder<N> child(Class<C> caze, Class<N> container);

        /**
         * Append the specified listItem as a child of the current InstanceIdentifier referenced by the builder. This
         * method should be used when you want to build an instance identifier by appending a specific list element to
         * the identifier.
         *
         * @param listItem List to append
         * @param listKey List key
         * @param <N> List type
         * @param <L> Key type
         * @return this builder
         * @throws NullPointerException if any argument is null
         */
        <N extends KeyAware<L> & ChildOf<? super T>, L extends Key<N>>
            @NonNull KeyedInstanceIdentifierBuilder<N, L> child(Class<@NonNull N> listItem, L listKey);

        /**
         * Append the specified listItem as a child of the current InstanceIdentifier referenced by the builder. This
         * method should be used when you want to build an instance identifier by appending a specific list element to
         * the identifier and the {@code list} is defined in a {@code grouping} used in a {@code case} statement.
         *
         * @param caze Choice case class
         * @param listItem List to append
         * @param listKey List key
         * @param <C> Case type
         * @param <N> List type
         * @param <L> Key type
         * @return this builder
         * @throws NullPointerException if any argument is null
         */
        <C extends ChoiceIn<? super T> & DataObject, L extends Key<N>, N extends KeyAware<L> & ChildOf<? super C>>
            @NonNull KeyedInstanceIdentifierBuilder<N, L> child(Class<C> caze, Class<N> listItem, L listKey);

        <N extends ChildOf<? super T> & Identifiable<?>> @NonNull WildcardBuilder<N> wildcardChild(
                Class<N> container);

        <C extends ChoiceIn<? super T> & DataObject, N extends ChildOf<? super C> & Identifiable<?>>
            @NonNull WildcardBuilder<N> wildcardChild(Class<C> caze, Class<N> container);

        /**
         * Build an identifier which refers to a specific augmentation of the current InstanceIdentifier referenced by
         * the builder.
         *
         * @param container augmentation class
         * @param <N> augmentation type
         * @return this builder
         * @throws NullPointerException if {@code container} is null
         */
        <N extends DataObject & Augmentation<? super T>> @NonNull InstanceIdentifierBuilder<N> augmentation(
                Class<N> container);


        <N extends DataObject & Augmentation<? super T> & Identifiable<?>> @NonNull
                WildcardBuilder<N> wildcardAugmentation(Class<N> container);

        /**
         * Build the instance identifier.
         *
         * @return Resulting instance identifier.
         */
        @NonNull KeyedInstanceIdentifier<T,K> build();
    }
}
