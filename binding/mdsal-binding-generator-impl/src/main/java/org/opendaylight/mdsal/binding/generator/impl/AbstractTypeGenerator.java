/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.mdsal.binding.model.util.Types.STRING;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.AccessModifier;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.AnnotableTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.TypeMemberBuilder;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.mdsal.binding.yang.types.AbstractTypeProvider;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;

abstract class AbstractTypeGenerator {
    private static final JavaTypeName DEPRECATED_ANNOTATION = JavaTypeName.create(Deprecated.class);
    private static final JavaTypeName OVERRIDE_ANNOTATION = JavaTypeName.create(Override.class);

    private final Map<QNameModule, ModuleContext> genCtx = new HashMap<>();

    /**
     * Provide methods for converting YANG types to JAVA types.
     */
    private final AbstractTypeProvider typeProvider;

    /**
     * Holds reference to schema context to resolve data of augmented element when creating augmentation builder.
     */
    private final @NonNull EffectiveModelContext schemaContext;

    AbstractTypeGenerator(final EffectiveModelContext context, final AbstractTypeProvider typeProvider,
            final Map<SchemaNode, JavaTypeName> renames) {
        this.schemaContext = requireNonNull(context);
        this.typeProvider = requireNonNull(typeProvider);
    }

    final @NonNull EffectiveModelContext schemaContext() {
        return schemaContext;
    }

    final Collection<ModuleContext> moduleContexts() {
        return genCtx.values();
    }

    final ModuleContext moduleContext(final QNameModule module) {
        return requireNonNull(genCtx.get(module), () -> "Module context not found for module " + module);
    }

    final AbstractTypeProvider typeProvider() {
        return typeProvider;
    }

    abstract void addCodegenInformation(GeneratedTypeBuilderBase<?> genType, Module module);

    abstract void addCodegenInformation(GeneratedTypeBuilderBase<?> genType, Module module, SchemaNode node);

    abstract void addCodegenInformation(GeneratedTypeBuilder interfaceBuilder, Module module, String description,
            Collection<? extends SchemaNode> nodes);

    abstract void addComment(TypeMemberBuilder<?> genType, DocumentedNode node);

