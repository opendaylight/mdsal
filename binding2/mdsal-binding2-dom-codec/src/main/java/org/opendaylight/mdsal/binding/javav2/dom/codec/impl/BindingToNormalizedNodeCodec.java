/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.impl;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableBiMap;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.javav2.dom.codec.api.BindingTreeCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.api.BindingTreeNodeCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.api.factory.BindingTreeCodecFactory;
import org.opendaylight.mdsal.binding.javav2.dom.codec.api.serializer.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.javav2.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.mdsal.binding.javav2.runtime.context.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.javav2.runtime.context.ModuleInfoBackedContext;
import org.opendaylight.mdsal.binding.javav2.runtime.reflection.BindingReflections;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.Notification;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeArgument;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.YangModuleInfo;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.codec.DeserializationException;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.OperationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Codec for serialize/deserialize Binding and DOM data.
 */
@Beta
public final class BindingToNormalizedNodeCodec
        implements BindingTreeCodecFactory, BindingNormalizedNodeSerializer, SchemaContextListener, AutoCloseable {

    private static final long WAIT_DURATION_SEC = 5;
    private static final Logger LOG = LoggerFactory.getLogger(BindingToNormalizedNodeCodec.class);

    private final BindingNormalizedNodeCodecRegistry codecRegistry;
    private final GeneratedClassLoadingStrategy classLoadingStrategy;
    private final FutureSchema futureSchema;
    private final LoadingCache<InstanceIdentifier<?>, YangInstanceIdentifier> iiCache = CacheBuilder.newBuilder()
            .softValues().build(new CacheLoader<InstanceIdentifier<?>, YangInstanceIdentifier>() {

                @Override
                public YangInstanceIdentifier load(@Nonnull final InstanceIdentifier<?> key) throws Exception {
                    return toYangInstanceIdentifierBlocking(key);
                }

            });

    private volatile BindingRuntimeContext runtimeContext;

    /**
     * Init class without waiting for schema.
     *
     * @param classLoadingStrategy
     *            - class loader
     * @param codecRegistry
     *            - codec registry
     */
    public BindingToNormalizedNodeCodec(final GeneratedClassLoadingStrategy classLoadingStrategy,
            final BindingNormalizedNodeCodecRegistry codecRegistry) {
        this(classLoadingStrategy, codecRegistry, false);
    }

    /**
     * Init class with waiting for schema.
     *
     * @param classLoadingStrategy
     *            - class loader
     * @param codecRegistry
     *            - codec registry
     * @param waitForSchema
     *            - boolean of waiting for schema
     */
    public BindingToNormalizedNodeCodec(final GeneratedClassLoadingStrategy classLoadingStrategy,
            final BindingNormalizedNodeCodecRegistry codecRegistry, final boolean waitForSchema) {
        this.classLoadingStrategy = Preconditions.checkNotNull(classLoadingStrategy, "classLoadingStrategy");
        this.codecRegistry = Preconditions.checkNotNull(codecRegistry, "codecRegistry");
        this.futureSchema = waitForSchema ? new FutureSchema(WAIT_DURATION_SEC, TimeUnit.SECONDS) : null;
    }

    /**
     * Translates supplied Binding Instance Identifier into NormalizedNode instance identifier with waiting
     * for schema.
     *
     * @param binding
     *            - Binding Instance Identifier
     * @return DOM Instance Identifier
     */
    public YangInstanceIdentifier toYangInstanceIdentifierBlocking(final InstanceIdentifier<? extends TreeNode> binding) {
        try {
            return codecRegistry.toYangInstanceIdentifier(binding);
        } catch (final MissingSchemaException e) {
            waitForSchema(decompose(binding), e);
            return codecRegistry.toYangInstanceIdentifier(binding);
        }
    }

    /**
     * Translates supplied Binding Instance Identifier into NormalizedNode instance identifier.
     *
     * @param binding
     *            - Binding Instance Identifier
     * @return DOM Instance Identifier
     * @throws IllegalArgumentException
     *             If supplied Instance Identifier is not valid.
     */
    public YangInstanceIdentifier toNormalized(final InstanceIdentifier<? extends TreeNode> binding) {
        return codecRegistry.toYangInstanceIdentifier(binding);
    }

    @Nullable
    @Override
    public YangInstanceIdentifier toYangInstanceIdentifier(@Nonnull final InstanceIdentifier<?> binding) {
        return codecRegistry.toYangInstanceIdentifier(binding);
    }

    /**
     * Get cached DOM identifier of Binding identifier.
     *
     * @param binding
     *            - binding identifier
     * @return DOM identifier
     */
    public YangInstanceIdentifier toYangInstanceIdentifierCached(final InstanceIdentifier<?> binding) {
        return iiCache.getUnchecked(binding);
    }

    @Nullable
    @Override
    public <T extends TreeNode> Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>
            toNormalizedNode(final InstanceIdentifier<T> path, final T data) {
        try {
            return codecRegistry.toNormalizedNode(path, data);
        } catch (final MissingSchemaException e) {
            waitForSchema(decompose(path), e);
            return codecRegistry.toNormalizedNode(path, data);
        }
    }

    /**
     * Converts Binding Map.Entry to DOM Map.Entry.
     *
     * <p>
     * Same as {@link #toNormalizedNode(InstanceIdentifier, TreeNode)}.
     *
     * @param binding
     *            Map Entry with InstanceIdentifier as key and DataObject as value.
     * @return DOM Map Entry with {@link YangInstanceIdentifier} as key and {@link NormalizedNode} as value.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Nullable
    public Entry<YangInstanceIdentifier, NormalizedNode<?, ?>>
            toNormalizedNode(final Entry<InstanceIdentifier<? extends TreeNode>, TreeNode> binding) {
        return toNormalizedNode((InstanceIdentifier) binding.getKey(), binding.getValue());
    }

    @Nullable
    @Override
    public Entry<InstanceIdentifier<?>, TreeNode> fromNormalizedNode(@Nonnull final YangInstanceIdentifier path,
            final NormalizedNode<?, ?> data) {
        return codecRegistry.fromNormalizedNode(path, data);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Nullable
    @Override
    public Notification fromNormalizedNodeNotification(@Nonnull final SchemaPath path,
            @Nonnull final ContainerNode data) {
        return codecRegistry.fromNormalizedNodeNotification(path, data);
    }

    @Nullable
    @Override
    public TreeNode fromNormalizedNodeOperationData(@Nonnull final SchemaPath path, @Nonnull final ContainerNode data) {
        return codecRegistry.fromNormalizedNodeOperationData(path, data);
    }

    @Nullable
    @Override
    public InstanceIdentifier<?> fromYangInstanceIdentifier(@Nonnull final YangInstanceIdentifier dom) {
        return codecRegistry.fromYangInstanceIdentifier(dom);
    }

    @SuppressWarnings("rawtypes")
    @Nonnull
    @Override
    public ContainerNode toNormalizedNodeNotification(@Nonnull final Notification data) {
        return codecRegistry.toNormalizedNodeNotification(data);
    }

    @Nonnull
    @Override
    public ContainerNode toNormalizedNodeOperationData(@Nonnull final TreeNode data) {
        return codecRegistry.toNormalizedNodeOperationData(data);
    }

    /**
     * Returns a Binding-Aware instance identifier from normalized instance-identifier if it is possible to
     * create representation.
     *
     * <p>
     * Returns Optional.absent for cases where target is mixin node except augmentation.
     *
     */
    public Optional<InstanceIdentifier<? extends TreeNode>> toBinding(final YangInstanceIdentifier normalized)
            throws DeserializationException {
        try {
            return Optional.fromNullable(codecRegistry.fromYangInstanceIdentifier(normalized));
        } catch (final IllegalArgumentException e) {
            return Optional.absent();
        }
    }

    /**
     * DOM to Binding.
     *
     * @param normalized
     *            - DOM object
     * @return Binding object
     * @throws DeserializationException
     */
    @SuppressWarnings("unchecked")
    public Optional<Entry<InstanceIdentifier<? extends TreeNode>, TreeNode>>
            toBinding(@Nonnull final Entry<YangInstanceIdentifier, ? extends NormalizedNode<?, ?>> normalized)
                    throws DeserializationException {
        try {
            final Entry<InstanceIdentifier<? extends TreeNode>, TreeNode> binding =
                    Entry.class.cast(codecRegistry.fromNormalizedNode(normalized.getKey(), normalized.getValue()));
            return Optional.fromNullable(binding);
        } catch (final IllegalArgumentException e) {
            return Optional.absent();
        }
    }

    @Override
    public void onGlobalContextUpdated(final SchemaContext arg0) {
        runtimeContext = BindingRuntimeContext.create(classLoadingStrategy, arg0);
        codecRegistry.onBindingRuntimeContextUpdated(runtimeContext);
        if (futureSchema != null) {
            futureSchema.onRuntimeContextUpdated(runtimeContext);
        }
    }

    /**
     * Prepare deserialize function of Binding identifier to DOM.
     *
     * @param path
     *            - Binding identifier
     * @return DOM function
     */
    public <T extends TreeNode> Function<Optional<NormalizedNode<?, ?>>, Optional<T>>
            deserializeFunction(final InstanceIdentifier<T> path) {
        return codecRegistry.deserializeFunction(path);
    }

    /**
     * Get codec registry.
     *
     * @return codec registry
     */
    public BindingNormalizedNodeCodecRegistry getCodecRegistry() {
        return codecRegistry;
    }

    @Override
    public void close() {
        // NOOP Intentionally
    }

    /**
     * Get codec factory.
     *
     * @return codec factory
     */
    public BindingNormalizedNodeCodecRegistry getCodecFactory() {
        return codecRegistry;
    }

    /**
     * Resolve method with path of specific RPC as binding object.
     *
     * @param key
     *            - RPC as binding object
     * @return map of method with path of specific RPC
     */
    public ImmutableBiMap<Method, SchemaPath> getRPCMethodToSchemaPath(final Class<?> key) {
        final Module module = getModuleBlocking(key);
        final ImmutableBiMap.Builder<Method, SchemaPath> ret = ImmutableBiMap.builder();
        try {
            for (final RpcDefinition rpcDef : module.getRpcs()) {
                YangModuleInfo modInfo;
                try {
                    modInfo = BindingReflections.getModuleInfo(key);
                } catch (final Exception e) {
                    throw new IllegalStateException(e);
                }

                ((ModuleInfoBackedContext) classLoadingStrategy).registerModuleInfo(modInfo);
                final Method method = runtimeContext.findRpcMethod(key, rpcDef);
                ret.put(method, rpcDef.getPath());
            }
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException("RPC defined in model does not have representation in generated class.", e);
        }
        return ret.build();
    }

    /**
     * Resolve method with path of specific Action as binding object.
     *
     * @param key
     *            - action as binding object
     * @return map of method with path of specific action
     */
    public ImmutableBiMap<Method, SchemaPath> getActionMethodToSchemaPath(final Class<?> key) {
        final Module module = getModuleBlocking(key);

        final ImmutableBiMap.Builder<Method, SchemaPath> ret = ImmutableBiMap.builder();
        try {
            for (final ActionDefinition actionDefinition : runtimeContext.getSchemaContext().getActions()) {
                final QName qName = actionDefinition.getQName();
                if (qName.getModule().equals(module.getQNameModule())) {
                    final Method method = runtimeContext.findOperationMethod(key, actionDefinition);
                    ret.put(method, actionDefinition.getPath());
                }
            }
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException("Action defined in model does not have representation in generated class.",
                    e);
        }
        return ret.build();
    }


    /**
     * Resolve method with definition of specific RPC as binding object.
     *
     * @param key
     *            - RPC as binding object
     * @return map of method with definition of specific RPC
     */
    public ImmutableBiMap<Method, OperationDefinition> getRPCMethodToSchema(final Class<?> key) {
        final Module module = getModuleBlocking(key);
        final ImmutableBiMap.Builder<Method, OperationDefinition> ret = ImmutableBiMap.builder();
        try {
            for (final RpcDefinition rpcDef : module.getRpcs()) {
                final Method method = runtimeContext.findRpcMethod(key, rpcDef);
                ret.put(method, rpcDef);
            }
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException("RPC defined in model does not have representation in generated class.", e);
        }
        return ret.build();
    }

    /**
     * Resolve method with definition of specific action as binding object.
     *
     * @param key
     *            - action as binding object
     * @return map of method with definition of specific action
     */
    public ImmutableBiMap<Method, OperationDefinition> getActionMethodToSchema(final Class<?> key) {
        final Module module = getModuleBlocking(key);
        final ImmutableBiMap.Builder<Method, OperationDefinition> ret = ImmutableBiMap.builder();
        try {
            for (final ActionDefinition actionDefinition : runtimeContext.getSchemaContext().getActions()) {
                final QName qName = actionDefinition.getQName();
                if (qName.getModule().equals(module.getQNameModule())) {
                    final Method method = runtimeContext.findOperationMethod(key, actionDefinition);
                    ret.put(method, actionDefinition);
                }
            }
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException("Action defined in model does not have representation in generated class.",
                    e);
        }
        return ret.build();
    }

    private Module getModuleBlocking(final Class<?> modeledClass) {
        final QNameModule moduleName = BindingReflections.getQNameModule(modeledClass);
        final URI namespace = moduleName.getNamespace();
        final Date revision = moduleName.getRevision();
        BindingRuntimeContext localRuntimeContext = runtimeContext;
        Module module = localRuntimeContext == null ? null
                : localRuntimeContext.getSchemaContext().findModuleByNamespaceAndRevision(namespace, revision);
        if (module == null && futureSchema != null && futureSchema.waitForSchema(namespace, revision)) {
            localRuntimeContext = runtimeContext;
            Preconditions.checkState(localRuntimeContext != null, "BindingRuntimeContext is not available.");
            module = localRuntimeContext.getSchemaContext().findModuleByNamespaceAndRevision(namespace, revision);
        }
        Preconditions.checkState(module != null, "Schema for %s is not available.", modeledClass);
        return module;
    }

    private void waitForSchema(final Collection<Class<?>> binding, final MissingSchemaException exception) {
        if (futureSchema != null) {
            LOG.warn("Blocking thread to wait for schema convergence updates for {} {}", futureSchema.getDuration(),
                    futureSchema.getUnit());
            if (!futureSchema.waitForSchema(binding)) {
                return;
            }
        }
        throw exception;
    }

    @Override
    public BindingTreeCodec create(final BindingRuntimeContext context) {
        return codecRegistry.create(context);
    }

    @Override
    public BindingTreeCodec create(final SchemaContext context, final Class<?>... bindingClasses) {
        return codecRegistry.create(context, bindingClasses);
    }

    /**
     * Get subtree codec of DOM identifier.
     *
     * @param domIdentifier
     *            - DOM identifier
     * @return codec for subtree
     */
    @Nonnull
    public Map.Entry<InstanceIdentifier<?>, BindingTreeNodeCodec<?>>
            getSubtreeCodec(final YangInstanceIdentifier domIdentifier) {

        final BindingTreeCodec currentCodecTree = codecRegistry.getCodecContext();
        final InstanceIdentifier<?> bindingPath = codecRegistry.fromYangInstanceIdentifier(domIdentifier);
        Preconditions.checkArgument(bindingPath != null);
        /**
         * If we are able to deserialize YANG instance identifier, getSubtreeCodec must return non-null value.
         */
        final BindingTreeNodeCodec<?> codecContext = currentCodecTree.getSubtreeCodec(bindingPath);
        return new SimpleEntry<>(bindingPath, codecContext);
    }

    /**
     * Get specific notification classes as Binding objects.
     *
     * @param interested
     *            - set of specific notifications paths
     * @return notification as Binding objects according to input set of their DOM paths
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Set<Class<? extends Notification>> getNotificationClasses(final Set<SchemaPath> interested) {
        final Set<Class<? extends Notification>> result = new HashSet<>();
        final Set<NotificationDefinition> knownNotifications = runtimeContext.getSchemaContext().getNotifications();
        for (final NotificationDefinition notification : knownNotifications) {
            if (interested.contains(notification.getPath())) {
                try {
                    result.add((Class<? extends Notification>) runtimeContext.getClassForSchema(notification));
                } catch (final IllegalStateException e) {
                    // Ignore
                    LOG.warn("Class for {} is currently not known.", notification.getPath(), e);
                }
            }
        }
        return result;
    }

    private static Collection<Class<?>> decompose(final InstanceIdentifier<?> path) {
        final Set<Class<?>> clazzes = new HashSet<>();
        for (final TreeArgument<?> arg : path.getPathArguments()) {
            clazzes.add(arg.getType());
        }
        return clazzes;
    }

    /**
     * Resolve DOM object on specific DOM identifier.
     *
     * @param parentPath
     *            - DOM identifier
     * @return DOM object
     */
    public NormalizedNode<?, ?> instanceIdentifierToNode(final YangInstanceIdentifier parentPath) {
        return ImmutableNodes.fromInstanceId(runtimeContext.getSchemaContext(), parentPath);
    }

    /**
     * Get default DOM object on path for list.
     *
     * @param parentMapPath
     *            - path
     * @return specific DOM object
     */
    public NormalizedNode<?, ?> getDefaultNodeFor(final YangInstanceIdentifier parentMapPath) {
        final BindingTreeNodeCodec<?> mapCodec = codecRegistry.getCodecContext().getSubtreeCodec(parentMapPath);
        final Object schema = mapCodec.getSchema();
        if (schema instanceof ListSchemaNode) {
            final ListSchemaNode castedSchema = (ListSchemaNode) schema;
            if (castedSchema.isUserOrdered()) {
                return Builders.orderedMapBuilder(castedSchema).build();
            } else {
                return Builders.mapBuilder(castedSchema).build();
            }
        }
        throw new IllegalArgumentException("Path does not point to list schema node");
    }

    /**
     * Binding subtree identifiers to DOM subtree identifiers.
     *
     * @param subtrees
     *            - binding subtree
     * @return DOM subtree
     */
    public Collection<DOMDataTreeIdentifier>
            toDOMDataTreeIdentifiers(final Collection<DataTreeIdentifier<?>> subtrees) {
        final Set<DOMDataTreeIdentifier> ret = new HashSet<>(subtrees.size());

        for (final DataTreeIdentifier<?> subtree : subtrees) {
            ret.add(toDOMDataTreeIdentifier(subtree));
        }
        return ret;
    }

    /**
     * Create new DOM data tree identifier from Binding data tree identifier.
     *
     * @param path
     *            - binding data tree identifier
     * @return DOM data tree identifier
     */
    public DOMDataTreeIdentifier toDOMDataTreeIdentifier(final DataTreeIdentifier<?> path) {
        final YangInstanceIdentifier domPath = toYangInstanceIdentifierBlocking(path.getRootIdentifier());
        return new DOMDataTreeIdentifier(path.getDatastoreType(), domPath);
    }
}

