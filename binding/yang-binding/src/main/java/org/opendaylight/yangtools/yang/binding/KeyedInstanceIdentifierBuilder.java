/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.util.HashCodeBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;

public class KeyedInstanceIdentifierBuilder<T extends DataObject> implements
        InstanceIdentifierBuilder<T> {
    private final ImmutableList.Builder<PathArgument> pathBuilder;
    private final HashCodeBuilder<PathArgument> hashBuilder;
    private final Iterable<? extends PathArgument> basePath;
    private boolean wildcard = false;
    private PathArgument arg = null;

    KeyedInstanceIdentifierBuilder() {
        this.hashBuilder = new HashCodeBuilder<>();
        this.basePath = null;
        this.pathBuilder = ImmutableList.builder();
    }
    KeyedInstanceIdentifierBuilder(final PathArgument item, final Iterable<? extends PathArgument> pathArguments,
            final int hash, final boolean wildcard, final ImmutableList.@Nullable Builder<PathArgument> pathBuilder) {
        this.hashBuilder = new HashCodeBuilder<>(hash);
        this.basePath = pathArguments;
        this.wildcard = wildcard;
        this.arg = item;
        if (pathBuilder != null) {
            this.pathBuilder = pathBuilder;
        } else {
            this.pathBuilder = ImmutableList.builder();
        }
    }

    @Override
    public @NonNull <N extends ChildOf<? super T>> InstanceIdentifierBuilder<N> child(
            Class<N> container) {
        return null;
    }

    @Override
    public <N extends ChildOf<? super T> & Identifiable<?>> InstanceIdentifierBuilder<N> wildcardChild(
            Class<N> container) {
        return null;
    }

    @Override
    public @NonNull <C extends ChoiceIn<? super T> & DataObject, N extends ChildOf<? super C>> InstanceIdentifierBuilder<N> child(
            Class<C> caze, Class<N> container) {
        return null;
    }

    @Override
    public @NonNull <N extends Identifiable<K> & ChildOf<? super T>, K extends Identifier<N>> InstanceIdentifierBuilder<N> child(
            Class<@NonNull N> listItem, K listKey) {
        return null;
    }

    @Override
    public @NonNull <C extends ChoiceIn<? super T> & DataObject, K extends Identifier<N>,
            N extends Identifiable<K> & ChildOf<? super C>> InstanceIdentifierBuilder<N> child(Class<C> caze,
            Class<N> listItem, K listKey) {
        return null;
    }

    @Override
    public @NonNull <N extends DataObject & Augmentation<? super T>> InstanceIdentifierBuilder<N> augmentation(
            Class<N> container) {
        return null;
    }

    @Override
    public @NonNull InstanceIdentifier<T> build() {
        return null;
    }

    <N extends DataObject> @NonNull InstanceIdentifierBuilder<N> addWildNode(final PathArgument newArg) {
        if (Identifiable.class.isAssignableFrom(newArg.getType())) {
            wildcard = true;
        }
        return addNode(newArg);
    }

    @SuppressWarnings("unchecked")
    <N extends DataObject> @NonNull InstanceIdentifierBuilder<N> addNode(final PathArgument newArg) {
        return null;
    }

    @SuppressWarnings("unchecked")
    <N extends DataObject> @NonNull KeyedInstanceIdentifierBuilder<N> addKeyedNode(final PathArgument newArg) {
        arg = newArg;
        hashBuilder.addArgument(newArg);
        pathBuilder.add(newArg);
        return (KeyedInstanceIdentifierBuilder<N>) this;
    }

    private <N extends DataObject> @NonNull InstanceIdentifierBuilder<N> addNode(final Class<N> container) {
        return addWildNode(Item.of(container));
    }
}
