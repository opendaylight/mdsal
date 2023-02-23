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
import org.opendaylight.yangtools.util.HashCodeBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.IdentifiableItem;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier.KeyedInstanceIdentifierBuilder;

abstract class AbstractInstanceIdentifier<T extends DataObject> {
    protected final ImmutableList.Builder<PathArgument> pathBuilder;
    protected final HashCodeBuilder<PathArgument> hashBuilder;
    protected final Iterable<? extends PathArgument> basePath;
    protected boolean wildcard = false;
    protected PathArgument arg = null;

    AbstractInstanceIdentifier() {
        this.hashBuilder = new HashCodeBuilder<>();
        this.pathBuilder = ImmutableList.builder();
        this.basePath = null;
    }

    AbstractInstanceIdentifier(final PathArgument item, final Iterable<? extends PathArgument> pathArguments,
            final int hash, final boolean wildcard) {
        this.hashBuilder = new HashCodeBuilder<>(hash);
        this.basePath = pathArguments;
        this.pathBuilder = ImmutableList.builder();
        this.wildcard = wildcard;
        this.arg = item;
    }

    AbstractInstanceIdentifier(final PathArgument item, final Iterable<? extends PathArgument> pathArguments,
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
        if (obj instanceof AbstractInstanceIdentifier) {
            @SuppressWarnings("unchecked")
            final AbstractInstanceIdentifier<T> otherBuilder = (AbstractInstanceIdentifier<T>) obj;
            return wildcard == otherBuilder.wildcard && Objects.equals(basePath, otherBuilder.basePath)
                    && Objects.equals(arg, otherBuilder.arg)
                    && Objects.equals(hashBuilder.build(), otherBuilder.hashBuilder.build());
        }
        return false;
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

    protected abstract <N extends DataObject> InstanceIdentifierBuilderImpl<N> getInstanceIdentifierBuilder();

    protected abstract <N extends DataObject & Identifiable<K>, K extends Identifier<N>>
        KeyedInstanceIdentifierBuilderImpl<N,K> getKeyedInstanceIdentifierBuilder(K key);
}
