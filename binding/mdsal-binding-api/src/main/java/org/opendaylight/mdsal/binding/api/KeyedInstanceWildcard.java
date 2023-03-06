/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import java.io.Serial;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.InstanceIdentifier.IdentifiableItem;
import org.opendaylight.mdsal.binding.api.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.ChoiceIn;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;

/**
 * An {@link InstanceIdentifier}, which has a list key attached at its last path element.
 *
 * @param <T> Target data type
 * @param <K> Target key type
 */
public class KeyedInstanceWildcard<T extends Identifiable<K> & DataObject, K extends Identifier<T>>
        extends InstanceWildcard<T> {
    @Serial
    private static final long serialVersionUID = 2L;

    private final K key;

    KeyedInstanceWildcard(final Class<@NonNull T> type, final Iterable<PathArgument> pathArguments, final int hash,
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
    public final @NonNull KeyedWildcardBuilderImpl<T,K> builder() {
        return new KeyedWildcardBuilderImpl<>(IdentifiableItem.of(getTargetType(), key), pathArguments,
                hashCode());
    }

    @Override
    protected boolean fastNonEqual(final InstanceWildcard<?> other) {
        final KeyedInstanceWildcard<?, ?> kii = (KeyedInstanceWildcard<?, ?>) other;

        /*
         * We could do an equals() here, but that may actually be expensive.
         * equals() in superclass falls back to a full compare, which will
         * end up running that equals anyway, so do not bother here.
         */
        return key == null != (kii.key == null);
    }

    public interface KeyedWildcardBuilder<T extends DataObject & Identifiable<K>, K extends Identifier<T>> {
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
        <N extends ChildOf<? super T>> @NonNull WildcardBuilder<N> child(Class<N> container);

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
            @NonNull WildcardBuilder<N> child(Class<C> caze, Class<N> container);

        /**
         * Append the specified listItem as a child of the current InstanceIdentifier referenced by the builder. This
         * method should be used when you want to build an instance identifier by appending a specific list element to
         * the identifier.
         *
         * @param listItem List to append
         * @param listKey List key
         * @param <N> List type
         * @param <K> Key type
         * @return this builder
         * @throws NullPointerException if any argument is null
         */
        <N extends Identifiable<K> & ChildOf<? super T>, K extends Identifier<N>>
            @NonNull KeyedWildcardBuilder<N,K> child(Class<@NonNull N> listItem, K listKey);

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
         * @param <K> Key type
         * @return this builder
         * @throws NullPointerException if any argument is null
         */
        <C extends ChoiceIn<? super T> & DataObject, K extends Identifier<N>,
                N extends Identifiable<K> & ChildOf<? super C>> @NonNull KeyedWildcardBuilder<N,K> child(
                Class<C> caze, Class<N> listItem, K listKey);

        /**
         * Build an identifier which refers to a specific augmentation of the current InstanceIdentifier referenced by
         * the builder.
         *
         * @param container augmentation class
         * @param <N> augmentation type
         * @return this builder
         * @throws NullPointerException if {@code container} is null
         */
        <N extends DataObject & Augmentation<? super T>> @NonNull WildcardBuilder<N> augmentation(
                Class<N> container);

        /**
         * Build the instance identifier.
         *
         * @return Resulting instance identifier.
         */
        @NonNull KeyedInstanceWildcard<T,K> build();
    }
}
