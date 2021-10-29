/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Throwables;
import com.google.common.base.Verify;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.runtime.api.ActionRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeTypes;
import org.opendaylight.mdsal.binding.runtime.api.ChoiceRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.NotificationRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.ChoiceIn;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedListAction;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.binding.RpcInput;
import org.opendaylight.yangtools.yang.binding.RpcOutput;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerLike;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

final class SchemaRootCodecContext<D extends DataObject> extends DataContainerCodecContext<D, BindingRuntimeTypes> {

    private final LoadingCache<Class<? extends DataObject>, DataContainerCodecContext<?, ?>> childrenByClass =
        CacheBuilder.newBuilder().build(new CacheLoader<>() {
            @Override
            public DataContainerCodecContext<?, ?> load(final Class<? extends DataObject> key) {
                if (Notification.class.isAssignableFrom(key)) {
                    checkArgument(key.isInterface(), "Supplied class must be interface.");

                    // TODO: we should be able to work with bindingChild() instead of schemaTreeChild() here
                    final QName qname = BindingReflections.findQName(key);
                    final RuntimeType child = getType().schemaTreeChild(qname);
                    checkArgument(child instanceof NotificationRuntimeType, "Supplied %s is not valid notification",
                        key);
                    return new NotificationCodecContext<>(key, (NotificationRuntimeType) child, factory());
                }
                if (RpcInput.class.isAssignableFrom(key) || RpcOutput.class.isAssignableFrom(key)) {
                    final QName qname = BindingReflections.findQName(key);
                    final QNameModule qnameModule = qname.getModule();
                    final Module module = factory().getRuntimeContext().getEffectiveModelContext()
                        .findModule(qnameModule).orElseThrow(
                            () -> new IllegalArgumentException("Failed to find module for " + qnameModule));
                    final String className = BindingMapping.getClassName(qname);

                    for (final RpcDefinition potential : module.getRpcs()) {
                        final QName potentialQName = potential.getQName();
                        /*
                         * Check if rpc and class represents data from same module and then checks if rpc local name
                         * produces same class name as class name appended with Input/Output based on QName associated
                         * with binding class.
                         *
                         * FIXME: Rework this to have more precise logic regarding Binding Specification.
                         */
                        if (key.getSimpleName().equals(BindingMapping.getClassName(potentialQName) + className)) {
                            final ContainerLike schema = getRpcDataSchema(potential, qname);
                            checkArgument(schema != null, "Schema for %s does not define input / output.",
                                potentialQName);
                            return DataContainerCodecPrototype.from(key, schema, factory()).get();
                        }
                    }

                    throw new IllegalArgumentException("Supplied class " + key + " is not valid RPC class.");
                }
                return createDataTreeChildContext(key);
            }
        });

    private final LoadingCache<Class<? extends Action<?, ?, ?>>, ActionCodecContext> actionsByClass =
        CacheBuilder.newBuilder().build(new CacheLoader<>() {
            @Override
            public ActionCodecContext load(final Class<? extends Action<?, ?, ?>> key) {
                return createActionContext(key);
            }
        });

    private final LoadingCache<Class<? extends DataObject>, ChoiceNodeCodecContext<?>> choicesByClass =
        CacheBuilder.newBuilder().build(new CacheLoader<>() {
            @Override
            public ChoiceNodeCodecContext<?> load(final Class<? extends DataObject> key) {
                return createChoiceDataContext(key);
            }
        });

    private final LoadingCache<QName, DataContainerCodecContext<?,?>> childrenByQName =
        CacheBuilder.newBuilder().build(new CacheLoader<>() {
            @Override
            public DataContainerCodecContext<?, ?> load(final QName qname) throws ClassNotFoundException {
                final var type = getType();
                final var child = childNonNull(type.schemaTreeChild(qname), qname,
                    "Argument %s is not valid child of %s", qname, type);
                final var childSchema = child.schema();
                if (childSchema instanceof DataNodeContainer || childSchema instanceof ChoiceSchemaNode) {
                    @SuppressWarnings("unchecked")
                    final Class<? extends DataObject> childCls = (Class<? extends DataObject>)
                        factory().getRuntimeContext().loadClass(child.javaType());
                    return streamChild(childCls);
                }

                throw new UnsupportedOperationException("Unsupported child type " + childSchema.getClass());
            }
        });

    private final LoadingCache<Absolute, RpcInputCodec<?>> rpcDataByPath =
        CacheBuilder.newBuilder().build(new CacheLoader<>() {
            @Override
            public RpcInputCodec<?> load(final Absolute key) {
                @SuppressWarnings("unchecked")
                final Class<? extends DataContainer> cls = (Class<? extends DataContainer>)
                    factory().getRuntimeContext().getClassForSchema(key);
                return getRpc(cls);
            }
        });

