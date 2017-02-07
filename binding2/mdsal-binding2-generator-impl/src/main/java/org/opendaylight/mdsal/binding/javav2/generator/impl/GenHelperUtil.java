/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil.computeDefaultSUID;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil.encodeAngleBrackets;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil.packageNameForGeneratedType;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingTypes.IDENTIFIER;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingTypes.IDENTIFIABLE_ITEM;
import static org.opendaylight.mdsal.binding.javav2.generator.util.Types.BOOLEAN;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.opendaylight.mdsal.binding.javav2.generator.impl.txt.yangTemplateForModule;
import org.opendaylight.mdsal.binding.javav2.generator.impl.txt.yangTemplateForNode;
import org.opendaylight.mdsal.binding.javav2.generator.impl.util.YangTextTemplate;
import org.opendaylight.mdsal.binding.javav2.generator.util.BindingTypes;
import org.opendaylight.mdsal.binding.javav2.generator.util.Types;
import org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder.GeneratedPropertyBuilderImpl;
import org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder.GeneratedTOBuilderImpl;
import org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder.GeneratedTypeBuilderImpl;
import org.opendaylight.mdsal.binding.javav2.model.api.Constant;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingNamespaceType;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentable;
import org.opendaylight.mdsal.binding.javav2.util.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
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
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.model.util.SchemaNodeUtils;
import org.opendaylight.yangtools.yang.model.util.type.CompatUtils;

/**
 * Helper util class used for generation of types in binding spec v2.
 */
@Beta
final class GenHelperUtil {

    private static final Pattern UNICODE_CHAR_PATTERN = Pattern.compile("\\\\+u");
    private static final Splitter BSDOT_SPLITTER = Splitter.on("\\.");
    private static final char NEW_LINE = '\n';

    /**
     * Constant with the concrete name of identifier.
     */
    private static final String AUGMENT_IDENTIFIER_NAME = "augment-identifier";

