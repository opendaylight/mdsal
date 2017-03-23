/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.annotateDeprecatedIfNecessary;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.createDescription;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.GenHelperUtil.moduleTypeBuilder;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil.encodeAngleBrackets;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingTypes.NOTIFICATION;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.mdsal.binding.javav2.generator.spi.TypeProvider;
import org.opendaylight.mdsal.binding.javav2.generator.util.BindingTypes;
import org.opendaylight.mdsal.binding.javav2.generator.util.Types;
import org.opendaylight.mdsal.binding.javav2.generator.yang.types.TypeProviderImpl;
import org.opendaylight.mdsal.binding.javav2.model.api.AccessModifier;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.javav2.util.BindingMapping;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.util.DataNodeIterator;

@Beta
final class ModuleToGenType {

    private ModuleToGenType() {
        throw new UnsupportedOperationException("Utility class");
    }

    static Map<Module, ModuleContext> generate(final Module module, Map<String, Map<String, GeneratedTypeBuilder>>
            genTypeBuilders, final SchemaContext schemaContext, TypeProvider typeProvider, Map<Module,
            ModuleContext> genCtx, final boolean verboseClassComments) {

        genCtx.put(module, new ModuleContext());
        genCtx = allTypeDefinitionsToGenTypes(module, genCtx, typeProvider);
        genCtx = notificationsToGenType(module, genCtx, schemaContext, genTypeBuilders, verboseClassComments, typeProvider);

        //TODO: call generate for other entities (groupings, rpcs, actions, identities)

        if (!module.getChildNodes().isEmpty()) {
            final GeneratedTypeBuilder moduleType = GenHelperUtil.moduleToDataType(module, genCtx, verboseClassComments);
            genCtx.get(module).addModuleNode(moduleType);
            final String basePackageName = BindingMapping.getRootPackageName(module);
            GenHelperUtil.resolveDataSchemaNodes(module, basePackageName, moduleType, moduleType, module
                    .getChildNodes(), genCtx, schemaContext, verboseClassComments, genTypeBuilders, typeProvider);
        }

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
    private static Map<Module, ModuleContext> allTypeDefinitionsToGenTypes(final Module module, Map<Module, ModuleContext> genCtx,
            TypeProvider typeProvider) {
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
    private static Map<Module, ModuleContext> notificationsToGenType(final Module module, Map<Module, ModuleContext> genCtx,
            final SchemaContext schemaContext, Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders,
            final boolean verboseClassComments, TypeProvider typeProvider) {
        checkArgument(module != null, "Module reference cannot be NULL.");
        checkArgument(module.getName() != null, "Module name cannot be NULL.");
        final Set<NotificationDefinition> notifications = module.getNotifications();
        if (notifications.isEmpty()) {
            return genCtx;
        }

        final GeneratedTypeBuilder listenerInterface = moduleTypeBuilder(module, "Listener", verboseClassComments);
        listenerInterface.addImplementsType(BindingTypes.NOTIFICATION_LISTENER);
        final String basePackageName = BindingMapping.getRootPackageName(module);

        for (final NotificationDefinition notification : notifications) {
            if (notification != null) {
                GenHelperUtil.processUsesAugments(schemaContext, notification, module, genCtx, genTypeBuilders,
                        verboseClassComments, typeProvider);

                final GeneratedTypeBuilder notificationInterface = GenHelperUtil.addDefaultInterfaceDefinition
                        (basePackageName, notification, null, module, genCtx, schemaContext,
                                verboseClassComments, genTypeBuilders);
                annotateDeprecatedIfNecessary(notification.getStatus(), notificationInterface);
                notificationInterface.addImplementsType(NOTIFICATION);
                genCtx.get(module).addChildNodeType(notification, notificationInterface);

                // Notification object
                GenHelperUtil.resolveDataSchemaNodes(module, basePackageName, notificationInterface,
                        notificationInterface, notification.getChildNodes(), genCtx, schemaContext,
                        verboseClassComments, genTypeBuilders, typeProvider);

                listenerInterface.addMethod("on" + notificationInterface.getName())
                        .setAccessModifier(AccessModifier.PUBLIC).addParameter(notificationInterface, "notification")
                        .setComment(encodeAngleBrackets(notification.getDescription())).setReturnType(Types.VOID);
            }
        }
        listenerInterface.setDescription(createDescription(notifications, module.getName(), verboseClassComments));

        genCtx.get(module).addTopLevelNodeType(listenerInterface);

        return genCtx;
    }
}
