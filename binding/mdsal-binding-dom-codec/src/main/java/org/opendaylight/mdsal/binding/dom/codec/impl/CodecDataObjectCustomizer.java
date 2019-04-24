/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.field.FieldDescription.InDefinedShape;
import net.bytebuddy.description.field.FieldList;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodDescription.ForLoadedMethod;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.Implementation.Context;
import net.bytebuddy.implementation.bytecode.Addition;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.Multiplication;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.assign.TypeCasting;
import net.bytebuddy.implementation.bytecode.constant.ClassConstant;
import net.bytebuddy.implementation.bytecode.constant.IntegerConstant;
import net.bytebuddy.implementation.bytecode.constant.TextConstant;
import net.bytebuddy.implementation.bytecode.member.FieldAccess;
import net.bytebuddy.implementation.bytecode.member.FieldAccess.Defined;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.ClassWriter;
import net.bytebuddy.jar.asm.Label;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.dom.codec.loader.CodecClassLoader;
import org.opendaylight.mdsal.binding.dom.codec.loader.CodecClassLoader.ByteBuddyCustomizer;
import org.opendaylight.mdsal.binding.dom.codec.loader.CodecClassLoader.ByteBuddyResult;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Private support for generating AbstractDataObject specializations.
 */
final class CodecDataObjectCustomizer<T extends CodecDataObject<?>> implements ByteBuddyCustomizer<T> {
    enum EnableFramesComputing implements AsmVisitorWrapper {
        INSTANCE;

        @Override
        public int mergeWriter(final int flags) {
            return flags | ClassWriter.COMPUTE_FRAMES;
        }

        @Override
        public int mergeReader(final int flags) {
            return flags | ClassWriter.COMPUTE_FRAMES;
        }

        @Override
        public ClassVisitor wrap(final TypeDescription td, final ClassVisitor cv, final Implementation.Context ctx,
                final TypePool tp, final FieldList<FieldDescription.InDefinedShape> fields, final MethodList<?> methods,
                final int wflags, final int rflags) {
            return cv;
        }
    }

    private static final class IfEq implements StackManipulation {
        private static final StackManipulation.Size SIZE = new StackManipulation.Size(-1, 0);

        private final Label label;

        IfEq(final Label label) {
            this.label = requireNonNull(label);
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public StackManipulation.Size apply(final MethodVisitor mv, final Implementation.Context ctx) {
            mv.visitJumpInsn(Opcodes.IFEQ, label);
            return SIZE;
        }
    }

    private static final class Mark implements StackManipulation {
        private static final StackManipulation.Size SIZE = new StackManipulation.Size(0, 0);

        private final Label label;

        Mark(final Label label) {
            this.label = requireNonNull(label);
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public StackManipulation.Size apply(final MethodVisitor mv, final Implementation.Context ctx) {
            mv.visitLabel(label);
            return SIZE;
        }
    }

    private abstract static class AbstractFieldInitializer implements ByteCodeAppender {
        private final String methodName;
        private final String fieldName;

        AbstractFieldInitializer(final String methodName, final String fieldName) {
            this.methodName = requireNonNull(methodName);
            this.fieldName = requireNonNull(fieldName);
        }

        final Defined fieldAccess(final Context implementationContext) {
            return FieldAccess.forField(implementationContext.getInstrumentedType().getDeclaredFields()
                .filter(ElementMatchers.named(fieldName)).getOnly());
        }

        final TextConstant methodNameText() {
            return new TextConstant(methodName);
        }
    }

    private static final class ArfuInitializer extends AbstractFieldInitializer {
        private static final StackManipulation OBJECT_CLASS = ClassConstant.of(TypeDescription.OBJECT);
        private static final StackManipulation ARFU_NEWUPDATER =  MethodInvocation.invoke(
            findMethod(AtomicReferenceFieldUpdater.class, "newUpdater", Class.class, Class.class, String.class));

        ArfuInitializer(final String methodName, final String fieldName) {
            super(methodName, fieldName);
        }

        @Override
        public Size apply(final MethodVisitor methodVisitor, final Context implementationContext,
                final MethodDescription instrumentedMethod) {

            StackManipulation.Size operandStackSize = new StackManipulation.Compound(
                ClassConstant.of(implementationContext.getInstrumentedType()),
                OBJECT_CLASS,
                methodNameText(),
                ARFU_NEWUPDATER,
                fieldAccess(implementationContext).write())
                    .apply(methodVisitor, implementationContext);

            return new Size(operandStackSize.getMaximalSize(), instrumentedMethod.getStackSize());
        }
    }

