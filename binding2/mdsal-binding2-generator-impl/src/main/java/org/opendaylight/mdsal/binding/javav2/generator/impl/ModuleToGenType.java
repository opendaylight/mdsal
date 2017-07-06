/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.createDescription;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.GenHelperUtil.groupingsToGenTypes;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.GenHelperUtil.moduleTypeBuilder;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.GenHelperUtil.processUsesImplements;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.GenHelperUtil.resolveNotification;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingTypes.NOTIFICATION_LISTENER;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.mdsal.binding.javav2.generator.spi.TypeProvider;
import org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder.GeneratedTOBuilderImpl;
import org.opendaylight.mdsal.binding.javav2.generator.yang.types.TypeProviderImpl;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingNamespaceType;
import org.opendaylight.mdsal.binding.javav2.util.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationNodeContainer;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.util.DataNodeIterator;

@Beta
final class ModuleToGenType {

    private ModuleToGenType() {
        throw new UnsupportedOperationException("Utility class");
    }

    static Map<Module, ModuleContext> generate(final Module module, final Map<String, Map<String, GeneratedTypeBuilder>>
            genTypeBuilders, final SchemaContext schemaContext, final TypeProvider typeProvider, Map<Module,
            ModuleContext> genCtx, final boolean verboseClassComments) {

        genCtx.put(module, new ModuleContext());
        genCtx = allTypeDefinitionsToGenTypes(module, genCtx, typeProvider);
        genCtx = groupingsToGenTypes(module, module.getGroupings(), genCtx, schemaContext, verboseClassComments,
                genTypeBuilders, typeProvider);
        genCtx = allIdentitiesToGenTypes(module, schemaContext, genCtx, verboseClassComments,  genTypeBuilders, typeProvider);
        genCtx = notificationsToGenType(module, genCtx, schemaContext, genTypeBuilders, verboseClassComments, typeProvider);

        if (!module.getChildNodes().isEmpty()) {
            final GeneratedTypeBuilder moduleType = GenHelperUtil.moduleToDataType(module, genCtx, verboseClassComments);
            genCtx.get(module).addModuleNode(moduleType);
            final String basePackageName = BindingMapping.getRootPackageName(module);
            GenHelperUtil.resolveDataSchemaNodes(module, basePackageName, moduleType, moduleType, module
                    .getChildNodes(), genCtx, schemaContext, verboseClassComments, genTypeBuilders, typeProvider,
                    BindingNamespaceType.Data);
        }

        //after potential parent data schema nodes
        genCtx = actionsAndRPCMethodsToGenType(module, genCtx, schemaContext, verboseClassComments,
                genTypeBuilders, typeProvider);

        return genCtx;
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
    private static Map<Module, ModuleContext> allTypeDefinitionsToGenTypes(final Module module, final Map<Module, ModuleContext> genCtx,
            final TypeProvider typeProvider) {
        Preconditions.checkArgument(module != null, "Module reference cannot be NULL.");
        Preconditions.checkArgument(module.getName() != null, "Module name cannot be NULL.");
        final DataNodeIterator it = new DataNodeIterator(module);
        final List<TypeDefinition<?>> typeDefinitions = it.allTypedefs();
        Preconditions.checkState(typeDefinitions != null, "Type Definitions for module «module.name» cannot be NULL.");

        typeDefinitions.stream().filter(typedef -> typedef != null).forEach(typedef -> {
            final Type type = ((TypeProviderImpl) typeProvider).generatedTypeForExtendedDefinitionType(typedef,
                    typedef);
            if (type != null) {
                final ModuleContext ctx = genCtx.get(module);
                ctx.addTypedefType(typedef.getPath(), type);
                ctx.addTypeToSchema(type, typedef);
            }
        });
        return genCtx;
    }

    private static Map<Module, ModuleContext> actionsAndRPCMethodsToGenType(final Module module, Map<Module,
            ModuleContext> genCtx, final SchemaContext schemaContext, final boolean verboseClassComments,
            final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders, final TypeProvider typeProvider) {

        genCtx = RpcActionGenHelper.rpcMethodsToGenType(module, genCtx, schemaContext, verboseClassComments,
                genTypeBuilders, typeProvider);
        genCtx = RpcActionGenHelper.actionMethodsToGenType(module, genCtx, schemaContext, verboseClassComments,
                genTypeBuilders, typeProvider);

        return genCtx;
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
     *            {@link GenHelperUtil#identityToGenType(Module, String, IdentitySchemaNode, SchemaContext, Map, boolean, Map, TypeProvider, Map)}
     * @param genCtx generated context
     * @return returns generated context
     *
     */
    private static Map<Module, ModuleContext> allIdentitiesToGenTypes(final Module module,
            final SchemaContext schemaContext, Map<Module, ModuleContext> genCtx, boolean verboseClassComments,
            final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders, final TypeProvider typeProvider) {

        final Set<IdentitySchemaNode> schemaIdentities = module.getIdentities();
        final String basePackageName = BindingMapping.getRootPackageName(module);

        if (schemaIdentities != null && !schemaIdentities.isEmpty()) {
            for (final IdentitySchemaNode identity : schemaIdentities) {
                GenHelperUtil.identityToGenType(module, basePackageName, identity, schemaContext, genCtx,
                    verboseClassComments);
            }
        }

        return genCtx;
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
    private static Map<Module, ModuleContext> notificationsToGenType(final Module module, final Map<Module, ModuleContext> genCtx,
            final SchemaContext schemaContext, final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders,
            final boolean verboseClassComments, final TypeProvider typeProvider) {
        checkArgument(module != null, "Module reference cannot be NULL.");
        checkArgument(module.getName() != null, "Module name cannot be NULL.");
        final Set<NotificationDefinition> notifications = module.getNotifications();
        if (notifications.isEmpty()) {
            return genCtx;
        }

        final GeneratedTypeBuilder listenerInterface = moduleTypeBuilder(module, "Listener", verboseClassComments);
        listenerInterface.addImplementsType(NOTIFICATION_LISTENER);
        final String basePackageName = BindingMapping.getRootPackageName(module);

        for (final NotificationDefinition notification : notifications) {
            if (notification != null) {
                resolveNotification(listenerInterface, null, basePackageName, notification, module, schemaContext,
                        verboseClassComments, genTypeBuilders, typeProvider, genCtx);
                processUsesImplements(notification, module, schemaContext, genCtx, BindingNamespaceType.Data);
            }
        }

        //YANG 1.1 allows notifications be tied to containers and lists
        final Collection<DataSchemaNode> potentials = module.getChildNodes();
        Set<NotificationDefinition> tiedNotifications = null;

        for (final DataSchemaNode potential : potentials) {
            if (potential instanceof NotificationNodeContainer) {
                tiedNotifications = ((NotificationNodeContainer) potential)
                        .getNotifications();
                for (final NotificationDefinition tiedNotification: tiedNotifications) {
                    if (tiedNotification != null) {
                        resolveNotification(listenerInterface, potential.getQName().getLocalName(), basePackageName,
                                tiedNotification, module, schemaContext, verboseClassComments, genTypeBuilders,
                                typeProvider, genCtx);
                        processUsesImplements(tiedNotification, module, schemaContext, genCtx, BindingNamespaceType.Data);
                    }
                }
            }
        }

        if (tiedNotifications != null) {
            listenerInterface.setDescription(createDescription(ImmutableSet.<NotificationDefinition>builder()
                .addAll(notifications).addAll(tiedNotifications).build(), module, verboseClassComments));
        } else {
            listenerInterface.setDescription(createDescription(notifications, module, verboseClassComments));
        }

        genCtx.get(module).addTopLevelNodeType(listenerInterface);

        return genCtx;
    }
}
