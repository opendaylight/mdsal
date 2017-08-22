/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding.javav2.dom.codec.api.BindingTreeCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.api.BindingTreeNodeCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.IdentifiableItemCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.IdentityCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.InstanceIdentifierCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.OperationInputCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.UnionTypeCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.CaseNodeCodecContext;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.ChoiceNodeCodecContext;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.ListNodeCodecContext;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.NotificationCodecContext;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.SchemaRootCodecContext;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.ValueContext;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base.NodeCodecContext.CodecContextFactory;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.value.ValueTypeCodec;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifier;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifierNormalizer;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.javav2.runtime.context.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.javav2.runtime.reflection.BindingReflections;
import org.opendaylight.mdsal.binding.javav2.spec.base.Identifiable;
import org.opendaylight.mdsal.binding.javav2.spec.base.IdentifiableItem;
import org.opendaylight.mdsal.binding.javav2.spec.base.Identifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.Instantiable;
import org.opendaylight.mdsal.binding.javav2.spec.base.Notification;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeArgument;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingStreamEventWriter;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.TreeNodeSerializer;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Binding codec context for holding runtime part and codecs.
 */
@Beta
public final class BindingCodecContext implements CodecContextFactory, BindingTreeCodec, Immutable {

    private static final Logger LOG = LoggerFactory.getLogger(BindingCodecContext.class);

    private final Codec<YangInstanceIdentifier, InstanceIdentifier<?>> instanceIdentifierCodec;
    private final Codec<QName, Class<?>> identityCodec;
    private final BindingNormalizedNodeCodecRegistry registry;
    private final BindingRuntimeContext context;
    private final SchemaRootCodecContext<?> root;

    /**
     * Prepare runtime context and codec registry.
     *
     * @param context
     *            - runtime context
     * @param registry
     *            - binding normalized node codec registry
     */
    public BindingCodecContext(final BindingRuntimeContext context, final BindingNormalizedNodeCodecRegistry registry) {
        this.context = Preconditions.checkNotNull(context, "Binding Runtime Context is required.");
        this.root = SchemaRootCodecContext.create(this);
        this.identityCodec = new IdentityCodec(context);
        this.instanceIdentifierCodec = new InstanceIdentifierCodec(this);
        this.registry = Preconditions.checkNotNull(registry);
    }

    @Override
    public BindingRuntimeContext getRuntimeContext() {
        return context;
    }

    /**
     * Get instance identifier codec.
     *
     * @return instance identifier codec
     */
    public Codec<YangInstanceIdentifier, InstanceIdentifier<?>> getInstanceIdentifierCodec() {
        return instanceIdentifierCodec;
    }