    private static final class NcsInitializer extends AbstractFieldInitializer {
        private static final StackManipulation BRIDGE_RESOLVE = MethodInvocation.invoke(
            findMethod(CodecDataObjectBridge.class, "resolve", String.class));

        NcsInitializer(final String methodName, final String fieldName) {
            super(methodName, fieldName);
        }

        @Override
        public Size apply(final MethodVisitor methodVisitor, final Context implementationContext,
                final MethodDescription instrumentedMethod) {
            StackManipulation.Size operandStackSize = new StackManipulation.Compound(
                methodNameText(),
                BRIDGE_RESOLVE,
                fieldAccess(implementationContext).write())
                    .apply(methodVisitor, implementationContext);
            return new Size(operandStackSize.getMaximalSize(), instrumentedMethod.getStackSize());
        }
    }

    private static final class IicInitializer extends AbstractFieldInitializer {
        private static final StackManipulation BRIDGE_RESOLVE = MethodInvocation.invoke(
            findMethod(CodecDataObjectBridge.class, "resolveKey", String.class));

        IicInitializer(final String methodName, final String fieldName) {
            super(methodName, fieldName);
        }

        @Override
        public Size apply(final MethodVisitor methodVisitor, final Context implementationContext,
                final MethodDescription instrumentedMethod) {
            StackManipulation.Size operandStackSize = new StackManipulation.Compound(
                methodNameText(),
                BRIDGE_RESOLVE,
                fieldAccess(implementationContext).write())
                    .apply(methodVisitor, implementationContext);
            return new Size(operandStackSize.getMaximalSize(), instrumentedMethod.getStackSize());
        }
    }

    private abstract static class AbstractAppender implements ByteCodeAppender {
        static final StackManipulation THIS = MethodVariableAccess.loadThis();

    }

    private abstract static class AbstractMethod extends AbstractAppender {
        private final String arfuName;
        private final String ctxName;

        AbstractMethod(final String arfuName, final String ctxName) {
            this.arfuName = requireNonNull(arfuName);
            this.ctxName = requireNonNull(ctxName);
        }

        @Override
        public final Size apply(final MethodVisitor methodVisitor, final Context implementationContext,
                final MethodDescription instrumentedMethod) {
            final TypeDescription retType = instrumentedMethod.getReturnType().asErasure();
            checkArgument(!retType.isPrimitive(), "%s must return a non-primitive", instrumentedMethod);

            final FieldList<InDefinedShape> fields = implementationContext.getInstrumentedType().getDeclaredFields();

            StackManipulation.Size operandStackSize = new StackManipulation.Compound(
                THIS,
                getField(fields, arfuName),
                getField(fields, ctxName),
                codecMember(),
                TypeCasting.to(retType),
                MethodReturn.REFERENCE)
                    .apply(methodVisitor, implementationContext);

            return new Size(operandStackSize.getMaximalSize(), instrumentedMethod.getStackSize());
        }

        abstract StackManipulation codecMember();

        private static StackManipulation getField(final FieldList<InDefinedShape> fields, final String name) {
            return FieldAccess.forField(fields.filter(ElementMatchers.named(name)).getOnly()).read();
        }
    }

    private static final class GetMethod extends AbstractMethod {
        private static final StackManipulation CODEC_MEMBER = MethodInvocation.invoke(findMethod(CodecDataObject.class,
            "codecMember", AtomicReferenceFieldUpdater.class, NodeContextSupplier.class));

        GetMethod(final String arfuName, final String ctxName) {
            super(arfuName, ctxName);
        }

        @Override
        StackManipulation codecMember() {
            return CODEC_MEMBER;
        }
    }

    private abstract static class AbstractMethodImplementation implements Implementation {
        private static final Generic BB_ARFU = TypeDefinition.Sort.describe(AtomicReferenceFieldUpdater.class);
        private static final Generic BB_OBJECT = TypeDefinition.Sort.describe(Object.class);
        private static final int PRIV_VOLATILE = Opcodes.ACC_PRIVATE | Opcodes.ACC_VOLATILE | Opcodes.ACC_SYNTHETIC;

        static final int PRIV_CONST = Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL
                | Opcodes.ACC_SYNTHETIC;

        final String methodName;

        AbstractMethodImplementation(final String methodName) {
            this.methodName = requireNonNull(methodName);
        }

