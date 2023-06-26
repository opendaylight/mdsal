/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.api;

import com.google.common.annotations.Beta;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * Common interface shared between {@link BindingDataObjectCodecTreeNode} and {@link BindingAugmentationCodecTreeNode}.
 * This interface should never be implemented on its own.
 *
 * @param <T> DataObject type
 */
public interface CommonDataObjectCodecTreeNode<T extends DataObject> extends BindingDataContainerCodecTreeNode<T> {
    /**
     * Returns nested node context using supplied YANG Instance Identifier.
     *
     * @param child
     *            Yang Instance Identifier Argument
     * @return Context of child
     * @throws IllegalArgumentException
     *             If supplied argument does not represent valid child.
     */
    @NonNull BindingCodecTreeNode yangPathArgumentChild(YangInstanceIdentifier.@NonNull PathArgument child);

    /**
     * Returns nested node context using supplied Binding Instance Identifier and adds YANG instance identifiers to
     * the supplied list.
     *
     * @param arg
     *            Binding Instance Identifier Argument
     * @param builder
     *            Mutable instance of list, which is appended by YangInstanceIdentifiers
     *            as tree is walked. Use null if such side-product is not needed.
     * @return Context of child
     * @throws IllegalArgumentException
     *             If supplied argument does not represent valid child.
     */
    @NonNull CommonDataObjectCodecTreeNode<?> bindingPathArgumentChild(InstanceIdentifier.@NonNull PathArgument arg,
            @Nullable List<YangInstanceIdentifier.PathArgument> builder);

    /**
     * Serializes path argument for current node.
     *
     * @param arg Binding Path Argument, may be null if Binding Instance Identifier does not have
     *        representation for current node (e.g. choice or case).
     * @return Yang Path Argument, may be null if Yang Instance Identifier does not have
     *         representation for current node (e.g. case).
     * @throws IllegalArgumentException If supplied {@code arg} is not valid.
     */
    @Beta
    YangInstanceIdentifier.@Nullable PathArgument serializePathArgument(InstanceIdentifier.@Nullable PathArgument arg);

    /**
     * Deserializes path argument for current node.
     *
     * @param arg Yang Path Argument, may be null if Yang Instance Identifier does not have
     *         representation for current node (e.g. case).
     * @return Binding Path Argument, may be null if Binding Instance Identifier does not have
     *        representation for current node (e.g. choice or case).
     * @throws IllegalArgumentException If supplied {@code arg} is not valid.
     */
    @Beta
    InstanceIdentifier.@Nullable PathArgument deserializePathArgument(
            YangInstanceIdentifier.@Nullable PathArgument arg);
}
