/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.api.serializer;

import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.Notification;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Serialization service, which provides two-way serialization between Java
 * Binding Data representation and NormalizedNode representation.
 */
public interface BindingNormalizedNodeSerializer {

    /**
     * Translates supplied YANG Instance Identifier into Binding instance
     * identifier.
     *
     * @param dom
     *            - YANG Instance Identifier
     * @return Binding Instance Identifier, or null if the instance identifier
     *         is not representable
     */
    @Nullable
    InstanceIdentifier<? extends TreeNode> fromYangInstanceIdentifier(@Nonnull YangInstanceIdentifier dom);

    /**
     * Translates supplied YANG Instance Identifier and NormalizedNode into
     * Binding data.
     *
     * @param path
     *            - Binding Instance Identifier
     * @param data
     *            - NormalizedNode representing data
     * @return DOM Instance Identifier
     */
    @Nullable
    Entry<InstanceIdentifier<? extends TreeNode>, TreeNode> fromNormalizedNode(@Nonnull YangInstanceIdentifier path,
            NormalizedNode<?, ?> data);

    /**
     * Translates supplied Binding Instance Identifier into NormalizedNode
     * instance identifier.
     *
     * @param binding
     *            - Binding Instance Identifier
     * @return DOM Instance Identifier
     * @throws IllegalArgumentException
     *             - if supplied Instance Identifier is not valid
     */
    @Nullable
    YangInstanceIdentifier toYangInstanceIdentifier(@Nonnull InstanceIdentifier<? extends TreeNode> binding);

    /**
     * Translates supplied Binding Instance Identifier and data into
     * NormalizedNode representation.
     *
     * @param path
     *            - Binding Instance Identifier pointing to data
     * @param data
     *            - representing Data Tree
     * @param <T> data type
     * @return NormalizedNode representation
     * @throws IllegalArgumentException
     *             - if supplied Instance Identifier is not valid.
     */
    @Nullable
    <T extends TreeNode> Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>
            toNormalizedNode(InstanceIdentifier<T> path, T data);

    /**
     * Translates supplied NormalizedNode Notification into Binding data.
     *
     * @param path
     *            - Schema Path of Notification, schema path is absolute, and
     *            consists of Notification QName
     * @param data
     *            - NormalizedNode representing data
     * @return Binding representation of Notification
     */
    @Nullable
    Notification<?> fromNormalizedNodeNotification(@Nonnull SchemaPath path, @Nonnull ContainerNode data);

    /**
     * Translates supplied Binding Notification or output into NormalizedNode
     * notification.
     *
     * @param data
     *            NormalizedNode representing notification data
     * @return NormalizedNode representation of notification
     */
    @Nonnull
    ContainerNode toNormalizedNodeNotification(@Nonnull Notification<?> data);

    /**
     * Translates supplied NormalizedNode operation (RPC, Action) input or
     * output into Binding data.
     *
     * @param path
     *            - schema path of operation data, schema path consists of
     *            rpc/action QName and input/output QName.
     * @param data
     *            - NormalizedNode representing data
     * @return Binding representation of operation data
     */
    @Nullable
    TreeNode fromNormalizedNodeOperationData(@Nonnull SchemaPath path, @Nonnull ContainerNode data);

    /**
     * Translates supplied Binding operation (RPC, Action) input or output into
     * NormalizedNode data.
     *
     * @param data
     *            - NormalizedNode representing rpc/action data
     * @return NormalizedNode representation of operation data
     */
    @Nonnull
    ContainerNode toNormalizedNodeOperationData(@Nonnull TreeNode data);
}