    /**
     * Constant with the concrete name of namespace.
     */
    private static final String YANG_EXT_NAMESPACE = "urn:opendaylight:yang:extension:yang-ext";

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
    static GeneratedTypeBuilder moduleToDataType(final Module module, Map<Module, ModuleContext> genCtx, final boolean verboseClassComments) {
        checkArgument(module != null, "Module reference cannot be NULL.");

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
    static GeneratedTypeBuilder moduleTypeBuilder(final Module module, final String postfix, final boolean verboseClassComments) {
        checkArgument(module != null, "Module reference cannot be NULL.");
        final String packageName = BindingMapping.getRootPackageName(module);
        final String moduleName = BindingMapping.getClassName(module.getName()) + postfix;

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
     * @param genCtx
     * @return generated type builder with all implemented types
     */
    private static GeneratedTypeBuilder addImplementedInterfaceFromUses(final DataNodeContainer dataNodeContainer,
                          final GeneratedTypeBuilder builder, Map<Module, ModuleContext> genCtx) {
        for (final UsesNode usesNode : dataNodeContainer.getUses()) {
            if (usesNode.getGroupingPath() != null) {
                final GeneratedType genType = findGroupingByPath(usesNode.getGroupingPath(), genCtx).toInstance();
                if (genType == null) {
                    throw new IllegalStateException("Grouping " + usesNode.getGroupingPath() + "is not resolved for "
                            + builder.getName());
                }

                builder.addImplementsType(genType);
            }
        }
        return builder;
    }

     static GeneratedTypeBuilder findGroupingByPath(final SchemaPath path, Map<Module, ModuleContext> genCtx) {
        for (final ModuleContext ctx : genCtx.values()) {
            final GeneratedTypeBuilder result = ctx.getGrouping(path);
            if (result != null) {
                return result;
            }
        }
        return null;
     }

    private static String createDescription(final Module module, final boolean verboseClassComments) {
        final StringBuilder sb = new StringBuilder();
        final String moduleDescription = encodeAngleBrackets(module.getDescription());
        final String formattedDescription = YangTextTemplate.formatToParagraph(moduleDescription, 0);

        if (!Strings.isNullOrEmpty(formattedDescription)) {
            sb.append(formattedDescription);
            sb.append(NEW_LINE);
        }

        if (verboseClassComments) {
            sb.append("<p>");
            sb.append("This class represents the following YANG schema fragment defined in module <b>");
            sb.append(module.getName());
            sb.append("</b>");
            sb.append(NEW_LINE);
            sb.append("<pre>");
            sb.append(NEW_LINE);
            sb.append(encodeAngleBrackets(yangTemplateForModule.render(module).body()));
            sb.append("</pre>");
        }

        return replaceAllIllegalChars(sb);
    }

    @VisibleForTesting
    public static String replaceAllIllegalChars(final StringBuilder stringBuilder){
        final String ret = UNICODE_CHAR_PATTERN.matcher(stringBuilder).replaceAll("\\\\\\\\u");
        return ret.isEmpty() ? "" : ret;
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
                          final Iterable<DataSchemaNode> schemaNodes, Map<Module, ModuleContext> genCtx,
                          final SchemaContext schemaContext, final boolean verboseClassComments,
                          Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders) {
        if (schemaNodes != null && parent != null) {
            for (final DataSchemaNode schemaNode : schemaNodes) {
                if (!schemaNode.isAugmenting() && !schemaNode.isAddedByUses()) {
                    addSchemaNodeToBuilderAsMethod(basePackageName, schemaNode, parent, childOf, module, genCtx,
                            schemaContext, verboseClassComments, genTypeBuilders);
                }
            }
        }
        return parent;
    }

    static Map<Module, ModuleContext> processUsesAugments(final SchemaContext schemaContext, final
                        DataNodeContainer node, final Module module, Map<Module, ModuleContext> genCtx,  Map<String,
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

    static GeneratedTypeBuilder findChildNodeByPath(final SchemaPath path, Map<Module, ModuleContext> genCtx) {
        for (final ModuleContext ctx : genCtx.values()) {
            final GeneratedTypeBuilder result = ctx.getChildNode(path);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    static GeneratedTypeBuilder findCaseByPath(final SchemaPath path, Map<Module, ModuleContext> genCtx) {
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
                Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders, Map<Module, ModuleContext> genCtx) {

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
     * @param unknownSchemaNodes unknows schema nodes
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

    static GeneratedTypeBuilder addDefaultInterfaceDefinition(final String packageName, final SchemaNode
            schemaNode, final Module module, Map<Module, ModuleContext> genCtx, final SchemaContext schemaContext,
            final boolean verboseClassComments, Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders) {
        return addDefaultInterfaceDefinition(packageName, schemaNode, null, module, genCtx, schemaContext,
                verboseClassComments, genTypeBuilders);
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
            schemaNode, final Type parent, final Module module, Map<Module, ModuleContext> genCtx,
            final SchemaContext schemaContext, final boolean verboseClassComments, Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders) {
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
                       Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders) {
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
        Map<Module, ModuleContext> genCtx, final SchemaContext schemaContext, final boolean verboseClassComments,
        Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders) {
        //TODO: implement rest of schema nodes GTO building
        if (node != null && typeBuilder != null) {
            if (node instanceof ContainerSchemaNode) {
                containerToGenType(module, basePackageName, typeBuilder, childOf, (ContainerSchemaNode) node,
                        schemaContext, verboseClassComments, genCtx, genTypeBuilders);
            } else if (node instanceof ListSchemaNode) {
                listToGenType(module, basePackageName, typeBuilder, childOf, (ListSchemaNode) node,
                        schemaContext, verboseClassComments, genCtx, genTypeBuilders);
            }
        }

    }

    private static void containerToGenType(final Module module, final String basePackageName,
        final GeneratedTypeBuilder parent, final GeneratedTypeBuilder childOf, final ContainerSchemaNode node,
        final SchemaContext schemaContext, final boolean verboseClassComments, Map<Module, ModuleContext> genCtx,
        Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders) {

        final GeneratedTypeBuilder genType = processDataSchemaNode(module, basePackageName, childOf, node,
                schemaContext, verboseClassComments, genCtx, genTypeBuilders);
        if (genType != null) {
            constructGetter(parent, node.getQName().getLocalName(), node.getDescription(), genType, node.getStatus());
            resolveDataSchemaNodes(module, basePackageName, genType, genType, node.getChildNodes(), genCtx,
                    schemaContext, verboseClassComments, genTypeBuilders);
        }
    }

    private static void listToGenType(final Module module, final String basePackageName, final GeneratedTypeBuilder
            parent, final GeneratedTypeBuilder childOf, final ListSchemaNode node, final SchemaContext schemaContext,
            final boolean verboseClassComments, Map<Module, ModuleContext> genCtx,
            Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders) {

        final GeneratedTypeBuilder genType = processDataSchemaNode(module, basePackageName, childOf, node,
                schemaContext, verboseClassComments, genCtx, genTypeBuilders);
        if (genType != null) {
            constructGetter(parent, node.getQName().getLocalName(), node.getDescription(),
                    Types.listTypeFor(genType), node.getStatus());

            final List<String> listKeys = listKeys(node);
            final String packageName = packageNameForGeneratedType(basePackageName, node.getPath(), BindingNamespaceType.Data);
            final GeneratedTOBuilder genTOBuilder = resolveListKeyTOBuilder(packageName, node);
            if (genTOBuilder != null) {
                final Type identifierMarker = Types.parameterizedTypeFor(IDENTIFIER, genType);
                final Type identifiableMarker = Types.parameterizedTypeFor(IDENTIFIABLE_ITEM, genTOBuilder);
                genTOBuilder.addImplementsType(identifierMarker);
                genType.addImplementsType(identifiableMarker);
            }

            for (final DataSchemaNode schemaNode : node.getChildNodes()) {
                if (!schemaNode.isAugmenting()) {
                    addSchemaNodeToListBuilders(basePackageName, schemaNode, genType, genTOBuilder, listKeys, module,
                            schemaContext, verboseClassComments, genCtx, genTypeBuilders);
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
    private static void addSchemaNodeToListBuilders(final String basePackageName, final DataSchemaNode schemaNode,
        final GeneratedTypeBuilder typeBuilder, final GeneratedTOBuilder genTOBuilder, final List<String> listKeys,
        final Module module, final SchemaContext schemaContext, final boolean verboseClassComments, Map<Module, ModuleContext> genCtx,
        Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders) {

        checkArgument(schemaNode != null, "Data Schema Node cannot be NULL.");
        checkArgument(typeBuilder != null, "Generated Type Builder cannot be NULL.");

        if (schemaNode instanceof LeafSchemaNode) {
            final LeafSchemaNode leaf = (LeafSchemaNode) schemaNode;
            final String leafName = leaf.getQName().getLocalName();
            final Type type = null; //TODO: resolveLeafSchemaNodeAsMethod(typeBuilder, leaf, module);
            if (listKeys.contains(leafName)) {
                if (type == null) {
                    resolveLeafSchemaNodeAsProperty(genTOBuilder, leaf, true, module, schemaContext, genCtx);
                } else {
                    resolveLeafSchemaNodeAsProperty(genTOBuilder, leaf, type, true);
                }
            }
        } else if (!schemaNode.isAddedByUses()) {
            //TODO: implement rest of schema nodes GTO building
            if (schemaNode instanceof ContainerSchemaNode) {
                containerToGenType(module, basePackageName, typeBuilder, typeBuilder, (ContainerSchemaNode) schemaNode,
                        schemaContext, verboseClassComments, genCtx, genTypeBuilders);
            } else if (schemaNode instanceof ListSchemaNode) {
                listToGenType(module, basePackageName, typeBuilder, typeBuilder, (ListSchemaNode) schemaNode,
                        schemaContext, verboseClassComments, genCtx, genTypeBuilders);
            }
        }
    }

    private static GeneratedTypeBuilder processDataSchemaNode(final Module module, final String basePackageName,
        final GeneratedTypeBuilder childOf, final DataSchemaNode node, final SchemaContext schemaContext,
        final boolean verboseClassComments, Map<Module, ModuleContext> genCtx,
        Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders) {

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



    private static boolean resolveLeafSchemaNodeAsProperty(final GeneratedTOBuilder toBuilder, final LeafSchemaNode
            leaf,
        final boolean isReadOnly, final Module module, final SchemaContext schemaContext, Map<Module, ModuleContext> genCtx) {
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
                //TODO: this needs to be passed in
                //returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, leaf);
                returnType = null;
            }
            return resolveLeafSchemaNodeAsProperty(toBuilder, leaf, returnType, isReadOnly);
        }
        return false;
    }

    private static void typeBuildersToGenTypes(final Module module, final GeneratedTypeBuilder typeBuilder,
        final GeneratedTOBuilder genTOBuilder, Map<Module, ModuleContext> genCtx) {
        checkArgument(typeBuilder != null, "Generated Type Builder cannot be NULL.");

        if (genTOBuilder != null) {
            final GeneratedTransferObject genTO = genTOBuilder.toInstance();
            //TODO make sure this is correct
            constructGetter(typeBuilder, "key", "Returns Primary Key of Yang List Type", genTO, Status.CURRENT);
            genCtx.get(module).addGeneratedTOBuilder(genTOBuilder);
        }
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

    /**
     * Created a method signature builder as part of
     * <code>interfaceBuilder</code>.
     *
     * The method signature builder is created for the getter method of
     * <code>schemaNodeName</code>. Also <code>comment</code> and
     * <code>returnType</code> information are added to the builder.
     *
     * @param interfaceBuilder
     *            generated type builder for which the getter method should be
     *            created
     * @param schemaNodeName
     *            string with schema node name. The name will be the part of the
     *            getter method name.
     * @param comment
     *            string with comment for the getter method
     * @param returnType
     *            type which represents the return type of the getter method
     * @param status
     *            status from yang file, for deprecated annotation
     * @return method signature builder which represents the getter method of
     *         <code>interfaceBuilder</code>
     */
    private static MethodSignatureBuilder constructGetter(final GeneratedTypeBuilder interfaceBuilder,
                                                          final String schemaNodeName, final String comment, final Type returnType, final Status status) {

        final MethodSignatureBuilder getMethod = interfaceBuilder
                .addMethod(getterMethodName(schemaNodeName, returnType));
        if (status == Status.DEPRECATED) {
            getMethod.addAnnotation("", "Deprecated");
        }
        getMethod.setComment(encodeAngleBrackets(comment));
        getMethod.setReturnType(returnType);
        return getMethod;
    }

    /**
     * Creates the name of the getter method name from <code>localName</code>.
     *
     * @param localName
     *            string with the name of the getter method
     * @param returnType
     *            return type
     * @return string with the name of the getter method for
     *         <code>methodName</code> in JAVA method format
     */
    private static String getterMethodName(final String localName, final Type returnType) {
        final StringBuilder method = new StringBuilder();
        if (BOOLEAN.equals(returnType)) {
            method.append("is");
        } else {
            method.append("get");
        }
        final String name = BindingMapping.toFirstUpper(BindingMapping.getPropertyName(localName));
        method.append(name);
        return method.toString();
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
    private static List<String> listKeys(final ListSchemaNode list) {
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
    private static GeneratedTOBuilder resolveListKeyTOBuilder(final String packageName, final ListSchemaNode list) {
        GeneratedTOBuilder genTOBuilder = null;
        //TODO: implement properly namespace for keys as defined in binding v2 spec
        if ((list.getKeyDefinition() != null) && (!list.getKeyDefinition().isEmpty())) {
            final String listName = list.getQName().getLocalName() + "Key";
            final String genTOName = BindingMapping.getClassName(listName);
            genTOBuilder = new GeneratedTOBuilderImpl(packageName, genTOName);
        }
        return genTOBuilder;
    }

    private static Constant qNameConstant(final GeneratedTypeBuilderBase<?> toBuilder, final String constantName,
                                          final QName name) {
        return toBuilder.addConstant(Types.typeForClass(QName.class), constantName, name);
    }

    private static String createDescription(final SchemaNode schemaNode, final String fullyQualifiedName,
                             final SchemaContext schemaContext, final boolean verboseClassComments) {
        final StringBuilder sb = new StringBuilder();
        final String nodeDescription = encodeAngleBrackets(schemaNode.getDescription());
        final String formattedDescription = YangTextTemplate.formatToParagraph(nodeDescription, 0);

        if (!Strings.isNullOrEmpty(formattedDescription)) {
            sb.append(formattedDescription);
            sb.append(NEW_LINE);
        }

        if (verboseClassComments) {
            final Module module = SchemaContextUtil.findParentModule(schemaContext, schemaNode);
            final StringBuilder linkToBuilderClass = new StringBuilder();
            final String[] namespace = Iterables.toArray(BSDOT_SPLITTER.split(fullyQualifiedName), String.class);
            final String className = namespace[namespace.length - 1];

            if (hasBuilderClass(schemaNode)) {
                linkToBuilderClass.append(className);
                linkToBuilderClass.append("Builder");
            }

            sb.append("<p>");
            sb.append("This class represents the following YANG schema fragment defined in module <b>");
            sb.append(module.getName());
            sb.append("</b>");
            sb.append(NEW_LINE);
            sb.append("<pre>");
            sb.append(NEW_LINE);
            sb.append(encodeAngleBrackets(yangTemplateForNode.render(schemaNode).body()));
            sb.append("</pre>");
            sb.append(NEW_LINE);
            sb.append("The schema path to identify an instance is");
            sb.append(NEW_LINE);
            sb.append("<i>");
            sb.append(YangTextTemplate.formatSchemaPath(module.getName(), schemaNode.getPath().getPathFromRoot()));
            sb.append("</i>");
            sb.append(NEW_LINE);

            if (hasBuilderClass(schemaNode)) {
                sb.append(NEW_LINE);
                sb.append("<p>To create instances of this class use " + "{@link " + linkToBuilderClass + "}.");
                sb.append(NEW_LINE);
                sb.append("@see ");
                sb.append(linkToBuilderClass);
                sb.append(NEW_LINE);
                if (schemaNode instanceof ListSchemaNode) {
                    final List<QName> keyDef = ((ListSchemaNode)schemaNode).getKeyDefinition();
                    if (keyDef != null && !keyDef.isEmpty()) {
                        sb.append("@see ");
                        sb.append(className);
                        sb.append("Key");
                    }
                    sb.append(NEW_LINE);
                }
            }
        }

        return replaceAllIllegalChars(sb);
    }

    private static void annotateDeprecatedIfNecessary(final Status status, final GeneratedTypeBuilder builder) {
        if (status == Status.DEPRECATED) {
            builder.addAnnotation("", "Deprecated");
        }
    }

    private static boolean hasBuilderClass(final SchemaNode schemaNode) {
        if (schemaNode instanceof ContainerSchemaNode || schemaNode instanceof ListSchemaNode ||
                schemaNode instanceof RpcDefinition || schemaNode instanceof NotificationDefinition) {
            return true;
        }
        return false;
    }

}
