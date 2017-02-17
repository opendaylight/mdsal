/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.impl;

import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.annotateDeprecatedIfNecessary;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.augGenTypeName;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.constructGetter;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.createDescription;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.getAugmentIdentifier;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.qNameConstant;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil.packageNameForGeneratedType;
import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opendaylight.mdsal.binding.javav2.generator.util.BindingTypes;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifier;
import org.opendaylight.mdsal.binding.javav2.generator.util.NonJavaCharsConverter;
import org.opendaylight.mdsal.binding.javav2.generator.util.Types;
import org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder.GeneratedTypeBuilderImpl;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingNamespaceType;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentable;
import org.opendaylight.mdsal.binding.javav2.util.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;

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
     * @param genCtx
     * @param verboseClassComments
     *
     * @return <code>GeneratedTypeBuilder</code> which is internal
     *         representation of the module
     * @throws IllegalArgumentException
     *             if module is null
     */
    static GeneratedTypeBuilder moduleToDataType(final Module module, final Map<Module, ModuleContext> genCtx, final boolean verboseClassComments) {
        Preconditions.checkArgument(module != null, "Module reference cannot be NULL.");

        final GeneratedTypeBuilder moduleDataTypeBuilder = moduleTypeBuilder(module, "Data", verboseClassComments);
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
     * @param verboseClassComments
     * @return instance of GeneratedTypeBuilder which represents
     *         <code>module</code>.
     * @throws IllegalArgumentException
     *             if <code>module</code> is null
     */
    private static GeneratedTypeBuilder moduleTypeBuilder(final Module module, final String postfix, final boolean
            verboseClassComments) {
        Preconditions.checkArgument(module != null, "Module reference cannot be NULL.");
        final String packageName = BindingMapping.getRootPackageName(module);
        final String moduleName = BindingMapping.getClassName(NonJavaCharsConverter.convertIdentifier(module.getName
                (), JavaIdentifier.CLASS)) + postfix;

        final GeneratedTypeBuilderImpl moduleBuilder = new GeneratedTypeBuilderImpl(packageName, moduleName);
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
    private static GeneratedTypeBuilder addImplementedInterfaceFromUses(final DataNodeContainer dataNodeContainer,
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
                          final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders) {

        if ((schemaNodes != null) && (parent != null)) {
            for (final DataSchemaNode schemaNode : schemaNodes) {
                if (!schemaNode.isAugmenting() && !schemaNode.isAddedByUses()) {
                    addSchemaNodeToBuilderAsMethod(basePackageName, schemaNode, parent, childOf, module, genCtx,
                            schemaContext, verboseClassComments, genTypeBuilders);
                }
            }
        }
        return parent;
    }

    static GeneratedTypeBuilder addDefaultInterfaceDefinition(final String packageName, final SchemaNode
            schemaNode, final Module module, final Map<Module, ModuleContext> genCtx, final SchemaContext schemaContext,
                                                              final boolean verboseClassComments, final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders) {
        return addDefaultInterfaceDefinition(packageName, schemaNode, null, module, genCtx, schemaContext,
                verboseClassComments, genTypeBuilders);
    }

    static Map<Module, ModuleContext> processUsesAugments(final SchemaContext schemaContext, final
                        DataNodeContainer node, final Module module, Map<Module, ModuleContext> genCtx,  final Map<String,
                        Map<String, GeneratedTypeBuilder>> genTypeBuilders, final boolean verboseClassComments) {
        final String basePackageName = BindingMapping.getRootPackageName(module);
        for (final UsesNode usesNode : node.getUses()) {
            for (final AugmentationSchema augment : usesNode.getAugmentations()) {
                genCtx = AugmentToGenType.usesAugmentationToGenTypes(schemaContext, basePackageName, augment, module,
                        usesNode,
                        node, genCtx, genTypeBuilders, verboseClassComments);
                genCtx = processUsesAugments(schemaContext, augment, module, genCtx, genTypeBuilders, verboseClassComments);
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
     * @return generated type builder for augment in genCtx
     */
    static Map<Module, ModuleContext> addRawAugmentGenTypeDefinition(final Module module, final String augmentPackageName,
                final String basePackageName, final Type targetTypeRef, final AugmentationSchema augSchema,
                final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders, final Map<Module, ModuleContext> genCtx) {

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

        GeneratedTypeBuilder augTypeBuilder = new GeneratedTypeBuilderImpl(augmentPackageName, augTypeName);

        augTypeBuilder.addImplementsType(BindingTypes.TREE_NODE);
        augTypeBuilder.addImplementsType(Types.augmentationTypeFor(targetTypeRef));
        annotateDeprecatedIfNecessary(augSchema.getStatus(), augTypeBuilder);
        augTypeBuilder = addImplementedInterfaceFromUses(augSchema, augTypeBuilder, genCtx);

        augTypeBuilder = augSchemaNodeToMethods(module, basePackageName, augTypeBuilder, augTypeBuilder, augSchema
                .getChildNodes());
        augmentBuilders.put(augTypeName, augTypeBuilder);

        if(!augSchema.getChildNodes().isEmpty()) {
            genCtx.get(module).addTypeToAugmentation(augTypeBuilder, augSchema);

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
                                                        final GeneratedTypeBuilder typeBuilder, final GeneratedTypeBuilder childOf,
                                                        final Iterable<DataSchemaNode> schemaNodes) {
        if ((schemaNodes != null) && (typeBuilder != null)) {
            for (final DataSchemaNode schemaNode : schemaNodes) {
                if (!schemaNode.isAugmenting()) {
                    //TODO: design decomposition and implement it
                    //addSchemaNodeToBuilderAsMethod(basePackageName, schemaNode, typeBuilder, childOf, module);
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
     * @param packageName
     *            string with the name of the package to which
     *            <code>schemaNode</code> belongs.
     * @param schemaNode
     *            schema node for which is created generated type builder
     * @param parent
     *            parent type (can be null)
     * @param schemaContext schema context
     * @return generated type builder <code>schemaNode</code>
     */
    private static GeneratedTypeBuilder addDefaultInterfaceDefinition(final String packageName, final SchemaNode
            schemaNode, final Type parent, final Module module, final Map<Module, ModuleContext> genCtx,
            final SchemaContext schemaContext, final boolean verboseClassComments, final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders) {

        GeneratedTypeBuilder it = addRawInterfaceDefinition(packageName, schemaNode, schemaContext, "",
                verboseClassComments, genTypeBuilders);
        if (parent == null) {
            it.addImplementsType(BindingTypes.TREE_NODE);
        } else {
            it.addImplementsType(BindingTypes.treeChildNode(parent));
        }
        if (!(schemaNode instanceof GroupingDefinition)) {
            it.addImplementsType(BindingTypes.augmentable(it));
        }

        if (schemaNode instanceof DataNodeContainer) {
            //TODO: design decomposition and implement it
            //groupingsToGenTypes(module, ((DataNodeContainer) schemaNode).getGroupings());
            it = addImplementedInterfaceFromUses((DataNodeContainer) schemaNode, it, genCtx);
        }

        return it;
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
     * @param schemaContext
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
    private static GeneratedTypeBuilder addRawInterfaceDefinition(final String packageName, final SchemaNode schemaNode,
                       final SchemaContext schemaContext, final String prefix, final boolean verboseClassComments,
                       final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders) {

        Preconditions.checkArgument(schemaNode != null, "Data Schema Node cannot be NULL.");
        Preconditions.checkArgument(packageName != null, "Package Name for Generated Type cannot be NULL.");
        final String schemaNodeName = schemaNode.getQName().getLocalName();
        Preconditions.checkArgument(schemaNodeName != null, "Local Name of QName for Data Schema Node cannot be NULL.");

        String genTypeName;
        if (prefix == null) {
            genTypeName = BindingMapping
                    .getClassName(NonJavaCharsConverter.normalizeClassIdentifier(packageName, schemaNodeName));
        } else {
            genTypeName = prefix + BindingMapping
                    .getClassName(NonJavaCharsConverter.normalizeClassIdentifier(packageName, schemaNodeName));
        }

        final GeneratedTypeBuilderImpl newType = new GeneratedTypeBuilderImpl(packageName, genTypeName);
        final Module module = SchemaContextUtil.findParentModule(schemaContext, schemaNode);
        qNameConstant(newType, BindingMapping.QNAME_STATIC_FIELD_NAME, schemaNode.getQName());
        newType.addComment(schemaNode.getDescription());
        newType.setDescription(createDescription(schemaNode, newType.getFullyQualifiedName(), schemaContext, verboseClassComments));
        newType.setReference(schemaNode.getReference());
        newType.setSchemaPath((List<QName>) schemaNode.getPath().getPathFromRoot());
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

    private static void addSchemaNodeToBuilderAsMethod(final String basePackageName, final DataSchemaNode node,
        final GeneratedTypeBuilder typeBuilder, final GeneratedTypeBuilder childOf, final Module module,
        final Map<Module, ModuleContext> genCtx, final SchemaContext schemaContext, final boolean verboseClassComments,
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders) {
        //TODO: implement rest of schema nodes GTO building
        if ((node != null) && (typeBuilder != null)) {
            if (node instanceof ContainerSchemaNode) {
                containerToGenType(module, basePackageName, typeBuilder, childOf, (ContainerSchemaNode) node,
                        schemaContext, verboseClassComments, genCtx, genTypeBuilders);
            }
        }

    }

    private static void containerToGenType(final Module module, final String basePackageName,
        final GeneratedTypeBuilder parent, final GeneratedTypeBuilder childOf, final ContainerSchemaNode node,
        final SchemaContext schemaContext, final boolean verboseClassComments, final Map<Module, ModuleContext> genCtx,
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders) {

        final GeneratedTypeBuilder genType = processDataSchemaNode(module, basePackageName, childOf, node,
                schemaContext, verboseClassComments, genCtx, genTypeBuilders);
        if (genType != null) {
            constructGetter(parent, node.getQName().getLocalName(), node.getDescription(), genType, node.getStatus());
            resolveDataSchemaNodes(module, basePackageName, genType, genType, node.getChildNodes(), genCtx,
                    schemaContext, verboseClassComments, genTypeBuilders);
        }
    }

    private static GeneratedTypeBuilder processDataSchemaNode(final Module module, final String basePackageName,
        final GeneratedTypeBuilder childOf, final DataSchemaNode node, final SchemaContext schemaContext,
        final boolean verboseClassComments, final Map<Module, ModuleContext> genCtx, final Map<String, Map<String,
        GeneratedTypeBuilder>> genTypeBuilders) {

        if (node.isAugmenting() || node.isAddedByUses()) {
            return null;
        }
        final String packageName = packageNameForGeneratedType(basePackageName, node.getPath(), BindingNamespaceType.Data);
        final GeneratedTypeBuilder genType = addDefaultInterfaceDefinition(packageName, node, childOf, module,
                genCtx, schemaContext, verboseClassComments, genTypeBuilders);
        genType.addComment(node.getDescription());
        annotateDeprecatedIfNecessary(node.getStatus(), genType);
        genType.setDescription(createDescription(node, genType.getFullyQualifiedName(), schemaContext, verboseClassComments));
        genType.setModuleName(module.getName());
        genType.setReference(node.getReference());
        genType.setSchemaPath((List) node.getPath().getPathFromRoot());
        if (node instanceof DataNodeContainer) {
            genCtx.get(module).addChildNodeType(node, genType);
            //TODO: implement groupings to GTO building first
            // groupingsToGenTypes(module, ((DataNodeContainer) node).getGroupings());
            processUsesAugments(schemaContext, (DataNodeContainer) node, module, genCtx, genTypeBuilders,
                    verboseClassComments);
        }
        return genType;
    }

}
