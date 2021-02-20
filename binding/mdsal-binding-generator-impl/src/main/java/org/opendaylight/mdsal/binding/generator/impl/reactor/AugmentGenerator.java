/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.odlext.model.api.AugmentIdentifierEffectiveStatement;
import org.opendaylight.yangtools.yang.common.AbstractQName;
import org.opendaylight.yangtools.yang.common.UnqualifiedQName;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code augment} statement.
 */
public final class AugmentGenerator extends AbstractCompositeGenerator<AugmentEffectiveStatement> {
    private AbstractExplicitGenerator<?> target;

    AugmentGenerator(final AugmentEffectiveStatement statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterSchemaTree(statement().argument());
    }

    @Override
    void linkCompositeDependencies(final GeneratorContext context) {
        super.linkCompositeDependencies(context);

        // FIXME: we need two-step resolution here:

//        if (targetSchemaNode instanceof DataSchemaNode && ((DataSchemaNode) targetSchemaNode).isAddedByUses()) {
//            if (targetSchemaNode instanceof DerivableSchemaNode) {
//                targetSchemaNode = ((DerivableSchemaNode) targetSchemaNode).getOriginal().orElse(null);
//            }
//            if (targetSchemaNode == null) {
//                throw new IllegalStateException("Failed to find target node from grouping in augmentation " + augSchema
//                        + " in module " + context.module().getName());
//            }
//        }

        target = context.resolveSchemaNode(statement().argument());
    }

    @Override
    AbstractQName localName() {
        // Look for explicit name
        final UnqualifiedQName explicit = statement()
            .findFirstEffectiveSubstatementArgument(AugmentIdentifierEffectiveStatement.class).orElse(null);
        return explicit != null ? explicit : deriveLocalName();

    }

    private @NonNull AbstractQName deriveLocalName() {
        final AbstractQName ref = target.localName();

        int offset = 0;
        for (Generator gen : parent()) {
            if (gen == this) {
                break;
            }
            if (gen instanceof AugmentGenerator && ref.equals(((AugmentGenerator) gen).target.localName())) {
                offset++;
            }
        }

        return UnqualifiedQName.of(ref.getLocalName() + offset);
    }

    @Override
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final GeneratedTypeBuilder builder = builderFactory.newGeneratedTypeBuilder(typeName());

        // FIXME: implement this

        return builder.build();
    }
}
