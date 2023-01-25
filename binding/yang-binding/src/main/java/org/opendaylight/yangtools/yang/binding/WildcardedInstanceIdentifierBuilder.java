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
import com.google.common.collect.Iterables;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.IdentifiableItem;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;

public class WildcardedInstanceIdentifierBuilder<T extends DataObject> extends
        AbstractInstanceIdentifierBuilder<T> {

    WildcardedInstanceIdentifierBuilder() {
        super();
    }

    WildcardedInstanceIdentifierBuilder(final PathArgument item, final Iterable<? extends PathArgument> pathArguments,
            final int hash, final ImmutableList.@Nullable Builder<PathArgument> pathBuilder) {
        super(item, pathArguments, hash, pathBuilder);
    }

    WildcardedInstanceIdentifierBuilder(final PathArgument item, final Iterable<? extends PathArgument> pathArguments,
            final int hash) {
        super(item, pathArguments, hash);
    }

    @Override
    boolean wildcarded() {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull <N extends ChildOf<? super T>> InstanceIdentifierBuilder<N> child(
            Class<N> container) {
        Preconditions.checkArgument(Identifiable.class.isAssignableFrom(container),
                "Must not be Identifiable. Use wildcardChild() method instead");
        addNode(Item.of(container));
        return (WildcardedInstanceIdentifierBuilder<N>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull <C extends ChoiceIn<? super T> & DataObject, N extends ChildOf<? super C>>
    InstanceIdentifierBuilder<N> child(Class<C> caze, Class<N> container) {
        Preconditions.checkArgument(Identifiable.class.isAssignableFrom(container),
                "Must not be Identifiable. Use wildcardChild() method instead");
        addNode(Item.of(caze, container));
        return (WildcardedInstanceIdentifierBuilder<N>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull <N extends Identifiable<K> & ChildOf<? super T>,
            K extends Identifier<N>> InstanceIdentifierBuilder<N> child(Class<@NonNull N> listItem, K listKey) {
        addNode(IdentifiableItem.of(listItem, listKey));
        return (WildcardedInstanceIdentifierBuilder<N>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull <C extends ChoiceIn<? super T> & DataObject, K extends Identifier<N>,
            N extends Identifiable<K> & ChildOf<? super C>> InstanceIdentifierBuilder<N> child(
            Class<C> caze, Class<N> listItem, K listKey) {
        addNode(IdentifiableItem.of(caze, listItem, listKey));
        return (WildcardedInstanceIdentifierBuilder<N>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <N extends ChildOf<? super T> & Identifiable<?>> WildcardedInstanceIdentifierBuilder<N> wildcardChild(
            Class<N> container) {
        addNode(Item.of(container));
        return (WildcardedInstanceIdentifierBuilder<N>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull <C extends ChoiceIn<? super T> & DataObject & Identifiable<?>, N extends ChildOf<? super C>>
            WildcardedInstanceIdentifierBuilder<N> wildcardChild(Class<C> caze, Class<N> container) {
        addNode(Item.of(caze, container));
        return (WildcardedInstanceIdentifierBuilder<N>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull <N extends DataObject & Augmentation<? super T>> InstanceIdentifierBuilder<N> augmentation(
            Class<N> container) {
        Preconditions.checkArgument(Identifiable.class.isAssignableFrom(container),
                "Must not be Identifiable. Use wildcardChild() method instead");
        addNode(Item.of(container));
        return (WildcardedInstanceIdentifierBuilder<N>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull <N extends DataObject & Augmentation<? super T> & Identifiable<?>>
            WildcardedInstanceIdentifierBuilder<N> wildcardAugmentation(Class<N> container) {
        addNode(Item.of(container));
        return (WildcardedInstanceIdentifierBuilder<N>) this;
    }

    @Override
    public @NonNull WildcardedInstanceIdentifier<T> build() {
        Preconditions.checkState(arg != null, "No path arguments present");

        final Iterable<PathArgument> pathArguments;
        if (basePath == null) {
            pathArguments = pathBuilder.build();
        } else {
            pathArguments = Iterables.concat(basePath, pathBuilder.build());
        }

        return WildcardedInstanceIdentifier.trustedCreate(arg, pathArguments, hashBuilder.build(), true);
    }
}
