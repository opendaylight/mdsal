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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.InstanceIdentifier.PathArgument;
import org.opendaylight.mdsal.binding.api.KeyedInstanceIdentifier.KeyedInstanceIdentifierBuilder;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Key;
import org.opendaylight.yangtools.yang.binding.KeyAware;

public class KeyedInstanceIdentifierBuilderImpl<T extends DataObject & KeyAware<K>, K extends Key<T>>
        extends InstanceIdentifierBuilderImpl<T> implements KeyedInstanceIdentifierBuilder<T, K> {
    KeyedInstanceIdentifierBuilderImpl(final PathArgument item, final Iterable<? extends PathArgument> pathArguments,
            final int hash) {
        super(item, pathArguments, hash);
    }

    KeyedInstanceIdentifierBuilderImpl(final PathArgument item, final Iterable<? extends PathArgument> pathArguments,
            final ImmutableList.Builder<PathArgument> pathBuilder, final int hash) {
        super(item, pathArguments, pathBuilder, hash);
    }

    @Override
    protected <N extends DataObject> InstanceIdentifierBuilderImpl<N> getInstanceIdentifierBuilder() {
        return new InstanceIdentifierBuilderImpl<>(arg, basePath, pathBuilder, hashCode());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <N extends DataObject & KeyAware<Y>, Y extends Key<N>>
            KeyedInstanceIdentifierBuilderImpl<N, Y> getKeyedInstanceIdentifierBuilder(final Y key) {
        return (KeyedInstanceIdentifierBuilderImpl<N, Y>) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull KeyedInstanceIdentifier<T,K> build() {
        Preconditions.checkState(arg != null, "No path arguments present");

        if (Identifiable.class.isAssignableFrom(arg.getType())) {
            final K key;
            if (arg instanceof InstanceIdentifier.IdentifiableItem<?,?> ii) {
                key = (K)ii.getKey();
            } else {
                key = null;
            }

            final Iterable<PathArgument> pathArguments;
            if (basePath == null) {
                pathArguments = pathBuilder.build();
            } else {
                pathArguments = Iterables.concat(basePath, pathBuilder.build());
            }

            return new KeyedInstanceIdentifier<>((Class<T>)arg.getType(), pathArguments, hashCode(), key);
        }

        throw new IllegalStateException("Last path argument is not keyed.");
    }
}
