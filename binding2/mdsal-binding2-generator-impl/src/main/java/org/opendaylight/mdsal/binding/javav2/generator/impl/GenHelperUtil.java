/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.impl;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.opendaylight.mdsal.binding.javav2.generator.impl.txt.yangTemplateForModule;
import org.opendaylight.mdsal.binding.javav2.generator.impl.txt.yangTemplateForNode;
import org.opendaylight.mdsal.binding.javav2.generator.impl.util.YangTextTemplate;
import org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil;
import org.opendaylight.mdsal.binding.javav2.generator.util.BindingTypes;
import org.opendaylight.mdsal.binding.javav2.generator.util.Types;
import org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder.GeneratedTypeBuilderImpl;
import org.opendaylight.mdsal.binding.javav2.model.api.Constant;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentable;
import org.opendaylight.mdsal.binding.javav2.util.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;

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
    static GeneratedTypeBuilder moduleTypeBuilder(final Module module, final String postfix, final boolean verboseClassComments) {
        Preconditions.checkArgument(module != null, "Module reference cannot be NULL.");
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
        final String moduleDescription = BindingGeneratorUtil.encodeAngleBrackets(module.getDescription());
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
            sb.append(BindingGeneratorUtil.encodeAngleBrackets(yangTemplateForModule.render(module).body()));
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
                          final GeneratedTypeBuilder parent, final GeneratedTypeBuilder childOf, final Iterable<DataSchemaNode> schemaNodes) {
        if (schemaNodes != null && parent != null) {
            for (final DataSchemaNode schemaNode : schemaNodes) {
                if (!schemaNode.isAugmenting() && !schemaNode.isAddedByUses()) {
                    //TODO: design decomposition and implement it
                    //addSchemaNodeToBuilderAsMethod(basePackageName, schemaNode, parent, childOf, module);
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
     * @param unknownSchemaNodes
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
     * @param schemaContext
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
        Preconditions.checkArgument(schemaNode != null, "Data Schema Node cannot be NULL.");
        Preconditions.checkArgument(packageName != null, "Package Name for Generated Type cannot be NULL.");
        Preconditions.checkArgument(schemaNode.getQName() != null, "QName for Data Schema Node cannot be NULL.");
        final String schemaNodeName = schemaNode.getQName().getLocalName();
        Preconditions.checkArgument(schemaNodeName != null, "Local Name of QName for Data Schema Node cannot be NULL.");

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

        //FIXME: update genTypeBuilders for callers
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

    private static Constant qNameConstant(final GeneratedTypeBuilderBase<?> toBuilder, final String constantName,
                                          final QName name) {
        return toBuilder.addConstant(Types.typeForClass(QName.class), constantName, name);
    }

    private static String createDescription(final SchemaNode schemaNode, final String fullyQualifiedName,
                             final SchemaContext schemaContext, final boolean verboseClassComments) {
        final StringBuilder sb = new StringBuilder();
        final String nodeDescription = BindingGeneratorUtil.encodeAngleBrackets(schemaNode.getDescription());
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
            sb.append(BindingGeneratorUtil.encodeAngleBrackets(yangTemplateForNode.render(schemaNode).body()));
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
