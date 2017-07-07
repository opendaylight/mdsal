/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.impl;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.opendaylight.mdsal.binding.javav2.generator.spi.TypeProvider;
import org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingNamespaceType;
import org.opendaylight.mdsal.binding.javav2.util.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;

@Beta
final class AugmentToGenType {

    /**
     * Comparator based on augment target path.
     */
    private static final Comparator<AugmentationSchema> AUGMENT_COMP = (o1, o2) -> {
        final Iterator<QName> thisIt = o1.getTargetPath().getPathFromRoot().iterator();
        final Iterator<QName> otherIt = o2.getTargetPath().getPathFromRoot().iterator();

        while (thisIt.hasNext()) {
            if (!otherIt.hasNext()) {
                return 1;
            }

            final int comp = thisIt.next().compareTo(otherIt.next());
            if (comp != 0) {
                return comp;
            }
        }

        return otherIt.hasNext() ? -1 : 0;
    };

    /**
     * Comparator based on augment target path.
     */
    private static final Comparator<Map.Entry<SchemaPath, List<AugmentationSchema>>> AUGMENTS_COMP = (o1, o2) -> {
        final Iterator<QName> thisIt = o1.getKey().getPathFromRoot().iterator();
        final Iterator<QName> otherIt = o2.getKey().getPathFromRoot().iterator();

        while (thisIt.hasNext()) {
            if (!otherIt.hasNext()) {
                return 1;
            }

            final int comp = thisIt.next().compareTo(otherIt.next());
            if (comp != 0) {
                return comp;
            }
        }

        return otherIt.hasNext() ? -1 : 0;
    };

    private AugmentToGenType() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Converts all <b>augmentation</b> of the module to the list
     * <code>Type</code> objects.
     *
     * @param module
     *            module from which is obtained list of all augmentation objects
     *            to iterate over them
     * @param schemaContext actual schema context
     * @param typeProvider actual type provider instance
     * @param genCtx generated input context
     * @param genTypeBuilders auxiliary type builders map
     * @param verboseClassComments verbosity switch
     *
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if the module is null</li>
     *             <li>if the name of module is null</li>
     *             </ul>
     * @throws IllegalStateException
     *             if set of augmentations from module is null
     */
    static Map<Module, ModuleContext> generate(final Module module, final SchemaContext schemaContext,
            final TypeProvider typeProvider, final Map<Module, ModuleContext> genCtx,
            Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders, final boolean verboseClassComments) {

        Preconditions.checkArgument(module != null, "Module reference cannot be NULL.");
        Preconditions.checkArgument(module.getName() != null, "Module name cannot be NULL.");
        Preconditions.checkState(module.getAugmentations() != null, "Augmentations Set cannot be NULL.");

        final String basePackageName = BindingMapping.getRootPackageName(module);
        final List<AugmentationSchema> augmentations = resolveAugmentations(module);
        Map<Module, ModuleContext> resultCtx = genCtx;

        //let's group augments by target path
        Map<SchemaPath, List<AugmentationSchema>> augmentationsGrouped =
                augmentations.stream().collect(Collectors.groupingBy(AugmentationSchema::getTargetPath));

        List<Map.Entry<SchemaPath, List<AugmentationSchema>>> sortedAugmentationsGrouped =
                new ArrayList<>(augmentationsGrouped.entrySet());
        Collections.sort(sortedAugmentationsGrouped, AUGMENTS_COMP);

        //process child nodes of grouped augment entries
        for (Map.Entry<SchemaPath, List<AugmentationSchema>> schemaPathAugmentListEntry : sortedAugmentationsGrouped) {
            resultCtx = augmentationToGenTypes(basePackageName, schemaPathAugmentListEntry, module, schemaContext,
                    verboseClassComments, resultCtx, genTypeBuilders, typeProvider);

            for (AugmentationSchema augSchema : schemaPathAugmentListEntry.getValue()) {
                GenHelperUtil.processUsesImplements(augSchema, module, schemaContext, genCtx, BindingNamespaceType.Data);
            }

        }

        return resultCtx;
    }

