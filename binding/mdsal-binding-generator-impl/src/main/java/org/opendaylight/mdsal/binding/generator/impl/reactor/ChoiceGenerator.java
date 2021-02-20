/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code choice} statement.
 */
public final class ChoiceGenerator extends AbstractCompositeGenerator<ChoiceEffectiveStatement> {
    ChoiceGenerator(final ChoiceEffectiveStatement statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        // No-op
    }

    @Override
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        return null;

//        final GeneratedTypeBuilder newType = builderFactory.newGeneratedTypeBuilder(typeName());
//        qnameConstant(newType, context.moduleInfoType(), schemaNode.getQName().getLocalName());

//        final Module module = context.module();
//        addCodegenInformation(newType, module, schemaNode);
//        newType.setSchemaPath(schemaNode.getPath());
//        newType.setModuleName(module.getName());

//        final GeneratedTypeBuilder choiceTypeBuilder = addRawInterfaceDefinition(context,
//            JavaTypeName.create(packageNameForGeneratedType(context.modulePackageName(), choiceNode.getPath()),
//            BindingMapping.getClassName(choiceNode.getQName())), choiceNode);
//        choiceTypeBuilder.addImplementsType(choiceIn(parent));
//        annotateDeprecatedIfNecessary(choiceNode, choiceTypeBuilder);
//        context.addChildNodeType(choiceNode, choiceTypeBuilder);
//
//        final GeneratedType choiceType = choiceTypeBuilder.build();
//        generateTypesFromChoiceCases(context, choiceType, choiceNode, inGrouping);
//
//        constructGetter(parent, choiceType, choiceNode);
    }
}
