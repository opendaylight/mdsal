/*
 * Copyright (c) 2018 ZTE Corp. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.util;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.mdsal.binding.model.util.BindingGeneratorUtil.packageNameForGeneratedType;
import static org.opendaylight.mdsal.binding.spec.naming.BindingMapping.getClassName;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.mdsal.binding.common.JavaTypeName;
import org.opendaylight.mdsal.binding.generator.api.BindingRuntimeTypes;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.util.ReferencedTypeImpl;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionNodeContainer;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationNodeContainer;
import org.opendaylight.yangtools.yang.model.api.OperationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.util.ModuleDependencySort;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;

/**
 * A Lightweight generator only provides referenced types and schema nodes for runtime.
 * One should use reflections of the given class to fetch information instead of from GTs or GTOs.
 */
//TODO: Splits out an abstract super class - say AbstractRuntimeBindingGenerator to share
//common progress of walking all type nodes between binding runtimes.
@Beta
public final class RuntimeBindingGenerator {
    private final SchemaContext context;
    private final Map<Type, AugmentationSchemaNode> augmentationToSchema = new HashMap<>();
    private final BiMap<Type, WithStatus> typeToDefiningSchema = HashBiMap.create();
    private final Multimap<Type, Type> choiceToCases = HashMultimap.create();
    private final Map<QName, Type> identities = new HashMap<>();

    //TODO: These two constants should be centrally defined in common class
    //that would be shared between bindings.
    /**
     * Constant with the concrete name of identifier.
     */
    private static final String AUGMENT_IDENTIFIER_NAME = "augment-identifier";

    /**
     * Constant with the concrete name of namespace.
     */
    private static final String YANG_EXT_NAMESPACE = "urn:opendaylight:yang:extension:yang-ext";

    RuntimeBindingGenerator(final SchemaContext context) {
        Preconditions.checkArgument(context != null, "Schema Context reference cannot be NULL.");
        this.context = context;
        generateRuntimeTypes();
    }

    private String packageNameForNode(final Module module, final SchemaNode node) {
        return packageNameForGeneratedType(packageNameForModule(module), node.getPath());
    }

    private String packageNameForModule(final Module module) {
        //TODO: Caches allocation of root package name to avoid duplications.
        return BindingMapping.getRootPackageName(module.getQNameModule());
    }

    private Boolean dataNodeFilter(final Module module, final DataSchemaNode node) {
        return resolveDataSchemaNodesCheck(node);
    }

    private void generateActionTypes(final Module module, final ActionNodeContainer actionNodeContainer) {
        actionNodeContainer.getActions().forEach(node -> generateActionType(module,  node));
    }

    private void generateRuntimeTypes() {
        Preconditions.checkState(context.getModules() != null, "Schema Context does not contain defined modules.");
        final List<Module> contextModules = ModuleDependencySort.sort(context.getModules());

        for (final Module contextModule : contextModules) {
            generateIdentTypes(contextModule);
            generateTypes(contextModule, null, contextModule);
            generateAugmentationTypes(contextModule);
            generateRpcTypes(contextModule);
        }
    }

    public BindingRuntimeTypes getRuntimeTypes() {
        return new BindingRuntimeTypes(augmentationToSchema, typeToDefiningSchema, choiceToCases, identities);
    }

    private void generateIdentTypes(final Module module) {
        module.getIdentities().forEach(node -> generateIdentType(module, node));
    }

    private void generateIdentType(final Module module, final IdentitySchemaNode ident) {
        final String packageName = packageNameForNode(module, ident);
        this.identities.put(ident.getQName(), newType(packageName, ident.getQName().getLocalName()));
    }

    private void generateDataTypes(final Module module, final Type parent, final DataNodeContainer container) {
        container.getChildNodes().stream().filter(node -> dataNodeFilter(module, node))
            .forEach(node -> generateNodeType(module, parent, node));
    }

    private void generateTypes(final Module module, final Type parent, final DataNodeContainer container) {
        generateTypedefTypes(module, container);
        generateGroupingTypes(module, parent, container);
        generateDataTypes(module, parent, container);

        //TODO: Generates types of uses augmentations.

        if (container instanceof NotificationNodeContainer) {
            generateNotificationTypes(module, (NotificationNodeContainer) container);
        }

        if (container instanceof ActionNodeContainer) {
            generateActionTypes(module, (ActionNodeContainer) container);
        }
    }

    private void generateRpcTypes(final Module module) {
        module.getRpcs().forEach(node -> generateRpcType(module, node));
    }

    private void generateRpcType(final Module module, final RpcDefinition rpc) {
        generateTypedefTypes(module, rpc.getTypeDefinitions());
        generateRpcArgType(module, rpc, rpc.getInput());
        generateRpcArgType(module, rpc, rpc.getOutput());
    }

    private void generateRpcArgType(final Module module, final RpcDefinition rpc,
                                       final ContainerSchemaNode argNode) {
        final String packageName = BindingMapping.getRootPackageName(module.getQNameModule());

        final String typeName = BindingMapping.getClassName(rpc.getQName())
            + BindingMapping.getClassName(argNode.getQName());

        final Type type = addTypeToSchema(packageName, typeName, (WithStatus) argNode);
        generateTypes(module, type, argNode);
    }

