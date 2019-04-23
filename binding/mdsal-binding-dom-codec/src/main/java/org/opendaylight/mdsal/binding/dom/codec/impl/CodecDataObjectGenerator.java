/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.mdsal.binding.dom.codec.impl.ByteBuddyUtils.THIS;
import static org.opendaylight.mdsal.binding.dom.codec.impl.ByteBuddyUtils.getField;
import static org.opendaylight.mdsal.binding.dom.codec.impl.ByteBuddyUtils.invokeMethod;
import static org.opendaylight.mdsal.binding.dom.codec.impl.ByteBuddyUtils.putField;

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
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
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
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.Label;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.dom.codec.loader.CodecClassLoader;
import org.opendaylight.mdsal.binding.dom.codec.loader.CodecClassLoader.ClassGenerator;
import org.opendaylight.mdsal.binding.dom.codec.loader.CodecClassLoader.GeneratorResult;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Private support for generating {@link CodecDataObject} and {@link AugmentableCodecDataObject} specializations.
 *
 * <p>
 * Code generation here is probably more involved than usual mainly due to the fact we *really* want to express the
 * strong connection between a generated class and BindingCodecContext in terms of a true constant, which boils down to
 * {@code private static final NodeContextSupplier NCS}. Having such constants provides significant boost to JITs
 * ability to optimize code -- especially with inlining and constant propagation.
 *
 * <p>
 * The accessor mapping performance is critical due to users typically not taking care of storing the results acquired
 * by an invocation, assuming the accessors are backed by a normal field -- which of course is not true, as the results
 * are lazily computed.
 *
 * <p>
 * The design is such that for a particular structure like:
 * <pre>
 *     container foo {
 *         leaf bar {
 *             type string;
 *         }
 *     }
 * </pre>
 * we end up generating a class with the following layout:
 * <pre>
 *     public final class Foo$$$codecImpl extends CodecDataObject implements Foo {
 *         private static final AtomicRefereceFieldUpdater<Foo$$$codecImpl, Object> getBar$$$A;
 *         private static final NodeContextSupplier getBar$$$C;
 *         private volatile Object getBar;
 *
 *         public Foo$$$codecImpl(NormalizedNodeContainer data) {
 *             super(data);
 *         }
 *
 *         public Bar getBar() {
 *             return (Bar) codecMember(getBar$$$A, getBar$$$C);
 *         }
 *     }
 * </pre>
 *
 * <p>
 * This strategy minimizes the bytecode footprint and follows the generally good idea of keeping common logic in a
 * single place in a maintainable form. The glue code is extremely light (~6 instructions), which is beneficial on both
 * sides of invocation:
 * - generated method can readily be inlined into the caller
 * - it forms a call site into which codeMember() can be inlined with both AtomicReferenceFieldUpdater and
 *   NodeContextSupplier being constant
 *
 * <p>
 * The second point is important here, as it allows the invocation logic around AtomicRefereceFieldUpdater to completely
 * disappear, becoming synonymous with operations of a volatile field. NodeContextSupplier being constant also means
 * it will resolve to one of its two implementations, allowing NodeContextSupplier.get() to be resolved to a constant
 * (pointing to the supplier itself) or to a simple volatile read (which will be non-null after first access).
 *
 * <p>
 * The sticky point here is the NodeContextSupplier, as it is a heap object which cannot normally be looked up from the
 * static context in which the static class initializer operates -- so we need perform some sort of a trick here.
 *
 * <p>
 * Eventhough ByteBuddy provides facilities for bridging references to type fields, those facilities operate on volatile
 * fields -- hence they do not quite work for us.
 *
 * <p>
 * Another alternative, which we used in Javassist-generated DataObjectSerializers, is to muck with the static field
 * using reflection -- which works, but requires redefinition of Field.modifiers, which is something Java 9 complains
 * about quite noisily.
 *
 * <p>
 * We take a different approach here, which takes advantage of the fact we are in control of both code generation (here)
 * and class loading (in {@link CodecClassLoader}). The process is performed in four steps:
 * <ul>
 * <li>During code generation, the context fields are pointed towards {@link CodecDataObjectBridge#resolve(String)} and
 *     {@link CodecDataObjectBridge#resolveKey(String)} methods, which are public and static, hence perfectly usable
 *     in the context of a class initializer.</li>
 * <li>During class loading of generated byte code, the original instance of the generator is called to wrap the actual
 *     class loading operation. At this point the generator installs itself as the current generator for this thread via
 *     {@link CodecDataObjectBridge#setup(CodecDataObjectGenerator)} and allows the class to be loaded.
 * <li>After the class has been loaded, but before the call returns, we will force the class to initialize, at which
 *     point the static invocations will be redirect to {@link #resolve(String)} and {@link #resolveKey(String)}
 *     methods, thus initializing the fields to the intended constants.</li>
 * <li>Before returning from the class loading call, the generator will detach itself via
 *     {@link CodecDataObjectBridge#tearDown(CodecDataObjectGenerator)}.</li>
 *
 * <p>
 * This strategy works due to close cooperation with the target ClassLoader, as the entire code generation and loading
 * block runs with the class loading lock for this FQCN and the reference is not leaked until the process completes.
 */
final class CodecDataObjectGenerator<T extends CodecDataObject<?>> implements ClassGenerator<T> {
    private static final Logger LOG = LoggerFactory.getLogger(CodecDataObjectGenerator.class);
    private static final Generic BB_BOOLEAN = TypeDefinition.Sort.describe(boolean.class);
    private static final Generic BB_DATAOBJECT = TypeDefinition.Sort.describe(DataObject.class);
    private static final Generic BB_HELPER = TypeDefinition.Sort.describe(ToStringHelper.class);
    private static final Generic BB_INT = TypeDefinition.Sort.describe(int.class);
    private static final Generic BB_IIC = TypeDefinition.Sort.describe(IdentifiableItemCodec.class);
    private static final Generic BB_NCS = TypeDefinition.Sort.describe(NodeContextSupplier.class);

    private static final Builder<?> CDO;
    private static final Builder<?> ACDO;

    static {
        final ByteBuddy bb = new ByteBuddy();
        CDO = bb.subclass(CodecDataObject.class).visit(ByteBuddyUtils.computeFrames());
        ACDO = bb.subclass(AugmentableCodecDataObject.class).visit(ByteBuddyUtils.computeFrames());
    }

    private static final StackManipulation BRIDGE_RESOLVE = invokeMethod(CodecDataObjectBridge.class,
        "resolve", String.class);
    private static final StackManipulation BRIDGE_RESOLVE_KEY = invokeMethod(CodecDataObjectBridge.class,
        "resolveKey", String.class);
    private static final StackManipulation CODEC_MEMBER = invokeMethod(CodecDataObject.class,
        "codecMember", AtomicReferenceFieldUpdater.class, NodeContextSupplier.class);
    private static final StackManipulation CODEC_MEMBER_KEY = invokeMethod(CodecDataObject.class,
        "codecMember",  AtomicReferenceFieldUpdater.class, IdentifiableItemCodec.class);

    private static final StackManipulation ARRAYS_EQUALS = invokeMethod(Arrays.class, "equals",
        byte[].class, byte[].class);
    private static final StackManipulation OBJECTS_EQUALS = invokeMethod(Objects.class, "equals",
        Object.class, Object.class);
    private static final StackManipulation HELPER_ADD = invokeMethod(ToStringHelper.class, "add",
        String.class, Object.class);

    private static final StackManipulation FIRST_ARG_REF = MethodVariableAccess.REFERENCE.loadFrom(1);

    private static final StackManipulation[] EMPTY_MANIPULATIONS = new StackManipulation[0];
    private static final int PROT_FINAL = Opcodes.ACC_PROTECTED | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC;
    private static final int PUB_FINAL = Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC;

    private final ImmutableMap<Method, NodeContextSupplier> properties;
    private final Entry<Method, IdentifiableItemCodec> keyMethod;
    private final Builder<?> template;

    private CodecDataObjectGenerator(final Builder<?> template,
            final ImmutableMap<Method, NodeContextSupplier> properties,
            final @Nullable Entry<Method, IdentifiableItemCodec> keyMethod) {
        this.template = requireNonNull(template);
        this.properties = requireNonNull(properties);
        this.keyMethod = keyMethod;
    }

    static <D extends DataObject, T extends CodecDataObject<T>> Class<T> generate(final CodecClassLoader loader,
            final Class<D> bindingInterface, final ImmutableMap<Method, NodeContextSupplier> properties,
            final Entry<Method, IdentifiableItemCodec> keyMethod) {
        return doGenerate(loader, bindingInterface, new CodecDataObjectGenerator<>(CDO, properties, keyMethod));
    }

    static <D extends DataObject, T extends CodecDataObject<T>> Class<T> generateAugmentable(
            final CodecClassLoader loader, final Class<D> bindingInterface,
            final ImmutableMap<Method, NodeContextSupplier> properties,
            final Entry<Method, IdentifiableItemCodec> keyMethod) {
        return doGenerate(loader, bindingInterface, new CodecDataObjectGenerator<>(ACDO, properties, keyMethod));
    }

    private static <D extends DataObject, T extends CodecDataObject<T>> Class<T> doGenerate(
            final CodecClassLoader loader, final Class<D> bindingInterface,
            final CodecDataObjectGenerator<T> generator) {
        return loader.generateClass(bindingInterface, "codecImpl", generator);
    }

    @Override
    public GeneratorResult<T> generateClass(final CodecClassLoader loeader, final String fqcn,
            final Class<?> bindingInterface) {
        LOG.trace("Generating class {}", fqcn);

        @SuppressWarnings("unchecked")
        Builder<T> builder = (Builder<T>) template.name(fqcn).implement(bindingInterface);

        for (Method method : properties.keySet()) {
            LOG.trace("Generating for method {}", method);
            final String methodName = method.getName();
            final TypeDescription retType = TypeDescription.ForLoadedType.of(method.getReturnType());
            builder = builder.defineMethod(methodName, retType, PUB_FINAL)
                    .intercept(new MethodImplementation(BB_NCS, BRIDGE_RESOLVE, CODEC_MEMBER, methodName, retType));
        }

        if (keyMethod != null) {
            LOG.trace("Generating for key {}", keyMethod);
            final Method method = keyMethod.getKey();
            final String methodName = method.getName();
            final TypeDescription retType = TypeDescription.ForLoadedType.of(method.getReturnType());
            builder = builder.defineMethod(methodName, retType, PUB_FINAL)
                    .intercept(new MethodImplementation(BB_IIC, BRIDGE_RESOLVE_KEY, CODEC_MEMBER_KEY, methodName,
                        retType));
        }

        // Index all property methods, turning them into "getFoo()" invocations, retaining order. We will be using
        // those invocations in each of the three methods. Note that we do not glue the invocations to 'this', as we
        // will be invoking them on 'other' in codecEquals()
        final ImmutableMap<StackManipulation, Method> methods = Maps.uniqueIndex(properties.keySet(),
            ByteBuddyUtils::invokeMethod);

        // Final bits:
        return GeneratorResult.of(builder
                // codecHashCode() ...
                .defineMethod("codecHashCode", BB_INT, PROT_FINAL)
                .intercept(new Implementation.Simple(new CodecHashCode(methods)))
                // ... codecEquals() ...
                .defineMethod("codecEquals", BB_BOOLEAN, PROT_FINAL).withParameter(BB_DATAOBJECT)
                .intercept(codecEquals(methods))
                // ... and codecFillToString() ...
                .defineMethod("codecFillToString", BB_HELPER, PROT_FINAL).withParameter(BB_HELPER)
                .intercept(codecFillToString(methods))
                // ... set class as final ...
                .modifiers(PUB_FINAL)
                // ... and build it
                .make());
    }

    @Override
    public Class<T> customizeLoading(final @NonNull Supplier<Class<T>> loader) {
        final CodecDataObjectGenerator<?> prev = CodecDataObjectBridge.setup(this);
        try {
            final Class<T> result = loader.get();

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

    private static Implementation codecEquals(final ImmutableMap<StackManipulation, Method> properties) {
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
            manipulations.add(FIRST_ARG_REF);
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

        return new Implementation.Simple(manipulations.toArray(EMPTY_MANIPULATIONS));
    }

    private static Implementation codecFillToString(final ImmutableMap<StackManipulation, Method> properties) {
        final List<StackManipulation> manipulations = new ArrayList<>(properties.size() * 4 + 2);
        // push 'return helper' to stack...
        manipulations.add(FIRST_ARG_REF);
        for (Entry<StackManipulation, Method> entry : properties.entrySet()) {
            // .add("getFoo", getFoo())
            manipulations.add(new TextConstant(entry.getValue().getName()));
            manipulations.add(THIS);
            manipulations.add(entry.getKey());
            manipulations.add(HELPER_ADD);
        }
        // ... execute 'return helper'
        manipulations.add(MethodReturn.REFERENCE);

        return new Implementation.Simple(manipulations.toArray(EMPTY_MANIPULATIONS));
    }

    private static final class MethodImplementation implements Implementation {
        private static final Generic BB_ARFU = TypeDefinition.Sort.describe(AtomicReferenceFieldUpdater.class);
        private static final Generic BB_OBJECT = TypeDefinition.Sort.describe(Object.class);
        private static final StackManipulation OBJECT_CLASS = ClassConstant.of(TypeDescription.OBJECT);
        private static final StackManipulation ARFU_NEWUPDATER = invokeMethod(AtomicReferenceFieldUpdater.class,
            "newUpdater", Class.class, Class.class, String.class);

        private static final int PRIV_CONST = Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL
                | Opcodes.ACC_SYNTHETIC;
        private static final int PRIV_VOLATILE = Opcodes.ACC_PRIVATE | Opcodes.ACC_VOLATILE | Opcodes.ACC_SYNTHETIC;

        private final Generic contextType;
        private final StackManipulation resolveMethod;
        private final StackManipulation codecMember;
        private final TypeDescription retType;

        // getFoo
        private final String methodName;
        // getFoo$$$A
        private final String arfuName;
        // getFoo$$$C
        private final String contextName;

        MethodImplementation(final Generic contextType, final StackManipulation resolveMethod,
            final StackManipulation codecMember, final String methodName, final TypeDescription retType) {
            this.contextType = requireNonNull(contextType);
            this.resolveMethod = requireNonNull(resolveMethod);
            this.codecMember = requireNonNull(codecMember);
            this.methodName = requireNonNull(methodName);
            this.retType = requireNonNull(retType);
            this.arfuName = methodName + "$$$A";
            this.contextName = methodName + "$$$C";
        }

        @Override
        public InstrumentedType prepare(final InstrumentedType instrumentedType) {
            final InstrumentedType tmp = instrumentedType
                    // private static final AtomicReferenceFieldUpdater<This, Object> getFoo$$$A;
                    .withField(new FieldDescription.Token(arfuName, PRIV_CONST, BB_ARFU))
                    // private static final <CONTEXT_TYPE> getFoo$$$C;
                    .withField(new FieldDescription.Token(contextName, PRIV_CONST, contextType))
                    // private volatile Object getFoo;
                    .withField(new FieldDescription.Token(methodName, PRIV_VOLATILE, BB_OBJECT));

            // "getFoo"
            final TextConstant methodNameText = new TextConstant(methodName);

            return tmp
                .withInitializer(new ByteCodeAppender.Simple(
                    // getFoo$$$A = AtomicReferenceFieldUpdater.newUpdater(This.class, Object.class, "getFoo");
                    ClassConstant.of(tmp),
                    OBJECT_CLASS,
                    methodNameText,
                    ARFU_NEWUPDATER,
                    putField(tmp, arfuName),
                    // getFoo$$$C = CodecDataObjectBridge.<RESOLVE_METHOD>("getFoo");
                    methodNameText,
                    resolveMethod,
                    putField(tmp, contextName)));
        }

        @Override
        public ByteCodeAppender appender(final Target implementationTarget) {
            final TypeDescription instrumentedType = implementationTarget.getInstrumentedType();
            return new ByteCodeAppender.Simple(
                // return (FooType) codecMember(getFoo$$$A, getFoo$$$C);
                THIS,
                getField(instrumentedType, arfuName),
                getField(instrumentedType, contextName),
                codecMember,
                TypeCasting.to(retType),
                MethodReturn.REFERENCE);
        }
    }

    private static final class CodecHashCode implements ByteCodeAppender {
        private static final StackManipulation THIRTY_ONE = IntegerConstant.forValue(31);
        private static final StackManipulation LOAD_RESULT = MethodVariableAccess.INTEGER.loadFrom(1);
        private static final StackManipulation STORE_RESULT = MethodVariableAccess.INTEGER.storeAt(1);
        private static final StackManipulation ARRAYS_HASHCODE = invokeMethod(Arrays.class, "hashCode", byte[].class);
        private static final StackManipulation OBJECTS_HASHCODE = invokeMethod(Objects.class, "hashCode", Object.class);

        private final ImmutableMap<StackManipulation, Method> properties;

        CodecHashCode(final ImmutableMap<StackManipulation, Method> properties) {
            this.properties = requireNonNull(properties);
        }

        @Override
        public Size apply(final MethodVisitor methodVisitor, final Context implementationContext,
                final MethodDescription instrumentedMethod) {
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

            StackManipulation.Size operandStackSize = new StackManipulation.Compound(manipulations)
                    .apply(methodVisitor, implementationContext);
            return new Size(operandStackSize.getMaximalSize(), instrumentedMethod.getStackSize() + 1);
        }
    }
}
