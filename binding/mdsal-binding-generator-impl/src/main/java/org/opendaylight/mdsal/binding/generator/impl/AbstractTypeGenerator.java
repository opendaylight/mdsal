/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.mdsal.binding.model.util.BindingGeneratorUtil.computeDefaultSUID;
import static org.opendaylight.mdsal.binding.model.util.BindingGeneratorUtil.packageNameForAugmentedGeneratedType;
import static org.opendaylight.mdsal.binding.model.util.BindingGeneratorUtil.packageNameForGeneratedType;
import static org.opendaylight.mdsal.binding.model.util.BindingTypes.BASE_IDENTITY;
import static org.opendaylight.mdsal.binding.model.util.BindingTypes.DATA_OBJECT;
import static org.opendaylight.mdsal.binding.model.util.BindingTypes.DATA_ROOT;
import static org.opendaylight.mdsal.binding.model.util.BindingTypes.IDENTIFIABLE;
import static org.opendaylight.mdsal.binding.model.util.BindingTypes.IDENTIFIER;
import static org.opendaylight.mdsal.binding.model.util.BindingTypes.INSTANCE_IDENTIFIER;
import static org.opendaylight.mdsal.binding.model.util.BindingTypes.NOTIFICATION;
import static org.opendaylight.mdsal.binding.model.util.BindingTypes.NOTIFICATION_LISTENER;
import static org.opendaylight.mdsal.binding.model.util.BindingTypes.ROUTING_CONTEXT;
import static org.opendaylight.mdsal.binding.model.util.BindingTypes.RPC_SERVICE;
import static org.opendaylight.mdsal.binding.model.util.BindingTypes.augmentable;
import static org.opendaylight.mdsal.binding.model.util.BindingTypes.childOf;
import static org.opendaylight.mdsal.binding.model.util.BindingTypes.choiceIn;
import static org.opendaylight.mdsal.binding.model.util.Types.BOOLEAN;
import static org.opendaylight.mdsal.binding.model.util.Types.FUTURE;
import static org.opendaylight.mdsal.binding.model.util.Types.typeForClass;
import static org.opendaylight.yangtools.yang.model.util.SchemaContextUtil.findDataSchemaNode;
import static org.opendaylight.yangtools.yang.model.util.SchemaContextUtil.findNodeInSchemaContext;
import static org.opendaylight.yangtools.yang.model.util.SchemaContextUtil.findParentModule;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.mdsal.binding.model.api.AccessModifier;
import org.opendaylight.mdsal.binding.model.api.Constant;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.model.api.Restrictions;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.AnnotableTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.EnumBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.TypeMemberBuilder;
import org.opendaylight.mdsal.binding.model.util.BindingGeneratorUtil;
import org.opendaylight.mdsal.binding.model.util.ReferencedTypeImpl;
import org.opendaylight.mdsal.binding.model.util.TypeConstants;
import org.opendaylight.mdsal.binding.model.util.Types;
import org.opendaylight.mdsal.binding.model.util.generated.type.builder.GeneratedPropertyBuilderImpl;
import org.opendaylight.mdsal.binding.yang.types.AbstractTypeProvider;
import org.opendaylight.mdsal.binding.yang.types.BaseYangTypes;
import org.opendaylight.mdsal.binding.yang.types.GroupingDefinitionDependencySort;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.ActionNodeContainer;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.OperationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.DataNodeIterator;
import org.opendaylight.yangtools.yang.model.util.ModuleDependencySort;
import org.opendaylight.yangtools.yang.model.util.SchemaNodeUtils;
import org.opendaylight.yangtools.yang.model.util.type.CompatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractTypeGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(BindingGeneratorImpl.class);
    private static final Splitter COLON_SPLITTER = Splitter.on(':');
    private static final JavaTypeName DEPRECATED_ANNOTATION = JavaTypeName.create(Deprecated.class);
    private static final JavaTypeName OVERRIDE_ANNOTATION = JavaTypeName.create(Override.class);

    /**
     * Comparator based on augment target path.
     */
    private static final Comparator<AugmentationSchemaNode> AUGMENT_COMP = (o1, o2) -> {
        final Iterator<QName> thisIt = o1.getTargetPath().getPathFromRoot().iterator();
        final Iterator<QName> otherIt = o2.getTargetPath().getPathFromRoot().iterator();

        while (thisIt.hasNext()) {
            if (!otherIt.hasNext()) {
                return 1;
            }

            final int comp = thisIt.next().compareTo(otherIt.next());
            if (comp != 0) {
                return comp;
            }
        }

        return otherIt.hasNext() ? -1 : 0;
    };

    /**
     * Constant with the concrete name of identifier.
     */
    private static final String AUGMENT_IDENTIFIER_NAME = "augment-identifier";

    /**
     * Constant with the concrete name of namespace.
     */
    private static final String YANG_EXT_NAMESPACE = "urn:opendaylight:yang:extension:yang-ext";

    private final Map<QNameModule, ModuleContext> genCtx = new HashMap<>();

    /**
     * Outer key represents the package name. Outer value represents map of all
     * builders in the same package. Inner key represents the schema node name
     * (in JAVA class/interface name format). Inner value represents instance of
     * builder for schema node specified in key part.
     */
    private final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();

    /**
     * Provide methods for converting YANG types to JAVA types.
     */
    private final AbstractTypeProvider typeProvider;

    /**
     * Holds reference to schema context to resolve data of augmented element
     * when creating augmentation builder
     */
    private final SchemaContext schemaContext;

    AbstractTypeGenerator(final SchemaContext context, final AbstractTypeProvider typeProvider) {
        this.schemaContext = requireNonNull(context);
        this.typeProvider = requireNonNull(typeProvider);

        final List<Module> contextModules = ModuleDependencySort.sort(schemaContext.getModules());
        final List<ModuleContext> contexts = new ArrayList<>(contextModules.size());
        for (final Module contextModule : contextModules) {
            contexts.add(moduleToGenTypes(contextModule));
        }

        contexts.forEach(this::allAugmentsToGenTypes);
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

    private ModuleContext moduleToGenTypes(final Module module) {
        final ModuleContext context = new ModuleContext(module);
        genCtx.put(module.getQNameModule(), context);
        allTypeDefinitionsToGenTypes(context);
        groupingsToGenTypes(context, module.getGroupings());
        rpcMethodsToGenType(context);
        allIdentitiesToGenTypes(context);
        notificationsToGenType(context);

        if (!module.getChildNodes().isEmpty()) {
            final GeneratedTypeBuilder moduleType = moduleToDataType(context);
            context.addModuleNode(moduleType);
            resolveDataSchemaNodes(context, moduleType, moduleType, module.getChildNodes());
        }
        return context;
    }

    /**
     * Converts all extended type definitions of module to the list of
     * <code>Type</code> objects.
     *
     * @param module
     *            module from which is obtained set of type definitions
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if module is null</li>
     *             <li>if name of module is null</li>
     *             </ul>
     * @throws IllegalStateException
     *             if set of type definitions from module is null
     */
    private void allTypeDefinitionsToGenTypes(final ModuleContext context) {
        final Module module = context.module();
        checkArgument(module.getName() != null, "Module name cannot be NULL.");
        final DataNodeIterator it = new DataNodeIterator(module);
        final List<TypeDefinition<?>> typeDefinitions = it.allTypedefs();
        checkState(typeDefinitions != null, "Type Definitions for module %s cannot be NULL.", module.getName());

        for (final TypeDefinition<?> typedef : typeDefinitions) {
            if (typedef != null) {
                final Type type = typeProvider.generatedTypeForExtendedDefinitionType(typedef,  typedef);
                if (type != null) {
                    context.addTypedefType(typedef.getPath(), type);
                    context.addTypeToSchema(type,typedef);
                }
            }
        }
    }

    private GeneratedTypeBuilder processDataSchemaNode(final ModuleContext context, final GeneratedTypeBuilder childOf,
            final DataSchemaNode node) {
        if (node.isAugmenting() || node.isAddedByUses()) {
            return null;
        }
        final GeneratedTypeBuilder genType = addDefaultInterfaceDefinition(context, node, childOf);
        annotateDeprecatedIfNecessary(node.getStatus(), genType);

        final Module module = context.module();
        genType.setModuleName(module.getName());
        addCodegenInformation(genType, module, node);
        genType.setSchemaPath(node.getPath());
        if (node instanceof DataNodeContainer) {
            context.addChildNodeType(node, genType);
            groupingsToGenTypes(context, ((DataNodeContainer) node).getGroupings());
            processUsesAugments((DataNodeContainer) node, context);
        }
        return genType;
    }

    private void containerToGenType(final ModuleContext context, final GeneratedTypeBuilder parent,
            final GeneratedTypeBuilder childOf, final ContainerSchemaNode node) {
        final GeneratedTypeBuilder genType = processDataSchemaNode(context, childOf, node);
        if (genType != null) {
            constructGetter(parent, genType, node);
            resolveDataSchemaNodes(context, genType, genType, node.getChildNodes());
            actionsToGenType(context, genType, node);
        }
    }

    private void listToGenType(final ModuleContext context,
            final GeneratedTypeBuilder parent, final GeneratedTypeBuilder childOf, final ListSchemaNode node) {
        final GeneratedTypeBuilder genType = processDataSchemaNode(context, childOf, node);
        if (genType != null) {
            constructGetter(parent, Types.listTypeFor(genType), node);

            final List<String> listKeys = listKeys(node);
            final GeneratedTOBuilder genTOBuilder = resolveListKeyTOBuilder(context, node);
            if (genTOBuilder != null) {
                final Type identifierMarker = Types.parameterizedTypeFor(IDENTIFIER, genType);
                final Type identifiableMarker = Types.parameterizedTypeFor(IDENTIFIABLE, genTOBuilder);
                genTOBuilder.addImplementsType(identifierMarker);
                genType.addImplementsType(identifiableMarker);

                actionsToGenType(context, genType, node);
            }

            for (final DataSchemaNode schemaNode : node.getChildNodes()) {
                if (!schemaNode.isAugmenting()) {
                    addSchemaNodeToListBuilders(context, schemaNode, genType, genTOBuilder, listKeys);
                }
            }

            // serialVersionUID
            if (genTOBuilder != null) {
                final GeneratedPropertyBuilder prop = new GeneratedPropertyBuilderImpl("serialVersionUID");
                prop.setValue(Long.toString(computeDefaultSUID(genTOBuilder)));
                genTOBuilder.setSUID(prop);
            }

            typeBuildersToGenTypes(context, genType, genTOBuilder);
        }
    }

    private void processUsesAugments(final DataNodeContainer node, final ModuleContext context) {
        for (final UsesNode usesNode : node.getUses()) {
            for (final AugmentationSchemaNode augment : usesNode.getAugmentations()) {
                usesAugmentationToGenTypes(context, augment, usesNode, node);
                processUsesAugments(augment, context);
            }
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
    private void allAugmentsToGenTypes(final ModuleContext context) {
        final Module module = context.module();
        checkArgument(module != null, "Module reference cannot be NULL.");
        checkArgument(module.getName() != null, "Module name cannot be NULL.");
        checkState(module.getAugmentations() != null, "Augmentations Set cannot be NULL.");

        for (final AugmentationSchemaNode augment : resolveAugmentations(module)) {
            augmentationToGenTypes(context, augment);
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
    private static List<AugmentationSchemaNode> resolveAugmentations(final Module module) {
        checkArgument(module != null, "Module reference cannot be NULL.");
        checkState(module.getAugmentations() != null, "Augmentations Set cannot be NULL.");

        final Set<AugmentationSchemaNode> augmentations = module.getAugmentations();
        final List<AugmentationSchemaNode> sortedAugmentations = new ArrayList<>(augmentations);
        sortedAugmentations.sort(AUGMENT_COMP);

        return sortedAugmentations;
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
    private GeneratedTypeBuilder moduleToDataType(final ModuleContext context) {
        final GeneratedTypeBuilder moduleDataTypeBuilder = moduleTypeBuilder(context, "Data");
        final Module module = context.module();
        addImplementedInterfaceFromUses(module, moduleDataTypeBuilder);
        moduleDataTypeBuilder.addImplementsType(DATA_ROOT);

        addCodegenInformation(moduleDataTypeBuilder, module);
        return moduleDataTypeBuilder;
    }

    private <T extends DataSchemaNode & ActionNodeContainer> void actionsToGenType(final ModuleContext context,
            final GeneratedTypeBuilder parentBuilder, final T node) {
        final Collection<ActionDefinition> actions = node.getActions();
        if (actions.isEmpty()) {
            return;
        }

        final GeneratedTypeBuilder builder = typeProvider.newGeneratedTypeBuilder(
            parentBuilder.getIdentifier().createSibling(parentBuilder.getName() + "Service"));
        builder.addImplementsType(Types.typeForClass(RpcService.class));
        for (final ActionDefinition action : actions) {
            final String operName = BindingMapping.getClassName(action.getQName());
            final MethodSignatureBuilder method = builder.addMethod(BindingMapping.getPropertyName(operName));

            // Do not refer to annotation class, as it may not be available at runtime
            method.addAnnotation("javax.annotation", "CheckReturnValue");
            addComment(method, action);
            method.addParameter(INSTANCE_IDENTIFIER, "path");
            method.addParameter(
                createOperationContainer(context, operName, action, verifyNotNull(action.getInput())), "input");
            method.setReturnType(Types.parameterizedTypeFor(FUTURE,
                Types.parameterizedTypeFor(Types.typeForClass(RpcResult.class),
                    createOperationContainer(context, operName, action, verifyNotNull(action.getOutput())))));
        }
        addCodegenInformation(builder, context.module(), "Actions", actions);

        context.addChildNodeType(node, builder);
    }

    /**
     * Converts all <b>RPCs</b> input and output substatements of the module
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
    private void rpcMethodsToGenType(final ModuleContext context) {
        final Module module = context.module();
        checkArgument(module.getName() != null, "Module name cannot be NULL.");
        final Collection<RpcDefinition> rpcDefinitions = module.getRpcs();
        if (rpcDefinitions.isEmpty()) {
            return;
        }

        final GeneratedTypeBuilder interfaceBuilder = moduleTypeBuilder(context, "Service");
        interfaceBuilder.addImplementsType(RPC_SERVICE);

        addCodegenInformation(interfaceBuilder, module, "RPCs", rpcDefinitions);

        for (final RpcDefinition rpc : rpcDefinitions) {
            final String rpcName = BindingMapping.getClassName(rpc.getQName());
            final String rpcMethodName = BindingMapping.getPropertyName(rpcName);
            final MethodSignatureBuilder method = interfaceBuilder.addMethod(rpcMethodName);

            // Do not refer to annotation class, as it may not be available at runtime
            method.addAnnotation("javax.annotation", "CheckReturnValue");
            addComment(method, rpc);
            method.addParameter(
                createOperationContainer(context, rpcName, rpc, verifyNotNull(rpc.getInput())), "input");
            method.setReturnType(Types.parameterizedTypeFor(FUTURE,
                Types.parameterizedTypeFor(Types.typeForClass(RpcResult.class),
                    createOperationContainer(context, rpcName, rpc, verifyNotNull(rpc.getOutput())))));

            addComment(method, rpc);
        }

        context.addTopLevelNodeType(interfaceBuilder);
    }

    private Type createOperationContainer(final ModuleContext context, final String operName,
            final OperationDefinition oper, final ContainerSchemaNode schema) {
        processUsesAugments(schema, context);
        final GeneratedTypeBuilder outType = addRawInterfaceDefinition(
            JavaTypeName.create(context.modulePackageName(), operName + BindingMapping.getClassName(schema.getQName())),
            schema);
        addImplementedInterfaceFromUses(schema, outType);
        outType.addImplementsType(DATA_OBJECT);
        outType.addImplementsType(augmentable(outType));
        annotateDeprecatedIfNecessary(oper.getStatus(), outType);
        resolveDataSchemaNodes(context, outType, outType, schema.getChildNodes());
        context.addChildNodeType(schema, outType);
        return outType.build();
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
    private void notificationsToGenType(final ModuleContext context) {
        final Module module = context.module();
        checkArgument(module.getName() != null, "Module name cannot be NULL.");
        final Set<NotificationDefinition> notifications = module.getNotifications();
        if (notifications.isEmpty()) {
            return;
        }

        final GeneratedTypeBuilder listenerInterface = moduleTypeBuilder(context, "Listener");
        listenerInterface.addImplementsType(NOTIFICATION_LISTENER);

        for (final NotificationDefinition notification : notifications) {
            if (notification != null) {
                processUsesAugments(notification, context);

                final GeneratedTypeBuilder notificationInterface = addDefaultInterfaceDefinition(
                    context.modulePackageName(), notification, null, context);
                annotateDeprecatedIfNecessary(notification.getStatus(), notificationInterface);
                notificationInterface.addImplementsType(NOTIFICATION);
                context.addChildNodeType(notification, notificationInterface);

                // Notification object
                resolveDataSchemaNodes(context, notificationInterface, notificationInterface,
                    notification.getChildNodes());

                addComment(listenerInterface.addMethod("on" + notificationInterface.getName())
                    .setAccessModifier(AccessModifier.PUBLIC).addParameter(notificationInterface, "notification")
                    .setReturnType(Types.primitiveVoidType()), notification);
            }
        }

        addCodegenInformation(listenerInterface, module, "notifications", notifications);
        context.addTopLevelNodeType(listenerInterface);
    }

    /**
     * Converts all <b>identities</b> of the module to the list of
     * <code>Type</code> objects.
     *
     * @param module
     *            module from which is obtained set of all identity objects to
     *            iterate over them
     * @param schemaContext
     *            schema context only used as input parameter for method
     *            {@link BindingGeneratorImpl#identityToGenType}
     *
     */
    private void allIdentitiesToGenTypes(final ModuleContext context) {
        final Set<IdentitySchemaNode> schemaIdentities = context.module().getIdentities();

        if (schemaIdentities != null && !schemaIdentities.isEmpty()) {
            for (final IdentitySchemaNode identity : schemaIdentities) {
                identityToGenType(context, identity);
            }
        }
    }

    /**
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
     */
    private void identityToGenType(final ModuleContext context,final IdentitySchemaNode identity) {
        if (identity == null) {
            return;
        }
        final GeneratedTypeBuilder newType = typeProvider.newGeneratedTypeBuilder(JavaTypeName.create(
            packageNameForGeneratedType(context.modulePackageName(), identity.getPath()),
            BindingMapping.getClassName(identity.getQName())));
        final Set<IdentitySchemaNode> baseIdentities = identity.getBaseIdentities();
        if (!baseIdentities.isEmpty()) {
            for (IdentitySchemaNode baseIdentity : baseIdentities) {
                final QName qname = baseIdentity.getQName();
                final GeneratedTransferObject gto = typeProvider.newGeneratedTOBuilder(JavaTypeName.create(
                    BindingMapping.getRootPackageName(qname.getModule()), BindingMapping.getClassName(qname))).build();
                newType.addImplementsType(gto);
            }
        } else {
            newType.addImplementsType(BASE_IDENTITY);
        }

        final Module module = context.module();
        addCodegenInformation(newType, module, identity);
        newType.setModuleName(module.getName());
        newType.setSchemaPath(identity.getPath());

        qnameConstant(newType, JavaTypeName.create(context.modulePackageName(), BindingMapping.MODULE_INFO_CLASS_NAME),
            identity.getQName().getLocalName());

        context.addIdentityType(identity.getQName(), newType);
    }

    private static Constant qnameConstant(final GeneratedTypeBuilderBase<?> toBuilder,
            final JavaTypeName yangModuleInfo, final String localName) {
        return toBuilder.addConstant(typeForClass(QName.class), BindingMapping.QNAME_STATIC_FIELD_NAME,
            new SimpleImmutableEntry<>(yangModuleInfo, localName));
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
     *
     */
    private void groupingsToGenTypes(final ModuleContext context, final Collection<GroupingDefinition> groupings) {
        for (final GroupingDefinition grouping : new GroupingDefinitionDependencySort().sort(groupings)) {
            // Converts individual grouping to GeneratedType. Firstly generated type builder is created and every child
            // node of grouping is resolved to the method.
            final GeneratedTypeBuilder genType = addDefaultInterfaceDefinition(context, grouping);
            annotateDeprecatedIfNecessary(grouping.getStatus(), genType);
            context.addGroupingType(grouping.getPath(), genType);
            resolveDataSchemaNodes(context, genType, genType, grouping.getChildNodes());
            groupingsToGenTypes(context, grouping.getGroupings());
            processUsesAugments(grouping, context);
        }
    }

    /**
     * Adds enumeration builder created from <code>enumTypeDef</code> to
     * <code>typeBuilder</code>.
     *
     * Each <code>enumTypeDef</code> item is added to builder with its name and
     * value.
     *
     * @param enumTypeDef
     *            EnumTypeDefinition contains enum data
     * @param enumName
     *            string contains name which will be assigned to enumeration
     *            builder
     * @param typeBuilder
     *            GeneratedTypeBuilder to which will be enum builder assigned
     * @param module
     *            Module in which type should be generated
     * @return enumeration builder which contains data from
     *         <code>enumTypeDef</code>
     */
    private EnumBuilder resolveInnerEnumFromTypeDefinition(final EnumTypeDefinition enumTypeDef, final QName enumName,
            final GeneratedTypeBuilder typeBuilder, final ModuleContext context) {
        if (enumTypeDef != null && typeBuilder != null && enumTypeDef.getQName().getLocalName() != null) {
            final EnumBuilder enumBuilder = typeBuilder.addEnumeration(BindingMapping.getClassName(enumName));
            typeProvider.addEnumDescription(enumBuilder, enumTypeDef);
            enumBuilder.updateEnumPairsFromEnumTypeDef(enumTypeDef);
            context.addInnerTypedefType(enumTypeDef.getPath(), enumBuilder);
            return enumBuilder;
        }
        return null;
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
    private GeneratedTypeBuilder moduleTypeBuilder(final ModuleContext context, final String postfix) {
        final Module module = context.module();
        final String moduleName = BindingMapping.getClassName(module.getName()) + postfix;
        final GeneratedTypeBuilder moduleBuilder = typeProvider.newGeneratedTypeBuilder(
            JavaTypeName.create(context.modulePackageName(), moduleName));

        moduleBuilder.setModuleName(moduleName);
        addCodegenInformation(moduleBuilder, module);
        return moduleBuilder;
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
    private void augmentationToGenTypes(final ModuleContext context, final AugmentationSchemaNode augSchema) {
        checkArgument(augSchema != null, "Augmentation Schema cannot be NULL.");
        checkState(augSchema.getTargetPath() != null,
                "Augmentation Schema does not contain Target Path (Target Path is NULL).");

        processUsesAugments(augSchema, context);
        final SchemaPath targetPath = augSchema.getTargetPath();
        SchemaNode targetSchemaNode = null;

        targetSchemaNode = findDataSchemaNode(schemaContext, targetPath);
        if (targetSchemaNode instanceof DataSchemaNode && ((DataSchemaNode) targetSchemaNode).isAddedByUses()) {
            if (targetSchemaNode instanceof DerivableSchemaNode) {
                targetSchemaNode = ((DerivableSchemaNode) targetSchemaNode).getOriginal().orElse(null);
            }
            if (targetSchemaNode == null) {
                throw new IllegalStateException("Failed to find target node from grouping in augmentation " + augSchema
                        + " in module " + context.module().getName());
            }
        }
        if (targetSchemaNode == null) {
            throw new IllegalArgumentException("augment target not found: " + targetPath);
        }

        GeneratedTypeBuilder targetTypeBuilder = findChildNodeByPath(targetSchemaNode.getPath());
        if (targetTypeBuilder == null) {
            targetTypeBuilder = findCaseByPath(targetSchemaNode.getPath());
        }
        if (targetTypeBuilder == null) {
            throw new NullPointerException("Target type not yet generated: " + targetSchemaNode);
        }

        if (!(targetSchemaNode instanceof ChoiceSchemaNode)) {
            final Type targetType = new ReferencedTypeImpl(targetTypeBuilder.getIdentifier());
            addRawAugmentGenTypeDefinition(context, targetType, augSchema);

        } else {
            generateTypesFromAugmentedChoiceCases(context, targetTypeBuilder.build(),
                    (ChoiceSchemaNode) targetSchemaNode, augSchema.getChildNodes(), null);
        }
    }

    private void usesAugmentationToGenTypes(final ModuleContext context, final AugmentationSchemaNode augSchema,
            final UsesNode usesNode, final DataNodeContainer usesNodeParent) {
        checkArgument(augSchema != null, "Augmentation Schema cannot be NULL.");
        checkState(augSchema.getTargetPath() != null,
                "Augmentation Schema does not contain Target Path (Target Path is NULL).");

        processUsesAugments(augSchema, context);
        final SchemaPath targetPath = augSchema.getTargetPath();
        final SchemaNode targetSchemaNode = findOriginalTargetFromGrouping(targetPath, usesNode);
        if (targetSchemaNode == null) {
            throw new IllegalArgumentException("augment target not found: " + targetPath);
        }

        GeneratedTypeBuilder targetTypeBuilder = findChildNodeByPath(targetSchemaNode.getPath());
        if (targetTypeBuilder == null) {
            targetTypeBuilder = findCaseByPath(targetSchemaNode.getPath());
        }
        if (targetTypeBuilder == null) {
            throw new NullPointerException("Target type not yet generated: " + targetSchemaNode);
        }

        if (!(targetSchemaNode instanceof ChoiceSchemaNode)) {
            if (usesNodeParent instanceof SchemaNode) {
                addRawAugmentGenTypeDefinition(context,
                    packageNameForAugmentedGeneratedType(context.modulePackageName(),
                        ((SchemaNode) usesNodeParent).getPath()),
                    targetTypeBuilder.build(), augSchema);
            } else {
                addRawAugmentGenTypeDefinition(context, targetTypeBuilder.build(), augSchema);
            }
        } else {
            generateTypesFromAugmentedChoiceCases(context, targetTypeBuilder.build(),
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

        SchemaNode result = targetGrouping;
        for (final QName node : targetPath.getPathFromRoot()) {
            if (result instanceof DataNodeContainer) {
                final QName resultNode = node.withModule(result.getQName().getModule());
                result = ((DataNodeContainer) result).getDataChildByName(resultNode);
            } else if (result instanceof ChoiceSchemaNode) {
                result = findNamedCase((ChoiceSchemaNode) result, node.getLocalName());
            }
        }
        if (result == null) {
            return null;
        }

        if (result instanceof DerivableSchemaNode) {
            DerivableSchemaNode castedResult = (DerivableSchemaNode) result;
            Optional<? extends SchemaNode> originalNode = castedResult.getOriginal();
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
                throw new IllegalStateException("Failed to generate code for augment in " + parentUsesNode);
            }

            return resultDataSchemaNode;
        }

        throw new IllegalStateException(
            "Target node of uses-augment statement must be DataSchemaNode. Failed to generate code for augment in "
                    + parentUsesNode);
    }

    /**
     * Returns a generated type builder for an augmentation.
     *
     * The name of the type builder is equal to the name of augmented node with
     * serial number as suffix.
     *
     * @param context
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
    private GeneratedTypeBuilder addRawAugmentGenTypeDefinition(final ModuleContext context,
            final String augmentPackageName, final Type targetTypeRef,
            final AugmentationSchemaNode augSchema) {
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

        final GeneratedTypeBuilder augTypeBuilder = typeProvider.newGeneratedTypeBuilder(
            JavaTypeName.create(augmentPackageName, augTypeName));

        augTypeBuilder.addImplementsType(DATA_OBJECT);
        augTypeBuilder.addImplementsType(Types.augmentationTypeFor(targetTypeRef));
        annotateDeprecatedIfNecessary(augSchema.getStatus(), augTypeBuilder);
        addImplementedInterfaceFromUses(augSchema, augTypeBuilder);

        augSchemaNodeToMethods(context,augTypeBuilder, augTypeBuilder, augSchema.getChildNodes());
        augmentBuilders.put(augTypeName, augTypeBuilder);

        if (!augSchema.getChildNodes().isEmpty()) {
            context.addTypeToAugmentation(augTypeBuilder, augSchema);

        }
        context.addAugmentType(augTypeBuilder);
        return augTypeBuilder;
    }

    private GeneratedTypeBuilder addRawAugmentGenTypeDefinition(final ModuleContext context, final Type targetTypeRef,
            final AugmentationSchemaNode augSchema) {
        return addRawAugmentGenTypeDefinition(context, context.modulePackageName(), targetTypeRef, augSchema);
    }

    /**
     *
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

    /**
     * Adds the methods to <code>typeBuilder</code> which represent subnodes of
     * node for which <code>typeBuilder</code> was created.
     *
     * The subnodes aren't mapped to the methods if they are part of grouping or
     * augment (in this case are already part of them).
     *
     * @param module
     *            current module
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
    private GeneratedTypeBuilder resolveDataSchemaNodes(final ModuleContext context, final GeneratedTypeBuilder parent,
            final GeneratedTypeBuilder childOf, final Iterable<DataSchemaNode> schemaNodes) {
        if (schemaNodes != null && parent != null) {
            for (final DataSchemaNode schemaNode : schemaNodes) {
                if (!schemaNode.isAugmenting() && !schemaNode.isAddedByUses()) {
                    addSchemaNodeToBuilderAsMethod(context, schemaNode, parent, childOf);
                }
            }
        }
        return parent;
    }

    /**
     * Adds the methods to <code>typeBuilder</code> what represents subnodes of
     * node for which <code>typeBuilder</code> was created.
     *
     * @param module
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
    private GeneratedTypeBuilder augSchemaNodeToMethods(final ModuleContext context,
            final GeneratedTypeBuilder typeBuilder, final GeneratedTypeBuilder childOf,
            final Iterable<DataSchemaNode> schemaNodes) {
        if (schemaNodes != null && typeBuilder != null) {
            for (final DataSchemaNode schemaNode : schemaNodes) {
                if (!schemaNode.isAugmenting()) {
                    addSchemaNodeToBuilderAsMethod(context, schemaNode, typeBuilder, childOf);
                }
            }
        }
        return typeBuilder;
    }

    /**
     * Adds to <code>typeBuilder</code> a method which is derived from
     * <code>schemaNode</code>.
     *
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
    private void addSchemaNodeToBuilderAsMethod(final ModuleContext context, final DataSchemaNode node,
            final GeneratedTypeBuilder typeBuilder, final GeneratedTypeBuilder childOf) {
        if (node != null && typeBuilder != null) {
            if (node instanceof LeafSchemaNode) {
                resolveLeafSchemaNodeAsMethod(typeBuilder, (LeafSchemaNode) node, context);
            } else if (node instanceof LeafListSchemaNode) {
                resolveLeafListSchemaNode(typeBuilder, (LeafListSchemaNode) node, context);
            } else if (node instanceof ContainerSchemaNode) {
                containerToGenType(context, typeBuilder, childOf, (ContainerSchemaNode) node);
            } else if (node instanceof ListSchemaNode) {
                listToGenType(context, typeBuilder, childOf, (ListSchemaNode) node);
            } else if (node instanceof ChoiceSchemaNode) {
                choiceToGeneratedType(context, typeBuilder, (ChoiceSchemaNode) node);
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
     * @param context
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
    private void choiceToGeneratedType(final ModuleContext context, final GeneratedTypeBuilder parent,
            final ChoiceSchemaNode choiceNode) {
        checkArgument(choiceNode != null, "Choice Schema Node cannot be NULL.");

        if (!choiceNode.isAddedByUses()) {
            final GeneratedTypeBuilder choiceTypeBuilder = addRawInterfaceDefinition(
                JavaTypeName.create(packageNameForGeneratedType(context.modulePackageName(), choiceNode.getPath()),
                BindingMapping.getClassName(choiceNode.getQName())), choiceNode);
            choiceTypeBuilder.addImplementsType(choiceIn(parent));
            annotateDeprecatedIfNecessary(choiceNode.getStatus(), choiceTypeBuilder);
            context.addChildNodeType(choiceNode, choiceTypeBuilder);

            final GeneratedType choiceType = choiceTypeBuilder.build();
            generateTypesFromChoiceCases(context, choiceType, choiceNode);

            constructGetter(parent, choiceType, choiceNode);
        }
    }

    /**
     * Converts <code>caseNodes</code> set to list of corresponding generated types.
     *
     * For every <i>case</i> which isn't added through augment or <i>uses</i> is created generated type builder.
     * The package names for the builder is created as concatenation of the module package and names of all parents
     * nodes of the concrete <i>case</i>. There is also relation "<i>implements type</i>" between every case builder
     * and <i>choice</i> type
     *
     * @param context
     *            current module context
     * @param refChoiceType
     *            type which represents superior <i>case</i>
     * @param choiceNode
     *            choice case node which is mapped to generated type
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>refChoiceType</code> equals null</li>
     *             <li>if <code>caseNodes</code> equals null</li>
     *             </ul>
     */
    private void generateTypesFromChoiceCases(final ModuleContext context, final Type refChoiceType,
            final ChoiceSchemaNode choiceNode) {
        checkArgument(refChoiceType != null, "Referenced Choice Type cannot be NULL.");
        checkArgument(choiceNode != null, "ChoiceNode cannot be NULL.");

        for (final CaseSchemaNode caseNode : choiceNode.getCases().values()) {
            if (caseNode != null && !caseNode.isAddedByUses() && !caseNode.isAugmenting()) {
                final GeneratedTypeBuilder caseTypeBuilder = addDefaultInterfaceDefinition(context, caseNode);
                caseTypeBuilder.addImplementsType(refChoiceType);
                annotateDeprecatedIfNecessary(caseNode.getStatus(), caseTypeBuilder);
                context.addCaseType(caseNode.getPath(), caseTypeBuilder);
                context.addChoiceToCaseMapping(refChoiceType, caseTypeBuilder, caseNode);
                final Iterable<DataSchemaNode> caseChildNodes = caseNode.getChildNodes();
                if (caseChildNodes != null) {
                    final SchemaPath choiceNodeParentPath = choiceNode.getPath().getParent();

                    if (!Iterables.isEmpty(choiceNodeParentPath.getPathFromRoot())) {
                        SchemaNode parent = findDataSchemaNode(schemaContext, choiceNodeParentPath);

                        if (parent instanceof AugmentationSchemaNode) {
                            final AugmentationSchemaNode augSchema = (AugmentationSchemaNode) parent;
                            final SchemaPath targetPath = augSchema.getTargetPath();
                            SchemaNode targetSchemaNode = findDataSchemaNode(schemaContext, targetPath);
                            if (targetSchemaNode instanceof DataSchemaNode
                                    && ((DataSchemaNode) targetSchemaNode).isAddedByUses()) {
                                if (targetSchemaNode instanceof DerivableSchemaNode) {
                                    targetSchemaNode = ((DerivableSchemaNode) targetSchemaNode).getOriginal()
                                            .orElse(null);
                                }
                                if (targetSchemaNode == null) {
                                    throw new IllegalStateException(
                                            "Failed to find target node from grouping for augmentation " + augSchema
                                                    + " in module " + context.module().getName());
                                }
                            }
                            parent = targetSchemaNode;
                        }

                        checkState(parent != null, "Could not find Choice node parent %s", choiceNodeParentPath);
                        GeneratedTypeBuilder childOfType = findChildNodeByPath(parent.getPath());
                        if (childOfType == null) {
                            childOfType = findGroupingByPath(parent.getPath());
                        }
                        resolveDataSchemaNodes(context, caseTypeBuilder, childOfType, caseChildNodes);
                    } else {
                        resolveDataSchemaNodes(context, caseTypeBuilder, moduleToDataType(context), caseChildNodes);
                    }
               }
            }
            processUsesAugments(caseNode, context);
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
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>basePackageName</code> is null</li>
     *             <li>if <code>targetType</code> is null</li>
     *             <li>if <code>augmentedNodes</code> is null</li>
     *             </ul>
     */
    private void generateTypesFromAugmentedChoiceCases(final ModuleContext context,
            final Type targetType, final ChoiceSchemaNode targetNode, final Iterable<DataSchemaNode> augmentedNodes,
            final DataNodeContainer usesNodeParent) {
        checkArgument(targetType != null, "Referenced Choice Type cannot be NULL.");
        checkArgument(augmentedNodes != null, "Set of Choice Case Nodes cannot be NULL.");

        for (final DataSchemaNode caseNode : augmentedNodes) {
            if (caseNode != null) {
                final GeneratedTypeBuilder caseTypeBuilder = addDefaultInterfaceDefinition(context, caseNode);
                caseTypeBuilder.addImplementsType(targetType);

                GeneratedTypeBuilder childOfType = findChildOfType(targetNode);
                CaseSchemaNode node = null;
                final String caseLocalName = caseNode.getQName().getLocalName();
                if (caseNode instanceof CaseSchemaNode) {
                    node = (CaseSchemaNode) caseNode;
                } else if (findNamedCase(targetNode, caseLocalName) == null) {
                    final String targetNodeLocalName = targetNode.getQName().getLocalName();
                    for (DataSchemaNode dataSchemaNode : usesNodeParent.getChildNodes()) {
                        if (dataSchemaNode instanceof ChoiceSchemaNode
                                && targetNodeLocalName.equals(dataSchemaNode.getQName().getLocalName())) {
                            node = findNamedCase((ChoiceSchemaNode) dataSchemaNode, caseLocalName);
                            break;
                        }
                    }
                } else {
                    node = findNamedCase(targetNode, caseLocalName);
                }
                final Iterable<DataSchemaNode> childNodes = node.getChildNodes();
                if (childNodes != null) {
                    resolveDataSchemaNodes(context, caseTypeBuilder, childOfType, childNodes);
                }
                context.addCaseType(caseNode.getPath(), caseTypeBuilder);
                context.addChoiceToCaseMapping(targetType, caseTypeBuilder, node);
            }
        }
    }

    private GeneratedTypeBuilder findChildOfType(final ChoiceSchemaNode targetNode) {
        final SchemaPath nodePath = targetNode.getPath();
        final SchemaPath parentSp = nodePath.getParent();
        if (parentSp.getParent() == null) {
            return moduleContext(nodePath.getLastComponent().getModule()).getModuleNode();
        }

        final SchemaNode parent = findDataSchemaNode(schemaContext, parentSp);
        GeneratedTypeBuilder childOfType = null;
        if (parent instanceof CaseSchemaNode) {
            childOfType = findCaseByPath(parent.getPath());
        } else if (parent instanceof DataSchemaNode || parent instanceof NotificationDefinition) {
            childOfType = findChildNodeByPath(parent.getPath());
        } else if (parent instanceof GroupingDefinition) {
            childOfType = findGroupingByPath(parent.getPath());
        }

        if (childOfType == null) {
            throw new IllegalArgumentException("Failed to find parent type of choice " + targetNode);
        }

        return childOfType;
    }

    private static CaseSchemaNode findNamedCase(final ChoiceSchemaNode choice, final String caseName) {
        final List<CaseSchemaNode> cases = choice.findCaseNodes(caseName);
        return cases.isEmpty() ? null : cases.get(0);
    }

    private static boolean isInnerType(final LeafSchemaNode leaf, final TypeDefinition<?> type) {
        // New parser with encapsulated type
        if (leaf.getPath().equals(type.getPath())) {
            return true;
        }

        // Embedded type definition with new parser. Also takes care of the old parser with bits
        if (leaf.getPath().equals(type.getPath().getParent())) {
            return true;
        }

        return false;
    }

    private void addPatternConstant(final GeneratedTypeBuilder typeBuilder, final String leafName,
            final List<PatternConstraint> patternConstraints) {
        if (!patternConstraints.isEmpty()) {
            final StringBuilder field = new StringBuilder().append(TypeConstants.PATTERN_CONSTANT_NAME).append("_")
                .append(BindingMapping.getPropertyName(leafName));
            typeBuilder.addConstant(Types.listTypeFor(BaseYangTypes.STRING_TYPE), field.toString(),
                typeProvider.resolveRegExpressions(patternConstraints));
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
    private Type resolveLeafSchemaNodeAsMethod(final GeneratedTypeBuilder typeBuilder, final LeafSchemaNode leaf,
            final ModuleContext context) {
        if (leaf == null || typeBuilder == null || leaf.isAddedByUses()) {
            return null;
        }

        final Module parentModule = findParentModule(schemaContext, leaf);
        Type returnType = null;

        final TypeDefinition<?> typeDef = CompatUtils.compatLeafType(leaf);
        if (isInnerType(leaf, typeDef)) {
            if (typeDef instanceof EnumTypeDefinition) {
                returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, leaf);
                final EnumTypeDefinition enumTypeDef = (EnumTypeDefinition) typeDef;
                final EnumBuilder enumBuilder = resolveInnerEnumFromTypeDefinition(enumTypeDef, leaf.getQName(),
                    typeBuilder, context);
                if (enumBuilder != null) {
                    returnType = enumBuilder.toInstance(typeBuilder);
                }
                typeProvider.putReferencedType(leaf.getPath(), returnType);
            } else if (typeDef instanceof UnionTypeDefinition) {
                GeneratedTOBuilder genTOBuilder = addTOToTypeBuilder((UnionTypeDefinition) typeDef, typeBuilder, leaf,
                    parentModule);
                if (genTOBuilder != null) {
                    returnType = createReturnTypeForUnion(genTOBuilder, typeDef, typeBuilder, parentModule);
                    // Store the inner type within the union so that we can find the reference for it
                    context.addInnerTypedefType(typeDef.getPath(), returnType);
                }
            } else if (typeDef instanceof BitsTypeDefinition) {
                GeneratedTOBuilder genTOBuilder = addTOToTypeBuilder((BitsTypeDefinition) typeDef, typeBuilder, leaf,
                    parentModule);
                if (genTOBuilder != null) {
                    returnType = genTOBuilder.build();
                }
            } else {
                // It is constrained version of already declared type (inner declared type exists,
                // onlyfor special cases (Enum, Union, Bits), which were already checked.
                // In order to get proper class we need to look up closest derived type
                // and apply restrictions from leaf type
                final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(typeDef);
                returnType = typeProvider.javaTypeForSchemaDefinitionType(getBaseOrDeclaredType(typeDef), leaf,
                        restrictions);
                addPatternConstant(typeBuilder, leaf.getQName().getLocalName(), restrictions.getPatternConstraints());
            }
        } else {
            final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(typeDef);
            returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, leaf, restrictions);
            addPatternConstant(typeBuilder, leaf.getQName().getLocalName(), restrictions.getPatternConstraints());
        }

        if (returnType == null) {
            return null;
        }

        if (typeDef instanceof EnumTypeDefinition) {
            typeProvider.putReferencedType(leaf.getPath(), returnType);
        }

        final MethodSignatureBuilder getter = constructGetter(typeBuilder,  returnType, leaf);
        processContextRefExtension(leaf, getter, parentModule);
        return returnType;
    }

    private static TypeDefinition<?> getBaseOrDeclaredType(final TypeDefinition<?> typeDef) {
        // Returns DerivedType in case of new parser.
        final TypeDefinition<?> baseType = typeDef.getBaseType();
        return baseType != null && baseType.getBaseType() != null ? baseType : typeDef;
    }

    private void processContextRefExtension(final LeafSchemaNode leaf, final MethodSignatureBuilder getter,
            final Module module) {
        for (final UnknownSchemaNode node : leaf.getUnknownSchemaNodes()) {
            final QName nodeType = node.getNodeType();
            if ("context-reference".equals(nodeType.getLocalName())) {
                final String nodeParam = node.getNodeParameter();
                IdentitySchemaNode identity = null;
                String basePackageName = null;
                final Iterable<String> splittedElement = COLON_SPLITTER.split(nodeParam);
                final Iterator<String> iterator = splittedElement.iterator();
                final int length = Iterables.size(splittedElement);
                if (length == 1) {
                    identity = findIdentityByName(module.getIdentities(), iterator.next());
                    basePackageName = BindingMapping.getRootPackageName(module.getQNameModule());
                } else if (length == 2) {
                    final String prefix = iterator.next();
                    final Module dependentModule = findModuleFromImports(module.getImports(), prefix);
                    if (dependentModule == null) {
                        throw new IllegalArgumentException("Failed to process context-reference: unknown prefix "
                                + prefix);
                    }
                    identity = findIdentityByName(dependentModule.getIdentities(), iterator.next());
                    basePackageName = BindingMapping.getRootPackageName(dependentModule.getQNameModule());
                } else {
                    throw new IllegalArgumentException("Failed to process context-reference: unknown identity "
                            + nodeParam);
                }
                if (identity == null) {
                    throw new IllegalArgumentException("Failed to process context-reference: unknown identity "
                            + nodeParam);
                }

                final AnnotationTypeBuilder rc = getter.addAnnotation(ROUTING_CONTEXT);
                final String packageName = packageNameForGeneratedType(basePackageName, identity.getPath());
                final String genTypeName = BindingMapping.getClassName(identity.getQName().getLocalName());
                rc.addParameter("value", packageName + "." + genTypeName + ".class");
            }
        }
    }

    private static IdentitySchemaNode findIdentityByName(final Set<IdentitySchemaNode> identities, final String name) {
        for (final IdentitySchemaNode id : identities) {
            if (id.getQName().getLocalName().equals(name)) {
                return id;
            }
        }
        return null;
    }

    private Module findModuleFromImports(final Set<ModuleImport> imports, final String prefix) {
        for (final ModuleImport imp : imports) {
            if (imp.getPrefix().equals(prefix)) {
                return schemaContext.findModule(imp.getModuleName(), imp.getRevision()).orElse(null);
            }
        }
        return null;
    }

    private boolean resolveLeafSchemaNodeAsProperty(final GeneratedTOBuilder toBuilder, final LeafSchemaNode leaf,
            final boolean isReadOnly) {
        if (leaf != null && toBuilder != null) {
            Type returnType;
            final TypeDefinition<?> typeDef = CompatUtils.compatLeafType(leaf);
            if (typeDef instanceof UnionTypeDefinition) {
                // GeneratedType for this type definition should have be already created
                final ModuleContext mc = moduleContext(typeDef.getQName().getModule());
                returnType = mc.getTypedefs().get(typeDef.getPath());
                if (returnType == null) {
                    // This may still be an inner type, try to find it
                    returnType = mc.getInnerType(typeDef.getPath());
                }
            } else if (typeDef instanceof EnumTypeDefinition && typeDef.getBaseType() == null) {
                // Annonymous enumeration (already generated, since it is inherited via uses).
                LeafSchemaNode originalLeaf = (LeafSchemaNode) SchemaNodeUtils.getRootOriginalIfPossible(leaf);
                QName qname = originalLeaf.getQName();
                returnType = moduleContext(qname.getModule()).getInnerType(originalLeaf.getType().getPath());
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
    private boolean resolveLeafSchemaNodeAsProperty(final GeneratedTOBuilder toBuilder, final LeafSchemaNode leaf,
            final Type returnType, final boolean isReadOnly) {
        if (returnType == null) {
            return false;
        }
        final String leafName = leaf.getQName().getLocalName();
        final GeneratedPropertyBuilder propBuilder = toBuilder.addProperty(BindingMapping.getPropertyName(leafName));
        propBuilder.setReadOnly(isReadOnly);
        propBuilder.setReturnType(returnType);
        addComment(propBuilder, leaf);

        toBuilder.addEqualsIdentity(propBuilder);
        toBuilder.addHashIdentity(propBuilder);
        toBuilder.addToStringProperty(propBuilder);
        return true;
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
     * @return boolean value
     *         <ul>
     *         <li>true - if <code>node</code>, <code>typeBuilder</code>,
     *         nodeName equal null or <code>node</code> is added by <i>uses</i></li>
     *         <li>false - other cases</li>
     *         </ul>
     */
    private boolean resolveLeafListSchemaNode(final GeneratedTypeBuilder typeBuilder, final LeafListSchemaNode node,
            final ModuleContext context) {
        if (node == null || typeBuilder == null || node.isAddedByUses()) {
            return false;
        }

        final QName nodeName = node.getQName();

        final TypeDefinition<?> typeDef = node.getType();
        final Module parentModule = findParentModule(schemaContext, node);

        Type returnType = null;
        if (typeDef.getBaseType() == null) {
            if (typeDef instanceof EnumTypeDefinition) {
                returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, node);
                final EnumTypeDefinition enumTypeDef = (EnumTypeDefinition) typeDef;
                final EnumBuilder enumBuilder = resolveInnerEnumFromTypeDefinition(enumTypeDef, nodeName,
                    typeBuilder, context);
                returnType = new ReferencedTypeImpl(enumBuilder.getIdentifier());
                typeProvider.putReferencedType(node.getPath(), returnType);
            } else if (typeDef instanceof UnionTypeDefinition) {
                final GeneratedTOBuilder genTOBuilder = addTOToTypeBuilder((UnionTypeDefinition)typeDef, typeBuilder,
                    node, parentModule);
                if (genTOBuilder != null) {
                    returnType = createReturnTypeForUnion(genTOBuilder, typeDef, typeBuilder, parentModule);
                }
            } else if (typeDef instanceof BitsTypeDefinition) {
                final GeneratedTOBuilder genTOBuilder = addTOToTypeBuilder((BitsTypeDefinition)typeDef, typeBuilder,
                    node, parentModule);
                returnType = genTOBuilder.build();
            } else {
                final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(typeDef);
                returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, node, restrictions);
                addPatternConstant(typeBuilder, node.getQName().getLocalName(), restrictions.getPatternConstraints());
            }
        } else {
            final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(typeDef);
            returnType = typeProvider.javaTypeForSchemaDefinitionType(typeDef, node, restrictions);
            addPatternConstant(typeBuilder, node.getQName().getLocalName(), restrictions.getPatternConstraints());
        }

        final ParameterizedType listType = Types.listTypeFor(returnType);
        constructGetter(typeBuilder, listType, node);
        return true;
    }

    private Type createReturnTypeForUnion(final GeneratedTOBuilder genTOBuilder, final TypeDefinition<?> typeDef,
            final GeneratedTypeBuilder typeBuilder, final Module parentModule) {
        final GeneratedTOBuilder returnType = typeProvider.newGeneratedTOBuilder(genTOBuilder.getIdentifier());

        addCodegenInformation(returnType, parentModule, typeDef);
        returnType.setSchemaPath(typeDef.getPath());
        returnType.setModuleName(parentModule.getName());

        genTOBuilder.setTypedef(true);
        genTOBuilder.setIsUnion(true);
        AbstractTypeProvider.addUnitsToGenTO(genTOBuilder, typeDef.getUnits().orElse(null));



        final GeneratedTOBuilder unionBuilder = createUnionBuilder(genTOBuilder, typeBuilder);


        final MethodSignatureBuilder method = unionBuilder.addMethod("getDefaultInstance");
        method.setReturnType(returnType);
        method.addParameter(Types.STRING, "defaultValue");
        method.setAccessModifier(AccessModifier.PUBLIC);
        method.setStatic(true);

        final Set<Type> types = typeProvider.getAdditionalTypes().get(parentModule);
        if (types == null) {
            typeProvider.getAdditionalTypes().put(parentModule,
                    Sets.newHashSet(unionBuilder.build()));
        } else {
            types.add(unionBuilder.build());
        }
        return returnType.build();
    }

    private GeneratedTOBuilder createUnionBuilder(final GeneratedTOBuilder genTOBuilder,
            final GeneratedTypeBuilder typeBuilder) {
        // Append enclosing path hierarchy without dots
        final StringBuilder sb = new StringBuilder();
        genTOBuilder.getIdentifier().localNameComponents().forEach(sb::append);
        final GeneratedTOBuilder unionBuilder = typeProvider.newGeneratedTOBuilder(
            JavaTypeName.create(typeBuilder.getPackageName(), sb.append("Builder").toString()));
        unionBuilder.setIsUnionBuilder(true);
        return unionBuilder;
    }

    private GeneratedTypeBuilder addDefaultInterfaceDefinition(final ModuleContext context,
            final SchemaNode schemaNode) {
        return addDefaultInterfaceDefinition(context, schemaNode, null);
    }

    private GeneratedTypeBuilder addDefaultInterfaceDefinition(final ModuleContext context,
            final SchemaNode schemaNode, final GeneratedTypeBuilder childOf) {
        final String packageName = packageNameForGeneratedType(context.modulePackageName(), schemaNode.getPath());
        return addDefaultInterfaceDefinition(packageName, schemaNode, childOf, context);
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
            final Type parent, final ModuleContext context) {
        final GeneratedTypeBuilder it = addRawInterfaceDefinition(
            JavaTypeName.create(packageName, BindingMapping.getClassName(schemaNode.getQName())), schemaNode);

        it.addImplementsType(parent == null ? DATA_OBJECT : childOf(parent));
        if (!(schemaNode instanceof GroupingDefinition)) {
            it.addImplementsType(augmentable(it));
        }
        if (schemaNode instanceof DataNodeContainer) {
            final DataNodeContainer containerSchema = (DataNodeContainer) schemaNode;
            groupingsToGenTypes(context, containerSchema.getGroupings());
            addImplementedInterfaceFromUses(containerSchema, it);
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
    private GeneratedTypeBuilder addRawInterfaceDefinition(final ModuleContext context, final SchemaNode schemaNode,
            final String prefix) {
        return addRawInterfaceDefinition(
            JavaTypeName.create(packageNameForGeneratedType(context.modulePackageName(), schemaNode.getPath()),
                prefix + BindingMapping.getClassName(schemaNode.getQName())), schemaNode);
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
    private GeneratedTypeBuilder addRawInterfaceDefinition(final JavaTypeName identifier, final SchemaNode schemaNode) {
        checkArgument(schemaNode != null, "Data Schema Node cannot be NULL.");
        checkArgument(schemaNode.getQName() != null, "QName for Data Schema Node cannot be NULL.");
        final String schemaNodeName = schemaNode.getQName().getLocalName();
        checkArgument(schemaNodeName != null, "Local Name of QName for Data Schema Node cannot be NULL.");

        // FIXME: Validation of name conflict
        final GeneratedTypeBuilder newType = typeProvider.newGeneratedTypeBuilder(identifier);
        final Module module = findParentModule(schemaContext, schemaNode);
        qnameConstant(newType, JavaTypeName.create(BindingMapping.getRootPackageName(module.getQNameModule()),
            BindingMapping.MODULE_INFO_CLASS_NAME), schemaNode.getQName().getLocalName());

        addCodegenInformation(newType, module, schemaNode);
        newType.setSchemaPath(schemaNode.getPath());
        newType.setModuleName(module.getName());

        final String packageName = identifier.packageName();
        final String simpleName = identifier.simpleName();
        if (!genTypeBuilders.containsKey(packageName)) {
            final Map<String, GeneratedTypeBuilder> builders = new HashMap<>();
            builders.put(simpleName, newType);
            genTypeBuilders.put(packageName, builders);
        } else {
            final Map<String, GeneratedTypeBuilder> builders = genTypeBuilders.get(packageName);
            if (!builders.containsKey(simpleName)) {
                builders.put(simpleName, newType);
            }
        }
        return newType;
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
    public static String getterMethodName(final String localName, final Type returnType) {
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
     * Created a method signature builder as part of <code>interfaceBuilder</code>.
     *
     * The method signature builder is created for the getter method of <code>schemaNodeName</code>.
     * Also <code>comment</code> and <code>returnType</code> information are added to the builder.
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
            getterMethodName(node.getQName().getLocalName(), returnType));
        getMethod.setReturnType(returnType);

        annotateDeprecatedIfNecessary(node.getStatus(), getMethod);
        if (!returnType.getPackageName().isEmpty()) {
            // The return type has a package, so it's not a primitive type
            getMethod.addAnnotation("javax.annotation", "Nullable");
        }
        addComment(getMethod, node);

        return getMethod;
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
    private void addSchemaNodeToListBuilders(final ModuleContext context, final DataSchemaNode schemaNode,
            final GeneratedTypeBuilder typeBuilder, final GeneratedTOBuilder genTOBuilder,
            final List<String> listKeys) {
        checkArgument(schemaNode != null, "Data Schema Node cannot be NULL.");
        checkArgument(typeBuilder != null, "Generated Type Builder cannot be NULL.");

        if (schemaNode instanceof LeafSchemaNode) {
            final LeafSchemaNode leaf = (LeafSchemaNode) schemaNode;
            final String leafName = leaf.getQName().getLocalName();
            Type type = resolveLeafSchemaNodeAsMethod(typeBuilder, leaf, context);
            if (listKeys.contains(leafName)) {
                if (type == null) {
                    resolveLeafSchemaNodeAsProperty(genTOBuilder, leaf, true);
                } else {
                    resolveLeafSchemaNodeAsProperty(genTOBuilder, leaf, type, true);
                }
            }
        } else if (!schemaNode.isAddedByUses()) {
            if (schemaNode instanceof LeafListSchemaNode) {
                resolveLeafListSchemaNode(typeBuilder, (LeafListSchemaNode) schemaNode, context);
            } else if (schemaNode instanceof ContainerSchemaNode) {
                containerToGenType(context, typeBuilder, typeBuilder, (ContainerSchemaNode) schemaNode);
            } else if (schemaNode instanceof ChoiceSchemaNode) {
                choiceToGeneratedType(context, typeBuilder, (ChoiceSchemaNode) schemaNode);
            } else if (schemaNode instanceof ListSchemaNode) {
                listToGenType(context, typeBuilder, typeBuilder, (ListSchemaNode) schemaNode);
            }
        }
    }

    private static void typeBuildersToGenTypes(final ModuleContext context, final GeneratedTypeBuilder typeBuilder,
            final GeneratedTOBuilder genTOBuilder) {
        checkArgument(typeBuilder != null, "Generated Type Builder cannot be NULL.");

        if (genTOBuilder != null) {
            final GeneratedTransferObject genTO = genTOBuilder.build();
            // Add Identifiable.getKey() for items
            typeBuilder.addMethod(BindingMapping.IDENTIFIABLE_KEY_NAME).setReturnType(genTO)
                .addAnnotation(OVERRIDE_ANNOTATION);
            context.addGeneratedTOBuilder(genTOBuilder);
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
    private GeneratedTOBuilder resolveListKeyTOBuilder(final ModuleContext context, final ListSchemaNode list) {
        if (list.getKeyDefinition() != null && !list.getKeyDefinition().isEmpty()) {
            return typeProvider.newGeneratedTOBuilder(JavaTypeName.create(
                packageNameForGeneratedType(context.modulePackageName(), list.getPath()),
                BindingMapping.getClassName(list.getQName().getLocalName() + "Key")));
        }
        return null;
    }

    /**
     * Builds a GeneratedTOBuilder for a UnionType {@link UnionTypeDefinition}.
     *
     * If more then one generated TO builder is created for enclosing then all
     * of the generated TO builders are added to <code>typeBuilder</code> as
     * enclosing transfer objects.
     *
     * @param typeDef
     *            type definition which can be of type <code>UnionType</code> or
     *            <code>BitsTypeDefinition</code>
     * @param typeBuilder
     *            generated type builder to which is added generated TO created
     *            from <code>typeDef</code>
     * @param leaf
     *            string with name for generated TO builder
     * @param parentModule
     *            parent module
     * @return generated TO builder for <code>typeDef</code>
     */
    private GeneratedTOBuilder addTOToTypeBuilder(final UnionTypeDefinition typeDef,
            final GeneratedTypeBuilder typeBuilder, final DataSchemaNode leaf, final Module parentModule) {
        final List<GeneratedTOBuilder> types = typeProvider.provideGeneratedTOBuildersForUnionTypeDef(
            typeBuilder.getIdentifier().createEnclosed(BindingMapping.getClassName(leaf.getQName())),
            typeDef, leaf);

        checkState(!types.isEmpty(), "No GeneratedTOBuilder objects generated from union %s", typeDef);
        final List<GeneratedTOBuilder> genTOBuilders = new ArrayList<>(types);
        final GeneratedTOBuilder resultTOBuilder = types.remove(0);
        for (final GeneratedTOBuilder genTOBuilder : types) {
            resultTOBuilder.addEnclosingTransferObject(genTOBuilder);
        }

        final GeneratedPropertyBuilder genPropBuilder = resultTOBuilder.addProperty("value");
        genPropBuilder.setReturnType(Types.CHAR_ARRAY);
        resultTOBuilder.addEqualsIdentity(genPropBuilder);
        resultTOBuilder.addHashIdentity(genPropBuilder);
        resultTOBuilder.addToStringProperty(genPropBuilder);
        processEnclosedTOBuilderes(typeBuilder, genTOBuilders);
        return resultTOBuilder;
    }

    /**
     * Builds generated TO builders for <code>typeDef</code> of type {@link BitsTypeDefinition} which are
     * also added to <code>typeBuilder</code> as enclosing transfer object.
     *
     * If more then one generated TO builder is created for enclosing then all
     * of the generated TO builders are added to <code>typeBuilder</code> as
     * enclosing transfer objects.
     *
     * @param typeDef
     *            type definition which can be of type <code>UnionType</code> or
     *            <code>BitsTypeDefinition</code>
     * @param typeBuilder
     *            generated type builder to which is added generated TO created
     *            from <code>typeDef</code>
     * @param leaf
     *            string with name for generated TO builder
     * @param parentModule
     *            parent module
     * @return generated TO builder for <code>typeDef</code>
     */
    private GeneratedTOBuilder addTOToTypeBuilder(final BitsTypeDefinition typeDef,
            final GeneratedTypeBuilder typeBuilder, final DataSchemaNode leaf, final Module parentModule) {
        final GeneratedTOBuilder genTOBuilder = typeProvider.provideGeneratedTOBuilderForBitsTypeDefinition(
            typeBuilder.getIdentifier().createEnclosed(BindingMapping.getClassName(leaf.getQName())),
            typeDef, parentModule.getName());
        typeBuilder.addEnclosingTransferObject(genTOBuilder);
        return genTOBuilder;

    }

    private static GeneratedTOBuilder processEnclosedTOBuilderes(final GeneratedTypeBuilder typeBuilder,
            final List<GeneratedTOBuilder> genTOBuilders) {
        for (final GeneratedTOBuilder genTOBuilder : genTOBuilders) {
            typeBuilder.addEnclosingTransferObject(genTOBuilder);
        }
        return genTOBuilders.get(0);
    }

    /**
     * Adds the implemented types to type builder.
     *
     * The method passes through the list of <i>uses</i> in
     * {@code dataNodeContainer}. For every <i>use</i> is obtained corresponding
     * generated type from {@link ModuleContext#groupings
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
            final GeneratedType genType = findGroupingByPath(usesNode.getGroupingPath()).build();
            if (genType == null) {
                throw new IllegalStateException("Grouping " + usesNode.getGroupingPath() + "is not resolved for "
                        + builder.getName());
            }

            builder.addImplementsType(genType);
        }
        return builder;
    }

    private GeneratedTypeBuilder findChildNodeByPath(final SchemaPath path) {
        for (final ModuleContext ctx : genCtx.values()) {
            final GeneratedTypeBuilder result = ctx.getChildNode(path);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private GeneratedTypeBuilder findGroupingByPath(final SchemaPath path) {
        for (final ModuleContext ctx : genCtx.values()) {
            final GeneratedTypeBuilder result = ctx.getGrouping(path);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private GeneratedTypeBuilder findCaseByPath(final SchemaPath path) {
        for (final ModuleContext ctx : genCtx.values()) {
            final GeneratedTypeBuilder result = ctx.getCase(path);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private static void annotateDeprecatedIfNecessary(final Status status, final AnnotableTypeBuilder builder) {
        if (status == Status.DEPRECATED) {
            builder.addAnnotation(DEPRECATED_ANNOTATION);
        }
    }
}
