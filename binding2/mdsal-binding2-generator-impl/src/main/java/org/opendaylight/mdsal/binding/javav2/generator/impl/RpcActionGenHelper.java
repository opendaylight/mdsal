/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.impl;

import static com.google.common.base.Preconditions.checkState;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.checkModuleAndModuleName;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.createDescription;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.GenHelperUtil.moduleTypeBuilder;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifier;
import org.opendaylight.mdsal.binding.javav2.generator.util.NonJavaCharsConverter;
import org.opendaylight.mdsal.binding.javav2.generator.util.Types;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.javav2.spec.base.Action;
import org.opendaylight.mdsal.binding.javav2.spec.base.Rpc;
import org.opendaylight.mdsal.binding.javav2.util.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.ActionNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

/**
 *
 * Util class used for generation of types for RPCs, routedRPCs and Actions (YANG 1.1 only)
 * in Binding spec. v2. In case of routed RPC detected in input YANG, RPC is turned to Action.
 *
 */
@Beta
public final class RpcActionGenHelper {

    private static final QName CONTEXT_REFERENCE =
            QName.create("urn:opendaylight:yang:extension:yang-ext", "2013-07-09", "context-reference").intern();

    private RpcActionGenHelper() {
        throw new UnsupportedOperationException("Util class");
    }

    /**
     * Let's find out what context we are talking about
     * 1. global RPC
     * 2. routed RPC
     *
     * In 2nd case, we need Binding Generator behave like YANG 1.1 Action
     *
     * @param schemaNode RPC input node
     * @return
     */
    public static Optional<QName> getRoutingContext(final DataSchemaNode schemaNode) {
        for (UnknownSchemaNode extension : schemaNode.getUnknownSchemaNodes()) {
            if (CONTEXT_REFERENCE.equals(extension.getNodeType())) {
                return Optional.fromNullable(extension.getQName());
            }
        }
        return Optional.absent();
    }

    //TODO: add Javadoc
    /**
     *
     * @param module
     * @param genCtx
     * @param verboseClassComments
     * @return
     */
    static Map<Module, ModuleContext> actionMethodsToGenType(final Module module, Map<Module, ModuleContext> genCtx,
            final SchemaContext schemaContext, final boolean verboseClassComments) {
        checkModuleAndModuleName(module);

        final Collection<DataSchemaNode> potentials = module.getChildNodes();

        for (DataSchemaNode potential : potentials) {
            if (potential instanceof ActionNodeContainer) {
                final Set<ActionDefinition> actions = ((ActionNodeContainer) potential).getActions();
                for (ActionDefinition action: actions) {
                    final String basePackageName = BindingMapping.getRootPackageName(module);
                    //Binding spec v2 - one interface per one Action, so let's have MyModuleMyCallAction.java
                    final String postfix = NonJavaCharsConverter.convertIdentifier(action.getQName().getLocalName(),
                            JavaIdentifier.CLASS);
                    final GeneratedTypeBuilder interfaceBuilder = moduleTypeBuilder(module, postfix + "Action",
                            verboseClassComments);
                    interfaceBuilder.addImplementsType(Types.typeForClass(Action.class));
                    final String fullyQualifiedName = basePackageName + "." +
                            NonJavaCharsConverter.convertIdentifier(action.getQName().getLocalName(), JavaIdentifier.CLASS);
                    interfaceBuilder.setDescription(createDescription(action, fullyQualifiedName,
                            schemaContext, verboseClassComments));

                    //TODO: implement further down...
                }
            }
        }

        return genCtx;
    }

    //FIXME: correct Javadoc
    /**
     *
     * Converts all global <b>RPCs</b> inputs and outputs sub-statements of the module
     * to the list of <code>Type</code> objects. In addition are to containers
     * and lists which belong to input or output also part of returning list.
     *
     * @param module
     *            module from which is obtained set of all rpc objects to
     *            iterate over them
     * @param genCtx
     * @param verboseClassComments
     *
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if the module is null</li>
     *             <li>if the name of module is null</li>
     *             </ul>
     * @throws IllegalStateException
     *             if set of RPCs from module is null
     *
     * @return
     *
     */
     static Map<Module, ModuleContext> rpcMethodsToGenType(final Module module, Map<Module, ModuleContext> genCtx,
            final SchemaContext schemaContext, final boolean verboseClassComments) {
        checkModuleAndModuleName(module);

        final Set<RpcDefinition> rpcDefinitions = module.getRpcs();
        checkState(rpcDefinitions != null, "Set of RPCs from module " + module.getName() + " cannot be NULL.");
        if (rpcDefinitions.isEmpty()) {
            return genCtx;
        }

        for (final RpcDefinition rpc : rpcDefinitions) {
            if (rpc != null) {
                final String basePackageName = BindingMapping.getRootPackageName(module);
                //Binding spec v2 - one interface per one RPC, so let's have MyModuleMyCallRpc.java
                final String postfix = NonJavaCharsConverter.convertIdentifier(rpc.getQName().getLocalName(), JavaIdentifier.CLASS);
                final GeneratedTypeBuilder interfaceBuilder = moduleTypeBuilder(module, postfix + "Rpc",
                        verboseClassComments);
                interfaceBuilder.addImplementsType(Types.typeForClass(Rpc.class));
                final String fullyQualifiedName = basePackageName + "." +
                        NonJavaCharsConverter.convertIdentifier(rpc.getQName().getLocalName(), JavaIdentifier.CLASS);
                interfaceBuilder.setDescription(createDescription(rpc, fullyQualifiedName, schemaContext,
                        verboseClassComments));

                //TODO: implement further down...
            }
        }

        return genCtx;

    }
}
