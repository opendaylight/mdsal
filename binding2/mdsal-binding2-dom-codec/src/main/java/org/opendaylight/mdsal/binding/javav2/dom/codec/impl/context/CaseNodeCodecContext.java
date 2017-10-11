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
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base.DataContainerCodecPrototype;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base.TreeNodeCodecContext;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeArgument;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;

/**
 * Codec context for serializing and deserializing choice case node and it's
 * path.
 *
 * @param <D>
 *            - type of tree node
 */
@Beta
public final class CaseNodeCodecContext<D extends TreeNode> extends TreeNodeCodecContext<D, CaseSchemaNode> {

    /**
     * Prepare context for choice case node from prototype.
     *
     * @param prototype
     *            - codec prototype of choice case node
     */
    public CaseNodeCodecContext(final DataContainerCodecPrototype<CaseSchemaNode> prototype) {
        super(prototype);
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void addYangPathArgument(final TreeArgument arg, final List<PathArgument> builder) {
        // add your implementation
    }

    @Nonnull
    @Override
    public D deserialize(@Nonnull final NormalizedNode<?, ?> normalizedNode) {
        Preconditions.checkState(normalizedNode instanceof ChoiceNode);
        return createBindingProxy((ChoiceNode) normalizedNode);
    }

    @Override
    protected Object deserializeObject(final NormalizedNode<?, ?> normalizedNode) {
        return deserialize(normalizedNode);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public PathArgument serializePathArgument(final TreeArgument arg) {
        Preconditions.checkArgument(arg == null);
        return null;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public TreeArgument deserializePathArgument(final YangInstanceIdentifier.PathArgument arg) {
        Preconditions.checkArgument(arg == null);
        return null;
    }
}