    private void generateActionType(final Module module, final OperationDefinition operation) {
        final String packageName = packageNameForGeneratedType(
            BindingMapping.getRootPackageName(module.getQNameModule()), operation.getPath());
        final String className = BindingMapping.getClassName(operation.getQName());

        addTypeToSchema(packageName, className, operation);
        generateTypedefTypes(module, operation.getTypeDefinitions());
        generateActionArgType(module, operation, operation.getInput());
        generateActionArgType(module, operation, operation.getOutput());
    }

    private void generateActionArgType(final Module module, final OperationDefinition operation,
                                          final ContainerSchemaNode argNode) {
        final String packageName = packageNameForGeneratedType(
            BindingMapping.getRootPackageName(module.getQNameModule()), argNode.getPath());

        final String typeName = BindingMapping.getClassName(argNode.getQName());

        final Type type = addTypeToSchema(packageName, typeName, (WithStatus) argNode);
        generateTypes(module, type, argNode);
    }

    private void generateNotificationTypes(final Module module, final NotificationNodeContainer notificationContainer) {
        notificationContainer.getNotifications().forEach(node -> generateNotificationType(module, node));
    }

    private void generateNotificationType(final Module module, final NotificationDefinition notification) {
        final String packageName = packageNameForNode(module, (SchemaNode) notification);
        final Type type = addTypeToSchema(packageName, notification);
        generateTypes(module, type, notification);
    }

    private void generateTypedefTypes(final Module module, final DataNodeContainer container) {
        container.getTypeDefinitions().forEach(typedef -> generateTypedefType(module, typedef));
    }

    private void generateGroupingTypes(final Module module, final Type parent, final DataNodeContainer container) {
        container.getGroupings().forEach(grouping -> generateNodeType(module, parent, grouping));
    }

    private void generateTypedefTypes(final Module module, final Set<TypeDefinition<?>> typedefs) {
        typedefs.forEach(typedef -> generateTypedefType(module, typedef));
    }

    protected void generateAugmentationTypes(final Module module) {
        module.getAugmentations().forEach(node -> generateAugmentationType(module, node));
    }

    private String generateAugIdentifier(final String packageName, final String initIdentifier,final int rank) {
        final Type temp = newType(packageName, initIdentifier + rank);
        if (this.typeToDefiningSchema.get(temp) != null) {
            return generateAugIdentifier(packageName, initIdentifier, rank + 1);
        }
        return initIdentifier + rank;
    }

    protected void generateAugmentationType(final Module module, AugmentationSchemaNode node)  {
        final DataSchemaNode targetNode = (DataSchemaNode) SchemaContextUtil
            .findDataSchemaNode(context, node.getTargetPath());
        final Type type;
        if (!(targetNode instanceof ChoiceSchemaNode)) {
            final String packageName =packageNameForModule(module);
            String augIdentifier;
            augIdentifier = getAugmentIdentifier(node.getUnknownSchemaNodes());
            if (augIdentifier == null) {
                augIdentifier = new StringBuilder().append(targetNode.getQName().getLocalName()).toString();
                if (this.typeToDefiningSchema.get(newType(packageName, augIdentifier)) != null) {
                    augIdentifier = generateAugIdentifier(packageName, augIdentifier, 1);
                }
            }
            type = addTypeToSchema(packageName, augIdentifier, node);
            this.augmentationToSchema.put(type, node);
        } else {
            if (targetNode.isAddedByUses()) {
                type = this.typeToDefiningSchema.inverse()
                    .get(((DerivableSchemaNode) targetNode).getOriginal().orElse(null));
            } else {
                type = this.typeToDefiningSchema.inverse().get(targetNode);
            }
        }

        generateTypes(module, type, node);
    }

    private void generateTypedefType(final Module module, final TypeDefinition typedef) {
        final String packageName = packageNameForModule(module);
        addTypeToSchema(packageName, typedef);
    }

    private Type generateType(final Module module, final SchemaNode node) {
        final String packageName = packageNameForNode(module, node);
        return addTypeToSchema(packageName, node);
    }

    private void generateNodeType(final Module module, final Type parent, final SchemaNode node) {
        final Type type = generateType(module, node);
        if (node instanceof DataNodeContainer) {
            if (node instanceof CaseSchemaNode) {
                requireNonNull(parent, "Parent type can not be null for " + node);
                this.choiceToCases.put(parent, type);
            }
            generateTypes(module, type, (DataNodeContainer) node);
        } else if(node instanceof ChoiceSchemaNode) {
            ((ChoiceSchemaNode) node).getCases().values().stream()
                .filter(caseNode -> dataNodeFilter(module, caseNode))
                .forEach(caseNode -> generateNodeType(module, type, caseNode));
        }
    }

    private Type addTypeToSchema(final String packageName, final SchemaNode node) {
        return addTypeToSchema(packageName, node.getQName().getLocalName(), node);
    }

    private Type addTypeToSchema(final String packageName, final String rawTypeName, final WithStatus node) {
        final Type type = newType(packageName, rawTypeName);
        this.typeToDefiningSchema.put(type, node);
        return type;
    }

    private Type newType(final String normalizedPackageName, final String rawTypeName) {
        return new ReferencedTypeImpl(JavaTypeName.create(normalizedPackageName, getClassName(rawTypeName)));
    }

    static boolean resolveDataSchemaNodesCheck(final DataSchemaNode schemaNode) {
        return !schemaNode.isAugmenting() && !schemaNode.isAddedByUses();
    }

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
}
