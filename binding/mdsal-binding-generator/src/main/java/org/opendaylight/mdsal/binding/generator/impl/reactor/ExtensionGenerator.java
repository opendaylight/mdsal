/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;

import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.binding.YangExtension;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

final class ExtensionGenerator extends AbstractExplicitGenerator<ExtensionEffectiveStatement, ExtensionRuntimeType> {
    ExtensionGenerator(final ExtensionEffectiveStatement statement, final ModuleGenerator parent) {
        super(statement, parent);
    }

    @Override
    StatementNamespace namespace() {
        return StatementNamespace.EXTENSION;
    }

    @Override
    void pushToInference(final SchemaInferenceStack inferenceStack) {
        throw new UnsupportedOperationException("Cannot push " + statement() + " to data tree");
    }

    @Override
    ExtensionRuntimeType createExternalRuntimeType(final Type type) {
        verify(type instanceof GeneratedTransferObject, "Unexpected type %s", type);
        return new DefaultExtensionRuntimeType((GeneratedTransferObject) type, statement());
    }

    @Override
    ExtensionRuntimeType createInternalRuntimeType(final AugmentResolver resolver,
            final ExtensionEffectiveStatement statement, final Type type) {
        // 'extension' statements are not part of schema tree and hence should never an internal reference
        throw new UnsupportedOperationException("Should never be called");
    }

    @Override
    GeneratedTransferObject createTypeImpl(final TypeBuilderFactory builderFactory) {
        final var builder = builderFactory.newGeneratedTOBuilder(typeName());
        builder.addImplementsType(BindingTypes.yangExtension(builder, Type.of(getParent().typeName())));

        annotateDeprecatedIfNecessary(statement(), builder);

        final var module = currentModule();
        module.addQNameConstant(builder, localName());

        // Constant implementation
        builder.addConstant(Type.of(builder), BindingMapping.VALUE_STATIC_FIELD_NAME, YangExtension.class);

        builderFactory.addCodegenInformation(module, statement(), builder);
        builder.setModuleName(module.statement().argument().getLocalName());

        return builder.build();
    }

    @Override
    void addAsGetterMethod(final GeneratedTypeBuilderBase<?> builder, final TypeBuilderFactory builderFactory) {
        // features are a separate concept
    }
}