    abstract void addRpcMethodComment(TypeMemberBuilder<?> genType, RpcDefinition node);

//    private Optional<ActionDefinition> findOrigAction(final DataNodeContainer parent, final ActionDefinition action) {
//        final QName qname = action.getQName();
//        for (UsesNode uses : parent.getUses()) {
//            final GroupingDefinition grp = uses.getSourceGrouping();
//            // Target grouping may reside in a different module, hence we need to rebind the QName to match grouping's
//            // namespace
//            final Optional<ActionDefinition> found = grp.findAction(qname.bindTo(grp.getQName().getModule()));
//            if (found.isPresent()) {
//                final ActionDefinition result = found.get();
//                return result.isAddedByUses() ? findOrigAction(grp, result) : found;
//            }
//        }
//
//        return Optional.empty();
//    }

//    private void usesAugmentationToGenTypes(final ModuleContext context, final AugmentationSchemaNode augSchema,
//            final UsesNode usesNode, final DataNodeContainer usesNodeParent, final boolean inGrouping) {
//        checkArgument(augSchema != null, "Augmentation Schema cannot be NULL.");
//        checkState(augSchema.getTargetPath() != null,
//                "Augmentation Schema does not contain Target Path (Target Path is NULL).");
//
//        processUsesAugments(augSchema, context, inGrouping);
//        final SchemaNodeIdentifier targetPath = augSchema.getTargetPath();
//        final SchemaNode targetSchemaNode = findOriginalTargetFromGrouping(targetPath, usesNode);
//        if (targetSchemaNode == null) {
//            throw new IllegalArgumentException("augment target not found: " + targetPath);
//        }
//
//        GeneratedTypeBuilder targetTypeBuilder = findChildNodeByPath(targetSchemaNode.getPath());
//        if (targetTypeBuilder == null) {
//            targetTypeBuilder = findCaseByPath(targetSchemaNode.getPath());
//        }
//        if (targetTypeBuilder == null) {
//            throw new NullPointerException("Target type not yet generated: " + targetSchemaNode);
//        }
//
//        if (!(targetSchemaNode instanceof ChoiceSchemaNode)) {
//            if (usesNodeParent instanceof SchemaNode) {
//                addRawAugmentGenTypeDefinition(context,
//                    packageNameForAugmentedGeneratedType(context.modulePackageName(),
//                        ((SchemaNode) usesNodeParent).getPath()),
//                    targetTypeBuilder.build(), augSchema, inGrouping);
//            } else {
//                addRawAugmentGenTypeDefinition(context, targetTypeBuilder.build(), augSchema, inGrouping);
//            }
//        } else {
//            generateTypesFromAugmentedChoiceCases(context, targetTypeBuilder.build(),
//                (ChoiceSchemaNode) targetSchemaNode, augSchema.getChildNodes(), usesNodeParent, inGrouping);
//        }
//    }
//
//    /**
//     * Convenient method to find node added by uses statement.
//     *
//     * @param targetPath node path
//     * @param parentUsesNode parent of uses node
//     * @return node from its original location in grouping
//     */
//    private static DataSchemaNode findOriginalTargetFromGrouping(final SchemaNodeIdentifier targetPath,
//            final UsesNode parentUsesNode) {
//        SchemaNode result = parentUsesNode.getSourceGrouping();
//        for (final QName node : targetPath.getNodeIdentifiers()) {
//            // FIXME: this dispatch is rather ugly, we probably want to refactor it a bit
//            if (result instanceof DataNodeContainer) {
//                final QName resultNode = node.bindTo(result.getQName().getModule());
//
//                SchemaNode found = ((DataNodeContainer) result).dataChildByName(resultNode);
//                if (found == null) {
//                    if (result instanceof ActionNodeContainer) {
//                        found = ((ActionNodeContainer) result).findAction(resultNode).orElse(null);
//                    }
//                    if (found == null && result instanceof NotificationNodeContainer) {
//                        found = ((NotificationNodeContainer) result).findNotification(resultNode).orElse(null);
//                    }
//                }
//                result = found;
//            } else if (result instanceof ChoiceSchemaNode) {
//                result = findNamedCase((ChoiceSchemaNode) result, node.getLocalName());
//            } else if (result instanceof ActionDefinition) {
//                final ActionDefinition action = (ActionDefinition) result;
//                final QName resultNode = node.bindTo(result.getQName().getModule());
//
//                final InputSchemaNode input = action.getInput();
//                final OutputSchemaNode output = action.getOutput();
//                if (resultNode.equals(input.getQName())) {
//                    result = input;
//                } else if (resultNode.equals(output.getQName())) {
//                    result = output;
//                } else {
//                    result = null;
//                }
//            } else if (result != null) {
//                throw new IllegalStateException("Cannot handle " + result);
//            }
//        }
//        if (result == null) {
//            return null;
//        }
//
//        if (result instanceof DerivableSchemaNode) {
//            DerivableSchemaNode castedResult = (DerivableSchemaNode) result;
//            Optional<? extends SchemaNode> originalNode = castedResult.getOriginal();
//            if (castedResult.isAddedByUses() && originalNode.isPresent()) {
//                result = originalNode.get();
//            }
//        }
//
//        if (result instanceof DataSchemaNode) {
//            DataSchemaNode resultDataSchemaNode = (DataSchemaNode) result;
//            if (resultDataSchemaNode.isAddedByUses()) {
//                // The original node is required, but we have only the copy of
//                // the original node.
//                // Maybe this indicates a bug in Yang parser.
//                throw new IllegalStateException("Failed to generate code for augment in " + parentUsesNode);
//            }
//
//            return resultDataSchemaNode;
//        }
//
//        throw new IllegalStateException(
//            "Target node of uses-augment statement must be DataSchemaNode. Failed to generate code for augment in "
//                    + parentUsesNode);
//    }

