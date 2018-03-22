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
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingTypes.IDENTIFIABLE;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingTypes.IDENTIFIER;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingTypes.NOTIFICATION;
import static org.opendaylight.mdsal.binding.javav2.generator.util.Types.parameterizedTypeFor;
import static org.opendaylight.mdsal.binding.javav2.generator.util.Types.wildcardTypeFor;
import static org.opendaylight.mdsal.binding.javav2.generator.yang.types.TypeGenHelper.resolveRegExpressions;
import static org.opendaylight.yangtools.yang.model.util.SchemaContextUtil.findParentModule;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding.javav2.generator.context.ModuleContext;
import org.opendaylight.mdsal.binding.javav2.generator.spi.TypeProvider;
import org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil;
import org.opendaylight.mdsal.binding.javav2.generator.util.BindingTypes;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifier;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifierNormalizer;
import org.opendaylight.mdsal.binding.javav2.generator.util.ReferencedTypeImpl;
import org.opendaylight.mdsal.binding.javav2.generator.util.TypeComments;
import org.opendaylight.mdsal.binding.javav2.generator.util.Types;
import org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder.GeneratedPropertyBuilderImpl;
import org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder.GeneratedTypeBuilderImpl;
import org.opendaylight.mdsal.binding.javav2.generator.yang.types.BaseYangTypes;
import org.opendaylight.mdsal.binding.javav2.generator.yang.types.GroupingDefinitionDependencySort;
import org.opendaylight.mdsal.binding.javav2.generator.yang.types.TypeProviderImpl;
import org.opendaylight.mdsal.binding.javav2.model.api.AccessModifier;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.javav2.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.javav2.model.api.Restrictions;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.YangSourceDefinition;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.EnumBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.javav2.spec.base.BaseIdentity;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingNamespaceType;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentable;
import org.opendaylight.mdsal.binding.javav2.util.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
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
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
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
     * @param moduleContext
     *            Module object from which builder will be created
     * @param genCtx generated context
     * @param verboseClassComments verbosity switch
     *
     * @return <code>GeneratedTypeBuilder</code> which is internal
     *         representation of the module
     * @throws IllegalArgumentException
     *             if module is null
     */
    static GeneratedTypeBuilder moduleToDataType(final ModuleContext moduleContext,
            final Map<Module, ModuleContext> genCtx, final boolean verboseClassComments) {

        final GeneratedTypeBuilder moduleDataTypeBuilder = moduleTypeBuilder("Data", verboseClassComments,
            moduleContext);
        addImplementedInterfaceFromUses(moduleContext.module(), moduleDataTypeBuilder, genCtx);
        moduleDataTypeBuilder.addImplementsType(BindingTypes.TREE_ROOT);

        if (verboseClassComments) {
            YangSourceDefinition.of(moduleContext.module()).ifPresent(moduleDataTypeBuilder::setYangSourceDefinition);
            TypeComments.description(moduleContext.module()).ifPresent(moduleDataTypeBuilder::addComment);
            moduleContext.module().getDescription().ifPresent(moduleDataTypeBuilder::setDescription);
            moduleContext.module().getReference().ifPresent(moduleDataTypeBuilder::setReference);
        }

        return moduleDataTypeBuilder;
    }

    /**
     * Generates type builder for <code>module</code>.
     *
     * @param postfix
     *            string which is added to the module class name representation
     *            as suffix
     * @param verboseClassComments verbosity switch
     * @return instance of GeneratedTypeBuilder which represents
     *         <code>module</code>.
     * @throws IllegalArgumentException
     *             if <code>module</code> is null
     */
    static GeneratedTypeBuilder moduleTypeBuilder(final String postfix, final boolean
            verboseClassComments, final ModuleContext context) {
        Preconditions.checkArgument(context != null, "context cannot be NULL.");
        // underscore used as separator for distinction of module name parts
        final String moduleName = new StringBuilder(context.module().getName()).append('_').append(postfix).toString();

        final GeneratedTypeBuilderImpl moduleBuilder = new GeneratedTypeBuilderImpl(context.normalizedRootPackageName(),
            moduleName, true, false, context);
        if (verboseClassComments) {
            YangSourceDefinition.of(context.module()).ifPresent(moduleBuilder::setYangSourceDefinition);
            TypeComments.description(context.module()).ifPresent(moduleBuilder::addComment);
            context.module().getDescription().ifPresent(moduleBuilder::setDescription);
            context.module().getReference().ifPresent(moduleBuilder::setReference);
        }
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

    static GeneratedTypeBuilder findIdentityByQname(final QName qname, final Map<Module, ModuleContext> genCtx) {
        for (final ModuleContext ctx : genCtx.values()) {
            final GeneratedTypeBuilder result = ctx.getIdentities().get(qname);
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
     * @param moduleContext
     *            current module context
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
    static GeneratedTypeBuilder resolveDataSchemaNodes(final ModuleContext moduleContext,
                          final GeneratedTypeBuilder parent, final GeneratedTypeBuilder childOf,
                          final Iterable<DataSchemaNode> schemaNodes, final Map<Module, ModuleContext> genCtx,
                          final SchemaContext schemaContext, final boolean verboseClassComments,
                          final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders,
                          final TypeProvider typeProvider, final BindingNamespaceType namespaceType) {

        if (schemaNodes != null && parent != null) {
            for (final DataSchemaNode schemaNode : schemaNodes) {
                if (resolveDataSchemaNodesCheck(moduleContext.module(), schemaContext, schemaNode)) {
                    addSchemaNodeToBuilderAsMethod(schemaNode, parent, childOf, moduleContext, genCtx,
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
        }

        final QName qname = schemaNode.getPath().getLastComponent();
        final Module originalModule = schemaContext.findModule(qname.getModule()).get();
        return module.equals(originalModule);
    }

    private static QName createQNameFromSuperNode(final Module module, final Object node, final SchemaNode superChildNode) {
        QName childNodeQName = null;
        if (node instanceof Module) {
            childNodeQName = QName.create(((Module) node).getQNameModule(), superChildNode.getQName().getLocalName());
        } else if (node instanceof SchemaNode) {
            childNodeQName = QName.create(((SchemaNode) node).getQName(), superChildNode.getQName().getLocalName());
        } else if (node instanceof AugmentationSchemaNode) {
            childNodeQName = QName.create(module.getQNameModule(), superChildNode.getQName().getLocalName());
        } else {
            throw new IllegalArgumentException("Not support node type:" + node);
        }

        return childNodeQName;
    }

    private static void addUsesImplements(final SchemaNode superNode, final Module superModule,
            final Object node, final ModuleContext moduleContext, final SchemaContext schemaContext,
            final Map<Module, ModuleContext> genCtx, final BindingNamespaceType namespaceType) {
        if (superNode instanceof DataNodeContainer) {
            for (DataSchemaNode superChildNode : ((DataNodeContainer) superNode).getChildNodes()) {
                if (superChildNode instanceof DataNodeContainer || superChildNode instanceof ChoiceSchemaNode) {
                    final QName childQName = createQNameFromSuperNode(moduleContext.module(), node, superChildNode);
                    DataSchemaNode childNode = ((DataNodeContainer) node).getDataChildByName(childQName);
                    Preconditions.checkNotNull(childNode, "%s->%s", node, childQName);

                    final GeneratedTypeBuilder type = moduleContext.getChildNode(childNode.getPath());
                    final GeneratedTypeBuilder superType = genCtx.get(superModule).getChildNode(superChildNode.getPath());

                    Preconditions.checkNotNull(type, "%s->%s", moduleContext, childNode.getPath());
                    Preconditions.checkNotNull(superType, "%s->%s", superModule, superChildNode.getPath());
                    type.addImplementsType(superType);
                    if (superChildNode instanceof ListSchemaNode
                            && !((ListSchemaNode) superChildNode).getKeyDefinition().isEmpty()) {
                        if (BindingNamespaceType.isGrouping(namespaceType)) {
                            moduleContext.getKeyType(childNode.getPath())
                                    .addImplementsType(genCtx.get(superModule).getKeyType(superChildNode.getPath()));
                        } else if (BindingNamespaceType.isData(namespaceType)) {
                            moduleContext.getKeyGenTO(childNode.getPath())
                                    .addImplementsType(genCtx.get(superModule).getKeyType(superChildNode.getPath()));
                        }
                    }
                    addUsesImplements(superChildNode, superModule, childNode, moduleContext, schemaContext, genCtx,
                        namespaceType);
                }
            }
        } else if (superNode instanceof ChoiceSchemaNode) {
            for (CaseSchemaNode superCaseNode : ((ChoiceSchemaNode) superNode).getCases().values()) {
                final QName childQName = createQNameFromSuperNode(moduleContext.module(), node, superCaseNode);
                CaseSchemaNode caseNode = ((ChoiceSchemaNode) node).getCaseNodeByName(childQName);
                Preconditions.checkNotNull(caseNode, "%s->%s", node, childQName);

                final GeneratedTypeBuilder type = moduleContext.getCase(caseNode.getPath());
                final GeneratedTypeBuilder superType = genCtx.get(superModule).getCase(superCaseNode.getPath());
                Preconditions.checkNotNull(type, "%s->%s", moduleContext, caseNode.getPath());
                Preconditions.checkNotNull(superType, "%s->%s", superModule, superCaseNode.getPath());
                type.addImplementsType(superType);
                addUsesImplements(superCaseNode, superModule, caseNode, moduleContext, schemaContext, genCtx, namespaceType);
            }
        } else {
            throw new IllegalArgumentException("Not support super node :" + superNode);
        }
    }

    private static GroupingDefinition findGroupingNodeFromUses(final ModuleContext moduleContext, final SchemaContext schemaContext,
            final Object parentNode, final UsesNode usesNode) {
        SchemaNode groupingNode;
        if (parentNode instanceof Module) {
            final Module superModule = schemaContext.findModule(
                usesNode.getGroupingPath().getLastComponent().getModule()).get();
            groupingNode = superModule.getGroupings()
                    .stream().filter(grouping -> grouping.getPath().equals(usesNode.getGroupingPath()))
                    .findFirst().orElse(null);
        } else {
            //FIXME: Schema path is not unique for Yang 1.1, findDataSchemaNode always does search from data node first.
            final Iterable<QName> prefixedPath = usesNode.getGroupingPath().getPathFromRoot();
            final QName current = prefixedPath.iterator().next();
            final Module targetModule = schemaContext.findModule(current.getModule()).orElse(null);
            Preconditions.checkArgument(targetModule != null, "Cannot find target module for %s and %s.",
                    current.getNamespace(), current.getRevision());
            groupingNode = targetModule.getGroupings().stream()
                    .filter(grouping -> grouping.getPath().equals(usesNode.getGroupingPath()))
                    .collect(Collectors.toList()).get(0);
            if (groupingNode == null) {
                groupingNode = SchemaContextUtil.findDataSchemaNode(schemaContext, usesNode.getGroupingPath());
            }
        }

        Preconditions.checkNotNull(groupingNode, "%s->%s", moduleContext, usesNode.getGroupingPath());
        Preconditions.checkState(groupingNode instanceof GroupingDefinition, "%s->%s", moduleContext,
            usesNode.getGroupingPath());
        return (GroupingDefinition) groupingNode;
    }

    static Map<Module, ModuleContext> processUsesImplements(final Object node, final ModuleContext moduleContext,
            final SchemaContext schemaContext, final Map<Module, ModuleContext> genCtx, final BindingNamespaceType namespaceType) {
        if (node instanceof DataNodeContainer) {
            for (final UsesNode usesNode : ((DataNodeContainer) node).getUses()) {
                final GroupingDefinition grouping = findGroupingNodeFromUses(moduleContext, schemaContext, node, usesNode);
                final Module superModule = SchemaContextUtil.findParentModule(schemaContext, grouping);
                addUsesImplements(grouping, superModule, node, moduleContext, schemaContext, genCtx, namespaceType);
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

    static Map<Module, ModuleContext> addRawAugmentGenTypeDefinition(final ModuleContext moduleContext,
            final String augmentPackageName, final GeneratedTypeBuilder targetTypeBuilder, final SchemaNode targetNode,
            final List<AugmentationSchemaNode> schemaPathAugmentListEntry,
            final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders,
            final Map<Module, ModuleContext> genCtx, final SchemaContext schemaContext,
            final boolean verboseClassComments, final TypeProvider typeProvider,
            final BindingNamespaceType namespaceType) {

        //pick augmentation grouped by augmentation target, there is always at least one
        final AugmentationSchemaNode augSchema = schemaPathAugmentListEntry.get(0);

        Map<String, GeneratedTypeBuilder> augmentBuilders = genTypeBuilders.computeIfAbsent(
                augmentPackageName, k -> new HashMap<>());

        //this requires valid semantics in YANG model
        String augIdentifier = null;
        for (AugmentationSchemaNode aug : schemaPathAugmentListEntry) {
            // FIXME: when there're multiple augment identifiers for augmentations of same target,
            // it would pick the first identifier.
            augIdentifier = getAugmentIdentifier(aug.getUnknownSchemaNodes());
            break;
        }

        if (augIdentifier == null) {
            augIdentifier = new StringBuilder(moduleContext.module().getName())
                    .append('_').append(targetNode.getQName().getLocalName()).toString();
        }

        GeneratedTypeBuilderImpl augTypeBuilder = new GeneratedTypeBuilderImpl(augmentPackageName, augIdentifier,
                true, false, moduleContext);

        augTypeBuilder.addImplementsType(BindingTypes.TREE_NODE);
        augTypeBuilder.addImplementsType(parameterizedTypeFor(BindingTypes.INSTANTIABLE, augTypeBuilder));
        augTypeBuilder.addImplementsType(Types.augmentationTypeFor(targetTypeBuilder.toInstance()));
        augTypeBuilder.setNormalizedRootPackageName(moduleContext.normalizedRootPackageName());
        augTypeBuilder.setWithBuilder(true);
        augTypeBuilder.setBindingNamespaceType(namespaceType);
        annotateDeprecatedIfNecessary(augSchema.getStatus(), augTypeBuilder);

        //If target is case node then pass down parent type (the closest ancestor) to children data nodes.
        final GeneratedTypeBuilder childOf;
        if (targetNode instanceof CaseSchemaNode) {
            childOf = (GeneratedTypeBuilder) targetTypeBuilder.getParentTypeForBuilder();
        } else {
            augTypeBuilder.setParentTypeForBuilder(targetTypeBuilder);
            childOf = targetTypeBuilder;
        }

        //produces getters for augTypeBuilder eventually
        for (AugmentationSchemaNode aug : schemaPathAugmentListEntry) {
            //apply all uses
            addImplementedInterfaceFromUses(aug, augTypeBuilder, genCtx);
            augSchemaNodeToMethods(moduleContext, augTypeBuilder, childOf,
                aug.getChildNodes(), genCtx, schemaContext, verboseClassComments, typeProvider, genTypeBuilders,
                targetTypeBuilder.getBindingNamespaceType());
        }

        augmentBuilders.put(augTypeBuilder.getName(), augTypeBuilder);

        if(!augSchema.getChildNodes().isEmpty()) {
            moduleContext.addTargetToAugmentation(augTypeBuilder, augSchema.getTargetPath());
        }
        moduleContext.addAugmentType(augTypeBuilder);
        moduleContext.addTypeToAugmentations(augTypeBuilder, schemaPathAugmentListEntry);
        return genCtx;
    }

    /**
     * Adds the methods to <code>typeBuilder</code> what represents subnodes of
     * node for which <code>typeBuilder</code> was created.
     *
     * @param moduleContext
     *            current module
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
    private static GeneratedTypeBuilder augSchemaNodeToMethods(final ModuleContext moduleContext,
            final GeneratedTypeBuilder typeBuilder, final GeneratedTypeBuilder childOf,
            final Iterable<DataSchemaNode> schemaNodes,
            final Map<Module, ModuleContext> genCtx, final SchemaContext schemaContext, final boolean
            verboseClassComments, final TypeProvider typeProvider, final Map<String, Map<String,
            GeneratedTypeBuilder>> genTypeBuilders, final BindingNamespaceType namespaceType) {
        if (schemaNodes != null && typeBuilder != null) {
            for (final DataSchemaNode schemaNode : schemaNodes) {
                if (!schemaNode.isAugmenting()) {
                    addSchemaNodeToBuilderAsMethod(schemaNode, typeBuilder, childOf, moduleContext, genCtx,
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
     * @param schemaNode
     *            schema node for which is created generated type builder
     * @param childOf
     *            parent generated type of data tree node, which should be
     *            set null if current schema node is not a data tree node.
     * @param schemaContext schema context
     * @return generated type builder <code>schemaNode</code>
     */
    public static GeneratedTypeBuilder addDefaultInterfaceDefinition(final SchemaNode
            schemaNode, @Nullable final Type childOf, final ModuleContext moduleContext, final Map<Module, ModuleContext> genCtx,
            final SchemaContext schemaContext, final boolean verboseClassComments, final Map<String, Map<String,
            GeneratedTypeBuilder>> genTypeBuilders, final TypeProvider typeProvider, final BindingNamespaceType namespaceType) {

        GeneratedTypeBuilder it = addRawInterfaceDefinition(schemaNode, schemaContext, "", "",
                verboseClassComments, genTypeBuilders, namespaceType, moduleContext);

        if (BindingNamespaceType.isData(namespaceType)) {
            if (childOf == null) {
                it.addImplementsType(BindingTypes.TREE_NODE);
            } else {
                if (!(schemaNode instanceof ListSchemaNode) ||
                    ((ListSchemaNode) schemaNode).getKeyDefinition().isEmpty()) {
                    it.addImplementsType(parameterizedTypeFor(BindingTypes.TREE_CHILD_NODE, childOf,
                        parameterizedTypeFor(BindingTypes.ITEM, it)));
                }
            }

            if (!(schemaNode instanceof CaseSchemaNode)) {
                it.addImplementsType(parameterizedTypeFor(BindingTypes.INSTANTIABLE, it));
            }

            it.addImplementsType(BindingTypes.augmentable(it));
        }

        if (schemaNode instanceof DataNodeContainer) {
            groupingsToGenTypes(moduleContext, ((DataNodeContainer) schemaNode).getGroupings(), genCtx, schemaContext,
                    verboseClassComments, genTypeBuilders, typeProvider);
            it = addImplementedInterfaceFromUses((DataNodeContainer) schemaNode, it, genCtx);
        }

        return it;
    }

    static GeneratedTypeBuilder resolveNotification(final GeneratedTypeBuilder listenerInterface, final String
            parentName, final NotificationDefinition notification, final ModuleContext moduleContext,
            final SchemaContext schemaContext, final boolean verboseClassComments, final Map<String, Map<String, GeneratedTypeBuilder>>
            genTypeBuilders, final TypeProvider typeProvider, final Map<Module, ModuleContext> genCtx) {
        final GeneratedTypeBuilder notificationInterface = addDefaultInterfaceDefinition
                (notification, null, moduleContext, genCtx, schemaContext,
                        verboseClassComments, genTypeBuilders, typeProvider, BindingNamespaceType.Notification);
        annotateDeprecatedIfNecessary(notification.getStatus(), notificationInterface);
        notificationInterface.addImplementsType(NOTIFICATION);
        moduleContext.addChildNodeType(notification, notificationInterface);

        // Notification object
        resolveDataSchemaNodes(moduleContext, notificationInterface,
                notificationInterface, notification.getChildNodes(), genCtx, schemaContext,
                verboseClassComments, genTypeBuilders, typeProvider, BindingNamespaceType.Notification);

        //in case of tied notification, incorporate parent's localName
        final StringBuilder sb = new StringBuilder("on_");
        if (parentName != null) {
            sb.append(parentName).append('_');
        }
        sb.append(notificationInterface.getName());

        listenerInterface.addMethod(JavaIdentifierNormalizer.normalizeSpecificIdentifier(sb.toString(), JavaIdentifier.METHOD))
                .setAccessModifier(AccessModifier.PUBLIC).addParameter(notificationInterface, "notification")
                .setComment(encodeAngleBrackets(notification.getDescription().orElse(null))).setReturnType(Types.VOID);
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
    static GeneratedTypeBuilder addRawInterfaceDefinition(final SchemaNode schemaNode,
            final SchemaContext schemaContext, final String prefix, final String suffix,
            final boolean verboseClassComments, final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders,
            final BindingNamespaceType namespaceType, final ModuleContext context) {

        Preconditions.checkArgument(schemaNode != null, "Data Schema Node cannot be NULL.");
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

        final GeneratedTypeBuilderImpl newType = new GeneratedTypeBuilderImpl(
            packageNameForGeneratedType(context.normalizedNSPackageName(namespaceType), schemaNode.getPath()),
            schemaNodeName, context);
        qNameConstant(newType, BindingMapping.QNAME_STATIC_FIELD_NAME, schemaNode.getQName());
        if (verboseClassComments) {
            YangSourceDefinition.of(context.module(), schemaNode).ifPresent(newType::setYangSourceDefinition);
            TypeComments.description(schemaNode).ifPresent(newType::addComment);
            schemaNode.getDescription().ifPresent(newType::setDescription);
            schemaNode.getReference().ifPresent(newType::setReference);
        }
        newType.setSchemaPath((List<QName>) schemaNode.getPath().getPathFromRoot());
        newType.setModuleName(context.module().getName());
        newType.setNormalizedRootPackageName(context.normalizedRootPackageName());
        newType.setWithBuilder(AuxiliaryGenUtils.hasBuilderClass(schemaNode, namespaceType));
        newType.setBindingNamespaceType(namespaceType);

        if (!genTypeBuilders.containsKey(context.normalizedNSPackageName(namespaceType))) {
            final Map<String, GeneratedTypeBuilder> builders = new HashMap<>();
            builders.put(newType.getName(), newType);
            genTypeBuilders.put(context.normalizedNSPackageName(namespaceType), builders);
        } else {
            final Map<String, GeneratedTypeBuilder> builders =
                genTypeBuilders.get(context.normalizedNSPackageName(namespaceType));
            if (!builders.containsKey(newType.getName())) {
                builders.put(newType.getName(), newType);
            }
        }
        return newType;

    }

    private static void addSchemaNodeToBuilderAsMethod(final DataSchemaNode node,
        final GeneratedTypeBuilder typeBuilder, final GeneratedTypeBuilder childOf, final ModuleContext moduleContext,
        final Map<Module, ModuleContext> genCtx, final SchemaContext schemaContext, final boolean verboseClassComments,
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders, final TypeProvider typeProvider,
        final BindingNamespaceType namespaceType) {

        if (node != null && typeBuilder != null) {
            if (node instanceof ContainerSchemaNode) {
                containerToGenType(moduleContext, typeBuilder, childOf, (ContainerSchemaNode) node,
                        schemaContext, verboseClassComments, genCtx, genTypeBuilders, typeProvider, namespaceType);
            } else if (node instanceof LeafListSchemaNode) {
                resolveLeafListSchemaNode(schemaContext, typeBuilder, (LeafListSchemaNode) node, moduleContext,
                            typeProvider, genCtx, verboseClassComments);
            } else if (node instanceof LeafSchemaNode) {
                resolveLeafSchemaNodeAsMethod("", schemaContext, typeBuilder, genCtx, (LeafSchemaNode) node,
                    moduleContext, typeProvider, verboseClassComments);
            } else if (node instanceof ListSchemaNode) {
                listToGenType(moduleContext, typeBuilder, childOf, (ListSchemaNode) node, schemaContext,
                        verboseClassComments, genCtx, genTypeBuilders, typeProvider, namespaceType);
            } else if (node instanceof ChoiceSchemaNode) {
                choiceToGenType(moduleContext, schemaContext, verboseClassComments, typeBuilder, childOf,
                        (ChoiceSchemaNode) node, genTypeBuilders, genCtx, typeProvider, namespaceType);
            } else if (node instanceof AnyXmlSchemaNode || node instanceof AnyDataSchemaNode) {
                resolveAnyNodeAsMethod(typeBuilder, node);
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
     * @param moduleContext
     *            current module
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
    private static void choiceToGenType(final ModuleContext moduleContext, final SchemaContext schemaContext, final boolean
            verboseClasssComments, final GeneratedTypeBuilder parent,
            final GeneratedTypeBuilder childOf, final ChoiceSchemaNode choiceNode,
            final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders,
            final Map<Module, ModuleContext> genCtx, final TypeProvider typeProvider,
            final BindingNamespaceType namespaceType) {
        checkArgument(choiceNode != null, "Choice Schema Node cannot be NULL.");

        final GeneratedTypeBuilder choiceTypeBuilder = addRawInterfaceDefinition(choiceNode,
            schemaContext, "", "", verboseClasssComments, genTypeBuilders, namespaceType, moduleContext);
        constructGetter(parent, choiceNode.getQName().getLocalName(),
                choiceNode.getDescription().orElse(null), choiceTypeBuilder, choiceNode.getStatus());
        choiceTypeBuilder.setParentTypeForBuilder(childOf);
        if (BindingNamespaceType.isData(namespaceType)) {
            choiceTypeBuilder.addImplementsType(parameterizedTypeFor(BindingTypes.INSTANTIABLE, choiceTypeBuilder));
        }
        annotateDeprecatedIfNecessary(choiceNode.getStatus(), choiceTypeBuilder);
        moduleContext.addChildNodeType(choiceNode, choiceTypeBuilder);
        generateTypesFromChoiceCases(moduleContext, schemaContext, genCtx, choiceTypeBuilder.toInstance(),
            choiceNode, verboseClasssComments, typeProvider, genTypeBuilders, namespaceType);
    }

    private static void containerToGenType(final ModuleContext moduleContext, final GeneratedTypeBuilder parent,
            final GeneratedTypeBuilder childOf, final ContainerSchemaNode node,
            final SchemaContext schemaContext, final boolean verboseClassComments, final Map<Module, ModuleContext> genCtx,
            final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders, final TypeProvider typeProvider,
            final BindingNamespaceType namespaceType) {

        final GeneratedTypeBuilder genType = processDataSchemaNode(moduleContext, childOf, node,
                schemaContext, verboseClassComments, genCtx, genTypeBuilders, typeProvider, namespaceType);
        if (genType != null) {
            StringBuilder getterName = new StringBuilder(node.getQName().getLocalName());
            constructGetter(parent, getterName.toString(), node.getDescription().orElse(null), genType, node.getStatus());
            resolveDataSchemaNodes(moduleContext, genType, genType, node.getChildNodes(), genCtx,
                    schemaContext, verboseClassComments, genTypeBuilders, typeProvider, namespaceType);
            processUsesImplements(node, moduleContext, schemaContext, genCtx, namespaceType);
        }
    }

    private static void listToGenType(final ModuleContext moduleContext, final GeneratedTypeBuilder
            parent, final GeneratedTypeBuilder childOf, final ListSchemaNode node, final SchemaContext schemaContext,
            final boolean verboseClassComments, final Map<Module, ModuleContext> genCtx,
            final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders, final TypeProvider typeProvider,
            final BindingNamespaceType namespaceType) {

        final GeneratedTypeBuilder genType = processDataSchemaNode(moduleContext, childOf, node,
                schemaContext, verboseClassComments, genCtx, genTypeBuilders, typeProvider, namespaceType);
        if (genType != null) {
            final String nodeName = node.getQName().getLocalName();

            Type getterReturnType = Types.listTypeFor(genType);
            if (BindingNamespaceType.isGrouping(namespaceType)) {
                getterReturnType = Types.listTypeFor(wildcardTypeFor(genType.getPackageName(), genType.getName(),
                                true, true, null));
            }
            constructGetter(parent, nodeName, node.getDescription().orElse(null), getterReturnType, node.getStatus());

            final List<QName> listKeys = node.getKeyDefinition();
            final String packageName = new StringBuilder(packageNameForGeneratedType(
                    moduleContext.normalizedNSPackageName(BindingNamespaceType.Key), node.getPath()))
                .append('.').append(nodeName).toString();
            //FIXME: Is it neccessary to generate interface of key and implemented by class?
            if (BindingNamespaceType.isGrouping(namespaceType)) {
                final GeneratedTypeBuilder genTypeBuilder = resolveListKeyTypeBuilder(packageName, node, moduleContext);
                for (final DataSchemaNode schemaNode : node.getChildNodes()) {
                    if (resolveDataSchemaNodesCheck(moduleContext.module(), schemaContext, schemaNode)) {
                        addSchemaNodeToListTypeBuilders(nodeName, schemaNode, genType, genTypeBuilder, listKeys,
                            moduleContext, typeProvider, schemaContext, genCtx, genTypeBuilders, verboseClassComments, namespaceType);
                    }
                }
                if (genTypeBuilder != null) {
                    typeBuildersToGenTypes(genType, genTypeBuilder.toInstance(), namespaceType);
                    moduleContext.addKeyType(node.getPath(), genTypeBuilder);
                }
                processUsesImplements(node, moduleContext, schemaContext, genCtx, namespaceType);
            } else {
                final GeneratedTOBuilder genTOBuilder = resolveListKeyTOBuilder(packageName, node, moduleContext);
                if (genTOBuilder != null) {
                    final Type identifiableMarker = Types.parameterizedTypeFor(IDENTIFIABLE, genTOBuilder);
                    genTOBuilder.addImplementsType(IDENTIFIER);
                    genType.addImplementsType(identifiableMarker);
                    if (BindingNamespaceType.isData(namespaceType)) {
                        genType.addImplementsType(parameterizedTypeFor(BindingTypes.TREE_CHILD_NODE, childOf,
                            parameterizedTypeFor(BindingTypes.IDENTIFIABLE_ITEM, genType, genTOBuilder)));
                    }
                }

                for (final DataSchemaNode schemaNode : node.getChildNodes()) {
                    if (resolveDataSchemaNodesCheck(moduleContext.module(), schemaContext, schemaNode)) {
                        addSchemaNodeToListBuilders(nodeName, schemaNode, genType, genTOBuilder, listKeys,
                            moduleContext, typeProvider, schemaContext, genCtx, genTypeBuilders, verboseClassComments, namespaceType);
                    }
                }
                processUsesImplements(node, moduleContext, schemaContext, genCtx, namespaceType);

                // serialVersionUID
                if (genTOBuilder != null) {
                    final GeneratedPropertyBuilder prop = new GeneratedPropertyBuilderImpl("serialVersionUID");
                    prop.setValue(Long.toString(computeDefaultSUID(genTOBuilder)));
                    genTOBuilder.setSUID(prop);
                    typeBuildersToGenTypes(genType, genTOBuilder.toInstance(), namespaceType);
                    moduleContext.addGeneratedTOBuilder(node.getPath(), genTOBuilder);
                }
            }
        }
    }

    private static void typeBuildersToGenTypes(final GeneratedTypeBuilder typeBuilder, final Type keyType,
            final BindingNamespaceType namespaceType) {
        checkArgument(typeBuilder != null, "Generated Type Builder cannot be NULL.");
        if (keyType != null) {
            Type returnKeyType = keyType;
            if (BindingNamespaceType.isGrouping(namespaceType)) {
                returnKeyType = wildcardTypeFor(keyType.getPackageName(), keyType.getName(),
                        true, true, null);
            }
            constructGetter(typeBuilder, "identifier", "Returns Primary Key of Yang List Type", returnKeyType, Status.CURRENT);

        }
    }

    private static void addPatternConstant(final GeneratedTypeBuilder typeBuilder, final String leafName,
            final List<PatternConstraint> patternConstraints) {
        if (!patternConstraints.isEmpty()) {
            final StringBuilder field = new StringBuilder();
            field.append(BindingMapping.PATTERN_CONSTANT_NAME).append("_")
                .append(JavaIdentifierNormalizer.normalizeSpecificIdentifier(leafName, JavaIdentifier.METHOD));
            typeBuilder.addConstant(Types.listTypeFor(BaseYangTypes.STRING_TYPE), field.toString(),
                resolveRegExpressions(patternConstraints));
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
     * @param moduleContext
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
            final ModuleContext moduleContext, final TypeProvider typeProvider, final boolean verboseClassComments) {
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

        if (leaf.isAddedByUses()) {
            Preconditions.checkState(leaf instanceof DerivableSchemaNode);
            if (isInnerType(leaf, typeDef)) {
                final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(typeDef);
                returnType = typeProvider.javaTypeForSchemaDefinitionType(getBaseOrDeclaredType(typeDef), leaf,
                        restrictions, moduleContext);
            } else {
                if (typeDef.getBaseType() == null && (typeDef instanceof EnumTypeDefinition
                        || typeDef instanceof UnionTypeDefinition || typeDef instanceof BitsTypeDefinition)) {
                    LeafSchemaNode originalLeaf = (LeafSchemaNode) ((DerivableSchemaNode) leaf).getOriginal().orElse(null);
                    Preconditions.checkNotNull(originalLeaf);
                    returnType = genCtx.get(findParentModule(schemaContext, originalLeaf)).getInnerType(typeDef.getPath());
                } else {
                    final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(typeDef);
                    returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, leaf, restrictions,
                        moduleContext);
                }
            }
        } else if (isInnerType(leaf, typeDef)) {
            if (typeDef instanceof EnumTypeDefinition) {
                returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, leaf, moduleContext);
                final EnumTypeDefinition enumTypeDef = (EnumTypeDefinition) typeDef;
                final EnumBuilder enumBuilder = resolveInnerEnumFromTypeDefinition(enumTypeDef, leaf.getQName(),
                        genCtx, typeBuilder, moduleContext);
                if (enumBuilder != null) {
                    returnType = enumBuilder.toInstance(typeBuilder);
                }
                ((TypeProviderImpl) typeProvider).putReferencedType(leaf.getPath(), returnType);
            } else if (typeDef instanceof UnionTypeDefinition) {
                final GeneratedTOBuilder genTOBuilder = addTOToTypeBuilder(typeDef, typeBuilder, leaf, parentModule,
                        typeProvider, schemaContext, moduleContext, genCtx);
                if (genTOBuilder != null) {
                    //TODO: https://bugs.opendaylight.org/show_bug.cgi?id=2289
                    returnType = createReturnTypeForUnion(genTOBuilder, typeDef, typeBuilder, parentModule,
                        typeProvider, verboseClassComments);
                }
            } else if (typeDef instanceof BitsTypeDefinition) {
                final GeneratedTOBuilder genTOBuilder = addTOToTypeBuilder(typeDef, typeBuilder, leaf, parentModule,
                        typeProvider, schemaContext, moduleContext, genCtx);
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
                        restrictions, moduleContext);
                addPatternConstant(typeBuilder, leafName, restrictions.getPatternConstraints());
            }
        } else {
            final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(typeDef);
            returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, leaf, restrictions, moduleContext);
            addPatternConstant(typeBuilder, leafName, restrictions.getPatternConstraints());
        }

        if (returnType == null) {
            return null;
        }

        if (typeDef instanceof EnumTypeDefinition) {
            ((TypeProviderImpl) typeProvider).putReferencedType(leaf.getPath(), returnType);
        }

        final String leafGetterName;
        if ("key".equals(leafName.toLowerCase())) {
            StringBuilder sb = new StringBuilder(leafName)
                .append('_').append("RESERVED_WORD");
            leafGetterName = sb.toString();
        } else {
            leafGetterName = leafName;
        }
        constructGetter(typeBuilder, leafGetterName, leaf.getDescription().orElse(""), returnType, leaf.getStatus());
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
     * @param moduleContext module context
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
            typeBuilder, final LeafListSchemaNode node, final ModuleContext moduleContext, final TypeProvider typeProvider,
            final Map<Module, ModuleContext> genCtx, final boolean verboseClassComments) {
        if (node == null || typeBuilder == null) {
            return false;
        }

        final QName nodeName = node.getQName();

        final TypeDefinition<?> typeDef = node.getType();
        final Module parentModule = findParentModule(schemaContext, node);

        Type returnType = null;
        if (typeDef.getBaseType() == null) {
            if (typeDef instanceof EnumTypeDefinition) {
                returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, node, moduleContext);
                final EnumTypeDefinition enumTypeDef = (EnumTypeDefinition) typeDef;
                final EnumBuilder enumBuilder = resolveInnerEnumFromTypeDefinition(enumTypeDef, nodeName,
                        genCtx, typeBuilder, moduleContext);
                returnType = new ReferencedTypeImpl(enumBuilder.getPackageName(), enumBuilder.getName(), true,
                        null);
                ((TypeProviderImpl) typeProvider).putReferencedType(node.getPath(), returnType);
            } else if (typeDef instanceof UnionTypeDefinition) {
                final GeneratedTOBuilder genTOBuilder = addTOToTypeBuilder(typeDef, typeBuilder, node, parentModule,
                        typeProvider, schemaContext, moduleContext, genCtx);
                if (genTOBuilder != null) {
                    returnType = createReturnTypeForUnion(genTOBuilder, typeDef, typeBuilder, parentModule,
                        typeProvider, verboseClassComments);
                }
            } else if (typeDef instanceof BitsTypeDefinition) {
                final GeneratedTOBuilder genTOBuilder = addTOToTypeBuilder(typeDef, typeBuilder, node, parentModule,
                        typeProvider, schemaContext, moduleContext, genCtx);
                returnType = genTOBuilder.toInstance();
            } else {
                final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(typeDef);
                returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, node, restrictions, moduleContext);

                addPatternConstant(typeBuilder, nodeName.getLocalName(), restrictions.getPatternConstraints());
            }
        } else {
            final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(typeDef);
            returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, node, restrictions, moduleContext);

            addPatternConstant(typeBuilder, nodeName.getLocalName(), restrictions.getPatternConstraints());
        }

        final ParameterizedType listType = Types.listTypeFor(returnType);
        constructGetter(typeBuilder, nodeName.getLocalName(), node.getDescription().orElse(null), listType, node.getStatus());
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
     * @param moduleContext
     *            current module
     * @param schemaContext
     *            current schema context
     * @param genCtx
     *            actual generated context
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
    private static void generateTypesFromChoiceCases(final ModuleContext moduleContext, final SchemaContext schemaContext,
            final Map<Module, ModuleContext> genCtx, final GeneratedType refChoiceType,
            final ChoiceSchemaNode choiceNode, final boolean verboseClassComments,
            final TypeProvider typeProvider, final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders,
            final BindingNamespaceType namespaceType) {
        checkArgument(refChoiceType != null, "Referenced Choice Type cannot be NULL.");
        checkArgument(choiceNode != null, "ChoiceNode cannot be NULL.");

        for (final CaseSchemaNode caseNode : choiceNode.getCases().values()) {
            if (caseNode != null && resolveDataSchemaNodesCheck(moduleContext.module(), schemaContext, caseNode)) {
                final GeneratedTypeBuilder caseTypeBuilder = addDefaultInterfaceDefinition(caseNode,
                    null, moduleContext, genCtx, schemaContext, verboseClassComments,
                    genTypeBuilders, typeProvider, namespaceType);
                caseTypeBuilder.addImplementsType(refChoiceType);
                caseTypeBuilder.setParentTypeForBuilder(refChoiceType.getParentTypeForBuilder());
                annotateDeprecatedIfNecessary(caseNode.getStatus(), caseTypeBuilder);
                moduleContext.addCaseType(caseNode.getPath(), caseTypeBuilder);
                if (BindingNamespaceType.isData(namespaceType)) {
                    moduleContext.addChoiceToCaseMapping(refChoiceType, caseTypeBuilder, caseNode);
                }

                resolveDataSchemaNodes(moduleContext, caseTypeBuilder,
                        (GeneratedTypeBuilder) refChoiceType.getParentTypeForBuilder(), caseNode.getChildNodes(),
                    genCtx, schemaContext, verboseClassComments, genTypeBuilders, typeProvider, namespaceType);
                processUsesImplements(caseNode, moduleContext, schemaContext, genCtx, namespaceType);
            }
        }
    }

    private static Type resolveAnyNodeAsMethod(final GeneratedTypeBuilder typeBuilder,final DataSchemaNode node) {
        final String anyName = node.getQName().getLocalName();
        if (anyName == null) {
            return null;
        }

        Type returnType = Types.DOCUMENT;
        constructGetter(typeBuilder, anyName, node.getDescription().orElse(""), returnType, node.getStatus());
        return returnType;
    }

    /**
     * Adds <code>schemaNode</code> to <code>typeBuilder</code> as getter method
     * or to <code>genTOBuilder</code> as property.
     *
     * @param nodeName
     *            string contains the name of list
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
    private static void addSchemaNodeToListBuilders(final String nodeName,
            final DataSchemaNode schemaNode, final GeneratedTypeBuilder typeBuilder,
            final GeneratedTOBuilder genTOBuilder, final List<QName> listKeys, final ModuleContext moduleContext,
            final TypeProvider typeProvider, final SchemaContext schemaContext, final Map<Module, ModuleContext> genCtx,
            final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders, final boolean verboseClassComments,
            final BindingNamespaceType namespaceType) {
        checkArgument(schemaNode != null, "Data Schema Node cannot be NULL.");
        checkArgument(typeBuilder != null, "Generated Type Builder cannot be NULL.");

        if (schemaNode instanceof LeafSchemaNode) {
            final LeafSchemaNode leaf = (LeafSchemaNode) schemaNode;
            final QName leafQName = leaf.getQName();

            final Type type = resolveLeafSchemaNodeAsMethod(nodeName, schemaContext, typeBuilder, genCtx, leaf,
                moduleContext, typeProvider, verboseClassComments);
            if (listKeys.contains(leafQName)) {
                if (type == null) {
                    resolveLeafSchemaNodeAsProperty(nodeName, schemaContext, typeProvider, genCtx, genTOBuilder, leaf, true,
                        moduleContext);
                } else {
                    AuxiliaryGenUtils.resolveLeafSchemaNodeAsProperty(nodeName, genTOBuilder, leaf, type, true);
                }
            }
        } else {
            if (schemaNode instanceof LeafListSchemaNode) {
                resolveLeafListSchemaNode(schemaContext, typeBuilder, (LeafListSchemaNode) schemaNode, moduleContext,
                        typeProvider, genCtx, verboseClassComments);
            } else if (schemaNode instanceof ContainerSchemaNode) {
                containerToGenType(moduleContext, typeBuilder, typeBuilder, (ContainerSchemaNode) schemaNode,
                        schemaContext, verboseClassComments, genCtx, genTypeBuilders, typeProvider, namespaceType);
            } else if (schemaNode instanceof ListSchemaNode) {
                listToGenType(moduleContext, typeBuilder, typeBuilder, (ListSchemaNode) schemaNode,
                        schemaContext, verboseClassComments, genCtx, genTypeBuilders, typeProvider, namespaceType);
            } else if (schemaNode instanceof ChoiceSchemaNode) {
                choiceToGenType(moduleContext, schemaContext, verboseClassComments, typeBuilder, typeBuilder,
                        (ChoiceSchemaNode) schemaNode, genTypeBuilders, genCtx, typeProvider, namespaceType);
            }
        }
    }

    private static void addSchemaNodeToListTypeBuilders(final String nodeName,
            final DataSchemaNode schemaNode, final GeneratedTypeBuilder typeBuilder,
            final GeneratedTypeBuilder genTypeBuilder, final List<QName> listKeys, final ModuleContext moduleContext,
            final TypeProvider typeProvider, final SchemaContext schemaContext, final Map<Module, ModuleContext> genCtx,
            final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders, final boolean verboseClassComments,
            final BindingNamespaceType namespaceType) {
        checkArgument(schemaNode != null, "Data Schema Node cannot be NULL.");
        checkArgument(typeBuilder != null, "Generated Type Builder cannot be NULL.");

        if (schemaNode instanceof LeafSchemaNode) {
            final LeafSchemaNode leaf = (LeafSchemaNode) schemaNode;
            final QName leafQName = leaf.getQName();
            resolveLeafSchemaNodeAsMethod(nodeName, schemaContext, typeBuilder, genCtx, leaf,
                moduleContext, typeProvider, verboseClassComments);
            if (listKeys.contains(leafQName)) {
                resolveLeafSchemaNodeAsMethod(nodeName, schemaContext, genTypeBuilder, genCtx, leaf, moduleContext,
                        typeProvider, verboseClassComments);
            }
        } else {
            if (schemaNode instanceof LeafListSchemaNode) {
                resolveLeafListSchemaNode(schemaContext, typeBuilder, (LeafListSchemaNode) schemaNode, moduleContext,
                        typeProvider, genCtx, verboseClassComments);
            } else if (schemaNode instanceof ContainerSchemaNode) {
                containerToGenType(moduleContext, typeBuilder, typeBuilder, (ContainerSchemaNode) schemaNode,
                        schemaContext, verboseClassComments, genCtx, genTypeBuilders, typeProvider, namespaceType);
            } else if (schemaNode instanceof ListSchemaNode) {
                listToGenType(moduleContext, typeBuilder, typeBuilder, (ListSchemaNode) schemaNode,
                        schemaContext, verboseClassComments, genCtx, genTypeBuilders, typeProvider, namespaceType);
            } else if (schemaNode instanceof ChoiceSchemaNode) {
                choiceToGenType(moduleContext, schemaContext, verboseClassComments, typeBuilder, typeBuilder,
                        (ChoiceSchemaNode) schemaNode, genTypeBuilders, genCtx, typeProvider, namespaceType);
            }
        }
    }

    private static boolean resolveLeafSchemaNodeAsProperty(final String nodeName, final SchemaContext schemaContext, final TypeProvider
            typeProvider, final Map<Module, ModuleContext> genCtx, final GeneratedTOBuilder
            toBuilder, final LeafSchemaNode leaf, final boolean isReadOnly, final ModuleContext moduleContext) {

        if (leaf != null && toBuilder != null) {
            Type returnType;
            final TypeDefinition<?> typeDef = leaf.getType();
            if (typeDef instanceof UnionTypeDefinition) {
                // GeneratedType for this type definition should be already
                // created
                final QName qname = typeDef.getQName();
                final Module unionModule = schemaContext.findModule(qname.getModule()).get();
                final ModuleContext mc = genCtx.get(unionModule);
                returnType = mc.getTypedefs().get(typeDef.getPath());
            } else if (typeDef instanceof EnumTypeDefinition && typeDef.getBaseType() == null) {
                // Annonymous enumeration (already generated, since it is inherited via uses).
                LeafSchemaNode originalLeaf = (LeafSchemaNode) SchemaNodeUtils.getRootOriginalIfPossible(leaf);
                QName qname = originalLeaf.getQName();
                final Module enumModule =  schemaContext.findModule(qname.getModule()).orElse(null);
                returnType = genCtx.get(enumModule).getInnerType(originalLeaf.getType().getPath());
            } else {
                returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, leaf, moduleContext);
            }
            return AuxiliaryGenUtils.resolveLeafSchemaNodeAsProperty(nodeName, toBuilder, leaf, returnType, isReadOnly);
        }
        return false;
    }

    private static TypeDefinition<?> getBaseOrDeclaredType(final TypeDefinition<?> typeDef) {
        final TypeDefinition<?> baseType = typeDef.getBaseType();
        return baseType != null && baseType.getBaseType() != null ? baseType : typeDef;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static GeneratedTypeBuilder processDataSchemaNode(final ModuleContext moduleContext,
        final GeneratedTypeBuilder childOf, final DataSchemaNode node, final SchemaContext schemaContext,
        final boolean verboseClassComments, final Map<Module, ModuleContext> genCtx, final Map<String, Map<String,
        GeneratedTypeBuilder>> genTypeBuilders, final TypeProvider typeProvider, final BindingNamespaceType namespaceType) {

        final GeneratedTypeBuilder genType = addDefaultInterfaceDefinition(node, childOf, moduleContext,
                genCtx, schemaContext, verboseClassComments, genTypeBuilders, typeProvider, namespaceType);
        annotateDeprecatedIfNecessary(node.getStatus(), genType);
        genType.setModuleName(moduleContext.module().getName());
        if (verboseClassComments) {
            YangSourceDefinition.of(moduleContext.module(), node).ifPresent(genType::setYangSourceDefinition);
            TypeComments.description(node).ifPresent(genType::addComment);
            node.getDescription().ifPresent(genType::setDescription);
            node.getReference().ifPresent(genType::setReference);
        }
        genType.setSchemaPath((List) node.getPath().getPathFromRoot());
        if (BindingNamespaceType.isData(namespaceType)) {
            genType.setParentTypeForBuilder(childOf);
        }

        if (node instanceof DataNodeContainer) {
            moduleContext.addChildNodeType(node, genType);
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
     * @param moduleContext
     *            current module context
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
    static void groupingsToGenTypes(final ModuleContext moduleContext, final Collection<GroupingDefinition>
            groupings, final Map<Module, ModuleContext> genCtx, final SchemaContext schemaContext, final boolean
            verboseClassComments, final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders,
                                    final TypeProvider typeProvider) {
        final List<GroupingDefinition> groupingsSortedByDependencies = new GroupingDefinitionDependencySort()
                .sort(groupings);
        for (final GroupingDefinition grouping : groupingsSortedByDependencies) {
            groupingToGenType(grouping, moduleContext, genCtx, schemaContext,
                    verboseClassComments, genTypeBuilders, typeProvider);
        }
    }

    /**
     * Converts individual grouping to GeneratedType. Firstly generated type
     * builder is created and every child node of grouping is resolved to the
     * method.
     *
     * @param grouping
     *            GroupingDefinition which contains data about grouping
     * @param moduleContext
     *            current module context
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
    private static void groupingToGenType(final GroupingDefinition grouping, final ModuleContext
            moduleContext, Map<Module, ModuleContext> genCtx, final SchemaContext schemaContext, final boolean
            verboseClassComments, final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders,
            final TypeProvider typeProvider) {
        final GeneratedTypeBuilder genType = addDefaultInterfaceDefinition(grouping, null, moduleContext, genCtx,
                schemaContext, verboseClassComments, genTypeBuilders, typeProvider, BindingNamespaceType.Grouping);
        annotateDeprecatedIfNecessary(grouping.getStatus(), genType);
        moduleContext.addGroupingType(grouping, genType);
        resolveDataSchemaNodes(moduleContext, genType, genType, grouping.getChildNodes(), genCtx,
                schemaContext, verboseClassComments, genTypeBuilders, typeProvider, BindingNamespaceType.Grouping);
        processUsesImplements(grouping, moduleContext, schemaContext, genCtx, BindingNamespaceType.Grouping);
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
     * @param moduleContext
     *            current module context
     * @param identity
     *            IdentitySchemaNode which contains data about identity
     * @param schemaContext
     *            SchemaContext which is used to get package and name
     *            information about base of identity
     * @param genCtx generated context
     * @return returns generated context
     */
    static void identityToGenType(final ModuleContext moduleContext,
            final IdentitySchemaNode identity, final SchemaContext schemaContext, final Map<Module, ModuleContext> genCtx,
            final boolean verboseClassComments) {

        resolveIdentitySchemaNode(schemaContext, identity, moduleContext, verboseClassComments, genCtx);
    }

    private static GeneratedTypeBuilder resolveIdentitySchemaNode(final SchemaContext schemaContext,
            final IdentitySchemaNode identity, final ModuleContext moduleContext, final boolean verboseClassComments,
            final Map<Module, ModuleContext> genCtx) {
        Preconditions.checkNotNull(identity,"Identity can not be null!");

        //check first if identity has been resolved as base identity of some other one
        GeneratedTypeBuilder newType = findIdentityByQname(identity.getQName(), genCtx);
        if (newType == null) {
            final Module parentModule = SchemaContextUtil.findParentModule(schemaContext, identity);
            Preconditions.checkState(moduleContext.module().equals(parentModule),
                    "If the type is null ,it must be in the same module, otherwise it must has been"
                            + "resolved by an imported module.");

            final String packageName = BindingGeneratorUtil.packageNameForGeneratedType(
                moduleContext.normalizedNSPackageName(BindingNamespaceType.Identity), identity.getPath());
            newType = new GeneratedTypeBuilderImpl(packageName, identity.getQName().getLocalName(), true, false,
                moduleContext);

            final Set<IdentitySchemaNode> baseIdentities = identity.getBaseIdentities();
            if (baseIdentities.size() == 0) {
                //no base - abstract
                final GeneratedTypeBuilderImpl genType = new GeneratedTypeBuilderImpl(BaseIdentity.class.getPackage().getName(),
                        BaseIdentity.class.getSimpleName(), moduleContext);
                newType.addImplementsType(genType.toInstance());
            } else {
                //multiple bases - inheritance
                for (IdentitySchemaNode baseIdentity : baseIdentities) {
                    GeneratedTypeBuilder baseType = resolveIdentitySchemaNode(schemaContext,
                        baseIdentity, moduleContext, verboseClassComments, genCtx);
                    newType.addImplementsType(baseType.toInstance());
                }
            }

            if (verboseClassComments) {
                YangSourceDefinition.of(moduleContext.module()).ifPresent(newType::setYangSourceDefinition);
                TypeComments.description(moduleContext.module()).ifPresent(newType::addComment);
                moduleContext.module().getDescription().ifPresent(newType::setDescription);
                moduleContext.module().getReference().ifPresent(newType::setReference);
            }
            newType.setSchemaPath((List) identity.getPath().getPathFromRoot());

            qNameConstant(newType, BindingMapping.QNAME_STATIC_FIELD_NAME, identity.getQName());

            moduleContext.addIdentityType(identity.getQName(), newType);
        }
        return newType;
    }
}
