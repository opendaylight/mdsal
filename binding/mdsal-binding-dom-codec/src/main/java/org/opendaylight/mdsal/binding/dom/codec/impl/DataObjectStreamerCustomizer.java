/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.mdsal.binding.dom.codec.impl.ByteBuddyUtils.getField;
import static org.opendaylight.mdsal.binding.dom.codec.impl.ByteBuddyUtils.invokeMethod;
import static org.opendaylight.mdsal.binding.dom.codec.impl.ByteBuddyUtils.putField;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDefinition.Sort;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.Implementation.Context;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.Duplication;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.TypeCreation;
import net.bytebuddy.implementation.bytecode.constant.ClassConstant;
import net.bytebuddy.implementation.bytecode.constant.IntegerConstant;
import net.bytebuddy.implementation.bytecode.constant.TextConstant;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.dom.codec.impl.NodeCodecContext.CodecContextFactory;
import org.opendaylight.mdsal.binding.dom.codec.loader.CodecClassLoader;
import org.opendaylight.mdsal.binding.dom.codec.loader.CodecClassLoader.ByteBuddyCustomizer;
import org.opendaylight.mdsal.binding.dom.codec.loader.CodecClassLoader.ByteBuddyResult;
import org.opendaylight.mdsal.binding.dom.codec.util.BindingSchemaMapping;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerRegistry;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
public final class DataObjectStreamerCustomizer implements ByteBuddyCustomizer {
    static final String INSTANCE_FIELD = "INSTANCE";

    private static final Logger LOG = LoggerFactory.getLogger(DataObjectStreamerCustomizer.class);
    private static final Generic BB_VOID = TypeDefinition.Sort.describe(void.class);
    private static final Generic BB_DATAOBJECT = TypeDefinition.Sort.describe(DataObject.class);
    private static final Generic BB_DOSR = TypeDefinition.Sort.describe(DataObjectSerializerRegistry.class);
    private static final Generic BB_BESV = TypeDefinition.Sort.describe(BindingStreamEventWriter.class);
    private static final Generic BB_IOX = TypeDefinition.Sort.describe(IOException.class);
    private static final int PUB_FINAL = Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC;
    private static final int PUB_CONST = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL
            | Opcodes.ACC_SYNTHETIC;

    private static final StackManipulation REG = MethodVariableAccess.REFERENCE.loadFrom(1);
    private static final StackManipulation OBJ = MethodVariableAccess.REFERENCE.loadFrom(2);
    private static final StackManipulation STREAM = MethodVariableAccess.REFERENCE.loadFrom(3);
    private static final StackManipulation UNKNOWN_SIZE = IntegerConstant.forValue(
        BindingStreamEventWriter.UNKNOWN_SIZE);


    private static final StackManipulation KEY = invokeMethod(Identifiable.class, "key");

    private static final StackManipulation START_AUGMENTATION_NODE = invokeMethod(BindingStreamEventWriter.class,
        "startAugmentationNode", Class.class);
    private static final StackManipulation START_CASE = invokeMethod(BindingStreamEventWriter.class,
        "startCase", Class.class, int.class);
    private static final StackManipulation START_CONTAINER_NODE = invokeMethod(BindingStreamEventWriter.class,
        "startContainerNode", Class.class, int.class);
    private static final StackManipulation END_NODE = invokeMethod(BindingStreamEventWriter.class,
        "endNode");

    // startMapEntryNode(obj.key(), UNKNOWN_SIZE);
    private static final StackManipulation START_MAP_ENTRY_NODE = new StackManipulation.Compound(
        STREAM,
        OBJ,
        KEY,
        UNKNOWN_SIZE,
        invokeMethod(BindingStreamEventWriter.class, "startMapEntryNode", Identifier.class, int.class));

    // stream.startUnkeyedListItem(UNKNOWN_SIZE);
    private static final StackManipulation START_UNKEYED_LIST_ITEM = new StackManipulation.Compound(
        UNKNOWN_SIZE,
        invokeMethod(BindingStreamEventWriter.class, "startUnkeyedListItem", int.class));

