/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import com.google.common.base.VerifyException;
import org.opendaylight.mdsal.binding.generator.impl.rt.DefaultFeatureRuntimeType;
import org.opendaylight.mdsal.binding.model.api.DataObjectField;
import org.opendaylight.mdsal.binding.model.api.FeatureArchetype;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.runtime.api.FeatureRuntimeType;
import org.opendaylight.yangtools.yang.binding.contract.StatementNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

final class FeatureGenerator extends AbstractExplicitGenerator<FeatureEffectiveStatement, FeatureRuntimeType> {
    FeatureGenerator(final FeatureEffectiveStatement statement, final ModuleGenerator parent) {
        super(statement, parent);
    }

    @Override
    StatementNamespace namespace() {
        return StatementNamespace.FEATURE;
    }

    @Override
    void pushToInference(final SchemaInferenceStack inferenceStack) {
        throw new UnsupportedOperationException("Cannot push " + statement() + " to data tree");
    }

    @Override
    FeatureRuntimeType createExternalRuntimeType(final Type type) {
        if (type instanceof FeatureArchetype feature) {
            return new DefaultFeatureRuntimeType(feature);
        }
        throw new VerifyException("Unexpected type " + type);
    }

    @Override
    FeatureRuntimeType createInternalRuntimeType(final AugmentResolver resolver,
            final FeatureEffectiveStatement statement, final Type type) {
        // 'feature' statements are not part of schema tree and hence should never an internal reference
        throw new UnsupportedOperationException("Should never be called");
    }

    @Override
    FeatureArchetype createTypeImpl() {
//        final var builder = builderFactory.newGeneratedTOBuilder(typeName());
//        builder.addImplementsType(BindingTypes.yangFeature(builder, Type.of(getParent().typeName())));
//
//        annotateDeprecatedIfNecessary(statement(), builder);
//
//        final var module = currentModule();
//        module.addQNameConstant(builder, localName());
//
//        // Constant implementation
//        builder.addConstant(Type.of(builder), Naming.VALUE_STATIC_FIELD_NAME, YangFeature.class);
//
//        builderFactory.addCodegenInformation(module, statement(), builder);
//        builder.setModuleName(module.statement().argument().getLocalName());

        return new FeatureArchetype(typeName(), statement());
    }

    @Override
    DataObjectField<?> generateDataObjectField() {
        // features are a separate concept
        return null;
    }
}
