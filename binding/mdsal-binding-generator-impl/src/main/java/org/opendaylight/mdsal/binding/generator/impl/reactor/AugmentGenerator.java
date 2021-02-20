/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;

import java.util.Comparator;
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
 * Generator corresponding to a {@code augment} statement.
 */
final class AugmentGenerator extends AbstractCompositeGenerator<AugmentEffectiveStatement> {
    /**
     * Comparator comparing target path length. This is useful for quickly determining order the order in which two
     * (or more) {@link AugmentGenerator}s need to be evaluated. This is necessary when augments are layered on top of
     * each other:
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
    static final Comparator<AugmentGenerator> COMPARATOR =
        Comparator.comparingInt(augment -> augment.statement().argument().getNodeIdentifiers().size());

    private AbstractCompositeGenerator<?> targetGen;

    AugmentGenerator(final AugmentEffectiveStatement statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterSchemaTree(statement().argument());
    }

    void linkAugmentationTarget(final GeneratorContext context) {
        // FIXME: we need two-step resolution here:

//      if (targetSchemaNode instanceof DataSchemaNode && ((DataSchemaNode) targetSchemaNode).isAddedByUses()) {
//          if (targetSchemaNode instanceof DerivableSchemaNode) {
//              targetSchemaNode = ((DerivableSchemaNode) targetSchemaNode).getOriginal().orElse(null);
//          }
//          if (targetSchemaNode == null) {
//              throw new IllegalStateException("Failed to find target node from grouping in augmentation " + augSchema
//                  + " in module " + context.module().getName());
//          }
//      }

        final AbstractExplicitGenerator<?> target = context.resolveSchemaNode(statement().argument());
        verify(target instanceof AbstractCompositeGenerator, "Unexpected target %s", targetGen);
        targetGen = (AbstractCompositeGenerator<?>) target;
        targetGen.addAugment(this);
    }

    @Override
    AbstractQName localName() {
        throw new UnsupportedOperationException();
    }

    @Override
    Member createMember(final CollisionDomain domain) {
        final AbstractQName explicitIdentifier = statement()
            .findFirstEffectiveSubstatementArgument(AugmentIdentifierEffectiveStatement.class).orElse(null);
        if (explicitIdentifier != null) {
            return domain.addPrimary(new CamelCaseNamingStrategy(StatementNamespace.DEFAULT, explicitIdentifier));
        }

        final AbstractQName ref = targetGen.localName();

        int offset = 1;
        for (Generator gen : getParent()) {
            if (gen == this) {
                break;
            }
            if (gen instanceof AugmentGenerator && ref.equals(((AugmentGenerator) gen).targetGen.localName())) {
                offset++;
            }
        }

        return domain.addSecondary(targetGen.getMember(), String.valueOf(offset), statement().argument());
    }

    @Override
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final GeneratedTypeBuilder builder = builderFactory.newGeneratedTypeBuilder(typeName());

        builder.addImplementsType(BindingTypes.augmentation(targetGen.getGeneratedType(builderFactory)));
        addUsesInterfaces(builder, builderFactory);
        addConcreteInterfaceMethods(builder);

        addGetterMethods(builder, builderFactory);
        annotateDeprecatedIfNecessary(builder);

        // FIXME: implement this
        //        augSchemaNodeToMethods(context, builder, augSchema.getChildNodes(), inGrouping);

        return builder.build();
    }

    @Override
    void addAsGetterMethod(final GeneratedTypeBuilderBase<?> builder, final TypeBuilderFactory builderFactory) {
        // Augments are never added as getters, as they are handled via Augmentable mechanics
    }
}
