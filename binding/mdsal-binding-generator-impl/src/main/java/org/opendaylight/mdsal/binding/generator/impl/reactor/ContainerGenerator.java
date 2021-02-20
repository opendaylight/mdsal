/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code container} statement.
 */
public final class ContainerGenerator extends AbstractCompositeGenerator<ContainerEffectiveStatement> {
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
        addUsesInterfaces(builder, builderFactory);
        addConcreteInterfaceMethods(builder);
        annotateDeprecatedIfNecessary(builder);

//      final Module module = context.module();
//      genType.setModuleName(module.getName());
//      addCodegenInformation(genType, module, node);
//      genType.setSchemaPath(node.getPath());
//      if (node instanceof DataNodeContainer) {
//          context.addChildNodeType(node, genType);
//          groupingsToGenTypes(context, ((DataNodeContainer) node).getGroupings());
//          processUsesAugments((DataNodeContainer) node, context, inGrouping);
//      }

        addGetterMethods(builder, builderFactory);

        return builder.build();
    }

//private Type containerToGenType(final ModuleContext context, final GeneratedTypeBuilder parent,
//        final Type baseInterface, final ContainerSchemaNode node, final boolean inGrouping) {
//    final GeneratedTypeBuilder genType = processDataSchemaNode(context, baseInterface, node, inGrouping);
//    if (genType != null) {
//        constructGetter(parent, genType, node);
//        resolveDataSchemaNodes(context, genType, genType, node.getChildNodes(), inGrouping);
//        actionsToGenType(context, genType, node, null, inGrouping);
//        notificationsToGenType(context, genType, node, null, inGrouping);
//    }
//    return genType;
//}
}