    /**
     * Returns list of <code>AugmentationSchema</code> objects. The objects are
     * sorted according to the length of their target path from the shortest to
     * the longest.
     *
     * @param module
     *            module from which is obtained list of all augmentation objects
     * @return list of sorted <code>AugmentationSchema</code> objects obtained
     *         from <code>module</code>
     * @throws IllegalArgumentException
     *             if module is null
     * @throws IllegalStateException
     *             if set of module augmentations is null
     */
    private static List<AugmentationSchema> resolveAugmentations(final Module module) {
        Preconditions.checkArgument(module != null, "Module reference cannot be NULL.");
        Preconditions.checkState(module.getAugmentations() != null, "Augmentations Set cannot be NULL.");

        final Set<AugmentationSchema> augmentations = module.getAugmentations();
        final List<AugmentationSchema> sortedAugmentations = new ArrayList<>(augmentations);
        Collections.sort(sortedAugmentations, AUGMENT_COMP);

        return sortedAugmentations;
    }

    /**
     * Converts <code>augSchema</code> to list of <code>Type</code> which
     * contains generated type for augmentation. In addition there are also
     * generated types for all containers, list and choices which are child of
     * <code>augSchema</code> node or a generated types for cases are added if
     * augmented node is choice.
     *
     * @param basePackageName
     *            string with the name of the package to which the augmentation
     *            belongs
     * @param schemaPathAugmentListEntry
     *            list of AugmentationSchema nodes grouped by target path
     * @param module current module
     * @param schemaContext actual schema context
     * @param verboseClassComments verbosity switch
     * @param genCtx generated input context
     * @param genTypeBuilders auxiliary type builders map
     * @param typeProvider actual type provider instance
     * @throws IllegalArgumentException
     *             if <code>augmentPackageName</code> equals null
     * @throws IllegalStateException
     *             if augment target path is null
     * @return generated context
     */
    private static Map<Module, ModuleContext> augmentationToGenTypes(final String basePackageName,
            final Entry<SchemaPath, List<AugmentationSchema>> schemaPathAugmentListEntry, final Module module,
            final SchemaContext schemaContext, final boolean verboseClassComments,
            Map<Module, ModuleContext> genCtx, Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders,
            final TypeProvider typeProvider) {
        Preconditions.checkArgument(basePackageName != null, "Package Name cannot be NULL.");
        Preconditions.checkArgument(schemaPathAugmentListEntry != null, "Augmentation List Entry cannot be NULL.");
        final SchemaPath targetPath = schemaPathAugmentListEntry.getKey();
        Preconditions.checkState(targetPath != null,
                "Augmentation List Entry does not contain Target Path (Target Path is NULL).");

        final List<AugmentationSchema> augmentationSchemaList = schemaPathAugmentListEntry.getValue();
        Preconditions.checkState(augmentationSchemaList.size() > 0,
                "Augmentation List cannot be empty.");

        SchemaNode targetSchemaNode = SchemaContextUtil.findDataSchemaNode(schemaContext, targetPath);
        if (targetSchemaNode instanceof DataSchemaNode && ((DataSchemaNode) targetSchemaNode).isAddedByUses()) {
            if (targetSchemaNode instanceof DerivableSchemaNode) {
                targetSchemaNode = ((DerivableSchemaNode) targetSchemaNode).getOriginal().orNull();
            }
            if (targetSchemaNode == null) {
                throw new IllegalStateException("Failed to find target node from grouping in augmentation " +
                        schemaPathAugmentListEntry.getValue().get(0)
                        + " in module " + module.getName());
            }
        }
        if (targetSchemaNode == null) {
            throw new IllegalArgumentException("augment target not found: " + targetPath);
        }

        GeneratedTypeBuilder targetTypeBuilder = GenHelperUtil.findChildNodeByPath(targetSchemaNode.getPath(),
                genCtx);
        if (targetTypeBuilder == null) {
            targetTypeBuilder = GenHelperUtil.findCaseByPath(targetSchemaNode.getPath(), genCtx);
        }
        if (targetTypeBuilder == null) {
            throw new NullPointerException("Target type not yet generated: " + targetSchemaNode);
        }

        final String augmentNamespacePackageName =
                BindingGeneratorUtil.packageNameForAugmentedGeneratedType(basePackageName, targetPath);

        if (!(targetSchemaNode instanceof ChoiceSchemaNode)) {
            genCtx = GenHelperUtil.addRawAugmentGenTypeDefinition(module, augmentNamespacePackageName,
                    targetTypeBuilder.toInstance(), schemaPathAugmentListEntry.getValue(), genTypeBuilders, genCtx,
                    schemaContext, verboseClassComments, typeProvider, BindingNamespaceType.Data);
        } else {
            genCtx = generateTypesFromAugmentedChoiceCases(schemaContext, module, basePackageName,
                    targetTypeBuilder.toInstance(), (ChoiceSchemaNode) targetSchemaNode,
                    schemaPathAugmentListEntry.getValue(),
                    null, genCtx, verboseClassComments, genTypeBuilders, typeProvider,
                    BindingNamespaceType.Data);
        }
        return genCtx;
    }

