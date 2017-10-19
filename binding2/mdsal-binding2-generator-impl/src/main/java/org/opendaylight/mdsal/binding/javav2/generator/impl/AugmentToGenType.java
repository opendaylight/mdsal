/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.impl;

import static org.opendaylight.mdsal.binding.javav2.generator.impl.GenHelperUtil.processUsesImplements;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import org.opendaylight.mdsal.binding.javav2.generator.context.ModuleContext;
import org.opendaylight.mdsal.binding.javav2.generator.spi.TypeProvider;
import org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingNamespaceType;
import org.opendaylight.mdsal.binding.javav2.util.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
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
    private static final Comparator<AugmentationSchemaNode> AUGMENT_COMP = (o1, o2) -> {
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
    private static final Comparator<Map.Entry<SchemaPath, List<AugmentationSchemaNode>>> AUGMENTS_COMP = (o1, o2) -> {
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
            final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders, final boolean verboseClassComments) {

        Preconditions.checkArgument(module != null, "Module reference cannot be NULL.");
        Preconditions.checkArgument(module.getName() != null, "Module name cannot be NULL.");
        Preconditions.checkState(module.getAugmentations() != null, "Augmentations Set cannot be NULL.");

        final String basePackageName = BindingMapping.getRootPackageName(module);
        final List<AugmentationSchemaNode> augmentations = resolveAugmentations(module, schemaContext);
        Map<Module, ModuleContext> resultCtx = genCtx;

        //let's group augments by target path
        Map<SchemaPath, List<AugmentationSchemaNode>> augmentationsGrouped =
                augmentations.stream().collect(Collectors.groupingBy(AugmentationSchemaNode::getTargetPath));

        List<Map.Entry<SchemaPath, List<AugmentationSchemaNode>>> sortedAugmentationsGrouped =
                new ArrayList<>(augmentationsGrouped.entrySet());
        Collections.sort(sortedAugmentationsGrouped, AUGMENTS_COMP);

        //process child nodes of grouped augment entries
        for (Map.Entry<SchemaPath, List<AugmentationSchemaNode>> schemaPathAugmentListEntry
                : sortedAugmentationsGrouped) {
            resultCtx = augmentationToGenTypes(basePackageName, schemaPathAugmentListEntry, module, schemaContext,
                    verboseClassComments, resultCtx, genTypeBuilders, typeProvider);

            for (AugmentationSchemaNode augSchema : schemaPathAugmentListEntry.getValue()) {
                processUsesImplements(augSchema, module, schemaContext, genCtx, BindingNamespaceType.Data);
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
    @VisibleForTesting
    static List<AugmentationSchemaNode> resolveAugmentations(final Module module,
            final SchemaContext schemaContext) {
        Preconditions.checkArgument(module != null, "Module reference cannot be NULL.");
        Preconditions.checkState(module.getAugmentations() != null,
            "Augmentations Set cannot be NULL.");

        final List<AugmentationSchemaNode> sortedAugmentations = module.getAugmentations().stream()
                .filter(aug -> !module.equals(findAugmentTargetModule(schemaContext, aug)))
                .collect(Collectors.toList());
        sortedAugmentations.sort(AUGMENT_COMP);
        return sortedAugmentations;
    }

    public static Module findAugmentTargetModule(final SchemaContext schemaContext,
            final AugmentationSchemaNode aug) {
        Preconditions.checkNotNull(aug, "Augmentation schema can not be null.");
        final QName first = aug.getTargetPath().getPathFromRoot().iterator().next();
        return schemaContext.findModule(first.getModule()).orElse(null);
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
    @VisibleForTesting
    static Map<Module, ModuleContext> augmentationToGenTypes(final String basePackageName,
            final Entry<SchemaPath, List<AugmentationSchemaNode>> schemaPathAugmentListEntry, final Module module,
            final SchemaContext schemaContext, final boolean verboseClassComments,
            Map<Module, ModuleContext> genCtx, final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders,
            final TypeProvider typeProvider) {
        Preconditions.checkArgument(basePackageName != null, "Package Name cannot be NULL.");
        Preconditions.checkArgument(schemaPathAugmentListEntry != null,
            "Augmentation List Entry cannot be NULL.");
        final SchemaPath targetPath = schemaPathAugmentListEntry.getKey();
        Preconditions.checkState(targetPath != null,
                "Augmentation List Entry does not contain Target Path (Target Path is NULL).");

        final List<AugmentationSchemaNode> augmentationSchemaList = schemaPathAugmentListEntry.getValue();
        Preconditions.checkState(!augmentationSchemaList.isEmpty(),
                "Augmentation List cannot be empty.");

        SchemaNode targetSchemaNode = SchemaContextUtil.findDataSchemaNode(schemaContext, targetPath);
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

        final String augmentPackageName =
            BindingGeneratorUtil.packageNameWithNamespacePrefix(basePackageName, BindingNamespaceType.Data);

        if (!(targetSchemaNode instanceof ChoiceSchemaNode)) {
            genCtx = GenHelperUtil.addRawAugmentGenTypeDefinition(module, augmentPackageName,
                targetTypeBuilder.toInstance(), targetSchemaNode, schemaPathAugmentListEntry.getValue(),
                genTypeBuilders, genCtx, schemaContext, verboseClassComments, typeProvider, BindingNamespaceType.Data);
        } else {
            genCtx = generateTypesFromAugmentedChoiceCases(schemaContext, module, basePackageName,
                targetTypeBuilder.toInstance(), (ChoiceSchemaNode) targetSchemaNode,
                schemaPathAugmentListEntry.getValue(),genCtx, verboseClassComments, genTypeBuilders, typeProvider,
                BindingNamespaceType.Data);
        }
        return genCtx;
    }

    /**
     * Convenient method to find node added by uses statement.
     * @param schemaContext
     *            actual schema context
     * @param targetPath
     *            node path
     * @param parentUsesNode
     *            parent of uses node
     * @return node from its original location in grouping
     */
    @VisibleForTesting
    static DataSchemaNode findOriginalTargetFromGrouping(final SchemaContext schemaContext,
            final SchemaPath targetPath, final UsesNode parentUsesNode) {
        SchemaNode targetGrouping = null;
        QName current = parentUsesNode.getGroupingPath().getPathFromRoot().iterator().next();
        Module module = schemaContext.findModule(current.getModule()).orElse(null);
        if (module == null) {
            throw new IllegalArgumentException("Fialed to find module for grouping in: " + parentUsesNode);
        }
        for (GroupingDefinition group : module.getGroupings()) {
            if (group.getQName().equals(current)) {
                targetGrouping = group;
                break;
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
                result = findNamedCase((ChoiceSchemaNode) result, node.getLocalName());
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
     *            actual schema context
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
    @VisibleForTesting
    static Map<Module, ModuleContext> generateTypesFromAugmentedChoiceCases(
            final SchemaContext schemaContext, final Module module,
            final String basePackageName, final GeneratedType targetType, final ChoiceSchemaNode targetNode,
            final List<AugmentationSchemaNode> schemaPathAugmentListEntry,
            final Map<Module, ModuleContext> genCtx, final boolean verboseClassComments,
            final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders, final TypeProvider typeProvider,
            final BindingNamespaceType namespaceType) {
        Preconditions.checkArgument(basePackageName != null, "Base Package Name cannot be NULL.");
        Preconditions.checkArgument(targetType != null, "Referenced Choice Type cannot be NULL.");
        Preconditions.checkArgument(schemaPathAugmentListEntry != null, "Set of Choice Case Nodes cannot be NULL.");


        for (final AugmentationSchemaNode augmentationSchema : schemaPathAugmentListEntry) {
            for (final DataSchemaNode childNode : augmentationSchema.getChildNodes()) {
                if (childNode != null) {
                    final GeneratedTypeBuilder caseTypeBuilder =
                        GenHelperUtil.addDefaultInterfaceDefinition(basePackageName, childNode, null, module,
                            genCtx, schemaContext, verboseClassComments, genTypeBuilders, typeProvider, namespaceType);
                    caseTypeBuilder.addImplementsType(targetType);
                    caseTypeBuilder.setParentTypeForBuilder(targetType.getParentTypeForBuilder());

                    //Since uses augment nodes has been processed as inline nodes,
                    //we just take two situations below.
                    final CaseSchemaNode caseNode;
                    if (childNode instanceof CaseSchemaNode) {
                        caseNode = (CaseSchemaNode) childNode;
                    } else {
                        caseNode = findNamedCase(targetNode, childNode.getQName().getLocalName());
                        if (caseNode == null) {
                            throw new IllegalArgumentException("Failed to find case node " + childNode);
                        }
                    }

                    final Collection<DataSchemaNode> childNodes = caseNode.getChildNodes();
                    if (!childNodes.isEmpty()) {
                        GenHelperUtil.resolveDataSchemaNodes(module, basePackageName, caseTypeBuilder,
                                (GeneratedTypeBuilder) targetType.getParentTypeForBuilder(),  childNodes, genCtx,
                            schemaContext, verboseClassComments, genTypeBuilders, typeProvider, namespaceType);
                        processUsesImplements(caseNode, module, schemaContext, genCtx, namespaceType);
                    }
                    genCtx.get(module).addCaseType(childNode.getPath(), caseTypeBuilder);
                    genCtx.get(module).addChoiceToCaseMapping(targetType, caseTypeBuilder, caseNode);
                }
            }
        }
        return genCtx;
    }

    private static CaseSchemaNode findNamedCase(final ChoiceSchemaNode choice, final String caseName) {
        final List<CaseSchemaNode> cases = choice.findCaseNodes(caseName);
        return cases.isEmpty() ? null : cases.get(0);
    }
}
