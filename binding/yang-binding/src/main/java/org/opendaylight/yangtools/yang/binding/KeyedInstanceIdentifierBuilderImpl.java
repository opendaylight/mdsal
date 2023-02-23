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
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.IdentifiableItem;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier.KeyedInstanceIdentifierBuilder;

public class KeyedInstanceIdentifierBuilderImpl<T extends DataObject & Identifiable<K>,K extends Identifier<T>>
        extends AbstractInstanceIdentifier<T> implements KeyedInstanceIdentifierBuilder<T,K> {

    KeyedInstanceIdentifierBuilderImpl(final PathArgument item, final Iterable<? extends PathArgument> pathArguments,
            ImmutableList.Builder<PathArgument> pathBuilder, final int hash, final boolean wildcard) {
        super(item, pathArguments, pathBuilder, hash, wildcard);
    }

    @Override
    protected <N extends DataObject> InstanceIdentifierBuilderImpl<N> getInstanceIdentifierBuilder() {
        return new InstanceIdentifierBuilderImpl<>(arg, basePath, pathBuilder, hashBuilder.hashCode(), wildcard);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <N extends DataObject & Identifiable<Y>, Y extends Identifier<N>>
        KeyedInstanceIdentifierBuilderImpl<N, Y> getKeyedInstanceIdentifierBuilder(Y key) {
        return (KeyedInstanceIdentifierBuilderImpl<N,Y>) this;
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
            N extends Identifiable<K> & ChildOf<? super C>> @NonNull KeyedInstanceIdentifierBuilder<N,K> child(
                    final Class<C> caze, final Class<N> listItem, final K listKey) {
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