    private static final StackManipulation STREAM_ANYXML = invokeMethod(DataObjectStreamer.class,
        "streamAnyxml", BindingStreamEventWriter.class, String.class, Object.class);
    private static final StackManipulation STREAM_CHOICE = invokeMethod(DataObjectStreamer.class,
        "streamChoice", Class.class, DataObjectSerializerRegistry.class, BindingStreamEventWriter.class,
        DataContainer.class);
    private static final StackManipulation STREAM_CONTAINER = invokeMethod(DataObjectStreamer.class,
        "streamContainer", DataObjectStreamer.class, DataObjectSerializerRegistry.class, BindingStreamEventWriter.class,
        DataObject.class);
    private static final StackManipulation STREAM_LEAF = invokeMethod(DataObjectStreamer.class,
        "streamLeaf", BindingStreamEventWriter.class, String.class, Object.class);
    private static final StackManipulation STREAM_LEAF_LIST = invokeMethod(DataObjectStreamer.class,
        "streamLeafList",
        BindingStreamEventWriter.class, String.class, List.class);
    private static final StackManipulation STREAM_ORDERED_LEAF_LIST = invokeMethod(DataObjectStreamer.class,
        "streamOrderedLeafList", BindingStreamEventWriter.class, String.class, List.class);
    private static final StackManipulation STREAM_LIST = invokeMethod(DataObjectStreamer.class,
        "streamList", Class.class, DataObjectStreamer.class, DataObjectSerializerRegistry.class,
        BindingStreamEventWriter.class, List.class);
    private static final StackManipulation STREAM_MAP = invokeMethod(DataObjectStreamer.class,
        "streamMap", Class.class, DataObjectStreamer.class, DataObjectSerializerRegistry.class,
        BindingStreamEventWriter.class, List.class);
    private static final StackManipulation STREAM_ORDERED_MAP = invokeMethod(DataObjectStreamer.class,
        "streamOrderedMap", Class.class, DataObjectStreamer.class, DataObjectSerializerRegistry.class,
        BindingStreamEventWriter.class, List.class);

    // streamAugmentations(reg, stream, obj);
    private static final StackManipulation STREAM_AUGMENTATIONS = new StackManipulation.Compound(
        REG,
        STREAM,
        OBJ,
        invokeMethod(DataObjectStreamer.class, "streamAugmentations", DataObjectSerializerRegistry.class,
            BindingStreamEventWriter.class, Augmentable.class));

    private final ImmutableMap<String, Type> props;
    private final CodecContextFactory registry;
    private final StackManipulation startEvent;
    private final DataNodeContainer schema;
    private final Class<?> type;

    DataObjectStreamerCustomizer(final CodecContextFactory registry, final GeneratedType genType,
            final DataNodeContainer schema, final Class<?> type, final StackManipulation startEvent) {
        this.registry = requireNonNull(registry);
        this.schema = requireNonNull(schema);
        this.type = requireNonNull(type);
        this.startEvent = requireNonNull(startEvent);
        props = collectAllProperties(genType);
    }

    public static DataObjectStreamerCustomizer create(final CodecContextFactory registry, final Class<?> type) {
        final Entry<GeneratedType, WithStatus> typeAndSchema = registry.getRuntimeContext().getTypeWithSchema(type);
        final WithStatus schema = typeAndSchema.getValue();

        final StackManipulation startEvent;
        if (schema instanceof ContainerSchemaNode || schema instanceof NotificationDefinition) {
            startEvent = classUnknownSizeMethod(START_CONTAINER_NODE, type);
        } else if (schema instanceof ListSchemaNode) {
            startEvent = ((ListSchemaNode) schema).getKeyDefinition().isEmpty() ? START_UNKEYED_LIST_ITEM
                    : START_MAP_ENTRY_NODE;
        } else if (schema instanceof AugmentationSchemaNode) {
            // startAugmentationNode(Foo.class);
            startEvent = new StackManipulation.Compound(
                ClassConstant.of(Sort.describe(type).asErasure()),
                START_AUGMENTATION_NODE);
        } else if (schema instanceof CaseSchemaNode) {
            startEvent = classUnknownSizeMethod(START_CASE, type);
        } else {
            throw new UnsupportedOperationException("Schema type " + schema.getClass() + " is not supported");
        }

        return new DataObjectStreamerCustomizer(registry, typeAndSchema.getKey(), (DataNodeContainer) schema, type,
            startEvent);
    }