    /**
     * Generates list of generated types for all the cases of a choice which are added to the choice through
     * the augment.
     *
     * @param module current module
     * @param basePackageName string contains name of package to which augment belongs. If an augmented choice is
     *                        from an other package (pcg1) than an augmenting choice (pcg2) then case's
     *                        of the augmenting choice will belong to pcg2.
     * @param targetType Type which represents target choice
     * @param targetNode node which represents target choice
     * @param augmentedNodes set of choice case nodes for which is checked if are/are not added to choice through
     *                       augmentation
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>basePackageName</code> is null</li>
     *             <li>if <code>targetType</code> is null</li>
     *             <li>if <code>augmentedNodes</code> is null</li>
     *             </ul>
     */
    // FIXME: nullness rules need to untangled in this method
//    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH")
//    private void generateTypesFromAugmentedChoiceCases(final ModuleContext context,
//            final Type targetType, final ChoiceSchemaNode targetNode,
//            final Iterable<? extends DataSchemaNode> augmentedNodes,
//            final DataNodeContainer usesNodeParent, final boolean inGrouping) {
//        checkArgument(targetType != null, "Referenced Choice Type cannot be NULL.");
//        checkArgument(augmentedNodes != null, "Set of Choice Case Nodes cannot be NULL.");
//
//        for (final DataSchemaNode caseNode : augmentedNodes) {
//            if (caseNode != null) {
//                final GeneratedTypeBuilder caseTypeBuilder = addDefaultInterfaceDefinition(context, caseNode);
//                caseTypeBuilder.addImplementsType(targetType);
//                addConcreteInterfaceMethods(caseTypeBuilder);
//
//                CaseSchemaNode node = null;
//                final String caseLocalName = caseNode.getQName().getLocalName();
//                if (caseNode instanceof CaseSchemaNode) {
//                    node = (CaseSchemaNode) caseNode;
//                } else if (findNamedCase(targetNode, caseLocalName) == null) {
//                    final String targetNodeLocalName = targetNode.getQName().getLocalName();
//                    for (DataSchemaNode dataSchemaNode : usesNodeParent.getChildNodes()) {
//                        if (dataSchemaNode instanceof ChoiceSchemaNode
//                                && targetNodeLocalName.equals(dataSchemaNode.getQName().getLocalName())) {
//                            node = findNamedCase((ChoiceSchemaNode) dataSchemaNode, caseLocalName);
//                            break;
//                        }
//                    }
//                } else {
//                    node = findNamedCase(targetNode, caseLocalName);
//                }
//                final Iterable<? extends DataSchemaNode> childNodes = node.getChildNodes();
//                if (childNodes != null) {
//                    resolveDataSchemaNodes(context, caseTypeBuilder, findChildOfType(targetNode), childNodes,
//                        inGrouping);
//                }
//                context.addCaseType(caseNode.getPath(), caseTypeBuilder);
//                context.addChoiceToCaseMapping(targetType, caseTypeBuilder, node);
//            }
//        }
//    }
//
//    private GeneratedTypeBuilder findChildOfType(final ChoiceSchemaNode targetNode) {
//        final SchemaPath nodePath = targetNode.getPath();
//        final SchemaPath parentSp = nodePath.getParent();
//        if (parentSp.getParent() == null) {
//            return moduleContext(nodePath.getLastComponent().getModule()).getModuleNode();
//        }
//
//        final SchemaNode parent = findDataSchemaNode(schemaContext, parentSp);
//        GeneratedTypeBuilder childOfType = null;
//        if (parent instanceof CaseSchemaNode) {
//            childOfType = findCaseByPath(parent.getPath());
//        } else if (parent instanceof DataSchemaNode || parent instanceof NotificationDefinition) {
//            childOfType = findChildNodeByPath(parent.getPath());
//        } else if (parent instanceof GroupingDefinition) {
//            childOfType = findGroupingByPath(parent.getPath());
//        }
//
//        if (childOfType == null) {
//            throw new IllegalArgumentException("Failed to find parent type of choice " + targetNode);
//        }
//
//        return childOfType;
//    }
//
//    private static CaseSchemaNode findNamedCase(final ChoiceSchemaNode choice, final String caseName) {
//        final List<? extends CaseSchemaNode> cases = choice.findCaseNodes(caseName);
//        return cases.isEmpty() ? null : cases.get(0);
//    }

//    private void processContextRefExtension(final LeafSchemaNode leaf, final MethodSignatureBuilder getter,
//            final Module module) {
//        for (final UnknownSchemaNode node : leaf.getUnknownSchemaNodes()) {
//            final QName nodeType = node.getNodeType();
//            if ("context-reference".equals(nodeType.getLocalName())) {
//                final String nodeParam = node.getNodeParameter();
//                IdentitySchemaNode identity = null;
//                String basePackageName = null;
//                final Iterable<String> splittedElement = COLON_SPLITTER.split(nodeParam);
//                final Iterator<String> iterator = splittedElement.iterator();
//                final int length = Iterables.size(splittedElement);
//                if (length == 1) {
//                    identity = findIdentityByName(module.getIdentities(), iterator.next());
//                    basePackageName = BindingMapping.getRootPackageName(module.getQNameModule());
//                } else if (length == 2) {
//                    final String prefix = iterator.next();
//                    final Module dependentModule = findModuleFromImports(module.getImports(), prefix);
//                    if (dependentModule == null) {
//                        throw new IllegalArgumentException("Failed to process context-reference: unknown prefix "
//                                + prefix);
//                    }
//                    identity = findIdentityByName(dependentModule.getIdentities(), iterator.next());
//                    basePackageName = BindingMapping.getRootPackageName(dependentModule.getQNameModule());
//                } else {
//                    throw new IllegalArgumentException("Failed to process context-reference: unknown identity "
//                            + nodeParam);
//                }
//                if (identity == null) {
//                    throw new IllegalArgumentException("Failed to process context-reference: unknown identity "
//                            + nodeParam);
//                }
//
//                final AnnotationTypeBuilder rc = getter.addAnnotation(ROUTING_CONTEXT);
//                final String packageName = packageNameForGeneratedType(basePackageName, identity.getPath());
//                final String genTypeName = BindingMapping.getClassName(identity.getQName().getLocalName());
//                rc.addParameter("value", packageName + "." + genTypeName + ".class");
//            }
//        }
//    }
//
//    private static IdentitySchemaNode findIdentityByName(final Collection<? extends IdentitySchemaNode> identities,
//            final String name) {
//        for (final IdentitySchemaNode id : identities) {
//            if (id.getQName().getLocalName().equals(name)) {
//                return id;
//            }
//        }
//        return null;
//    }
//
//    private Module findModuleFromImports(final Collection<? extends ModuleImport> imports, final String prefix) {
//        for (final ModuleImport imp : imports) {
//            if (imp.getPrefix().equals(prefix)) {
//                return schemaContext.findModule(imp.getModuleName(), imp.getRevision()).orElse(null);
//            }
//        }
//        return null;
//    }

