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
import static org.opendaylight.mdsal.binding.javav2.generator.impl.GenHelperUtil.addImplementedInterfaceFromUses;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.GenHelperUtil.addRawInterfaceDefinition;
import static org.opendaylight.mdsal.binding.javav2.generator.impl.GenHelperUtil.moduleTypeBuilder;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil.encodeAngleBrackets;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingTypes.ACTION;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingTypes.INPUT;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingTypes.INSTANTIABLE;
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
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifier;
import org.opendaylight.mdsal.binding.javav2.generator.util.NonJavaCharsConverter;
import org.opendaylight.mdsal.binding.javav2.generator.util.Types;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.mdsal.binding.javav2.spec.base.Action;
import org.opendaylight.mdsal.binding.javav2.spec.base.ListAction;
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
                final GeneratedTypeBuilder generatedRpcTypeBuilder = resolveOperation(parent, rpc, module,
                        schemaContext, verboseClassComments, genTypeBuilders, genCtx,
                        typeProvider, false);

                genCtx.get(module).addTopLevelNodeType(generatedRpcTypeBuilder);
            }
        }
        return genCtx;
    }

    /**
     * Converts Action or routed RPC into generated type
     * @return generated Action
     */
    private static GeneratedTypeBuilder resolveOperation(final DataSchemaNode parent, final OperationDefinition operation,
            final Module module, final SchemaContext schemaContext, final boolean verboseClassComments,
            Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders, final Map<Module, ModuleContext> genCtx,
            TypeProvider typeProvider, final boolean isAction) {


        final String postfix = NonJavaCharsConverter.convertIdentifier(operation.getQName().getLocalName(),
                JavaIdentifier.CLASS);
        final String operationName = isAction ? postfix + "_" + "Action" : postfix + "_" + "Rpc";
        final GeneratedTypeBuilder interfaceBuilder = moduleTypeBuilder(module, operationName,
                verboseClassComments);

        final String basePackageName = interfaceBuilder.getPackageName();

        if (parent instanceof ListSchemaNode) {
            interfaceBuilder.addImplementsType(Types.typeForClass(ListAction.class));
        } else {
            interfaceBuilder.addImplementsType(Types.typeForClass(Action.class));
        }
        interfaceBuilder.setDescription(createDescription(operation, interfaceBuilder.getFullyQualifiedName(),
                schemaContext, verboseClassComments));
        final String operationComment = encodeAngleBrackets(operation.getDescription());
        final String operationClassName = NonJavaCharsConverter.convertIdentifier(operation.getQName().getLocalName(),
                JavaIdentifier.CLASS);
        final MethodSignatureBuilder operationMethod = interfaceBuilder.addMethod("invoke");
        final ContainerSchemaNode input = operation.getInput();
        final ContainerSchemaNode output = operation.getOutput();

        //input
        final GeneratedTypeBuilder inType = addRawInterfaceDefinition(basePackageName, input,
                schemaContext,
                operationClassName, verboseClassComments, genTypeBuilders);
        addImplementedInterfaceFromUses(input, inType, genCtx);
        inType.addImplementsType(TREE_NODE);
        inType.addImplementsType(parameterizedTypeFor(INPUT, inType));
        inType.addImplementsType(parameterizedTypeFor(INSTANTIABLE, inType));
        inType.addImplementsType(augmentable(inType));
        annotateDeprecatedIfNecessary(operation.getStatus(), inType);
        GenHelperUtil.resolveDataSchemaNodes(module, basePackageName, inType, inType, input.getChildNodes(), genCtx,
                schemaContext, verboseClassComments, genTypeBuilders, typeProvider);

        final MethodSignatureBuilder inputMethod = interfaceBuilder.addMethod("implementedInterface");
        inputMethod.setReturnType(parameterizedTypeFor(CLASS, inType));
        inputMethod.addAnnotation("", "Override");

        genCtx.get(module).addChildNodeType(input, inType);

        //output
        final GeneratedTypeBuilder outType = addRawInterfaceDefinition(basePackageName, output, schemaContext,
                operationClassName, verboseClassComments, genTypeBuilders);
        addImplementedInterfaceFromUses(output, outType, genCtx);
        outType.addImplementsType(TREE_NODE);
        outType.addImplementsType(parameterizedTypeFor(OUTPUT, outType));
        outType.addImplementsType(parameterizedTypeFor(INSTANTIABLE, outType));
        outType.addImplementsType(augmentable(outType));
        annotateDeprecatedIfNecessary(operation.getStatus(), outType);
        GenHelperUtil.resolveDataSchemaNodes(module, basePackageName, outType, outType, output.getChildNodes(), genCtx,
                schemaContext, verboseClassComments, genTypeBuilders, typeProvider);


        final MethodSignatureBuilder outputMethod = interfaceBuilder.addMethod("implementedInterface");
        outputMethod.setReturnType(parameterizedTypeFor(CLASS, outType));
        outputMethod.addAnnotation("", "Override");

        genCtx.get(module).addChildNodeType(input, inType);

        interfaceBuilder.addImplementsType(parameterizedTypeFor(isAction ? ACTION : RPC, inType, outType));

        final GeneratedType inTypeInstance = inType.toInstance();
        operationMethod.addParameter(inTypeInstance, "input");
        operationMethod.addParameter(parameterizedTypeFor(RPC_CALLBACK, outType), "callback");

        operationMethod.setComment(operationComment);
        operationMethod.setReturnType(VOID);

        return interfaceBuilder;
    }
}
