/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
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
import org.opendaylight.mdsal.binding.api.InstanceIdentifier.InstanceIdentifierBuilder;
import org.opendaylight.mdsal.binding.api.InstanceIdentifier.Item;
import org.opendaylight.mdsal.binding.api.InstanceIdentifier.PathArgument;
import org.opendaylight.mdsal.binding.api.InstanceWildcard.WildcardBuilder;
import org.opendaylight.mdsal.binding.api.KeyedInstanceIdentifier.KeyedInstanceIdentifierBuilder;
import org.opendaylight.yangtools.util.HashCodeBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.ChoiceIn;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;

class InstanceIdentifierBuilderImpl<T extends DataObject>
        implements InstanceIdentifierBuilder<T> {

    protected final ImmutableList.Builder<PathArgument> pathBuilder;
    private final HashCodeBuilder<PathArgument> hashBuilder;
    protected final Iterable<? extends PathArgument> basePath;
    protected PathArgument arg = null;

    InstanceIdentifierBuilderImpl() {
        this.hashBuilder = new HashCodeBuilder<>();
        this.pathBuilder = ImmutableList.builder();
        this.basePath = null;
    }

    InstanceIdentifierBuilderImpl(final PathArgument item, final Iterable<? extends PathArgument> pathArguments,
            final int hash) {
        this.hashBuilder = new HashCodeBuilder<>(hash);
        this.basePath = pathArguments;
        this.pathBuilder = ImmutableList.builder();
        this.arg = item;
    }

    InstanceIdentifierBuilderImpl(final PathArgument item, final Iterable<? extends PathArgument> pathArguments,
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
        if (obj instanceof InstanceIdentifierBuilderImpl<?>) {
            @SuppressWarnings("unchecked")
            final InstanceIdentifierBuilderImpl<T> otherBuilder = (InstanceIdentifierBuilderImpl<T>) obj;
            return Objects.equals(basePath, otherBuilder.basePath)
                    && Objects.equals(arg, otherBuilder.arg)
                    && Objects.equals(hashBuilder.build(), otherBuilder.hashBuilder.build());
        }
        return false;
    }

    @Override
    public <N extends ChildOf<? super T>> @NonNull InstanceIdentifierBuilder<N> child(final Class<N> container) {
        return addNode(Item.of(container));
    }

    @Override
    public <C extends ChoiceIn<? super T> & DataObject, N extends ChildOf<? super C>> @NonNull
            InstanceIdentifierBuilder<N> child(final Class<C> caze, final Class<N> container) {
        return addNode(Item.of(caze, container));
    }

    @Override
    public <N extends Identifiable<K> & ChildOf<? super T>, K extends Identifier<N>> @NonNull
            KeyedInstanceIdentifierBuilder<N,K> child(final Class<@NonNull N> listItem, final K listKey) {
        return addNode(IdentifiableItem.of(listItem, listKey));
    }

    @Override
    public <C extends ChoiceIn<? super T> & DataObject, K extends Identifier<N>,
        N extends Identifiable<K> & ChildOf<? super C>> @NonNull KeyedInstanceIdentifierBuilder<N,K>
        child(final Class<C> caze, final Class<N> listItem, final K listKey) {
        return addNode(IdentifiableItem.of(caze, listItem, listKey));
    }

    @Override
    public @NonNull <N extends ChildOf<? super T> & Identifiable<?>> WildcardBuilder<N> wildcardChild(
            Class<N> container) {
        return addWildNode(Item.of(container));
    }

    @Override
    public @NonNull <C extends ChoiceIn<? super T> & DataObject, N extends ChildOf<? super C> & Identifiable<?>>
    WildcardBuilder<N> wildcardChild(Class<C> caze, Class<N> container) {
        return addWildNode(Item.of(caze, container));
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
    public <N extends DataObject & Augmentation<? super T>> @NonNull InstanceIdentifierBuilder<N> augmentation(
            final Class<N> container) {
        return addNode(Item.of(container));
    }

    @Override
    public @NonNull <N extends DataObject & Augmentation<? super T> & Identifiable<?>> WildcardBuilder<N>
    wildcardAugmentation(Class<N> container) {
        return addWildNode(Item.of(container));
    }

    <N extends DataObject & Identifiable<?>> @NonNull WildcardBuilderImpl<N> addWildNode(final Item<N> newArg) {
        addNodeInternal(newArg);
        return (WildcardBuilderImpl<N>) getWildcardBuilder();
    }

    <N extends DataObject> @NonNull InstanceIdentifierBuilderImpl<N> addNode(final Item<?> newArg) {
        Preconditions.checkArgument(!Identifiable.class.isAssignableFrom(newArg.getType()),
                "The type of new element is Identifiable<?>. "
                        + "Supply a key or use the respective method prefixed with \"wildcard\" instead.");
        addNodeInternal(newArg);
        return getInstanceIdentifierBuilder();
    }

    @SuppressWarnings("unchecked")
    <N extends DataObject & Identifiable<K>, K extends Identifier<N>> @NonNull KeyedInstanceIdentifierBuilder<N,K>
        addNode(final IdentifiableItem<N,K> newArg) {
        addNodeInternal(newArg);
        return getKeyedInstanceIdentifierBuilder(newArg.getKey());
    }

    protected void addNodeInternal(final PathArgument newArg) {
        arg = newArg;
        hashBuilder.addArgument(newArg);
        pathBuilder.add(newArg);
    }

    @SuppressWarnings("unchecked")
    protected <N extends DataObject> InstanceIdentifierBuilderImpl<N> getInstanceIdentifierBuilder() {
        return (InstanceIdentifierBuilderImpl<N>) this;
    }

    protected <N extends DataObject & Identifiable<K>, K extends Identifier<N>>
        KeyedInstanceIdentifierBuilderImpl<N, K> getKeyedInstanceIdentifierBuilder(K key) {
        return new KeyedInstanceIdentifierBuilderImpl<>(arg, basePath, pathBuilder, hashCode());
    }

    protected <N extends DataObject & Identifiable<?>> WildcardBuilder<N> getWildcardBuilder() {
        return new WildcardBuilderImpl<>(arg, basePath, pathBuilder, hashCode());
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull InstanceIdentifier<T> build() {
        Preconditions.checkState(arg != null, "No path arguments present");

        final Iterable<PathArgument> pathArguments;
        if (basePath == null) {
            pathArguments = pathBuilder.build();
        } else {
            pathArguments = Iterables.concat(basePath, pathBuilder.build());
        }
        return new InstanceIdentifier<>((Class<T>)arg.getType(), pathArguments, hashCode());
    }
}