        @Override
        public final InstrumentedType prepare(final InstrumentedType instrumentedType) {
            final String arfuName = arfuName(methodName);

            return prepare(instrumentedType
                // AtomicReferenceFieldUpdater ...
                .withField(new FieldDescription.Token(arfuName, PRIV_CONST, BB_ARFU))
                .withInitializer(new ArfuInitializer(methodName, arfuName))
                // ... corresponding volatile field ...
                .withField(new FieldDescription.Token(methodName, PRIV_VOLATILE, BB_OBJECT)), ctxName(methodName));
        }

        @Override
        public final ByteCodeAppender appender(final Target implementationTarget) {
            return appender(implementationTarget, arfuName(methodName), ctxName(methodName));
        }

        abstract InstrumentedType prepare(InstrumentedType instrumentedType, String ctxName);

        abstract ByteCodeAppender appender(Target implementationTarget, String arfuName, String ctxName);

        private static String arfuName(final String methodName) {
            return methodName + "$$$ARFU";
        }

        private static String ctxName(final String methodName) {
            return methodName + "$$$CTX";
        }
    }

    private static final class GetMethodImplementation extends AbstractMethodImplementation {
        private static final Generic BB_NCS = TypeDefinition.Sort.describe(NodeContextSupplier.class);

        GetMethodImplementation(final String methodName) {
            super(methodName);
        }

        @Override
        InstrumentedType prepare(final InstrumentedType instrumentedType, final String ctxName) {
            return instrumentedType
                    // NodeContextSupplier ...
                    .withField(new FieldDescription.Token(ctxName, PRIV_CONST, BB_NCS))
                    .withInitializer(new NcsInitializer(methodName, ctxName));
        }

        @Override
        ByteCodeAppender appender(final Target implementationTarget, final String arfuName, final String ctxName) {
            return new GetMethod(arfuName, ctxName);
        }
    }

    private static final class KeyMethod extends AbstractMethod {
        private static final StackManipulation CODEC_MEMBER = MethodInvocation.invoke(findMethod(CodecDataObject.class,
            "codecMember", AtomicReferenceFieldUpdater.class, IdentifiableItemCodec.class));

        KeyMethod(final String arfuName, final String ctxName) {
            super(arfuName, ctxName);
        }

        @Override
        StackManipulation codecMember() {
            return CODEC_MEMBER;
        }
    }

    private static final class KeyMethodImplementation extends AbstractMethodImplementation {
        private static final Generic BB_IIC = TypeDefinition.Sort.describe(IdentifiableItemCodec.class);

        KeyMethodImplementation(final String methodName) {
            super(methodName);
        }

        @Override
        InstrumentedType prepare(final InstrumentedType instrumentedType, final String ctxName) {
            return instrumentedType
                    // IdentifiableItemCodec ...
                    .withField(new FieldDescription.Token(ctxName, PRIV_CONST, BB_IIC))
                    .withInitializer(new IicInitializer(methodName, ctxName));
        }

        @Override
        ByteCodeAppender appender(final Target implementationTarget, final String arfuName, final String ctxName) {
            return new KeyMethod(arfuName, ctxName);
        }
    }

    private abstract static class AbstractAllPropertiesAppender extends AbstractAppender {
        final ImmutableMap<StackManipulation, Method> properties;

        AbstractAllPropertiesAppender(final ImmutableMap<StackManipulation, Method> properties) {
            this.properties = requireNonNull(properties);
        }
    }

    private static final class Equals extends AbstractAllPropertiesAppender {
        private static final StackManipulation ARRAYS_EQUALS = MethodInvocation.invoke(findMethod(Arrays.class,
            "equals", byte[].class, byte[].class));
        private static final StackManipulation OBJECTS_EQUALS = MethodInvocation.invoke(findMethod(Objects.class,
            "equals", Object.class, Object.class));
        private static final StackManipulation OBJ = MethodVariableAccess.REFERENCE.loadFrom(1);

        Equals(final ImmutableMap<StackManipulation, Method> properties) {
            super(properties);
        }

        @Override
        public Size apply(final MethodVisitor methodVisitor, final Context implementationContext,
                final MethodDescription instrumentedMethod) {
            final Label falseLabel = new Label();
            final IfEq ifFalse = new IfEq(falseLabel);

            final List<StackManipulation> manipulations = new ArrayList<>(properties.size() * 6 + 5);
            for (Entry<StackManipulation, Method> entry : properties.entrySet()) {
                manipulations.add(THIS);
                manipulations.add(entry.getKey());
                manipulations.add(OBJ);
                manipulations.add(entry.getKey());
                manipulations.add(entry.getValue().getReturnType().isArray() ? ARRAYS_EQUALS : OBJECTS_EQUALS);
                manipulations.add(ifFalse);
            }

            manipulations.add(IntegerConstant.ONE);
            manipulations.add(MethodReturn.INTEGER);
            manipulations.add(new Mark(falseLabel));
            manipulations.add(IntegerConstant.ZERO);
            manipulations.add(MethodReturn.INTEGER);

            StackManipulation.Size operandStackSize = new StackManipulation.Compound(manipulations)
                    .apply(methodVisitor, implementationContext);
            return new Size(operandStackSize.getMaximalSize(), instrumentedMethod.getStackSize());
        }
    }

