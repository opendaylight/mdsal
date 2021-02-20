/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code list} statement.
 */
public final class ListGenerator extends AbstractCompositeGenerator<ListEffectiveStatement> {
    ListGenerator(final ListEffectiveStatement statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterDataTree(statement().getIdentifier());
    }

    @Override
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        // FIXME: implement this
        throw new UnsupportedOperationException();
    }

//    private GeneratedTypeBuilder processDataSchemaNode(final ModuleContext context, final Type baseInterface,
//        final DataSchemaNode node, final boolean inGrouping) {
//    if (node.isAugmenting() || node.isAddedByUses()) {
//        return null;
//    }
//    final GeneratedTypeBuilder genType = addDefaultInterfaceDefinition(context, node, baseInterface);
//    addConcreteInterfaceMethods(genType);
//    annotateDeprecatedIfNecessary(node, genType);
//
//    final Module module = context.module();
//    genType.setModuleName(module.getName());
//    addCodegenInformation(genType, module, node);
//    genType.setSchemaPath(node.getPath());
//    if (node instanceof DataNodeContainer) {
//        context.addChildNodeType(node, genType);
//        groupingsToGenTypes(context, ((DataNodeContainer) node).getGroupings());
//        processUsesAugments((DataNodeContainer) node, context, inGrouping);
//    }
//    return genType;
//}


//    private GeneratedTypeBuilder listToGenType(final ModuleContext context, final GeneratedTypeBuilder parent,
//        final Type baseInterface, final ListSchemaNode node, final boolean inGrouping) {
//    final GeneratedTypeBuilder genType = processDataSchemaNode(context, baseInterface, node, inGrouping);
//    if (genType != null) {
//        final List<String> listKeys = listKeys(node);
//        final GeneratedTOBuilder keyTypeBuilder;
//        if (!listKeys.isEmpty()) {
//            keyTypeBuilder = typeProvider.newGeneratedTOBuilder(JavaTypeName.create(
//                packageNameForGeneratedType(context.modulePackageName(), node.getPath()),
//                BindingMapping.getClassName(node.getQName().getLocalName() + "Key")))
//                    .addImplementsType(identifier(genType));
//            genType.addImplementsType(identifiable(keyTypeBuilder));
//        } else {
//            keyTypeBuilder = null;
//        }
//
//        // Decide whether to generate a List or a Map
//        final ParameterizedType listType;
//        if (keyTypeBuilder != null && !node.isUserOrdered()) {
//            listType = mapTypeFor(keyTypeBuilder, genType);
//        } else {
//            listType = listTypeFor(genType);
//        }
//
//        constructGetter(parent, listType, node).setMechanics(ValueMechanics.NULLIFY_EMPTY);
//        constructNonnull(parent, listType, node);
//
//        actionsToGenType(context, genType, node, keyTypeBuilder, inGrouping);
//        notificationsToGenType(context, genType, node, keyTypeBuilder, inGrouping);
//
//        for (final DataSchemaNode schemaNode : node.getChildNodes()) {
//            if (!schemaNode.isAugmenting()) {
//                addSchemaNodeToListBuilders(context, schemaNode, genType, keyTypeBuilder, listKeys, inGrouping);
//            }
//        }
//
//        // serialVersionUID
//        if (keyTypeBuilder != null) {
//            final GeneratedPropertyBuilder prop = new GeneratedPropertyBuilderImpl("serialVersionUID");
//            prop.setValue(Long.toString(computeDefaultSUID(keyTypeBuilder)));
//            keyTypeBuilder.setSUID(prop);
//        }
//
//        typeBuildersToGenTypes(context, genType, keyTypeBuilder);
//    }
//    return genType;
//}
}