    private Type createReturnTypeForUnion(final GeneratedTOBuilder genTOBuilder, final UnionTypeDefinition typeDef,
            final GeneratedTypeBuilder typeBuilder, final Module parentModule) {
        final GeneratedTOBuilder returnTypeBuilder = typeProvider.newGeneratedTOBuilder(genTOBuilder.getIdentifier());
        returnTypeBuilder.setIsUnion(true);
        addCodegenInformation(returnTypeBuilder, parentModule, typeDef);
        returnTypeBuilder.setSchemaPath(typeDef.getPath());
        returnTypeBuilder.setModuleName(parentModule.getName());
        final GeneratedTransferObject returnType = returnTypeBuilder.build();

        genTOBuilder.setTypedef(true);
        genTOBuilder.setIsUnion(true);
        AbstractTypeProvider.addUnitsToGenTO(genTOBuilder, typeDef.getUnits().orElse(null));

        createUnionBuilder(genTOBuilder, typeBuilder, returnType, parentModule);
        return returnType;
    }

    private void createUnionBuilder(final GeneratedTOBuilder genTOBuilder, final GeneratedTypeBuilder typeBuilder,
            final GeneratedTransferObject returnType, final Module parentModule) {
        // Append enclosing path hierarchy without dots
        final StringBuilder sb = new StringBuilder();
        genTOBuilder.getIdentifier().localNameComponents().forEach(sb::append);
        final GeneratedTOBuilder unionBuilder = typeProvider.newGeneratedTOBuilder(
            JavaTypeName.create(typeBuilder.getPackageName(), sb.append("Builder").toString()));
        unionBuilder.setIsUnionBuilder(true);

        final MethodSignatureBuilder method = unionBuilder.addMethod("getDefaultInstance");
        method.setReturnType(returnType);
        method.addParameter(STRING, "defaultValue");
        method.setAccessModifier(AccessModifier.PUBLIC);
        method.setStatic(true);

        final GeneratedTransferObject unionBuilderType = unionBuilder.build();
        typeProvider.getAdditionalTypes().computeIfAbsent(parentModule, key -> new HashSet<>()).add(unionBuilderType);
    }

    /**
     * Created a method signature builder as part of <code>interfaceBuilder</code>. The method signature builder is
     * created for the getter method of <code>schemaNodeName</code>. Also <code>comment</code>
     * and <code>returnType</code> information are added to the builder.
     *
     * @param interfaceBuilder generated type builder for which the getter method should be created
     * @param returnType type which represents the return type of the getter method
     * @param schemaNodeName string with schema node name. The name will be the part of the getter method name.
     * @param comment string with comment for the getter method
     * @param status status from yang file, for deprecated annotation
     * @return method signature builder which represents the getter method of <code>interfaceBuilder</code>
     */
    private MethodSignatureBuilder constructGetter(final GeneratedTypeBuilder interfaceBuilder, final Type returnType,
            final SchemaNode node) {
        final MethodSignatureBuilder getMethod = interfaceBuilder.addMethod(
            BindingMapping.getGetterMethodName(node.getQName().getLocalName()));
        getMethod.setReturnType(returnType);

        annotateDeprecatedIfNecessary(node, getMethod);
        addComment(getMethod, node);

        return getMethod;
    }

