/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.MethodSignature.ValueMechanics;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code container} statement.
 */
final class ContainerGenerator extends AbstractCompositeGenerator<ContainerEffectiveStatement> {
    ContainerGenerator(final ContainerEffectiveStatement statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterDataTree(statement().getIdentifier());
    }

    @Override
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final GeneratedTypeBuilder builder = builderFactory.newGeneratedTypeBuilder(typeName());
        addImplementsChildOf(builder);
        addAugmentable(builder);
        addUsesInterfaces(builder, builderFactory);
        addConcreteInterfaceMethods(builder);

        final ModuleGenerator module = currentModule();
        module.addQNameConstant(builder, localName());

        addGetterMethods(builder, builderFactory);

        annotateDeprecatedIfNecessary(builder);
        if (builderFactory instanceof TypeBuilderFactory.Codegen) {
            addCodegenInformation(module, statement(), builder);
        }
        builder.setModuleName(module.statement().argument().getLocalName());
//      builder.setSchemaPath(node.getPath());

        return builder.build();
    }

    @Override
    MethodSignatureBuilder constructGetter(final GeneratedTypeBuilderBase<?> builder, final Type returnType) {
        final MethodSignatureBuilder ret = super.constructGetter(builder, returnType)
                .setMechanics(ValueMechanics.NORMAL);

        if (statement().findFirstEffectiveSubstatementArgument(PresenceEffectiveStatement.class).isEmpty()) {
            final MethodSignatureBuilder nonnull = builder
                    .addMethod(BindingMapping.getNonnullMethodName(localName().getLocalName()))
                    .setReturnType(returnType)
                    .setDefault(false);
            annotateDeprecatedIfNecessary(nonnull);
        }

        return ret;
    }
}
