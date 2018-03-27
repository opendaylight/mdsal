/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.runtime.context;

import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil.packageNameForGeneratedType;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil.packageNameWithNamespacePrefix;
import static org.opendaylight.mdsal.binding.javav2.util.BindingMapping.getRootPackageName;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.opendaylight.mdsal.binding.javav2.generator.util.ReferencedTypeImpl;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingNamespaceType;
import org.opendaylight.mdsal.binding.javav2.util.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionNodeContainer;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationNodeContainer;
import org.opendaylight.yangtools.yang.model.api.OperationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.util.ModuleDependencySort;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;

/**
 * A Lightweight generator only provides referenced types and schema nodes for runtime.
 * One should use reflections of the given class to fetch information instead of from GTs or GTOs.
 */
@Beta
public class RuntimeBindingGenerator {
    private final SchemaContext context;
    private final BiMap<Type, SchemaNode> typeToSchemas = HashBiMap.create();
    private final BiMap<SchemaPath,Type> targetToAugmentation = HashBiMap.create();
    private final Multimap<Type, AugmentationSchemaNode> augmentationToSchemas = HashMultimap.create();
    private final Multimap<Type, Type> choiceToCases = HashMultimap.create();
    private final Map<QName, Type> identities = new HashMap<>();

    //TODO: these two constants should be centrally defined in common class
    //that would be shared between bindings.
    /**
     * Constant with the concrete name of identifier.
     */
    private static final String AUGMENT_IDENTIFIER_NAME = "augment-identifier";

    /**
     * Constant with the concrete name of namespace.
     */
    private static final String YANG_EXT_NAMESPACE = "urn:opendaylight:yang:extension:yang-ext";


    public RuntimeBindingGenerator(final SchemaContext context) {
        this.context = context;
        generateTypes();
    }

    private void generateTypes() {
        Preconditions.checkArgument(context != null, "Schema Context reference cannot be NULL.");
        Preconditions.checkState(context.getModules() != null, "Schema Context does not contain defined modules.");

        final List<Module> contextModules = ModuleDependencySort.sort(context.getModules());

        for (final Module contextModule : contextModules) {
            generateIdentTypes(contextModule);
            generateDataNodeTypes(contextModule, null, contextModule);
            generateAugmentationTypes(contextModule);
            generateRpcTypes(contextModule);
        }
    }

    public BiMap<Type, Object> getTypeToSchemas() {
        return ImmutableBiMap.copyOf(typeToSchemas);
    }

    public BiMap<SchemaPath, Type> getTargetToAugmentation() {
        return ImmutableBiMap.copyOf(targetToAugmentation);
    }

    public Multimap<Type, AugmentationSchemaNode> getAugmentationToSchemas() {
        return ImmutableMultimap.copyOf(augmentationToSchemas);
    }

    public Multimap<Type, Type> getChoiceToCases() {
        return ImmutableMultimap.copyOf(choiceToCases);
    }

    public Map<QName, Type> getIdentities() {
        return ImmutableMap.copyOf(identities);
    }

    private void generateIdentTypes(final Module module) {
        module.getIdentities().forEach(node -> generateIdentType(module, node));
    }

    private void generateIdentType(final Module module, final IdentitySchemaNode ident) {
        final String packageName = packageNameForGeneratedType(BindingMapping.getRootPackageName(module),
            ident.getPath(), BindingNamespaceType.Identity);
        this.identities.put(ident.getQName(), new ReferencedTypeImpl(packageName, ident.getQName().getLocalName(),
            true, false));
    }

    private void generateDataNodeTypes(final Module module, final Type parent, final DataNodeContainer container) {
        generateTypedefTypes(module, container);
        if (container instanceof NotificationNodeContainer) {
            generateNotificationTypes(module, (NotificationNodeContainer) container);
        }

        if (container instanceof ActionNodeContainer) {
            generateActionTypes(module, (ActionNodeContainer) container);
        }
        container.getChildNodes().stream().filter(node -> resolveDataSchemaNodesCheck(module, context, node))
            .forEach(node -> generateDataNodeType(module, parent, node));
    }

    private void generateActionTypes(final Module module, final ActionNodeContainer actionNodeContainer) {
        actionNodeContainer.getActions().stream().forEach(node -> generateOperationType(module,  node));
    }

    private void generateRpcTypes(final Module module) {
        module.getRpcs().stream().forEach(node -> generateOperationType(module, node));
    }

    private void generateOperationType(final Module module, final OperationDefinition operation) {
        final String packageName = BindingMapping.getRootPackageName(module);
        final StringBuilder sb = new StringBuilder(operation.getQName().getLocalName()).append('_');
        if (operation instanceof RpcDefinition) {
            sb.append("Action");
        } else {
            sb.append("Rpc");
        }

        final Type type = addTypeToSchema(packageName, sb.toString(), operation);
        generateTypedefTypes(module, operation.getTypeDefinitions());
        generateOperationArgType(module, type, operation.getInput());
        generateOperationArgType(module, type, operation.getOutput());
    }

    private void generateOperationArgType(final Module module, final Type parent, final DataNodeContainer argNode) {
        final String packageName = packageNameForGeneratedType(BindingMapping.getRootPackageName(module),
            ((SchemaNode) argNode).getPath(), BindingNamespaceType.Data);

        final String typeName = new StringBuilder(parent.getName()).append('_')
            .append(((SchemaNode) argNode).getQName().getLocalName()).toString();

        final Type type = addTypeToSchema(packageName, typeName, (SchemaNode) argNode);
        generateDataNodeTypes(module, type, argNode);
    }

