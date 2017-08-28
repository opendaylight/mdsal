/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding.javav2.dom.codec.api.BindingNormalizedNodeCachingCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.api.BindingTreeNodeCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base.NodeCodecContext;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base.NodeContextSupplier;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeArgument;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AnyXmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;

@Beta
public final class AnyxmlNodeCodecContext<D extends TreeNode> extends NodeCodecContext<D> implements NodeContextSupplier {

    private final YangInstanceIdentifier.PathArgument yangIdentifier;
    private final Codec<Object, Object> valueCodec;
    private final Method getter;
    private final DataSchemaNode schema;

    AnyxmlNodeCodecContext(final DataSchemaNode schema, final Codec<Object, Object> codec, final Method getter,
                           final SchemaContext schemaContext) {
        this.yangIdentifier = new YangInstanceIdentifier.NodeIdentifier(schema.getQName());
        this.valueCodec = Preconditions.checkNotNull(codec);
        this.getter = Preconditions.checkNotNull(getter);
        this.schema = Preconditions.checkNotNull(schema);
    }

    @Override
    public YangInstanceIdentifier.PathArgument getDomPathArgument() {
        return yangIdentifier;
    }

    public Codec<Object, Object> getValueCodec() {
        return valueCodec;
    }

    @Nonnull
    @Override
    public D deserialize(@Nonnull final NormalizedNode<?, ?> normalizedNode) {
        throw new UnsupportedOperationException("Leaf can not be deserialized to TreeNode");
    }

    @Nonnull
    @Override
    public NodeCodecContext<?> get() {
        return this;
    }

    public final Method getGetter() {
        return getter;
    }

    @Nonnull
    @Override
    public BindingTreeNodeCodec<?> bindingPathArgumentChild(@Nonnull final TreeArgument<?> arg,
            final List<YangInstanceIdentifier.PathArgument> builder) {
        throw new IllegalArgumentException("Leaf does not have children");
    }

    @Nonnull
    @Override
    public BindingNormalizedNodeCachingCodec<D> createCachingCodec(
            @Nonnull final ImmutableCollection<Class<? extends TreeNode>> cacheSpecifier) {
        throw new UnsupportedOperationException("Leaves does not support caching codec.");
    }

    @Nonnull
    @Override
    public Class<D> getBindingClass() {
        throw new UnsupportedOperationException("Leaf does not have DataObject representation");
    }

    @Nonnull
    @Override
    public NormalizedNode<?, ?> serialize(@Nonnull final D data) {
        throw new UnsupportedOperationException("Separate serialization of leaf node is not supported.");
    }

    @Override
    public void writeAsNormalizedNode(final D data, final NormalizedNodeStreamWriter writer) {
        throw new UnsupportedOperationException("Separate serialization of leaf node is not supported.");
    }

    @Nonnull
    @Override
    public <E extends TreeNode> BindingTreeNodeCodec<E> streamChild(@Nonnull final Class<E> childClass) {
        throw new IllegalArgumentException("Leaf does not have children");
    }

    @Override
    public <E extends TreeNode> Optional<? extends BindingTreeNodeCodec<E>> possibleStreamChild(
            @Nonnull final Class<E> childClass) {
        throw new IllegalArgumentException("Leaf does not have children");
    }

    @Nonnull
    @Override
    public BindingTreeNodeCodec<?> yangPathArgumentChild(final YangInstanceIdentifier.PathArgument child) {
        throw new IllegalArgumentException("Leaf does not have children");
    }

    @Override
    protected Object deserializeObject(final NormalizedNode<?, ?> normalizedNode) {
        if (normalizedNode instanceof AnyXmlNode) {
            return valueCodec.deserialize(normalizedNode.getValue());
        }
        return null;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public TreeArgument deserializePathArgument(final YangInstanceIdentifier.PathArgument arg) {
        Preconditions.checkArgument(getDomPathArgument().equals(arg));
        return null;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public YangInstanceIdentifier.PathArgument serializePathArgument(final TreeArgument arg) {
        return getDomPathArgument();
    }

    @Nonnull
    @Override
    public DataSchemaNode getSchema() {
        return schema;
    }
}