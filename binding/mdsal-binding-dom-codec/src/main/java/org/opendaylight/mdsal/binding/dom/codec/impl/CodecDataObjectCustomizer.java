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
import static org.opendaylight.mdsal.binding.dom.codec.impl.ByteBuddyUtils.findMethod;

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
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.field.FieldDescription.InDefinedShape;
import net.bytebuddy.description.field.FieldList;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodDescription.ForLoadedMethod;
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
import net.bytebuddy.jar.asm.Label;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;
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
final class CodecDataObjectCustomizer implements ByteBuddyCustomizer {
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
    public <T> ByteBuddyResult<T> customize(final CodecClassLoader loader, final Class<?> bindingInterface,
            final String fqn, final Builder<T> builder) {
        LOG.trace("Generating class {}", fqn);

        Builder<T> tmp = builder.visit(ByteBuddyUtils.computeFrames()).implement(bindingInterface);

        for (Method method : properties.keySet()) {
            LOG.trace("Generating for method {}", method);
            final String methodName = method.getName();
            tmp = tmp.defineMethod(methodName, method.getReturnType(), PUB_FINAL)
                    .intercept(new GetMethodImplementation(methodName));
        }

        if (keyMethod != null) {
            LOG.trace("Generating for key {}", keyMethod);
            final Method method = keyMethod.getKey();
            final String methodName = method.getName();
            tmp = tmp.defineMethod(methodName, method.getReturnType(), PUB_FINAL)
                    .intercept(new KeyMethodImplementation(methodName));
        }

        // Index all property methods, turning them into "getFoo()" invocations, retaining order. We will be using
        // those invocations in each of the three methods. Note that we do not glue the invocations to 'this', as we
        // will be invoking them on 'other' in codecEquals()
        final ImmutableMap<StackManipulation, Method> methods = Maps.uniqueIndex(properties.keySet(),
            method -> MethodInvocation.invoke(new ForLoadedMethod(method)));

        // Final bits:
        return ByteBuddyResult.of(tmp
                // codecHashCode() ...
                .defineMethod("codecHashCode", BB_INT, PROT_FINAL)
                .intercept(new AbstractImplementation() {
                    @Override
                    public ByteCodeAppender appender(final Target implementationTarget) {
                        return new CodecHashCode(methods);
                    }
                })
                // ... codecEquals() ...
                .defineMethod("codecEquals", BB_BOOLEAN, PROT_FINAL).withParameter(BB_DATAOBJECT)
                .intercept(new AbstractImplementation() {
                    @Override
                    public ByteCodeAppender appender(final Target implementationTarget) {
                        return new CodecEquals(methods);
                    }
                })
                // ... and codecFillToString() ...
                .defineMethod("codecFillToString", BB_HELPER, PROT_FINAL).withParameter(BB_HELPER)
                .intercept(new AbstractImplementation() {
                    @Override
                    public ByteCodeAppender appender(final Target implementationTarget) {
                        return new CodecFillToString(methods);
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

    // Abstract base ByteCodeAppender with some static utilities
    private abstract static class AbstractAppender implements ByteCodeAppender {
        static final StackManipulation THIS = MethodVariableAccess.loadThis();

        static final StackManipulation getField(final FieldList<InDefinedShape> fields, final String fieldName) {
            return fieldAccess(fields, fieldName).read();
        }

        static final StackManipulation putField(final Context implementationContext, final String fieldName) {
            return fieldAccess(implementationContext.getInstrumentedType().getDeclaredFields(), fieldName).write();
        }

        private static Defined fieldAccess(final FieldList<InDefinedShape> fields, final String fieldName) {
            return FieldAccess.forField(fields.filter(ElementMatchers.named(fieldName)).getOnly());
        }
    }

    // Abstract implementation of a getFoo() or key() method
    private abstract static class AbstractMethodImplementation implements Implementation {
        // Method ByteCodeAppender is an inner class, because it needs to know the method name as well as the names
        // of generated constant fields
        abstract class AbstractMethod extends AbstractAppender {
            @Override
            public final Size apply(final MethodVisitor methodVisitor, final Context implementationContext,
                    final MethodDescription instrumentedMethod) {
                final TypeDescription retType = instrumentedMethod.getReturnType().asErasure();
                checkArgument(!retType.isPrimitive(), "%s must return a non-primitive", instrumentedMethod);

                final FieldList<InDefinedShape> fields = implementationContext.getInstrumentedType()
                        .getDeclaredFields();
                StackManipulation.Size operandStackSize = new StackManipulation.Compound(
                    // return (FooType) codecMember(getFoo$$$A, getFoo$$$C);
                    THIS,
                    getField(fields, arfuName()),
                    getField(fields, contextName()),
                    codecMember(),
                    TypeCasting.to(retType),
                    MethodReturn.REFERENCE)
                        .apply(methodVisitor, implementationContext);

                return new Size(operandStackSize.getMaximalSize(), instrumentedMethod.getStackSize());
            }

            abstract StackManipulation codecMember();
        }

        // initializer of the AtomicReferenceFieldUpdater field
        private final class ArfuInitializer extends AbstractAppender {
            @Override
            public Size apply(final MethodVisitor methodVisitor, final Context implementationContext,
                    final MethodDescription instrumentedMethod) {

                StackManipulation.Size operandStackSize = new StackManipulation.Compound(
                    // ... = AtomicReferenceFieldUpdater.newUpdater(This.class, Object.class, "getFoo");
                    ClassConstant.of(implementationContext.getInstrumentedType()),
                    OBJECT_CLASS,
                    methodNameText(),
                    ARFU_NEWUPDATER,
                    putField(implementationContext, arfuName()))
                        .apply(methodVisitor, implementationContext);

                return new Size(operandStackSize.getMaximalSize(), instrumentedMethod.getStackSize());
            }
        }

        // abstract initializer for the context field
        abstract class AbstractContextInitializer extends AbstractAppender {
            @Override
            public final Size apply(final MethodVisitor methodVisitor, final Context implementationContext,
                    final MethodDescription instrumentedMethod) {
                StackManipulation.Size operandStackSize = new StackManipulation.Compound(
                    // ... = CodecDataObjectBridge.{resolve,resolveKey}("getFoo");
                    methodNameText(),
                    resolveMethod(),
                    putField(implementationContext, contextName()))
                        .apply(methodVisitor, implementationContext);
                return new Size(operandStackSize.getMaximalSize(), instrumentedMethod.getStackSize());
            }

            abstract StackManipulation resolveMethod();
        }

        private static final Generic BB_ARFU = TypeDefinition.Sort.describe(AtomicReferenceFieldUpdater.class);
        private static final Generic BB_OBJECT = TypeDefinition.Sort.describe(Object.class);
        private static final StackManipulation OBJECT_CLASS = ClassConstant.of(TypeDescription.OBJECT);
        private static final StackManipulation ARFU_NEWUPDATER =  MethodInvocation.invoke(
            findMethod(AtomicReferenceFieldUpdater.class, "newUpdater", Class.class, Class.class, String.class));

        private static final int PRIV_VOLATILE = Opcodes.ACC_PRIVATE | Opcodes.ACC_VOLATILE | Opcodes.ACC_SYNTHETIC;

        static final int PRIV_CONST = Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL
                | Opcodes.ACC_SYNTHETIC;

        // We will use method name and field names multiple times, as well the String containing the method name. We
        // keep them in a central place to avoid duplication and memory overhead
        private final TextConstant methodNameText;
        private final String methodName;
        private final String arfuName;
        private final String contextName;

        AbstractMethodImplementation(final String methodName) {
            this.methodName = requireNonNull(methodName);
            this.methodNameText = new TextConstant(methodName);
            this.arfuName = methodName + "$$$A";
            this.contextName = methodName + "$$$C";
        }

        // getFoo$$$A
        final String arfuName() {
            return arfuName;
        }

        // getFoo$$$C
        final String contextName() {
            return contextName;
        }

        // "getFoo"
        final TextConstant methodNameText() {
            return methodNameText;
        }

        @Override
        public InstrumentedType prepare(final InstrumentedType instrumentedType) {
            return instrumentedType
                // private static final AtomicReferenceFieldUpdater<This, Object> getFoo$$$A = ...
                .withField(new FieldDescription.Token(arfuName, PRIV_CONST, BB_ARFU))
                .withInitializer(new ArfuInitializer())
                // private volatile Object getFoo;
                .withField(new FieldDescription.Token(methodName, PRIV_VOLATILE, BB_OBJECT));
        }

        @Override
        public abstract AbstractMethod appender(Target implementationTarget);
    }

    // Specialization for "getFoo()", which is using NodeContextSupplier as its context
    private static final class GetMethodImplementation extends AbstractMethodImplementation {
        private static final Generic BB_NCS = TypeDefinition.Sort.describe(NodeContextSupplier.class);
        private static final StackManipulation BRIDGE_RESOLVE = MethodInvocation.invoke(findMethod(
            CodecDataObjectBridge.class, "resolve", String.class));
        private static final StackManipulation CODEC_MEMBER = MethodInvocation.invoke(findMethod(
            CodecDataObject.class, "codecMember", AtomicReferenceFieldUpdater.class, NodeContextSupplier.class));

        GetMethodImplementation(final String methodName) {
            super(methodName);
        }

        @Override
        public InstrumentedType prepare(final InstrumentedType instrumentedType) {
            return super.prepare(instrumentedType
                // private static final NodeContextSupplier getFoo$$$C = ...
                .withField(new FieldDescription.Token(contextName(), PRIV_CONST, BB_NCS))
                .withInitializer(new AbstractContextInitializer() {
                    @Override
                    StackManipulation resolveMethod() {
                        return BRIDGE_RESOLVE;
                    }
                }));
        }

        @Override
        public AbstractMethod appender(final Target implementationTarget) {
            return new AbstractMethod() {
                @Override
                StackManipulation codecMember() {
                    return CODEC_MEMBER;
                }
            };
        }
    }

    // Specialization for "key()", which is using IdentifiableItemCodec as its context
    private static final class KeyMethodImplementation extends AbstractMethodImplementation {
        private static final Generic BB_IIC = TypeDefinition.Sort.describe(IdentifiableItemCodec.class);
        private static final StackManipulation BRIDGE_RESOLVE = MethodInvocation.invoke(findMethod(
            CodecDataObjectBridge.class, "resolveKey", String.class));
        private static final StackManipulation CODEC_MEMBER = MethodInvocation.invoke(findMethod(
            CodecDataObject.class, "codecMember", AtomicReferenceFieldUpdater.class, IdentifiableItemCodec.class));

        KeyMethodImplementation(final String methodName) {
            super(methodName);
        }

        @Override
        public InstrumentedType prepare(final InstrumentedType instrumentedType) {
            return super.prepare(instrumentedType
                    // private static final IdentifiableItemCodec getFoo$C = ...
                    .withField(new FieldDescription.Token(contextName(), PRIV_CONST, BB_IIC))
                    .withInitializer(new AbstractContextInitializer() {
                        @Override
                        StackManipulation resolveMethod() {
                            return BRIDGE_RESOLVE;
                        }
                    }));
        }

        @Override
        public AbstractMethod appender(final Target implementationTarget) {
            return new AbstractMethod() {
                @Override
                StackManipulation codecMember() {
                    return CODEC_MEMBER;
                }
            };
        }
    }

    private abstract static class AbstractAllPropertiesAppender extends AbstractAppender {
        final ImmutableMap<StackManipulation, Method> properties;

        AbstractAllPropertiesAppender(final ImmutableMap<StackManipulation, Method> properties) {
            this.properties = requireNonNull(properties);
        }

        @Override
        public final Size apply(final MethodVisitor methodVisitor, final Context implementationContext,
                final MethodDescription instrumentedMethod) {
            StackManipulation.Size operandStackSize = new StackManipulation.Compound(manipulations())
                    .apply(methodVisitor, implementationContext);
            return new Size(operandStackSize.getMaximalSize(), instrumentedMethod.getStackSize() + localCount());
        }

        abstract List<StackManipulation> manipulations();

        int localCount() {
            return 0;
        }
    }

    private static final class CodecEquals extends AbstractAllPropertiesAppender {
        private static final StackManipulation OTHER = MethodVariableAccess.REFERENCE.loadFrom(1);
        private static final StackManipulation ARRAYS_EQUALS = MethodInvocation.invoke(findMethod(Arrays.class,
            "equals", byte[].class, byte[].class));
        private static final StackManipulation OBJECTS_EQUALS = MethodInvocation.invoke(findMethod(Objects.class,
            "equals", Object.class, Object.class));

        CodecEquals(final ImmutableMap<StackManipulation, Method> properties) {
            super(properties);
        }

        @Override
        List<StackManipulation> manipulations() {
            // Label for 'return false;'
            final Label falseLabel = new Label();
            // Condition for 'if (!...)'
            final StackManipulation ifFalse = ByteBuddyUtils.ifEq(falseLabel);

            final List<StackManipulation> manipulations = new ArrayList<>(properties.size() * 6 + 5);
            for (Entry<StackManipulation, Method> entry : properties.entrySet()) {
                // if (!java.util.(Objects|Arrays).equals(getFoo(), other.getFoo())) {
                //     return false;
                // }
                manipulations.add(THIS);
                manipulations.add(entry.getKey());
                manipulations.add(OTHER);
                manipulations.add(entry.getKey());
                manipulations.add(entry.getValue().getReturnType().isArray() ? ARRAYS_EQUALS : OBJECTS_EQUALS);
                manipulations.add(ifFalse);
            }

            // return true;
            manipulations.add(IntegerConstant.ONE);
            manipulations.add(MethodReturn.INTEGER);
            // L0: return false;
            manipulations.add(ByteBuddyUtils.markLabel(falseLabel));
            manipulations.add(IntegerConstant.ZERO);
            manipulations.add(MethodReturn.INTEGER);

            return manipulations;
        }
    }

    private static final class CodecHashCode extends AbstractAllPropertiesAppender {
        private static final StackManipulation THIRTY_ONE = IntegerConstant.forValue(31);
        private static final StackManipulation LOAD_RESULT = MethodVariableAccess.INTEGER.loadFrom(1);
        private static final StackManipulation STORE_RESULT = MethodVariableAccess.INTEGER.storeAt(1);
        private static final StackManipulation ARRAYS_HASHCODE = MethodInvocation.invoke(findMethod(Arrays.class,
            "hashCode", byte[].class));
        private static final StackManipulation OBJECTS_HASHCODE = MethodInvocation.invoke(findMethod(Objects.class,
            "hashCode", Object.class));

        CodecHashCode(final ImmutableMap<StackManipulation, Method> properties) {
            super(properties);
        }

        @Override
        List<StackManipulation> manipulations() {
            final List<StackManipulation> manipulations = new ArrayList<>(properties.size() * 8 + 4);
            // int result = 1;
            manipulations.add(IntegerConstant.ONE);
            manipulations.add(STORE_RESULT);

            for (Entry<StackManipulation, Method> entry : properties.entrySet()) {
                // result = 31 * result + java.util.(Objects,Arrays).hashCode(getFoo());
                manipulations.add(THIRTY_ONE);
                manipulations.add(LOAD_RESULT);
                manipulations.add(Multiplication.INTEGER);
                manipulations.add(THIS);
                manipulations.add(entry.getKey());
                manipulations.add(entry.getValue().getReturnType().isArray() ? ARRAYS_HASHCODE : OBJECTS_HASHCODE);
                manipulations.add(Addition.INTEGER);
                manipulations.add(STORE_RESULT);
            }
            // return result;
            manipulations.add(LOAD_RESULT);
            manipulations.add(MethodReturn.INTEGER);

            return manipulations;
        }

        @Override
        int localCount() {
            // We are allocating 'int result'
            return 1;
        }
    }

    private static final class CodecFillToString extends AbstractAllPropertiesAppender {
        private static final StackManipulation HELPER = MethodVariableAccess.REFERENCE.loadFrom(1);
        private static final StackManipulation HELPER_ADD = MethodInvocation.invoke(findMethod(ToStringHelper.class,
            "add", String.class, Object.class));

        CodecFillToString(final ImmutableMap<StackManipulation, Method> properties) {
            super(properties);
        }

        @Override
        List<StackManipulation> manipulations() {
            final List<StackManipulation> manipulations = new ArrayList<>(properties.size() * 4 + 2);
            // push 'return helper' to stack...
            manipulations.add(HELPER);
            for (Entry<StackManipulation, Method> entry : properties.entrySet()) {
                // .add("getFoo", getFoo())
                manipulations.add(new TextConstant(entry.getValue().getName()));
                manipulations.add(THIS);
                manipulations.add(entry.getKey());
                manipulations.add(HELPER_ADD);
            }
            // ... execute 'return helper'
            manipulations.add(MethodReturn.REFERENCE);

            return manipulations;
        }
    }

    private abstract static class AbstractImplementation implements Implementation {
        @Override
        public final InstrumentedType prepare(final InstrumentedType instrumentedType) {
            return instrumentedType;
        }
    }
}