    private void generateNotificationTypes(final Module module, final NotificationNodeContainer notificationContainer) {
        notificationContainer.getNotifications().stream().forEach(node -> generateNotificationType(module, node));
    }

    private void generateNotificationType(final Module module, final NotificationDefinition notification) {
        final String packageName = packageNameForGeneratedType(BindingMapping.getRootPackageName(module),
            notification.getPath(), BindingNamespaceType.Data);
        final Type type = addTypeToSchema(packageName, notification);
        generateDataNodeTypes(module, type, notification);
    }

    private void generateTypedefTypes(final Module module, final DataNodeContainer container) {
        container.getTypeDefinitions().forEach(typedef -> generateTypedefType(module, typedef));
    }

    private void generateTypedefTypes(final Module module, final Set<TypeDefinition<?>> typedefs) {
        typedefs.forEach(typedef -> generateTypedefType(module, typedef));
    }

    private static Module findAugmentTargetModule(final SchemaContext schemaContext,
                                                 final AugmentationSchemaNode aug) {
        Preconditions.checkNotNull(aug, "Augmentation schema can not be null.");
        final QName first = aug.getTargetPath().getPathFromRoot().iterator().next();
        return schemaContext.findModule(first.getModule()).orElse(null);
    }

    private void generateAugmentationTypes(final Module module) {
        module.getAugmentations().stream()
            .filter(aug -> !module.equals(findAugmentTargetModule(context, aug)))
            .collect(Collectors.groupingBy(AugmentationSchemaNode::getTargetPath)).entrySet().stream()
            .forEach(entry -> generateAugmentationType(module, entry));
    }

    private void generateAugmentationType(final Module module, final Map.Entry<SchemaPath, List<AugmentationSchemaNode>> entry)  {
        final DataSchemaNode targetNode = (DataSchemaNode) SchemaContextUtil.findDataSchemaNode(context, entry.getKey());

        String augIdentifier = null;
        for (AugmentationSchemaNode aug : entry.getValue()) {
            // FIXME: when there're multiple augment identifiers for augmentations of same target,
            // it would pick the first identifier.
            augIdentifier = getAugmentIdentifier(aug.getUnknownSchemaNodes());
            break;
        }

        if (augIdentifier == null) {
            augIdentifier = new StringBuilder(module.getName())
                .append('_').append(targetNode.getQName().getLocalName()).toString();
        }

        final String packageName = packageNameWithNamespacePrefix(getRootPackageName(module),
            BindingNamespaceType.Data);

        if (!(targetNode instanceof ChoiceSchemaNode)) {
            final Type type = new ReferencedTypeImpl(packageName, augIdentifier,
                true, false);
            this.augmentationToSchemas.putAll(type, entry.getValue());
            this.targetToAugmentation.put(entry.getKey(), type);
        }

        entry.getValue().stream().forEach(aug -> generateDataNodeTypes(module,
            this.typeToSchemas.inverse().get(targetNode), aug));
    }

    private void generateTypedefType(final Module module, final TypeDefinition typedef) {
        final String packageName = packageNameWithNamespacePrefix(getRootPackageName(module),
            BindingNamespaceType.Typedef);
        addTypeToSchema(packageName, typedef);
    }

    private Type generateDataType(final Module module, final DataSchemaNode dataNode) {
        final String packageName = packageNameForGeneratedType(BindingMapping.getRootPackageName(module),
            dataNode.getPath(), BindingNamespaceType.Data);
        return addTypeToSchema(packageName, dataNode);
    }

    private void generateDataNodeType(final Module module, final Type parent, final DataSchemaNode dataNode) {
        final Type type = generateDataType(module, dataNode);
        if (dataNode instanceof DataNodeContainer) {
            if (dataNode instanceof CaseSchemaNode) {
                this.choiceToCases.put(parent, type);
            }
            generateDataNodeTypes(module, type, (DataNodeContainer) dataNode);
        } else if(dataNode instanceof ChoiceSchemaNode) {
            ((ChoiceSchemaNode) dataNode).getCases().values().stream()
                .filter(caseNode -> resolveDataSchemaNodesCheck(module, context, caseNode))
                .forEach(caseNode -> generateDataNodeType(module, type, caseNode));
        }
    }

    private Type addTypeToSchema(final String packageName, final SchemaNode node) {
        return addTypeToSchema(packageName, node.getQName().getLocalName(), node);
    }

    private Type addTypeToSchema(final String packageName, final String rawTypeName, final SchemaNode node) {
        final Type type = new ReferencedTypeImpl(packageName, rawTypeName,
            true, false);
        this.typeToSchemas.put(type, node);
        return type;
    }

    private static boolean resolveDataSchemaNodesCheck(final Module module, final SchemaContext schemaContext,
                                               final DataSchemaNode schemaNode) {
        if (!schemaNode.isAugmenting()) {
            return true;
        }

        final QName qname = schemaNode.getPath().getLastComponent();
        final Module originalModule = schemaContext.findModule(qname.getModule()).orElse(null);
        return module.equals(originalModule);
    }

    /**
     * @param unknownSchemaNodes unknown schema nodes
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

}
