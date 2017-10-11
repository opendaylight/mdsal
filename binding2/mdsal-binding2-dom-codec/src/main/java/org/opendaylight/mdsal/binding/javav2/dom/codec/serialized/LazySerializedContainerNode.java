/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.serialized;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Serialize operation from binding to DOM.
 */
public class LazySerializedContainerNode implements ContainerNode {

    private final NodeIdentifier identifier;
    private final TreeNode bindingData;

    private BindingNormalizedNodeCodecRegistry registry;
    private ContainerNode domData;

    private LazySerializedContainerNode(final QName identifier, final TreeNode binding,
            final BindingNormalizedNodeCodecRegistry registry) {
        this.identifier = NodeIdentifier.create(identifier);
        this.bindingData = binding;
        this.registry = registry;
        this.domData = null;
    }

    /**
     * Prepare serializer of binding data with specific codec.
     *
     * @param operationName
     *            - qname of operation
     * @param data
     *            - binding operation
     * @param codec
     *            - specifc codec for operation
     * @return instance of lazy serialized container node
     */
    public static NormalizedNode<?, ?> create(final SchemaPath operationName, final TreeNode data,
            final BindingNormalizedNodeCodecRegistry codec) {
        return new LazySerializedContainerNode(operationName.getLastComponent(), data, codec);
    }

    /**
     * Prepare serializer of binding data with specific codec and pre-cached serialized leaf holding routing
     * information.
     *
     * @param operationName
     *            - operation name
     * @param data
     *            - Binding data
     * @param contextRef
     *            - leaf context reference
     * @param codec
     *            - specific codec
     * @return insntance of lazy serialized container node with pre-cached serialized leaf
     */
    public static NormalizedNode<?, ?> withContextRef(final SchemaPath operationName, final TreeNode data,
            final LeafNode<?> contextRef, final BindingNormalizedNodeCodecRegistry codec) {
        return new WithContextRef(operationName.getLastComponent(), data, contextRef, codec);
    }

    @Override
    public Map<QName, String> getAttributes() {
        return delegate().getAttributes();
    }

    private ContainerNode delegate() {
        if (domData == null) {
            domData = registry.toNormalizedNodeOperationData(bindingData);
            registry = null;
        }
        return domData;
    }

    @Override
    public final QName getNodeType() {
        return identifier.getNodeType();
    }

    @Override
    public final Collection<DataContainerChild<? extends PathArgument, ?>> getValue() {
        return delegate().getValue();
    }

    @Nonnull
    @Override
    public final NodeIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public Optional<DataContainerChild<? extends PathArgument, ?>> getChild(final PathArgument child) {
        return delegate().getChild(child);
    }

    @Override
    public final Object getAttributeValue(final QName name) {
        return delegate().getAttributeValue(name);
    }

    /**
     * Get binding data.
     *
     * @return binding data.
     */
    public final TreeNode bindingData() {
        return bindingData;
    }

    /**
     * Lazy Serialized Node with pre-cached serialized leaf holding routing information.
     *
     */
    private static final class WithContextRef extends LazySerializedContainerNode {

        private final LeafNode<?> contextRef;

        private WithContextRef(final QName identifier, final TreeNode binding, final LeafNode<?> contextRef,
                final BindingNormalizedNodeCodecRegistry registry) {
            super(identifier, binding, registry);
            this.contextRef = contextRef;
        }

        @Override
        public Optional<DataContainerChild<? extends PathArgument, ?>> getChild(final PathArgument child) {
            /*
             * Use precached value of routing field and do not run full serialization if we are accessing it.
             */
            if (contextRef.getIdentifier().equals(child)) {
                return Optional.of(contextRef);
            }
            return super.getChild(child);
        }
    }

}
