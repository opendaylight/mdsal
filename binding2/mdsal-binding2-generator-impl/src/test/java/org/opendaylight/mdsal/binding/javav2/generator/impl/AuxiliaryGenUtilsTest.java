/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.generator.context.ModuleContext;
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
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class AuxiliaryGenUtilsTest {

    @Test
    public void constructorTest() throws NoSuchMethodException {
        final Constructor<AuxiliaryGenUtils> constructor = AuxiliaryGenUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
        } catch (final InstantiationException | IllegalAccessException
            | InvocationTargetException | IllegalArgumentException e) {
            assertTrue(e.getCause() instanceof UnsupportedOperationException);
        }
    }

    @Test
    public void annotateDeprecatedIfNecessaryNonDepricatedTest() {
        final GeneratedTypeBuilderImpl generatedTypeBuilder =
                new GeneratedTypeBuilderImpl("test.deprecated", "Non_Deprecated", new ModuleContext());
        final Status status = Status.CURRENT;

        AuxiliaryGenUtils.annotateDeprecatedIfNecessary(status, generatedTypeBuilder);
        assertTrue(generatedTypeBuilder.toInstance().getAnnotations().isEmpty());
    }

    @Test
    public void annotateDeprecatedIfNecessaryDepricatedTest() {
        final GeneratedTypeBuilderImpl generatedTypeBuilder =
                new GeneratedTypeBuilderImpl("test.deprecated", "Deprecated", new ModuleContext());
        final Status status = Status.DEPRECATED;

        AuxiliaryGenUtils.annotateDeprecatedIfNecessary(status, generatedTypeBuilder);
        assertFalse(generatedTypeBuilder.toInstance().getAnnotations().isEmpty());
        assertEquals("Deprecated", generatedTypeBuilder.toInstance().getAnnotations().get(0).getName());
    }

    @Test
    public void hasBuilderClassFalseTest() {
        assertEquals(false, hasBuilderClass(LeafSchemaNode.class));
    }

    @Test
    public void hasBuilderClassContainerTest() {
        assertEquals(true, hasBuilderClass(ContainerSchemaNode.class));
    }

    @Test
    public void hasBuilderClassListTest() {
        assertEquals(true, hasBuilderClass(ListSchemaNode.class));
    }

    @Test
    public void hasBuilderClassRpcTest() {
        assertEquals(true, hasBuilderClass(RpcDefinition.class));
    }

    @Test
    public void hasBuilderClassNotificationTest() {
        assertEquals(true, hasBuilderClass(NotificationDefinition.class));
    }

    @Test
    public void qNameConstantTest() {
        final GeneratedTypeBuilderBase<?> gtbb = new GeneratedTypeBuilderImpl("test", "qname_constants",
            new ModuleContext());
        final String constantName = "ConstantName";
        final QName constantQName = QName.create("urn:constant", "2017-04-06", constantName);

        final Constant result = AuxiliaryGenUtils.qNameConstant(gtbb, constantName, constantQName);
        assertEquals(constantName, result.getName());
    }

    @Test
    public void constructGetterTest() {
        final GeneratedTypeBuilder gtb = new GeneratedTypeBuilderImpl("test", "Getter_of", new ModuleContext());
        final String schemaNodeName = "schema_node_getter";
        final String comment = null;
        final Type returnType = Types.STRING;
        final Status status = Status.DEPRECATED;

        final MethodSignatureBuilder result = AuxiliaryGenUtils.constructGetter(gtb, schemaNodeName, comment,
            returnType, status);
        assertEquals("getSchemaNodeGetter", result.toInstance(returnType).getName());
    }

    @Test
    public void getterMethodNameBooleanTest() throws Exception {
        assertEquals("isBooleanMethod",
            AuxiliaryGenUtils.getterMethodName("boolean_method", Types.BOOLEAN));
    }

    @Test
    public void getterMethodNameTest() throws Exception {
        assertEquals("getClazz", AuxiliaryGenUtils.getterMethodName("clazz", Types.CLASS));
    }

    @Test
    public void isNullOrEmptyIsNullTest() {
        assertTrue(AuxiliaryGenUtils.isNullOrEmpty(null));
    }

    @Test
    public void isNullOrEmptyIsEmptyTest() {
        assertTrue(AuxiliaryGenUtils.isNullOrEmpty(new ArrayList<>()));
    }

    @Test
    public void isNullOrEmptyNotNullNotEmptyTest() {
        final Collection<Object> list = new ArrayList<>();
        list.add(new Object());
        assertFalse(AuxiliaryGenUtils.isNullOrEmpty(list));
    }

    @Test
    public void augGenTypeNameTest() {
        final Map<String, GeneratedTypeBuilder> builders = new HashMap<>();
        builders.put("genTypeName1", new GeneratedTypeBuilderImpl("pckg.a1", "gen_a_1", new ModuleContext()));
        builders.put("genTypeName2", new GeneratedTypeBuilderImpl("pckg.a2", "gen_a_2", new ModuleContext()));
        final String genTypeName = "genTypeName";
        assertEquals("genTypeName3", AuxiliaryGenUtils.augGenTypeName(builders, genTypeName));
    }

    @Test
    public void getAugmentIdentifierNullTest() {
        assertNull(AuxiliaryGenUtils.getAugmentIdentifier(new ArrayList<>()));
    }

    @Test
    public void getAugmentIdentifierTest() {
        final List<UnknownSchemaNode> list = new ArrayList<>();
        final UnknownSchemaNode unknownSchemaNode = mock(UnknownSchemaNode.class);
        final QName qname =
                QName.create("urn:opendaylight:yang:extension:yang-ext", "2017-10-04", "augment-identifier");
        when(unknownSchemaNode.getNodeType()).thenReturn(qname);
        final String value = "node parameter";
        when(unknownSchemaNode.getNodeParameter()).thenReturn(value);
        list.add(unknownSchemaNode);

        assertEquals(value, AuxiliaryGenUtils.getAugmentIdentifier(list));
    }

    @Test
    public void resolveInnerEnumFromTypeDefinitionNullTest() {
        EnumTypeDefinition enumTypeDefinition = null;
        final QName qname = null;
        final GeneratedTypeBuilder gtb = null;
        final Map<Module, ModuleContext> map = new HashMap<>();
        final Module module = null;

        EnumBuilder result = AuxiliaryGenUtils.resolveInnerEnumFromTypeDefinition(enumTypeDefinition, qname, map, gtb,
            module);
        assertEquals(null, result);

        enumTypeDefinition = mock(EnumTypeDefinition.class);
        result = AuxiliaryGenUtils.resolveInnerEnumFromTypeDefinition(enumTypeDefinition, qname, map, gtb, module);
        assertEquals(null, result);
    }

    @Test
    public void resolveInnerEnumFromTypeDefinitionTest() {
        final QName qname = QName.create("urn:enum:test", "2017-12-04", "enum-test");
        final EnumTypeDefinition enumTypeDefinition = mock(EnumTypeDefinition.class);
        final QName enumQName = QName.create(qname, "enum-qname-test");
        when(enumTypeDefinition.getQName()).thenReturn(enumQName);
        final SchemaPath schemaPath = SchemaPath.create(true, enumQName);
        when(enumTypeDefinition.getPath()).thenReturn(schemaPath);
        when(enumTypeDefinition.getDescription()).thenReturn(Optional.empty());
        when(enumTypeDefinition.getReference()).thenReturn(Optional.empty());
        final GeneratedTypeBuilder gtb = new GeneratedTypeBuilderImpl("urn.enum.test.pckg", "enum-test",
            new ModuleContext());
        final Map<Module, ModuleContext> map = new HashMap<>();
        final Module module = mock(Module.class);
        final ModuleContext moduleContext = new ModuleContext();
        map.put(module, moduleContext);

        final EnumBuilder result = AuxiliaryGenUtils.resolveInnerEnumFromTypeDefinition(enumTypeDefinition, qname, map,
            gtb, module);
        assertNotNull(result);
    }

    @Test
    public void addTOToTypeBuilderNullTest() {
        final BooleanTypeDefinition typeDef = mock(BooleanTypeDefinition.class);
        final GeneratedTypeBuilder typeBuilder =
                new GeneratedTypeBuilderImpl("test.boolean.type.def", "boolean-type-def", new ModuleContext());
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

        final GeneratedTOBuilder result = AuxiliaryGenUtils.addTOToTypeBuilder(typeDef, typeBuilder, leaf, parentModule,
            typeProvider, new ModuleContext(), genCtx);
        assertEquals(null, result);
    }

    @Test
    public void addTOToTypeBuilderUnionTest() {
        assertNotNull(addTOToBuilder("/base/test-union.yang"));
    }

    @Test
    public void addTOToTypeBuilderBitsTest() {
        assertNotNull(addTOToBuilder("/base/test-bits.yang"));
    }

    private static GeneratedTOBuilder addTOToBuilder(final String yangPath) {
        final GeneratedTypeBuilder typeBuilder = new GeneratedTypeBuilderImpl("test.boolean.spc.def", "spec-type-def",
            new ModuleContext());
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResource(yangPath);
        final TypeProviderImpl typeProvider = new TypeProviderImpl(schemaContext);
        final LeafSchemaNode leafSchemaNode =
                (LeafSchemaNode) schemaContext.getModules().iterator().next().getChildNodes().iterator().next();
        final TypeDefinition<? extends TypeDefinition<?>> typeDef = leafSchemaNode.getType();
        final Map<Module, ModuleContext> genCtx = new HashMap<>();
        genCtx.put(schemaContext.getModules().iterator().next(), new ModuleContext());

        return AuxiliaryGenUtils.addTOToTypeBuilder(typeDef, typeBuilder, leafSchemaNode,
            schemaContext.getModules().iterator().next(), typeProvider, new ModuleContext(), genCtx);
    }

    @Test
    public void createReturnTypeForUnionTest() {
        final GeneratedTypeBuilder typeBuilder = new GeneratedTypeBuilderImpl("test.boolean.spc.def",
                "spec-type-def", new ModuleContext());
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResource("/base/test-union.yang");
        final TypeProviderImpl typeProvider = new TypeProviderImpl(schemaContext);
        final LeafSchemaNode leafSchemaNode =
                (LeafSchemaNode) schemaContext.getModules().iterator().next().getChildNodes().iterator().next();
        final TypeDefinition<? extends TypeDefinition<?>> typeDef = leafSchemaNode.getType();

        final Type result = AuxiliaryGenUtils.createReturnTypeForUnion(addTOToBuilder("/base/test-union.yang"), typeDef,
            typeBuilder, schemaContext.getModules().iterator().next(), typeProvider, false);
        assertNotNull(result);
    }

    @Test
    public void isInnerTypeTrueTest() {
        final LeafSchemaNode leaf = mock(LeafSchemaNode.class);
        final TypeDefinition<?> type = mock(TypeDefinition.class);
        final QName qname = QName.create("namespace", "2017-12-04", "localName");
        final SchemaPath path = SchemaPath.create(true, qname);
        when(leaf.getPath()).thenReturn(path);
        when(type.getPath()).thenReturn(path);

        assertTrue(AuxiliaryGenUtils.isInnerType(leaf, type));

        final QName qnameParent = QName.create(qname, "qnameParent");
        final SchemaPath parent = SchemaPath.create(true, qname, qnameParent);
        when(type.getPath()).thenReturn(parent);

        assertTrue(AuxiliaryGenUtils.isInnerType(leaf, type));
    }

    @Test
    public void isInnerTypeFalseTest() {
        final LeafSchemaNode leaf = mock(LeafSchemaNode.class);
        final TypeDefinition<?> type = mock(TypeDefinition.class);
        final QName qname = QName.create("namespace", "2017-12-04", "localName");
        final SchemaPath path = SchemaPath.create(true, qname);
        when(leaf.getPath()).thenReturn(path);

        final QName qnameChild = QName.create(qname, "qnameChild");
        final QName qnameParent = QName.create(qname, "qnameParent");
        final SchemaPath parent = SchemaPath.create(true, qnameChild, qnameParent);

        when(type.getPath()).thenReturn(parent);

        assertFalse(AuxiliaryGenUtils.isInnerType(leaf, type));
    }

    @Test
    public void resolveListKeyTOBuilderTest() {
        final String pckgName = "pckg.name.test";
        final ListSchemaNode list = mock(ListSchemaNode.class);
        final List<QName> keyDefs = new ArrayList<>();
        final QName qname = QName.create("namespace", "2017-12-04", "localname");
        keyDefs.add(qname);
        when(list.getKeyDefinition()).thenReturn(keyDefs);
        when(list.getQName()).thenReturn(qname);

        final GeneratedTOBuilder result = AuxiliaryGenUtils.resolveListKeyTOBuilder(pckgName, list,
            new ModuleContext());
        assertNotNull(result);
        assertEquals("LocalnameKey", result.getName());
    }

    @Test
    public void resolveLeafSchemaNodeAsPropertyFalseTest() {
        final GeneratedTOBuilder gtob = mock(GeneratedTOBuilder.class);
        final LeafSchemaNode leaf = mock(LeafSchemaNode.class);
        final boolean isReadOnly = true;
        final Type type = null;

        assertFalse(AuxiliaryGenUtils.resolveLeafSchemaNodeAsProperty("list", gtob, leaf, type, isReadOnly));
    }

    @Test
    public void resolveLeafSchemaNodeAsPropertyTrueTest() {
        final GeneratedTOBuilder gtob = new GeneratedTOBuilderImpl("pckg.name.gto.tst", "gto_name", true);
        final LeafSchemaNode leaf = mock(LeafSchemaNode.class);
        when(leaf.getDescription()).thenReturn(Optional.empty());
        when(leaf.getReference()).thenReturn(Optional.empty());

        final boolean isReadOnly = true;
        final Type type = mock(Type.class);
        when(leaf.getQName()).thenReturn(QName.create("ns", "2017-12-04", "ln"));

        assertTrue(AuxiliaryGenUtils.resolveLeafSchemaNodeAsProperty("list", gtob, leaf, type, isReadOnly));
    }

    @Test
    public void checkModuleAndModuleNameTest() {
        final Module module = mock(Module.class);
        when(module.getName()).thenReturn("moduleName");
        AuxiliaryGenUtils.checkModuleAndModuleName(module);
    }

    private static <T extends SchemaNode> boolean hasBuilderClass(final Class<T> clazz) {
        return AuxiliaryGenUtils.hasBuilderClass(mock(clazz), BindingNamespaceType.Data);
    }
}
