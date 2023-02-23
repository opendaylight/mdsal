/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import java.io.ObjectStreamException;
import java.io.Serial;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;

/**
 * An {@link InstanceIdentifier}, which has a list key attached at its last path element.
 *
 * @param <T> Target data type
 * @param <K> Target key type
 */
public class KeyedInstanceIdentifier<T extends Identifiable<K> & DataObject, K extends Identifier<T>>
        extends InstanceIdentifier<T> {
    @Serial
    private static final long serialVersionUID = 2L;

    private final K key;

    KeyedInstanceIdentifier(final Class<@NonNull T> type, final Iterable<PathArgument> pathArguments,
            final boolean wildcarded, final int hash, final K key) {
        super(type, pathArguments, wildcarded, hash);
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
    public final InstanceIdentifierBuilder<T> builder() {
        return new InstanceIdentifierBuilderImpl<>(IdentifiableItem.of(getTargetType(), key), pathArguments,
                hashCode(), isWildcarded());
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

    @Serial
    private Object writeReplace() throws ObjectStreamException {
        return new KeyedInstanceIdentifierV2<>(this);
    }

    public interface KeyedInstanceIdentifierBuilder<T extends DataObject & Identifiable<K>, K extends Identifier<T>> {
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
         * @param <K> Key type
         * @return this builder
         * @throws NullPointerException if any argument is null
         */
        <N extends Identifiable<K> & ChildOf<? super T>, K extends Identifier<N>>
            @NonNull KeyedInstanceIdentifierBuilder<N,K> child(Class<@NonNull N> listItem, K listKey);

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
                N extends Identifiable<K> & ChildOf<? super C>> @NonNull KeyedInstanceIdentifierBuilder<N,K> child(
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
        <N extends DataObject & Augmentation<? super T>> @NonNull InstanceIdentifierBuilder<N> augmentation(
                Class<N> container);

        /**
         * Build the instance identifier.
         *
         * @return Resulting instance identifier.
         */
        @NonNull InstanceIdentifier<T> build();
    }
}
