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
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.util.HashCodeBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;

public abstract class AbstractInstanceIdentifierBuilder<T extends DataObject> implements InstanceIdentifierBuilder<T> {

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

    @Override
    public InstanceIdentifier<T> build() {
        Preconditions.checkState(arg != null, "No path arguments present");

        final Iterable<PathArgument> pathArguments;
        if (basePath == null) {
            pathArguments = pathBuilder.build();
        } else {
            pathArguments = Iterables.concat(basePath, pathBuilder.build());
        }

        return InstanceIdentifier.trustedCreate(arg, pathArguments, hashBuilder.build(), wildcarded());
    }

    @SuppressWarnings("unchecked")
    <N extends DataObject> @NonNull AbstractInstanceIdentifierBuilder<N> addNode(final PathArgument newArg) {
        arg = newArg;
        hashBuilder.addArgument(newArg);
        pathBuilder.add(newArg);
        return (AbstractInstanceIdentifierBuilder<N>)this;
    }

    protected <T extends DataObject> InstanceIdentifierBuilderImpl<T> getNormalBuilder() {
        return new InstanceIdentifierBuilderImpl<T>(arg, basePath, hashBuilder.hashCode(), pathBuilder);
    }

    protected <T extends Identifiable<K> & DataObject,K extends Identifier<T>>
        KeyedInstanceIdentifierBuilder<T,K> getKeyedBuilder() {
        return new KeyedInstanceIdentifierBuilder<>(arg, basePath, hashBuilder.hashCode(), pathBuilder);
    }

    protected <T extends DataObject> WildcardedInstanceIdentifierBuilder<T> getWildcardedBuilder() {
        return new WildcardedInstanceIdentifierBuilder<>(arg, basePath, hashBuilder.hashCode(), pathBuilder);
    }
}
