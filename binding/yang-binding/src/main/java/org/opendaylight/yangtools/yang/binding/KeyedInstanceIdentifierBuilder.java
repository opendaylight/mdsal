/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.IdentifiableItem;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;

public class KeyedInstanceIdentifierBuilder<T extends DataObject & Identifiable<K>,
        K extends Identifier<T>> extends AbstractInstanceIdentifierBuilder<T> {

    @Override
    boolean wildcarded() {
        return false;
    }

    KeyedInstanceIdentifierBuilder() {
        super();
    }

    KeyedInstanceIdentifierBuilder(final PathArgument item, final Iterable<? extends PathArgument> pathArguments,
            final int hash) {
        super(item, pathArguments, hash);
    }

    KeyedInstanceIdentifierBuilder(final PathArgument item, final Iterable<? extends PathArgument> pathArguments,
            final int hash, final ImmutableList.@Nullable Builder<PathArgument> pathBuilder) {
        super(item, pathArguments, hash, pathBuilder);
    }

    @Override
    public @NonNull <N extends ChildOf<? super T>> InstanceIdentifierBuilderImpl<N> child(
            Class<N> container) {
        Preconditions.checkArgument(!Identifiable.class.isAssignableFrom(container),
                "Must not be Identifiable. Use wildcardChild() method instead");
        addNode(Item.of(container));
        return getNormalBuilder();
    }

    @Override
    public @NonNull <C extends ChoiceIn<? super T> & DataObject, N extends ChildOf<? super C>>
            InstanceIdentifierBuilderImpl<N> child(Class<C> caze, Class<N> container) {
        Preconditions.checkArgument(!Identifiable.class.isAssignableFrom(container),
                "Must not be Identifiable. Use wildcardChild() method instead");
        addNode(Item.of(container));
        return getNormalBuilder();
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull <N extends Identifiable<K> & ChildOf<? super T>, K extends Identifier<N>>
            KeyedInstanceIdentifierBuilder<N,K> child(Class<@NonNull N> listItem, K listKey) {
        addNode(IdentifiableItem.of(listItem, listKey));
        return (KeyedInstanceIdentifierBuilder<N, K>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull <C extends ChoiceIn<? super T> & DataObject, K extends Identifier<N>,
            N extends Identifiable<K> & ChildOf<? super C>> KeyedInstanceIdentifierBuilder<N,K> child(Class<C> caze,
            Class<N> listItem, K listKey) {
        addNode(IdentifiableItem.of(caze, listItem, listKey));
        return (KeyedInstanceIdentifierBuilder<N, K>) this;
    }

    @Override
    public <N extends ChildOf<? super T> & Identifiable<?>> WildcardedInstanceIdentifierBuilder<N> wildcardChild(
            Class<N> container) {
        addNode(Item.of(container));
        return getWildcardedBuilder();
    }

    @Override
    public @NonNull <C extends ChoiceIn<? super T> & DataObject & Identifiable<?>, N extends ChildOf<? super C>>
            WildcardedInstanceIdentifierBuilder<N> wildcardChild(Class<C> caze, Class<N> container) {
        addNode(Item.of(caze, container));
        return getWildcardedBuilder();
    }

    @Override
    public @NonNull <N extends DataObject & Augmentation<? super T>> InstanceIdentifierBuilder<N> augmentation(
            Class<N> container) {
        Preconditions.checkArgument(!Identifiable.class.isAssignableFrom(container),
                "Must not be Identifiable. Use wildcardChild() method instead");
        addNode(Item.of(container));
        return getNormalBuilder();
    }

    @Override
    public @NonNull <N extends DataObject & Augmentation<? super T> & Identifiable<?>>
            WildcardedInstanceIdentifierBuilder<N> wildcardAugmentation(Class<N> container) {
        addNode(Item.of(container));
        return getWildcardedBuilder();
    }

}
