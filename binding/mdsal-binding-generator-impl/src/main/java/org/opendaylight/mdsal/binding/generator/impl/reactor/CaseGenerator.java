/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;

import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.util.BindingTypes;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code case} statement.
 */
final class CaseGenerator extends AbstractCompositeGenerator<CaseEffectiveStatement> {
    CaseGenerator(final CaseEffectiveStatement statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        // No-op
    }

    @Override
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final GeneratedTypeBuilder builder = builderFactory.newGeneratedTypeBuilder(typeName());
        builder.addImplementsType(BindingTypes.DATA_OBJECT);

        // We also are implementing target choice's type. This is tricky, as we need to cover two distinct cases:
        // - being a child of a choice (i.e. normal definition)
        // - being a child of an augment (i.e. augmented into a choice)
        final AbstractCompositeGenerator<?> parent = getParent();
        final ChoiceGenerator choice;
        if (parent instanceof AbstractAugmentGenerator) {
            final AbstractCompositeGenerator<?> target = ((AbstractAugmentGenerator) parent).targetGenerator();
            verify(target instanceof ChoiceGenerator, "Unexpected parent augment %s target %s", parent, target);
            choice = (ChoiceGenerator) target;
        } else {
            verify(parent instanceof ChoiceGenerator, "Unexpected parent %s", parent);
            choice = (ChoiceGenerator) parent;
        }

        // Most generators have a parent->child dependency due to parent methods' return types and therefore children
        // must not request parent's type. That is not true for choice->case relationship and hence we do not need to
        // go through DefaultType here.
        builder.addImplementsType(choice.getGeneratedType(builderFactory));
        addAugmentable(builder);
        addUsesInterfaces(builder, builderFactory);
        addConcreteInterfaceMethods(builder);
        annotateDeprecatedIfNecessary(builder);

        addGetterMethods(builder, builderFactory);

        // FIXME: implement this
        //        context.addCaseType(caseNode.getPath(), caseTypeBuilder);
        //        context.addChoiceToCaseMapping(refChoiceType, caseTypeBuilder, caseNode);
        //        final SchemaPath choiceNodeParentPath = choiceNode.getPath().getParent();
        //
        //        if (!Iterables.isEmpty(choiceNodeParentPath.getPathFromRoot())) {
        //            SchemaNode parent = findDataSchemaNode(schemaContext, choiceNodeParentPath);
        //
        //            if (parent instanceof AugmentationSchemaNode) {
        //                final AugmentationSchemaNode augSchema = (AugmentationSchemaNode) parent;
        //                final SchemaNodeIdentifier targetPath = augSchema.getTargetPath();
        //                // FIXME: can we use findDataSchemaNode?
        //                SchemaNode targetSchemaNode = findNodeInSchemaContext(schemaContext,
        //                    targetPath.getNodeIdentifiers());
        //                if (targetSchemaNode instanceof DataSchemaNode
        //                       && ((DataSchemaNode) targetSchemaNode).isAddedByUses()) {
        //                    if (targetSchemaNode instanceof DerivableSchemaNode) {
        //                        targetSchemaNode = ((DerivableSchemaNode) targetSchemaNode).getOriginal()
        //                                .orElse(null);
        //                    }
        //                    if (targetSchemaNode == null) {
        //                        throw new IllegalStateException(
        //                                "Failed to find target node from grouping for augmentation " + augSchema
        //                                        + " in module " + context.module().getName());
        //                    }
        //                }
        //                parent = targetSchemaNode;
        //            }
        //
        //            checkState(parent != null, "Could not find Choice node parent %s", choiceNodeParentPath);
        //            Type childOfType = findChildNodeByPath(parent.getPath());
        //            if (childOfType == null) {
        //                childOfType = findGroupingByPath(parent.getPath());
        //            }
        //            resolveDataSchemaNodes(context, caseTypeBuilder, childOfType, caseChildNodes, inGrouping);
        //        } else {
        //            resolveDataSchemaNodes(context, caseTypeBuilder, moduleToDataType(context), caseChildNodes,
        //                inGrouping);
        //        }

        return builder.build();
    }
}