    private final LoadingCache<Absolute, NotificationCodecContext<?>> notificationsByPath =
        CacheBuilder.newBuilder().build(new CacheLoader<>() {
            @Override
            public NotificationCodecContext<?> load(final Absolute key) {
                final Class<?> cls = factory().getRuntimeContext().getClassForSchema(key);
                checkArgument(Notification.class.isAssignableFrom(cls), "Path %s does not represent a notification",
                    key);
                return getNotificationImpl(cls);
            }
        });

    private SchemaRootCodecContext(final DataContainerCodecPrototype<BindingRuntimeTypes> dataPrototype) {
        super(dataPrototype);
    }

    /**
     * Creates RootNode from supplied CodecContextFactory.
     *
     * @param factory
     *            CodecContextFactory
     * @return A new root node
     */
    static SchemaRootCodecContext<?> create(final CodecContextFactory factory) {
        return new SchemaRootCodecContext<>(DataContainerCodecPrototype.rootPrototype(factory));
    }

    @Override
    public WithStatus getSchema() {
        return getType().getEffectiveModelContext();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C extends DataObject> DataContainerCodecContext<C, ?> streamChild(final Class<C> childClass) {
        return (DataContainerCodecContext<C, ?>) getOrRethrow(childrenByClass, childClass);
    }

    @Override
    public <C extends DataObject> Optional<DataContainerCodecContext<C, ?>> possibleStreamChild(
            final Class<C> childClass) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public DataContainerCodecContext<?,?> yangPathArgumentChild(final PathArgument arg) {
        return getOrRethrow(childrenByQName, arg.getNodeType());
    }

    @Override
    public D deserialize(final NormalizedNode normalizedNode) {
        throw new UnsupportedOperationException("Could not create Binding data representation for root");
    }

    ActionCodecContext getAction(final Class<? extends Action<?, ?, ?>> action) {
        return getOrRethrow(actionsByClass, action);
    }

    NotificationCodecContext<?> getNotification(final Absolute notification) {
        return getOrRethrow(notificationsByPath, notification);
    }

    NotificationCodecContext<?> getNotification(final Class<? extends Notification<?>> notification) {
        return getNotificationImpl(notification);
    }

    private NotificationCodecContext<?> getNotificationImpl(final Class<?> notification) {
        return (NotificationCodecContext<?>) streamChild(notification.asSubclass(DataObject.class));
    }

    ContainerNodeCodecContext<?> getRpc(final Class<? extends DataContainer> rpcInputOrOutput) {
        return (ContainerNodeCodecContext<?>) streamChild((Class<? extends DataObject>)rpcInputOrOutput);
    }

    RpcInputCodec<?> getRpc(final Absolute containerPath) {
        return getOrRethrow(rpcDataByPath, containerPath);
    }

    DataContainerCodecContext<?, ?> createDataTreeChildContext(final Class<? extends DataObject> key) {
        final RuntimeType childSchema = childNonNull(getType().bindingChild(JavaTypeName.create(key)), key,
            "%s is not top-level item.", key);
        // FIXME: bad cast
        return DataContainerCodecPrototype.from(key, (CompositeRuntimeType) childSchema, factory()).get();
    }

    ActionCodecContext createActionContext(final Class<? extends Action<?, ?, ?>> action) {
        if (KeyedListAction.class.isAssignableFrom(action)) {
            return prepareActionContext(2, 3, 4, action, KeyedListAction.class);
        } else if (Action.class.isAssignableFrom(action)) {
            return prepareActionContext(1, 2, 3, action, Action.class);
        }
        throw new IllegalArgumentException("The specific action type does not exist for action " + action.getName());
    }

    private ActionCodecContext prepareActionContext(final int inputOffset, final int outputOffset,
            final int expectedArgsLength, final Class<? extends Action<?, ?, ?>> action, final Class<?> actionType) {
        final Optional<ParameterizedType> optParamType = ClassLoaderUtils.findParameterizedType(action, actionType);
        checkState(optParamType.isPresent(), "%s does not specialize %s", action, actionType);

        final ParameterizedType paramType = optParamType.get();
        final Type[] args = paramType.getActualTypeArguments();
        checkArgument(args.length == expectedArgsLength, "Unexpected (%s) Action generatic arguments", args.length);
        final ActionRuntimeType schema = factory().getRuntimeContext().getActionDefinition(action);
        return new ActionCodecContext(
            DataContainerCodecPrototype.from(asClass(args[inputOffset], RpcInput.class), schema.input(),
                factory()).get(),
            DataContainerCodecPrototype.from(asClass(args[outputOffset], RpcOutput.class), schema.output(),
                factory()).get());
    }

    private static <T extends DataObject> Class<? extends T> asClass(final Type type, final Class<T> target) {
        verify(type instanceof Class, "Type %s is not a class", type);
        return ((Class<?>) type).asSubclass(target);
    }

    /**
     * Returns RPC input or output schema based on supplied QName.
     *
     * @param rpc RPC Definition
     * @param qname input or output QName with namespace same as RPC
     * @return input or output schema. Returns null if RPC does not have input/output specified.
     */
    private static @Nullable ContainerLike getRpcDataSchema(final @NonNull RpcDefinition rpc,
            final @NonNull QName qname) {
        requireNonNull(rpc, "Rpc Schema must not be null");
        switch (requireNonNull(qname, "QName must not be null").getLocalName()) {
            case "input":
                return rpc.getInput();
            case "output":
                return rpc.getOutput();
            default:
                throw new IllegalArgumentException("Supplied qname " + qname
                        + " does not represent rpc input or output.");
        }
    }

    /**
     * Returns RPC Input or Output Data container from RPC definition.
     *
     * @param schema SchemaContext in which lookup should be performed.
     * @param path Schema path of RPC input/output data container
     * @return Notification schema or null, if notification is not present in schema context.
     */
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
        justification = "https://github.com/spotbugs/spotbugs/issues/811")
    private static @Nullable ContainerLike getRpcDataSchema(final @NonNull EffectiveModelContext schema,
            final @NonNull Absolute path) {
        requireNonNull(schema, "Schema context must not be null.");
        requireNonNull(path, "Schema path must not be null.");
        final Iterator<QName> it = path.getNodeIdentifiers().iterator();
        checkArgument(it.hasNext(), "Rpc must have QName.");
        final QName rpcName = it.next();
        checkArgument(it.hasNext(), "input or output must be part of path.");
        final QName inOrOut = it.next();
        for (final RpcDefinition potential : schema.getOperations()) {
            if (rpcName.equals(potential.getQName())) {
                return getRpcDataSchema(potential, inOrOut);
            }
        }
        return null;
    }

