/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import com.google.common.base.Preconditions;
import java.io.Serial;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;

public class WildcardedInstanceIdentifier<T extends DataObject> extends InstanceIdentifier<T> {

    //FIXME what value to assign
    @Serial
    private static final long serialVersionUID = 2L;

    @Override
    public  boolean isWildcarded() {
        return true;
    }

    WildcardedInstanceIdentifier(Class<T> type, Iterable<PathArgument> pathArguments,
            boolean wildcarded, int hash) {
        super(type, pathArguments, wildcarded, hash);
    }

    @SuppressWarnings("unchecked")
    static <N extends DataObject> @NonNull WildcardedInstanceIdentifier<N> trustedCreate(final PathArgument arg,
            final Iterable<PathArgument> pathArguments, final int hash, boolean wildcarded) {
        Preconditions.checkArgument(wildcarded, "Must be wildcarded.");

        return new WildcardedInstanceIdentifier<>((Class<N>) arg.getType(), pathArguments, true, hash);
    }

    //TODO add dynamic .builder() method returning WildcardedInstanceIdentifierBuilder

    public interface WildcardedInstanceIdentifierBuilder<T extends DataObject> {
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
        <N extends ChildOf<? super T>> @NonNull WildcardedInstanceIdentifierBuilder<N> child(Class<N> container);

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
        @NonNull WildcardedInstanceIdentifierBuilder<N> child(Class<C> caze, Class<N> container);

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
        @NonNull WildcardedInstanceIdentifierBuilder<N> child(Class<@NonNull N> listItem, K listKey);

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
                N extends Identifiable<K> & ChildOf<? super C>> @NonNull WildcardedInstanceIdentifierBuilder<N> child(
                Class<C> caze, Class<N> listItem, K listKey);

        <N extends ChildOf<? super T> & Identifiable<?>> WildcardedInstanceIdentifierBuilder<N> wildcardChild(
                Class<N> container);

        <C extends ChoiceIn<? super T> & DataObject & Identifiable<?>, N extends ChildOf<? super C>>
        @NonNull WildcardedInstanceIdentifierBuilder<N> wildcardChild(Class<C> caze, Class<N> container);

        /**
         * Build an identifier which refers to a specific augmentation of the current InstanceIdentifier referenced by
         * the builder.
         *
         * @param container augmentation class
         * @param <N> augmentation type
         * @return this builder
         * @throws NullPointerException if {@code container} is null
         */
        <N extends DataObject & Augmentation<? super T>> @NonNull WildcardedInstanceIdentifierBuilder<N> augmentation(
                Class<N> container);

        <N extends DataObject & Augmentation<? super T> & Identifiable<?>> @NonNull
                WildcardedInstanceIdentifierBuilder<N> wildcardAugmentation(Class<N> container);

        /**
         * Build the instance identifier.
         *
         * @return Resulting instance identifier.
         */
        @NonNull WildcardedInstanceIdentifier<T> build();
    }
}
