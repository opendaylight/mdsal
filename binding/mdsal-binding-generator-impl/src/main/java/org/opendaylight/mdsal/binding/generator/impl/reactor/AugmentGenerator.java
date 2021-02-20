/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.opendaylight.mdsal.binding.generator.impl.reactor.CollisionDomain.Member;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code augment} statement.
 */
public final class AugmentGenerator extends AbstractCompositeGenerator<AugmentEffectiveStatement> {
    private final int index;

    private AbstractExplicitGenerator<?> target;

    AugmentGenerator(final AugmentEffectiveStatement statement, final AbstractCompositeGenerator<?> parent,
            final int index) {
        super(statement, parent);
        this.index = index;
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        // No-op
    }

    @Override
    void linkCompositeDependencies(final GeneratorContext context) {
        super.linkCompositeDependencies(context);
        target = context.resolveSchemaNode(statement().argument());
    }


    @Override
    Member createMember() {
        return parent().domain().addSecondary(target.getMember(), String.valueOf(index));
    }

    @Override
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final GeneratedTypeBuilder builder = builderFactory.newGeneratedTypeBuilder(typeName());

        // FIXME: implement this

        return builder.build();
    }
}