    ChoiceNodeCodecContext<?> createChoiceDataContext(final Class<? extends DataObject> caseType) {
        final Class<?> choiceClass = findCaseChoice(caseType);
        checkArgument(choiceClass != null, "Class %s is not a valid case representation", caseType);
        final DataSchemaNode schema = factory().getRuntimeContext().getSchemaDefinition(choiceClass);
        checkArgument(schema instanceof ChoiceSchemaNode, "Class %s does not refer to a choice", caseType);

        final DataContainerCodecContext<?, ChoiceRuntimeType> choice = DataContainerCodecPrototype.from(choiceClass,
            (ChoiceSchemaNode)schema, factory()).get();
        Verify.verify(choice instanceof ChoiceNodeCodecContext);
        return (ChoiceNodeCodecContext<?>) choice;
    }

    @Override
    protected Object deserializeObject(final NormalizedNode normalizedNode) {
        throw new UnsupportedOperationException("Unable to deserialize root");
    }

    @Override
    public InstanceIdentifier.PathArgument deserializePathArgument(final YangInstanceIdentifier.PathArgument arg) {
        checkArgument(arg == null);
        return null;
    }

    @Override
    public YangInstanceIdentifier.PathArgument serializePathArgument(final InstanceIdentifier.PathArgument arg) {
        checkArgument(arg == null);
        return null;
    }

    @Override
    public DataContainerCodecContext<?, ?> bindingPathArgumentChild(final InstanceIdentifier.PathArgument arg,
            final List<PathArgument> builder) {
        final Optional<? extends Class<? extends DataObject>> caseType = arg.getCaseType();
        if (caseType.isPresent()) {
            final @NonNull Class<? extends DataObject> type = caseType.orElseThrow();
            final ChoiceNodeCodecContext<?> choice = choicesByClass.getUnchecked(type);
            choice.addYangPathArgument(arg, builder);
            final DataContainerCodecContext<?, ?> caze = choice.streamChild(type);
            caze.addYangPathArgument(arg, builder);
            return caze.bindingPathArgumentChild(arg, builder);
        }

        return super.bindingPathArgumentChild(arg, builder);
    }

    private static Class<?> findCaseChoice(final Class<? extends DataObject> caseClass) {
        for (Type type : caseClass.getGenericInterfaces()) {
            if (type instanceof Class) {
                final Class<?> typeClass = (Class<?>) type;
                if (ChoiceIn.class.isAssignableFrom(typeClass)) {
                    return typeClass.asSubclass(ChoiceIn.class);
                }
            }
        }

        return null;
    }

    private static <K,V> V getOrRethrow(final LoadingCache<K, V> cache, final K key) {
        try {
            return cache.getUnchecked(key);
        } catch (final UncheckedExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause != null) {
                Throwables.throwIfUnchecked(cause);
            }
            throw e;
        }
    }
}
