/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.addTOToTypeBuilder;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.annotateDeprecatedIfNecessary;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.constructGetter;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.createDescription;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.createReturnTypeForUnion;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.getAugmentIdentifier;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.isInnerType;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.qNameConstant;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.resolveInnerEnumFromTypeDefinition;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.resolveListKeyTOBuilder;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.resolveListKeyTypeBuilder;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil.computeDefaultSUID;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil.encodeAngleBrackets;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil.packageNameForGeneratedType;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingTypes.NOTIFICATION;
import static org.opendaylight.mdsal.binding.javav2.generator.util.Types.parameterizedTypeFor;
import static org.opendaylight.mdsal.binding.javav2.generator.util.Types.wildcardTypeFor;
import static org.opendaylight.yangtools.yang.model.util.SchemaContextUtil.findDataSchemaNode;
import static org.opendaylight.yangtools.yang.model.util.SchemaContextUtil.findParentModule;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.mdsal.binding.javav2.generator.context.ModuleContext;
import java.util.stream.Collectors;
import org.opendaylight.mdsal.binding.javav2.generator.spi.TypeProvider;
import org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil;
import org.opendaylight.mdsal.binding.javav2.generator.util.BindingTypes;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifier;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifierNormalizer;
import org.opendaylight.mdsal.binding.javav2.generator.util.ReferencedTypeImpl;
import org.opendaylight.mdsal.binding.javav2.generator.util.Types;
import org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder.GeneratedPropertyBuilderImpl;
import org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder.GeneratedTOBuilderImpl;
import org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder.GeneratedTypeBuilderImpl;
import org.opendaylight.mdsal.binding.javav2.generator.yang.types.GroupingDefinitionDependencySort;
import org.opendaylight.mdsal.binding.javav2.generator.yang.types.TypeProviderImpl;
import org.opendaylight.mdsal.binding.javav2.model.api.AccessModifier;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.javav2.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.javav2.model.api.Restrictions;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.EnumBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.mdsal.binding.javav2.spec.base.BaseIdentity;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingNamespaceType;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentable;
import org.opendaylight.mdsal.binding.javav2.util.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.meta.ModelStatement;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.model.util.SchemaNodeUtils;

/**
 * Helper util class used for generation of types in Binding spec v2.
 */
@Beta
final class GenHelperUtil {

    private GenHelperUtil() {
        throw new UnsupportedOperationException("Util class");
    }

