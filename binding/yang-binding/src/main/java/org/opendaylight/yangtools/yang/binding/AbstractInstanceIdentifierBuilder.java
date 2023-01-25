/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import com.google.common.collect.ImmutableList;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.util.HashCodeBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.IdentifiableItem;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier.KeyedInstanceIdentifierBuilder;
import org.opendaylight.yangtools.yang.binding.WildcardedInstanceIdentifier.WildcardedInstanceIdentifierBuilder;

public abstract class AbstractInstanceIdentifierBuilder<T extends DataObject> {

    protected final ImmutableList.Builder<PathArgument> pathBuilder;
    protected final HashCodeBuilder<PathArgument> hashBuilder;
    protected final Iterable<? extends PathArgument> basePath;
    protected PathArgument arg = null;

    abstract boolean wildcarded();

    AbstractInstanceIdentifierBuilder() {
        this.hashBuilder = new HashCodeBuilder<>();
        this.basePath = null;
        this.pathBuilder = ImmutableList.builder();
    }

    AbstractInstanceIdentifierBuilder(final PathArgument item, final Iterable<? extends PathArgument> pathArguments,
            final int hash) {
        this.hashBuilder = new HashCodeBuilder<>(hash);
        this.basePath = pathArguments;
        this.arg = item;
        this.pathBuilder = ImmutableList.builder();
    }

    AbstractInstanceIdentifierBuilder(final PathArgument item, final Iterable<? extends PathArgument> pathArguments,
            final int hash, final ImmutableList.@Nullable Builder<PathArgument> pathBuilder) {
        this.hashBuilder = new HashCodeBuilder<>(hash);
        this.basePath = pathArguments;
        this.arg = item;
        this.pathBuilder = Objects.requireNonNullElseGet(pathBuilder, ImmutableList::builder);
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
        if (obj instanceof AbstractInstanceIdentifierBuilder) {
            @SuppressWarnings("unchecked")
            final AbstractInstanceIdentifierBuilder<T> otherBuilder = (AbstractInstanceIdentifierBuilder<T>) obj;
            return wildcarded() == otherBuilder.wildcarded() && Objects.equals(basePath, otherBuilder.basePath)
                    && Objects.equals(arg, otherBuilder.arg)
                    && Objects.equals(hashBuilder.build(), otherBuilder.hashBuilder.build());
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    <N extends DataObject> @NonNull InstanceIdentifierBuilder<N> addNode(final Item<N> newArg) {
        addNodeInternal(newArg);
        return (InstanceIdentifierBuilder<N>)this;
    }

    @SuppressWarnings("unchecked")
    <N extends DataObject & Identifiable<K>, K extends Identifier<N>> @NonNull KeyedInstanceIdentifierBuilder<N,K>
        addNode(final IdentifiableItem<N,K> newArg) {
        addNodeInternal(newArg);
        return (KeyedInstanceIdentifierBuilder<N,K>)this;
    }

    @SuppressWarnings("unchecked")
    <N extends DataObject> @NonNull WildcardedInstanceIdentifierBuilder<N>
        addWildNode(final Item<N> newArg) {
        addNodeInternal(newArg);
        return (WildcardedInstanceIdentifierBuilder<N>) addNode(newArg);
    }

    protected <T extends DataObject> InstanceIdentifierBuilderImpl<T> getNormalBuilder() {
        return new InstanceIdentifierBuilderImpl<T>(arg, basePath, hashBuilder.hashCode(), pathBuilder);
    }

    protected <T extends Identifiable<K> & DataObject,K extends Identifier<T>>
        KeyedInstanceIdentifierBuilderImpl<T,K> getKeyedBuilder() {
        return new KeyedInstanceIdentifierBuilderImpl<>(arg, basePath, hashBuilder.hashCode(), pathBuilder);
    }

    protected <T extends DataObject> WildcardedInstanceIdentifierBuilderImpl<T> getWildcardedBuilder() {
        return new WildcardedInstanceIdentifierBuilderImpl<>(arg, basePath, hashBuilder.hashCode(), pathBuilder);
    }

    protected void addNodeInternal(final PathArgument newArg) {
        arg = newArg;
        hashBuilder.addArgument(newArg);
        pathBuilder.add(newArg);
    }
}
