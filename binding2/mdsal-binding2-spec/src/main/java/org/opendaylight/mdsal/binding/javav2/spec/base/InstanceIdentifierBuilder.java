/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.spec.base;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentation;
import org.opendaylight.mdsal.binding.javav2.spec.structural.TreeChildNode;
import org.opendaylight.yangtools.concepts.Builder;

@Beta
public interface InstanceIdentifierBuilder<T extends TreeNode> extends Builder<InstanceIdentifier<T>> {
    /**
     * Append the specified container as a child of the current InstanceIdentifier referenced by the
     * builder.
     *
     * This method should be used when you want to build an instance identifier by appending
     * top-level elements
     *
     * Example,
     *
     * <pre>
     * InstanceIdentifier.builder().child(Nodes.class).build();
     *
     * </pre>
     *
     * NOTE :- The above example is only for illustration purposes InstanceIdentifier.builder() has
     * been deprecated and should not be used. Use InstanceIdentifier.builder(Nodes.class) instead
     *
     * @param container
     * @param <N>
     * @return
     */
    // FIXME: Why TreeNode needs to be explicitly mentioned, whern ChildTreeNode is derived from
    // TreeNode?
    <N extends TreeNode & TreeChildNode<? super T, Item<N>>> InstanceIdentifierBuilder<N> child(Class<N> container);

    /**
     * Append the specified listItem as a child of the current InstanceIdentifier referenced by the
     * builder.
     *
     * This method should be used when you want to build an instance identifier by appending a
     * specific list element to the identifier
     *
     * @param listItem
     * @param listKey
     * @param <N>
     * @param <K>
     * @return
     */
    <N extends TreeChildNode<? super T, ?>, K> InstanceIdentifierBuilder<N> child(Class<N> listItem, K listKey);

    /**
     * Build an identifier which refers to a specific augmentation of the current InstanceIdentifier
     * referenced by the builder
     *
     * @param container
     * @param <N>
     * @return
     */
    <N extends TreeNode & Augmentation<? super T>> InstanceIdentifierBuilder<N> augmentation(Class<N> container);

    /**
     * Build the instance identifier.
     *
     * @return
     */
    @Override
    InstanceIdentifier<T> build();

    /*
     * @deprecated use #build()
     */
    @Deprecated
    InstanceIdentifier<T> toInstance();
}