    static Map<Module, ModuleContext> usesAugmentationToGenTypes(final SchemaContext schemaContext,
           final String augmentPackageName, final List<AugmentationSchema> schemaPathAugmentListEntry, final Module module,
           final UsesNode usesNode, final DataNodeContainer usesNodeParent, Map<Module, ModuleContext> genCtx,
           Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders, final boolean verboseClassComments,
           final TypeProvider typeProvider, final BindingNamespaceType namespaceType) {

        Preconditions.checkArgument(augmentPackageName != null, "Package Name cannot be NULL.");
        Preconditions.checkArgument(schemaPathAugmentListEntry != null,
                "Augmentation Schema List Entry cannot be NULL.");
        Preconditions.checkState(schemaPathAugmentListEntry.size() > 0,
                "Augmentation Schema List cannot be empty");

        final SchemaPath targetPath = schemaPathAugmentListEntry.get(0).getTargetPath();
        Preconditions.checkState(targetPath != null,
                "Augmentation Schema does not contain Target Path (Target Path is NULL).");

        final SchemaNode targetSchemaNode = findOriginalTargetFromGrouping(schemaContext, targetPath, usesNode);
        if (targetSchemaNode == null) {
            throw new IllegalArgumentException("augment target not found: " + targetPath);
        }

        GeneratedTypeBuilder targetTypeBuilder = GenHelperUtil.findChildNodeByPath(targetSchemaNode.getPath(),
                genCtx);
        if (targetTypeBuilder == null) {
            targetTypeBuilder = GenHelperUtil.findCaseByPath(targetSchemaNode.getPath(), genCtx);
        }
        if (targetTypeBuilder == null) {
            throw new NullPointerException("Target type not yet generated: " + targetSchemaNode);
        }

        if (!(targetSchemaNode instanceof ChoiceSchemaNode)) {
            String packageName = augmentPackageName;
            if (usesNodeParent instanceof SchemaNode) {
                packageName = BindingGeneratorUtil.packageNameForAugmentedGeneratedType(augmentPackageName,
                        ((SchemaNode) usesNodeParent).getPath());
            } else if (usesNodeParent instanceof AugmentationSchema) {
                Type parentTypeBuilder = genCtx.get(module).getTargetToAugmentation()
                        .get(((AugmentationSchema) usesNodeParent).getTargetPath());
                packageName = BindingGeneratorUtil.packageNameForAugmentedGeneratedType(parentTypeBuilder.getPackageName(),
                        (AugmentationSchema)usesNodeParent);
            }
            genCtx = GenHelperUtil.addRawAugmentGenTypeDefinition(module, packageName,
                    targetTypeBuilder.toInstance(), schemaPathAugmentListEntry, genTypeBuilders, genCtx,
                    schemaContext, verboseClassComments, typeProvider, namespaceType);
            return genCtx;
        } else {
            genCtx = generateTypesFromAugmentedChoiceCases(schemaContext, module, augmentPackageName,
                    targetTypeBuilder.toInstance(), (ChoiceSchemaNode) targetSchemaNode,
                    schemaPathAugmentListEntry,
                    usesNodeParent, genCtx, verboseClassComments, genTypeBuilders, typeProvider, namespaceType);
            return genCtx;
        }
    }

