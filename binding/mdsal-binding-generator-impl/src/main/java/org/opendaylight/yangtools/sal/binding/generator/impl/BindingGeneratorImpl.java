/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.opendaylight.yangtools.binding.generator.util.BindingGeneratorUtil.computeDefaultSUID;
import static org.opendaylight.yangtools.binding.generator.util.BindingGeneratorUtil.encodeAngleBrackets;
import static org.opendaylight.yangtools.binding.generator.util.BindingGeneratorUtil.packageNameForAugmentedGeneratedType;
import static org.opendaylight.yangtools.binding.generator.util.BindingGeneratorUtil.packageNameForGeneratedType;
import static org.opendaylight.yangtools.binding.generator.util.BindingTypes.DATA_OBJECT;
import static org.opendaylight.yangtools.binding.generator.util.BindingTypes.DATA_ROOT;
import static org.opendaylight.yangtools.binding.generator.util.BindingTypes.IDENTIFIABLE;
import static org.opendaylight.yangtools.binding.generator.util.BindingTypes.IDENTIFIER;
import static org.opendaylight.yangtools.binding.generator.util.BindingTypes.NOTIFICATION;
import static org.opendaylight.yangtools.binding.generator.util.BindingTypes.augmentable;
import static org.opendaylight.yangtools.binding.generator.util.Types.FUTURE;
import static org.opendaylight.yangtools.binding.generator.util.Types.VOID;
import static org.opendaylight.yangtools.binding.generator.util.Types.typeForClass;
import static org.opendaylight.yangtools.yang.model.util.SchemaContextUtil.findDataSchemaNode;
import static org.opendaylight.yangtools.yang.model.util.SchemaContextUtil.findNodeInSchemaContext;
import static org.opendaylight.yangtools.yang.model.util.SchemaContextUtil.findParentModule;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.binding.generator.util.BindingTypes;
import org.opendaylight.yangtools.binding.generator.util.ReferencedTypeImpl;
import org.opendaylight.yangtools.binding.generator.util.Types;
import org.opendaylight.yangtools.binding.generator.util.generated.type.builder.GeneratedPropertyBuilderImpl;
import org.opendaylight.yangtools.binding.generator.util.generated.type.builder.GeneratedTOBuilderImpl;
import org.opendaylight.yangtools.binding.generator.util.generated.type.builder.GeneratedTypeBuilderImpl;
import org.opendaylight.yangtools.sal.binding.generator.api.BindingGenerator;
import org.opendaylight.yangtools.sal.binding.generator.spi.TypeProvider;
import org.opendaylight.yangtools.sal.binding.model.api.AccessModifier;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.yangtools.sal.binding.yang.types.GroupingDefinitionDependencySort;
import org.opendaylight.yangtools.sal.binding.yang.types.TypeProviderImpl;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaNodeUtils;
import org.opendaylight.yangtools.yang.model.util.type.CompatUtils;
import org.opendaylight.yangtools.yang.parser.builder.util.Comparators;
import org.opendaylight.yangtools.yang.parser.util.ModuleDependencySort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BindingGeneratorImpl implements BindingGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(BindingGeneratorImpl.class);

    private final Map<Module, ModuleContext> genCtx = new HashMap<>();
    /**
     * Constant with the concrete name of identifier.
     */
    private static final String AUGMENT_IDENTIFIER_NAME = "augment-identifier";

    /**
     * Constant with the concrete name of namespace.
     */
    private static final String YANG_EXT_NAMESPACE = "urn:opendaylight:yang:extension:yang-ext";

    /**
     * When set to true, generated classes will include javadoc comments which
     * are useful for users.
     */
    private final boolean verboseClassComments;

    /**
     * Outer key represents the package name. Outer value represents map of all
     * builders in the same package. Inner key represents the schema node name
     * (in JAVA class/interface name format). Inner value represents instance of
     * builder for schema node specified in key part.
     */
    private Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders;

    /**
     * Provide methods for converting YANG types to JAVA types.
     */
    private TypeProvider typeProvider;

    /**
     * Holds reference to schema context to resolve data of augmented element
     * when creating augmentation builder
     */
    private SchemaContext schemaContext;

    /**
     * Create a new binding generator.
     *
     * @param verboseClassComments generate verbose comments
     */
    private LeafResolver leafResolver;
    private LeafListResolver leafListResolver;
    private IdentityResolver identityResolver;

    public BindingGeneratorImpl(final boolean verboseClassComments) {
        this.verboseClassComments = verboseClassComments;
    }

    /**
     * Resolves generated types from <code>context</code> schema nodes of all
     * modules.
     *
     * Generated types are created for modules, groupings, types, containers,
     * lists, choices, augments, rpcs, notification, identities.
     *
     * @param context
     *            schema context which contains data about all schema nodes
     *            saved in modules
     * @return list of types (usually <code>GeneratedType</code>
     *         <code>GeneratedTransferObject</code>which are generated from
     *         <code>context</code> data.
     * @throws IllegalArgumentException
     *             if arg <code>context</code> is null
     * @throws IllegalStateException
     *             if <code>context</code> contain no modules
     */
    @Override
    public List<Type> generateTypes(final SchemaContext context) {
        checkArgument(context != null, "Schema Context reference cannot be NULL.");
        checkState(context.getModules() != null, "Schema Context does not contain defined modules.");
        schemaContext = context;
        typeProvider = new TypeProviderImpl(context);
        final Set<Module> modules = context.getModules();
        leafResolver = new LeafResolver(schemaContext, typeProvider, genCtx);
        leafListResolver = new LeafListResolver(schemaContext, typeProvider, genCtx);
        identityResolver = new IdentityResolver(schemaContext, genCtx);
        return generateTypes(context, modules);
    }

    /**
     * Resolves generated types from <code>context</code> schema nodes only for
     * modules specified in <code>modules</code>
     *
     * Generated types are created for modules, groupings, types, containers,
     * lists, choices, augments, rpcs, notification, identities.
     *
     * @param context
     *            schema context which contains data about all schema nodes
     *            saved in modules
     * @param modules
     *            set of modules for which schema nodes should be generated
     *            types
     * @return list of types (usually <code>GeneratedType</code> or
     *         <code>GeneratedTransferObject</code>) which:
     *         <ul>
     *         <li>are generated from <code>context</code> schema nodes and</li>
     *         <li>are also part of some of the module in <code>modules</code>
     *         set.</li>
     *         </ul>
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if arg <code>context</code> is null or</li>
     *             <li>if arg <code>modules</code> is null</li>
     *             </ul>
     * @throws IllegalStateException
     *             if <code>context</code> contain no modules
     */
    @Override
    public List<Type> generateTypes(final SchemaContext context, final Set<Module> modules) {
        checkArgument(context != null, "Schema Context reference cannot be NULL.");
        checkState(context.getModules() != null, "Schema Context does not contain defined modules.");
        checkArgument(modules != null, "Set of Modules cannot be NULL.");

        schemaContext = context;
        typeProvider = new TypeProviderImpl(context);
        final Module[] modulesArray = new Module[context.getModules().size()];
        context.getModules().toArray(modulesArray);
        final List<Module> contextModules = ModuleDependencySort.sort(modulesArray);
        genTypeBuilders = new HashMap<>();
        leafResolver = new LeafResolver(schemaContext, typeProvider, genCtx);
        leafListResolver = new LeafListResolver(schemaContext, typeProvider, genCtx);
        identityResolver = new IdentityResolver(schemaContext, genCtx);
        for (final Module contextModule : contextModules) {
            moduleToGenTypes(contextModule, context);
        }
        for (final Module contextModule : contextModules) {
            allAugmentsToGenTypes(contextModule);
        }

        final List<Type> filteredGenTypes = new ArrayList<>();
        for (final Module m : modules) {
            final ModuleContext ctx = checkNotNull(genCtx.get(m), "Module context not found for module %s", m);
            filteredGenTypes.addAll(ctx.getGeneratedTypes());
            final Set<Type> additionalTypes = ((TypeProviderImpl) typeProvider).getAdditionalTypes().get(m);
            if (additionalTypes != null) {
                filteredGenTypes.addAll(additionalTypes);
            }
        }

        return filteredGenTypes;
    }

    private void moduleToGenTypes(final Module module, final SchemaContext context) {
        genCtx.put(module, new ModuleContext());
        TypeDefGenerator typeDefGenerator = new TypeDefGenerator(typeProvider, genCtx);
        typeDefGenerator.allTypeDefinitionsToGenTypes(module);
        groupingsToGenTypes(module, module.getGroupings());
        rpcMethodsToGenType(module);
        identityResolver.allIdentitiesToGenTypes(module, context, verboseClassComments);
        notificationsToGenType(module);

        if (!module.getChildNodes().isEmpty()) {
            final GeneratedTypeBuilder moduleType = moduleToDataType(module);
            genCtx.get(module).addModuleNode(moduleType);
            final String basePackageName = BindingMapping.getRootPackageName(module.getQNameModule());
            resolveDataSchemaNodes(module, basePackageName, moduleType, moduleType, module.getChildNodes());
        }
    }

    private GeneratedTypeBuilder processDataSchemaNode(final Module module, final String basePackageName,
            final GeneratedTypeBuilder childOf, final DataSchemaNode node) {
        if (node.isAugmenting() || node.isAddedByUses()) {
            return null;
        }
        final String packageName = packageNameForGeneratedType(basePackageName, node.getPath());
        final GeneratedTypeBuilder genType = addDefaultInterfaceDefinition(packageName, node, childOf, module);
        genType.addComment(node.getDescription());
        BindingTextUtils.annotateDeprecatedIfNecessary(node.getStatus(), genType);
        final String parentName = BindingTextUtils.getParentName(schemaContext, node);
        genType.setDescription(BindingTextUtils.createDescription(node, genType.getFullyQualifiedName(),
                verboseClassComments, parentName));
        genType.setModuleName(module.getName());
        genType.setReference(node.getReference());
        genType.setSchemaPath(node.getPath().getPathFromRoot());
        if (node instanceof DataNodeContainer) {
            genCtx.get(module).addChildNodeType(node, genType);
            groupingsToGenTypes(module, ((DataNodeContainer) node).getGroupings());
            processUsesAugments((DataNodeContainer) node, module);
        }
        return genType;
    }

    private void containerToGenType(final Module module, final String basePackageName,
            final GeneratedTypeBuilder parent, final GeneratedTypeBuilder childOf, final ContainerSchemaNode node) {
        final GeneratedTypeBuilder genType = processDataSchemaNode(module, basePackageName, childOf, node);
        if (genType != null) {
            BindingTextUtils.constructGetter(parent, node.getQName().getLocalName(), node.getDescription(), genType,
                    node.getStatus());
            resolveDataSchemaNodes(module, basePackageName, genType, genType, node.getChildNodes());
        }
    }

    /**
     * Converts all <b>augmentation</b> of the module to the list
     * <code>Type</code> objects.
     *
     * @param module
     *            module from which is obtained list of all augmentation objects
     *            to iterate over them
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if the module is null</li>
     *             <li>if the name of module is null</li>
     *             </ul>
     * @throws IllegalStateException
     *             if set of augmentations from module is null
     */
    public void allAugmentsToGenTypes(final Module module) {
        checkArgument(module != null, "Module reference cannot be NULL.");
        checkArgument(module.getName() != null, "Module name cannot be NULL.");
        checkState(module.getAugmentations() != null, "Augmentations Set cannot be NULL.");

        final String basePackageName = BindingMapping.getRootPackageName(module.getQNameModule());
        final List<AugmentationSchema> augmentations = resolveAugmentations(module);
        for (final AugmentationSchema augment : augmentations) {
            augmentationToGenTypes(basePackageName, augment, module);
        }
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
    private List<AugmentationSchema> resolveAugmentations(final Module module) {
        checkArgument(module != null, "Module reference cannot be NULL.");
        checkState(module.getAugmentations() != null, "Augmentations Set cannot be NULL.");

        final Set<AugmentationSchema> augmentations = module.getAugmentations();
        final List<AugmentationSchema> sortedAugmentations = new ArrayList<>(augmentations);
        Collections.sort(sortedAugmentations, Comparators.AUGMENT_COMP);

        return sortedAugmentations;
    }

    /**
     * Converts <code>augSchema</code> to list of <code>Type</code> which
     * contains generated type for augmentation. In addition there are also
     * generated types for all containers, list and choices which are child of
     * <code>augSchema</code> node or a generated types for cases are added if
     * augmented node is choice.
     *
     * @param augmentPackageName
     *            string with the name of the package to which the augmentation
     *            belongs
     * @param augSchema
     *            AugmentationSchema which is contains data about augmentation
     *            (target path, childs...)
     * @param module
     *            current module
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>augmentPackageName</code> equals null</li>
     *             <li>if <code>augSchema</code> equals null</li>
     *             </ul>
     * @throws IllegalStateException
     *             if augment target path is null
     */
    private void augmentationToGenTypes(final String augmentPackageName, final AugmentationSchema augSchema,
                                        final Module module) {
        checkArgument(augmentPackageName != null, "Package Name cannot be NULL.");
        checkArgument(augSchema != null, "Augmentation Schema cannot be NULL.");
        checkState(augSchema.getTargetPath() != null,
                "Augmentation Schema does not contain Target Path (Target Path is NULL).");

        processUsesAugments(augSchema, module);
        final SchemaPath targetPath = augSchema.getTargetPath();
        SchemaNode targetSchemaNode = findDataSchemaNode(schemaContext, targetPath);
        if (targetSchemaNode instanceof DataSchemaNode && ((DataSchemaNode) targetSchemaNode).isAddedByUses()) {
            if (targetSchemaNode instanceof DerivableSchemaNode) {
                targetSchemaNode = ((DerivableSchemaNode) targetSchemaNode).getOriginal().orNull();
            }
            if (targetSchemaNode == null) {
                throw new IllegalStateException("Failed to find target node from grouping in augmentation " + augSchema
                        + " in module " + module.getName());
            }
        }
        if (targetSchemaNode == null) {
            throw new IllegalArgumentException("augment target not found: " + targetPath);
        }

        GeneratedTypeBuilder targetTypeBuilder = BindingResolverUtils.findChildNodeByPath(targetSchemaNode.getPath()
                , genCtx.values());
        if (targetTypeBuilder == null) {
            targetTypeBuilder = BindingResolverUtils.findCaseByPath(targetSchemaNode.getPath(), genCtx.values());
        }
        if (targetTypeBuilder == null) {
            throw new NullPointerException("Target type not yet generated: " + targetSchemaNode);
        }

        if (!(targetSchemaNode instanceof ChoiceSchemaNode)) {
            final String packageName = augmentPackageName;
            final Type targetType = new ReferencedTypeImpl(targetTypeBuilder.getPackageName(),
                    targetTypeBuilder.getName());
            addRawAugmentGenTypeDefinition(module, packageName, augmentPackageName, targetType, augSchema);

        } else {
            generateTypesFromAugmentedChoiceCases(module, augmentPackageName, targetTypeBuilder.toInstance(),
                    (ChoiceSchemaNode) targetSchemaNode, augSchema.getChildNodes(), null);
        }
    }

    public void processUsesAugments(final DataNodeContainer node, final Module module) {
        final String basePackageName = BindingMapping.getRootPackageName(module.getQNameModule());
        for (final UsesNode usesNode : node.getUses()) {
            for (final AugmentationSchema augment : usesNode.getAugmentations()) {
                usesAugmentationToGenTypes(basePackageName, augment, module, usesNode, node);
                processUsesAugments(augment, module);
            }
        }
    }

    private void usesAugmentationToGenTypes(final String augmentPackageName, final AugmentationSchema augSchema,
                                            final Module module, final UsesNode usesNode, final DataNodeContainer usesNodeParent) {
        checkArgument(augmentPackageName != null, "Package Name cannot be NULL.");
        checkArgument(augSchema != null, "Augmentation Schema cannot be NULL.");
        checkState(augSchema.getTargetPath() != null,
                "Augmentation Schema does not contain Target Path (Target Path is NULL).");

        processUsesAugments(augSchema, module);
        final SchemaPath targetPath = augSchema.getTargetPath();
        final SchemaNode targetSchemaNode = findOriginalTargetFromGrouping(targetPath, usesNode);
        if (targetSchemaNode == null) {
            throw new IllegalArgumentException("augment target not found: " + targetPath);
        }

        GeneratedTypeBuilder targetTypeBuilder = BindingResolverUtils.findChildNodeByPath(targetSchemaNode.getPath()
                , genCtx.values());
        if (targetTypeBuilder == null) {
            targetTypeBuilder = BindingResolverUtils.findCaseByPath(targetSchemaNode.getPath(), genCtx.values());
        }
        if (targetTypeBuilder == null) {
            throw new NullPointerException("Target type not yet generated: " + targetSchemaNode);
        }

        if (!(targetSchemaNode instanceof ChoiceSchemaNode)) {
            String packageName = augmentPackageName;
            if (usesNodeParent instanceof SchemaNode) {
                packageName = packageNameForAugmentedGeneratedType(augmentPackageName, ((SchemaNode) usesNodeParent).getPath());
            }
            addRawAugmentGenTypeDefinition(module, packageName, augmentPackageName, targetTypeBuilder.toInstance(),
                    augSchema);
        } else {
            generateTypesFromAugmentedChoiceCases(module, augmentPackageName, targetTypeBuilder.toInstance(),
                    (ChoiceSchemaNode) targetSchemaNode, augSchema.getChildNodes(), usesNodeParent);
        }
    }

    /**
     * Convenient method to find node added by uses statement.
     *
     * @param targetPath
     *            node path
     * @param parentUsesNode
     *            parent of uses node
     * @return node from its original location in grouping
     */
    private DataSchemaNode findOriginalTargetFromGrouping(final SchemaPath targetPath, final UsesNode parentUsesNode) {
        final SchemaNode targetGrouping = findNodeInSchemaContext(schemaContext, parentUsesNode.getGroupingPath()
                .getPathFromRoot());
        if (!(targetGrouping instanceof GroupingDefinition)) {
            throw new IllegalArgumentException("Failed to generate code for augment in " + parentUsesNode);
        }

        final GroupingDefinition grouping = (GroupingDefinition) targetGrouping;
        SchemaNode result = grouping;
        for (final QName node : targetPath.getPathFromRoot()) {
            // finding by local name is valid, grouping cannot contain nodes
            // with same name and different namespace
            if (result instanceof DataNodeContainer) {
                result = ((DataNodeContainer) result).getDataChildByName(node.getLocalName());
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
     * @param augmentedNodes
     *            set of choice case nodes for which is checked if are/aren't
     *            added to choice through augmentation
     * @return list of generated types which represents augmented cases of
     *         choice <code>refChoiceType</code>
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>basePackageName</code> is null</li>
     *             <li>if <code>targetType</code> is null</li>
     *             <li>if <code>augmentedNodes</code> is null</li>
     *             </ul>
     */
    private void generateTypesFromAugmentedChoiceCases(final Module module, final String basePackageName,
                                                       final Type targetType, final ChoiceSchemaNode targetNode, final Iterable<DataSchemaNode> augmentedNodes,
                                                       final DataNodeContainer usesNodeParent) {
        checkArgument(basePackageName != null, "Base Package Name cannot be NULL.");
        checkArgument(targetType != null, "Referenced Choice Type cannot be NULL.");
        checkArgument(augmentedNodes != null, "Set of Choice Case Nodes cannot be NULL.");

        for (final DataSchemaNode caseNode : augmentedNodes) {
            if (caseNode != null) {
                final String packageName = packageNameForGeneratedType(basePackageName, caseNode.getPath());
                final GeneratedTypeBuilder caseTypeBuilder = addDefaultInterfaceDefinition(packageName, caseNode, module);
                caseTypeBuilder.addImplementsType(targetType);

                final SchemaPath nodeSp = targetNode.getPath();
                SchemaNode parent = findDataSchemaNode(schemaContext, nodeSp.getParent());

                GeneratedTypeBuilder childOfType = null;
                final Collection<ModuleContext> ModuleContexts = genCtx.values();
                if (parent instanceof Module) {
                    childOfType = genCtx.get(parent).getModuleNode();
                } else if (parent instanceof ChoiceCaseNode) {
                    childOfType = BindingResolverUtils.findCaseByPath(parent.getPath(), ModuleContexts);
                } else if (parent instanceof DataSchemaNode || parent instanceof NotificationDefinition) {
                    childOfType = BindingResolverUtils.findChildNodeByPath(parent.getPath(), ModuleContexts);
                } else if (parent instanceof GroupingDefinition) {
                    childOfType = BindingResolverUtils.findGroupingByPath(parent.getPath(), ModuleContexts);
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
                    resolveDataSchemaNodes(module, basePackageName, caseTypeBuilder, childOfType, childNodes);
                }
                genCtx.get(module).addCaseType(caseNode.getPath(), caseTypeBuilder);
                genCtx.get(module).addChoiceToCaseMapping(targetType, caseTypeBuilder, node);
            }
        }
    }

    /**
     * Returns a generated type builder for an augmentation.
     *
     * The name of the type builder is equal to the name of augmented node with
     * serial number as suffix.
     *
     * @param module
     *            current module
     * @param augmentPackageName
     *            string with contains the package name to which the augment
     *            belongs
     * @param basePackageName
     *            string with the package name to which the augmented node
     *            belongs
     * @param targetTypeRef
     *            target type
     * @param augSchema
     *            augmentation schema which contains data about the child nodes
     *            and uses of augment
     * @return generated type builder for augment
     */
    private GeneratedTypeBuilder addRawAugmentGenTypeDefinition(final Module module, final String augmentPackageName,
                                                                final String basePackageName, final Type targetTypeRef, final AugmentationSchema augSchema) {
        Map<String, GeneratedTypeBuilder> augmentBuilders = genTypeBuilders.get(augmentPackageName);
        if (augmentBuilders == null) {
            augmentBuilders = new HashMap<>();
            genTypeBuilders.put(augmentPackageName, augmentBuilders);
        }
        final String augIdentifier = getAugmentIdentifier(augSchema.getUnknownSchemaNodes());

        String augTypeName;
        if (augIdentifier != null) {
            augTypeName = BindingMapping.getClassName(augIdentifier);
        } else {
            augTypeName = augGenTypeName(augmentBuilders, targetTypeRef.getName());
        }

        final GeneratedTypeBuilder augTypeBuilder = new GeneratedTypeBuilderImpl(augmentPackageName, augTypeName);

        augTypeBuilder.addImplementsType(DATA_OBJECT);
        augTypeBuilder.addImplementsType(Types.augmentationTypeFor(targetTypeRef));
        BindingTextUtils.annotateDeprecatedIfNecessary(augSchema.getStatus(), augTypeBuilder);
        addImplementedInterfaceFromUses(augSchema, augTypeBuilder);

        augSchemaNodeToMethods(module, basePackageName, augTypeBuilder, augTypeBuilder, augSchema.getChildNodes());
        augmentBuilders.put(augTypeName, augTypeBuilder);

        if(!augSchema.getChildNodes().isEmpty()) {
            genCtx.get(module).addTargetToAugmentation(targetTypeRef, augTypeBuilder);
            genCtx.get(module).addTypeToAugmentation(augTypeBuilder, augSchema);

        }
        genCtx.get(module).addAugmentType(augTypeBuilder);
        return augTypeBuilder;
    }

    /**
     * Adds the methods to <code>typeBuilder</code> what represents subnodes of
     * node for which <code>typeBuilder</code> was created.
     *
     * @param module
     *            current module
     * @param basePackageName
     *            string contains the module package name
     * @param typeBuilder
     *            generated type builder which represents any node. The subnodes
     *            of this node are added to the <code>typeBuilder</code> as
     *            methods. The subnode can be of type leaf, leaf-list, list,
     *            container, choice.
     * @param childOf
     *            parent type
     * @param schemaNodes
     *            set of data schema nodes which are the children of the node
     *            for which <code>typeBuilder</code> was created
     * @return generated type builder which is the same object as the input
     *         parameter <code>typeBuilder</code>. The getter method could be
     *         added to it.
     */
    private GeneratedTypeBuilder augSchemaNodeToMethods(final Module module, final String basePackageName,
                                                        final GeneratedTypeBuilder typeBuilder, final GeneratedTypeBuilder childOf,
                                                        final Iterable<DataSchemaNode> schemaNodes) {
        if ((schemaNodes != null) && (typeBuilder != null)) {
            for (final DataSchemaNode schemaNode : schemaNodes) {
                if (!schemaNode.isAugmenting()) {
                    addSchemaNodeToBuilderAsMethod(basePackageName, schemaNode, typeBuilder, childOf, module);
                }
            }
        }
        return typeBuilder;
    }

    /**
     *
     * @param unknownSchemaNodes
     *          list of unknown schema nodes from augmentation
     * @return nodeParameter of UnknownSchemaNode
     */
    private static String getAugmentIdentifier(final List<UnknownSchemaNode> unknownSchemaNodes) {
        for (final UnknownSchemaNode unknownSchemaNode : unknownSchemaNodes) {
            final QName nodeType = unknownSchemaNode.getNodeType();
            if (AUGMENT_IDENTIFIER_NAME.equals(nodeType.getLocalName())
                    && YANG_EXT_NAMESPACE.equals(nodeType.getNamespace().toString())) {
                return unknownSchemaNode.getNodeParameter();
            }
        }
        return null;
    }

    /**
     * Returns first unique name for the augment generated type builder. The
     * generated type builder name for augment consists from name of augmented
     * node and serial number of its augmentation.
     *
     * @param builders
     *            map of builders which were created in the package to which the
     *            augmentation belongs
     * @param genTypeName
     *            string with name of augmented node
     * @return string with unique name for augmentation builder
     */
    private static String augGenTypeName(final Map<String, GeneratedTypeBuilder> builders, final String genTypeName) {
        int index = 1;
        if (builders != null) {
            while (builders.containsKey(genTypeName + index)) {
                index = index + 1;
            }
        }
        return genTypeName + index;
    }

    /**
     * Create GeneratedTypeBuilder object from module argument.
     *
     * @param module
     *            Module object from which builder will be created
     * @return <code>GeneratedTypeBuilder</code> which is internal
     *         representation of the module
     * @throws IllegalArgumentException
     *             if module is null
     */
    private GeneratedTypeBuilder moduleToDataType(final Module module) {
        checkArgument(module != null, "Module reference cannot be NULL.");

        final GeneratedTypeBuilder moduleDataTypeBuilder = moduleTypeBuilder(module, "Data");
        addImplementedInterfaceFromUses(module, moduleDataTypeBuilder);
        moduleDataTypeBuilder.addImplementsType(DATA_ROOT);
        moduleDataTypeBuilder.addComment(module.getDescription());
        moduleDataTypeBuilder.setDescription(BindingTextUtils.createDescription(module, verboseClassComments));
        moduleDataTypeBuilder.setReference(module.getReference());
        return moduleDataTypeBuilder;
    }

    /**
     * Converts all <b>rpcs</b> inputs and outputs substatements of the module
     * to the list of <code>Type</code> objects. In addition are to containers
     * and lists which belong to input or output also part of returning list.
     *
     * @param module
     *            module from which is obtained set of all rpc objects to
     *            iterate over them
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if the module is null</li>
     *             <li>if the name of module is null</li>
     *             </ul>
     * @throws IllegalStateException
     *             if set of rpcs from module is null
     */
    private void rpcMethodsToGenType(final Module module) {
        checkArgument(module != null, "Module reference cannot be NULL.");
        checkArgument(module.getName() != null, "Module name cannot be NULL.");
        final Set<RpcDefinition> rpcDefinitions = module.getRpcs();
        checkState(rpcDefinitions != null, "Set of rpcs from module " + module.getName() + " cannot be NULL.");
        if (rpcDefinitions.isEmpty()) {
            return;
        }

        final String basePackageName = BindingMapping.getRootPackageName(module.getQNameModule());
        final GeneratedTypeBuilder interfaceBuilder = moduleTypeBuilder(module, "Service");
        interfaceBuilder.addImplementsType(Types.typeForClass(RpcService.class));
        interfaceBuilder.setDescription(BindingTextUtils.createDescription(rpcDefinitions, module.getName(), verboseClassComments));

        for (final RpcDefinition rpc : rpcDefinitions) {
            if (rpc != null) {
                final String rpcName = BindingMapping.getClassName(rpc.getQName());
                final String rpcMethodName = BindingMapping.getPropertyName(rpcName);
                final String rpcComment = encodeAngleBrackets(rpc.getDescription());
                final MethodSignatureBuilder method = interfaceBuilder.addMethod(rpcMethodName);
                final ContainerSchemaNode input = rpc.getInput();
                final ContainerSchemaNode output = rpc.getOutput();

                if (input != null) {
                    final GeneratedTypeBuilder inType = addRawInterfaceDefinition(basePackageName, input, rpcName);
                    addImplementedInterfaceFromUses(input, inType);
                    inType.addImplementsType(DATA_OBJECT);
                    inType.addImplementsType(augmentable(inType));
                    BindingTextUtils.annotateDeprecatedIfNecessary(rpc.getStatus(), inType);
                    resolveDataSchemaNodes(module, basePackageName, inType, inType, input.getChildNodes());
                    genCtx.get(module).addChildNodeType(input, inType);
                    final GeneratedType inTypeInstance = inType.toInstance();
                    method.addParameter(inTypeInstance, "input");
                }

                Type outTypeInstance = VOID;
                if (output != null) {
                    final GeneratedTypeBuilder outType = addRawInterfaceDefinition(basePackageName, output, rpcName);
                    addImplementedInterfaceFromUses(output, outType);
                    outType.addImplementsType(DATA_OBJECT);
                    outType.addImplementsType(augmentable(outType));
                    BindingTextUtils.annotateDeprecatedIfNecessary(rpc.getStatus(), outType);
                    resolveDataSchemaNodes(module, basePackageName, outType, outType, output.getChildNodes());
                    genCtx.get(module).addChildNodeType(output, outType);
                    outTypeInstance = outType.toInstance();
                }

                final Type rpcRes = Types.parameterizedTypeFor(Types.typeForClass(RpcResult.class), outTypeInstance);
                method.setComment(rpcComment);
                method.setReturnType(Types.parameterizedTypeFor(FUTURE, rpcRes));
            }
        }

        genCtx.get(module).addTopLevelNodeType(interfaceBuilder);
    }

    /**
     * Converts all <b>notifications</b> of the module to the list of
     * <code>Type</code> objects. In addition are to this list added containers
     * and lists which are part of this notification.
     *
     * @param module
     *            module from which is obtained set of all notification objects
     *            to iterate over them
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if the module equals null</li>
     *             <li>if the name of module equals null</li>
     *             </ul>
     * @throws IllegalStateException
     *             if set of notifications from module is null
     */
    private void notificationsToGenType(final Module module) {
        checkArgument(module != null, "Module reference cannot be NULL.");
        checkArgument(module.getName() != null, "Module name cannot be NULL.");
        final Set<NotificationDefinition> notifications = module.getNotifications();
        checkState(notifications != null, "Set of notification from module " + module.getName() + " cannot be NULL.");
        if (notifications.isEmpty()) {
            return;
        }

        final GeneratedTypeBuilder listenerInterface = moduleTypeBuilder(module, "Listener");
        listenerInterface.addImplementsType(BindingTypes.NOTIFICATION_LISTENER);
        final String basePackageName = BindingMapping.getRootPackageName(module.getQNameModule());



        for (final NotificationDefinition notification : notifications) {
            if (notification != null) {
                processUsesAugments(notification, module);

                final GeneratedTypeBuilder notificationInterface = addDefaultInterfaceDefinition(basePackageName,
                        notification, null, module);
                BindingTextUtils.annotateDeprecatedIfNecessary(notification.getStatus(), notificationInterface);
                notificationInterface.addImplementsType(NOTIFICATION);
                genCtx.get(module).addChildNodeType(notification, notificationInterface);

                // Notification object
                resolveDataSchemaNodes(module, basePackageName, notificationInterface, notificationInterface,
                        notification.getChildNodes());

                listenerInterface.addMethod("on" + notificationInterface.getName())
                .setAccessModifier(AccessModifier.PUBLIC).addParameter(notificationInterface, "notification")
                .setComment(encodeAngleBrackets(notification.getDescription())).setReturnType(Types.VOID);
            }
        }
        listenerInterface.setDescription(BindingTextUtils.createDescription(notifications, module.getName(), verboseClassComments));

        genCtx.get(module).addTopLevelNodeType(listenerInterface);
    }

    /**
     * Converts all <b>groupings</b> of the module to the list of
     * <code>Type</code> objects. Firstly are groupings sorted according mutual
     * dependencies. At least dependent (independent) groupings are in the list
     * saved at first positions. For every grouping the record is added to map
     * {@link BindingGeneratorImpl allGroupings}
     *
     * @param module
     *            current module
     * @param groupings
     *            Collection of groupings from which types will be generated
     *
     */
    public void groupingsToGenTypes(final Module module, final Collection<GroupingDefinition> groupings) {
        final String basePackageName = BindingMapping.getRootPackageName(module.getQNameModule());
        final List<GroupingDefinition> groupingsSortedByDependencies = new GroupingDefinitionDependencySort()
                .sort(groupings);
        for (final GroupingDefinition grouping : groupingsSortedByDependencies) {
            groupingToGenType(basePackageName, grouping, module);
        }
    }

    /**
     * Converts individual grouping to GeneratedType. Firstly generated type
     * builder is created and every child node of grouping is resolved to the
     * method.
     *
     * @param basePackageName
     *            string contains the module package name
     * @param grouping
     *            GroupingDefinition which contains data about grouping
     * @param module
     *            current module
     * @return GeneratedType which is generated from grouping (object of type
     *         <code>GroupingDefinition</code>)
     */
    private void groupingToGenType(final String basePackageName, final GroupingDefinition grouping, final Module module) {
        final String packageName = packageNameForGeneratedType(basePackageName, grouping.getPath());
        final GeneratedTypeBuilder genType = addDefaultInterfaceDefinition(packageName, grouping, module);
        BindingTextUtils.annotateDeprecatedIfNecessary(grouping.getStatus(), genType);
        genCtx.get(module).addGroupingType(grouping.getPath(), genType);
        resolveDataSchemaNodes(module, basePackageName, genType, genType, grouping.getChildNodes());
        groupingsToGenTypes(module, grouping.getGroupings());
        processUsesAugments(grouping, module);
    }

    /**
     * Generates type builder for <code>module</code>.
     *
     * @param module
     *            Module which is source of package name for generated type
     *            builder
     * @param postfix
     *            string which is added to the module class name representation
     *            as suffix
     * @return instance of GeneratedTypeBuilder which represents
     *         <code>module</code>.
     * @throws IllegalArgumentException
     *             if <code>module</code> is null
     */
    private GeneratedTypeBuilder moduleTypeBuilder(final Module module, final String postfix) {
        checkArgument(module != null, "Module reference cannot be NULL.");
        final String packageName = BindingMapping.getRootPackageName(module.getQNameModule());
        final String moduleName = BindingMapping.getClassName(module.getName()) + postfix;

        final GeneratedTypeBuilderImpl moduleBuilder = new GeneratedTypeBuilderImpl(packageName, moduleName);
        moduleBuilder.setDescription(BindingTextUtils.createDescription(module, verboseClassComments));
        moduleBuilder.setReference(module.getReference());
        moduleBuilder.setModuleName(moduleName);

        return moduleBuilder;
    }

    /**
     * Adds the methods to <code>typeBuilder</code> which represent subnodes of
     * node for which <code>typeBuilder</code> was created.
     *
     * The subnodes aren't mapped to the methods if they are part of grouping or
     * augment (in this case are already part of them).
     *
     * @param module
     *            current module
     * @param basePackageName
     *            string contains the module package name
     * @param parent
     *            generated type builder which represents any node. The subnodes
     *            of this node are added to the <code>typeBuilder</code> as
     *            methods. The subnode can be of type leaf, leaf-list, list,
     *            container, choice.
     * @param childOf
     *            parent type
     * @param schemaNodes
     *            set of data schema nodes which are the children of the node
     *            for which <code>typeBuilder</code> was created
     * @return generated type builder which is the same builder as input
     *         parameter. The getter methods (representing child nodes) could be
     *         added to it.
     */
    private GeneratedTypeBuilder resolveDataSchemaNodes(final Module module, final String basePackageName,
            final GeneratedTypeBuilder parent, final GeneratedTypeBuilder childOf, final Iterable<DataSchemaNode> schemaNodes) {
        if (schemaNodes != null && parent != null) {
            for (final DataSchemaNode schemaNode : schemaNodes) {
                if (!schemaNode.isAugmenting() && !schemaNode.isAddedByUses()) {
                    addSchemaNodeToBuilderAsMethod(basePackageName, schemaNode, parent, childOf, module);
                }
            }
        }
        return parent;
    }

    /**
     * Adds to <code>typeBuilder</code> a method which is derived from
     * <code>schemaNode</code>.
     *
     * @param basePackageName
     *            string with the module package name
     * @param node
     *            data schema node which is added to <code>typeBuilder</code> as
     *            a method
     * @param typeBuilder
     *            generated type builder to which is <code>schemaNode</code>
     *            added as a method.
     * @param childOf
     *            parent type
     * @param module
     *            current module
     */
    private void addSchemaNodeToBuilderAsMethod(final String basePackageName, final DataSchemaNode node,
            final GeneratedTypeBuilder typeBuilder, final GeneratedTypeBuilder childOf, final Module module) {
        if (node != null && typeBuilder != null) {
            if (node instanceof LeafSchemaNode) {
                leafResolver.resolveLeafSchemaNodeAsMethod(typeBuilder, (LeafSchemaNode) node, module);
            } else if (node instanceof LeafListSchemaNode) {
                leafListResolver.resolveLeafListSchemaNode(typeBuilder, (LeafListSchemaNode) node,module);
            } else if (node instanceof ContainerSchemaNode) {
                containerToGenType(module, basePackageName, typeBuilder, childOf, (ContainerSchemaNode) node);
            } else if (node instanceof ListSchemaNode) {
                listToGenType(module, basePackageName, typeBuilder, childOf, (ListSchemaNode) node);
            } else if (node instanceof ChoiceSchemaNode) {
                choiceToGeneratedType(module, basePackageName, typeBuilder, (ChoiceSchemaNode) node);
            } else {
                // TODO: anyxml not yet supported
                LOG.debug("Unable to add schema node {} as method in {}: unsupported type of node.", node.getClass(),
                        typeBuilder.getFullyQualifiedName());
            }
        }
    }

    /**
     * Converts <code>choiceNode</code> to the list of generated types for
     * choice and its cases.
     *
     * The package names for choice and for its cases are created as
     * concatenation of the module package (<code>basePackageName</code>) and
     * names of all parents node.
     *
     * @param module
     *            current module
     * @param basePackageName
     *            string with the module package name
     * @param parent
     *            parent type
     * @param choiceNode
     *            choice node which is mapped to generated type. Also child
     *            nodes - cases are mapped to generated types.
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>basePackageName</code> is null</li>
     *             <li>if <code>choiceNode</code> is null</li>
     *             </ul>
     */
    private void choiceToGeneratedType(final Module module, final String basePackageName,
            final GeneratedTypeBuilder parent, final ChoiceSchemaNode choiceNode) {
        checkArgument(basePackageName != null, "Base Package Name cannot be NULL.");
        checkArgument(choiceNode != null, "Choice Schema Node cannot be NULL.");

        if (!choiceNode.isAddedByUses()) {
            final String packageName = packageNameForGeneratedType(basePackageName, choiceNode.getPath());
            final GeneratedTypeBuilder choiceTypeBuilder = addRawInterfaceDefinition(packageName, choiceNode);
            BindingTextUtils.constructGetter(parent, choiceNode.getQName().getLocalName(), choiceNode.getDescription(),
                    choiceTypeBuilder, choiceNode.getStatus());
            choiceTypeBuilder.addImplementsType(typeForClass(DataContainer.class));
            BindingTextUtils.annotateDeprecatedIfNecessary(choiceNode.getStatus(), choiceTypeBuilder);
            genCtx.get(module).addChildNodeType(choiceNode, choiceTypeBuilder);
            generateTypesFromChoiceCases(module, basePackageName, choiceTypeBuilder.toInstance(), choiceNode);
        }
    }

    /**
     * Converts <code>caseNodes</code> set to list of corresponding generated
     * types.
     *
     * For every <i>case</i> which isn't added through augment or <i>uses</i> is
     * created generated type builder. The package names for the builder is
     * created as concatenation of the module package (
     * <code>basePackageName</code>) and names of all parents nodes of the
     * concrete <i>case</i>. There is also relation "<i>implements type</i>"
     * between every case builder and <i>choice</i> type
     *
     * @param module
     *            current module
     * @param basePackageName
     *            string with the module package name
     * @param refChoiceType
     *            type which represents superior <i>case</i>
     * @param choiceNode
     *            choice case node which is mapped to generated type
     * @return list of generated types for <code>caseNodes</code>.
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>basePackageName</code> equals null</li>
     *             <li>if <code>refChoiceType</code> equals null</li>
     *             <li>if <code>caseNodes</code> equals null</li>
     *             </ul>
     */
    private void generateTypesFromChoiceCases(final Module module, final String basePackageName,
            final Type refChoiceType, final ChoiceSchemaNode choiceNode) {
        checkArgument(basePackageName != null, "Base Package Name cannot be NULL.");
        checkArgument(refChoiceType != null, "Referenced Choice Type cannot be NULL.");
        checkArgument(choiceNode != null, "ChoiceNode cannot be NULL.");

        final Set<ChoiceCaseNode> caseNodes = choiceNode.getCases();
        if (caseNodes == null) {
            return;
        }

        for (final ChoiceCaseNode caseNode : caseNodes) {
            if (caseNode != null && !caseNode.isAddedByUses() && !caseNode.isAugmenting()) {
                final String packageName = packageNameForGeneratedType(basePackageName, caseNode.getPath());
                final GeneratedTypeBuilder caseTypeBuilder = addDefaultInterfaceDefinition(packageName, caseNode, module);
                caseTypeBuilder.addImplementsType(refChoiceType);
                BindingTextUtils.annotateDeprecatedIfNecessary(caseNode.getStatus(), caseTypeBuilder);
                genCtx.get(module).addCaseType(caseNode.getPath(), caseTypeBuilder);
                genCtx.get(module).addChoiceToCaseMapping(refChoiceType, caseTypeBuilder, caseNode);
                final Iterable<DataSchemaNode> caseChildNodes = caseNode.getChildNodes();
                if (caseChildNodes != null) {
                    final SchemaPath choiceNodeParentPath = choiceNode.getPath().getParent();

                    if (!Iterables.isEmpty(choiceNodeParentPath.getPathFromRoot())) {
                        SchemaNode parent = findDataSchemaNode(schemaContext, choiceNodeParentPath);

                        if (parent instanceof AugmentationSchema) {
                            final AugmentationSchema augSchema = (AugmentationSchema) parent;
                            final SchemaPath targetPath = augSchema.getTargetPath();
                            SchemaNode targetSchemaNode = findDataSchemaNode(schemaContext, targetPath);
                            if (targetSchemaNode instanceof DataSchemaNode
                                    && ((DataSchemaNode) targetSchemaNode).isAddedByUses()) {
                                if (targetSchemaNode instanceof DerivableSchemaNode) {
                                    targetSchemaNode = ((DerivableSchemaNode) targetSchemaNode).getOriginal().orNull();
                                }
                                if (targetSchemaNode == null) {
                                    throw new IllegalStateException(
                                            "Failed to find target node from grouping for augmentation " + augSchema
                                                    + " in module " + module.getName());
                                }
                            }
                            parent = targetSchemaNode;
                        }

                        Preconditions.checkState(parent != null, "Could not find Choice node parent %s",
                                choiceNodeParentPath);
                        GeneratedTypeBuilder childOfType = BindingResolverUtils.findChildNodeByPath(parent.getPath(), genCtx.values());
                        if (childOfType == null) {
                            childOfType = BindingResolverUtils.findGroupingByPath(parent.getPath(), genCtx.values());
                        }
                        resolveDataSchemaNodes(module, basePackageName, caseTypeBuilder, childOfType, caseChildNodes);
                    } else {
                        resolveDataSchemaNodes(module, basePackageName, caseTypeBuilder, moduleToDataType(module),
                                caseChildNodes);
                    }
               }
            }
            processUsesAugments(caseNode, module);
        }
    }

    private boolean resolveLeafSchemaNodeAsProperty(final GeneratedTOBuilder toBuilder, final LeafSchemaNode leaf,
            final boolean isReadOnly, final Module module) {
        if ((leaf != null) && (toBuilder != null)) {
            String leafDesc = leaf.getDescription();
            if (leafDesc == null) {
                leafDesc = "";
            }
            Type returnType = null;
            final TypeDefinition<?> typeDef = CompatUtils.compatLeafType(leaf);
            if (typeDef instanceof UnionTypeDefinition) {
                // GeneratedType for this type definition should be already
                // created
                final QName qname = typeDef.getQName();
                final Module unionModule = schemaContext.findModuleByNamespaceAndRevision(qname.getNamespace(),
                        qname.getRevision());
                final ModuleContext mc = genCtx.get(unionModule);
                returnType = mc.getTypedefs().get(typeDef.getPath());
            } else if (typeDef instanceof EnumTypeDefinition && typeDef.getBaseType() == null) {
                // Annonymous enumeration (already generated, since it is inherited via uses).
                LeafSchemaNode originalLeaf = (LeafSchemaNode) SchemaNodeUtils.getRootOriginalIfPossible(leaf);
                QName qname = originalLeaf.getQName();
                final Module enumModule =  schemaContext.findModuleByNamespaceAndRevision(qname.getNamespace(),
                        qname.getRevision());
                returnType = genCtx.get(enumModule).getInnerType(originalLeaf.getType().getPath());
            } else {
                returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, leaf);
            }
            return resolveLeafSchemaNodeAsProperty(toBuilder, leaf, returnType, isReadOnly);
        }
        return false;
    }

    /**
     * Converts <code>leaf</code> schema node to property of generated TO
     * builder.
     *
     * @param toBuilder
     *            generated TO builder to which is <code>leaf</code> added as
     *            property
     * @param leaf
     *            leaf schema node which is added to <code>toBuilder</code> as
     *            property
     * @param returnType
     *            property type
     * @param isReadOnly
     *            boolean value which says if leaf property is|isn't read only
     * @return boolean value
     *         <ul>
     *         <li>false - if <code>leaf</code>, <code>toBuilder</code> or leaf
     *         name equals null or if leaf is added by <i>uses</i>.</li>
     *         <li>true - other cases</li>
     *         </ul>
     */
    private static boolean resolveLeafSchemaNodeAsProperty(final GeneratedTOBuilder toBuilder, final LeafSchemaNode leaf,
            final Type returnType, final boolean isReadOnly) {
        if (returnType == null) {
            return false;
        }
        final String leafName = leaf.getQName().getLocalName();
        final String leafDesc = encodeAngleBrackets(leaf.getDescription());
        final GeneratedPropertyBuilder propBuilder = toBuilder.addProperty(BindingMapping.getPropertyName(leafName));
        propBuilder.setReadOnly(isReadOnly);
        propBuilder.setReturnType(returnType);
        propBuilder.setComment(leafDesc);
        toBuilder.addEqualsIdentity(propBuilder);
        toBuilder.addHashIdentity(propBuilder);
        toBuilder.addToStringProperty(propBuilder);
        return true;
    }

    private GeneratedTypeBuilder addDefaultInterfaceDefinition(final String packageName, final SchemaNode schemaNode,
            final Module module) {
        return addDefaultInterfaceDefinition(packageName, schemaNode, null, module);
    }

    /**
     * Instantiates generated type builder with <code>packageName</code> and
     * <code>schemaNode</code>.
     *
     * The new builder always implements
     * {@link org.opendaylight.yangtools.yang.binding.DataObject DataObject}.<br>
     * If <code>schemaNode</code> is instance of GroupingDefinition it also
     * implements {@link org.opendaylight.yangtools.yang.binding.Augmentable
     * Augmentable}.<br>
     * If <code>schemaNode</code> is instance of
     * {@link org.opendaylight.yangtools.yang.model.api.DataNodeContainer
     * DataNodeContainer} it can also implement nodes which are specified in
     * <i>uses</i>.
     *
     * @param packageName
     *            string with the name of the package to which
     *            <code>schemaNode</code> belongs.
     * @param schemaNode
     *            schema node for which is created generated type builder
     * @param parent
     *            parent type (can be null)
     * @return generated type builder <code>schemaNode</code>
     */
    private GeneratedTypeBuilder addDefaultInterfaceDefinition(final String packageName, final SchemaNode schemaNode,
            final Type parent, final Module module) {
        final GeneratedTypeBuilder it = addRawInterfaceDefinition(packageName, schemaNode, "");
        if (parent == null) {
            it.addImplementsType(DATA_OBJECT);
        } else {
            it.addImplementsType(BindingTypes.childOf(parent));
        }
        if (!(schemaNode instanceof GroupingDefinition)) {
            it.addImplementsType(augmentable(it));
        }

        if (schemaNode instanceof DataNodeContainer) {
            groupingsToGenTypes(module, ((DataNodeContainer) schemaNode).getGroupings());
            addImplementedInterfaceFromUses((DataNodeContainer) schemaNode, it);
        }

        return it;
    }

    /**
     * Wraps the calling of the same overloaded method.
     *
     * @param packageName
     *            string with the package name to which returning generated type
     *            builder belongs
     * @param schemaNode
     *            schema node which provide data about the schema node name
     * @return generated type builder for <code>schemaNode</code>
     */
    private GeneratedTypeBuilder addRawInterfaceDefinition(final String packageName, final SchemaNode schemaNode) {
        return addRawInterfaceDefinition(packageName, schemaNode, "");
    }

    /**
     * Returns reference to generated type builder for specified
     * <code>schemaNode</code> with <code>packageName</code>.
     *
     * Firstly the generated type builder is searched in
     * {@link BindingGeneratorImpl#genTypeBuilders genTypeBuilders}. If it isn't
     * found it is created and added to <code>genTypeBuilders</code>.
     *
     * @param packageName
     *            string with the package name to which returning generated type
     *            builder belongs
     * @param schemaNode
     *            schema node which provide data about the schema node name
     * @param prefix
     *            return type name prefix
     * @return generated type builder for <code>schemaNode</code>
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>schemaNode</code> is null</li>
     *             <li>if <code>packageName</code> is null</li>
     *             <li>if QName of schema node is null</li>
     *             <li>if schemaNode name is null</li>
     *             </ul>
     *
     */
    private GeneratedTypeBuilder addRawInterfaceDefinition(final String packageName, final SchemaNode schemaNode,
            final String prefix) {
        checkArgument(schemaNode != null, "Data Schema Node cannot be NULL.");
        checkArgument(packageName != null, "Package Name for Generated Type cannot be NULL.");
        checkArgument(schemaNode.getQName() != null, "QName for Data Schema Node cannot be NULL.");
        final String schemaNodeName = schemaNode.getQName().getLocalName();
        checkArgument(schemaNodeName != null, "Local Name of QName for Data Schema Node cannot be NULL.");

        String genTypeName;
        if (prefix == null) {
            genTypeName = BindingMapping.getClassName(schemaNodeName);
        } else {
            genTypeName = prefix + BindingMapping.getClassName(schemaNodeName);
        }

        // FIXME: Validation of name conflict
        final GeneratedTypeBuilderImpl newType = new GeneratedTypeBuilderImpl(packageName, genTypeName);
        final Module module = findParentModule(schemaContext, schemaNode);
        BindingTextUtils.qnameConstant(newType, BindingMapping.QNAME_STATIC_FIELD_NAME, schemaNode.getQName());
        newType.addComment(schemaNode.getDescription());
        final String parentName = BindingTextUtils.getParentName(schemaContext, schemaNode);
        newType.setDescription(BindingTextUtils.createDescription(schemaNode, newType.getFullyQualifiedName(),
                verboseClassComments, parentName));
        newType.setReference(schemaNode.getReference());
        newType.setSchemaPath(schemaNode.getPath().getPathFromRoot());
        newType.setModuleName(module.getName());

        if (!genTypeBuilders.containsKey(packageName)) {
            final Map<String, GeneratedTypeBuilder> builders = new HashMap<>();
            builders.put(genTypeName, newType);
            genTypeBuilders.put(packageName, builders);
        } else {
            final Map<String, GeneratedTypeBuilder> builders = genTypeBuilders.get(packageName);
            if (!builders.containsKey(genTypeName)) {
                builders.put(genTypeName, newType);
            }
        }
        return newType;
    }

    /**
     * Adds <code>schemaNode</code> to <code>typeBuilder</code> as getter method
     * or to <code>genTOBuilder</code> as property.
     *
     * @param basePackageName
     *            string contains the module package name
     * @param schemaNode
     *            data schema node which should be added as getter method to
     *            <code>typeBuilder</code> or as a property to
     *            <code>genTOBuilder</code> if is part of the list key
     * @param typeBuilder
     *            generated type builder for the list schema node
     * @param genTOBuilder
     *            generated TO builder for the list keys
     * @param listKeys
     *            list of string which contains names of the list keys
     * @param module
     *            current module
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>schemaNode</code> equals null</li>
     *             <li>if <code>typeBuilder</code> equals null</li>
     *             </ul>
     */
    private void addSchemaNodeToListBuilders(final String basePackageName, final DataSchemaNode schemaNode,
            final GeneratedTypeBuilder typeBuilder, final GeneratedTOBuilder genTOBuilder, final List<String> listKeys,
            final Module module) {
        checkArgument(schemaNode != null, "Data Schema Node cannot be NULL.");
        checkArgument(typeBuilder != null, "Generated Type Builder cannot be NULL.");

        if (schemaNode instanceof LeafSchemaNode) {
            final LeafSchemaNode leaf = (LeafSchemaNode) schemaNode;
            final String leafName = leaf.getQName().getLocalName();
            final Type type = leafResolver.resolveLeafSchemaNodeAsMethod(typeBuilder, leaf,module);
            if (listKeys.contains(leafName)) {
                if (type == null) {
                    resolveLeafSchemaNodeAsProperty(genTOBuilder, leaf, true, module);
                } else {
                    resolveLeafSchemaNodeAsProperty(genTOBuilder, leaf, type, true);
                }
            }
        } else if (!schemaNode.isAddedByUses()) {
            if (schemaNode instanceof LeafListSchemaNode) {
                leafListResolver.resolveLeafListSchemaNode(typeBuilder, (LeafListSchemaNode) schemaNode, module);
            } else if (schemaNode instanceof ContainerSchemaNode) {
                containerToGenType(module, basePackageName, typeBuilder, typeBuilder, (ContainerSchemaNode) schemaNode);
            } else if (schemaNode instanceof ChoiceSchemaNode) {
                choiceToGeneratedType(module, basePackageName, typeBuilder, (ChoiceSchemaNode) schemaNode);
            } else if (schemaNode instanceof ListSchemaNode) {
                listToGenType(module, basePackageName, typeBuilder, typeBuilder, (ListSchemaNode)
                        schemaNode);
            }
        }
    }

    private void listToGenType(final Module module, final String basePackageName, final GeneratedTypeBuilder parent,
                               final GeneratedTypeBuilder childOf, final ListSchemaNode node) {
        final GeneratedTypeBuilder genType = processDataSchemaNode(module, basePackageName, childOf, node);
        if (genType != null) {
            BindingTextUtils.constructGetter(parent, node.getQName().getLocalName(), node.getDescription(),
                    Types.listTypeFor(genType), node.getStatus());

            final List<String> listKeys = listKeys(node);
            final String packageName = packageNameForGeneratedType(basePackageName, node.getPath());
            final GeneratedTOBuilder genTOBuilder = resolveListKeyTOBuilder(packageName, node);
            if (genTOBuilder != null) {
                final Type identifierMarker = Types.parameterizedTypeFor(IDENTIFIER, genType);
                final Type identifiableMarker = Types.parameterizedTypeFor(IDENTIFIABLE, genTOBuilder);
                genTOBuilder.addImplementsType(identifierMarker);
                genType.addImplementsType(identifiableMarker);
            }

            for (final DataSchemaNode schemaNode : node.getChildNodes()) {
                if (!schemaNode.isAugmenting()) {
                    addSchemaNodeToListBuilders(basePackageName, schemaNode, genType, genTOBuilder, listKeys, module);
                }
            }

            // serialVersionUID
            if (genTOBuilder != null) {
                final GeneratedPropertyBuilder prop = new GeneratedPropertyBuilderImpl("serialVersionUID");
                prop.setValue(Long.toString(computeDefaultSUID(genTOBuilder)));
                genTOBuilder.setSUID(prop);
            }

            typeBuildersToGenTypes(module, genType, genTOBuilder);
        }
    }

    /**
     * Generates for the <code>list</code> which contains any list keys special
     * generated TO builder.
     *
     * @param packageName
     *            string with package name to which the list belongs
     * @param list
     *            list schema node which is source of data about the list name
     * @return generated TO builder which represents the keys of the
     *         <code>list</code> or null if <code>list</code> is null or list of
     *         key definitions is null or empty.
     */
    private GeneratedTOBuilder resolveListKeyTOBuilder(final String packageName, final ListSchemaNode list) {
        GeneratedTOBuilder genTOBuilder = null;
        if ((list.getKeyDefinition() != null) && (!list.getKeyDefinition().isEmpty())) {
            final String listName = list.getQName().getLocalName() + "Key";
            final String genTOName = BindingMapping.getClassName(listName);
            genTOBuilder = new GeneratedTOBuilderImpl(packageName, genTOName);
        }
        return genTOBuilder;
    }

    private void typeBuildersToGenTypes(final Module module, final GeneratedTypeBuilder typeBuilder,
                                        final GeneratedTOBuilder genTOBuilder) {
        checkArgument(typeBuilder != null, "Generated Type Builder cannot be NULL.");

        if (genTOBuilder != null) {
            final GeneratedTransferObject genTO = genTOBuilder.toInstance();
            BindingTextUtils.constructGetter(typeBuilder, "key", "Returns Primary Key of Yang List Type", genTO,
                    Status.CURRENT);
            genCtx.get(module).addGeneratedTOBuilder(genTOBuilder);
        }
    }

    /**
     * Selects the names of the list keys from <code>list</code> and returns
     * them as the list of the strings
     *
     * @param list
     *            of string with names of the list keys
     * @return list of string which represents names of the list keys. If the
     *         <code>list</code> contains no keys then the empty list is
     *         returned.
     */
    private List<String> listKeys(final ListSchemaNode list) {
        final List<String> listKeys = new ArrayList<>();

        final List<QName> keyDefinition = list.getKeyDefinition();
        if (keyDefinition != null) {
            for (final QName keyDef : keyDefinition) {
                listKeys.add(keyDef.getLocalName());
            }
        }
        return listKeys;
    }

    /**
     * Adds the implemented types to type builder.
     *
     * The method passes through the list of <i>uses</i> in
     * {@code dataNodeContainer}. For every <i>use</i> is obtained corresponding
     * generated type from {@link BindingGeneratorImpl#allGroupings
     * allGroupings} which is added as <i>implements type</i> to
     * <code>builder</code>
     *
     * @param dataNodeContainer
     *            element which contains the list of used YANG groupings
     * @param builder
     *            builder to which are added implemented types according to
     *            <code>dataNodeContainer</code>
     * @return generated type builder with all implemented types
     */
    private GeneratedTypeBuilder addImplementedInterfaceFromUses(final DataNodeContainer dataNodeContainer,
            final GeneratedTypeBuilder builder) {
        for (final UsesNode usesNode : dataNodeContainer.getUses()) {
            if (usesNode.getGroupingPath() != null) {
                final GeneratedType genType = BindingResolverUtils.findGroupingByPath(usesNode.getGroupingPath(),
                        genCtx.values()).toInstance();
                if (genType == null) {
                    throw new IllegalStateException("Grouping " + usesNode.getGroupingPath() + "is not resolved for "
                            + builder.getName());
                }

                builder.addImplementsType(genType);
                /*
                builder.addComment(genType.getDescription());
                builder.setDescription(genType.getDescription());
                builder.setModuleName(genType.getModuleName());
                builder.setReference(genType.getReference());
                builder.setSchemaPath(genType.getSchemaPath());
                */
            }
        }
        return builder;
    }

    public Map<Module, ModuleContext> getModuleContexts() {
        return genCtx;
    }
}