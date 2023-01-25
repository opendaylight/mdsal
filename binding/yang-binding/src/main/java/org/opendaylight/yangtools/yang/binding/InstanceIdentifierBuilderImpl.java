/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.IdentifiableItem;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier.KeyedInstanceIdentifierBuilder;

class InstanceIdentifierBuilderImpl<T extends DataObject> extends AbstractInstanceIdentifierBuilder<T>
    implements InstanceIdentifierBuilder<T> {

    @Override
    boolean wildcarded() {
        return false;
    }

    InstanceIdentifierBuilderImpl() {
        super();
    }

    InstanceIdentifierBuilderImpl(final PathArgument item, final Iterable<? extends PathArgument> pathArguments,
            final int hash) {
        super(item, pathArguments, hash);
    }

    InstanceIdentifierBuilderImpl(final PathArgument item, final Iterable<? extends PathArgument> pathArguments,
            final int hash, final ImmutableList.@Nullable Builder<PathArgument> pathBuilder) {
        super(item, pathArguments, hash, pathBuilder);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <N extends ChildOf<? super T>> @NonNull InstanceIdentifierBuilderImpl<N> child(final Class<N> container) {
        Preconditions.checkArgument(!Identifiable.class.isAssignableFrom(container),
                "Must not be Identifiable. Use wildcardChild() method instead");
        addNode(Item.of(container));
        return (InstanceIdentifierBuilderImpl<N>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C extends ChoiceIn<? super T> & DataObject, N extends ChildOf<? super C>> InstanceIdentifierBuilderImpl<N>
            child(final Class<C> caze, final Class<N> container) {
        Preconditions.checkArgument(!Identifiable.class.isAssignableFrom(container),
                "Must not be Identifiable. Use wildcardChild() method instead");
        addNode(Item.of(caze, container));
        return (InstanceIdentifierBuilderImpl<N>) this;
    }

    @Override
    public <N extends Identifiable<K> & ChildOf<? super T>, K extends Identifier<N>> KeyedInstanceIdentifierBuilder<N,K>
            child(final Class<@NonNull N> listItem, final K listKey) {
        addNode(IdentifiableItem.of(listItem, listKey));
        return getKeyedBuilder();
    }

    @Override
    public <C extends ChoiceIn<? super T> & DataObject, K extends Identifier<N>,
        N extends Identifiable<K> & ChildOf<? super C>> @NonNull KeyedInstanceIdentifierBuilderImpl<N,K> child(
                final Class<C> caze, final Class<N> listItem, final K listKey) {
        addNode(IdentifiableItem.of(caze, listItem, listKey));
        return getKeyedBuilder();
    }

    @Override
    public <N extends ChildOf<? super T> & Identifiable<?>> WildcardedInstanceIdentifierBuilderImpl<N> wildcardChild(
            final Class<N> container) {
        addNode(Item.of(container));
        return getWildcardedBuilder();
    }

    @Override
    public @NonNull <C extends ChoiceIn<? super T> & DataObject & Identifiable<?>, N extends ChildOf<? super C>>
    WildcardedInstanceIdentifierBuilderImpl<N> wildcardChild(Class<C> caze, Class<N> container) {
        addNode(Item.of(caze, container));
        return getWildcardedBuilder();
    }

    /**
     * Build an identifier which refers to a specific augmentation of the current InstanceIdentifier referenced by
     * the builder.
     *
     * @param container Augmentation to be added
     * @param <N> Augmentation type
     * @return This builder
     */
    @SuppressWarnings("unchecked")
    @Override
    public <N extends DataObject & Augmentation<? super T>> @NonNull InstanceIdentifierBuilderImpl<N> augmentation(
            final Class<N> container) {
        Preconditions.checkArgument(!Identifiable.class.isAssignableFrom(container),
                "Must not be Identifiable. Use wildcardAugmentation() method instead");
        addNode(Item.of(container));
        return (InstanceIdentifierBuilderImpl<N>) this;
    }

    @Override
    public <N extends DataObject & Augmentation<? super T> & Identifiable<?>> @NonNull
            WildcardedInstanceIdentifierBuilderImpl<N> wildcardAugmentation(Class<N> container) {
        addNode(Item.of(container));
        return getWildcardedBuilder();
    }

    @Override
    public InstanceIdentifier<T> build() {
        Preconditions.checkState(arg != null, "No path arguments present");

        final Iterable<PathArgument> pathArguments;
        if (basePath == null) {
            pathArguments = pathBuilder.build();
        } else {
            pathArguments = Iterables.concat(basePath, pathBuilder.build());
        }

        return InstanceIdentifier.trustedCreate(arg, pathArguments, hashBuilder.build(), wildcarded());
    }
}
