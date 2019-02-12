/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding.javav2.dom.codec.api.BindingNormalizedNodeCachingCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.api.BindingTreeNodeCodec;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeArgument;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
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
public final class LeafNodeCodecContext<D extends TreeNode> extends NodeCodecContext<D> implements NodeContextSupplier {

    private final YangInstanceIdentifier.PathArgument yangIdentifier;
    private final Codec<Object, Object> valueCodec;
    private final Method getter;
    private final DataSchemaNode schema;
    private final Object defaultObject;

    LeafNodeCodecContext(final DataSchemaNode schema, final Codec<Object, Object> codec, final Method getter,
            final SchemaContext schemaContext) {
        this.yangIdentifier = new YangInstanceIdentifier.NodeIdentifier(schema.getQName());
        this.valueCodec = requireNonNull(codec);
        this.getter = requireNonNull(getter);
        this.schema = requireNonNull(schema);

        this.defaultObject = createDefaultObject(schema, valueCodec, schemaContext);
    }

    private static Object createDefaultObject(final DataSchemaNode schema, final Codec<Object, Object> codec,
            final SchemaContext schemaContext) {
        if (schema instanceof LeafSchemaNode) {
            TypeDefinition<?> type = ((LeafSchemaNode) schema).getType();
            Optional<? extends Object> defaultValue = type.getDefaultValue();
            if (defaultValue.isPresent()) {
                if (type instanceof IdentityrefTypeDefinition) {
                    return qnameDomValueFromString(codec, schema, (String) defaultValue.get(), schemaContext);
                }
                return domValueFromString(codec, type, defaultValue.get());
            }

            while (type.getBaseType() != null && !type.getDefaultValue().isPresent()) {
                type = type.getBaseType();
            }

            defaultValue = type.getDefaultValue();
            if (defaultValue.isPresent()) {
                if (type instanceof IdentityrefTypeDefinition) {
                    return qnameDomValueFromString(codec, schema, (String) defaultValue.get(), schemaContext);
                }
                return domValueFromString(codec, type, defaultValue.get());
            }
        }
        return null;
    }

    private static Object qnameDomValueFromString(final Codec<Object, Object> codec, final DataSchemaNode schema,
            final String defaultValue, final SchemaContext schemaContext) {
        final int prefixEndIndex = defaultValue.indexOf(':');
        if (prefixEndIndex != -1) {
            final String defaultValuePrefix = defaultValue.substring(0, prefixEndIndex);

            final Module module = schemaContext.findModule(schema.getQName().getModule()).get();

            if (module.getPrefix().equals(defaultValuePrefix)) {
                return codec.deserialize(QName.create(module.getQNameModule(),
                    defaultValue.substring(prefixEndIndex + 1)));
            }

            final Set<ModuleImport> imports = module.getImports();
            for (final ModuleImport moduleImport : imports) {
                if (moduleImport.getPrefix().equals(defaultValuePrefix)) {
                    final Module importedModule = schemaContext.findModule(moduleImport.getModuleName(),
                        moduleImport.getRevision()).get();
                    return codec.deserialize(QName.create(importedModule.getQNameModule(),
                        defaultValue.substring(prefixEndIndex + 1)));
                }
            }
            return null;
        }

        return codec.deserialize(QName.create(schema.getQName(), defaultValue));
    }

    private static Object domValueFromString(final Codec<Object, Object> codec, final TypeDefinition<?> type,
            final Object defaultValue) {
        final TypeDefinitionAwareCodec<?, ?> typeDefAwareCodec = TypeDefinitionAwareCodec.from(type);
        if (typeDefAwareCodec != null) {
            final Object castedDefaultValue = typeDefAwareCodec.deserialize((String) defaultValue);
            return codec.deserialize(castedDefaultValue);
        }
        // FIXME: BUG-4647 Refactor / redesign this to throw hard error,
        // once BUG-4638 is fixed and will provide proper getDefaultValue implementation.
        return null;
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

    public Method getGetter() {
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
        if (normalizedNode instanceof LeafNode<?>) {
            return valueCodec.deserialize(normalizedNode.getValue());
        }
        if (normalizedNode instanceof LeafSetNode<?>) {
            @SuppressWarnings("unchecked")
            final Collection<LeafSetEntryNode<Object>> domValues = ((LeafSetNode<Object>) normalizedNode).getValue();
            final List<Object> result = new ArrayList<>(domValues.size());
            for (final LeafSetEntryNode<Object> valueNode : domValues) {
                result.add(valueCodec.deserialize(valueNode.getValue()));
            }
            return result;
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

    /**
     * Return the default value object.
     *
     * @return The default value object, or null if the default value is not defined.
     */
    @Nullable
    Object defaultObject() {
        return defaultObject;
    }
}
