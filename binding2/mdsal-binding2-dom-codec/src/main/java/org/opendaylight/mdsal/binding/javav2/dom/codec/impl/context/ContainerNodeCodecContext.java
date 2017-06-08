/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.OperationInputCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base.DataContainerCodecPrototype;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base.TreeNodeCodecContext;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

/**
 * Context for prototype of container node.
 *
 * @param <D>
 *            - type of tree node
 */
@Beta
public class ContainerNodeCodecContext<D extends TreeNode> extends TreeNodeCodecContext<D, ContainerSchemaNode>
        implements OperationInputCodec<D> {

    /**
     * Prepare context for container node from prototype.
     *
     * @param prototype
     *            - codec prototype of container node
     */
    public ContainerNodeCodecContext(final DataContainerCodecPrototype<ContainerSchemaNode> prototype) {
        super(prototype);
    }

    @Nonnull
    @Override
    public D deserialize(@Nonnull final NormalizedNode<?, ?> data) {
        Preconditions.checkState(data instanceof ContainerNode);
        return createBindingProxy((ContainerNode) data);
    }

    @Override
    protected Object deserializeObject(final NormalizedNode<?, ?> normalizedNode) {
        return deserialize(normalizedNode);
    }
}
