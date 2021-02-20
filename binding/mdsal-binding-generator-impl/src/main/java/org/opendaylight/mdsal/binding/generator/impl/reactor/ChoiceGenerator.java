/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.opendaylight.mdsal.binding.model.api.DefaultType;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.util.BindingTypes;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code choice} statement.
 */
final class ChoiceGenerator extends AbstractCompositeGenerator<ChoiceEffectiveStatement> {
    ChoiceGenerator(final ChoiceEffectiveStatement statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        // No-op
    }

    @Override
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final GeneratedTypeBuilder builder = builderFactory.newGeneratedTypeBuilder(typeName());

        final ModuleGenerator module = currentModule();
        module.addQNameConstant(builder, localName().getLocalName());

//      qnameConstant(builder, context.moduleInfoType(), schemaNode.getQName().getLocalName());

//      final Module module = context.module();
//      addCodegenInformation(newType, module, schemaNode);
//      newType.setSchemaPath(schemaNode.getPath());
//      newType.setModuleName(module.getName());

        builder.addImplementsType(BindingTypes.choiceIn(DefaultType.of(getParent().typeName())));
        annotateDeprecatedIfNecessary(builder);

        final GeneratedType choiceType = builder.build();
//      generateTypesFromChoiceCases(context, choiceType, choiceNode, inGrouping);

        return choiceType;
    }
}
