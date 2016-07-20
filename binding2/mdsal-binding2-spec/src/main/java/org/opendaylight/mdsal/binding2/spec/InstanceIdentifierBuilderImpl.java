/*
 * Copyright (c) 2013 - 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding2.spec;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.Objects;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.util.HashCodeBuilder;

@Beta
final class InstanceIdentifierBuilderImpl<T extends TreeNode>
 implements InstanceIdentifierBuilder<T> {
    private final ImmutableList.Builder<TreeArgument> pathBuilder = ImmutableList.builder();
    private final HashCodeBuilder<TreeArgument> hashBuilder;
    private final Iterable<? extends TreeArgument> basePath;
    private boolean wildcard = false;
    private TreeArgument arg = null;

    InstanceIdentifierBuilderImpl() {
        this.hashBuilder = new HashCodeBuilder<>();
        this.basePath = null;
    }

    InstanceIdentifierBuilderImpl(final TreeArgument item, final Iterable<? extends TreeArgument> pathArguments, final int hash, final boolean wildcard) {
        this.hashBuilder = new HashCodeBuilder<>(hash);
        this.basePath = pathArguments;
        this.wildcard = wildcard;
        this.arg = item;
    }

    @Override
    public int hashCode() {
        return hashBuilder.build();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof InstanceIdentifierBuilderImpl) {
            InstanceIdentifierBuilderImpl<T> otherBuilder = (InstanceIdentifierBuilderImpl<T>) obj;
            return wildcard == wildcard &&
                    Objects.equals(basePath, otherBuilder.basePath) &&
                    Objects.equals(arg, otherBuilder.arg) &&
                    Objects.equals(hashBuilder.build(), otherBuilder.hashBuilder.build());
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    <N extends TreeNode> InstanceIdentifierBuilderImpl<N> addNode(final Class<N> container) {
        arg = new Item<N>(container);
        hashBuilder.addArgument(arg);
        pathBuilder.add(arg);

        if (Identifiable.class.isAssignableFrom(container)) {
            wildcard = true;
        }

        return (InstanceIdentifierBuilderImpl<N>) this;
    }

    @SuppressWarnings("unchecked")
    <N extends TreeNode, K> InstanceIdentifierBuilderImpl<N> addNode(
            final Class<N> listItem, final K listKey) {
        arg = new IdentifiableItem<N, K>(listItem, listKey);
        hashBuilder.addArgument(arg);
        pathBuilder.add(arg);
        return (InstanceIdentifierBuilderImpl<N>) this;
    }

    @Override
    public <N extends TreeNode & TreeChildNode<? super T, Item<N>>> InstanceIdentifierBuilderImpl<N> child(
            final Class<N> container) {
        return addNode(container);
    }

    @Override
    public <N extends TreeChildNode<? super T, ?>, K> InstanceIdentifierBuilderImpl<N> child(
            final Class<N> listItem, final K listKey) {
        return addNode(listItem, listKey);
    }

    /**
     * Build an identifier which refers to a specific augmentation of the current InstanceIdentifier referenced by
     * the builder
     *
     * @param container
     * @param <N>
     * @return
     */
    @Override
    public <N extends TreeNode & Augmentation<? super T>> InstanceIdentifierBuilderImpl<N> augmentation(
            final Class<N> container) {
        return addNode(container);
    }

    @Override
    public InstanceIdentifier<T> build() {
        Preconditions.checkState(arg != null, "No path arguments present");

        final Iterable<TreeArgument> pathArguments;
        if (basePath == null) {
            pathArguments = pathBuilder.build();
        } else {
            pathArguments = Iterables.concat(basePath, pathBuilder.build());
        }

        @SuppressWarnings("unchecked")
        final InstanceIdentifier<T> ret = (InstanceIdentifier<T>) InstanceIdentifier.trustedCreate(arg, pathArguments, hashBuilder.build(), wildcard);
        return ret;
    }

    /*
     * @deprecated Use #build() instead.
     */
    @Override
    @Deprecated
    public InstanceIdentifier<T> toInstance() {
        return build();
    }
}
