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
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Common generator for {@code anydata} and {@code anyxml}.
 */
final class OpaqueObjectGenerator<T extends DataTreeEffectiveStatement<?>> extends Generator<T> {
    OpaqueObjectGenerator(final T statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterDataTree(statement().getIdentifier());
    }

    @Override
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final GeneratedTypeBuilder builder = builderFactory.newGeneratedTypeBuilder(typeName());

//        final GeneratedTypeBuilder newType = typeProvider.newGeneratedTypeBuilder(identifier);
//        qnameConstant(newType, context.moduleInfoType(), schemaNode.getQName().getLocalName());
//
//        final Module module = context.module();
//        addCodegenInformation(newType, module, schemaNode);
//        newType.setSchemaPath(schemaNode.getPath());
//        newType.setModuleName(module.getName());
//
//        final String packageName = identifier.packageName();
//        final String simpleName = identifier.simpleName();
//        if (!genTypeBuilders.containsKey(packageName)) {
//            final Map<String, GeneratedTypeBuilder> builders = new HashMap<>();
//            builders.put(simpleName, newType);
//            genTypeBuilders.put(packageName, builders);
//        } else {
//            final Map<String, GeneratedTypeBuilder> builders = genTypeBuilders.get(packageName);
//            if (!builders.containsKey(simpleName)) {
//                builders.put(simpleName, newType);
//            }
//        }
//        return newType;
//
//        final GeneratedTypeBuilder anyxmlTypeBuilder = addRawInterfaceDefinition(context,
//            JavaTypeName.create(packageNameForGeneratedType(context.modulePackageName(), anyNode.getPath()),
//            BindingMapping.getClassName(anyNode.getQName())), anyNode);
//        anyxmlTypeBuilder.addImplementsType(opaqueObject(anyxmlTypeBuilder)).addImplementsType(childOf(parent));
//        defaultImplementedInterace(anyxmlTypeBuilder);
//        annotateDeprecatedIfNecessary(anyNode, anyxmlTypeBuilder);
//        context.addChildNodeType(anyNode, anyxmlTypeBuilder);
//
//        constructGetter(parent, anyxmlTypeBuilder.build(), anyNode);

        // FIXME: implement this
        return builder.build();
    }
}
