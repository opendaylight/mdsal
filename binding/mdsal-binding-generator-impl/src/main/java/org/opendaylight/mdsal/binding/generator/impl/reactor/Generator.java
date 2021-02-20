/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.mdsal.binding.model.util.Types.STRING;
import static org.opendaylight.mdsal.binding.model.util.Types.classType;
import static org.opendaylight.mdsal.binding.model.util.Types.primitiveBooleanType;
import static org.opendaylight.mdsal.binding.model.util.Types.primitiveIntType;
import static org.opendaylight.mdsal.binding.model.util.Types.wildcardTypeFor;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.generator.impl.reactor.CollisionDomain.Member;
import org.opendaylight.mdsal.binding.model.api.AccessModifier;
import org.opendaylight.mdsal.binding.model.api.DefaultType;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.AnnotableTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.mdsal.binding.model.util.BindingGeneratorUtil;
import org.opendaylight.mdsal.binding.model.util.BindingTypes;
import org.opendaylight.mdsal.binding.model.util.Types;
import org.opendaylight.mdsal.binding.model.util.generated.type.builder.GeneratedPropertyBuilderImpl;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.type.TypeBuilder;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * A single node in generator tree. Each node will eventually resolve to a generated Java class. Each node also can have
 * a number of children, which are generators corresponding to the YANG subtree of this node.
 */
public abstract class Generator implements Iterable<Generator> {
    private static final JavaTypeName DEPRECATED_ANNOTATION = JavaTypeName.create(Deprecated.class);
    static final JavaTypeName OVERRIDE_ANNOTATION = JavaTypeName.create(Override.class);

    private final AbstractCompositeGenerator<?> parent;

    private Optional<Member> member;
    private GeneratorResult result;
    private JavaTypeName typeName;
    private String javaPackage;

    Generator() {
        this.parent = null;
    }

    Generator(final AbstractCompositeGenerator<?> parent) {
        this.parent = requireNonNull(parent);
    }

    public final @NonNull Optional<GeneratedType> generatedType() {
        return result.publicGeneratedType();
    }

    @Override
    public Iterator<Generator> iterator() {
        return Collections.emptyIterator();
    }

    /**
     * Return the {@link AbstractCompositeGenerator} inside which this generator is defined. It is illegal to call this
     * method on a {@link ModuleGenerator}.
     *
     * @return Parent generator
     */
    final @NonNull AbstractCompositeGenerator<?> getParent() {
        return verifyNotNull(parent, "No parent for %s", this);
    }

    boolean isEmpty() {
        return true;
    }

    @Nullable Generator findGenerator(final EffectiveStatement<?, ?> stmt) {
        return null;
    }

    final @NonNull Generator getGenerator(final EffectiveStatement<?, ?> stmt) {
        return verifyNotNull(findGenerator(stmt), "Cannot match statement %s in %s", stmt, this);
    }

    /**
     * Return the namespace of this statement.
     *
     * @return Corresponding namespace
     * @throws UnsupportedOperationException if this node does not have a corresponding namespace
     */
    @NonNull StatementNamespace namespace() {
        return StatementNamespace.DEFAULT;
    }

    @NonNull ModuleGenerator currentModule() {
        return getParent().currentModule();
    }

    /**
     * Push this statement into a {@link SchemaInferenceStack} so that the stack contains a resolvable {@code data tree}
     * hierarchy.
     *
     * @param inferenceStack Target inference stack
     */
    abstract void pushToInference(@NonNull SchemaInferenceStack inferenceStack);

    abstract @NonNull ClassPlacement classPlacement();

    final @NonNull Member getMember() {
        return verifyNotNull(ensureMember(), "No member for %s", this);
    }

    final Member ensureMember() {
        if (member == null) {
            final ClassPlacement placement = classPlacement();
            switch (placement) {
                case NONE:
                    member = Optional.empty();
                    break;
                case MEMBER:
                case PHANTOM:
                case TOP_LEVEL:
                    member = Optional.of(createMember(parentDomain()));
                    break;
                default:
                    throw new IllegalStateException("Unhandled placement " + placement);
            }
        }
        return member.orElse(null);
    }

    @NonNull CollisionDomain parentDomain() {
        return getParent().domain();
    }

    abstract @NonNull Member createMember(@NonNull CollisionDomain domain);

    /**
     * Create the type associated with this builder. This method idempotent.
     *
     * @param builderFactory Factory for {@link TypeBuilder}s
     * @throws NullPointerException if {@code builderFactory} is {@code null}
     */
    final void ensureType(final TypeBuilderFactory builderFactory) {
        if (result != null) {
            return;
        }

        final ClassPlacement placement = classPlacement();
        switch (placement) {
            case NONE:
            case PHANTOM:
                result = GeneratorResult.empty();
                break;
            case MEMBER:
                result = GeneratorResult.member(createTypeImpl(requireNonNull(builderFactory)));
                break;
            case TOP_LEVEL:
                result = GeneratorResult.toplevel(createTypeImpl(requireNonNull(builderFactory)));
                break;
            default:
                throw new IllegalStateException("Unhandled placement " + placement);
        }

        for (Generator child : this) {
            child.ensureType(builderFactory);
        }
    }

    final @NonNull GeneratedType getType(final TypeBuilderFactory builderFactory) {
        ensureType(builderFactory);
        return verifyNotNull(result.generatedType(), "No type generated for %s", this);
    }

    final boolean producesType() {
        final ClassPlacement placement = classPlacement();
        switch (placement) {
            case NONE:
            case PHANTOM:
                return false;
            case MEMBER:
            case TOP_LEVEL:
                return true;
            default:
                throw new IllegalStateException("Unhandled placement " + placement);
        }
    }

