/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import java.util.Iterator;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.OperationInputCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.UnmappedOperationInputCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base.DataContainerCodecContext;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base.DataContainerCodecPrototype;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifier;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifierNormalizer;
import org.opendaylight.mdsal.binding.javav2.runtime.reflection.BindingReflections;
import org.opendaylight.mdsal.binding.javav2.spec.base.Instantiable;
import org.opendaylight.mdsal.binding.javav2.spec.base.Notification;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeArgument;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.OperationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;

/**
 * Creates RootNode from supplied CodecContextFactory and make operations with.
 *
 * @param <D>
 *            - tree node type
 */
@Beta
public final class SchemaRootCodecContext<D extends TreeNode> extends DataContainerCodecContext<D, SchemaContext> {

    private final LoadingCache<Class<?>, DataContainerCodecContext<?, ?>> childrenByClass =
            CacheBuilder.newBuilder().build(new CacheLoader<Class<?>, DataContainerCodecContext<?, ?>>() {
                @Override
                public DataContainerCodecContext<?, ?> load(final Class<?> key) {
                    return createDataTreeChildContext(key);
                }
            });

    private final LoadingCache<Class<?>, ContainerNodeCodecContext<?>> operationDataByClass =
            CacheBuilder.newBuilder().build(new CacheLoader<Class<?>, ContainerNodeCodecContext<?>>() {
                @Override
                public ContainerNodeCodecContext<?> load(final Class<?> key) {
                    return createOperationDataContext(key);
                }
            });

    private final LoadingCache<Class<?>, NotificationCodecContext<?>> notificationsByClass =
            CacheBuilder.newBuilder().build(new CacheLoader<Class<?>, NotificationCodecContext<?>>() {
                @Override
                public NotificationCodecContext<?> load(final Class<?> key) {
                    return createNotificationDataContext(key);
                }
            });

    private final LoadingCache<QName, DataContainerCodecContext<?, ?>> childrenByQName =
            CacheBuilder.newBuilder().build(new CacheLoader<QName, DataContainerCodecContext<?, ?>>() {
                @SuppressWarnings("unchecked")
                @Override
                public DataContainerCodecContext<?, ?> load(final QName qname) {
                    final DataSchemaNode childSchema = getSchema().getDataChildByName(qname);
                    childNonNull(childSchema, qname, "Argument %s is not valid child of %s", qname, getSchema());
                    if (childSchema instanceof DataNodeContainer || childSchema instanceof ChoiceSchemaNode) {
                        @SuppressWarnings("rawtypes")
                        final Class childCls = factory().getRuntimeContext().getClassForSchema(childSchema);
                        return streamChild(childCls);
                    }

                    throw new UnsupportedOperationException("Unsupported child type " + childSchema.getClass());
                }
            });

    private final LoadingCache<SchemaPath, OperationInputCodec<?>> operationDataByPath =
            CacheBuilder.newBuilder().build(new CacheLoader<SchemaPath, OperationInputCodec<?>>() {
                @SuppressWarnings({ "rawtypes", "unchecked" })
                @Override
                public OperationInputCodec load(final SchemaPath key) {
                    final ContainerSchemaNode schema = getOperationDataSchema(getSchema(), key);
                    if (schema instanceof EffectiveStatement && ((EffectiveStatement) schema).getDeclared()
                            .getStatementSource() != StatementSource.DECLARATION) {
                        // This is an implicitly-defined input or output statement. We do not have a
                        // corresponding
                        // data representation, so we hard-wire it to null.
                        return UnmappedOperationInputCodec.getInstance();
                    }

                    final Class cls = factory().getRuntimeContext().getClassForSchema(schema);
                    return getOperation(cls);
                }
            });

    /**
     * Returns operation Input or Output Data container from operation definition.
     *
     * @param schema
     *            - SchemaContext in which lookup should be performed
     * @param path
     *            - Schema path of operation input/output data container
     * @return operation schema or null, if operation is not present in schema context.
     */
    private ContainerSchemaNode getOperationDataSchema(final SchemaContext schema, final SchemaPath path) {
        Preconditions.checkNotNull(schema, "Schema context must not be null.");
        Preconditions.checkNotNull(path, "Schema path must not be null.");
        final Iterator<QName> it = path.getPathFromRoot().iterator();
        Preconditions.checkArgument(it.hasNext(), "Operation must have QName.");
        final QName operationName = it.next();
        Preconditions.checkArgument(it.hasNext(), "input or output must be part of path.");
        final QName inOrOut = it.next();
        ContainerSchemaNode contSchemaNode = null;
        if ((contSchemaNode = getOperationDataSchema(schema.getOperations(), operationName, inOrOut)) == null) {
            contSchemaNode = getOperationDataSchema(schema.getActions(), operationName, inOrOut);
        }
        return contSchemaNode;
    }

