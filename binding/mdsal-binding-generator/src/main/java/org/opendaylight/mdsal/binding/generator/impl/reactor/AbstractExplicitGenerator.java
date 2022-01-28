/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.generator.impl.reactor.CollisionDomain.Member;
import org.opendaylight.mdsal.binding.model.api.MethodSignature.ValueMechanics;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.TypeMemberComment;
import org.opendaylight.mdsal.binding.model.api.type.builder.AnnotableTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.common.AbstractQName;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AddedByUsesAware;
import org.opendaylight.yangtools.yang.model.api.CopyableNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An explicit {@link Generator}, associated with a particular {@link EffectiveStatement}.
 */
public abstract class AbstractExplicitGenerator<T extends EffectiveStatement<?, ?>> extends Generator
        implements CopyableNode {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractExplicitGenerator.class);
    private static final Object ORIGINAL = new Object();

    private final @NonNull T statement;

    /**
     * Field tracking previous incarnation (along reverse of 'uses' and 'augment' axis) of this statement. This field
     * can either be one of:
     * <ul>
     *   <li>{@code null} when not resolved, i.e. access is not legal, or</li>
     *   <li>the {@link #ORIGINAL} object if this is the original definition, or</li>
     *   <li>an {@link AbstractExplicitGenerator} object pointing to previous incarnation, or</li>
     * </ul>
     */
    private Object prev;

    AbstractExplicitGenerator(final T statement) {
        this.statement = requireNonNull(statement);
    }

    AbstractExplicitGenerator(final T statement, final AbstractCompositeGenerator<?> parent) {
        super(parent);
        this.statement = requireNonNull(statement);
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
    public final boolean isAddedByUses() {
        return statement instanceof AddedByUsesAware && ((AddedByUsesAware) statement).isAddedByUses();
    }

    @Override
    public final boolean isAugmenting() {
        return statement instanceof CopyableNode && ((CopyableNode) statement).isAugmenting();
    }

    final void linkOriginalGenerator() {
        if (isAddedByUses() || isAugmenting()) {
            LOG.trace("Linking {}", this);
            prev = getParent().getOriginalChild(getQName());
            LOG.trace("Linked {} to {}", this, prev);
        } else {
            prev = ORIGINAL;
        }
    }

    final @Nullable AbstractExplicitGenerator<?> previous() {
        final var local = prev();
        return local instanceof AbstractExplicitGenerator ? (AbstractExplicitGenerator<?>) local : null;
    }

    @NonNull AbstractExplicitGenerator<?> getOriginal() {
        final var local = prev();
        if (local == ORIGINAL) {
            return this;
        }
        verify(local instanceof AbstractExplicitGenerator, "Unexpected previous %s", local);
        return ((AbstractExplicitGenerator<?>) local).getOriginal();
    }

    private @NonNull Object prev() {
        return verifyNotNull(prev, "Generator %s does not have linkage to previous instance resolved", this);
    }

    @Nullable AbstractExplicitGenerator<?> findSchemaTreeGenerator(final QName qname) {
        for (Generator child : this) {
            if (child instanceof AbstractExplicitGenerator) {
                final AbstractExplicitGenerator<?> gen = (AbstractExplicitGenerator<?>) child;
                final EffectiveStatement<?, ?> stmt = gen.statement();
                if (stmt instanceof SchemaTreeEffectiveStatement && qname.equals(stmt.argument())) {
                    return gen;
                }
            }
        }
        return null;
    }

    final @NonNull QName getQName() {
        final Object arg = statement.argument();
        verify(arg instanceof QName, "Unexpected argument %s", arg);
        return (QName) arg;
    }

    @NonNull AbstractQName localName() {
        // FIXME: this should be done in a nicer way
        final Object argument = statement.argument();
        verify(argument instanceof AbstractQName, "Illegal argument %s", argument);
        return (AbstractQName) argument;
    }

    @Override
    ClassPlacement classPlacement() {
        // We process nodes introduced through augment or uses separately
        // FIXME: this is not quite right!
        return isAddedByUses() || isAugmenting() ? ClassPlacement.NONE : ClassPlacement.TOP_LEVEL;
    }

    @Override
    Member createMember(final CollisionDomain domain) {
        return domain.addPrimary(this, new CamelCaseNamingStrategy(namespace(), localName()));
    }

    void addAsGetterMethod(final @NonNull GeneratedTypeBuilderBase<?> builder,
            final @NonNull TypeBuilderFactory builderFactory) {
        if (isAugmenting()) {
            // Do not process augmented nodes: they will be taken care of in their home augmentation
            return;
        }
        if (isAddedByUses()) {
            // If this generator has been added by a uses node, it is already taken care of by the corresponding
            // grouping. There is one exception to this rule: 'type leafref' can use a relative path to point
            // outside of its home grouping. In this case we need to examine the instantiation until we succeed in
            // resolving the reference.
            addAsGetterMethodOverride(builder, builderFactory);
            return;
        }

        final Type returnType = methodReturnType(builderFactory);
        constructGetter(builder, returnType);
        constructRequire(builder, returnType);
    }

    MethodSignatureBuilder constructGetter(final GeneratedTypeBuilderBase<?> builder, final Type returnType) {
        return constructGetter(builder, returnType, BindingMapping.getGetterMethodName(localName().getLocalName()));
    }

    final MethodSignatureBuilder constructGetter(final GeneratedTypeBuilderBase<?> builder,
            final Type returnType, final String methodName) {
        final MethodSignatureBuilder getMethod = builder.addMethod(methodName).setReturnType(returnType);

        annotateDeprecatedIfNecessary(getMethod);

        statement.findFirstEffectiveSubstatementArgument(DescriptionEffectiveStatement.class)
            .map(TypeMemberComment::referenceOf).ifPresent(getMethod::setComment);

        return getMethod;
    }

    void constructRequire(final GeneratedTypeBuilderBase<?> builder, final Type returnType) {
        // No-op in most cases
    }

    final void constructRequireImpl(final GeneratedTypeBuilderBase<?> builder, final Type returnType) {
        constructGetter(builder, returnType, BindingMapping.getRequireMethodName(localName().getLocalName()))
            .setDefault(true)
            .setMechanics(ValueMechanics.NONNULL);
    }

    void addAsGetterMethodOverride(final @NonNull GeneratedTypeBuilderBase<?> builder,
            final @NonNull TypeBuilderFactory builderFactory) {
        // No-op for most cases
    }

    @NonNull Type methodReturnType(final @NonNull TypeBuilderFactory builderFactory) {
        return getGeneratedType(builderFactory);
    }

    final void annotateDeprecatedIfNecessary(final AnnotableTypeBuilder builder) {
        annotateDeprecatedIfNecessary(statement, builder);
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        helper.add("argument", statement.argument());

        if (isAddedByUses()) {
            helper.addValue("addedByUses");
        }
        if (isAugmenting()) {
            helper.addValue("augmenting");
        }
        return helper;
    }
}
