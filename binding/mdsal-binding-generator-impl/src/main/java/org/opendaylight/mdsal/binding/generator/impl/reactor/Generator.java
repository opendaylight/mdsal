/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.mdsal.binding.model.util.Types.STRING;
import static org.opendaylight.mdsal.binding.model.util.Types.classType;
import static org.opendaylight.mdsal.binding.model.util.Types.primitiveBooleanType;
import static org.opendaylight.mdsal.binding.model.util.Types.primitiveIntType;
import static org.opendaylight.mdsal.binding.model.util.Types.wildcardTypeFor;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
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
    private static final JavaTypeName OVERRIDE_ANNOTATION = JavaTypeName.create(Override.class);

    private final AbstractCompositeGenerator<?> parent;

    private List<Generator> children = List.of();
    private Optional<GeneratedType> generatedType;
    private JavaTypeName typeName;
    private String assignedName;
    private String javaPackage;


    Generator() {
        this.parent = null;
    }

    Generator(final AbstractCompositeGenerator<?> parent) {
        this.parent = requireNonNull(parent);
    }

    /**
     * Return the {@link AbstractCompositeGenerator} inside which this generator is defined. It is illegal to call this
     * method on a {@link ModuleGenerator}.
     *
     * @return Parent generator
     */
    final @NonNull AbstractCompositeGenerator<?> parent() {
        return verifyNotNull(parent);
    }

    public final @NonNull Optional<GeneratedType> type() {
        return verifyNotNull(generatedType, "No type for %s", this);
    }

    @Override
    public final Iterator<Generator> iterator() {
        return children.iterator();
    }

    final boolean isEmpty() {
        return children.isEmpty();
    }

    final void addChildren(final Collection<Generator> generators) {
        if (children.isEmpty()) {
            children = new ArrayList<>(generators.size());
        }
        children.addAll(generators);
    }

    final @Nullable Generator findGenerator(final EffectiveStatement<?, ?> stmt) {
        for (Generator gen : children) {
            if (gen instanceof AbstractExplicitGenerator && ((AbstractExplicitGenerator<?>) gen).statement() == stmt) {
                return gen;
            }
        }
        return null;
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
        return parent().currentModule();
    }

    /**
     * Push this statement into a {@link SchemaInferenceStack} so that the stack contains a resolvable {@code data tree}
     * hierarchy.
     *
     * @param inferenceStack Target inference stack
     */
    abstract void pushToInference(@NonNull SchemaInferenceStack inferenceStack);

    /**
     * Return {@code true} if this generator actually produces a type.
     *
     * @return {@code true} if this generator is producing a type, {@code false} otherwise.
     */
    abstract boolean producesType();

    /**
     * Create the type associated with this builder. This method idempotent.
     *
     * @param builderFactory Factory for {@link TypeBuilder}s
     * @throws NullPointerException if {@code builderFactory} is {@code null}
     */
    final @Nullable GeneratedType createType(final TypeBuilderFactory builderFactory) {
        Optional<GeneratedType> local = generatedType;
        if (local != null) {
            return local.orElse(null);
        }

        if (producesType()) {
            local = Optional.of(createTypeImpl(requireNonNull(builderFactory)));
        } else {
            local = Optional.empty();
        }
        generatedType = local;

        for (Generator child : this) {
            child.createType(builderFactory);
        }

        return local.orElse(null);
    }

    /**
     * Create the type associated with this builder, as per {@link #createType(TypeBuilderFactory)} contract. This
     * method is guaranteed to be called at most once.
     *
     * @param builderFactory Factory for {@link TypeBuilder}s
     */
    abstract @NonNull GeneratedType createTypeImpl(@NonNull TypeBuilderFactory builderFactory);

    /**
     * Return the preferred Java class name based on Simple Name Mapping rules. This name is used for class name if it
     * does not incur a conflict among siblings.
     *
     * @return Preferred class name
     */
    abstract @NonNull String preferredName();

    abstract @NonNull String preferredSubpackage();

    final @NonNull String assignedName() {
        final String local = assignedName;
        checkState(local != null, "Attempted to access simple name of %s", this);
        return local;
    }

    final void setAssignedName(final String name) {
        checkState(assignedName == null, "Attempted to simple name %s with %s in %s", assignedName, name, this);
        this.assignedName = requireNonNull(name);
    }

    final @NonNull String javaPackage() {
        final String local = javaPackage;
        checkState(local != null, "Attempted to access Java package of %s", this);
        return local;
    }

    final void setJavaPackage(final String javaPackage) {
        checkState(this.javaPackage == null, "Attempted to re-assign package from %s to %s in %s", this.javaPackage,
            javaPackage, this);
        this.javaPackage = requireNonNull(javaPackage);
    }

    final @NonNull JavaTypeName typeName() {
        JavaTypeName local = typeName;
        if (local == null) {
            typeName = local = JavaTypeName.create(parent == null ? javaPackage() : parent.javaPackage(),
                assignedName());
        }
        return local;
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
    }

    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("simpleName", assignedName);
    }

    final void addImplementsChildOf(final GeneratedTypeBuilder builder) {
        builder.addImplementsType(BindingTypes.childOf(DefaultType.of(parent().typeName())));
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
     * @param gto transfer object which needs to be made serializable
     */
    static final void makeSerializable(final GeneratedTOBuilder gto) {
        gto.addImplementsType(Types.serializableType());
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