    private ContainerSchemaNode getOperationDataSchema(final Set<? extends OperationDefinition> operations,
            final QName operationName, final QName inOrOut) {
        for (final OperationDefinition potential : operations) {
            if (operationName.equals(potential.getQName())) {
                return getOperationDataSchema(potential, inOrOut);
            }
        }
        return null;
    }

    private final LoadingCache<SchemaPath, NotificationCodecContext<?>> notificationsByPath =
            CacheBuilder.newBuilder().build(new CacheLoader<SchemaPath, NotificationCodecContext<?>>() {

                @SuppressWarnings({ "rawtypes", "unchecked" })
                @Override
                public NotificationCodecContext load(final SchemaPath key) throws Exception {
                    final NotificationDefinition schema = SchemaContextUtil.getNotificationSchema(getSchema(), key);
                    final Class clz = factory().getRuntimeContext().getClassForSchema(schema);
                    return getNotification(clz);
                }
            });

    private SchemaRootCodecContext(final DataContainerCodecPrototype<SchemaContext> dataPrototype) {
        super(dataPrototype);
    }

    /**
     * Creates RootNode from supplied CodecContextFactory.
     *
     * @param factory
     *            - CodecContextFactory
     * @return schema root node
     */
    public static SchemaRootCodecContext<?> create(final CodecContextFactory factory) {
        final DataContainerCodecPrototype<SchemaContext> prototype = DataContainerCodecPrototype.rootPrototype(factory);
        return new SchemaRootCodecContext<>(prototype);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Nonnull
    @Override
    public <DV extends TreeNode> DataContainerCodecContext<DV, ?> streamChild(@Nonnull final Class<DV> childClass)
            throws IllegalArgumentException {
        /*
         * FIXME: This is still not solved for operations TODO: Probably performance wise operations, Data and
         * Notification loading cache should be merge for performance resons. Needs microbenchmark to
         * determine which is faster (keeping them separate or in same cache).
         */
        if (Notification.class.isAssignableFrom(childClass)) {
            return (DataContainerCodecContext<DV, ?>) getNotification((Class<? extends Notification>) childClass);
        }
        return (DataContainerCodecContext<DV, ?>) getOrRethrow(childrenByClass, childClass);
    }

    @Override
    public <E extends TreeNode> Optional<DataContainerCodecContext<E, ?>>
            possibleStreamChild(@Nonnull final Class<E> childClass) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Nonnull
    @Override
    public DataContainerCodecContext<?, ?> yangPathArgumentChild(final PathArgument arg) {
        return getOrRethrow(childrenByQName, arg.getNodeType());
    }

    @Nonnull
    @Override
    public D deserialize(@Nonnull final NormalizedNode<?, ?> normalizedNode) {
        throw new UnsupportedOperationException("Could not create Binding data representation for root");
    }

    @Override
    protected Object deserializeObject(final NormalizedNode<?, ?> normalizedNode) {
        throw new UnsupportedOperationException("Unable to deserialize root");
    }

    @Nullable
    @Override
    public TreeArgument<?> deserializePathArgument(@Nullable final YangInstanceIdentifier.PathArgument arg) {
        Preconditions.checkArgument(arg == null);
        return null;
    }

    @Nullable
    @Override
    public YangInstanceIdentifier.PathArgument serializePathArgument(@Nullable final TreeArgument<?> arg) {
        Preconditions.checkArgument(arg == null);
        return null;
    }

    /**
     * Get operation as binding object of binding class.
     *
     * @param operationInputOrOutput
     *            - binding class
     * @return container node codec context of operation
     */
    public ContainerNodeCodecContext<?> getOperation(final Class<? extends Instantiable<?>> operationInputOrOutput) {
        return getOrRethrow(operationDataByClass, operationInputOrOutput);
    }

    /**
     * Get notification as binding object of binding class.
     *
     * @param notification
     *            - binding class
     * @return notification codec context of notification
     */
    @SuppressWarnings("rawtypes")
    public NotificationCodecContext<?> getNotification(final Class<? extends Notification> notification) {
        return getOrRethrow(notificationsByClass, notification);
    }

    /**
     * Get notification as binding object according to schema path of notification.
     *
     * @param notification
     *            - schema path of notification
     * @return notification codec context of notification
     */
    public NotificationCodecContext<?> getNotification(final SchemaPath notification) {
        return getOrRethrow(notificationsByPath, notification);
    }

    /**
     * Get operation input as binding object according to schema path of operation.
     *
     * @param operation
     *            - schema path of operation
     * @return operation input codec of operation
     */
    public OperationInputCodec<?> getOperation(final SchemaPath operation) {
        return getOrRethrow(operationDataByPath, operation);
    }

    private DataContainerCodecContext<?, ?> createDataTreeChildContext(final Class<?> key) {
        final QName qname = BindingReflections.findQName(key);
        final DataSchemaNode childSchema =
                childNonNull(getSchema().getDataChildByName(qname), key, "%s is not top-level item.", key);
        return DataContainerCodecPrototype.from(key, childSchema, factory()).get();
    }

    private ContainerNodeCodecContext<?> createOperationDataContext(final Class<?> key) {
        Preconditions.checkArgument(Instantiable.class.isAssignableFrom(key));
        final QName qname = BindingReflections.findQName(key);
        final QNameModule module = qname.getModule();
        OperationDefinition operation = null;
        final SchemaContext schemaContext = getSchema();
        if ((operation = findPotentialOperation(schemaContext.getOperations(), module, key, qname)) == null) {
            operation = findPotentialOperation(schemaContext.getActions(), module, key, qname);
        }
        Preconditions.checkArgument(operation != null, "Supplied class %s is not valid operation class.", key);
        final ContainerSchemaNode schema = getOperationDataSchema(operation, qname);// SchemaNodeUtils.getRpcDataSchema(operation,
                                                                           // qname);
        Preconditions.checkArgument(schema != null, "Schema for %s does not define input / output.",
                operation.getQName());
        Class<?> actualKey = key;
        if (key.getSimpleName().endsWith("Impl")) {
            actualKey = key.getInterfaces()[0];
        }
        return (ContainerNodeCodecContext<?>) DataContainerCodecPrototype.from(actualKey, schema, factory()).get();
    }

    private ContainerSchemaNode getOperationDataSchema(final OperationDefinition operation, final QName qname) {
        Preconditions.checkNotNull(operation, "Operation Schema must not be null.");
        Preconditions.checkNotNull(qname, "QName must not be null.");
        switch (qname.getLocalName()) {
            case "input":
                return operation.getInput();
            case "output":
                return operation.getOutput();
            default:
                throw new IllegalArgumentException(
                        "Supplied qname " + qname + " does not represent operation input or output.");
        }
    }

    private OperationDefinition findPotentialOperation(final Set<? extends OperationDefinition> set,
            final QNameModule module, final Class<?> key, final QName qname) {
        OperationDefinition operation = null;
        for (final OperationDefinition potential : getSchema().getOperations()) {
            final QName potentialQName = potential.getQName();
            /*
             * Check if operation and class represents data from same module and then checks if operation
             * local name produces same class name as class name appended with Input/Output based on QName
             * associated with bidning class.
             *
             * FIXME: Rework this to have more precise logic regarding Binding Specification.
             */
            final String moduleClassName = JavaIdentifierNormalizer
                    .normalizeSpecificIdentifier(potentialQName.getLocalName(), JavaIdentifier.CLASS);
            final String keyClassName =
                    JavaIdentifierNormalizer.normalizeSpecificIdentifier(qname.getLocalName(), JavaIdentifier.CLASS);
            if (module.equals(potentialQName.getModule())
                    && (key.getSimpleName().equals(
                            new StringBuilder(moduleClassName).append(keyClassName).append("Impl").toString())
                            || key.getSimpleName()
                                    .equals(new StringBuilder(moduleClassName).append(keyClassName).toString()))) {
                operation = potential;
                break;
            }
        }
        return operation;
    }

    private NotificationCodecContext<?> createNotificationDataContext(final Class<?> notificationType) {
        Preconditions.checkArgument(Notification.class.isAssignableFrom(notificationType));
        Preconditions.checkArgument(notificationType.isInterface(), "Supplied class must be interface.");
        final QName qname = BindingReflections.findQName(notificationType);
        /**
         * FIXME: After Lithium cleanup of yang-model-api, use direct call on schema context to retrieve
         * notification via index.
         */
        final NotificationDefinition schema =
                SchemaContextUtil.getNotificationSchema(getSchema(), SchemaPath.create(true, qname));
        Preconditions.checkArgument(schema != null, "Supplied %s is not valid notification", notificationType);

        return new NotificationCodecContext<>(notificationType, schema, factory());
    }

    private static <K, V> V getOrRethrow(final LoadingCache<K, V> cache, final K key) {
        try {
            return cache.getUnchecked(key);
        } catch (final UncheckedExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause != null) {
                Throwables.propagateIfPossible(cause);
            }
            throw e;
        }
    }
}