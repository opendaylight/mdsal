/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.dom.codec.util.AbstractBindingLazyContainerNode;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * FIXME: This is a bit of functionality which should really live in binding-dom-codec, but for to happen we need
 *        to extends BindingNormalizedNodeCodecRegistry with the concept of a routing context -- which would be
 *        deprecated, as we want to move to actions in the long term.
 */
class LazySerializedContainerNode extends AbstractBindingLazyContainerNode<DataObject,
        BindingNormalizedNodeCodecRegistry> implements BindingDataAware {

    private LazySerializedContainerNode(final QName identifier, final DataObject binding,
            final BindingNormalizedNodeCodecRegistry registry) {
        super(NodeIdentifier.create(identifier), binding, requireNonNull(registry));
    }

    static ContainerNode create(final SchemaPath rpcName, final DataObject data,
            final BindingNormalizedNodeCodecRegistry codec) {
        return new LazySerializedContainerNode(rpcName.getLastComponent(), data, codec);
    }

    static ContainerNode withContextRef(final SchemaPath rpcName, final DataObject data,
            final LeafNode<?> contextRef, final BindingNormalizedNodeCodecRegistry codec) {
        return new WithContextRef(rpcName.getLastComponent(), data, contextRef, codec);
    }

    @Override
    public final DataObject bindingData() {
        return getDataObject();
    }

    @Override
    protected final ContainerNode computeContainerNode(final BindingNormalizedNodeCodecRegistry context) {
        return context.toNormalizedNodeRpcData(getDataObject());
    }

    /**
     * Lazy Serialized Node with pre-cached serialized leaf holding routing information.
     */
    private static final class WithContextRef extends LazySerializedContainerNode {
        private final LeafNode<?> contextRef;

        protected WithContextRef(final QName identifier, final DataObject binding, final LeafNode<?> contextRef,
                final BindingNormalizedNodeCodecRegistry registry) {
            super(identifier, binding, registry);
            this.contextRef = requireNonNull(contextRef);
        }

        @Override
        public Optional<DataContainerChild<? extends PathArgument, ?>> getChild(final PathArgument child) {
            // Use pre-cached value of routing field and do not run full serialization if we are accessing it.
            return contextRef.getIdentifier().equals(child) ?  Optional.of(contextRef) : super.getChild(child);
        }
    }
}