    /**
     * Create the type associated with this builder, as per {@link #ensureType(TypeBuilderFactory)} contract. This
     * method is guaranteed to be called at most once.
     *
     * @param builderFactory Factory for {@link TypeBuilder}s
     */
    abstract @NonNull GeneratedType createTypeImpl(@NonNull TypeBuilderFactory builderFactory);

    final @NonNull String assignedName() {
        return getMember().currentClass();
    }

    final @NonNull String javaPackage() {
        String local = javaPackage;
        if (local == null) {
            javaPackage = local = createJavaPackage();
        }
        return local;
    }

    @NonNull String createJavaPackage() {
        final String parentPackage = getPackageParent().javaPackage();
        final String myPackage = getMember().currentPackage();
        return BindingMapping.normalizePackageName(parentPackage + '.' + myPackage);
    }

    final @NonNull JavaTypeName typeName() {
        JavaTypeName local = typeName;
        if (local == null) {
            typeName = local = createTypeName();
        }
        return local;
    }

    @NonNull JavaTypeName createTypeName() {
        return JavaTypeName.create(getPackageParent().javaPackage(), assignedName());
    }

    @NonNull AbstractCompositeGenerator<?> getPackageParent() {
        return getParent();
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
    }

    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper;
    }

    final void addImplementsChildOf(final GeneratedTypeBuilder builder) {
        // choice/case hierarchy does not factor into 'ChildOf' hierarchy, hence we need to skip them
        AbstractCompositeGenerator<?> ancestor = getParent();
        while (ancestor instanceof CaseGenerator || ancestor instanceof ChoiceGenerator) {
            ancestor = ancestor.getParent();
        }
        builder.addImplementsType(BindingTypes.childOf(DefaultType.of(ancestor.typeName())));
    }

    /**
     * Add common methods implemented in a generated type. This includes {@link DataContainer#implementedInterface()} as
     * well has {@code bindingHashCode()}, {@code bindingEquals()} and {@code bindingToString()}.
     *
     * @param builder Target builder
     */
    static final void addConcreteInterfaceMethods(final GeneratedTypeBuilder builder) {
        defaultImplementedInterace(builder);

        builder.addMethod(BindingMapping.BINDING_HASHCODE_NAME)
            .setAccessModifier(AccessModifier.PUBLIC)
            .setStatic(true)
            .setReturnType(primitiveIntType());
        builder.addMethod(BindingMapping.BINDING_EQUALS_NAME)
            .setAccessModifier(AccessModifier.PUBLIC)
            .setStatic(true)
            .setReturnType(primitiveBooleanType());
        builder.addMethod(BindingMapping.BINDING_TO_STRING_NAME)
            .setAccessModifier(AccessModifier.PUBLIC)
            .setStatic(true)
            .setReturnType(STRING);
    }

    static void annotateDeprecatedIfNecessary(final EffectiveStatement<?, ?> stmt, final AnnotableTypeBuilder builder) {
        if (stmt instanceof WithStatus) {
            annotateDeprecatedIfNecessary((WithStatus) stmt, builder);
        }
    }

    static void annotateDeprecatedIfNecessary(final WithStatus node, final AnnotableTypeBuilder builder) {
        switch (node.getStatus()) {
            case DEPRECATED:
                // FIXME: we really want to use a pre-made annotation
                builder.addAnnotation(DEPRECATED_ANNOTATION);
                break;
            case OBSOLETE:
                builder.addAnnotation(DEPRECATED_ANNOTATION).addParameter("forRemoval", "true");
                break;
            case CURRENT:
                // No-op
                break;
            default:
                throw new IllegalStateException("Unhandled status in " + node);
        }
    }

    /**
     * Add {@link java.io.Serializable} to implemented interfaces of this TO. Also compute and add serialVersionUID
     * property.
     *
     * @param builder transfer object which needs to be made serializable
     */
    static final void makeSerializable(final GeneratedTOBuilder builder) {
        builder.addImplementsType(Types.serializableType());
        addSerialVersionUID(builder);
    }

    static final void addSerialVersionUID(final GeneratedTOBuilder gto) {
        final GeneratedPropertyBuilder prop = new GeneratedPropertyBuilderImpl("serialVersionUID");
        prop.setValue(Long.toString(BindingGeneratorUtil.computeDefaultSUID(gto)));
        gto.setSUID(prop);
    }

    /**
     * Add a {@link DataContainer#implementedInterface()} declaration with a narrower return type to specified builder.
     *
     * @param builder Target builder
     */
    static final void narrowImplementedInterface(final GeneratedTypeBuilder builder) {
        defineImplementedInterfaceMethod(builder, wildcardTypeFor(builder.getIdentifier()));
    }

    /**
     * Add a default implementation of {@link DataContainer#implementedInterface()} to specified builder.
     *
     * @param builder Target builder
     */
    static final void defaultImplementedInterace(final GeneratedTypeBuilder builder) {
        defineImplementedInterfaceMethod(builder, DefaultType.of(builder)).setDefault(true);
    }

    private static MethodSignatureBuilder defineImplementedInterfaceMethod(final GeneratedTypeBuilder typeBuilder,
            final Type classType) {
        final MethodSignatureBuilder ret = typeBuilder
                .addMethod(BindingMapping.DATA_CONTAINER_IMPLEMENTED_INTERFACE_NAME)
                .setAccessModifier(AccessModifier.PUBLIC)
                .setReturnType(classType(classType));
        ret.addAnnotation(OVERRIDE_ANNOTATION);
        return ret;
    }
}
