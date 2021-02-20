/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.mdsal.binding.model.util.Types.classType;
import static org.opendaylight.mdsal.binding.model.util.Types.wildcardTypeFor;

import com.google.common.base.CharMatcher;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Collections;
import java.util.Iterator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.AccessModifier;
import org.opendaylight.mdsal.binding.model.api.DefaultType;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.common.AbstractQName;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.type.TypeBuilder;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * A single node in generator tree. Each node will eventually resolve to a generated Java class. Each node also can have
 * a number of children, which are generators corresponding to the YANG subtree of this node.
 */
public abstract class Generator<T extends EffectiveStatement<?, ?>> implements Iterable<Generator<?>> {
    private static final JavaTypeName OVERRIDE_ANNOTATION = JavaTypeName.create(Override.class);
    private static final CharMatcher DASH_MATCHER = CharMatcher.is('-');

    private final AbstractCompositeGenerator<?> parent;
    private final @NonNull T statement;

    private JavaTypeName typeName;
    private String assignedName;
    private String javaPackage;
    private GeneratedType type;

    Generator(final T statement) {
        this.statement = requireNonNull(statement);
        this.parent = null;
    }

    Generator(final T statement, final AbstractCompositeGenerator<?> parent) {
        this.statement = requireNonNull(statement);
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

    /**
     * Return the {@link EffectiveStatement} associated with this generator.
     *
     * @return An EffectiveStatement
     */
    public final @NonNull T statement() {
        return statement;
    }

    @Override
    public Iterator<Generator<?>> iterator() {
        return Collections.emptyIterator();
    }

    @Nullable Generator<?> findGenerator(final EffectiveStatement<?, ?> stmt) {
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
     * Create the type associated with this builder. This method idempotent.
     *
     * @param builderFactory Factory for {@link TypeBuilder}s
     * @throws NullPointerException if {@code builderFactory} is {@code null}
     */
    final @NonNull GeneratedType createType(final TypeBuilderFactory builderFactory) {
        GeneratedType local = type;
        if (local == null) {
            type = local = createTypeImpl(requireNonNull(builderFactory));
        }
        return local;
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
    @NonNull String preferredName() {
        // FIXME: this should be done in a nicer way
        final Object argument = statement.argument();
        verify(argument instanceof QName, "Illegal argument %s", argument);
        return BindingMapping.getClassName((QName) argument);
    }

    final @NonNull String preferredSubpackage() {
        // FIXME: this should be done in a nicer way
        final Object argument = statement.argument();
        verify(argument instanceof QName, "Illegal argument %s", argument);
        final String localName = ((AbstractQName) argument).getLocalName();

        // Strip dashes, as they are not legal in package names.
        return DASH_MATCHER.removeFrom(localName);
    }

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
