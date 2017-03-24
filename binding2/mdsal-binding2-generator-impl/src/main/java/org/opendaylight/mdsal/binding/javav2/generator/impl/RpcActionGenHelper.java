/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.impl;

import static com.google.common.base.Preconditions.checkState;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.annotateDeprecatedIfNecessary;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.checkModuleAndModuleName;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.createDescription;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.AuxiliaryGenUtils.resolveListKeyTOBuilder;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.GenHelperUtil.addImplementedInterfaceFromUses;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.GenHelperUtil.addRawInterfaceDefinition;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.GenHelperUtil.moduleTypeBuilder;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil.encodeAngleBrackets;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingTypes.ACTION;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingTypes.INPUT;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingTypes.INSTANCE_IDENTIFIER;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingTypes.INSTANTIABLE;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingTypes.KEYED_INSTANCE_IDENTIFIER;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingTypes.LIST_ACTION;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingTypes.OUTPUT;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingTypes.RPC;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingTypes.RPC_CALLBACK;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingTypes.TREE_NODE;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingTypes.augmentable;
import static org.opendaylight.mdsal.binding.javav2.generator.util.Types.CLASS;
import static org.opendaylight.mdsal.binding.javav2.generator.util.Types.VOID;
import static org.opendaylight.mdsal.binding.javav2.generator.util.Types.parameterizedTypeFor;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.opendaylight.mdsal.binding.javav2.generator.spi.TypeProvider;
import org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingNamespaceType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.ActionNodeContainer;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.OperationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;

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
     * 1. routed RPC
     * 2. global RPC
     *
     * In 1st case, we need Binding Generator behave like YANG 1.1 Action
     *
     * @param schemaNode RPC input node
     * @return presence optional
     */
    private static Optional<QName> getRoutingContext(final DataSchemaNode schemaNode) {
        for (UnknownSchemaNode extension : schemaNode.getUnknownSchemaNodes()) {
            if (CONTEXT_REFERENCE.equals(extension.getNodeType())) {
                return Optional.fromNullable(extension.getQName());
            }
        }
        return Optional.absent();
    }

    /**
     * Converts Yang 1.1 <b>Actions</b> to list of <code>Type</code> objects.
     * @param module  module from which is obtained set of all Action objects to
     *            iterate over them
     * @param genCtx input, generated context
     * @param verboseClassComments verbosity switch
     * @return generated context
     */
    static Map<Module, ModuleContext> actionMethodsToGenType(final Module module, Map<Module, ModuleContext> genCtx,
            final SchemaContext schemaContext, final boolean verboseClassComments, Map<String, Map<String,
            GeneratedTypeBuilder>> genTypeBuilders, TypeProvider typeProvider) {

        checkModuleAndModuleName(module);
        final Collection<DataSchemaNode> potentials = module.getChildNodes();
        for (DataSchemaNode potential : potentials) {
            if (potential instanceof ActionNodeContainer) {
                final Set<ActionDefinition> actions = ((ActionNodeContainer) potential).getActions();
                for (ActionDefinition action: actions) {
                    genCtx.get(module).addChildNodeType(potential, resolveOperation(potential, action, module,
                            schemaContext, verboseClassComments, genTypeBuilders, genCtx, typeProvider, true));
                }
            }
        }
        return genCtx;
    }

    /**
     * Converts global <b>RPCs</b> inputs and outputs sub-statements of the module
     * to the list of <code>Type</code> objects. In addition, containers
     * and lists which belong to input or output are also part of returning list.
     * Detected routed RPCs are turned to Yang 1.1 Actions
     *
     * @param module
     *            module from which is obtained set of all RPC objects to
     *            iterate over them
     * @param genCtx input, generated context
     * @param verboseClassComments verbosity switch
     *
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if the module is null</li>
     *             <li>if the name of module is null</li>
     *             </ul>
     * @throws IllegalStateException
     *             if set of RPCs from module is null
     *
     * @return generated context
     */
     static Map<Module, ModuleContext> rpcMethodsToGenType(final Module module, Map<Module, ModuleContext> genCtx,
            final SchemaContext schemaContext, final boolean verboseClassComments, Map<String, Map<String,
             GeneratedTypeBuilder>> genTypeBuilders, TypeProvider typeProvider) {

        checkModuleAndModuleName(module);
        final Set<RpcDefinition> rpcDefinitions = module.getRpcs();
        checkState(rpcDefinitions != null, "Set of RPCs from module " + module.getName() + " cannot be NULL.");
        if (rpcDefinitions.isEmpty()) {
            return genCtx;
        }

        for (final RpcDefinition rpc : rpcDefinitions) {
            DataSchemaNode parent = (DataSchemaNode) SchemaContextUtil.findDataSchemaNode(schemaContext, rpc.getPath().getParent());
            //routedRPC?
            if (getRoutingContext(parent).isPresent()) {
                genCtx.get(module).addChildNodeType(parent, resolveOperation(parent, rpc, module, schemaContext,
                        verboseClassComments, genTypeBuilders, genCtx, typeProvider, true));
            } else {
                //global RPC only
                genCtx.get(module).addTopLevelNodeType(resolveOperation(parent, rpc, module, schemaContext,
                        verboseClassComments, genTypeBuilders, genCtx, typeProvider, false));

            }
        }
        return genCtx;
    }

    /**
     * Converts RPC, Action or routed RPC into generated type
     * @return generated type
     */
    private static GeneratedTypeBuilder resolveOperation(final DataSchemaNode parent, final OperationDefinition operation,
            final Module module, final SchemaContext schemaContext, final boolean verboseClassComments,
            Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders, final Map<Module, ModuleContext> genCtx,
            TypeProvider typeProvider, final boolean isAction) {

        //operation name
        final String operationName = operation.getQName().getLocalName();
        //concrete operation name
        final StringBuilder sb = new StringBuilder(operationName).append('_');
        if (isAction) {
            sb.append("Action");
        } else {
            sb.append("Rpc");
        }
        final GeneratedTypeBuilder interfaceBuilder = moduleTypeBuilder(module, sb.toString(),
                verboseClassComments);

        final String basePackageName = interfaceBuilder.getPackageName();

        interfaceBuilder.setDescription(createDescription(operation, interfaceBuilder.getFullyQualifiedName(),
                schemaContext, verboseClassComments));
        final String operationComment = encodeAngleBrackets(operation.getDescription());
        final MethodSignatureBuilder operationMethod = interfaceBuilder.addMethod("invoke");

        //input
        final ContainerSchemaNode input = operation.getInput();
        final GeneratedTypeBuilder inType = resolveOperationNode(module, operation.getInput(), basePackageName,
                schemaContext, operationName, verboseClassComments, typeProvider, genTypeBuilders, genCtx, true);
        annotateDeprecatedIfNecessary(operation.getStatus(), inType);
        genCtx.get(module).addChildNodeType(input, inType);

        //output
        final ContainerSchemaNode output = operation.getOutput();
        final GeneratedTypeBuilder outType = resolveOperationNode(module, operation.getOutput(), basePackageName,
                schemaContext, operationName, verboseClassComments, typeProvider, genTypeBuilders, genCtx, false);
        annotateDeprecatedIfNecessary(operation.getStatus(), outType);
        genCtx.get(module).addChildNodeType(output, outType);

        final GeneratedType inTypeInstance = inType.toInstance();
        operationMethod.addParameter(inTypeInstance, "input");

        if (isAction) {
            //action, routed RPC
            String packageName = BindingGeneratorUtil.packageNameForGeneratedType(basePackageName, parent.getPath(),
                    BindingNamespaceType.Data);
            GeneratedTypeBuilder parentType = addRawInterfaceDefinition(packageName, parent, schemaContext,
                    parent.getQName().getLocalName(), verboseClassComments, genTypeBuilders);
            parentType.addImplementsType(TREE_NODE);
            parentType.addImplementsType(augmentable(parentType));
            annotateDeprecatedIfNecessary(parent.getStatus(), parentType);

            GenHelperUtil.resolveDataSchemaNodes(module, basePackageName, parentType, parentType,
                    ((ContainerSchemaNode) parent).getChildNodes(), genCtx, schemaContext, verboseClassComments,
                    genTypeBuilders, typeProvider);

            operationMethod.addParameter(parameterizedTypeFor(INSTANCE_IDENTIFIER, parentType), "ii");

            if (parent instanceof ListSchemaNode) {
                //ListAction
                final GeneratedTOBuilder keyType = resolveListKeyTOBuilder(basePackageName, (ListSchemaNode) parent);
                operationMethod.addParameter(
                        parameterizedTypeFor(KEYED_INSTANCE_IDENTIFIER, parentType, keyType), "kii");
                operationMethod.setReturnType(keyType);
                interfaceBuilder.addImplementsType(parameterizedTypeFor(LIST_ACTION, parentType, inType, outType));
            } else {
                //Action
                operationMethod.addParameter(parameterizedTypeFor(INSTANCE_IDENTIFIER, parentType), "ii");
                interfaceBuilder.addImplementsType(parameterizedTypeFor(ACTION, parentType, inType, outType));
            }
        } else {
            //RPC
            interfaceBuilder.addImplementsType(parameterizedTypeFor(RPC, inType, outType));
        }

        operationMethod.addParameter(parameterizedTypeFor(RPC_CALLBACK, outType), "callback");

        operationMethod.setComment(operationComment);
        operationMethod.setReturnType(VOID);

        return interfaceBuilder;
    }

    private static GeneratedTypeBuilder resolveOperationNode(final Module module, final ContainerSchemaNode
            operationNode, final String basePackageName, final SchemaContext schemaContext, final String
            operationName, final boolean verboseClassComments, TypeProvider typeProvider, Map<String, Map<String,
            GeneratedTypeBuilder>> genTypeBuilders, final Map<Module, ModuleContext> genCtx, final boolean isInput) {

        final GeneratedTypeBuilder nodeType = addRawInterfaceDefinition(basePackageName, operationNode, schemaContext,
                operationName, verboseClassComments, genTypeBuilders);
        addImplementedInterfaceFromUses(operationNode, nodeType, genCtx);
        nodeType.addImplementsType(TREE_NODE);
        if (isInput) {
            nodeType.addImplementsType(parameterizedTypeFor(INPUT, nodeType));
        } else {
            nodeType.addImplementsType(parameterizedTypeFor(OUTPUT, nodeType));
        }
        nodeType.addImplementsType(parameterizedTypeFor(INSTANTIABLE, nodeType));
        nodeType.addImplementsType(augmentable(nodeType));
        GenHelperUtil.resolveDataSchemaNodes(module, basePackageName, nodeType, nodeType, operationNode.getChildNodes(), genCtx,
                schemaContext, verboseClassComments, genTypeBuilders, typeProvider);

        final MethodSignatureBuilder nodeMethod = nodeType.addMethod("implementedInterface");
        nodeMethod.setReturnType(parameterizedTypeFor(CLASS, nodeType));
        nodeMethod.addAnnotation("", "Override");

        return nodeType;
    }
}
