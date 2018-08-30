/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.GenHelperUtil.groupingsToGenTypes;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.GenHelperUtil.moduleTypeBuilder;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.GenHelperUtil.resolveNotification;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingTypes.NOTIFICATION_LISTENER;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.opendaylight.mdsal.binding.javav2.generator.context.ModuleContext;
import org.opendaylight.mdsal.binding.javav2.generator.spi.TypeProvider;
import org.opendaylight.mdsal.binding.javav2.generator.util.TypeComments;
import org.opendaylight.mdsal.binding.javav2.generator.yang.types.TypeProviderImpl;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.YangSourceDefinition;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingNamespaceType;
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

    static void generate(final Module module, final Map<String, Map<String, GeneratedTypeBuilder>>
            genTypeBuilders, final SchemaContext schemaContext, final TypeProvider typeProvider, final Map<Module,
            ModuleContext> genCtx, final boolean verboseClassComments) {
        final ModuleContext moduleContext = genCtx.computeIfAbsent(module, ModuleContextImpl::new);

        allTypeDefinitionsToGenTypes(moduleContext, typeProvider);
        groupingsToGenTypes(moduleContext, module.getGroupings(), genCtx, schemaContext, verboseClassComments,
            genTypeBuilders, typeProvider);
        allIdentitiesToGenTypes(moduleContext, schemaContext, genCtx, verboseClassComments, genTypeBuilders,
            typeProvider);

        if (!module.getChildNodes().isEmpty()) {
            final GeneratedTypeBuilder moduleType = GenHelperUtil.moduleToDataType(moduleContext, genCtx,
                verboseClassComments);
            moduleContext.addModuleNode(moduleType);
            GenHelperUtil.resolveDataSchemaNodes(moduleContext, moduleType, moduleType, module
                    .getChildNodes(), genCtx, schemaContext, verboseClassComments, genTypeBuilders, typeProvider,
                    BindingNamespaceType.Data);
        }

        notificationsToGenType(moduleContext, genCtx, schemaContext, genTypeBuilders, verboseClassComments,
            typeProvider);

        //after potential parent data schema nodes
        actionsAndRPCMethodsToGenType(moduleContext, genCtx, schemaContext, verboseClassComments,
                genTypeBuilders, typeProvider);
    }

    /**
     * Converts all extended type definitions of module to the list of
     * <code>Type</code> objects.
     *
     * @param moduleContext
     *            Holds information about generated entities in context of YANG module.
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if module is null</li>
     *             <li>if name of module is null</li>
     *             </ul>
     * @throws IllegalStateException
     *             if set of type definitions from module is null
     */
    private static void allTypeDefinitionsToGenTypes(final ModuleContext moduleContext,
            final TypeProvider typeProvider) {
        Preconditions.checkArgument(moduleContext != null, "moduleContext cannot be NULL.");
        final DataNodeIterator it = new DataNodeIterator(moduleContext.module());
        final List<TypeDefinition<?>> typeDefinitions = it.allTypedefs();
        Preconditions.checkState(typeDefinitions != null, "Type Definitions for module «module.name» cannot be NULL.");

        typeDefinitions.stream().filter(Objects::nonNull).forEach(typedef -> {
            final Type type = ((TypeProviderImpl) typeProvider).generatedTypeForExtendedDefinitionType(typedef,
                    typedef);
            if (type != null) {
                moduleContext.addTypedefType(typedef.getPath(), type);
                moduleContext.addTypeToSchema(type, typedef);
            }
        });
    }

    private static void actionsAndRPCMethodsToGenType(final ModuleContext moduleContext, final Map<Module,
            ModuleContext> genCtx, final SchemaContext schemaContext, final boolean verboseClassComments,
            final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders, final TypeProvider typeProvider) {

        RpcActionGenHelper.rpcMethodsToGenType(moduleContext, genCtx, schemaContext, verboseClassComments,
                genTypeBuilders, typeProvider);
        RpcActionGenHelper.actionMethodsToGenType(moduleContext, genCtx, schemaContext, verboseClassComments,
                genTypeBuilders, typeProvider);
    }

    /**
     * Converts all <b>identities</b> of the module to the list of
     * <code>Type</code> objects.
     *
     * @param moduleContext
     *            module context
     * @param schemaContext
     *            schema context only used as input parameter for method
     *            {@link GenHelperUtil#identityToGenType(ModuleContext, IdentitySchemaNode, SchemaContext, Map, boolean)}
     * @param genCtx generated context
     * @return returns generated context
     *
     */
    private static void allIdentitiesToGenTypes(final ModuleContext moduleContext,
            final SchemaContext schemaContext, Map<Module, ModuleContext> genCtx, boolean verboseClassComments,
            final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders, final TypeProvider typeProvider) {

        final Set<IdentitySchemaNode> schemaIdentities = moduleContext.module().getIdentities();

        if (schemaIdentities != null && !schemaIdentities.isEmpty()) {
            for (final IdentitySchemaNode identity : schemaIdentities) {
                GenHelperUtil.identityToGenType(moduleContext, identity, schemaContext, genCtx,
                    verboseClassComments);
            }
        }
    }

    /**
     * Converts all <b>notifications</b> of the module to the list of
     * <code>Type</code> objects. In addition are to this list added containers
     * and lists which are part of this notification.
     *
     * @param moduleContext
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
    private static void notificationsToGenType(final ModuleContext moduleContext,
            final Map<Module, ModuleContext> genCtx, final SchemaContext schemaContext,
            final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders,
            final boolean verboseClassComments, final TypeProvider typeProvider) {
        checkArgument(moduleContext != null, "Module reference cannot be NULL.");
        final Set<NotificationDefinition> notifications = moduleContext.module().getNotifications();
        if (notifications.isEmpty()) {
            return;
        }

        final GeneratedTypeBuilder listenerInterface = moduleTypeBuilder("Listener", verboseClassComments,
            moduleContext);
        listenerInterface.addImplementsType(NOTIFICATION_LISTENER);

        for (final NotificationDefinition notification : notifications) {
            if (notification != null) {
                resolveNotification(listenerInterface, null, notification, moduleContext, schemaContext,
                        verboseClassComments, genTypeBuilders, typeProvider, genCtx);
            }
        }

        //YANG 1.1 allows notifications be tied to containers and lists
        final Collection<DataSchemaNode> potentials = moduleContext.module().getChildNodes();
        Set<NotificationDefinition> tiedNotifications = null;

        for (final DataSchemaNode potential : potentials) {
            if (potential instanceof NotificationNodeContainer) {
                tiedNotifications = ((NotificationNodeContainer) potential)
                        .getNotifications();
                for (final NotificationDefinition tiedNotification: tiedNotifications) {
                    if (tiedNotification != null) {
                        resolveNotification(listenerInterface, potential.getQName().getLocalName(),
                            tiedNotification, moduleContext, schemaContext, verboseClassComments, genTypeBuilders,
                            typeProvider, genCtx);
                    }
                }
            }
        }

        if (verboseClassComments) {
            if (tiedNotifications != null) {
                YangSourceDefinition.of(moduleContext.module(),
                    ImmutableSet.<NotificationDefinition>builder().addAll(notifications).addAll(tiedNotifications)
                        .build()).ifPresent(listenerInterface::setYangSourceDefinition);
            } else {
                YangSourceDefinition.of(moduleContext.module(), notifications).ifPresent(listenerInterface::setYangSourceDefinition);
            }
            listenerInterface.addComment(TypeComments.javadoc(
                "Interface for receiving the following YANG notifications defined in module <b>" + moduleContext.module().getName()
                    + "</b>").get());
        }

        moduleContext.addTopLevelNodeType(listenerInterface);
    }
}