    @Override
    public <T> ByteBuddyResult<T> customize(final CodecClassLoader loader, final Class<?> bindingInterface,
            final String fqn, final Builder<T> builder) {
        LOG.trace("Definining streamer {}", fqn);

        final List<ChildStream> children = new ArrayList<>();
        for (final DataSchemaNode schemaChild : schema.getChildNodes()) {
            if (!schemaChild.isAugmenting()) {
                final String getterName = BindingSchemaMapping.getGetterMethodName(schemaChild);
                final Method getter;
                try {
                    getter = type.getMethod(getterName);
                } catch (NoSuchMethodException e) {
                    throw new IllegalStateException("Failed to find getter " + getterName, e);
                }

                final ChildStream child = createStream(loader, schemaChild, getter);
                if (child != null) {
                    children.add(child);
                }
            }
        }

        final ImmutableList.Builder<Class<?>> depBuilder = ImmutableList.builder();
        for (ChildStream child : children) {
            child.getDependency().ifPresent(depBuilder::add);
        }

        final ByteBuddyResult<T> result = ByteBuddyResult.of(builder.defineMethod("serialize", BB_VOID, PUB_FINAL)
            .withParameters(BB_DOSR, BB_DATAOBJECT, BB_BESV).throwing(BB_IOX).intercept(
                new SerializeImplementation(bindingInterface, startEvent, children)).make(),
            depBuilder.build());

        LOG.trace("Definition of {} done", fqn);
        return result;
    }

    private ChildStream createStream(final CodecClassLoader loader, final DataSchemaNode child,
            final Method getter) {
        if (child instanceof LeafSchemaNode) {
            return qnameChildStream(STREAM_LEAF, getter, child);
        }
        if (child instanceof ContainerSchemaNode) {
            final Class<? extends DataObject> itemClass = getter.getReturnType().asSubclass(DataObject.class);
            return containerChildStream(getter, registry.getDataObjectSerializer(itemClass));
        }
        if (child instanceof ListSchemaNode) {
            final String getterName = getter.getName();
            final Type childType = props.get(getterName);
            verify(childType instanceof ParameterizedType, "Unexpected type %s for %s", childType, getterName);
            final Type valueType = ((ParameterizedType) childType).getActualTypeArguments()[0];
            final Class<?> valueClass;
            try {
                valueClass = loader.loadClass(valueType.getFullyQualifiedName());
            } catch (ClassNotFoundException e) {
                throw new LinkageError("Failed to load " + valueType, e);
            }

            verify(DataObject.class.isAssignableFrom(valueClass), "Value type %s of %s is not a DataObject", valueClass,
                getter);
            final Class<? extends DataObject> itemClass = valueClass.asSubclass(DataObject.class);
            return listChildStream(getter, itemClass, (ListSchemaNode) child);
        }
        if (child instanceof ChoiceSchemaNode) {
            return choiceChildStream(getter);
        }
        if (child instanceof AnyXmlSchemaNode) {
            return qnameChildStream(STREAM_ANYXML, getter, child);
        }
        if (child instanceof LeafListSchemaNode) {
            return qnameChildStream(((LeafListSchemaNode) child).isUserOrdered() ? STREAM_ORDERED_LEAF_LIST
                    : STREAM_LEAF_LIST, getter, child);
        }

        LOG.debug("Ignoring {} due to unhandled schema {}", getter, child);
        return null;
    }

    private static ChildStream choiceChildStream(final Method getter) {
        // streamChoice(Foo.class, reg, stream, obj.getFoo());
        return new ChildStream(
            ClassConstant.of(Sort.describe(getter.getReturnType()).asErasure()),
            REG,
            STREAM,
            OBJ,
            invokeMethod(getter),
            STREAM_CHOICE);
    }

    private static ChildStream containerChildStream(final Method getter, final DataObjectStreamer<?> streamer) {
        // streamContainer(FooStreamer.INSTANCE, reg, stream, obj.getFoo());
        return new ChildStream(streamer,
            streamerInstance(streamer),
            REG,
            STREAM,
            OBJ,
            invokeMethod(getter),
            STREAM_CONTAINER);
    }

    private ChildStream listChildStream(final Method getter, final Class<? extends DataObject> itemClass,
            final ListSchemaNode schema) {
        final DataObjectStreamer<?> streamer = registry.getDataObjectSerializer(itemClass);
        final StackManipulation method;
        if (schema.getKeyDefinition().isEmpty()) {
            method = STREAM_LIST;
        } else {
            method = schema.isUserOrdered() ? STREAM_ORDERED_MAP : STREAM_MAP;
        }

        // <METHOD>(Foo.class, FooStreamer.INSTACE, reg, stream, obj.getFoo());
        return new ChildStream(streamer,
            ClassConstant.of(Sort.describe(itemClass).asErasure()),
            streamerInstance(streamer),
            REG,
            STREAM,
            OBJ,
            invokeMethod(getter),
            method);
    }

