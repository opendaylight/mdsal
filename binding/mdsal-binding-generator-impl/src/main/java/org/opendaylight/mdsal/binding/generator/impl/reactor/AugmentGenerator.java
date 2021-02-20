/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code augment} statement.
 */
public final class AugmentGenerator extends AbstractCompositeGenerator<AugmentEffectiveStatement> {
    private final int index;

    private AbstractExplicitGenerator<SchemaTreeEffectiveStatement<?>> target;

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
    String preferredName() {
        return target.assignedName() + index;
    }

    @Override
    String preferredSubpackage() {
        return target.preferredSubpackage() + index;
    }

    @Override
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        // FIXME: implement this
        throw new UnsupportedOperationException();
    }
}
