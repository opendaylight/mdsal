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
import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.augGenTypeName;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.constructGetter;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.createDescription;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.createReturnTypeForUnion;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.getAugmentIdentifier;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.isInnerType;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.qNameConstant;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.resolveInnerEnumFromTypeDefinition;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.resolveListKeyTOBuilder;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil.computeDefaultSUID;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil.encodeAngleBrackets;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil.packageNameForGeneratedType;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingTypes.NOTIFICATION;
import static org.opendaylight.mdsal.binding.javav2.generator.util.Types.parameterizedTypeFor;
import static org.opendaylight.yangtools.yang.model.util.SchemaContextUtil.findParentModule;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opendaylight.mdsal.binding.javav2.generator.spi.TypeProvider;
import org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil;
import org.opendaylight.mdsal.binding.javav2.generator.util.BindingTypes;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifier;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifierNormalizer;
import org.opendaylight.mdsal.binding.javav2.generator.util.Types;
import org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder.GeneratedPropertyBuilderImpl;
import org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder.GeneratedTypeBuilderImpl;
import org.opendaylight.mdsal.binding.javav2.generator.yang.types.GroupingDefinitionDependencySort;
import org.opendaylight.mdsal.binding.javav2.generator.yang.types.TypeProviderImpl;
import org.opendaylight.mdsal.binding.javav2.model.api.AccessModifier;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.javav2.model.api.Restrictions;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.EnumBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTOBuilder;
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
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
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
    static GeneratedTypeBuilder moduleTypeBuilder(final Module module, final String postfix, final boolean
            verboseClassComments) {
        Preconditions.checkArgument(module != null, "Module reference cannot be NULL.");
        final String packageName = BindingMapping.getRootPackageName(module);
        // underscore used as separator for distinction of module name parts
        final String moduleName = new StringBuilder(module.getName()).append('_').append(postfix).toString();

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
                          final TypeProvider typeProvider) {

        if (schemaNodes != null && parent != null) {
            for (final DataSchemaNode schemaNode : schemaNodes) {
                if (!schemaNode.isAugmenting() && !schemaNode.isAddedByUses()) {
                    addSchemaNodeToBuilderAsMethod(basePackageName, schemaNode, parent, childOf, module, genCtx,
                            schemaContext, verboseClassComments, genTypeBuilders, typeProvider);
                }
            }
        }
        return parent;
    }

    static GeneratedTypeBuilder addDefaultInterfaceDefinition(final String packageName, final SchemaNode
            schemaNode, final Module module, final Map<Module, ModuleContext> genCtx, final SchemaContext schemaContext,
            final boolean verboseClassComments, final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders,
            final TypeProvider typeProvider) {
        return addDefaultInterfaceDefinition(packageName, schemaNode, null, module, genCtx, schemaContext,
                verboseClassComments, genTypeBuilders, typeProvider);
    }

    static Map<Module, ModuleContext> processUsesAugments(final SchemaContext schemaContext, final
                        DataNodeContainer node, final Module module, Map<Module, ModuleContext> genCtx,
                        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders,
                        final boolean verboseClassComments, final TypeProvider typeProvider) {
        final String basePackageName = BindingMapping.getRootPackageName(module);
        for (final UsesNode usesNode : node.getUses()) {
            for (final AugmentationSchema augment : usesNode.getAugmentations()) {
                genCtx = AugmentToGenType.usesAugmentationToGenTypes(schemaContext, basePackageName, augment, module,
                        usesNode, node, genCtx, genTypeBuilders, verboseClassComments, typeProvider);
                genCtx = processUsesAugments(schemaContext, augment, module, genCtx, genTypeBuilders,
                        verboseClassComments, typeProvider);
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
                final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders, final Map<Module,
                ModuleContext> genCtx, final SchemaContext schemaContext, final boolean verboseClassComments, final
                TypeProvider typeProvider) {

        Map<String, GeneratedTypeBuilder> augmentBuilders = genTypeBuilders.computeIfAbsent(augmentPackageName, k -> new HashMap<>());
        String augIdentifier = getAugmentIdentifier(augSchema.getUnknownSchemaNodes());

        if (augIdentifier == null) {
            augIdentifier = augGenTypeName(augmentBuilders, targetTypeRef.getName());
        }

        GeneratedTypeBuilder augTypeBuilder = new GeneratedTypeBuilderImpl(augmentPackageName, augIdentifier);

        augTypeBuilder.addImplementsType(BindingTypes.TREE_NODE);
        augTypeBuilder.addImplementsType(parameterizedTypeFor(BindingTypes.INSTANTIABLE, augTypeBuilder));
        augTypeBuilder.addImplementsType(Types.augmentationTypeFor(targetTypeRef));
        annotateDeprecatedIfNecessary(augSchema.getStatus(), augTypeBuilder);
        augTypeBuilder = addImplementedInterfaceFromUses(augSchema, augTypeBuilder, genCtx);

        augTypeBuilder = augSchemaNodeToMethods(module, basePackageName, augTypeBuilder, augTypeBuilder, augSchema
                .getChildNodes(), genCtx, schemaContext, verboseClassComments, typeProvider, genTypeBuilders);
        augmentBuilders.put(augTypeBuilder.getName(), augTypeBuilder);

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
            final GeneratedTypeBuilder typeBuilder, final GeneratedTypeBuilder childOf, final Iterable<DataSchemaNode> schemaNodes,
            final Map<Module, ModuleContext> genCtx, final SchemaContext schemaContext, final boolean
            verboseClassComments, final TypeProvider typeProvider, final Map<String, Map<String,
            GeneratedTypeBuilder>> genTypeBuilders) {
        if (schemaNodes != null && typeBuilder != null) {
            for (final DataSchemaNode schemaNode : schemaNodes) {
                if (!schemaNode.isAugmenting()) {
                    addSchemaNodeToBuilderAsMethod(basePackageName, schemaNode, typeBuilder, childOf, module, genCtx,
                            schemaContext, verboseClassComments, genTypeBuilders, typeProvider);
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
    static GeneratedTypeBuilder addDefaultInterfaceDefinition(final String packageName, final SchemaNode
            schemaNode, final Type parent, final Module module, final Map<Module, ModuleContext> genCtx,
            final SchemaContext schemaContext, final boolean verboseClassComments, final Map<String, Map<String,
            GeneratedTypeBuilder>> genTypeBuilders, final TypeProvider typeProvider) {

        GeneratedTypeBuilder it = addRawInterfaceDefinition(packageName, schemaNode, schemaContext, "",
                verboseClassComments, genTypeBuilders);
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
                        verboseClassComments, genTypeBuilders, typeProvider);
        annotateDeprecatedIfNecessary(notification.getStatus(), notificationInterface);
        notificationInterface.addImplementsType(NOTIFICATION);
        genCtx.get(module).addChildNodeType(notification, notificationInterface);

        // Notification object
        resolveDataSchemaNodes(module, basePackageName, notificationInterface,
                notificationInterface, notification.getChildNodes(), genCtx, schemaContext,
                verboseClassComments, genTypeBuilders, typeProvider);

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
     * @param packageName
     *            string with the package name to which returning generated type
     *            builder belongs
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
    static GeneratedTypeBuilder addRawInterfaceDefinition(final String packageName, final SchemaNode schemaNode,
                       final SchemaContext schemaContext, final String prefix, final boolean verboseClassComments,
                       final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders) {

        Preconditions.checkArgument(schemaNode != null, "Data Schema Node cannot be NULL.");
        Preconditions.checkArgument(packageName != null, "Package Name for Generated Type cannot be NULL.");
        String schemaNodeName = schemaNode.getQName().getLocalName();
        Preconditions.checkArgument(schemaNodeName != null, "Local Name of QName for Data Schema Node cannot be NULL.");

        if (prefix != null && !prefix.isEmpty()) {
            // underscore used as separator for distinction of class name parts
            schemaNodeName = new StringBuilder(prefix).append('_').append(schemaNodeName).toString();
        }

        final GeneratedTypeBuilderImpl newType = new GeneratedTypeBuilderImpl(packageName, schemaNodeName);
        final Module module = SchemaContextUtil.findParentModule(schemaContext, schemaNode);
        qNameConstant(newType, BindingMapping.QNAME_STATIC_FIELD_NAME, schemaNode.getQName());
        newType.addComment(schemaNode.getDescription());
        newType.setDescription(createDescription(schemaNode, newType.getFullyQualifiedName(), schemaContext, verboseClassComments));
        newType.setReference(schemaNode.getReference());
        newType.setSchemaPath((List<QName>) schemaNode.getPath().getPathFromRoot());
        newType.setModuleName(module.getName());

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
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders, final TypeProvider typeProvider) {
        //TODO: implement rest of schema nodes GTO building
        if (node != null && typeBuilder != null) {
            if (node instanceof ContainerSchemaNode) {
                containerToGenType(module, basePackageName, typeBuilder, childOf, (ContainerSchemaNode) node,
                        schemaContext, verboseClassComments, genCtx, genTypeBuilders, typeProvider);
            } else if (node instanceof LeafSchemaNode) {
                resolveLeafSchemaNodeAsMethod(schemaContext, typeBuilder, genCtx, (LeafSchemaNode) node, module,
                        typeProvider);
            } else if (node instanceof ListSchemaNode) {
                listToGenType(module, basePackageName, typeBuilder, childOf, (ListSchemaNode) node, schemaContext,
                        verboseClassComments, genCtx, genTypeBuilders, typeProvider);
            }
        }

    }

    private static void containerToGenType(final Module module, final String basePackageName,
        final GeneratedTypeBuilder parent, final GeneratedTypeBuilder childOf, final ContainerSchemaNode node,
        final SchemaContext schemaContext, final boolean verboseClassComments, final Map<Module, ModuleContext> genCtx,
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders, final TypeProvider typeProvider) {

        final GeneratedTypeBuilder genType = processDataSchemaNode(module, basePackageName, childOf, node,
                schemaContext, verboseClassComments, genCtx, genTypeBuilders, typeProvider);
        if (genType != null) {
            constructGetter(parent, node.getQName().getLocalName(), node.getDescription(), genType, node.getStatus());
            resolveDataSchemaNodes(module, basePackageName, genType, genType, node.getChildNodes(), genCtx,
                    schemaContext, verboseClassComments, genTypeBuilders, typeProvider);
        }
    }

    private static void listToGenType(final Module module, final String basePackageName, final GeneratedTypeBuilder
            parent, final GeneratedTypeBuilder childOf, final ListSchemaNode node, final SchemaContext schemaContext,
            final boolean verboseClassComments, final Map<Module, ModuleContext> genCtx,
            final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders, final TypeProvider typeProvider) {
        final GeneratedTypeBuilder genType = processDataSchemaNode(module, basePackageName, childOf, node,
                schemaContext, verboseClassComments, genCtx, genTypeBuilders, typeProvider);
        if (genType != null) {
            final String nodeName = node.getQName().getLocalName();
            constructGetter(parent, nodeName, node.getDescription(),
                    Types.listTypeFor(genType), node.getStatus());
            final List<QName> listKeys = node.getKeyDefinition();
            final String packageName = new StringBuilder(packageNameForGeneratedType(basePackageName, node.getPath(),
                    BindingNamespaceType.Key)).append('.').append(nodeName).toString();

            final GeneratedTOBuilder genTOBuilder = resolveListKeyTOBuilder(packageName, node);

            for (final DataSchemaNode schemaNode : node.getChildNodes()) {
                if (!schemaNode.isAugmenting()) {
                    addSchemaNodeToListBuilders(nodeName, basePackageName, schemaNode, genType, genTOBuilder, listKeys,
                            module, typeProvider, schemaContext, genCtx, genTypeBuilders, verboseClassComments);
                }
            }

            // serialVersionUID
            if (genTOBuilder != null) {
                final GeneratedPropertyBuilder prop = new GeneratedPropertyBuilderImpl("serialVersionUID");
                prop.setValue(Long.toString(computeDefaultSUID(genTOBuilder)));
                genTOBuilder.setSUID(prop);
            }

            typeBuildersToGenTypes(module, genType, genTOBuilder, genCtx);
        }
    }

    private static void typeBuildersToGenTypes(final Module module, final GeneratedTypeBuilder typeBuilder,
            final GeneratedTOBuilder genTOBuilder, final Map<Module, ModuleContext> genCtx) {
        checkArgument(typeBuilder != null, "Generated Type Builder cannot be NULL.");
        if (genTOBuilder != null) {
            final GeneratedTransferObject genTO = genTOBuilder.toInstance();
            constructGetter(typeBuilder, "key", "Returns Primary Key of Yang List Type", genTO, Status.CURRENT);
            genCtx.get(module).addGeneratedTOBuilder(genTOBuilder);
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
    private static Type resolveLeafSchemaNodeAsMethod(final SchemaContext schemaContext, final GeneratedTypeBuilder
            typeBuilder, final Map<Module, ModuleContext> genCtx, final LeafSchemaNode leaf, final Module module,
            final TypeProvider typeProvider) {
        if (leaf == null || typeBuilder == null || leaf.isAddedByUses()) {
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
                returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, leaf);
                final EnumTypeDefinition enumTypeDef = (EnumTypeDefinition) typeDef;
                final EnumBuilder enumBuilder = resolveInnerEnumFromTypeDefinition(enumTypeDef, leaf.getQName(),
                        genCtx, typeBuilder, module);
                if (enumBuilder != null) {
                    returnType = enumBuilder.toInstance(typeBuilder);
                }
                ((TypeProviderImpl) typeProvider).putReferencedType(leaf.getPath(), returnType);
            } else if (typeDef instanceof UnionTypeDefinition) {
                final GeneratedTOBuilder genTOBuilder = addTOToTypeBuilder(typeDef, typeBuilder, leaf, parentModule,
                        typeProvider, schemaContext);
                if (genTOBuilder != null) {
                    //TODO: https://bugs.opendaylight.org/show_bug.cgi?id=2289
                    returnType = createReturnTypeForUnion(genTOBuilder, typeDef, typeBuilder, parentModule, typeProvider);
                }
            } else if (typeDef instanceof BitsTypeDefinition) {
                final GeneratedTOBuilder genTOBuilder = addTOToTypeBuilder(typeDef, typeBuilder, leaf, parentModule,
                        typeProvider, schemaContext);
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
                        restrictions);
            }
        } else {
            final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(typeDef);
            returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, leaf, restrictions);
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

        constructGetter(typeBuilder, leafName, leafDesc, returnType, leaf.getStatus());
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
            final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders, final boolean verboseClassComments) {
        checkArgument(schemaNode != null, "Data Schema Node cannot be NULL.");
        checkArgument(typeBuilder != null, "Generated Type Builder cannot be NULL.");

        if (schemaNode instanceof LeafSchemaNode) {
            final LeafSchemaNode leaf = (LeafSchemaNode) schemaNode;
            final QName leafQName = leaf.getQName();
            final String leafName = leafQName.getLocalName();
            String leafPckgName = basePackageName;
            boolean isKeyPart = false;
            if (listKeys.contains(leafQName)) {
                leafPckgName = new StringBuilder(leafPckgName).append('.').append(BindingNamespaceType.Key).append('.')
                        .append(nodeName).toString();
                isKeyPart = true;
            } else {
                leafPckgName = new StringBuilder(leafPckgName).append('.').append(BindingNamespaceType.Data).append('.')
                        .append(nodeName).toString();
            }

            final String leafGTOName = new StringBuilder(nodeName).append('_').append(leafName).toString();
            final GeneratedTypeBuilder leafGTp = new GeneratedTypeBuilderImpl(leafPckgName, leafGTOName);
            resolveLeafSchemaNodeAsMethod(schemaContext, leafGTp, genCtx, leaf, module,
                    typeProvider);

            constructGetter(typeBuilder, leafGTOName, schemaNode.getDescription(), leafGTp, Status.CURRENT);

            if (isKeyPart) {
                AuxiliaryGenUtils.resolveLeafSchemaNodeAsProperty(genTOBuilder, leaf, leafGTp, true);
            }
        } else if (!schemaNode.isAddedByUses()) {
            //TODO: implement leaf list to generated type
            //TODO: implement choice to generated type
            if (schemaNode instanceof ContainerSchemaNode) {
                containerToGenType(module, basePackageName, typeBuilder, typeBuilder, (ContainerSchemaNode) schemaNode,
                        schemaContext, verboseClassComments, genCtx, genTypeBuilders, typeProvider);
            } else if (schemaNode instanceof ListSchemaNode) {
                listToGenType(module, basePackageName, typeBuilder, typeBuilder, (ListSchemaNode) schemaNode,
                        schemaContext, verboseClassComments, genCtx, genTypeBuilders, typeProvider);
            }
        }
    }

    private static TypeDefinition<?> getBaseOrDeclaredType(final TypeDefinition<?> typeDef) {
        final TypeDefinition<?> baseType = typeDef.getBaseType();
        return (baseType != null && baseType.getBaseType() != null) ? baseType : typeDef;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static GeneratedTypeBuilder processDataSchemaNode(final Module module, final String basePackageName,
        final GeneratedTypeBuilder childOf, final DataSchemaNode node, final SchemaContext schemaContext,
        final boolean verboseClassComments, Map<Module, ModuleContext> genCtx, final Map<String, Map<String,
        GeneratedTypeBuilder>> genTypeBuilders, final TypeProvider typeProvider) {

        if (node.isAugmenting() || node.isAddedByUses()) {
            return null;
        }
        final String packageName = packageNameForGeneratedType(basePackageName, node.getPath(), BindingNamespaceType.Data);
        final GeneratedTypeBuilder genType = addDefaultInterfaceDefinition(packageName, node, childOf, module,
                genCtx, schemaContext, verboseClassComments, genTypeBuilders, typeProvider);
        genType.addComment(node.getDescription());
        annotateDeprecatedIfNecessary(node.getStatus(), genType);
        genType.setDescription(createDescription(node, genType.getFullyQualifiedName(), schemaContext, verboseClassComments));
        genType.setModuleName(module.getName());
        genType.setReference(node.getReference());
        genType.setSchemaPath((List) node.getPath().getPathFromRoot());
        genType.setParentTypeForBuilder(childOf);
        if (node instanceof DataNodeContainer) {
            genCtx.get(module).addChildNodeType(node, genType);
            genCtx = groupingsToGenTypes(module, ((DataNodeContainer) node).getGroupings(), genCtx, schemaContext,
                    verboseClassComments, genTypeBuilders, typeProvider);
            processUsesAugments(schemaContext, (DataNodeContainer) node, module, genCtx, genTypeBuilders,
                    verboseClassComments, typeProvider);
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
        final String packageName = packageNameForGeneratedType(basePackageName, grouping.getPath(), BindingNamespaceType.Grouping);
        final GeneratedTypeBuilder genType = addDefaultInterfaceDefinition(packageName, grouping, module, genCtx,
                schemaContext, verboseClassComments, genTypeBuilders, typeProvider);
        annotateDeprecatedIfNecessary(grouping.getStatus(), genType);
        genCtx.get(module).addGroupingType(grouping.getPath(), genType);
        resolveDataSchemaNodes(module, basePackageName, genType, genType, grouping.getChildNodes(), genCtx,
                schemaContext, verboseClassComments, genTypeBuilders, typeProvider);
        genCtx = groupingsToGenTypes(module, grouping.getGroupings(), genCtx, schemaContext, verboseClassComments,
                genTypeBuilders, typeProvider);
        genCtx = processUsesAugments(schemaContext, grouping, module, genCtx, genTypeBuilders, verboseClassComments,
                typeProvider);
        return genCtx;
    }
}