    private static ChildStream qnameChildStream(final StackManipulation method, final Method getter,
            final DataSchemaNode schema) {
        // <METHOD>(stream, "foo", obj.getFoo());
        return new ChildStream(
            STREAM,
            new TextConstant(schema.getQName().getLocalName()),
            OBJ,
            invokeMethod(getter),
            method);
    }

    private static StackManipulation streamerInstance(final DataObjectStreamer<?> streamer) {
        try {
            return getField(streamer.getClass().getDeclaredField(INSTANCE_FIELD));
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    private static StackManipulation classUnknownSizeMethod(final StackManipulation method, final Class<?> type) {
        // <METHOD>(Foo.class, UNKNOWN_SIZE);
        return new StackManipulation.Compound(
                ClassConstant.of(Sort.describe(type).asErasure()),
                UNKNOWN_SIZE,
                method);
    }

    private static ImmutableMap<String, Type> collectAllProperties(final GeneratedType type) {
        final Map<String, Type> props = new HashMap<>();
        collectAllProperties(type, props);
        return ImmutableMap.copyOf(props);
    }

    private static void collectAllProperties(final GeneratedType type, final Map<String, Type> hashMap) {
        for (final MethodSignature definition : type.getMethodDefinitions()) {
            hashMap.put(definition.getName(), definition.getReturnType());
        }
        for (final Type parent : type.getImplements()) {
            if (parent instanceof GeneratedType) {
                collectAllProperties((GeneratedType) parent, hashMap);
            }
        }
    }

    private static final class SerializeImplementation implements Implementation {
        private final List<ChildStream> children;
        private final StackManipulation startEvent;
        private final Class<?> bindingInterface;

        SerializeImplementation(final Class<?> bindingInterface, final StackManipulation startEvent,
                final List<ChildStream> children) {
            this.bindingInterface = requireNonNull(bindingInterface);
            this.startEvent = requireNonNull(startEvent);
            this.children = requireNonNull(children);
        }

        @Override
        public InstrumentedType prepare(final InstrumentedType instrumentedType) {
            return instrumentedType
                    // private static final This INSTANCE = new This();
                    .withField(new FieldDescription.Token(INSTANCE_FIELD, PUB_CONST, instrumentedType.asGenericType()))
                    .withInitializer(InitializeInstanceField.INSTANCE);
        }

        @Override
        public ByteCodeAppender appender(final Target implementationTarget) {
            final List<StackManipulation> manipulations = new ArrayList<>(children.size() + 6);

            // stream.<START_EVENT>(...);
            manipulations.add(STREAM);
            manipulations.add(startEvent);

            // ... emit children ...
            manipulations.addAll(children);

            if (Augmentable.class.isAssignableFrom(bindingInterface)) {
                // streamAugmentations(reg, stream, obj);
                manipulations.add(STREAM_AUGMENTATIONS);
            }

            // stream.endNode();
            manipulations.add(STREAM);
            manipulations.add(END_NODE);
            // return;
            manipulations.add(MethodReturn.VOID);

            return new ByteCodeAppender.Simple(manipulations);
        }
    }

    private static final class ChildStream extends StackManipulation.Compound {
        private final @Nullable Class<?> dependency;

        ChildStream(final StackManipulation... stackManipulation) {
            super(stackManipulation);
            dependency = null;
        }

        ChildStream(final DataObjectStreamer<?> streamer, final StackManipulation... stackManipulation) {
            super(stackManipulation);
            dependency = streamer.getClass();
        }

        Optional<Class<?>> getDependency() {
            return Optional.ofNullable(dependency);
        }
    }

    private enum InitializeInstanceField implements ByteCodeAppender {
        INSTANCE;

        @Override
        public Size apply(final MethodVisitor methodVisitor, final Context implementationContext,
                final MethodDescription instrumentedMethod) {
            StackManipulation.Size operandStackSize = new StackManipulation.Compound(
                TypeCreation.of(implementationContext.getInstrumentedType()),
                Duplication.SINGLE,
                MethodInvocation.invoke(implementationContext.getInstrumentedType().getDeclaredMethods()
                    .filter(ElementMatchers.isDefaultConstructor()).getOnly().asDefined()),
                putField(implementationContext, INSTANCE_FIELD))
                    .apply(methodVisitor, implementationContext);
            return new Size(operandStackSize.getMaximalSize(), instrumentedMethod.getStackSize());
        }
    }
}
