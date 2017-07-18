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

import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.generator.spi.TypeProvider;
import org.opendaylight.mdsal.binding.javav2.generator.util.Types;
import org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder.GeneratedTOBuilderImpl;
import org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder.GeneratedTypeBuilderImpl;
import org.opendaylight.mdsal.binding.javav2.generator.yang.types.TypeProviderImpl;
import org.opendaylight.mdsal.binding.javav2.model.api.Constant;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.EnumBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingNamespaceType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class AuxiliaryGenUtilsTest {

    @SuppressWarnings("unchecked")
    @Test(expected = UnsupportedOperationException.class)
    public void constructorTest() throws Throwable {
        final Constructor<AuxiliaryGenUtils> constructor =
                (Constructor<AuxiliaryGenUtils>) AuxiliaryGenUtils.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        final Object[] objs = {};
        try {
            constructor.newInstance(objs);
        } catch (final Exception e) {
            throw e.getCause();
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void annotateDeprecatedIfNecessaryNonDepricatedTest() throws Exception {
        final Class[] parameterTypes = { Status.class, GeneratedTypeBuilder.class };
        final Method generate =
                AuxiliaryGenUtils.class.getDeclaredMethod("annotateDeprecatedIfNecessary", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final GeneratedTypeBuilderImpl generatedTypeBuilder =
                new GeneratedTypeBuilderImpl("test.deprecated", "Non_Deprecated");
        final Status status = Status.CURRENT;

        final Object[] args = { status, generatedTypeBuilder };
        generate.invoke(AuxiliaryGenUtils.class, args);
        assertTrue(generatedTypeBuilder.toInstance().getAnnotations().isEmpty());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void annotateDeprecatedIfNecessaryDepricatedTest() throws Exception {
        final Class[] parameterTypes = { Status.class, GeneratedTypeBuilder.class };
        final Method generate =
                AuxiliaryGenUtils.class.getDeclaredMethod("annotateDeprecatedIfNecessary", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final GeneratedTypeBuilderImpl generatedTypeBuilder =
                new GeneratedTypeBuilderImpl("test.deprecated", "Deprecated");
        final Status status = Status.DEPRECATED;

        final Object[] args = { status, generatedTypeBuilder };
        generate.invoke(AuxiliaryGenUtils.class, args);
        assertTrue(!generatedTypeBuilder.toInstance().getAnnotations().isEmpty());
        assertEquals("Deprecated", generatedTypeBuilder.toInstance().getAnnotations().get(0).getName());
    }

    @Test
    public void hasBuilderClassFalseTest() throws Exception {
        assertEquals(false, hasBuilderClass(LeafSchemaNode.class));
    }

    @Test
    public void hasBuilderClassContainerTest() throws Exception {
        assertEquals(true, hasBuilderClass(ContainerSchemaNode.class));
    }

    @Test
    public void hasBuilderClassListTest() throws Exception {
        assertEquals(true, hasBuilderClass(ListSchemaNode.class));
    }

    @Test
    public void hasBuilderClassRpcTest() throws Exception {
        assertEquals(true, hasBuilderClass(RpcDefinition.class));
    }

    @Test
    public void hasBuilderClassNotificationTest() throws Exception {
        assertEquals(true, hasBuilderClass(NotificationDefinition.class));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void qNameConstantTest() throws Exception {
        final Class[] parameterTypes = { GeneratedTypeBuilderBase.class, String.class, QName.class };
        final Method generate = AuxiliaryGenUtils.class.getDeclaredMethod("qNameConstant", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final GeneratedTypeBuilderBase gtbb = new GeneratedTypeBuilderImpl("test", "qname_constants");
        final String constantName = "ConstantName";
        final QName constantQName = QName.create("urn:constant", "2017-04-06", constantName);

        final Object[] args = { gtbb, constantName, constantQName };
        final Constant result = (Constant) generate.invoke(AuxiliaryGenUtils.class, args);
        assertEquals(constantName, result.getName());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void constructGetterTest() throws Exception {
        final Class[] parameterTypes =
                { GeneratedTypeBuilder.class, String.class, String.class, Type.class, Status.class };
        final Method generate = AuxiliaryGenUtils.class.getDeclaredMethod("constructGetter", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final GeneratedTypeBuilder gtb = new GeneratedTypeBuilderImpl("test", "Getter_of");
        final String schemaNodeName = "schema_node_getter";
        final String comment = null;
        final Type returnType = Types.STRING;
        final Status status = Status.DEPRECATED;

        final Object[] args = { gtb, schemaNodeName, comment, returnType, status };
        final MethodSignatureBuilder result = (MethodSignatureBuilder) generate.invoke(AuxiliaryGenUtils.class, args);
        assertEquals(new StringBuilder("get").append("SchemaNodeGetter").toString(),
                result.toInstance(returnType).getName());
    }

    @Test
    public void getterMethodNameBooleanTest() throws Exception {
        assertEquals("isBooleanMethod", getterMethodName("boolean_method", Types.BOOLEAN));
    }

    @Test
    public void getterMethodNameTest() throws Exception {
        assertEquals("getClazz", getterMethodName("clazz", Types.CLASS));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void createDescriptionWithSchemaNodeTest() throws Exception {
        final Class[] parameterTypes = { SchemaNode.class, String.class, SchemaContext.class, boolean.class,
        BindingNamespaceType.class};
        final Method generate = AuxiliaryGenUtils.class.getDeclaredMethod("createDescription", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final SchemaContext schemaContext = YangParserTestUtils.parseYangSource("/generator/test-list.yang");
        final ListSchemaNode containerSchemaNode =
                (ListSchemaNode) schemaContext.getModules().iterator().next().getChildNodes().iterator().next();
        final String fullyQualifiedName =
                "org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314.data.MyList";

        final Object[] args = { containerSchemaNode, fullyQualifiedName, schemaContext, true, BindingNamespaceType.Data };
        final String result = (String) generate.invoke(AuxiliaryGenUtils.class, args);
        assertNotNull(result);
        assertTrue(result.contains("list my-list"));
        assertTrue(result.contains("leaf key"));
        assertTrue(result.contains("leaf key1"));
        assertTrue(result.contains("leaf key2"));
        assertTrue(result.contains("leaf foo"));
        assertTrue(result.contains("@see org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314.dto.MyListBuilder"));
        assertTrue(result.contains("@see org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314.key.my_list.MyListKey"));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void createDescriptionWithSchemaNodeWithDescriptionTest() throws Exception {
        final Class[] parameterTypes = { SchemaNode.class, String.class, SchemaContext.class, boolean.class,
                BindingNamespaceType.class};
        final Method generate = AuxiliaryGenUtils.class.getDeclaredMethod("createDescription", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final SchemaContext schemaContext =
                YangParserTestUtils.parseYangSource("/base/test-leaf-with-description.yang");
        final LeafSchemaNode containerSchemaNode =
                (LeafSchemaNode) schemaContext.getModules().iterator().next().getChildNodes().iterator().next();
        final String fullyQualifiedName = "test.base.cont.with.leaf.MyList";

        final Object[] args = { containerSchemaNode, fullyQualifiedName, schemaContext, true, BindingNamespaceType.Data};
        final String result = (String) generate.invoke(AuxiliaryGenUtils.class, args);
        assertNotNull(result);
        assertTrue(result.contains("I am leaf."));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void createDescriptionTest() throws Exception {
        final Class[] parameterTypes = { Module.class, boolean.class };
        final Method generate = AuxiliaryGenUtils.class.getDeclaredMethod("createDescription", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final SchemaContext schemaContext = YangParserTestUtils.parseYangSource("/base/test-module.yang");

        final Object[] args = { schemaContext.getModules().iterator().next(), true };
        final String result = (String) generate.invoke(AuxiliaryGenUtils.class, args);
        assertNotNull(result);
        assertTrue(result.contains("Base test module description"));
        assertTrue(result.contains("test-module"));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void createDescriptionWithSchemaNodesTest() throws Exception {
        final Class[] parameterTypes = { Set.class, Module.class, boolean.class };
        final Method generate = AuxiliaryGenUtils.class.getDeclaredMethod("createDescription", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final SchemaContext schemaContext = YangParserTestUtils.parseYangSource("/base/test-rpc-and-notification.yang");
        final Module module = schemaContext.getModules().iterator().next();
        Set schemaNodes = new HashSet<>();
        schemaNodes.add(module.getRpcs().iterator().next());

        final Object[] args = { schemaNodes, module, true };
        String result = (String) generate.invoke(AuxiliaryGenUtils.class, args);
        assertNotNull(result);
        assertTrue(result.contains(
                "Interface for implementing the following YANG RPCs defined in module <b>test-rpc-and-notification-module</b>"));
        assertTrue(result.contains("rpc my-rpc"));
        assertTrue(!result.contains("notification my-notification"));

        schemaNodes = new HashSet<>();
        schemaNodes.add(module.getNotifications().iterator().next());

        final Object[] args_n = { schemaNodes, module, true };
        result = (String) generate.invoke(AuxiliaryGenUtils.class, args_n);
        assertNotNull(result);
        assertTrue(result.contains(
                "Interface for receiving the following YANG notifications defined in module <b>test-rpc-and-notification-module</b>"));
        assertTrue(!result.contains("rpc my-rpc"));
        assertTrue(result.contains("notification my-notification"));
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void isNullOrEmptyIsNullTest() throws Exception {
        final Class[] parameterTypes = { Collection.class };
        final Method generate = AuxiliaryGenUtils.class.getDeclaredMethod("isNullOrEmpty", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final Collection list = null;

        final Object[] args_n = { list };
        final boolean result = (boolean) generate.invoke(AuxiliaryGenUtils.class, args_n);
        assertTrue(result);
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void isNullOrEmptyIsEmptyTest() throws Exception {
        final Class[] parameterTypes = { Collection.class };
        final Method generate = AuxiliaryGenUtils.class.getDeclaredMethod("isNullOrEmpty", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final Collection list = new ArrayList<>();

        final Object[] args_n = { list };
        final boolean result = (boolean) generate.invoke(AuxiliaryGenUtils.class, args_n);
        assertTrue(result);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void isNullOrEmptyNotNullNotEmptyTest() throws Exception {
        final Class[] parameterTypes = { Collection.class };
        final Method generate = AuxiliaryGenUtils.class.getDeclaredMethod("isNullOrEmpty", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final Collection list = new ArrayList<>();
        list.add(new Object());

        final Object[] args_n = { list };
        final boolean result = (boolean) generate.invoke(AuxiliaryGenUtils.class, args_n);
        assertTrue(!result);
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void augGenTypeNameTest() throws Exception {
        final Class[] parameterTypes = { Map.class, String.class };
        final Method generate = AuxiliaryGenUtils.class.getDeclaredMethod("augGenTypeName", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final Map<String, GeneratedTypeBuilder> builders = new HashMap<>();
        builders.put("genTypeName1", new GeneratedTypeBuilderImpl("pckg.a1", "gen_a_1"));
        builders.put("genTypeName2", new GeneratedTypeBuilderImpl("pckg.a2", "gen_a_2"));
        final String genTypeName = "genTypeName";

        final Object[] args_n = { builders, genTypeName };
        final String result = (String) generate.invoke(AuxiliaryGenUtils.class, args_n);
        assertEquals("genTypeName3", result);
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void getAugmentIdentifierNullTest() throws Exception {
        final Class[] parameterTypes = { List.class };
        final Method generate = AuxiliaryGenUtils.class.getDeclaredMethod("getAugmentIdentifier", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final List<UnknownSchemaNode> list = new ArrayList<>();

        final Object[] args_n = { list };
        final String result = (String) generate.invoke(AuxiliaryGenUtils.class, args_n);
        assertEquals(null, result);
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void getAugmentIdentifierTest() throws Exception {
        final Class[] parameterTypes = { List.class };
        final Method generate = AuxiliaryGenUtils.class.getDeclaredMethod("getAugmentIdentifier", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final List<UnknownSchemaNode> list = new ArrayList<>();
        final UnknownSchemaNode unknownSchemaNode = mock(UnknownSchemaNode.class);
        final QName qname =
                QName.create("urn:opendaylight:yang:extension:yang-ext", "2017-10-04", "augment-identifier");
        when(unknownSchemaNode.getNodeType()).thenReturn(qname);
        final String value = "node parameter";
        when(unknownSchemaNode.getNodeParameter()).thenReturn(value);
        list.add(unknownSchemaNode);

        final Object[] args_n = { list };
        final String result = (String) generate.invoke(AuxiliaryGenUtils.class, args_n);
        assertEquals(value, result);
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void resolveInnerEnumFromTypeDefinitionNullTest() throws Exception {
        final Class[] parameterTypes =
                { EnumTypeDefinition.class, QName.class, Map.class, GeneratedTypeBuilder.class, Module.class };
        final Method generate = AuxiliaryGenUtils.class.getDeclaredMethod("resolveInnerEnumFromTypeDefinition", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        EnumTypeDefinition enumTypeDefinition = null;
        final QName qname = null;
        final GeneratedTypeBuilder gtb = null;
        final Map map = new HashMap<>();
        final Module module = null;

        final Object[] args_n = { enumTypeDefinition, qname, map, gtb, module };
        EnumBuilder result = (EnumBuilder) generate.invoke(AuxiliaryGenUtils.class, args_n);
        assertEquals(null, result);

        enumTypeDefinition = mock(EnumTypeDefinition.class);
        final Object[] args1 = { enumTypeDefinition, qname, map, gtb, module };
        result = (EnumBuilder) generate.invoke(AuxiliaryGenUtils.class, args1);
        assertEquals(null, result);
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void resolveInnerEnumFromTypeDefinitionTest() throws Exception {
        final Class[] parameterTypes =
                { EnumTypeDefinition.class, QName.class, Map.class, GeneratedTypeBuilder.class, Module.class };
        final Method generate =
                AuxiliaryGenUtils.class.getDeclaredMethod("resolveInnerEnumFromTypeDefinition", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final QName qname = QName.create("urn:enum:test", "2017-12-04", "enum-test");
        final EnumTypeDefinition enumTypeDefinition = mock(EnumTypeDefinition.class);
        final QName enumQName = QName.create(qname, "enum-qname-test");
        when(enumTypeDefinition.getQName()).thenReturn(enumQName);
        final SchemaPath schemaPath = SchemaPath.create(true, enumQName);
        when(enumTypeDefinition.getPath()).thenReturn(schemaPath);
        final GeneratedTypeBuilder gtb = new GeneratedTypeBuilderImpl("urn.enum.test.pckg", "enum-test");
        final Map<Module, ModuleContext> map = new HashMap<>();
        final Module module = mock(Module.class);
        final ModuleContext moduleContext = new ModuleContext();
        map.put(module, moduleContext);

        final Object[] args1 = { enumTypeDefinition, qname, map, gtb, module };
        final EnumBuilder result = (EnumBuilder) generate.invoke(AuxiliaryGenUtils.class, args1);
        assertNotNull(result);
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void addTOToTypeBuilderNullTest() throws Exception {
        final Class[] parameterTypes =
                { TypeDefinition.class, GeneratedTypeBuilder.class, DataSchemaNode.class, Module.class,
                        TypeProvider.class, SchemaContext.class,  Map.class};
        final Method generate = AuxiliaryGenUtils.class.getDeclaredMethod("addTOToTypeBuilder", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final BooleanTypeDefinition typeDef = mock(BooleanTypeDefinition.class);
        final GeneratedTypeBuilder typeBuilder =
                new GeneratedTypeBuilderImpl("test.boolean.type.def", "boolean-type-def");
        final DataSchemaNode leaf = mock(DataSchemaNode.class);
        final QName qnameLeaf = QName.create("urn:leaf:qname:test", "2017-12-04", "leaf-qname-test");
        when(leaf.getQName()).thenReturn(qnameLeaf);
        final Module parentModule = mock(Module.class);
        final SchemaContext schemaContext = mock(SchemaContext.class);
        final Set<Module> modules = new HashSet<>();
        when(schemaContext.getModules()).thenReturn(modules);
        final TypeProviderImpl typeProvider = new TypeProviderImpl(schemaContext);
        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        genCtx.put(parentModule, new ModuleContext());

        final Object[] args1 = { typeDef, typeBuilder, leaf, parentModule, typeProvider, schemaContext, genCtx };
        final GeneratedTOBuilder result = (GeneratedTOBuilder) generate.invoke(AuxiliaryGenUtils.class, args1);
        assertEquals(null, result);
    }

    @Test
    public void addTOToTypeBuilderUnionTest() throws Exception {
        assertNotNull(addTOToBuilder("/base/test-union.yang"));
    }

    @Test
    public void addTOToTypeBuilderBitsTest() throws Exception {
        assertNotNull(addTOToBuilder("/base/test-bits.yang"));
    }

    @SuppressWarnings({ "rawtypes" })
    private GeneratedTOBuilder addTOToBuilder(final String yangPath)
            throws NoSuchMethodException, ReactorException, FileNotFoundException, URISyntaxException,
            IllegalAccessException, InvocationTargetException {
        final Class[] parameterTypes = { TypeDefinition.class, GeneratedTypeBuilder.class, DataSchemaNode.class,
                Module.class, TypeProvider.class, SchemaContext.class, Map.class };
        final Method generate = AuxiliaryGenUtils.class.getDeclaredMethod("addTOToTypeBuilder", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final GeneratedTypeBuilder typeBuilder =
                new GeneratedTypeBuilderImpl("test.boolean.spc.def", "spec-type-def");
        final SchemaContext schemaContext = YangParserTestUtils.parseYangSource(yangPath);
        final TypeProviderImpl typeProvider = new TypeProviderImpl(schemaContext);
        final LeafSchemaNode leafSchemaNode =
                (LeafSchemaNode) schemaContext.getModules().iterator().next().getChildNodes().iterator().next();
        final TypeDefinition<? extends TypeDefinition<?>> typeDef = leafSchemaNode.getType();
        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        genCtx.put(schemaContext.getModules().iterator().next(), new ModuleContext());

        final Object[] args1 = { typeDef, typeBuilder, leafSchemaNode, schemaContext.getModules().iterator().next(),
                typeProvider, schemaContext, genCtx };
        return (GeneratedTOBuilder) generate.invoke(AuxiliaryGenUtils.class, args1);
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void createReturnTypeForUnionTest() throws Exception {
        final Class[] parameterTypes = { GeneratedTOBuilder.class, TypeDefinition.class, GeneratedTypeBuilder.class,
                Module.class, TypeProvider.class };
        final Method generate = AuxiliaryGenUtils.class.getDeclaredMethod("createReturnTypeForUnion", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final GeneratedTypeBuilder typeBuilder = new GeneratedTypeBuilderImpl("test.boolean.spc.def", "spec-type-def");
        final SchemaContext schemaContext = YangParserTestUtils.parseYangSource("/base/test-union.yang");
        final TypeProviderImpl typeProvider = new TypeProviderImpl(schemaContext);
        final LeafSchemaNode leafSchemaNode =
                (LeafSchemaNode) schemaContext.getModules().iterator().next().getChildNodes().iterator().next();
        final TypeDefinition<? extends TypeDefinition<?>> typeDef = leafSchemaNode.getType();

        final Object[] args1 = { addTOToBuilder("/base/test-union.yang"), typeDef, typeBuilder,
                schemaContext.getModules().iterator().next(), typeProvider };
        final Type result = (Type) generate.invoke(AuxiliaryGenUtils.class, args1);
        assertNotNull(result);
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void isInnerTypeTrueTest() throws Exception {
        final Class[] parameterTypes = { LeafSchemaNode.class, TypeDefinition.class };
        final Method generate = AuxiliaryGenUtils.class.getDeclaredMethod("isInnerType", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final LeafSchemaNode leaf = mock(LeafSchemaNode.class);
        final TypeDefinition type = mock(TypeDefinition.class);
        final QName qname = QName.create("namespace", "2017-12-04", "localName");
        final SchemaPath path = SchemaPath.create(true, qname);
        when(leaf.getPath()).thenReturn(path);
        when(type.getPath()).thenReturn(path);

        final Object[] args1 = { leaf, type };
        boolean result = (boolean) generate.invoke(AuxiliaryGenUtils.class, args1);
        assertNotNull(result);
        assertTrue(result);

        final QName qnameParent = QName.create(qname, "qnameParent");
        final SchemaPath parent = SchemaPath.create(true, qname, qnameParent);
        when(type.getPath()).thenReturn(parent);

        final Object[] args2 = { leaf, type };
        result = (boolean) generate.invoke(AuxiliaryGenUtils.class, args2);
        assertNotNull(result);
        assertTrue(result);
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void isInnerTypeFalseTest() throws Exception {
        final Class[] parameterTypes = { LeafSchemaNode.class, TypeDefinition.class };
        final Method generate = AuxiliaryGenUtils.class.getDeclaredMethod("isInnerType", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final LeafSchemaNode leaf = mock(LeafSchemaNode.class);
        final TypeDefinition type = mock(TypeDefinition.class);
        final QName qname = QName.create("namespace", "2017-12-04", "localName");
        final SchemaPath path = SchemaPath.create(true, qname);
        when(leaf.getPath()).thenReturn(path);

        final QName qnameChild = QName.create(qname, "qnameChild");
        final QName qnameParent = QName.create(qname, "qnameParent");
        final SchemaPath parent = SchemaPath.create(true, qnameChild, qnameParent);

        when(type.getPath()).thenReturn(parent);

        final Object[] args2 = { leaf, type };
        final boolean result = (boolean) generate.invoke(AuxiliaryGenUtils.class, args2);
        assertNotNull(result);
        assertTrue(!result);
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void resolveListKeyTOBuilderTest() throws Exception {
        final Class[] parameterTypes = { String.class, ListSchemaNode.class };
        final Method generate = AuxiliaryGenUtils.class.getDeclaredMethod("resolveListKeyTOBuilder", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final String pckgName = "pckg.name.test";
        final ListSchemaNode list = mock(ListSchemaNode.class);
        final List<QName> keyDefs = new ArrayList<>();
        final QName qname = QName.create("namespace", "2017-12-04", "localname");
        keyDefs.add(qname);
        when(list.getKeyDefinition()).thenReturn(keyDefs);
        when(list.getQName()).thenReturn(qname);

        final Object[] args1 = { pckgName, list };
        final GeneratedTOBuilder result = (GeneratedTOBuilder) generate.invoke(AuxiliaryGenUtils.class, args1);
        assertNotNull(result);
        assertEquals("LocalnameKey", result.getName());
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void resolveLeafSchemaNodeAsPropertyFalseTest() throws Exception {
        final Class[] parameterTypes = { String.class, GeneratedTOBuilder.class, LeafSchemaNode.class, Type.class, boolean.class };
        final Method generate =
                AuxiliaryGenUtils.class.getDeclaredMethod("resolveLeafSchemaNodeAsProperty", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final GeneratedTOBuilder gtob = mock(GeneratedTOBuilder.class);
        final LeafSchemaNode leaf = mock(LeafSchemaNode.class);
        final boolean isReadOnly = true;
        final Type type = null;

        final Object[] args2 = { "list", gtob, leaf, type, isReadOnly };
        final boolean result = (boolean) generate.invoke(AuxiliaryGenUtils.class, args2);
        assertNotNull(result);
        assertTrue(!result);
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void resolveLeafSchemaNodeAsPropertyTrueTest() throws Exception {
        final Class[] parameterTypes = { String.class, GeneratedTOBuilder.class, LeafSchemaNode.class, Type.class, boolean.class };
        final Method generate =
                AuxiliaryGenUtils.class.getDeclaredMethod("resolveLeafSchemaNodeAsProperty", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final GeneratedTOBuilder gtob = new GeneratedTOBuilderImpl("pckg.name.gto.tst", "gto_name");
        final LeafSchemaNode leaf = mock(LeafSchemaNode.class);
        final boolean isReadOnly = true;
        final Type type = mock(Type.class);
        when(leaf.getQName()).thenReturn(QName.create("ns", "2017-12-04", "ln"));

        final Object[] args2 = { "list", gtob, leaf, type, isReadOnly };
        final boolean result = (boolean) generate.invoke(AuxiliaryGenUtils.class, args2);
        assertNotNull(result);
        assertTrue(result);
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void checkModuleAndModuleNameTest() throws Exception {
        final Class[] parameterTypes = { Module.class };
        final Method generate = AuxiliaryGenUtils.class.getDeclaredMethod("checkModuleAndModuleName", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final Module module = mock(Module.class);
        when(module.getName()).thenReturn("moduleName");
        final Object[] args2 = { module };
        generate.invoke(AuxiliaryGenUtils.class, args2);
    }

    @SuppressWarnings("rawtypes")
    private String getterMethodName(final String schemaNodeName, final Type returnType)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final Class[] parameterTypes =
                { String.class, Type.class };
        final Method generate = AuxiliaryGenUtils.class.getDeclaredMethod("getterMethodName", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final Object[] args = { schemaNodeName, returnType };
        return (String) generate.invoke(AuxiliaryGenUtils.class, args);
    }

    @SuppressWarnings("rawtypes")
    private <T extends SchemaNode> boolean hasBuilderClass(final Class<T> clazz) throws Exception {
        final Class[] parameterTypes = { SchemaNode.class, BindingNamespaceType.class };
        final Method generate = AuxiliaryGenUtils.class.getDeclaredMethod("hasBuilderClass", parameterTypes);
        assertNotNull(generate);
        generate.setAccessible(true);

        final T schemaNode = mock(clazz);

        final Object[] args = { schemaNode, BindingNamespaceType.Data };
        return (boolean) generate.invoke(AuxiliaryGenUtils.class, args);
    }
}
