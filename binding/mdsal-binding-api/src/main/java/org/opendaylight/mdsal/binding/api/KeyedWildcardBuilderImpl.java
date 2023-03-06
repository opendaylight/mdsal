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
import org.opendaylight.mdsal.binding.api.KeyedInstanceWildcard.KeyedWildcardBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;

public class KeyedWildcardBuilderImpl<T extends DataObject & Identifiable<K>,K extends Identifier<T>>
        extends WildcardBuilderImpl<T> implements
        KeyedWildcardBuilder<T,K> {

    KeyedWildcardBuilderImpl(final PathArgument item, final Iterable<? extends PathArgument> pathArguments,
            final int hash) {
        super(item, pathArguments, hash);
    }

    KeyedWildcardBuilderImpl(final PathArgument item, final Iterable<? extends PathArgument> pathArguments,
            ImmutableList.Builder<PathArgument> pathBuilder, final int hash) {
        super(item, pathArguments, pathBuilder, hash);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull KeyedInstanceWildcard<T,K> build() {
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

            return new KeyedInstanceWildcard<>((Class<T>)arg.getType(), pathArguments, hashCode(), key);
        }

        throw new IllegalStateException("Last path argument is not keyed.");
    }
}
