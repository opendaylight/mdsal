/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier.KeyedInstanceIdentifierBuilder;

public class KeyedInstanceIdentifierBuilderImpl<T extends DataObject & Identifiable<K>,K extends Identifier<T>>
        extends InstanceIdentifierBuilderImpl<T> implements KeyedInstanceIdentifierBuilder<T,K> {

    KeyedInstanceIdentifierBuilderImpl(final PathArgument item, final Iterable<? extends PathArgument> pathArguments,
            ImmutableList.Builder<PathArgument> pathBuilder, final int hash, final boolean wildcard) {
        super(item, pathArguments, pathBuilder, hash, wildcard);
    }

    @Override
    protected <N extends DataObject> InstanceIdentifierBuilderImpl<N> getInstanceIdentifierBuilder() {
        return new InstanceIdentifierBuilderImpl<>(arg, basePath, pathBuilder, hashCode(), wildcard);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <N extends DataObject & Identifiable<Y>, Y extends Identifier<N>>
        KeyedInstanceIdentifierBuilderImpl<N, Y> getKeyedInstanceIdentifierBuilder(Y key) {
        return (KeyedInstanceIdentifierBuilderImpl<N,Y>) this;
    }
}
