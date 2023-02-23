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
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.util.HashCodeBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.IdentifiableItem;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.KeyedBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;

class InstanceIdentifierBuilderImpl<T extends DataObject> implements InstanceIdentifierBuilder<T> {
    protected final ImmutableList.Builder<PathArgument> pathBuilder;
    protected final HashCodeBuilder<PathArgument> hashBuilder;
    protected final Iterable<? extends PathArgument> basePath;
    protected boolean wildcard = false;
    protected PathArgument arg = null;

    InstanceIdentifierBuilderImpl() {
        hashBuilder = new HashCodeBuilder<>();
        pathBuilder = ImmutableList.builder();
        basePath = null;
    }

    InstanceIdentifierBuilderImpl(final PathArgument item, final Iterable<? extends PathArgument> pathArguments,
            final int hash, final boolean wildcard) {
        hashBuilder = new HashCodeBuilder<>(hash);
        basePath = pathArguments;
        pathBuilder = ImmutableList.builder();
        this.wildcard = wildcard;
        arg = item;
    }

    InstanceIdentifierBuilderImpl(final PathArgument item, final Iterable<? extends PathArgument> pathArguments,
            final ImmutableList.Builder<PathArgument> pathBuilder, final int hash, final boolean wildcard) {
        hashBuilder = new HashCodeBuilder<>(hash);
        basePath = pathArguments;
        this.pathBuilder = pathBuilder;
        this.wildcard = wildcard;
        arg = item;
    }

    @Override
    public final int hashCode() {
        return hashBuilder.build();
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        // FIXME: check getClass() ?
        if (obj instanceof InstanceIdentifierBuilderImpl) {
            @SuppressWarnings("unchecked")
            final InstanceIdentifierBuilderImpl<T> otherBuilder = (InstanceIdentifierBuilderImpl<T>) obj;
            return wildcard == otherBuilder.wildcard && Objects.equals(basePath, otherBuilder.basePath)
                    && Objects.equals(arg, otherBuilder.arg)
                    && Objects.equals(hashBuilder.build(), otherBuilder.hashBuilder.build());
        }
        return false;
    }

    @Override
    public final <N extends ChildOf<? super T>> InstanceIdentifierBuilder<N> child(final Class<N> container) {
        return addNode(container);
    }

    @Override
    public final <C extends ChoiceIn<? super T> & DataObject, N extends ChildOf<? super C>>
            InstanceIdentifierBuilder<N> child(final Class<C> caze, final Class<N> container) {
        return addWildNode(Item.of(caze, container));
    }

    @Override
    public final <N extends Identifiable<K> & ChildOf<? super T>, K extends Identifier<N>> KeyedBuilder<N, K> child(
            final Class<@NonNull N> listItem, final K listKey) {
        return addNode(IdentifiableItem.of(listItem, listKey));
    }

    @Override
    public final <C extends ChoiceIn<? super T> & DataObject, K extends Identifier<N>,
            N extends Identifiable<K> & ChildOf<? super C>> @NonNull KeyedBuilder<N, K> child(final Class<C> caze,
                final Class<N> listItem, final K listKey) {
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
    public final <N extends DataObject & Augmentation<? super T>> InstanceIdentifierBuilder<N> augmentation(
            final Class<N> container) {
        return addNode(container);
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

        return InstanceIdentifier.trustedCreate(arg, pathArguments, hashBuilder.build(), wildcard);
    }

    <N extends DataObject> @NonNull InstanceIdentifierBuilderImpl<N> addWildNode(final Item<N> newArg) {
        return addNode(newArg);
    }

    @SuppressWarnings("unchecked")
    <N extends DataObject> @NonNull InstanceIdentifierBuilderImpl<N> addNode(final Item<N> newArg) {
        if (Identifiable.class.isAssignableFrom(newArg.getType())) {
            wildcard = true;
        }
        addNodeInternal(newArg);
        return (InstanceIdentifierBuilderImpl<N>) this;
    }

    <N extends DataObject & Identifiable<K>, K extends Identifier<N>> @NonNull KeyedBuilder<N, K> addNode(
            final IdentifiableItem<N, K> newArg) {
        addNodeInternal(newArg);
        return getKeyedInstanceIdentifierBuilder(newArg.getKey());
    }

    protected <N extends DataObject> @NonNull InstanceIdentifierBuilderImpl<N> addNode(final Class<N> container) {
        return addWildNode(Item.of(container));
    }

    protected void addNodeInternal(final PathArgument newArg) {
        arg = newArg;
        hashBuilder.addArgument(newArg);
        pathBuilder.add(newArg);
    }

    protected <N extends DataObject & Identifiable<K>, K extends Identifier<N>>
        KeyedInstanceIdentifierBuilderImpl<N, K> getKeyedInstanceIdentifierBuilder(final K key) {
        return new KeyedInstanceIdentifierBuilderImpl<>(arg, basePath, pathBuilder, hashCode(), wildcard);
    }
}
