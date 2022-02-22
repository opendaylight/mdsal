/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.dom.codec.spi.AbstractBindingLazyContainerNode;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

/*
 * FIXME: This is a bit of functionality which should really live in binding-dom-codec, but for that to happen we need
 *        to extend BindingNormalizedNodeSerializer with the concept of a routing context -- which would be deprecated,
 *        as we want to move to actions in the long term.
 *
 *        Even then, BindingNormalizedNodeCodecRegistry provides background updates to the context used in
 *        deserialization, which is currently being used.
 */
class LazySerializedContainerNode
        extends AbstractBindingLazyContainerNode<DataObject, BindingNormalizedNodeSerializer> {
    private final @NonNull Absolute path;

    private LazySerializedContainerNode(final Absolute path, final DataObject binding,
            final BindingNormalizedNodeSerializer codec) {
        super(NodeIdentifier.create(path.lastNodeIdentifier()), binding, requireNonNull(codec));
        this.path = path;
    }

    static LazySerializedContainerNode create(final Absolute path, final DataObject data,
            final BindingNormalizedNodeSerializer codec) {
        return data == null ? null : new LazySerializedContainerNode(path, data, codec);
    }

    static LazySerializedContainerNode withContextRef(final Absolute path, final DataObject data,
            final LeafNode<?> contextRef, final BindingNormalizedNodeSerializer serializer) {
        return new WithContextRef(path, data, contextRef, serializer);
    }

    @Override
    protected final ContainerNode computeContainerNode(final BindingNormalizedNodeSerializer context) {
        return context.toNormalizedNodeRpcData(path, getDataObject());
    }

    /**
     * Lazy Serialized Node with pre-cached serialized leaf holding routing information.
     */
    private static final class WithContextRef extends LazySerializedContainerNode {
        private final LeafNode<?> contextRef;

        WithContextRef(final Absolute path, final DataObject binding, final LeafNode<?> contextRef,
                final BindingNormalizedNodeSerializer codec) {
            super(path, binding, codec);
            this.contextRef = requireNonNull(contextRef);
        }

        @Override
        public DataContainerChild childByArg(final PathArgument child) {
            // Use pre-cached value of routing field and do not run full serialization if we are accessing it.
            return contextRef.getIdentifier().equals(child) ? contextRef : super.childByArg(child);
        }
    }
}
