/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.runtime.api.FeatureRuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

final class FeatureGenerator extends AbstractExplicitGenerator<FeatureEffectiveStatement, FeatureRuntimeType> {
    FeatureGenerator(final FeatureEffectiveStatement statement, final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    StatementNamespace namespace() {
        return StatementNamespace.FEATURE;
    }

    @Override
    Type runtimeJavaType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    FeatureRuntimeType createExternalRuntimeType(final Type type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    FeatureRuntimeType createInternalRuntimeType(final AugmentResolver resolver,
            final FeatureEffectiveStatement statement, final Type type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    void pushToInference(final SchemaInferenceStack inferenceStack) {
        // TODO Auto-generated method stub
    }

    @Override
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        // TODO Auto-generated method stub
        return null;
    }
}
