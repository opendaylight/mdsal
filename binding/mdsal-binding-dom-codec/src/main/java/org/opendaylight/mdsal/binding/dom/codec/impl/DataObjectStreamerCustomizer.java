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
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.opendaylight.yangtools.yang.common.QName;
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

    private final ImmutableMap<String, Type> props;
    private final CodecContextFactory registry;
    private final ByteCodeAppender startEvent;
    private final DataNodeContainer schema;
    private final Class<?> type;

    DataObjectStreamerCustomizer(final CodecContextFactory registry, final GeneratedType genType,
            final DataNodeContainer schema, final Class<?> type, final ByteCodeAppender startEvent) {
        this.registry = requireNonNull(registry);
        this.schema = requireNonNull(schema);
        this.type = requireNonNull(type);
        this.startEvent = requireNonNull(startEvent);
        props = collectAllProperties(genType);
    }

    public static DataObjectStreamerCustomizer create(final CodecContextFactory registry, final Class<?> type) {
        final Entry<GeneratedType, WithStatus> typeAndSchema = registry.getRuntimeContext().getTypeWithSchema(type);
        final WithStatus schema = typeAndSchema.getValue();

        final ByteCodeAppender startEvent;
        if (schema instanceof ContainerSchemaNode || schema instanceof NotificationDefinition) {
            startEvent = new StartContainerNode(type);
        } else if (schema instanceof ListSchemaNode) {
            startEvent = ((ListSchemaNode) schema).getKeyDefinition().isEmpty() ? StartUnkeyedListItem.INSTANCE
                    : StartMapEntryNode.INSTANCE;
        } else if (schema instanceof AugmentationSchemaNode) {
            startEvent = new StartAugmentationNode(type);
        } else if (schema instanceof CaseSchemaNode) {
            startEvent = new StartCase(type);
        } else {
            throw new UnsupportedOperationException("Schema type " + schema.getClass() + " is not supported");
        }

        return new DataObjectStreamerCustomizer(registry, typeAndSchema.getKey(), (DataNodeContainer) schema, type,
            startEvent);
    }

    private static final Generic BB_VOID = TypeDefinition.Sort.describe(void.class);
    private static final Generic BB_DATAOBJECT = TypeDefinition.Sort.describe(DataObject.class);
    private static final Generic BB_DOSR = TypeDefinition.Sort.describe(DataObjectSerializerRegistry.class);
    private static final Generic BB_BESV = TypeDefinition.Sort.describe(BindingStreamEventWriter.class);
    private static final Generic BB_IOX = TypeDefinition.Sort.describe(IOException.class);
    private static final int PUB_FINAL = Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC;
    private static final int PUB_CONST = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL
            | Opcodes.ACC_SYNTHETIC;

    @Override
    public <T> ByteBuddyResult<T> customize(final CodecClassLoader loader, final Class<?> bindingInterface,
            final String fqn, final Builder<T> builder) {
        LOG.trace("Definining streamer {}", fqn);

        final List<AbstractChild> children = new ArrayList<>();
        for (final DataSchemaNode schemaChild : schema.getChildNodes()) {
            if (!schemaChild.isAugmenting()) {
                final String getterName = BindingSchemaMapping.getGetterMethodName(schemaChild);
                final Method getter;
                try {
                    getter = type.getMethod(getterName);
                } catch (NoSuchMethodException e) {
                    throw new IllegalStateException("Failed to find getter " + getterName, e);
                }

                final AbstractChild child = createDefinition(loader, schemaChild, getter);
                if (child != null) {
                    children.add(child);
                }
            }
        }

        final ByteBuddyResult<T> result = ByteBuddyResult.of(builder.defineMethod("serialize", BB_VOID, PUB_FINAL)
            .withParameters(BB_DOSR, BB_DATAOBJECT, BB_BESV).throwing(BB_IOX).intercept(
                new SerializeImplementation(bindingInterface, startEvent, children)).make(),
            children.stream().filter(Predicates.instanceOf(AbstractStreamChild.class))
            .map(AbstractStreamChild.class::cast).map(AbstractStreamChild::streamerClass)
            .collect(ImmutableList.toImmutableList()));

        LOG.trace("Definition of {} done", fqn);
        return result;
    }

    private AbstractChild createDefinition(final CodecClassLoader loader, final DataSchemaNode child,
            final Method getter) {
        if (child instanceof LeafSchemaNode) {
            return new LeafChild(getter, child);
        }
        if (child instanceof ContainerSchemaNode) {
            final Class<? extends DataObject> itemClass = getter.getReturnType().asSubclass(DataObject.class);
            return new ContainerChild(getter, registry.getDataObjectSerializer(itemClass));
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
            final DataObjectStreamer<?> streamer = registry.getDataObjectSerializer(itemClass);

            final ListSchemaNode casted = (ListSchemaNode) child;
            if (casted.getKeyDefinition().isEmpty()) {
                return new ListChild(getter, streamer, itemClass);
            }
            return casted.isUserOrdered() ? new OrderedMapChild(getter, streamer, itemClass)
                    : new MapChild(getter, streamer, itemClass);
        }
        if (child instanceof ChoiceSchemaNode) {
            return new ChoiceChild(getter);
        }
        if (child instanceof AnyXmlSchemaNode) {
            return new AnyxmlChild(getter, child);
        }
        if (child instanceof LeafListSchemaNode) {
            return ((LeafListSchemaNode) child).isUserOrdered() ? new OrderedLeaflistChild(getter, child)
                    : new LeaflistChild(getter, child);
        }

        LOG.debug("Ignoring {} due to unhandled schema {}", getter, child);
        return null;
    }

    private abstract static class AbstractAppender implements ByteCodeAppender {
        static final StackManipulation REG = MethodVariableAccess.REFERENCE.loadFrom(1);
        static final StackManipulation OBJ = MethodVariableAccess.REFERENCE.loadFrom(2);
        static final StackManipulation STREAM = MethodVariableAccess.REFERENCE.loadFrom(3);

    }

    private abstract static class AbstractChild extends AbstractAppender {
        final Method getter;

        AbstractChild(final Method getter) {
            this.getter = requireNonNull(getter);
        }

        final StackManipulation getterInvocation() {
            return invokeMethod(getter);
        }
    }

    private abstract static class AbstractQNameChild extends AbstractChild {
        private final QName qname;

        AbstractQNameChild(final Method getter, final DataSchemaNode schema) {
            super(getter);
            this.qname = schema.getQName();
        }

        @Override
        public final Size apply(final MethodVisitor methodVisitor, final Context implementationContext,
                final MethodDescription instrumentedMethod) {
            StackManipulation.Size operandStackSize = new StackManipulation.Compound(
                // streamXXX(stream, "foo", obj.getFoo());
                STREAM,
                new TextConstant(qname.getLocalName()),
                OBJ,
                getterInvocation(),
                method())
                    .apply(methodVisitor, implementationContext);
            return new Size(operandStackSize.getMaximalSize(), instrumentedMethod.getStackSize());
        }

        abstract StackManipulation method();
    }

    private abstract static class AbstractStreamChild extends AbstractChild {
        private final DataObjectStreamer<?> streamer;

        AbstractStreamChild(final Method getter, final  DataObjectStreamer<?> streamer) {
            super(getter);
            this.streamer = requireNonNull(streamer);
        }

        final Class<?> streamerClass() {
            return streamer.getClass();
        }

        final StackManipulation streamerInstance() {
            try {
                return getField(streamerClass().getDeclaredField(INSTANCE_FIELD));
            } catch (NoSuchFieldException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private abstract static class AbstractListChild extends AbstractStreamChild {
        private final Class<? extends DataObject> itemClass;

        AbstractListChild(final Method getter, final DataObjectStreamer<?> streamer,
                final Class<? extends DataObject> itemClass) {
            super(getter, streamer);
            this.itemClass = requireNonNull(itemClass);
        }

        @Override
        public final Size apply(final MethodVisitor methodVisitor, final Context implementationContext,
                final MethodDescription instrumentedMethod) {
            StackManipulation.Size operandStackSize = new StackManipulation.Compound(
                // streamXXX(Foo.class, FooStreamer.INSTACE, reg, stream, obj.getFoo());
                ClassConstant.of(Sort.describe(itemClass).asErasure()),
                streamerInstance(),
                REG,
                STREAM,
                OBJ,
                getterInvocation(),
                method())
                    .apply(methodVisitor, implementationContext);
            return new Size(operandStackSize.getMaximalSize(), instrumentedMethod.getStackSize());
        }

        abstract StackManipulation method();
    }

    private static final class Augmentations extends AbstractAppender {
        static final Augmentations INSTANCE = new Augmentations();

        private static final StackManipulation METHOD = invokeMethod(DataObjectStreamer.class, "streamAugmentations",
            DataObjectSerializerRegistry.class, BindingStreamEventWriter.class, Augmentable.class);

        @Override
        public Size apply(final MethodVisitor methodVisitor, final Context implementationContext,
                final MethodDescription instrumentedMethod) {
            StackManipulation.Size operandStackSize = new StackManipulation.Compound(
                // streamAugmentations(reg, stream, obj);
                REG,
                STREAM,
                OBJ,
                METHOD)
                    .apply(methodVisitor, implementationContext);
            return new Size(operandStackSize.getMaximalSize(), instrumentedMethod.getStackSize());
        }
    }

    private static final class AnyxmlChild extends AbstractQNameChild {
        private static final StackManipulation METHOD = invokeMethod(DataObjectStreamer.class, "streamAnyxml",
            BindingStreamEventWriter.class, String.class, Object.class);

        AnyxmlChild(final Method getter, final DataSchemaNode schema) {
            super(getter, schema);
        }

        @Override
        StackManipulation method() {
            return METHOD;
        }
    }

    private static final class ChoiceChild extends AbstractChild {
        private static final StackManipulation METHOD = invokeMethod(DataObjectStreamer.class, "streamChoice",
            Class.class, DataObjectSerializerRegistry.class, BindingStreamEventWriter.class, DataContainer.class);

        ChoiceChild(final Method getter) {
            super(getter);
        }

        @Override
        public Size apply(final MethodVisitor methodVisitor, final Context implementationContext,
                final MethodDescription instrumentedMethod) {
            StackManipulation.Size operandStackSize = new StackManipulation.Compound(
                // streamChoice(Foo.class, reg, stream, obj.getFoo());
                ClassConstant.of(Sort.describe(getter.getReturnType()).asErasure()),
                REG,
                STREAM,
                OBJ,
                getterInvocation(),
                METHOD)
                    .apply(methodVisitor, implementationContext);
            return new Size(operandStackSize.getMaximalSize(), instrumentedMethod.getStackSize());
        }
    }

    private static final class ContainerChild extends AbstractStreamChild {
        private static final StackManipulation METHOD = invokeMethod(DataObjectStreamer.class, "streamContainer",
            DataObjectStreamer.class, DataObjectSerializerRegistry.class, BindingStreamEventWriter.class,
            DataObject.class);

        ContainerChild(final Method getter, final DataObjectStreamer<?> streamer) {
            super(getter, streamer);
        }

        @Override
        public Size apply(final MethodVisitor methodVisitor, final Context implementationContext,
                final MethodDescription instrumentedMethod) {
            StackManipulation.Size operandStackSize = new StackManipulation.Compound(
                // streamContainer(FooStreamer.INSTANCE, reg, stream, obj.getFoo());
                streamerInstance(),
                REG,
                STREAM,
                OBJ,
                getterInvocation(),
                METHOD)
                    .apply(methodVisitor, implementationContext);
            return new Size(operandStackSize.getMaximalSize(), instrumentedMethod.getStackSize());
        }
    }

    private static final class LeafChild extends AbstractQNameChild {
        private static final StackManipulation METHOD = invokeMethod(
            DataObjectStreamer.class, "streamLeaf", BindingStreamEventWriter.class, String.class, Object.class);

        LeafChild(final Method getter, final DataSchemaNode schema) {
            super(getter, schema);
        }

        @Override
        StackManipulation method() {
            return METHOD;
        }
    }

    private final static class LeaflistChild extends AbstractQNameChild {
        private static final StackManipulation METHOD = invokeMethod(DataObjectStreamer.class, "streamLeafList",
            BindingStreamEventWriter.class, String.class, List.class);

        LeaflistChild(final Method getter, final DataSchemaNode schema) {
            super(getter, schema);
        }

        @Override
        StackManipulation method() {
            return METHOD;
        }
    }

    private final static class OrderedLeaflistChild extends AbstractQNameChild {
        private static final StackManipulation METHOD = invokeMethod(DataObjectStreamer.class, "streamOrderedLeafList",
            BindingStreamEventWriter.class, String.class, List.class);

        OrderedLeaflistChild(final Method getter, final DataSchemaNode schema) {
            super(getter, schema);
        }

        @Override
        StackManipulation method() {
            return METHOD;
        }
    }

    private static final class ListChild extends AbstractListChild {
        private static final StackManipulation METHOD = invokeMethod(DataObjectStreamer.class, "streamList",
            Class.class, DataObjectStreamer.class, DataObjectSerializerRegistry.class, BindingStreamEventWriter.class,
            List.class);

        ListChild(final Method getter, final DataObjectStreamer<?> streamer,
                final Class<? extends DataObject> itemClass) {
            super(getter, streamer, itemClass);
        }

        @Override
        StackManipulation method() {
            return METHOD;
        }
    }

    private static final class MapChild extends AbstractListChild {
        private static final StackManipulation METHOD = invokeMethod(DataObjectStreamer.class, "streamMap",
            Class.class, DataObjectStreamer.class, DataObjectSerializerRegistry.class, BindingStreamEventWriter.class,
            List.class);

        MapChild(final Method getter, final DataObjectStreamer<?> streamer,
                final Class<? extends DataObject> itemClass) {
            super(getter, streamer, itemClass);
        }

        @Override
        StackManipulation method() {
            return METHOD;
        }
    }

    private static final class OrderedMapChild extends AbstractListChild {
        private static final StackManipulation METHOD = invokeMethod(DataObjectStreamer.class, "streamOrderedMap",
            Class.class, DataObjectStreamer.class, DataObjectSerializerRegistry.class, BindingStreamEventWriter.class,
            List.class);

        OrderedMapChild(final Method getter, final DataObjectStreamer<?> streamer,
                final Class<? extends DataObject> itemClass) {
            super(getter, streamer, itemClass);
        }

        @Override
        StackManipulation method() {
            return METHOD;
        }
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

    private static final class SerializeImplementation implements Implementation {
        private final List<AbstractChild> children;
        private final ByteCodeAppender startEvent;
        private final Class<?> bindingInterface;

        SerializeImplementation(final Class<?> bindingInterface, final ByteCodeAppender startEvent,
                final List<AbstractChild> children) {
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
            final List<ByteCodeAppender> invocations = new ArrayList<>();

            // Open this node ...
            invocations.add(startEvent);

            // ... emit children ...
            invocations.addAll(children);

            // ... optionally emit augmentations ...
            if (Augmentable.class.isAssignableFrom(bindingInterface)) {
                invocations.add(Augmentations.INSTANCE);
            }

            // ... close this node
            invocations.add(EndNode.INSTANCE);
            return new ByteCodeAppender.Compound(invocations);
        }
    }

    private abstract static class AbstractDOSIMethod extends AbstractAppender {
        static final StackManipulation UNKNOWN_SIZE = IntegerConstant.forValue(BindingStreamEventWriter.UNKNOWN_SIZE);
    }

    private abstract static class AbstractClassUnknownSizeMethod extends AbstractDOSIMethod {
        private final Class<?> type;

        AbstractClassUnknownSizeMethod(final Class<?> type) {
            this.type = requireNonNull(type);
        }

        @Override
        public final Size apply(final MethodVisitor methodVisitor, final Context implementationContext,
                final MethodDescription instrumentedMethod) {
            StackManipulation.Size operandStackSize = new StackManipulation.Compound(
                // stream.<METHOD>(Foo.class, UNKNOWN_SIZE);
                STREAM,
                ClassConstant.of(Sort.describe(type).asErasure()),
                UNKNOWN_SIZE,
                method())
                    .apply(methodVisitor, implementationContext);
            return new Size(operandStackSize.getMaximalSize(), instrumentedMethod.getStackSize());
        }

        abstract StackManipulation method();
    }

    private static final class EndNode extends AbstractDOSIMethod {
        static final ByteCodeAppender INSTANCE = new EndNode();

        private static final StackManipulation METHOD = invokeMethod(BindingStreamEventWriter.class, "endNode");

        @Override
        public Size apply(final MethodVisitor methodVisitor, final Context implementationContext,
                final MethodDescription instrumentedMethod) {
            StackManipulation.Size operandStackSize = new StackManipulation.Compound(
                // stream.endNode();
                // return;
                STREAM,
                METHOD,
                MethodReturn.VOID)
                    .apply(methodVisitor, implementationContext);
            return new Size(operandStackSize.getMaximalSize(), instrumentedMethod.getStackSize());
        }
    }

    private static final class StartAugmentationNode extends AbstractDOSIMethod {
        private static final StackManipulation METHOD = invokeMethod(BindingStreamEventWriter.class,
            "startAugmentationNode", Class.class);

        private final Class<?> type;

        StartAugmentationNode(final Class<?> type) {
            this.type = requireNonNull(type);
        }

        @Override
        public Size apply(final MethodVisitor methodVisitor, final Context implementationContext,
                final MethodDescription instrumentedMethod) {
            StackManipulation.Size operandStackSize = new StackManipulation.Compound(
                // stream.startAugmentationNode(Foo.class);
                STREAM,
                ClassConstant.of(Sort.describe(type).asErasure()),
                METHOD)
                    .apply(methodVisitor, implementationContext);
            return new Size(operandStackSize.getMaximalSize(), instrumentedMethod.getStackSize());
        }
    }

    private static final class StartCase extends AbstractClassUnknownSizeMethod {
        private static final StackManipulation METHOD = invokeMethod(BindingStreamEventWriter.class, "startCase",
            Class.class, int.class);

        StartCase(final Class<?> type) {
            super(type);
        }

        @Override
        StackManipulation method() {
            return METHOD;
        }
    }

    private static final class StartContainerNode extends AbstractClassUnknownSizeMethod {
        private static final StackManipulation METHOD = invokeMethod(BindingStreamEventWriter.class,
            "startContainerNode", Class.class, int.class);

        StartContainerNode(final Class<?> type) {
            super(type);
        }

        @Override
        StackManipulation method() {
            return METHOD;
        }
    }

    private static final class StartMapEntryNode extends AbstractDOSIMethod {
        static final ByteCodeAppender INSTANCE = new StartMapEntryNode();

        private static final StackManipulation METHOD = invokeMethod(BindingStreamEventWriter.class,
            "startMapEntryNode", Identifier.class, int.class);
        private static final StackManipulation GET_KEY = invokeMethod(Identifiable.class, "key");

        @Override
        public Size apply(final MethodVisitor methodVisitor, final Context implementationContext,
                final MethodDescription instrumentedMethod) {
            StackManipulation.Size operandStackSize = new StackManipulation.Compound(
                // stream.startMapEntryNode(obj.key(), UNKNOWN_SIZE);
                STREAM,
                OBJ,
                GET_KEY,
                UNKNOWN_SIZE,
                METHOD)
                    .apply(methodVisitor, implementationContext);
            return new Size(operandStackSize.getMaximalSize(), instrumentedMethod.getStackSize());
        }
    }

    private static final class StartUnkeyedListItem extends AbstractDOSIMethod {
        static final ByteCodeAppender INSTANCE = new StartUnkeyedListItem();

        private static final StackManipulation METHOD = invokeMethod(BindingStreamEventWriter.class,
            "startUnkeyedListItem", int.class);

        @Override
        public Size apply(final MethodVisitor methodVisitor, final Context implementationContext,
                final MethodDescription instrumentedMethod) {
            StackManipulation.Size operandStackSize = new StackManipulation.Compound(
                // stream.startUnkeyedListItem(UNKNOWN_SIZE);
                STREAM,
                UNKNOWN_SIZE,
                METHOD)
                    .apply(methodVisitor, implementationContext);
            return new Size(operandStackSize.getMaximalSize(), instrumentedMethod.getStackSize());
        }
    }
}
