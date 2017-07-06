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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    @SuppressWarnings("unchecked")
    @Test(expected = UnsupportedOperationException.class)
    public void constructorTest() throws Throwable {
        final Constructor<RpcActionGenHelper> constructor =
                (Constructor<RpcActionGenHelper>) RpcActionGenHelper.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        final Object[] objs = {};
        try {
            constructor.newInstance(objs);
        } catch (final Exception e) {
            throw e.getCause();
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void getRoutingContextAbsentTest() throws Exception {
        final Class[] parameterTypes = { DataSchemaNode.class };
        final Method generate = RpcActionGenHelper.class.getDeclaredMethod("getRoutingContext", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);
        final DataSchemaNode dataSchemaNode = mock(DataSchemaNode.class);
        final List<UnknownSchemaNode> unknownSchemaNodes = new ArrayList<>();
        when(dataSchemaNode.getUnknownSchemaNodes()).thenReturn(unknownSchemaNodes);

        final Object[] args = { dataSchemaNode };
        final Optional<QName> result = (Optional<QName>) generate.invoke(RpcActionGenHelper.class, args);
        assertNotNull(result);
        assertTrue(!result.isPresent());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void getRoutingContextTest() throws Exception {
        final Class[] parameterTypes = { DataSchemaNode.class };
        final Method generate = RpcActionGenHelper.class.getDeclaredMethod("getRoutingContext", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final DataSchemaNode dataSchemaNode = mock(DataSchemaNode.class);
        final List<UnknownSchemaNode> unknownSchemaNodes = new ArrayList<>();
        final Field contextRef = RpcActionGenHelper.class.getDeclaredField("CONTEXT_REFERENCE");
        contextRef.setAccessible(true);
        final QName nodeType = (QName) contextRef.get(RpcActionGenHelper.class);
        final UnknownSchemaNode unknownSchemaNode = mock(UnknownSchemaNode.class);
        when(unknownSchemaNode.getNodeType()).thenReturn(nodeType);
        final QName qname = QName.create("test", "2017-05-04", "unknown");
        when(unknownSchemaNode.getQName()).thenReturn(qname);
        unknownSchemaNodes.add(unknownSchemaNode);
        when(dataSchemaNode.getUnknownSchemaNodes()).thenReturn(unknownSchemaNodes);

        final Object[] args = { dataSchemaNode };
        final Optional<QName> result = (Optional<QName>) generate.invoke(RpcActionGenHelper.class, args);
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(qname, result.get());
    }

    @Ignore
    @Test
    public void actionMethodsToGenTypeContainerAsParentTest() throws Exception {
        actionMethodsToGenType(ContainerSchemaNode.class, false);
    }

    @Ignore
    @Test
    public void actionMethodsToGenTypeListAsParentTest() throws Exception {
        actionMethodsToGenType(ListSchemaNode.class, false);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void rpcMethodsToGenTypeNullRpcsTest() throws Exception {
        final Class[] parameterTypes =
                { Module.class, Map.class, SchemaContext.class, boolean.class, Map.class, TypeProvider.class };
        final Method generate = RpcActionGenHelper.class.getDeclaredMethod("rpcMethodsToGenType", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

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

        final Object[] args = { module, genCtx, schemaContext, verboseClassComments, genTypeBuilders, typeProvider };

        try {
            generate.invoke(RpcActionGenHelper.class, args);
            fail();
        } catch (final Exception e) {
            assertNotNull(e);
            assertTrue(e instanceof InvocationTargetException);
            final Throwable cause = e.getCause();
            assertNotNull(cause);
            assertTrue(cause instanceof IllegalStateException);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void rpcMethodsToGenTypeEmptyRpcsTest() throws Exception {
        final Class[] parameterTypes =
                { Module.class, Map.class, SchemaContext.class, boolean.class, Map.class, TypeProvider.class };
        final Method generate = RpcActionGenHelper.class.getDeclaredMethod("rpcMethodsToGenType", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

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

        final Object[] args = { module, genCtx, schemaContext, verboseClassComments, genTypeBuilders, typeProvider };
        final Map<Module, ModuleContext> result =
                (Map<Module, ModuleContext>) generate.invoke(RpcActionGenHelper.class, args);
        assertNotNull(result);
    }

    @Test
    public void rpcMethodsToGenTypeRoutedRpcTest() throws Exception {
        actionMethodsToGenType(ContainerSchemaNode.class, true);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void rpcMethodsToGenTypeRpcTest() throws Exception {
        final Class[] parameterTypes =
                { Module.class, Map.class, SchemaContext.class, boolean.class, Map.class, TypeProvider.class };
        final Method generate = RpcActionGenHelper.class.getDeclaredMethod("rpcMethodsToGenType", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final QName rpcQName = QName.create("test.rpc", "2017-05-04", "rpc-test");

        final ContainerSchemaNode rpcParent = mock(ContainerSchemaNode.class);
        final QName rpcParentQName = QName.create(rpcQName, "rpc-parent");
        when(rpcParent.getQName()).thenReturn(rpcParentQName);

        final Module module = mock(Module.class);
        when(module.getName()).thenReturn("module-name");
        when(module.getRevision()).thenReturn(rpcQName.getRevision());
        when(module.getNamespace()).thenReturn(rpcQName.getNamespace());
        final Set<RpcDefinition> rpcs = new HashSet<>();
        final RpcDefinition rpcDefinition = mock(RpcDefinition.class);
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
        when(rpcDefinition.getInput()).thenReturn(input);

        final ContainerSchemaNode output = mock(ContainerSchemaNode.class);
        final QName qnameOutput = QName.create(rpcQName, "rpc-output");
        final SchemaPath outputSchemaPath = SchemaPath.create(true, rpcQName, qnameOutput);
        when(output.getQName()).thenReturn(qnameOutput);
        when(output.getPath()).thenReturn(outputSchemaPath);
        when(rpcDefinition.getOutput()).thenReturn(output);

        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final ModuleContext moduleContext = new ModuleContext();
        genCtx.put(module, moduleContext);

        final SchemaContext schemaContext = mock(SchemaContext.class);
        when(schemaContext.findModuleByNamespaceAndRevision(rpcQName.getNamespace(), rpcQName.getRevision()))
                .thenReturn(module);

        final boolean verboseClassComments = false;
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();
        final TypeProvider typeProvider = mock(TypeProvider.class);

        final Object[] args = { module, genCtx, schemaContext, verboseClassComments, genTypeBuilders, typeProvider };
        final Map<Module, ModuleContext> result =
                (Map<Module, ModuleContext>) generate.invoke(RpcActionGenHelper.class, args);
        assertNotNull(result);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private <T extends ActionNodeContainer> void actionMethodsToGenType(final Class<T> clazz,
            final boolean isRoutedRpc) throws Exception {
        final Class[] parameterTypes =
                { Module.class, Map.class, SchemaContext.class, boolean.class, Map.class, TypeProvider.class };
        Method generate;
        if (isRoutedRpc) {
            generate = RpcActionGenHelper.class.getDeclaredMethod("rpcMethodsToGenType", parameterTypes);
        } else {
            generate = RpcActionGenHelper.class.getDeclaredMethod("actionMethodsToGenType", parameterTypes);
        }
        assertNotNull(generate);
        generate.setAccessible(true);

        final QName actionQName = QName.create("test.action", "2017-05-04", "action-test");

        final Module module = mock(Module.class);
        when(module.getName()).thenReturn("module-name");
        when(module.getRevision()).thenReturn(actionQName.getRevision());
        when(module.getNamespace()).thenReturn(actionQName.getNamespace());

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

        final ContainerSchemaNode input = mock(ContainerSchemaNode.class);
        final QName qnameInput = QName.create(actionQName, "action-input");
        final SchemaPath inputSchemaPath = SchemaPath.create(true, actionQName, qnameInput);
        when(input.getQName()).thenReturn(qnameInput);
        when(input.getPath()).thenReturn(inputSchemaPath);
        when(actionDefinition.getInput()).thenReturn(input);

        final ContainerSchemaNode output = mock(ContainerSchemaNode.class);
        final QName qnameOutput = QName.create(actionQName, "action-output");
        final SchemaPath outputSchemaPath = SchemaPath.create(true, actionQName, qnameOutput);
        when(output.getQName()).thenReturn(qnameOutput);
        when(output.getPath()).thenReturn(outputSchemaPath);
        when(actionDefinition.getOutput()).thenReturn(output);

        actions.add(actionDefinition);
        when(actionNodeContainer.getActions()).thenReturn(actions);
        childNodes.add((DataSchemaNode) actionNodeContainer);
        when(module.getChildNodes()).thenReturn(childNodes);

        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        final ModuleContext moduleContext = new ModuleContext();
        genCtx.put(module, moduleContext);

        final SchemaContext schemaContext = mock(SchemaContext.class);
        when(schemaContext.findModuleByNamespaceAndRevision(actionQName.getNamespace(), actionQName.getRevision()))
                .thenReturn(module);

        final boolean verboseClassComments = false;
        final Map<String, Map<String, GeneratedTypeBuilder>> genTypeBuilders = new HashMap<>();
        final TypeProvider typeProvider = mock(TypeProvider.class);

        if (isRoutedRpc) {
            final Set<RpcDefinition> rpcs = new HashSet<>();
            final RpcDefinition rpcDef = mock(RpcDefinition.class);
            when(rpcDef.getPath()).thenReturn(outputSchemaPath);
            when(rpcDef.getQName()).thenReturn(qnameOutput);
            when(module.getDataChildByName(actionQName)).thenReturn((ContainerSchemaNode) actionNodeContainer);
            final List<UnknownSchemaNode> unknownSchemaNodes = new ArrayList<>();
            final UnknownSchemaNode unknownSchemaNode = mock(UnknownSchemaNode.class);
            final Field contextRef = RpcActionGenHelper.class.getDeclaredField("CONTEXT_REFERENCE");
            contextRef.setAccessible(true);
            final QName nodeType = (QName) contextRef.get(RpcActionGenHelper.class);
            when(unknownSchemaNode.getNodeType()).thenReturn(nodeType);
            when(unknownSchemaNode.getQName()).thenReturn(nodeType);
            unknownSchemaNodes.add(unknownSchemaNode);
            when(((DataSchemaNode) actionNodeContainer).getUnknownSchemaNodes()).thenReturn(unknownSchemaNodes);
            when(rpcDef.getInput()).thenReturn(input);
            when(rpcDef.getOutput()).thenReturn(output);
            rpcs.add(rpcDef);
            when(module.getRpcs()).thenReturn(rpcs);
        }

        final Object[] args = { module, genCtx, schemaContext, verboseClassComments, genTypeBuilders, typeProvider };
        final Map<Module, ModuleContext> result =
                (Map<Module, ModuleContext>) generate.invoke(RpcActionGenHelper.class, args);
        assertNotNull(result);
    }
}
