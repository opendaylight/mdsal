/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.InstanceIdentifier.IdentifiableItem;
import org.opendaylight.mdsal.binding.api.InstanceIdentifier.Item;
import org.opendaylight.mdsal.binding.api.InstanceIdentifier.PathArgument;
import org.opendaylight.mdsal.binding.api.InstanceWildcard.WildcardBuilder;
import org.opendaylight.mdsal.binding.api.KeyedInstanceWildcard.KeyedWildcardBuilder;
import org.opendaylight.yangtools.util.HashCodeBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.ChoiceIn;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;

class WildcardBuilderImpl<T extends DataObject> implements WildcardBuilder<T> {

    protected final ImmutableList.Builder<PathArgument> pathBuilder;
    protected final HashCodeBuilder<PathArgument> hashBuilder;
    protected final Iterable<? extends PathArgument> basePath;
    protected PathArgument arg;

    WildcardBuilderImpl(final PathArgument item, final Iterable<? extends PathArgument> pathArguments,
            final int hash) {
        this.hashBuilder = new HashCodeBuilder<>(hash);
        this.basePath = pathArguments;
        this.pathBuilder = ImmutableList.builder();
        this.arg = item;
    }

    WildcardBuilderImpl(final PathArgument item, final Iterable<? extends PathArgument> pathArguments,
            final ImmutableList.Builder<PathArgument> pathBuilder, final int hash) {
        this.hashBuilder = new HashCodeBuilder<>(hash);
        this.basePath = pathArguments;
        this.pathBuilder = pathBuilder;
        this.arg = item;
    }

    @Override
    public int hashCode() {
        return hashBuilder.build();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof WildcardBuilderImpl) {
            @SuppressWarnings("unchecked")
            final WildcardBuilderImpl<T> otherBuilder = (WildcardBuilderImpl<T>) obj;
            return Objects.equals(basePath, otherBuilder.basePath)
                    && Objects.equals(arg, otherBuilder.arg)
                    && Objects.equals(hashBuilder.build(), otherBuilder.hashBuilder.build());
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    protected <N extends DataObject> WildcardBuilderImpl<N> getWildcardBuilder() {
        return (WildcardBuilderImpl<N>) this;
    }

    protected <N extends DataObject & Identifiable<K>, K extends Identifier<N>>
        KeyedWildcardBuilderImpl<N, K> getKeyedWildcardBuilder(K key) {
        return new KeyedWildcardBuilderImpl<>(arg, basePath, pathBuilder, hashCode());
    }

    @Override
    public <N extends ChildOf<? super T>> @NonNull WildcardBuilder<N> child(final Class<N> container) {
        return addNode(container);
    }

    @Override
    public <C extends ChoiceIn<? super T> & DataObject, N extends ChildOf<? super C>> @NonNull
            WildcardBuilder<N> child(final Class<C> caze, final Class<N> container) {
        return addNode(Item.of(caze, container));
    }

    @Override
    public <N extends Identifiable<K> & ChildOf<? super T>, K extends Identifier<N>> @NonNull
            KeyedWildcardBuilder<N,K> child(final Class<@NonNull N> listItem, final K listKey) {
        return addNode(IdentifiableItem.of(listItem, listKey));
    }

    @Override
    public <C extends ChoiceIn<? super T> & DataObject, K extends Identifier<N>,
        N extends Identifiable<K> & ChildOf<? super C>> @NonNull KeyedWildcardBuilder<N,K>
        child(final Class<C> caze, final Class<N> listItem, final K listKey) {
        return addNode(IdentifiableItem.of(caze, listItem, listKey));
    }

    /**
     * Build an identifier which refers to a specific augmentation of the current InstanceIdentifier referenced by
     * the builder.
     *
     * @param container Augmentation to be added
     * @param <N> Augmentation type
     * @return This builder
     */
    @Override
    public <N extends DataObject & Augmentation<? super T>> @NonNull WildcardBuilder<N> augmentation(
            final Class<N> container) {
        return addNode(container);
    }

    <N extends DataObject> @NonNull WildcardBuilderImpl<N> addNode(final Item<?> newArg) {
        addNodeInternal(newArg);
        return getWildcardBuilder();
    }

    @SuppressWarnings("unchecked")
    <N extends DataObject & Identifiable<K>, K extends Identifier<N>> @NonNull KeyedWildcardBuilderImpl<N,K>
        addNode(final IdentifiableItem<?,?> newArg) {
        addNodeInternal(newArg);
        return getKeyedWildcardBuilder((K)newArg.getKey());
    }

    protected  <N extends DataObject> @NonNull WildcardBuilderImpl<N> addNode(final Class<N> container) {
        return addNode(Item.of(container));
    }

    protected void addNodeInternal(final PathArgument newArg) {
        arg = newArg;
        hashBuilder.addArgument(newArg);
        pathBuilder.add(newArg);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull InstanceWildcard<T> build() {
        Preconditions.checkState(arg != null, "No path arguments present");

        final Iterable<PathArgument> pathArguments;
        if (basePath == null) {
            pathArguments = pathBuilder.build();
        } else {
            pathArguments = Iterables.concat(basePath, pathBuilder.build());
        }
        return new InstanceWildcard<>((Class<T>)arg.getType(), pathArguments, hashCode());
    }
}