    /**
     * Convenient method to find node added by uses statement.
     * @param schemaContext
     * @param targetPath
     *            node path
     * @param parentUsesNode
     *            parent of uses node
     * @return node from its original location in grouping
     */
    private static DataSchemaNode findOriginalTargetFromGrouping(final SchemaContext schemaContext, final SchemaPath targetPath,
                                          final UsesNode parentUsesNode) {
        SchemaNode targetGrouping = null;
        QName current = parentUsesNode.getGroupingPath().getPathFromRoot().iterator().next();
        Module module = schemaContext.findModuleByNamespaceAndRevision(current.getNamespace(), current.getRevision());
        if (module == null) {
            throw new IllegalArgumentException("Fialed to find module for grouping in: " + parentUsesNode);
        } else {
            for (GroupingDefinition group : module.getGroupings()) {
                if (group.getQName().equals(current)) {
                    targetGrouping = group;
                    break;
                }
            }
        }

        if (targetGrouping == null) {
            throw new IllegalArgumentException("Failed to generate code for augment in " + parentUsesNode);
        }

        SchemaNode result = targetGrouping;
        for (final QName node : targetPath.getPathFromRoot()) {
            if (result instanceof DataNodeContainer) {
                final QName resultNode = QName.create(result.getQName().getModule(), node.getLocalName());
                result = ((DataNodeContainer) result).getDataChildByName(resultNode);
            } else if (result instanceof ChoiceSchemaNode) {
                result = ((ChoiceSchemaNode) result).getCaseNodeByName(node.getLocalName());
            }
        }
        if (result == null) {
            return null;
        }

        if (result instanceof DerivableSchemaNode) {
            DerivableSchemaNode castedResult = (DerivableSchemaNode) result;
            Optional<? extends SchemaNode> originalNode = castedResult
                    .getOriginal();
            if (castedResult.isAddedByUses() && originalNode.isPresent()) {
                result = originalNode.get();
            }
        }

        if (result instanceof DataSchemaNode) {
            DataSchemaNode resultDataSchemaNode = (DataSchemaNode) result;
            if (resultDataSchemaNode.isAddedByUses()) {
                // The original node is required, but we have only the copy of
                // the original node.
                // Maybe this indicates a bug in Yang parser.
                throw new IllegalStateException(
                        "Failed to generate code for augment in "
                                + parentUsesNode);
            } else {
                return resultDataSchemaNode;
            }
        } else {
            throw new IllegalStateException(
                    "Target node of uses-augment statement must be DataSchemaNode. Failed to generate code for augment in "
                            + parentUsesNode);
        }
    }