    private static final class HashCode extends AbstractAllPropertiesAppender {
        private static final int RESULT = 1;
        private static final StackManipulation THIRTY_ONE = IntegerConstant.forValue(31);
        private static final StackManipulation LOAD_RESULT = MethodVariableAccess.INTEGER.loadFrom(RESULT);
        private static final StackManipulation STORE_RESULT = MethodVariableAccess.INTEGER.storeAt(RESULT);

        private static final StackManipulation ARRAYS_HASHCODE = MethodInvocation.invoke(findMethod(Arrays.class,
            "hashCode", byte[].class));
        private static final StackManipulation OBJECTS_HASHCODE = MethodInvocation.invoke(findMethod(Objects.class,
            "hashCode", Object.class));

        HashCode(final ImmutableMap<StackManipulation, Method> properties) {
            super(properties);
        }

        @Override
        public Size apply(final MethodVisitor methodVisitor, final Context implementationContext,
                final MethodDescription instrumentedMethod) {

            final List<StackManipulation> manipulations = new ArrayList<>(properties.size() * 8 + 4);
            manipulations.add(IntegerConstant.ONE);
            manipulations.add(STORE_RESULT);

            for (Entry<StackManipulation, Method> entry : properties.entrySet()) {
                manipulations.add(THIRTY_ONE);
                manipulations.add(LOAD_RESULT);
                manipulations.add(Multiplication.INTEGER);
                manipulations.add(THIS);
                manipulations.add(entry.getKey());
                manipulations.add(entry.getValue().getReturnType().isArray() ? ARRAYS_HASHCODE : OBJECTS_HASHCODE);
                manipulations.add(Addition.INTEGER);
                manipulations.add(STORE_RESULT);
            }
            manipulations.add(LOAD_RESULT);
            manipulations.add(MethodReturn.INTEGER);

            StackManipulation.Size operandStackSize = new StackManipulation.Compound(manipulations)
                    .apply(methodVisitor, implementationContext);
            return new Size(operandStackSize.getMaximalSize(), instrumentedMethod.getStackSize() + 1);
        }
    }

    private static final class ToString extends AbstractAllPropertiesAppender {
        private static final StackManipulation HELPER = MethodVariableAccess.REFERENCE.loadFrom(1);
        private static final StackManipulation HELPER_ADD = MethodInvocation.invoke(findMethod(ToStringHelper.class,
            "add", String.class, Object.class));

        ToString(final ImmutableMap<StackManipulation, Method> properties) {
            super(properties);
        }

        @Override
        public Size apply(final MethodVisitor methodVisitor, final Context implementationContext,
                final MethodDescription instrumentedMethod) {

            final List<StackManipulation> manipulations = new ArrayList<>(properties.size() * 4 + 2);
            manipulations.add(HELPER);
            for (Entry<StackManipulation, Method> entry : properties.entrySet()) {
                manipulations.add(new TextConstant(entry.getValue().getName()));
                manipulations.add(THIS);
                manipulations.add(entry.getKey());
                manipulations.add(HELPER_ADD);
            }
            manipulations.add(MethodReturn.REFERENCE);

            StackManipulation.Size operandStackSize = new StackManipulation.Compound(manipulations)
                    .apply(methodVisitor, implementationContext);
            return new Size(operandStackSize.getMaximalSize(), instrumentedMethod.getStackSize());        }

    }