    /**
     * Get identity codec.
     *
     * @return identity codec
     */
    public Codec<QName, Class<?>> getIdentityCodec() {
        return identityCodec;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public TreeNodeSerializer getEventStreamSerializer(final Class<?> type) {
        return registry.getSerializer((Class) type);
    }

    /**
     * Prepare specific writer for binding path.
     *
     * @param path
     *            - binding path
     * @param domWriter
     *            - DOM writer
     * @return entry of DOM path and writer
     */
    public Entry<YangInstanceIdentifier, BindingStreamEventWriter> newWriter(final InstanceIdentifier<?> path,
            final NormalizedNodeStreamWriter domWriter) {
        final List<YangInstanceIdentifier.PathArgument> yangArgs = new LinkedList<>();
        final DataContainerCodecContext<?, ?> codecContext = getCodecContextNode(path, yangArgs);
        return new SimpleEntry<>(YangInstanceIdentifier.create(yangArgs), codecContext.createWriter(domWriter));
    }

    /**
     * Prepare specific writer for binding path without DOM identifier.
     *
     * @param path
     *            - binding path
     * @param domWriter
     *            - DOM writer
     * @return stream event writer for binding path
     */
    public BindingStreamEventWriter newWriterWithoutIdentifier(final InstanceIdentifier<?> path,
            final NormalizedNodeStreamWriter domWriter) {
        return getCodecContextNode(path, null).createWriter(domWriter);
    }

    /**
     * Prepare specific writer for operations.
     *
     * @param operationInputOrOutput
     *            - binding data
     * @param domWriter
     *            - DOM writer
     * @return stream event writer for operation
     */
    public BindingStreamEventWriter newOperationWriter(final Class<? extends Instantiable<?>> operationInputOrOutput,
            final NormalizedNodeStreamWriter domWriter) {
        return root.getOperation(operationInputOrOutput).createWriter(domWriter);
    }

    @SuppressWarnings("rawtypes")
    public BindingStreamEventWriter newNotificationWriter(final Class<? extends Notification> notification,
            final NormalizedNodeStreamWriter domWriter) {
        return root.getNotification(notification).createWriter(domWriter);
    }

    /**
     * Prepare context from Binding and DOM path.
     *
     * @param binding
     *            - binding path
     * @param builder
     *            - DOM path
     *
     * @return context for path
     */
    public DataContainerCodecContext<?, ?> getCodecContextNode(final InstanceIdentifier<?> binding,
            final List<YangInstanceIdentifier.PathArgument> builder) {
        DataContainerCodecContext<?, ?> currentNode = root;
        for (final TreeArgument<?> bindingArg : binding.getPathArguments()) {
            currentNode = currentNode.bindingPathArgumentChild(bindingArg, builder);
            Preconditions.checkArgument(currentNode != null, "Supplied Instance Identifier %s is not valid.", binding);
        }
        return currentNode;
    }

    /**
     * Multi-purpose utility function. Traverse the codec tree, looking for the appropriate codec for the
     * specified {@link YangInstanceIdentifier}. As a side-effect, gather all traversed binding
     * {@link TreeArgument} into the supplied collection.
     *
     * @param dom
     *            {@link YangInstanceIdentifier} which is to be translated
     * @param bindingArguments
     *            Collection for traversed path arguments
     * @return Codec for target node, or @null if the node does not have a binding representation (choice,
     *         case, leaf).
     *
     */
    @Nullable
    public NodeCodecContext<?> getCodecContextNode(final @Nonnull YangInstanceIdentifier dom,
            final @Nullable Collection<TreeArgument<?>> bindingArguments) {
        NodeCodecContext<?> currentNode = root;
        ListNodeCodecContext<?> currentList = null;

        for (final YangInstanceIdentifier.PathArgument domArg : dom.getPathArguments()) {
            Preconditions.checkArgument(currentNode instanceof DataContainerCodecContext<?, ?>,
                    "Unexpected child of non-container node %s", currentNode);
            final DataContainerCodecContext<?, ?> previous = (DataContainerCodecContext<?, ?>) currentNode;
            final NodeCodecContext<?> nextNode = previous.yangPathArgumentChild(domArg);

            /*
             * List representation in YANG Instance Identifier consists of two arguments: first is list as a
             * whole, second is list as an item so if it is /list it means list as whole, if it is /list/list
             * - it is wildcarded and if it is /list/list[key] it is concrete item, all this variations are
             * expressed in Binding Aware Instance Identifier as Item or IdentifiableItem
             */
            if (currentList != null) {
                Preconditions.checkArgument(currentList == nextNode,
                        "List should be referenced two times in YANG Instance Identifier %s", dom);

                // We entered list, so now we have all information to emit
                // list path using second list argument.
                if (bindingArguments != null) {
                    bindingArguments.add(currentList.getBindingPathArgument(domArg));
                }
                currentList = null;
                currentNode = nextNode;
            } else if (nextNode instanceof ListNodeCodecContext) {
                // We enter list, we do not update current Node yet,
                // since we need to verify
                currentList = (ListNodeCodecContext<?>) nextNode;
            } else if (nextNode instanceof ChoiceNodeCodecContext) {
                // We do not add path argument for choice, since
                // it is not supported by binding instance identifier.
                currentNode = nextNode;
            } else if (nextNode instanceof DataContainerCodecContext<?, ?>) {
                if (bindingArguments != null) {
                    bindingArguments.add(((DataContainerCodecContext<?, ?>) nextNode).getBindingPathArgument(domArg));
                }
                currentNode = nextNode;
            } else if (nextNode instanceof LeafNodeCodecContext) {
                LOG.debug("Instance identifier referencing a leaf is not representable (%s)", dom);
                return null;
            }
        }

        // Algorithm ended in list as whole representation
        // we sill need to emit identifier for list
        if (currentNode instanceof ChoiceNodeCodecContext) {
            LOG.debug("Instance identifier targeting a choice is not representable (%s)", dom);
            return null;
        }
        if (currentNode instanceof CaseNodeCodecContext) {
            LOG.debug("Instance identifier targeting a case is not representable (%s)", dom);
            return null;
        }

        if (currentList != null) {
            if (bindingArguments != null) {
                bindingArguments.add(currentList.getBindingPathArgument(null));
            }
            return currentList;
        }
        return currentNode;
    }

    /**
     * Get notification codec according to notification schema path.
     *
     * @param notification
     *            - schema path of notification
     * @return notification codec context
     */
    public NotificationCodecContext<?> getNotificationContext(final SchemaPath notification) {
        return root.getNotification(notification);
    }

    /**
     * Get operation input codec.
     *
     * @param path
     *            - path of input data of operation
     * @return operation input codec
     */
    public OperationInputCodec<?> getOperationInputCodec(final SchemaPath path) {
        return root.getOperation(path);
    }

    @Override
    public ImmutableMap<String, LeafNodeCodecContext<?>> getLeafNodes(final Class<?> parentClass,
            final DataNodeContainer childSchema) {
        final Map<String, DataSchemaNode> getterToLeafSchema = new HashMap<>();
        for (final DataSchemaNode leaf : childSchema.getChildNodes()) {
            if (leaf instanceof TypedSchemaNode) {
                getterToLeafSchema.put(getGetterName(leaf.getQName(), ((TypedSchemaNode) leaf).getType()), leaf);
            }
        }
        return getLeafNodesUsingReflection(parentClass, getterToLeafSchema);
    }

    private static String getGetterName(final QName qName, final TypeDefinition<?> typeDef) {
        final String suffix =
                JavaIdentifierNormalizer.normalizeSpecificIdentifier(qName.getLocalName(), JavaIdentifier.CLASS);
        if (typeDef instanceof BooleanTypeDefinition || typeDef instanceof EmptyTypeDefinition) {
            return "is" + suffix;
        }
        return "get" + suffix;
    }

    private ImmutableMap<String, LeafNodeCodecContext<?>> getLeafNodesUsingReflection(final Class<?> parentClass,
            final Map<String, DataSchemaNode> getterToLeafSchema) {
        final Map<String, LeafNodeCodecContext<?>> leaves = new HashMap<>();
        for (final Method method : parentClass.getMethods()) {
            if (method.getParameterTypes().length == 0) {
                final DataSchemaNode schema = getterToLeafSchema.get(method.getName());
                final Class<?> valueType;
                if (schema instanceof LeafSchemaNode) {
                    valueType = method.getReturnType();
                } else if (schema instanceof LeafListSchemaNode) {
                    final Type genericType = ClassLoaderUtils.getFirstGenericParameter(method.getGenericReturnType());

                    if (genericType instanceof Class<?>) {
                        valueType = (Class<?>) genericType;
                    } else if (genericType instanceof ParameterizedType) {
                        valueType = (Class<?>) ((ParameterizedType) genericType).getRawType();
                    } else {
                        throw new IllegalStateException("Unexpected return type " + genericType);
                    }
                } else {
                    continue; // We do not have schema for leaf, so we will ignore it (eg. getClass,
                              // getImplementedInterface).
                }
                final Codec<Object, Object> codec = getCodec(valueType, schema);
                final LeafNodeCodecContext<?> leafNode =
                        new LeafNodeCodecContext<>(schema, codec, method, context.getSchemaContext());
                leaves.put(schema.getQName().getLocalName(), leafNode);
            }
        }
        return ImmutableMap.copyOf(leaves);
    }

    private Codec<Object, Object> getCodec(final Class<?> valueType, final DataSchemaNode schema) {
        Preconditions.checkArgument(schema instanceof TypedSchemaNode, "Unsupported leaf node type %s", schema);

        return getCodec(valueType, ((TypedSchemaNode) schema).getType());
    }

    /**
     * Get specific codec for binding class by type.
     *
     * @param valueType
     *            - binding class
     * @param instantiatedType
     *            - type definition
     * @return specific codec
     */
    public Codec<Object, Object> getCodec(final Class<?> valueType, final TypeDefinition<?> instantiatedType) {
        if (Class.class.equals(valueType)) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            final Codec<Object, Object> casted = (Codec) identityCodec;
            return casted;
        } else if (InstanceIdentifier.class.equals(valueType)) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            final Codec<Object, Object> casted = (Codec) instanceIdentifierCodec;
            return casted;
        } else if (Boolean.class.equals(valueType)) {
            if (instantiatedType instanceof EmptyTypeDefinition) {
                return ValueTypeCodec.EMPTY_CODEC;
            }
        } else if (BindingReflections.isBindingClass(valueType)) {
            return getCodecForBindingClass(valueType, instantiatedType);
        }
        return ValueTypeCodec.NOOP_CODEC;
    }

    private Codec<Object, Object> getCodecForBindingClass(final Class<?> valueType, final TypeDefinition<?> typeDef) {
        if (typeDef instanceof IdentityrefTypeDefinition) {
            return ValueTypeCodec.encapsulatedValueCodecFor(valueType, identityCodec);
        } else if (typeDef instanceof InstanceIdentifierTypeDefinition) {
            return ValueTypeCodec.encapsulatedValueCodecFor(valueType, instanceIdentifierCodec);
        } else if (typeDef instanceof UnionTypeDefinition) {
            final Callable<UnionTypeCodec> loader =
                    UnionTypeCodec.loader(valueType, (UnionTypeDefinition) typeDef, this);
            try {
                return loader.call();
            } catch (final Exception e) {
                throw new IllegalStateException("Unable to load codec for " + valueType, e);
            }
        } else if (typeDef instanceof LeafrefTypeDefinition) {
            final Entry<GeneratedType, Object> typeWithSchema = context.getTypeWithSchema(valueType);
            final Object schema = typeWithSchema.getValue();
            Preconditions.checkState(schema instanceof TypeDefinition<?>);
            return getCodec(valueType, (TypeDefinition<?>) schema);
        }
        return ValueTypeCodec.getCodecFor(valueType, typeDef);
    }

    @Override
    public Codec<NodeIdentifierWithPredicates, IdentifiableItem<?, ?>> getPathArgumentCodec(final Class<?> listClz,
            final ListSchemaNode schema) {
        final Class<? extends Identifier<?>> identifier =
                ClassLoaderUtils.findFirstGenericArgument(listClz, Identifiable.class);
        final Map<QName, ValueContext> valueCtx = new HashMap<>();
        for (final LeafNodeCodecContext<?> leaf : getLeafNodes(identifier, schema).values()) {
            final QName name = leaf.getDomPathArgument().getNodeType();
            valueCtx.put(name, new ValueContext(identifier, leaf));
        }
        return new IdentifiableItemCodec(schema, identifier, listClz, valueCtx);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends TreeNode> BindingTreeNodeCodec<T> getSubtreeCodec(final InstanceIdentifier<T> path) {
        return (BindingTreeNodeCodec<T>) getCodecContextNode(path, null);
    }

    @Nullable
    @Override
    public BindingTreeNodeCodec<?> getSubtreeCodec(final YangInstanceIdentifier path) {
        return getCodecContextNode(path, null);
    }

    @Nullable
    @Override
    public BindingTreeNodeCodec<?> getSubtreeCodec(final SchemaPath path) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