    private MethodSignatureBuilder constructOverrideGetter(final GeneratedTypeBuilder interfaceBuilder,
            final Type returnType, final SchemaNode node) {
        final MethodSignatureBuilder getter = constructGetter(interfaceBuilder, returnType, node);
        getter.addAnnotation(OVERRIDE_ANNOTATION);
        return getter;
    }

    /**
     * Builds a GeneratedTOBuilder for a UnionType {@link UnionTypeDefinition}. If more then one generated TO builder
     * is created for enclosing then all of the generated TO builders are added to <code>typeBuilder</code> as
     * enclosing transfer objects.
     *
     * @param typeDef type definition which can be of type <code>UnionType</code> or <code>BitsTypeDefinition</code>
     * @param typeBuilder generated type builder to which is added generated TO created from <code>typeDef</code>
     * @param leaf string with name for generated TO builder
     * @param parentModule parent module
     * @return generated TO builder for <code>typeDef</code>
     */
    private Type addTOToTypeBuilder(final UnionTypeDefinition typeDef,
            final GeneratedTypeBuilder typeBuilder, final DataSchemaNode leaf, final Module parentModule) {
        final List<GeneratedTOBuilder> types = typeProvider.provideGeneratedTOBuildersForUnionTypeDef(
            allocateNestedType(typeBuilder.getIdentifier(), leaf.getQName()), typeDef, leaf);

        checkState(!types.isEmpty(), "No GeneratedTOBuilder objects generated from union %s", typeDef);
        final List<GeneratedTOBuilder> genTOBuilders = new ArrayList<>(types);
        final GeneratedTOBuilder resultTOBuilder = types.remove(0);
        types.forEach(resultTOBuilder::addEnclosingTransferObject);
        genTOBuilders.forEach(typeBuilder::addEnclosingTransferObject);

        for (GeneratedTOBuilder builder : types) {
            if (builder.isUnion()) {
                final GeneratedTransferObject type = builder.build();
                createUnionBuilder(builder, typeBuilder, type, parentModule);
            }
        }

        return createReturnTypeForUnion(resultTOBuilder, typeDef, typeBuilder, parentModule);
    }

    /**
     * Builds generated TO builders for <code>typeDef</code> of type {@link BitsTypeDefinition} which are also added
     * to <code>typeBuilder</code> as enclosing transfer object. If more then one generated TO builder is created
     * for enclosing then all of the generated TO builders are added to <code>typeBuilder</code> as enclosing transfer
     * objects.
     *
     * @param typeDef type definition which can be of type <code>UnionType</code> or <code>BitsTypeDefinition</code>
     * @param typeBuilder generated type builder to which is added generated TO created from <code>typeDef</code>
     * @param leaf string with name for generated TO builder
     * @param parentModule parent module
     * @return generated TO builder for <code>typeDef</code>
     */
    private GeneratedTOBuilder addTOToTypeBuilder(final BitsTypeDefinition typeDef,
            final GeneratedTypeBuilder typeBuilder, final DataSchemaNode leaf, final Module parentModule) {
        final GeneratedTOBuilder genTOBuilder = typeProvider.provideGeneratedTOBuilderForBitsTypeDefinition(
            allocateNestedType(typeBuilder.getIdentifier(), leaf.getQName()), typeDef, parentModule.getName());
        typeBuilder.addEnclosingTransferObject(genTOBuilder);
        return genTOBuilder;
    }

    private static JavaTypeName allocateNestedType(final JavaTypeName parent, final QName child) {
        // Single '$' suffix cannot come from user, this mirrors AbstractGeneratedTypeBuilder.addEnumeration()
        return parent.createEnclosed(BindingMapping.getClassName(child), "$");
    }

    private static void annotateDeprecatedIfNecessary(final WithStatus node, final AnnotableTypeBuilder builder) {
        switch (node.getStatus()) {
            case DEPRECATED:
                // FIXME: we really want to use a pre-made annotation
                builder.addAnnotation(DEPRECATED_ANNOTATION);
                break;
            case OBSOLETE:
                builder.addAnnotation(DEPRECATED_ANNOTATION).addParameter("forRemoval", "true");
                break;
            case CURRENT:
                // No-op
                break;
            default:
                throw new IllegalStateException("Unhandled status in " + node);
        }
    }
}