    private abstract static class AbstractImplementation implements Implementation {
        @Override
        public final InstrumentedType prepare(final InstrumentedType instrumentedType) {
            return instrumentedType;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(CodecDataObjectCustomizer.class);
    private static final Generic BB_BOOLEAN = TypeDefinition.Sort.describe(boolean.class);
    private static final Generic BB_DATAOBJECT = TypeDefinition.Sort.describe(DataObject.class);
    private static final Generic BB_HELPER = TypeDefinition.Sort.describe(ToStringHelper.class);
    private static final Generic BB_INT = TypeDefinition.Sort.describe(int.class);
    private static final int PROT_FINAL = Opcodes.ACC_PROTECTED | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC;
    private static final int PUB_FINAL = Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC;

    private final ImmutableMap<Method, NodeContextSupplier> properties;
    private final Entry<Method, IdentifiableItemCodec> keyMethod;

    CodecDataObjectCustomizer(final ImmutableMap<Method, NodeContextSupplier> properties,
            final @Nullable Entry<Method, IdentifiableItemCodec> keyMethod) {
        this.properties = requireNonNull(properties);
        this.keyMethod = keyMethod;
    }

    @Override
    public ByteBuddyResult<? extends T> customize(final CodecClassLoader loader,
            final Class<?> bindingInterface, final String fqn, final Builder<? extends T> builder) {
        LOG.trace("Generating class {}", fqn);

        Builder<? extends T> tmp = builder.visit(EnableFramesComputing.INSTANCE).implement(bindingInterface);

        for (Method method : properties.keySet()) {
            LOG.trace("Generating for method {}", method);
            final String methodName = method.getName();
            tmp = tmp.defineMethod(methodName, method.getReturnType(), PUB_FINAL)
                    .intercept(new GetMethodImplementation(methodName));
        }

        if (keyMethod != null) {
            final Method method = keyMethod.getKey();
            final String methodName = method.getName();

            tmp = tmp.defineMethod(methodName, method.getReturnType(), PUB_FINAL)
                    .intercept(new KeyMethodImplementation(methodName));
        }

        final ImmutableMap<StackManipulation, Method> methods = Maps.uniqueIndex(properties.keySet(),
            method -> MethodInvocation.invoke(new ForLoadedMethod(method)));

        // Final bits:
        return ByteBuddyResult.of(tmp
                // codecHashCode() ...
                .defineMethod("codecHashCode", BB_INT, PROT_FINAL)
                .intercept(new AbstractImplementation() {
                    @Override
                    public ByteCodeAppender appender(final Target implementationTarget) {
                        return new HashCode(methods);
                    }
                })
                // ... codecEquals() ...
                .defineMethod("codecEquals", BB_BOOLEAN, PROT_FINAL).withParameter(BB_DATAOBJECT)
                .intercept(new AbstractImplementation() {
                    @Override
                    public ByteCodeAppender appender(final Target implementationTarget) {
                        return new Equals(methods);
                    }
                })
                // ... and codecFillToString() ...
                .defineMethod("codecFillToString", BB_HELPER, PROT_FINAL).withParameter(BB_HELPER)
                .intercept(new AbstractImplementation() {
                    @Override
                    public ByteCodeAppender appender(final Target implementationTarget) {
                        return new ToString(methods);
                    }
                })
                // ... set class as final ...
                .modifiers(PUB_FINAL)
                // ... and build it
                .make());
    }

    @Override
    public Class<?> customizeLoading(final @NonNull Supplier<Class<?>> loader) {
        final CodecDataObjectCustomizer prev = CodecDataObjectBridge.setup(this);
        try {
            final Class<?> result = loader.get();

            /*
             * This a bit of magic to support NodeContextSupplier constants. These constants need to be resolved while
             * we have the information needed to find them -- that information is being held in this instance and we
             * leak it to a thread-local variable held by CodecDataObjectBridge.
             *
             * By default the JVM will defer class initialization to first use, which unfortunately is too late for
             * us, and hence we need to force class to initialize.
             */
            try {
                Class.forName(result.getName(), true, result.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new LinkageError("Failed to find newly-defined " + result, e);
            }

            return result;
        } finally {
            CodecDataObjectBridge.tearDown(prev);
        }
    }


    @NonNull NodeContextSupplier resolve(final @NonNull String methodName) {
        final Optional<Entry<Method, NodeContextSupplier>> found = properties.entrySet().stream()
                .filter(entry -> methodName.equals(entry.getKey().getName())).findAny();
        verify(found.isPresent(), "Failed to find property for %s in %s", methodName, this);
        return verifyNotNull(found.get().getValue());
    }

    @NonNull IdentifiableItemCodec resolveKey(final @NonNull String methodName) {
        return verifyNotNull(verifyNotNull(keyMethod, "No key method attached for %s in %s", methodName, this)
            .getValue());
    }

    static ForLoadedMethod findMethod(final Class<?> clazz, final String name, final Class<?>... args) {
        try {
            return new ForLoadedMethod(clazz.getDeclaredMethod(name, args));
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }
}
