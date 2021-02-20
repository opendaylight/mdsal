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

import java.util.Comparator;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.generator.impl.reactor.CollisionDomain.Member;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.util.BindingTypes;
import org.opendaylight.yangtools.odlext.model.api.AugmentIdentifierEffectiveStatement;
import org.opendaylight.yangtools.yang.common.AbstractQName;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * A generator corresponding to a {@code augment} statement. This class is further specialized for the two distinct uses
 * an augment is used.
 */
abstract class AbstractAugmentGenerator extends AbstractCompositeGenerator<AugmentEffectiveStatement> {
    /**
     * Comparator comparing target path length. This is useful for quickly determining order the order in which two
     * (or more) {@link AbstractAugmentGenerator}s need to be evaluated. This is necessary when augments are layered on
     * top of each other:
     *
     * <p>
     * <pre>
     *   <code>
     *     container foo;
     *
     *     augment /foo/bar {
     *       container baz;
     *     }
     *
     *     augment /foo {
     *       container bar;
     *     }
     *   </code>
     * </pre>
     *
     * <p>
     * Evaluating these in the order of increasing argument component count solves this without having to perform a full
     * analysis.
     */
    static final Comparator<? super AbstractAugmentGenerator> COMPARATOR =
        Comparator.comparingInt(augment -> augment.statement().argument().getNodeIdentifiers().size());

    private AbstractCompositeGenerator<?> targetGen;

    AbstractAugmentGenerator(final AugmentEffectiveStatement statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
    }

    @Override
    final void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterSchemaTree(statement().argument());
    }

    @Override
    final AbstractQName localName() {
        throw new UnsupportedOperationException();
    }

    @Override
    final Member createMember(final CollisionDomain domain) {
        final AbstractQName explicitIdentifier = statement()
            .findFirstEffectiveSubstatementArgument(AugmentIdentifierEffectiveStatement.class).orElse(null);
        if (explicitIdentifier != null) {
            return domain.addPrimary(new CamelCaseNamingStrategy(StatementNamespace.DEFAULT, explicitIdentifier));
        }

        final AbstractCompositeGenerator<?> target = targetGenerator();
        final AbstractQName ref = target.localName();

        int offset = 1;
        for (Generator gen : getParent()) {
            if (gen == this) {
                break;
            }
            if (gen instanceof AbstractAugmentGenerator
                && ref.equals(((AbstractAugmentGenerator) gen).targetGenerator().localName())) {
                offset++;
            }
        }

        return domain.addSecondary(target.getMember(), String.valueOf(offset), statement().argument());
    }

    @Override
    final GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final GeneratedTypeBuilder builder = builderFactory.newGeneratedTypeBuilder(typeName());

        builder.addImplementsType(BindingTypes.augmentation(targetGenerator().getGeneratedType(builderFactory)));
        addUsesInterfaces(builder, builderFactory);
        addConcreteInterfaceMethods(builder);

        addGetterMethods(builder, builderFactory);
        annotateDeprecatedIfNecessary(builder);

        // FIXME: implement this
        //        augSchemaNodeToMethods(context, builder, augSchema.getChildNodes(), inGrouping);

        return builder.build();
    }

    @Override
    final void addAsGetterMethod(final GeneratedTypeBuilderBase<?> builder, final TypeBuilderFactory builderFactory) {
        // Augments are never added as getters, as they are handled via Augmentable mechanics
    }

    final void setTargetGenerator(final AbstractExplicitGenerator<?> target) {
        verify(target instanceof AbstractCompositeGenerator, "Unexpected target %s", target);
        targetGen = (AbstractCompositeGenerator<?>) target;
        targetGen.addAugment(this);
    }

    abstract void loadTargetGenerator();

    private @NonNull AbstractCompositeGenerator<?> targetGenerator() {
        final AbstractCompositeGenerator<?> existing = targetGen;
        if (existing != null) {
            return existing;
        }

        loadTargetGenerator();
        return verifyNotNull(targetGen, "No target for %s", this);
    }
}