    /**
     * Create GeneratedTypeBuilder object from module argument.
     *
     * @param module
     *            Module object from which builder will be created
     * @param genCtx generated context
     * @param verboseClassComments verbosity switch
     *
     * @return <code>GeneratedTypeBuilder</code> which is internal
     *         representation of the module
     * @throws IllegalArgumentException
     *             if module is null
     */
    static GeneratedTypeBuilder moduleToDataType(final Module module, final Map<Module, ModuleContext> genCtx, final boolean verboseClassComments) {
        Preconditions.checkArgument(module != null, "Module reference cannot be NULL.");

        final GeneratedTypeBuilder moduleDataTypeBuilder = moduleTypeBuilder(module, "Data", verboseClassComments,
                genCtx.get(module));
        addImplementedInterfaceFromUses(module, moduleDataTypeBuilder, genCtx);
        moduleDataTypeBuilder.addImplementsType(BindingTypes.TREE_ROOT);
        moduleDataTypeBuilder.addComment(module.getDescription());
        moduleDataTypeBuilder.setDescription(createDescription(module, verboseClassComments));
        moduleDataTypeBuilder.setReference(module.getReference());
        return moduleDataTypeBuilder;
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
     * @param verboseClassComments verbosity switch
     * @return instance of GeneratedTypeBuilder which represents
     *         <code>module</code>.
     * @throws IllegalArgumentException
     *             if <code>module</code> is null
     */
    static GeneratedTypeBuilder moduleTypeBuilder(final Module module, final String postfix, final boolean
            verboseClassComments, ModuleContext context) {
        Preconditions.checkArgument(module != null, "Module reference cannot be NULL.");
        final String packageName = BindingMapping.getRootPackageName(module);
        // underscore used as separator for distinction of module name parts
        final String moduleName = new StringBuilder(module.getName()).append('_').append(postfix).toString();

        final GeneratedTypeBuilderImpl moduleBuilder = new GeneratedTypeBuilderImpl(packageName, moduleName, context);
        moduleBuilder.setDescription(createDescription(module, verboseClassComments));
        moduleBuilder.setReference(module.getReference());
        moduleBuilder.setModuleName(moduleName);
        return moduleBuilder;
    }

    /**
     * Adds the implemented types to type builder.
     *
     * The method passes through the list of <i>uses</i> in
     * {@code dataNodeContainer}. For every <i>use</i> is obtained corresponding
     * generated type from all groupings
     * allGroupings} which is added as <i>implements type</i> to
     * <code>builder</code>
     *
     * @param dataNodeContainer
     *            element which contains the list of used YANG groupings
     * @param builder
     *            builder to which are added implemented types according to
     *            <code>dataNodeContainer</code>
     * @param genCtx generated context
     * @return generated type builder with all implemented types
     */
    static GeneratedTypeBuilder addImplementedInterfaceFromUses(final DataNodeContainer dataNodeContainer,
                          final GeneratedTypeBuilder builder, final Map<Module, ModuleContext> genCtx) {
        for (final UsesNode usesNode : dataNodeContainer.getUses()) {
            final GeneratedType genType = findGroupingByPath(usesNode.getGroupingPath(), genCtx).toInstance();
            if (genType == null) {
                throw new IllegalStateException("Grouping " + usesNode.getGroupingPath() + "is not resolved for "
                    + builder.getName());
            }
            builder.addImplementsType(genType);
        }
        return builder;
    }

     static GeneratedTypeBuilder findGroupingByPath(final SchemaPath path, final Map<Module, ModuleContext> genCtx) {
        for (final ModuleContext ctx : genCtx.values()) {
            final GeneratedTypeBuilder result = ctx.getGrouping(path);
            if (result != null) {
                return result;
            }
        }
        return null;
     }

    static GeneratedTOBuilder findIdentityByQname(final QName qname, final Map<Module, ModuleContext> genCtx) {
        for (final ModuleContext ctx : genCtx.values()) {
            final GeneratedTOBuilder result = ctx.getIdentities().get(qname);
            if (result != null) {
                return result;
            }
        }
        return null;
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
    static GeneratedTypeBuilder resolveDataSchemaNodes(final Module module, final String basePackageName,
                          final GeneratedTypeBuilder parent, final GeneratedTypeBuilder childOf,
                          final Iterable<DataSchemaNode> schemaNodes, final Map<Module, ModuleContext> genCtx,
                          final SchemaContext schemaContext, final boolean verboseClassComments,
                          final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders,
                          final TypeProvider typeProvider, final BindingNamespaceType namespaceType) {

        if (schemaNodes != null && parent != null) {
            for (final DataSchemaNode schemaNode : schemaNodes) {
                if (resolveDataSchemaNodesCheck(module, schemaContext, schemaNode)) {
                    addSchemaNodeToBuilderAsMethod(basePackageName, schemaNode, parent, childOf, module, genCtx,
                            schemaContext, verboseClassComments, genTypeBuilders, typeProvider, namespaceType);
                }
            }
        }
        return parent;
    }

    static boolean resolveDataSchemaNodesCheck(final Module module, final SchemaContext schemaContext,
            final DataSchemaNode schemaNode) {
        if (!schemaNode.isAugmenting()) {
            return true;
        } else if (schemaNode.isAugmenting()) {
            QName qname = schemaNode.getPath().getLastComponent();
            final Module originalModule = schemaContext.findModuleByNamespaceAndRevision(qname.getNamespace(),
                    qname.getRevision());
            if (module.equals(originalModule)) {
                return true;
            }
        }

        return false;
    }

    static GeneratedTypeBuilder addDefaultInterfaceDefinition(final String basePackageName, final SchemaNode
            schemaNode, final Module module, final Map<Module, ModuleContext> genCtx, final SchemaContext schemaContext,
            final boolean verboseClassComments, final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders,
            final TypeProvider typeProvider, final BindingNamespaceType namespaceType) {
        return addDefaultInterfaceDefinition(basePackageName, schemaNode, null, module, genCtx, schemaContext,
                verboseClassComments, genTypeBuilders, typeProvider , namespaceType);
    }

    private static QName createQNameFromSuperNode(final Module module, final Object node, final SchemaNode superChildNode) {
        QName childNodeQName = null;
        if (node instanceof Module) {
            childNodeQName = QName.create(((Module) node).getNamespace(), ((Module) node).getRevision(),
                    superChildNode.getQName().getLocalName());
        } else if (node instanceof SchemaNode) {
            childNodeQName = QName.create(((SchemaNode) node).getQName(), superChildNode.getQName().getLocalName());
        } else if (node instanceof AugmentationSchema) {
            childNodeQName = QName.create(module.getNamespace(), module.getRevision(), superChildNode.getQName().getLocalName());
        } else {
            throw new IllegalArgumentException("Not support node type:" + node.toString());
        }

        return childNodeQName;
    }

    private static void addUsesImplements(final SchemaNode superNode, final Module superModule,
            final Object node, final Module module, final SchemaContext schemaContext,
            Map<Module, ModuleContext> genCtx, final BindingNamespaceType namespaceType) {
        if (superNode instanceof DataNodeContainer) {
            for (DataSchemaNode superChildNode : ((DataNodeContainer) superNode).getChildNodes()) {
                if (superChildNode instanceof DataNodeContainer || superChildNode instanceof ChoiceSchemaNode) {
                    final QName childQName = createQNameFromSuperNode(module, node, superChildNode);
                    DataSchemaNode childNode = ((DataNodeContainer) node).getDataChildByName(childQName);
                    Preconditions.checkNotNull(childNode, node.toString() + "->" + childQName.toString());

                    final GeneratedTypeBuilder type = genCtx.get(module).getChildNode(childNode.getPath());
                    final GeneratedTypeBuilder superType = genCtx.get(superModule).getChildNode(superChildNode.getPath());

                    Preconditions.checkNotNull(type, module.toString() + "->" + childNode.getPath().toString());
                    Preconditions.checkNotNull(superType, superModule.toString() + "->" + superChildNode.getPath().toString());
                    type.addImplementsType(superType);
                    if (superChildNode instanceof ListSchemaNode
                            && !((ListSchemaNode) superChildNode).getKeyDefinition().isEmpty()) {
                        if (namespaceType.equals(BindingNamespaceType.Grouping)) {
                            genCtx.get(module).getKeyType(childNode.getPath())
                                    .addImplementsType(genCtx.get(superModule).getKeyType(superChildNode.getPath()));
                        } else if (namespaceType.equals(BindingNamespaceType.Data)) {
                            genCtx.get(module).getKeyGenTO(childNode.getPath())
                                    .addImplementsType(genCtx.get(superModule).getKeyType(superChildNode.getPath()));
                        }
                    }
                    addUsesImplements(superChildNode, superModule, childNode, module, schemaContext, genCtx, namespaceType);
                }
            }
        } else if (superNode instanceof ChoiceSchemaNode) {
            for (ChoiceCaseNode superCaseNode : ((ChoiceSchemaNode) superNode).getCases()) {
                final QName childQName = createQNameFromSuperNode(module, node, superCaseNode);
                ChoiceCaseNode caseNode = ((ChoiceSchemaNode) node).getCaseNodeByName(childQName);
                Preconditions.checkNotNull(caseNode, node.toString() + "->" + childQName.toString());

                final GeneratedTypeBuilder type = genCtx.get(module).getCase(caseNode.getPath());
                final GeneratedTypeBuilder superType = genCtx.get(superModule).getCase(superCaseNode.getPath());
                Preconditions.checkNotNull(type, module.toString() + "->" + caseNode.getPath().toString());
                Preconditions.checkNotNull(superType, superModule.toString() + "->" + superCaseNode.getPath().toString());
                type.addImplementsType(superType);
                addUsesImplements(superCaseNode, superModule, caseNode, module, schemaContext, genCtx, namespaceType);
            }
        } else {
            throw new IllegalArgumentException("Not support super node :" + superNode.toString());
        }
    }

    private static GroupingDefinition findGroupingNodeFromUses(final Module module, final SchemaContext schemaContext,
            final Object parentNode, final UsesNode usesNode) {
        SchemaNode groupingNode;
        if (parentNode instanceof Module) {
            final Module superModule = schemaContext.findModuleByNamespaceAndRevision(
                    usesNode.getGroupingPath().getLastComponent().getModule().getNamespace(),
                    usesNode.getGroupingPath().getLastComponent().getModule().getRevision());
            groupingNode = superModule.getGroupings()
                    .stream().filter(grouping -> grouping.getPath().equals(usesNode.getGroupingPath()))
                    .findFirst().orElse(null);
        } else {
            //FIXME: Schema path is not unique for Yang 1.1, findDataSchemaNode always does search from data node first.
            final Iterable<QName> prefixedPath = usesNode.getGroupingPath().getPathFromRoot();
            final QName current = prefixedPath.iterator().next();
            final Module targetModule = schemaContext.findModuleByNamespaceAndRevision(current.getNamespace(), current.getRevision());
            Preconditions.checkNotNull(targetModule, "Target module can not be null.");
            groupingNode = targetModule.getGroupings().stream().filter(grouping -> grouping.getPath().equals(usesNode.getGroupingPath()))
                    .collect(Collectors.toList()).get(0);
            if (groupingNode == null) {
                groupingNode = SchemaContextUtil.findDataSchemaNode(schemaContext, usesNode.getGroupingPath());
            }
        }
        Preconditions.checkNotNull(groupingNode, module.toString() + "->"
                + usesNode.getGroupingPath().toString());
        Preconditions.checkState(groupingNode instanceof GroupingDefinition,
                module.toString() + "->" + usesNode.getGroupingPath().toString());
        return (GroupingDefinition) groupingNode;
    }

    static Map<Module, ModuleContext> processUsesImplements(final Object node, final Module module,
            final SchemaContext schemaContext, Map<Module, ModuleContext> genCtx, final BindingNamespaceType namespaceType) {
        if (node instanceof DataNodeContainer) {
            for (final UsesNode usesNode : ((DataNodeContainer) node).getUses()) {
                final GroupingDefinition grouping = findGroupingNodeFromUses(module, schemaContext, node, usesNode);
                final Module superModule = SchemaContextUtil.findParentModule(schemaContext, grouping);
                addUsesImplements(grouping, superModule, node, module, schemaContext, genCtx, namespaceType);
            }
        }
        return genCtx;
    }

    static GeneratedTypeBuilder findChildNodeByPath(final SchemaPath path, final Map<Module, ModuleContext> genCtx) {
        for (final ModuleContext ctx : genCtx.values()) {
            final GeneratedTypeBuilder result = ctx.getChildNode(path);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    static GeneratedTypeBuilder findCaseByPath(final SchemaPath path, final Map<Module, ModuleContext> genCtx) {
        for (final ModuleContext ctx : genCtx.values()) {
            final GeneratedTypeBuilder result = ctx.getCase(path);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    static Map<Module, ModuleContext> addRawAugmentGenTypeDefinition(final Module module, final String augmentPackageName,
            final Type targetTypeRef, final SchemaNode targetNode, final List<AugmentationSchema> schemaPathAugmentListEntry,
            final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders, final Map<Module, ModuleContext> genCtx,
            final SchemaContext schemaContext, final boolean verboseClassComments, final TypeProvider typeProvider,
            final BindingNamespaceType namespaceType) {

        //pick augmentation grouped by augmentation target, there is always at least one
        final AugmentationSchema augSchema = schemaPathAugmentListEntry.get(0);

        Map<String, GeneratedTypeBuilder> augmentBuilders = genTypeBuilders.computeIfAbsent(
                augmentPackageName, k -> new HashMap<>());

        //this requires valid semantics in YANG model
        String augIdentifier = null;
        for (AugmentationSchema aug : schemaPathAugmentListEntry) {
            augIdentifier = getAugmentIdentifier(aug.getUnknownSchemaNodes());
            break;
        }

        if (augIdentifier == null) {
            augIdentifier = new StringBuilder(module.getName())
                    .append('_').append(targetNode.getQName().getLocalName()).toString();
        }

        GeneratedTypeBuilderImpl augTypeBuilder = new GeneratedTypeBuilderImpl(augmentPackageName, augIdentifier,
                true, false, genCtx.get(module));

        augTypeBuilder.addImplementsType(BindingTypes.TREE_NODE);
        augTypeBuilder.addImplementsType(parameterizedTypeFor(BindingTypes.INSTANTIABLE, augTypeBuilder));
        augTypeBuilder.addImplementsType(Types.augmentationTypeFor(targetTypeRef));
        augTypeBuilder.setBasePackageName(BindingMapping.getRootPackageName(module));
        augTypeBuilder.setWithBuilder(true);
        annotateDeprecatedIfNecessary(augSchema.getStatus(), augTypeBuilder);

        //produces getters for augTypeBuilder eventually
        for (AugmentationSchema aug : schemaPathAugmentListEntry) {
            //apply all uses
            addImplementedInterfaceFromUses(aug, augTypeBuilder, genCtx);
            augSchemaNodeToMethods(module, BindingMapping.getRootPackageName(module), augTypeBuilder, augTypeBuilder, aug.getChildNodes(),
               genCtx, schemaContext, verboseClassComments, typeProvider, genTypeBuilders, namespaceType);
        }

        augmentBuilders.put(augTypeBuilder.getName(), augTypeBuilder);

        if(!augSchema.getChildNodes().isEmpty()) {
            genCtx.get(module).addTypeToAugmentation(augTypeBuilder, augSchema);
            genCtx.get(module).addTargetToAugmentation(augTypeBuilder, augSchema.getTargetPath());
        }
        genCtx.get(module).addAugmentType(augTypeBuilder);
        return genCtx;
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
    private static GeneratedTypeBuilder augSchemaNodeToMethods(final Module module, final String basePackageName,
            final GeneratedTypeBuilder typeBuilder, final GeneratedTypeBuilder childOf, final Iterable<DataSchemaNode> schemaNodes,
            final Map<Module, ModuleContext> genCtx, final SchemaContext schemaContext, final boolean
            verboseClassComments, final TypeProvider typeProvider, final Map<String, Map<String,
            GeneratedTypeBuilder>> genTypeBuilders, final BindingNamespaceType namespaceType) {
        if (schemaNodes != null && typeBuilder != null) {
            for (final DataSchemaNode schemaNode : schemaNodes) {
                if (!schemaNode.isAugmenting()) {
                    addSchemaNodeToBuilderAsMethod(basePackageName, schemaNode, typeBuilder, childOf, module, genCtx,
                            schemaContext, verboseClassComments, genTypeBuilders, typeProvider, namespaceType);
                }
            }
        }
        return typeBuilder;
    }

    /**
     * Instantiates generated type builder with <code>packageName</code> and
     * <code>schemaNode</code>.
     *
     * The new builder always implements
     * {@link TreeNode TreeNode}.<br>
     * If <code>schemaNode</code> is instance of GroupingDefinition it also
     * implements {@link Augmentable
     * Augmentable}.<br>
     * If <code>schemaNode</code> is instance of
     * {@link org.opendaylight.yangtools.yang.model.api.DataNodeContainer
     * DataNodeContainer} it can also implement nodes which are specified in
     * <i>uses</i>.
     *
     * @param basePackageName
     *            string contains the module package name
     * @param schemaNode
     *            schema node for which is created generated type builder
     * @param parent
     *            parent type (can be null)
     * @param schemaContext schema context
     * @return generated type builder <code>schemaNode</code>
     */
    private static GeneratedTypeBuilder addDefaultInterfaceDefinition(final String basePackageName, final SchemaNode
            schemaNode, final Type parent, final Module module, final Map<Module, ModuleContext> genCtx,
            final SchemaContext schemaContext, final boolean verboseClassComments, final Map<String, Map<String,
            GeneratedTypeBuilder>> genTypeBuilders, final TypeProvider typeProvider, final BindingNamespaceType namespaceType) {

        String suffix = "";
        if (schemaNode instanceof GroupingDefinition) {
            suffix = "grouping";
        } else if (namespaceType.equals(BindingNamespaceType.Grouping)) {
            suffix = "data";
        }

        GeneratedTypeBuilder it = addRawInterfaceDefinition(basePackageName, schemaNode, schemaContext, "", suffix,
                verboseClassComments, genTypeBuilders, namespaceType, genCtx.get(module));
        if (namespaceType.equals(BindingNamespaceType.Data)) {
            if (parent == null) {
                it.addImplementsType(BindingTypes.TREE_NODE);
            } else {
                if (parent instanceof ListSchemaNode) {
                    it.addImplementsType(parameterizedTypeFor(BindingTypes.TREE_CHILD_NODE, parent, parameterizedTypeFor
                            (BindingTypes.IDENTIFIABLE_ITEM, parent)));
                } else {
                    it.addImplementsType(parameterizedTypeFor(BindingTypes.TREE_CHILD_NODE, parent, parameterizedTypeFor
                            (BindingTypes.ITEM, parent)));
                    it.addImplementsType(parameterizedTypeFor(BindingTypes.INSTANTIABLE, it));
                }
            }

            if (!(schemaNode instanceof GroupingDefinition)) {
                it.addImplementsType(BindingTypes.augmentable(it));
            }
        } else {
            it.addImplementsType(BindingTypes.TREE_NODE);
        }

        if (schemaNode instanceof DataNodeContainer) {
            groupingsToGenTypes(module, ((DataNodeContainer) schemaNode).getGroupings(), genCtx, schemaContext,
                    verboseClassComments, genTypeBuilders, typeProvider);
            it = addImplementedInterfaceFromUses((DataNodeContainer) schemaNode, it, genCtx);
        }

        return it;
    }

    static GeneratedTypeBuilder resolveNotification(final GeneratedTypeBuilder listenerInterface, String
            parentName, final String basePackageName, final NotificationDefinition notification, final Module module,
            final SchemaContext schemaContext, final boolean verboseClassComments, Map<String, Map<String, GeneratedTypeBuilder>>
            genTypeBuilders, TypeProvider typeProvider, Map<Module, ModuleContext> genCtx) {
        final GeneratedTypeBuilder notificationInterface = addDefaultInterfaceDefinition
                (basePackageName, notification, null, module, genCtx, schemaContext,
                        verboseClassComments, genTypeBuilders, typeProvider, BindingNamespaceType.Data);
        annotateDeprecatedIfNecessary(notification.getStatus(), notificationInterface);
        notificationInterface.addImplementsType(NOTIFICATION);
        genCtx.get(module).addChildNodeType(notification, notificationInterface);

        // Notification object
        resolveDataSchemaNodes(module, basePackageName, notificationInterface,
                notificationInterface, notification.getChildNodes(), genCtx, schemaContext,
                verboseClassComments, genTypeBuilders, typeProvider, BindingNamespaceType.Data);

        //in case of tied notification, incorporate parent's localName
        final StringBuilder sb = new StringBuilder("on_");
        if (parentName != null) {
            sb.append(parentName).append('_');
        }
        sb.append(notificationInterface.getName());

        listenerInterface.addMethod(JavaIdentifierNormalizer.normalizeSpecificIdentifier(sb.toString(), JavaIdentifier.METHOD))
                .setAccessModifier(AccessModifier.PUBLIC).addParameter(notificationInterface, "notification")
                .setComment(encodeAngleBrackets(notification.getDescription())).setReturnType(Types.VOID);
        return listenerInterface;
    }

    /**
     * Returns reference to generated type builder for specified
     * <code>schemaNode</code> with <code>packageName</code>.
     *
     * Firstly the generated type builder is searched in
     * {@link BindingGeneratorImpl#genTypeBuilders genTypeBuilders}. If it isn't
     * found it is created and added to <code>genTypeBuilders</code>.
     *
     * @param basePackageName
     *            string contains the module package name
     * @param schemaNode
     *            schema node which provide data about the schema node name
     * @param schemaContext schema context
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
    static GeneratedTypeBuilder addRawInterfaceDefinition(final String basePackageName, final SchemaNode schemaNode,
            final SchemaContext schemaContext, final String prefix, final String suffix,
            final boolean verboseClassComments, final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders,
            final BindingNamespaceType namespaceType, ModuleContext context) {

        Preconditions.checkArgument(schemaNode != null, "Data Schema Node cannot be NULL.");
        Preconditions.checkArgument(basePackageName != null, "Base package Name for Generated Type cannot be NULL.");
        String schemaNodeName = schemaNode.getQName().getLocalName();
        Preconditions.checkArgument(schemaNodeName != null, "Local Name of QName for Data Schema Node cannot be NULL.");

        if (prefix != null && !prefix.isEmpty()) {
            // underscore used as separator for distinction of class name parts
            schemaNodeName = new StringBuilder(prefix).append('_').append(schemaNodeName).toString();
        }

        if (suffix != null && !suffix.isEmpty()) {
            // underscore used as separator for distinction of class name parts
            schemaNodeName = new StringBuilder(schemaNodeName).append('_').append(suffix).toString();
        }

        final String packageName = packageNameForGeneratedType(basePackageName, schemaNode.getPath(), namespaceType);
        final GeneratedTypeBuilderImpl newType = new GeneratedTypeBuilderImpl(packageName, schemaNodeName, context);
        final Module module = SchemaContextUtil.findParentModule(schemaContext, schemaNode);
        qNameConstant(newType, BindingMapping.QNAME_STATIC_FIELD_NAME, schemaNode.getQName());
        newType.addComment(schemaNode.getDescription());
        newType.setDescription(createDescription(schemaNode, newType.getFullyQualifiedName(), schemaContext,
                verboseClassComments, namespaceType));
        newType.setReference(schemaNode.getReference());
        newType.setSchemaPath((List<QName>) schemaNode.getPath().getPathFromRoot());
        newType.setModuleName(module.getName());
        newType.setBasePackageName(BindingMapping.getRootPackageName(module));
        newType.setWithBuilder(AuxiliaryGenUtils.hasBuilderClass(schemaNode, namespaceType));

        if (!genTypeBuilders.containsKey(packageName)) {
            final Map<String, GeneratedTypeBuilder> builders = new HashMap<>();
            builders.put(newType.getName(), newType);
            genTypeBuilders.put(packageName, builders);
        } else {
            final Map<String, GeneratedTypeBuilder> builders = genTypeBuilders.get(packageName);
            if (!builders.containsKey(newType.getName())) {
                builders.put(newType.getName(), newType);
            }
        }
        return newType;

    }

    private static void addSchemaNodeToBuilderAsMethod(final String basePackageName, final DataSchemaNode node,
        final GeneratedTypeBuilder typeBuilder, final GeneratedTypeBuilder childOf, final Module module,
        final Map<Module, ModuleContext> genCtx, final SchemaContext schemaContext, final boolean verboseClassComments,
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders, final TypeProvider typeProvider,
        final BindingNamespaceType namespaceType) {

        if (node != null && typeBuilder != null) {
            if (node instanceof ContainerSchemaNode) {
                containerToGenType(module, basePackageName, typeBuilder, childOf, (ContainerSchemaNode) node,
                        schemaContext, verboseClassComments, genCtx, genTypeBuilders, typeProvider, namespaceType);
            } else if (node instanceof LeafListSchemaNode) {
                resolveLeafListSchemaNode(schemaContext, typeBuilder, (LeafListSchemaNode) node, module,
                            typeProvider, genCtx);
            } else if (node instanceof LeafSchemaNode) {
                resolveLeafSchemaNodeAsMethod("", schemaContext, typeBuilder, genCtx, (LeafSchemaNode) node, module,
                        typeProvider);
            } else if (node instanceof ListSchemaNode) {
                listToGenType(module, basePackageName, typeBuilder, childOf, (ListSchemaNode) node, schemaContext,
                        verboseClassComments, genCtx, genTypeBuilders, typeProvider, namespaceType);
            } else if (node instanceof ChoiceSchemaNode) {
                choiceToGenType(module, schemaContext, verboseClassComments, basePackageName, childOf,
                        (ChoiceSchemaNode) node, genTypeBuilders, genCtx, typeProvider, namespaceType);
            } else if (node instanceof AnyXmlSchemaNode || node instanceof AnyDataSchemaNode) {
                resolveAnyNodeAsMethod(schemaContext, typeBuilder, genCtx, node, module, typeProvider);
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
    private static void choiceToGenType(final Module module, final SchemaContext schemaContext, final boolean
            verboseClasssComments, final String basePackageName, final GeneratedTypeBuilder parent, final
            ChoiceSchemaNode choiceNode, final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders,
            final Map<Module, ModuleContext> genCtx, final TypeProvider typeProvider, final BindingNamespaceType namespaceType) {
        checkArgument(basePackageName != null, "Base Package Name cannot be NULL.");
        checkArgument(choiceNode != null, "Choice Schema Node cannot be NULL.");

        final GeneratedTypeBuilder choiceTypeBuilder = addRawInterfaceDefinition(basePackageName, choiceNode,
                schemaContext, "", "", verboseClasssComments, genTypeBuilders, namespaceType, genCtx.get(module));
        constructGetter(parent, choiceNode.getQName().getLocalName(),
                choiceNode.getDescription(), choiceTypeBuilder, choiceNode.getStatus());
        if (namespaceType.equals(BindingNamespaceType.Data)) {
            choiceTypeBuilder.addImplementsType(parameterizedTypeFor(BindingTypes.INSTANTIABLE, choiceTypeBuilder));
        }
        annotateDeprecatedIfNecessary(choiceNode.getStatus(), choiceTypeBuilder);
        genCtx.get(module).addChildNodeType(choiceNode, choiceTypeBuilder);
        generateTypesFromChoiceCases(module, schemaContext, genCtx, basePackageName, choiceTypeBuilder.toInstance(),
            choiceNode, verboseClasssComments, typeProvider, genTypeBuilders, namespaceType);
    }

    private static void containerToGenType(final Module module, final String basePackageName,
        final GeneratedTypeBuilder parent, final GeneratedTypeBuilder childOf, final ContainerSchemaNode node,
        final SchemaContext schemaContext, final boolean verboseClassComments, final Map<Module, ModuleContext> genCtx,
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders, final TypeProvider typeProvider,
        final BindingNamespaceType namespaceType) {

        final GeneratedTypeBuilder genType = processDataSchemaNode(module, basePackageName, childOf, node,
                schemaContext, verboseClassComments, genCtx, genTypeBuilders, typeProvider, namespaceType);
        if (genType != null) {
            StringBuilder getterName = new StringBuilder(node.getQName().getLocalName());
            final MethodSignatureBuilder getter = constructGetter(parent, getterName.toString(), node.getDescription(), genType, node.getStatus());
            resolveDataSchemaNodes(module, basePackageName, genType, genType, node.getChildNodes(), genCtx,
                    schemaContext, verboseClassComments, genTypeBuilders, typeProvider, namespaceType);
            processUsesImplements(node, module, schemaContext, genCtx, namespaceType);
        }
    }

    private static void listToGenType(final Module module, final String basePackageName, final GeneratedTypeBuilder
            parent, final GeneratedTypeBuilder childOf, final ListSchemaNode node, final SchemaContext schemaContext,
            final boolean verboseClassComments, final Map<Module, ModuleContext> genCtx,
            final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders, final TypeProvider typeProvider,
            final BindingNamespaceType namespaceType) {

        final GeneratedTypeBuilder genType = processDataSchemaNode(module, basePackageName, childOf, node,
                schemaContext, verboseClassComments, genCtx, genTypeBuilders, typeProvider, namespaceType);
        if (genType != null) {
            final String nodeName = node.getQName().getLocalName();

            Type getterReturnType = Types.listTypeFor(genType);
            if (namespaceType.equals(BindingNamespaceType.Grouping)) {
                getterReturnType = Types.listTypeFor(wildcardTypeFor(genType.getPackageName(), genType.getName(),
                                true, true, null));
            }
            constructGetter(parent, nodeName, node.getDescription(), getterReturnType, node.getStatus());

            final List<QName> listKeys = node.getKeyDefinition();
            final String packageName = new StringBuilder(packageNameForGeneratedType(basePackageName, node.getPath(),
                    BindingNamespaceType.Key)).append('.').append(nodeName).toString();
            //FIXME: Is it neccessary to generate interface of key and implemented by class?
            if (namespaceType.equals(BindingNamespaceType.Grouping)) {
                final GeneratedTypeBuilder genTypeBuilder = resolveListKeyTypeBuilder(packageName, node, genCtx.get(module));
                for (final DataSchemaNode schemaNode : node.getChildNodes()) {
                    if (resolveDataSchemaNodesCheck(module, schemaContext, schemaNode)) {
                        addSchemaNodeToListTypeBuilders(nodeName, basePackageName, schemaNode, genType, genTypeBuilder, listKeys,
                                module, typeProvider, schemaContext, genCtx, genTypeBuilders, verboseClassComments, namespaceType);
                    }
                }
                if (genTypeBuilder != null) {
                    typeBuildersToGenTypes(module, genType, genTypeBuilder.toInstance(), genCtx, namespaceType);
                    genCtx.get(module).addKeyType(node.getPath(), genTypeBuilder);
                }
            } else {
                final GeneratedTOBuilder genTOBuilder = resolveListKeyTOBuilder(packageName, node, genCtx.get(module));
                for (final DataSchemaNode schemaNode : node.getChildNodes()) {
                    if (resolveDataSchemaNodesCheck(module, schemaContext, schemaNode)) {
                        addSchemaNodeToListBuilders(nodeName, basePackageName, schemaNode, genType, genTOBuilder, listKeys,
                                module, typeProvider, schemaContext, genCtx, genTypeBuilders, verboseClassComments, namespaceType);
                    }
                }
                processUsesImplements(node, module, schemaContext, genCtx, namespaceType);

                // serialVersionUID
                if (genTOBuilder != null) {
                    final GeneratedPropertyBuilder prop = new GeneratedPropertyBuilderImpl("serialVersionUID");
                    prop.setValue(Long.toString(computeDefaultSUID(genTOBuilder)));
                    genTOBuilder.setSUID(prop);

                    typeBuildersToGenTypes(module, genType, genTOBuilder.toInstance(), genCtx, namespaceType);
                    genCtx.get(module).addGeneratedTOBuilder(node.getPath(), genTOBuilder);
                }
            }
        }
    }

    private static void typeBuildersToGenTypes(final Module module, final GeneratedTypeBuilder typeBuilder,
            final Type keyType, final Map<Module, ModuleContext> genCtx,
            final BindingNamespaceType namespaceType) {
        checkArgument(typeBuilder != null, "Generated Type Builder cannot be NULL.");
        if (keyType != null) {
            Type returnKeyType = keyType;
            if (namespaceType.equals(BindingNamespaceType.Grouping)) {
                returnKeyType = wildcardTypeFor(keyType.getPackageName(), keyType.getName(),
                        true, true, null);
            }
            constructGetter(typeBuilder, "key", "Returns Primary Key of Yang List Type", returnKeyType, Status.CURRENT);

        }
    }

    /**
     * Converts <code>leaf</code> to the getter method which is added to
     * <code>typeBuilder</code>.
     *
     * @param typeBuilder
     *            generated type builder to which is added getter method as
     *            <code>leaf</code> mapping
     * @param leaf
     *            leaf schema node which is mapped as getter method which is
     *            added to <code>typeBuilder</code>
     * @param module
     *            Module in which type was defined
     * @return boolean value
     *         <ul>
     *         <li>false - if <code>leaf</code> or <code>typeBuilder</code> are
     *         null</li>
     *         <li>true - in other cases</li>
     *         </ul>
     */
    private static Type resolveLeafSchemaNodeAsMethod(final String nodeName, final SchemaContext schemaContext,
            final GeneratedTypeBuilder typeBuilder, final Map<Module, ModuleContext> genCtx, final LeafSchemaNode leaf,
            final Module module, final TypeProvider typeProvider) {
        if (leaf == null || typeBuilder == null) {
            return null;
        }

        final String leafName = leaf.getQName().getLocalName();
        if (leafName == null) {
            return null;
        }

        final Module parentModule = findParentModule(schemaContext, leaf);
        Type returnType = null;

        final TypeDefinition<?> typeDef = leaf.getType();
        if (isInnerType(leaf, typeDef)) {
            if (typeDef instanceof EnumTypeDefinition) {
                returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, leaf, genCtx.get(module));
                final EnumTypeDefinition enumTypeDef = (EnumTypeDefinition) typeDef;
                final EnumBuilder enumBuilder = resolveInnerEnumFromTypeDefinition(enumTypeDef, leaf.getQName(),
                        genCtx, typeBuilder, module);
                if (enumBuilder != null) {
                    returnType = enumBuilder.toInstance(typeBuilder);
                }
                ((TypeProviderImpl) typeProvider).putReferencedType(leaf.getPath(), returnType);
            } else if (typeDef instanceof UnionTypeDefinition) {
                final GeneratedTOBuilder genTOBuilder = addTOToTypeBuilder(typeDef, typeBuilder, leaf, parentModule,
                        typeProvider, schemaContext, genCtx.get(module));
                if (genTOBuilder != null) {
                    //TODO: https://bugs.opendaylight.org/show_bug.cgi?id=2289
                    returnType = createReturnTypeForUnion(genTOBuilder, typeDef, typeBuilder, parentModule, typeProvider);
                }
            } else if (typeDef instanceof BitsTypeDefinition) {
                final GeneratedTOBuilder genTOBuilder = addTOToTypeBuilder(typeDef, typeBuilder, leaf, parentModule,
                        typeProvider, schemaContext, genCtx.get(module));
                if (genTOBuilder != null) {
                    returnType = genTOBuilder.toInstance();
                }
            } else {
                // It is constrained version of already declared type (inner declared type exists,
                // onlyfor special cases (Enum, Union, Bits), which were already checked.
                // In order to get proper class we need to look up closest derived type
                // and apply restrictions from leaf type
                final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(typeDef);
                returnType = typeProvider.javaTypeForSchemaDefinitionType(getBaseOrDeclaredType(typeDef), leaf,
                        restrictions, genCtx.get(module));
            }
        } else {
            final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(typeDef);
            returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, leaf, restrictions, genCtx.get(module));
        }

        if (returnType == null) {
            return null;
        }

        if (typeDef instanceof EnumTypeDefinition) {
            ((TypeProviderImpl) typeProvider).putReferencedType(leaf.getPath(), returnType);
        }

        String leafDesc = leaf.getDescription();
        if (leafDesc == null) {
            leafDesc = "";
        }

        final String leafGetterName;
        if ("key".equals(leafName.toLowerCase())) {
            StringBuilder sb = new StringBuilder(leafName)
                .append('_').append("RESERVED_WORD");
            leafGetterName = sb.toString();
        } else {
            leafGetterName = leafName;
        }
        constructGetter(typeBuilder, leafGetterName, leafDesc, returnType, leaf.getStatus());
        return returnType;
    }

    /**
     * Converts <code>node</code> leaf list schema node to getter method of
     * <code>typeBuilder</code>.
     *
     * @param typeBuilder
     *            generated type builder to which is <code>node</code> added as
     *            getter method
     * @param node
     *            leaf list schema node which is added to
     *            <code>typeBuilder</code> as getter method
     * @param module module
     * @param typeProvider type provider instance
     * @param genCtx actual generated context
     * @return boolean value
     *         <ul>
     *         <li>true - if <code>node</code>, <code>typeBuilder</code>,
     *         nodeName equal null or <code>node</code> is added by <i>uses</i></li>
     *         <li>false - other cases</li>
     *         </ul>
     */
    private static boolean resolveLeafListSchemaNode(final SchemaContext schemaContext, final GeneratedTypeBuilder
            typeBuilder, final LeafListSchemaNode node, final Module module, final TypeProvider typeProvider,
            final Map<Module, ModuleContext> genCtx) {
        if (node == null || typeBuilder == null) {
            return false;
        }

        final QName nodeName = node.getQName();

        final TypeDefinition<?> typeDef = node.getType();
        final Module parentModule = findParentModule(schemaContext, node);

        Type returnType = null;
        if (typeDef.getBaseType() == null) {
            if (typeDef instanceof EnumTypeDefinition) {
                returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, node, genCtx.get(module));
                final EnumTypeDefinition enumTypeDef = (EnumTypeDefinition) typeDef;
                final EnumBuilder enumBuilder = resolveInnerEnumFromTypeDefinition(enumTypeDef, nodeName,
                        genCtx, typeBuilder, module);
                returnType = new ReferencedTypeImpl(enumBuilder.getPackageName(), enumBuilder.getName(), true,
                        null);
                ((TypeProviderImpl) typeProvider).putReferencedType(node.getPath(), returnType);
            } else if (typeDef instanceof UnionTypeDefinition) {
                final GeneratedTOBuilder genTOBuilder = addTOToTypeBuilder(typeDef, typeBuilder, node, parentModule,
                        typeProvider, schemaContext, genCtx.get(module));
                if (genTOBuilder != null) {
                    returnType = createReturnTypeForUnion(genTOBuilder, typeDef, typeBuilder, parentModule, typeProvider);
                }
            } else if (typeDef instanceof BitsTypeDefinition) {
                final GeneratedTOBuilder genTOBuilder = addTOToTypeBuilder(typeDef, typeBuilder, node, parentModule,
                        typeProvider, schemaContext, genCtx.get(module));
                returnType = genTOBuilder.toInstance();
            } else {
                final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(typeDef);
                returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, node, restrictions, genCtx.get(module));
            }
        } else {
            final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(typeDef);
            returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, node, restrictions, genCtx.get(module));
        }

        final ParameterizedType listType = Types.listTypeFor(returnType);
        constructGetter(typeBuilder, nodeName.getLocalName(), node.getDescription(), listType, node.getStatus());
        return true;
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
     * @param schemaContext
     *            current schema context
     * @param genCtx
     *            actual generated context
     * @param basePackageName
     *            string with the module package name
     * @param refChoiceType
     *            type which represents superior <i>case</i>
     * @param choiceNode
     *            choice case node which is mapped to generated type
     * @param verboseClassComments
     *            Javadoc verbosity switch
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>basePackageName</code> equals null</li>
     *             <li>if <code>refChoiceType</code> equals null</li>
     *             <li>if <code>caseNodes</code> equals null</li>
     *             </ul>
     */
    private static void generateTypesFromChoiceCases(final Module module, final SchemaContext schemaContext,
            final Map<Module, ModuleContext> genCtx, final String basePackageName, final Type refChoiceType,
            final ChoiceSchemaNode choiceNode, final boolean verboseClassComments, final TypeProvider typeProvider,
            final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders, final BindingNamespaceType namespaceType) {
        checkArgument(basePackageName != null, "Base Package Name cannot be NULL.");
        checkArgument(refChoiceType != null, "Referenced Choice Type cannot be NULL.");
        checkArgument(choiceNode != null, "ChoiceNode cannot be NULL.");

        final Set<ChoiceCaseNode> caseNodes = choiceNode.getCases();
        if (caseNodes == null) {
            return;
        }

        for (final ChoiceCaseNode caseNode : caseNodes) {
            if (caseNode != null && resolveDataSchemaNodesCheck(module, schemaContext, caseNode)) {
                final GeneratedTypeBuilder caseTypeBuilder = addDefaultInterfaceDefinition(basePackageName, caseNode,
                    module, genCtx, schemaContext, verboseClassComments, genTypeBuilders, typeProvider, namespaceType);
                caseTypeBuilder.addImplementsType(refChoiceType);
                caseTypeBuilder.setParentTypeForBuilder(refChoiceType);
                annotateDeprecatedIfNecessary(caseNode.getStatus(), caseTypeBuilder);
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
                        GeneratedTypeBuilder childOfType = findChildNodeByPath(parent.getPath(), genCtx);
                        if (childOfType == null) {
                            childOfType = findGroupingByPath(parent.getPath(), genCtx);
                        }
                        resolveDataSchemaNodes(module, basePackageName, caseTypeBuilder, childOfType, caseChildNodes,
                                genCtx, schemaContext, verboseClassComments, genTypeBuilders, typeProvider, namespaceType);
                    } else {
                        final GeneratedTypeBuilder moduleType = genCtx.get(module).getModuleNode();
                        Preconditions.checkNotNull(moduleType, "Module type can not be null.");
                        resolveDataSchemaNodes(module, basePackageName, caseTypeBuilder, moduleType, caseChildNodes,
                            genCtx, schemaContext, verboseClassComments, genTypeBuilders, typeProvider, namespaceType);
                    }
                    processUsesImplements(caseNode, module, schemaContext, genCtx, namespaceType);
                }
            }
        }
    }

    private static Type resolveAnyNodeAsMethod(final SchemaContext schemaContext, final GeneratedTypeBuilder
            typeBuilder, final Map<Module, ModuleContext> genCtx, final DataSchemaNode node, final Module module,
            final TypeProvider typeProvider) {

        final String anyName = node.getQName().getLocalName();
        if (anyName == null) {
            return null;
        }

        String anyDesc = node.getDescription();
        if (anyDesc == null) {
            anyDesc = "";
        }

        Type returnType = Types.DOCUMENT;

        constructGetter(typeBuilder, anyName, anyDesc, returnType, node.getStatus());
        return returnType;
    }

    /**
     * Adds <code>schemaNode</code> to <code>typeBuilder</code> as getter method
     * or to <code>genTOBuilder</code> as property.
     *
     * @param nodeName
     *            string contains the name of list
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
     *            list of string which contains QNames of the list keys
     * @param module
     *            current module
     * @param typeProvider
     *            provider that defines contract for generated types
     * @param schemaContext
     *            schema context
     * @param genCtx
     *            map of generated entities in context of YANG modules
     * @param genTypeBuilders
     *            map of generated type builders
     * @param verboseClassComments
     *            generate verbose comments
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>schemaNode</code> equals null</li>
     *             <li>if <code>typeBuilder</code> equals null</li>
     *             </ul>
     */
    private static void addSchemaNodeToListBuilders(final String nodeName, final String basePackageName,
            final DataSchemaNode schemaNode, final GeneratedTypeBuilder typeBuilder,
            final GeneratedTOBuilder genTOBuilder, final List<QName> listKeys, final Module module,
            final TypeProvider typeProvider, final SchemaContext schemaContext, final Map<Module, ModuleContext> genCtx,
            final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders, final boolean verboseClassComments,
            final BindingNamespaceType namespaceType) {
        checkArgument(schemaNode != null, "Data Schema Node cannot be NULL.");
        checkArgument(typeBuilder != null, "Generated Type Builder cannot be NULL.");

        if (schemaNode instanceof LeafSchemaNode) {
            final LeafSchemaNode leaf = (LeafSchemaNode) schemaNode;
            final QName leafQName = leaf.getQName();

            final Type type = resolveLeafSchemaNodeAsMethod(nodeName, schemaContext, typeBuilder, genCtx, leaf, module,
                    typeProvider);
            if (listKeys.contains(leafQName)) {
                if (type == null) {
                    resolveLeafSchemaNodeAsProperty(nodeName, schemaContext, typeProvider, genCtx, genTOBuilder, leaf, true,
                        module);
                } else {
                    AuxiliaryGenUtils.resolveLeafSchemaNodeAsProperty(nodeName, genTOBuilder, leaf, type, true);
                }
            }
        } else {
            if (schemaNode instanceof LeafListSchemaNode) {
                resolveLeafListSchemaNode(schemaContext, typeBuilder, (LeafListSchemaNode) schemaNode, module,
                        typeProvider, genCtx);
            } else if (schemaNode instanceof ContainerSchemaNode) {
                containerToGenType(module, basePackageName, typeBuilder, typeBuilder, (ContainerSchemaNode) schemaNode,
                        schemaContext, verboseClassComments, genCtx, genTypeBuilders, typeProvider, namespaceType);
            } else if (schemaNode instanceof ListSchemaNode) {
                listToGenType(module, basePackageName, typeBuilder, typeBuilder, (ListSchemaNode) schemaNode,
                        schemaContext, verboseClassComments, genCtx, genTypeBuilders, typeProvider, namespaceType);
            } else if (schemaNode instanceof ChoiceSchemaNode) {
                choiceToGenType(module, schemaContext, verboseClassComments, basePackageName, typeBuilder,
                        (ChoiceSchemaNode) schemaNode, genTypeBuilders, genCtx, typeProvider, namespaceType);
            }
        }
    }

    private static void addSchemaNodeToListTypeBuilders(final String nodeName, final String basePackageName,
                                                    final DataSchemaNode schemaNode, final GeneratedTypeBuilder typeBuilder,
                                                    final GeneratedTypeBuilder genTypeBuilder, final List<QName> listKeys, final Module module,
                                                    final TypeProvider typeProvider, final SchemaContext schemaContext, final Map<Module, ModuleContext> genCtx,
                                                    final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders, final boolean verboseClassComments,
                                                    final BindingNamespaceType namespaceType) {
        checkArgument(schemaNode != null, "Data Schema Node cannot be NULL.");
        checkArgument(typeBuilder != null, "Generated Type Builder cannot be NULL.");

        if (schemaNode instanceof LeafSchemaNode) {
            final LeafSchemaNode leaf = (LeafSchemaNode) schemaNode;
            final QName leafQName = leaf.getQName();
            final Type type = resolveLeafSchemaNodeAsMethod(nodeName, schemaContext, typeBuilder, genCtx, leaf, module,
                    typeProvider);
            if (listKeys.contains(leafQName)) {
                resolveLeafSchemaNodeAsMethod(nodeName, schemaContext, genTypeBuilder, genCtx, leaf, module,
                        typeProvider);
            }
        } else {
            if (schemaNode instanceof LeafListSchemaNode) {
                resolveLeafListSchemaNode(schemaContext, typeBuilder, (LeafListSchemaNode) schemaNode, module,
                        typeProvider, genCtx);
            } else if (schemaNode instanceof ContainerSchemaNode) {
                containerToGenType(module, basePackageName, typeBuilder, typeBuilder, (ContainerSchemaNode) schemaNode,
                        schemaContext, verboseClassComments, genCtx, genTypeBuilders, typeProvider, namespaceType);
            } else if (schemaNode instanceof ListSchemaNode) {
                listToGenType(module, basePackageName, typeBuilder, typeBuilder, (ListSchemaNode) schemaNode,
                        schemaContext, verboseClassComments, genCtx, genTypeBuilders, typeProvider, namespaceType);
            } else if (schemaNode instanceof ChoiceSchemaNode) {
                choiceToGenType(module, schemaContext, verboseClassComments, basePackageName, typeBuilder,
                        (ChoiceSchemaNode) schemaNode, genTypeBuilders, genCtx, typeProvider, namespaceType);
            }
        }
    }

    private static boolean resolveLeafSchemaNodeAsProperty(final String nodeName, final SchemaContext schemaContext, final TypeProvider
            typeProvider, final Map<Module, ModuleContext> genCtx, final GeneratedTOBuilder
            toBuilder, final LeafSchemaNode leaf, final boolean isReadOnly, final Module module) {

        if (leaf != null && toBuilder != null) {
            Type returnType;
            final TypeDefinition<?> typeDef = leaf.getType();
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
                returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, leaf, genCtx.get(module));
            }
            return AuxiliaryGenUtils.resolveLeafSchemaNodeAsProperty(nodeName, toBuilder, leaf, returnType, isReadOnly);
        }
        return false;
    }

    private static TypeDefinition<?> getBaseOrDeclaredType(final TypeDefinition<?> typeDef) {
        final TypeDefinition<?> baseType = typeDef.getBaseType();
        return (baseType != null && baseType.getBaseType() != null) ? baseType : typeDef;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static GeneratedTypeBuilder processDataSchemaNode(final Module module, final String basePackageName,
        final GeneratedTypeBuilder childOf, final DataSchemaNode node, final SchemaContext schemaContext,
        final boolean verboseClassComments, Map<Module, ModuleContext> genCtx, final Map<String, Map<String,
        GeneratedTypeBuilder>> genTypeBuilders, final TypeProvider typeProvider, final BindingNamespaceType namespaceType) {

        final GeneratedTypeBuilder genType = addDefaultInterfaceDefinition(basePackageName, node, childOf, module,
                genCtx, schemaContext, verboseClassComments, genTypeBuilders, typeProvider, namespaceType);
        genType.addComment(node.getDescription());
        annotateDeprecatedIfNecessary(node.getStatus(), genType);
        genType.setDescription(createDescription(node, genType.getFullyQualifiedName(), schemaContext,
                verboseClassComments, namespaceType));
        genType.setModuleName(module.getName());
        genType.setReference(node.getReference());
        genType.setSchemaPath((List) node.getPath().getPathFromRoot());
        genType.setParentTypeForBuilder(childOf);
        if (node instanceof DataNodeContainer) {
            genCtx.get(module).addChildNodeType(node, genType);
        }
        return genType;
    }

    /**
     * Converts all <b>groupings</b> of the module to the list of
     * <code>Type</code> objects. Firstly are groupings sorted according mutual
     * dependencies. At least dependent (independent) groupings are in the list
     * saved at first positions. For every grouping the record is added to map
     * {@link ModuleContext#groupings allGroupings}
     *
     * @param module
     *            current module
     * @param groupings
     *            collection of groupings from which types will be generated
     * @param typeProvider
     *            provider that defines contract for generated types
     * @param schemaContext
     *            schema context
     * @param genCtx
     *            map of generated entities in context of YANG modules
     * @param genTypeBuilders
     *            map of generated type builders
     * @param verboseClassComments
     *            generate verbose comments
     *
     */
    static Map<Module, ModuleContext> groupingsToGenTypes(final Module module, final Collection<GroupingDefinition>
            groupings, Map<Module, ModuleContext> genCtx, final SchemaContext schemaContext, final boolean
            verboseClassComments, Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders, final TypeProvider typeProvider) {
        final String basePackageName = BindingMapping.getRootPackageName(module);
        final List<GroupingDefinition> groupingsSortedByDependencies = new GroupingDefinitionDependencySort()
                .sort(groupings);
        for (final GroupingDefinition grouping : groupingsSortedByDependencies) {
            genCtx = groupingToGenType(basePackageName, grouping, module, genCtx, schemaContext,
                    verboseClassComments, genTypeBuilders, typeProvider);
        }
        return genCtx;
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
     * @param typeProvider
     *            provider that defines contract for generated types
     * @param schemaContext
     *            schema context
     * @param genCtx
     *            map of generated entities in context of YANG modules
     * @param genTypeBuilders
     *            map of generated type builders
     * @param verboseClassComments
     *            generate verbose comments
     *
     * @return GeneratedType which is generated from grouping (object of type
     *         <code>GroupingDefinition</code>)
     */
    private static Map<Module, ModuleContext> groupingToGenType(final String basePackageName, final GroupingDefinition grouping, final Module
            module, Map<Module, ModuleContext> genCtx, final SchemaContext schemaContext, final boolean
            verboseClassComments, Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders, final TypeProvider typeProvider) {
        final GeneratedTypeBuilder genType = addDefaultInterfaceDefinition(basePackageName, grouping, module, genCtx,
                schemaContext, verboseClassComments, genTypeBuilders, typeProvider, BindingNamespaceType.Grouping);
        annotateDeprecatedIfNecessary(grouping.getStatus(), genType);
        genCtx.get(module).addGroupingType(grouping, genType);
        resolveDataSchemaNodes(module, basePackageName, genType, genType, grouping.getChildNodes(), genCtx,
                schemaContext, verboseClassComments, genTypeBuilders, typeProvider, BindingNamespaceType.Grouping);
        genCtx = processUsesImplements(grouping, module, schemaContext, genCtx, BindingNamespaceType.Grouping);
        return genCtx;
    }

    /**
     * //TODO: add information about multiple base identities in YANG 1.1
     * Converts the <b>identity</b> object to GeneratedType. Firstly it is
     * created transport object builder. If identity contains base identity then
     * reference to base identity is added to superior identity as its extend.
     * If identity doesn't contain base identity then only reference to abstract
     * class {@link org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode
     * BaseIdentity} is added
     *
     * @param module
     *            current module
     * @param basePackageName
     *            string contains the module package name
     * @param identity
     *            IdentitySchemaNode which contains data about identity
     * @param schemaContext
     *            SchemaContext which is used to get package and name
     *            information about base of identity
     * @param genCtx generated context
     * @return returns generated context
     */
    static Map<Module, ModuleContext> identityToGenType(final Module module, final String basePackageName,
            final IdentitySchemaNode identity, final SchemaContext schemaContext, Map<Module, ModuleContext> genCtx,
            boolean verboseClassComments) {

        resolveIdentitySchemaNode(basePackageName, schemaContext, identity, module, verboseClassComments, genCtx);
        return genCtx;
    }

    private static GeneratedTOBuilder resolveIdentitySchemaNode(final String basePackageName, final SchemaContext schemaContext,
            final IdentitySchemaNode identity, final Module module, final boolean verboseClassComments,
            final Map<Module, ModuleContext> genCtx) {
        Preconditions.checkNotNull(identity,"Identity can not be null!");

        //check first if identity has been resolved as base identity of some other one
        GeneratedTOBuilder newType = findIdentityByQname(identity.getQName(), genCtx);
        if (newType == null) {
            final Module parentModule = SchemaContextUtil.findParentModule(schemaContext, identity);
            Preconditions.checkState(module.equals(parentModule),
                    "If the type is null ,it must be in the same module, otherwise it must has been"
                            + "resolved by an imported module.");

            final String packageName = BindingGeneratorUtil.packageNameForGeneratedType(basePackageName, identity.getPath(),
                    BindingNamespaceType.Identity);
            newType = new GeneratedTOBuilderImpl(packageName, identity.getQName().getLocalName(), true, false,
                    genCtx.get(module));

            final Set<IdentitySchemaNode> baseIdentities = identity.getBaseIdentities();
            if (baseIdentities.size() == 0) {
                //no base - abstract
                final GeneratedTOBuilderImpl gto = new GeneratedTOBuilderImpl(BaseIdentity.class.getPackage().getName(),
                        BaseIdentity.class.getSimpleName(), genCtx.get(module));
                newType.setExtendsType(gto.toInstance());
            } else {
                //one base - inheritance
                final IdentitySchemaNode baseIdentity = baseIdentities.iterator().next();
                GeneratedTOBuilder baseType = resolveIdentitySchemaNode(basePackageName, schemaContext,
                    baseIdentity, module, verboseClassComments, genCtx);
                newType.setExtendsType(baseType.toInstance());
            }

            newType.setAbstract(true);
            newType.addComment(identity.getDescription());
            newType.setDescription(createDescription(identity, newType.getFullyQualifiedName(), schemaContext,
                    verboseClassComments, BindingNamespaceType.Identity));
            newType.setReference(identity.getReference());
            newType.setModuleName(module.getName());
            newType.setSchemaPath((List) identity.getPath().getPathFromRoot());

            qNameConstant(newType, BindingMapping.QNAME_STATIC_FIELD_NAME, identity.getQName());

            genCtx.get(module).addIdentityType(identity.getQName(), newType);
        }
        return newType;
    }
}
