/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javassist.ClassPool;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTreeFactory;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.dom.codec.gen.impl.StreamWriterGenerator;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.dom.codec.impl.MissingSchemaException;
import org.opendaylight.mdsal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.mdsal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.generator.util.JavassistUtils;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.codec.DeserializationException;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A combinations of {@link BindingCodecTreeFactory} and {@link BindingNormalizedNodeSerializer}, with internal
 * caching of instance identifiers.
 *
 * <p>
 * NOTE: this class is non-final to allow controller adapter migration without duplicated code.
 */
public class BindingToNormalizedNodeCodec implements BindingCodecTreeFactory,
        BindingNormalizedNodeSerializer, SchemaContextListener, AutoCloseable {

    private static final long WAIT_DURATION_SEC = 5;
    private static final Logger LOG = LoggerFactory.getLogger(BindingToNormalizedNodeCodec.class);

    private final LoadingCache<InstanceIdentifier<?>, YangInstanceIdentifier> iiCache = CacheBuilder.newBuilder()
            .softValues().build(new CacheLoader<InstanceIdentifier<?>, YangInstanceIdentifier>() {
                @Override
                public YangInstanceIdentifier load(@Nonnull final InstanceIdentifier<?> key) {
                    return toYangInstanceIdentifierBlocking(key);
                }
            });
    private final BindingNormalizedNodeCodecRegistry codecRegistry;
    private final ClassLoadingStrategy classLoadingStrategy;
    private final FutureSchema futureSchema;
    private ListenerRegistration<?> listenerRegistration;

    public BindingToNormalizedNodeCodec(final ClassLoadingStrategy classLoadingStrategy,
            final BindingNormalizedNodeCodecRegistry codecRegistry) {
        this(classLoadingStrategy, codecRegistry, false);
    }

    public BindingToNormalizedNodeCodec(final ClassLoadingStrategy classLoadingStrategy,
            final BindingNormalizedNodeCodecRegistry codecRegistry, final boolean waitForSchema) {
        this.classLoadingStrategy = Preconditions.checkNotNull(classLoadingStrategy, "classLoadingStrategy");
        this.codecRegistry = Preconditions.checkNotNull(codecRegistry, "codecRegistry");
        this.futureSchema = FutureSchema.create(WAIT_DURATION_SEC, TimeUnit.SECONDS, waitForSchema);
    }

    public static BindingToNormalizedNodeCodec newInstance(final ClassLoadingStrategy classLoadingStrategy,
            final DOMSchemaService schemaService) {
        final BindingNormalizedNodeCodecRegistry codecRegistry = new BindingNormalizedNodeCodecRegistry(
                StreamWriterGenerator.create(JavassistUtils.forClassPool(ClassPool.getDefault())));
        BindingToNormalizedNodeCodec instance = new BindingToNormalizedNodeCodec(
                classLoadingStrategy, codecRegistry, true);
        instance.listenerRegistration = schemaService.registerSchemaContextListener(instance);
        return instance;
    }

    protected YangInstanceIdentifier toYangInstanceIdentifierBlocking(
            final InstanceIdentifier<? extends DataObject> binding) {
        try {
            return codecRegistry.toYangInstanceIdentifier(binding);
        } catch (final MissingSchemaException e) {
            waitForSchema(decompose(binding), e);
            return codecRegistry.toYangInstanceIdentifier(binding);
        }
    }

    /**
     * Translates supplied Binding Instance Identifier into NormalizedNode
     * instance identifier.
     *
     * @param binding
     *            Binding Instance Identifier
     * @return DOM Instance Identifier
     * @throws IllegalArgumentException
     *             If supplied Instance Identifier is not valid.
     */
    public final YangInstanceIdentifier toNormalized(final InstanceIdentifier<? extends DataObject> binding) {
        return codecRegistry.toYangInstanceIdentifier(binding);
    }

    @Override
    public final YangInstanceIdentifier toYangInstanceIdentifier(@Nonnull final InstanceIdentifier<?> binding) {
        return codecRegistry.toYangInstanceIdentifier(binding);
    }

    protected YangInstanceIdentifier toYangInstanceIdentifierCached(final InstanceIdentifier<?> binding) {
        return iiCache.getUnchecked(binding);
    }

    @Override
    public final <T extends DataObject> Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> toNormalizedNode(
            final InstanceIdentifier<T> path, final T data) {
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
     * Same as {@link #toNormalizedNode(InstanceIdentifier, DataObject)}.
     *
     * @param binding Map Entry with InstanceIdentifier as key and DataObject as value.
     * @return DOM Map Entry with {@link YangInstanceIdentifier} as key and {@link NormalizedNode}
     *         as value.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public final Entry<YangInstanceIdentifier, NormalizedNode<?, ?>> toNormalizedNode(
            final Entry<InstanceIdentifier<? extends DataObject>, DataObject> binding) {
        return toNormalizedNode((InstanceIdentifier) binding.getKey(), binding.getValue());
    }

    @Override
    public final Entry<InstanceIdentifier<?>, DataObject> fromNormalizedNode(@Nonnull final YangInstanceIdentifier path,
            final NormalizedNode<?, ?> data) {
        return codecRegistry.fromNormalizedNode(path, data);
    }

    @Override
    public final Notification fromNormalizedNodeNotification(@Nonnull final SchemaPath path,
            @Nonnull final ContainerNode data) {
        return codecRegistry.fromNormalizedNodeNotification(path, data);
    }

    @Override
    public final DataObject fromNormalizedNodeRpcData(@Nonnull final SchemaPath path,
            @Nonnull final ContainerNode data) {
        return codecRegistry.fromNormalizedNodeRpcData(path, data);
    }

    @Override
    public final InstanceIdentifier<?> fromYangInstanceIdentifier(@Nonnull final YangInstanceIdentifier dom) {
        return codecRegistry.fromYangInstanceIdentifier(dom);
    }

    @Override
    public final ContainerNode toNormalizedNodeNotification(@Nonnull final Notification data) {
        return codecRegistry.toNormalizedNodeNotification(data);
    }

    @Override
    public final ContainerNode toNormalizedNodeRpcData(@Nonnull final DataContainer data) {
        return codecRegistry.toNormalizedNodeRpcData(data);
    }

    /**
     * Returns a Binding-Aware instance identifier from normalized
     * instance-identifier if it is possible to create representation.
     *
     * <p>
     * Returns Optional.absent for cases where target is mixin node except
     * augmentation.
     */
    public final Optional<InstanceIdentifier<? extends DataObject>> toBinding(final YangInstanceIdentifier normalized)
                    throws DeserializationException {
        try {
            return Optional.fromNullable(codecRegistry.fromYangInstanceIdentifier(normalized));
        } catch (final IllegalArgumentException e) {
            return Optional.absent();
        }
    }

    public final Optional<Entry<InstanceIdentifier<? extends DataObject>, DataObject>> toBinding(
            @Nonnull final Entry<YangInstanceIdentifier, ? extends NormalizedNode<?, ?>> normalized)
                    throws DeserializationException {
        try {
            /*
             * This cast is required, due to generics behaviour in openjdk / oracle javac.
             *
             * <p>
             * InstanceIdentifier has definition InstanceIdentifier<T extends DataObject>,
             * this means '?' is always  <? extends DataObject>. Eclipse compiler
             * is able to determine this relationship and treats
             * Entry<InstanceIdentifier<?>, DataObject> and Entry<InstanceIdentifier<? extends DataObject, DataObject>
             * as assignable. However openjdk / oracle javac treats this two types
             * as incompatible and issues a compile error.
             *
             * <p>
             * It is safe to lose generic information and cast it to other generic signature.
             */
            @SuppressWarnings("unchecked")
            final Entry<InstanceIdentifier<? extends DataObject>, DataObject> binding = Entry.class.cast(
                    codecRegistry.fromNormalizedNode(normalized.getKey(), normalized.getValue()));
            return Optional.fromNullable(binding);
        } catch (final IllegalArgumentException e) {
            return Optional.absent();
        }
    }

    @Override
    public void onGlobalContextUpdated(final SchemaContext context) {
        final BindingRuntimeContext runtimeContext = BindingRuntimeContext.create(classLoadingStrategy, context);
        codecRegistry.onBindingRuntimeContextUpdated(runtimeContext);
        futureSchema.onRuntimeContextUpdated(runtimeContext);
    }

    public final <T extends DataObject> Function<Optional<NormalizedNode<?, ?>>, Optional<T>> deserializeFunction(
            final InstanceIdentifier<T> path) {
        return codecRegistry.deserializeFunction(path);
    }

    public final BindingNormalizedNodeCodecRegistry getCodecRegistry() {
        return codecRegistry;
    }

    @Override
    public void close() {
        if (listenerRegistration != null) {
            listenerRegistration.close();
        }
    }

    public final BindingNormalizedNodeCodecRegistry getCodecFactory() {
        return codecRegistry;
    }

    // FIXME: This should be probably part of Binding Runtime context
    public final ImmutableBiMap<Method, SchemaPath> getRpcMethodToSchemaPath(final Class<? extends RpcService> key) {
        final Module module = getModuleBlocking(key);
        final ImmutableBiMap.Builder<Method, SchemaPath> ret = ImmutableBiMap.builder();
        try {
            for (final RpcDefinition rpcDef : module.getRpcs()) {
                final Method method = findRpcMethod(key, rpcDef);
                ret.put(method, rpcDef.getPath());
            }
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException("Rpc defined in model does not have representation in generated class.", e);
        }
        return ret.build();
    }

    protected ImmutableBiMap<Method, RpcDefinition> getRpcMethodToSchema(final Class<? extends RpcService> key) {
        final Module module = getModuleBlocking(key);
        final ImmutableBiMap.Builder<Method, RpcDefinition> ret = ImmutableBiMap.builder();
        try {
            for (final RpcDefinition rpcDef : module.getRpcs()) {
                final Method method = findRpcMethod(key, rpcDef);
                ret.put(method, rpcDef);
            }
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException("Rpc defined in model does not have representation in generated class.", e);
        }
        return ret.build();
    }

    private Module getModuleBlocking(final Class<?> modeledClass) {
        final QNameModule moduleName = BindingReflections.getQNameModule(modeledClass);
        BindingRuntimeContext localRuntimeContext = runtimeContext();
        Module module = localRuntimeContext == null ? null :
            localRuntimeContext.getSchemaContext().findModule(moduleName).orElse(null);
        if (module == null && futureSchema.waitForSchema(moduleName)) {
            localRuntimeContext = runtimeContext();
            Preconditions.checkState(localRuntimeContext != null, "BindingRuntimeContext is not available.");
            module = localRuntimeContext.getSchemaContext().findModule(moduleName).orElse(null);
        }
        checkState(module != null, "Schema for %s is not available; expected module name: %s.", modeledClass,
                moduleName);
        return module;
    }

    private void waitForSchema(final Collection<Class<?>> binding, final MissingSchemaException exception) {
        LOG.warn("Blocking thread to wait for schema convergence updates for {} {}", futureSchema.getDuration(),
            futureSchema.getUnit());
        if (!futureSchema.waitForSchema(binding)) {
            throw exception;
        }
    }

    private Method findRpcMethod(final Class<? extends RpcService> key, final RpcDefinition rpcDef)
            throws NoSuchMethodException {
        final String methodName = BindingMapping.getMethodName(rpcDef.getQName());
        final Class<?> inputClz = runtimeContext().getClassForSchema(rpcDef.getInput());
        return key.getMethod(methodName, inputClz);
    }

    @Override
    public final BindingCodecTree create(final BindingRuntimeContext context) {
        return codecRegistry.create(context);
    }

    @Override
    public final BindingCodecTree create(final SchemaContext context, final Class<?>... bindingClasses) {
        return codecRegistry.create(context, bindingClasses);
    }

    @Nonnull
    protected Entry<InstanceIdentifier<?>, BindingCodecTreeNode<?>> getSubtreeCodec(
            final YangInstanceIdentifier domIdentifier) {

        final BindingCodecTree currentCodecTree = codecRegistry.getCodecContext();
        final InstanceIdentifier<?> bindingPath = codecRegistry.fromYangInstanceIdentifier(domIdentifier);
        Preconditions.checkArgument(bindingPath != null);
        /**
         * If we are able to deserialize YANG instance identifier, getSubtreeCodec must
         * return non-null value.
         */
        final BindingCodecTreeNode<?> codecContext = currentCodecTree.getSubtreeCodec(bindingPath);
        return new SimpleEntry<>(bindingPath, codecContext);
    }

    @SuppressWarnings("unchecked")
    public final Set<Class<? extends Notification>> getNotificationClasses(final Set<SchemaPath> interested) {
        final Set<Class<? extends Notification>> result = new HashSet<>();
        final BindingRuntimeContext runtimeContext = runtimeContext();
        for (final NotificationDefinition notification : runtimeContext.getSchemaContext().getNotifications()) {
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

    SchemaPath getActionPath(final Class<? extends Action<?, ?, ?>> type) {
        final DataSchemaNode schema = runtimeContext().getSchemaDefinition(type);
        if (schema == null) {
            return Optional.absent();
        }

        verify(schema instanceof ActionDefinition, "Type %s resolves to non-action schema %s", type, schema);
        return Optional.of((ActionDefinition) schema);
    }

    private BindingRuntimeContext runtimeContext() {
        return futureSchema.runtimeContext();
    }

    private static Collection<Class<?>> decompose(final InstanceIdentifier<?> path) {
        return ImmutableSet.copyOf(Iterators.transform(path.getPathArguments().iterator(), PathArgument::getType));
    }

    protected NormalizedNode<?, ?> instanceIdentifierToNode(final YangInstanceIdentifier parentPath) {
        return ImmutableNodes.fromInstanceId(runtimeContext().getSchemaContext(), parentPath);
    }

    public NormalizedNode<?, ?> getDefaultNodeFor(final YangInstanceIdentifier parentMapPath) {
        final BindingCodecTreeNode<?> mapCodec = requireNonNull(
                codecRegistry.getCodecContext().getSubtreeCodec(parentMapPath),
                "Codec not found for yang instance identifier: " + parentMapPath);
        final WithStatus schema = mapCodec.getSchema();
        if (schema instanceof ListSchemaNode) {
            final ListSchemaNode castedSchema = (ListSchemaNode) schema;
            return castedSchema.isUserOrdered() ? Builders.orderedMapBuilder(castedSchema).build()
                    : Builders.mapBuilder(castedSchema).build();
        }
        throw new IllegalArgumentException("Path does not point to list schema node");
    }

    protected Collection<DOMDataTreeIdentifier> toDOMDataTreeIdentifiers(
            final Collection<DataTreeIdentifier<?>> subtrees) {
        return subtrees.stream().map(this::toDOMDataTreeIdentifier).collect(Collectors.toSet());
    }

    protected DOMDataTreeIdentifier toDOMDataTreeIdentifier(final DataTreeIdentifier<?> path) {
        final YangInstanceIdentifier domPath = toYangInstanceIdentifierBlocking(path.getRootIdentifier());
        return new DOMDataTreeIdentifier(path.getDatastoreType(), domPath);
    }
}