    /**
     * Generates list of generated types for all the cases of a choice which are
     * added to the choice through the augment.
     *
     * @param schemaContext
     * @param module
     *            current module
     * @param basePackageName
     *            string contains name of package to which augment belongs. If
     *            an augmented choice is from an other package (pcg1) than an
     *            augmenting choice (pcg2) then case's of the augmenting choice
     *            will belong to pcg2.
     * @param targetType
     *            Type which represents target choice
     * @param targetNode
     *            node which represents target choice
     * @param schemaPathAugmentListEntry
     *            list of AugmentationSchema nodes grouped by target path
     * @return list of generated types which represents augmented cases of
     *         choice <code>refChoiceType</code>
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>basePackageName</code> is null</li>
     *             <li>if <code>targetType</code> is null</li>
     *             <li>if <code>augmentedNodes</code> is null</li>
     *             </ul>
     */
    private static Map<Module, ModuleContext> generateTypesFromAugmentedChoiceCases(
            final SchemaContext schemaContext, final Module module,
            final String basePackageName, final Type targetType, final ChoiceSchemaNode targetNode,
            final List<AugmentationSchema> schemaPathAugmentListEntry,
            final DataNodeContainer usesNodeParent,
            Map<Module, ModuleContext> genCtx, final boolean verboseClassComments,
            Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders, final TypeProvider typeProvider,
            final BindingNamespaceType namespaceType) {
        Preconditions.checkArgument(basePackageName != null, "Base Package Name cannot be NULL.");
        Preconditions.checkArgument(targetType != null, "Referenced Choice Type cannot be NULL.");
        Preconditions.checkArgument(schemaPathAugmentListEntry != null, "Set of Choice Case Nodes cannot be NULL.");


        for (final AugmentationSchema augmentationSchema : schemaPathAugmentListEntry) {
            for (final DataSchemaNode caseNode : augmentationSchema.getChildNodes()) {
                if (caseNode != null) {
                    final GeneratedTypeBuilder caseTypeBuilder = GenHelperUtil.addDefaultInterfaceDefinition(basePackageName,
                            caseNode, module, genCtx, schemaContext, verboseClassComments, genTypeBuilders, typeProvider,
                            namespaceType);
                    caseTypeBuilder.addImplementsType(targetType);

                    SchemaNode parent;
                    final SchemaPath nodeSp = targetNode.getPath();
                    parent = SchemaContextUtil.findDataSchemaNode(schemaContext, nodeSp.getParent());

                    GeneratedTypeBuilder childOfType = null;
                    if (parent instanceof Module) {
                        childOfType = genCtx.get(parent).getModuleNode();
                    } else if (parent instanceof ChoiceCaseNode) {
                        childOfType = GenHelperUtil.findCaseByPath(parent.getPath(), genCtx);
                    } else if (parent instanceof DataSchemaNode || parent instanceof NotificationDefinition) {
                        childOfType = GenHelperUtil.findChildNodeByPath(parent.getPath(), genCtx);
                    } else if (parent instanceof GroupingDefinition) {
                        childOfType = GenHelperUtil.findGroupingByPath(parent.getPath(), genCtx);
                    }

                    if (childOfType == null) {
                        throw new IllegalArgumentException("Failed to find parent type of choice " + targetNode);
                    }

                    ChoiceCaseNode node = null;
                    final String caseLocalName = caseNode.getQName().getLocalName();
                    if (caseNode instanceof ChoiceCaseNode) {
                        node = (ChoiceCaseNode) caseNode;
                    } else if (targetNode.getCaseNodeByName(caseLocalName) == null) {
                        final String targetNodeLocalName = targetNode.getQName().getLocalName();
                        for (DataSchemaNode dataSchemaNode : usesNodeParent.getChildNodes()) {
                            if (dataSchemaNode instanceof ChoiceSchemaNode && targetNodeLocalName.equals(dataSchemaNode.getQName
                                    ().getLocalName())) {
                                node = ((ChoiceSchemaNode) dataSchemaNode).getCaseNodeByName(caseLocalName);
                                break;
                            }
                        }
                    } else {
                        node = targetNode.getCaseNodeByName(caseLocalName);
                    }
                    final Iterable<DataSchemaNode> childNodes = node.getChildNodes();
                    if (childNodes != null) {
                        GenHelperUtil.resolveDataSchemaNodes(module, basePackageName, caseTypeBuilder, childOfType,
                                childNodes, genCtx, schemaContext, verboseClassComments, genTypeBuilders, typeProvider,
                                namespaceType);
                    }
                    genCtx.get(module).addCaseType(caseNode.getPath(), caseTypeBuilder);
                    genCtx.get(module).addChoiceToCaseMapping(targetType, caseTypeBuilder, node);
                }
            }
        }
        return genCtx;
    }
}
