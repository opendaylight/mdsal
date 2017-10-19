/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.generator.context.ModuleContext;
import org.opendaylight.mdsal.binding.javav2.generator.spi.TypeProvider;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.ActionNodeContainer;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

public class RpcActionGenHelperTest {
    // Bridge for method references
    @FunctionalInterface
    private interface GeneratorMethod {
        Map<Module, ModuleContext> generate(Module module, Map<Module, ModuleContext> genCtx,
                SchemaContext schemaContext, boolean verboseClassComments,
                Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders, TypeProvider typeProvider);
    }

    @Test
    public void constructorTest() throws NoSuchMethodException {
        final Constructor<RpcActionGenHelper> constructor = RpcActionGenHelper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
        } catch (final InstantiationException | IllegalAccessException
            | InvocationTargetException | IllegalArgumentException e) {
            assertTrue(e.getCause() instanceof UnsupportedOperationException);
        }
    }

    @Test
    public void getRoutingContextAbsentTest() {
        final DataSchemaNode dataSchemaNode = mock(DataSchemaNode.class);
        final List<UnknownSchemaNode> unknownSchemaNodes = new ArrayList<>();
        when(dataSchemaNode.getUnknownSchemaNodes()).thenReturn(unknownSchemaNodes);

        final Optional<QName> result = RpcActionGenHelper.getRoutingContext(dataSchemaNode);
        assertNotNull(result);
        assertTrue(!result.isPresent());
    }

    @Test
    public void getRoutingContextTest() {
        final DataSchemaNode dataSchemaNode = mock(DataSchemaNode.class);
        final List<UnknownSchemaNode> unknownSchemaNodes = new ArrayList<>();
        final UnknownSchemaNode unknownSchemaNode = mock(UnknownSchemaNode.class);
        when(unknownSchemaNode.getNodeType()).thenReturn(RpcActionGenHelper.CONTEXT_REFERENCE);
        final QName qname = QName.create("test", "2017-05-04", "unknown");
        when(unknownSchemaNode.getQName()).thenReturn(qname);
        unknownSchemaNodes.add(unknownSchemaNode);
        when(dataSchemaNode.getUnknownSchemaNodes()).thenReturn(unknownSchemaNodes);

        final Optional<QName> result = RpcActionGenHelper.getRoutingContext(dataSchemaNode);
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(qname, result.get());
    }

    @Ignore
    @Test
    public void actionMethodsToGenTypeContainerAsParentTest() {
        actionMethodsToGenType(ContainerSchemaNode.class, false);
    }

    @Ignore
    @Test
    public void actionMethodsToGenTypeListAsParentTest() {
        actionMethodsToGenType(ListSchemaNode.class, false);
    }

    @Test(expected = IllegalStateException.class)
    public void rpcMethodsToGenTypeNullRpcsTest() {
        final QName rpcQName = QName.create("test.rpc", "2017-05-04", "rpc-test");

        final Module module = mock(Module.class);
        when(module.getName()).thenReturn("module-name");
        when(module.getRevision()).thenReturn(rpcQName.getRevision());
        when(module.getNamespace()).thenReturn(rpcQName.getNamespace());
        when(module.getRpcs()).thenReturn(null);

        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final SchemaContext schemaContext = mock(SchemaContext.class);
        final boolean verboseClassComments = false;
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();
        final TypeProvider typeProvider = mock(TypeProvider.class);

        RpcActionGenHelper.rpcMethodsToGenType(module, genCtx, schemaContext, verboseClassComments, genTypeBuilders,
            typeProvider);
    }

    @Test
    public void rpcMethodsToGenTypeEmptyRpcsTest() {
        final QName rpcQName = QName.create("test.rpc", "2017-05-04", "rpc-test");

        final Module module = mock(Module.class);
        when(module.getName()).thenReturn("module-name");
        when(module.getRevision()).thenReturn(rpcQName.getRevision());
        when(module.getNamespace()).thenReturn(rpcQName.getNamespace());
        final Set<RpcDefinition> rpcs = new HashSet<>();
        when(module.getRpcs()).thenReturn(rpcs);

        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final SchemaContext schemaContext = mock(SchemaContext.class);
        final boolean verboseClassComments = false;
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();
        final TypeProvider typeProvider = mock(TypeProvider.class);

        final Map<Module, ModuleContext> result = RpcActionGenHelper.rpcMethodsToGenType(module, genCtx, schemaContext,
            verboseClassComments, genTypeBuilders, typeProvider);
        assertNotNull(result);
    }

    @Test
    public void rpcMethodsToGenTypeRoutedRpcTest() {
        actionMethodsToGenType(ContainerSchemaNode.class, true);
    }

    @Test
    public void rpcMethodsToGenTypeRpcTest() {
        final QName rpcQName = QName.create("test.rpc", "2017-05-04", "rpc-test");

        final ContainerSchemaNode rpcParent = mock(ContainerSchemaNode.class);
        final QName rpcParentQName = QName.create(rpcQName, "rpc-parent");
        when(rpcParent.getQName()).thenReturn(rpcParentQName);

        final Module module = mock(Module.class);
        when(module.getName()).thenReturn("module-name");
        when(module.getRevision()).thenReturn(rpcQName.getRevision());
        when(module.getNamespace()).thenReturn(rpcQName.getNamespace());
        when(module.getDescription()).thenReturn(java.util.Optional.empty());
        when(module.getReference()).thenReturn(java.util.Optional.empty());
        final Set<RpcDefinition> rpcs = new HashSet<>();
        final RpcDefinition rpcDefinition = mock(RpcDefinition.class);
        when(rpcDefinition.getDescription()).thenReturn(java.util.Optional.empty());
        when(rpcDefinition.getReference()).thenReturn(java.util.Optional.empty());
        final SchemaPath rpcPath = SchemaPath.create(true, rpcParentQName, rpcQName);
        when(rpcDefinition.getPath()).thenReturn(rpcPath);
        when(rpcDefinition.getQName()).thenReturn(rpcQName);

        when(module.getDataChildByName(rpcParentQName)).thenReturn(rpcParent);
        rpcs.add(rpcDefinition);
        when(module.getRpcs()).thenReturn(rpcs);

        final ContainerSchemaNode input = mock(ContainerSchemaNode.class);
        final QName qnameInput = QName.create(rpcQName, "rpc-input");
        final SchemaPath inputSchemaPath = SchemaPath.create(true, rpcQName, qnameInput);
        when(input.getQName()).thenReturn(qnameInput);
        when(input.getPath()).thenReturn(inputSchemaPath);
        when(input.getDescription()).thenReturn(java.util.Optional.empty());
        when(input.getReference()).thenReturn(java.util.Optional.empty());

        when(rpcDefinition.getInput()).thenReturn(input);

        final ContainerSchemaNode output = mock(ContainerSchemaNode.class);
        final QName qnameOutput = QName.create(rpcQName, "rpc-output");
        final SchemaPath outputSchemaPath = SchemaPath.create(true, rpcQName, qnameOutput);
        when(output.getQName()).thenReturn(qnameOutput);
        when(output.getPath()).thenReturn(outputSchemaPath);
        when(output.getDescription()).thenReturn(java.util.Optional.empty());
        when(output.getReference()).thenReturn(java.util.Optional.empty());
        when(rpcDefinition.getOutput()).thenReturn(output);

        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final ModuleContext moduleContext = new ModuleContext();
        genCtx.put(module, moduleContext);

        final SchemaContext schemaContext = mock(SchemaContext.class);
        when(schemaContext.findModule(rpcQName.getModule())).thenReturn(java.util.Optional.of(module));

        final boolean verboseClassComments = false;
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();
        final TypeProvider typeProvider = mock(TypeProvider.class);

        final Map<Module, ModuleContext> result = RpcActionGenHelper.rpcMethodsToGenType(module, genCtx, schemaContext,
            verboseClassComments, genTypeBuilders, typeProvider);
        assertNotNull(result);
    }

    private static <T extends ActionNodeContainer> void actionMethodsToGenType(final Class<T> clazz,
            final boolean isRoutedRpc) {
        final GeneratorMethod generate;
        if (isRoutedRpc) {
            generate = RpcActionGenHelper::rpcMethodsToGenType;
        } else {
            generate = RpcActionGenHelper::actionMethodsToGenType;
        }

        final QName actionQName = QName.create("test.action", "2017-05-04", "action-test");

        final Module module = mock(Module.class);
        when(module.getName()).thenReturn("module-name");
        when(module.getRevision()).thenReturn(actionQName.getRevision());
        when(module.getNamespace()).thenReturn(actionQName.getNamespace());
        when(module.getDescription()).thenReturn(java.util.Optional.empty());
        when(module.getReference()).thenReturn(java.util.Optional.empty());

        final Collection<DataSchemaNode> childNodes = new ArrayList<>();
        final T actionNodeContainer = mock(clazz);
        final QName actionParentQName = QName.create(actionQName, "action-parent");
        final SchemaPath actionParentPath = SchemaPath.create(true, actionParentQName);
        when(((SchemaNode) actionNodeContainer).getPath()).thenReturn(actionParentPath);
        when(((SchemaNode) actionNodeContainer).getQName()).thenReturn(actionParentQName);
        if (clazz == ListSchemaNode.class) {
            final List<QName> keyQNames = new ArrayList<>();
            keyQNames.add(QName.create(actionParentQName, "keyActions"));
            when(((ListSchemaNode) actionNodeContainer).getKeyDefinition()).thenReturn(keyQNames);
        }

        final Set<ActionDefinition> actions = new HashSet<>();

        final ActionDefinition actionDefinition = mock(ActionDefinition.class);
        when(actionDefinition.getQName()).thenReturn(actionQName);
        final SchemaPath actionPath = SchemaPath.create(true, actionQName);
        when(actionDefinition.getPath()).thenReturn(actionPath);
        when(actionDefinition.getDescription()).thenReturn(java.util.Optional.empty());
        when(actionDefinition.getReference()).thenReturn(java.util.Optional.empty());

        final ContainerSchemaNode input = mock(ContainerSchemaNode.class);
        final QName qnameInput = QName.create(actionQName, "action-input");
        final SchemaPath inputSchemaPath = SchemaPath.create(true, actionQName, qnameInput);
        when(input.getQName()).thenReturn(qnameInput);
        when(input.getPath()).thenReturn(inputSchemaPath);
        when(input.getDescription()).thenReturn(java.util.Optional.empty());
        when(input.getReference()).thenReturn(java.util.Optional.empty());
        when(actionDefinition.getInput()).thenReturn(input);

        final ContainerSchemaNode output = mock(ContainerSchemaNode.class);
        final QName qnameOutput = QName.create(actionQName, "action-output");
        final SchemaPath outputSchemaPath = SchemaPath.create(true, actionQName, qnameOutput);
        when(output.getQName()).thenReturn(qnameOutput);
        when(output.getPath()).thenReturn(outputSchemaPath);
        when(output.getDescription()).thenReturn(java.util.Optional.empty());
        when(output.getReference()).thenReturn(java.util.Optional.empty());
        when(actionDefinition.getOutput()).thenReturn(output);

        actions.add(actionDefinition);
        when(actionNodeContainer.getActions()).thenReturn(actions);
        childNodes.add((DataSchemaNode) actionNodeContainer);
        when(module.getChildNodes()).thenReturn(childNodes);

        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final ModuleContext moduleContext = new ModuleContext();
        genCtx.put(module, moduleContext);

        final SchemaContext schemaContext = mock(SchemaContext.class);
        when(schemaContext.findModule(actionQName.getModule())).thenReturn(java.util.Optional.of(module));

        final boolean verboseClassComments = false;
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();
        final TypeProvider typeProvider = mock(TypeProvider.class);

        if (isRoutedRpc) {
            final Set<RpcDefinition> rpcs = new HashSet<>();
            final RpcDefinition rpcDef = mock(RpcDefinition.class);
            when(rpcDef.getPath()).thenReturn(outputSchemaPath);
            when(rpcDef.getQName()).thenReturn(qnameOutput);
            when(rpcDef.getDescription()).thenReturn(java.util.Optional.empty());
            when(rpcDef.getReference()).thenReturn(java.util.Optional.empty());

            when(module.getDataChildByName(actionQName)).thenReturn((ContainerSchemaNode) actionNodeContainer);
            final List<UnknownSchemaNode> unknownSchemaNodes = new ArrayList<>();
            final UnknownSchemaNode unknownSchemaNode = mock(UnknownSchemaNode.class);
            when(unknownSchemaNode.getNodeType()).thenReturn(RpcActionGenHelper.CONTEXT_REFERENCE);
            when(unknownSchemaNode.getQName()).thenReturn(RpcActionGenHelper.CONTEXT_REFERENCE);
            unknownSchemaNodes.add(unknownSchemaNode);
            when(((DataSchemaNode) actionNodeContainer).getUnknownSchemaNodes()).thenReturn(unknownSchemaNodes);
            when(rpcDef.getInput()).thenReturn(input);
            when(rpcDef.getOutput()).thenReturn(output);
            rpcs.add(rpcDef);
            when(module.getRpcs()).thenReturn(rpcs);
        }

        final Map<Module, ModuleContext> result = generate.generate(module, genCtx, schemaContext, verboseClassComments,
            genTypeBuilders, typeProvider);
        assertNotNull(result);
    }
}
