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
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier.KeyedInstanceIdentifierBuilder;

class InstanceIdentifierBuilderImpl<T extends DataObject>
        implements InstanceIdentifierBuilder<T> {
    protected final ImmutableList.Builder<PathArgument> pathBuilder;
    protected final HashCodeBuilder<PathArgument> hashBuilder;
    protected final Iterable<? extends PathArgument> basePath;
    protected boolean wildcard = false;
    protected PathArgument arg = null;

    InstanceIdentifierBuilderImpl() {
        this.hashBuilder = new HashCodeBuilder<>();
        this.pathBuilder = ImmutableList.builder();
        this.basePath = null;
    }

    InstanceIdentifierBuilderImpl(final PathArgument item, final Iterable<? extends PathArgument> pathArguments,
            final int hash, final boolean wildcard) {
        this.hashBuilder = new HashCodeBuilder<>(hash);
        this.basePath = pathArguments;
        this.pathBuilder = ImmutableList.builder();
        this.wildcard = wildcard;
        this.arg = item;
    }

    InstanceIdentifierBuilderImpl(final PathArgument item, final Iterable<? extends PathArgument> pathArguments,
            final ImmutableList.Builder<PathArgument> pathBuilder, final int hash, final boolean wildcard) {
        this.hashBuilder = new HashCodeBuilder<>(hash);
        this.basePath = pathArguments;
        this.pathBuilder = pathBuilder;
        this.wildcard = wildcard;
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
        if (obj instanceof InstanceIdentifierBuilderImpl) {
            @SuppressWarnings("unchecked")
            final InstanceIdentifierBuilderImpl<T> otherBuilder = (InstanceIdentifierBuilderImpl<T>) obj;
            return wildcard == otherBuilder.wildcard && Objects.equals(basePath, otherBuilder.basePath)
                    && Objects.equals(arg, otherBuilder.arg)
                    && Objects.equals(hashBuilder.build(), otherBuilder.hashBuilder.build());
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    protected <N extends DataObject> InstanceIdentifierBuilderImpl<N> getInstanceIdentifierBuilder() {
        return (InstanceIdentifierBuilderImpl<N>) this;
    }

    protected <N extends DataObject & Identifiable<K>, K extends Identifier<N>>
        KeyedInstanceIdentifierBuilderImpl<N, K> getKeyedInstanceIdentifierBuilder(K key) {
        return new KeyedInstanceIdentifierBuilderImpl<>(arg, basePath, pathBuilder, hashCode(), wildcard);
    }

    @Override
    public <N extends ChildOf<? super T>> @NonNull InstanceIdentifierBuilder<N> child(final Class<N> container) {
        return addNode(container);
    }

    @Override
    public <C extends ChoiceIn<? super T> & DataObject, N extends ChildOf<? super C>> @NonNull
            InstanceIdentifierBuilder<N> child(final Class<C> caze, final Class<N> container) {
        return addWildNode(Item.of(caze, container));
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
        return addNode(container);
    }


    <N extends DataObject> @NonNull InstanceIdentifierBuilderImpl<N> addWildNode(final Item<?> newArg) {
        return addNode(newArg);
    }

    @SuppressWarnings("unchecked")
    <N extends DataObject> @NonNull InstanceIdentifierBuilderImpl<N> addNode(final Item<?> newArg) {
        if (Identifiable.class.isAssignableFrom(newArg.getType())) {
            wildcard = true;
        }
        addNodeInternal(newArg);
        return getInstanceIdentifierBuilder();
    }

    @SuppressWarnings("unchecked")
    <N extends DataObject & Identifiable<K>, K extends Identifier<N>> @NonNull KeyedInstanceIdentifierBuilder<N,K>
    addNode(final IdentifiableItem<?,?> newArg) {
        addNodeInternal(newArg);
        return getKeyedInstanceIdentifierBuilder((K)newArg.getKey());
    }

    protected  <N extends DataObject> @NonNull InstanceIdentifierBuilderImpl<N> addNode(final Class<N> container) {
        return addWildNode(Item.of(container));
    }

    protected void addNodeInternal(final PathArgument newArg) {
        arg = newArg;
        hashBuilder.addArgument(newArg);
        pathBuilder.add(newArg);
    }

    @Override
    public @NonNull InstanceIdentifier<T> build() {
        Preconditions.checkState(arg != null, "No path arguments present");

        final Iterable<PathArgument> pathArguments;
        if (basePath == null) {
            pathArguments = pathBuilder.build();
        } else {
            pathArguments = Iterables.concat(basePath, pathBuilder.build());
        }

        return InstanceIdentifier.trustedCreate(arg, pathArguments, hashBuilder.build(), wildcard);
    }
}
