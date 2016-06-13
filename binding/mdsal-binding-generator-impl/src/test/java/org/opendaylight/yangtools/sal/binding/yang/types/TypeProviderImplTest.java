/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.yang.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

import java.io.File;
import java.net.URI;
import java.util.NoSuchElementException;
import org.junit.Test;
import org.opendaylight.yangtools.binding.generator.util.generated.type.builder.GeneratedTypeBuilderImpl;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.type.IdentityrefTypeBuilder;
import org.opendaylight.yangtools.yang.parser.util.YangValidationException;

public class TypeProviderImplTest {

    @Test (expected = YangValidationException.class)
    public void testLeafRefRelativeSelfReference() throws Exception {
        File relative = new File(getClass().getResource("/leafref/leafref-relative-invalid.yang").toURI());

        final SchemaContext schemaContext = TestUtils.parseYangSources(relative);
        final Module moduleRelative = schemaContext.findModuleByNamespace(new URI("urn:xml:ns:yang:lrr")).iterator().next();
        final TypeProviderImpl typeProvider = new TypeProviderImpl(schemaContext);

        DataSchemaNode leafref = ((ListSchemaNode) moduleRelative.getDataChildByName("neighbor")).getDataChildByName("neighbor-id");
        LeafSchemaNode leaf = (LeafSchemaNode) leafref;
        TypeDefinition<?> leafType = leaf.getType();
        Type leafrefResolvedType = typeProvider.javaTypeForSchemaDefinitionType(leafType, leaf);
    }

    @Test (expected = YangValidationException.class)
    public void testLeafRefAbsoluteSelfReference() throws Exception {
        File relative = new File(getClass().getResource("/leafref/leafref-absolute-invalid.yang").toURI());

        final SchemaContext schemaContext = TestUtils.parseYangSources(relative);
        final Module moduleRelative = schemaContext.findModuleByNamespace(new URI("urn:xml:ns:yang:lra")).iterator().next();
        final TypeProviderImpl typeProvider = new TypeProviderImpl(schemaContext);

        DataSchemaNode leafref = ((ListSchemaNode) moduleRelative.getDataChildByName("neighbor")).getDataChildByName("neighbor-id");
        LeafSchemaNode leaf = (LeafSchemaNode) leafref;
        TypeDefinition<?> leafType = leaf.getType();
        Type leafrefResolvedType = typeProvider.javaTypeForSchemaDefinitionType(leafType, leaf);
    }

    @Test
    public void testLeafRefRelativeAndAbsoluteValidReference() throws Exception {
        File valid = new File(getClass().getResource("/leafref/leafref-valid.yang").toURI());

        final SchemaContext schemaContext = TestUtils.parseYangSources(valid);
        final Module moduleValid = schemaContext.findModuleByNamespace(new URI("urn:xml:ns:yang:lrv")).iterator().next();
        final TypeProviderImpl typeProvider = new TypeProviderImpl(schemaContext);

        DataSchemaNode leafrefRel = ((ListSchemaNode) moduleValid.getDataChildByName("neighbor")).getDataChildByName
                ("neighbor-id");
        LeafSchemaNode leafRel = (LeafSchemaNode) leafrefRel;
        TypeDefinition<?> leafTypeRel = leafRel.getType();
        Type leafrefRelResolvedType = typeProvider.javaTypeForSchemaDefinitionType(leafTypeRel, leafRel);
        assertNotNull(leafrefRelResolvedType);

        DataSchemaNode leafrefAbs = ((ListSchemaNode) moduleValid.getDataChildByName("neighbor")).getDataChildByName
                ("neighbor2-id");
        LeafSchemaNode leafAbs = (LeafSchemaNode) leafrefAbs;
        TypeDefinition<?> leafTypeAbs = leafAbs.getType();
        Type leafrefAbsResolvedType = typeProvider.javaTypeForSchemaDefinitionType(leafTypeAbs, leafAbs);
        assertNotNull(leafrefAbsResolvedType);
    }

    @Test
    public void testMethodsOfTypeProviderImpl() throws Exception {
        final File abstractTopology = new File(getClass().getResource("/base-yang-types.yang")
                .toURI());

        final SchemaContext schemaContext = TestUtils.parseYangSources(abstractTopology);

        final TypeProviderImpl typeProvider = new TypeProviderImpl(schemaContext);

        final SchemaPath refTypePath = SchemaPath.create(true, QName.create("cont1"), QName.create("list1"));
        final GeneratedTypeBuilderImpl refType = new GeneratedTypeBuilderImpl("org.opendaylight.yangtools.test", "TestType");
        typeProvider.putReferencedType(refTypePath, refType);
        final StringTypeDefinition stringType = BaseTypes.stringType();

        final LeafSchemaNode leafSchemaNode = mock(LeafSchemaNode.class);
        doReturn(stringType).when(leafSchemaNode).getType();
        doReturn(SchemaPath.ROOT).when(leafSchemaNode).getPath();
        doReturn(QName.create("Cont1")).when(leafSchemaNode).getQName();

        // test constructor
        assertNotNull(typeProvider);

        // test getAdditionalTypes() method
        assertFalse(typeProvider.getAdditionalTypes().isEmpty());

        // test getConstructorPropertyName() method
        assertTrue(typeProvider.getConstructorPropertyName(null).isEmpty());
        assertEquals("value", typeProvider.getConstructorPropertyName(stringType));

        // test getParamNameFromType() method
        assertEquals("string", typeProvider.getParamNameFromType(stringType));

        // test getTypeDefaultConstruction() method for string type
        assertEquals("\"default value\"", typeProvider.getTypeDefaultConstruction(leafSchemaNode, "default value"));

        // binary type
        final BinaryTypeDefinition binaryType = BaseTypes.binaryType();

        reset(leafSchemaNode);
        doReturn(binaryType).when(leafSchemaNode).getType();
        doReturn(SchemaPath.ROOT).when(leafSchemaNode).getPath();
        doReturn(QName.create("Cont1")).when(leafSchemaNode).getQName();

        assertEquals("new byte[] {-45}", typeProvider.getTypeDefaultConstruction(leafSchemaNode, "01"));

        // boolean type
        final BooleanTypeDefinition booleanType = BaseTypes.booleanType();

        reset(leafSchemaNode);
        doReturn(booleanType).when(leafSchemaNode).getType();
        doReturn(SchemaPath.ROOT).when(leafSchemaNode).getPath();
        doReturn(QName.create("Cont1")).when(leafSchemaNode).getQName();

        assertEquals("new java.lang.Boolean(\"false\")", typeProvider.getTypeDefaultConstruction(leafSchemaNode, "false"));

        // decimal type
        final DecimalTypeDefinition decimalType = BaseTypes.decimalTypeBuilder(refTypePath).setFractionDigits(4).build();

        reset(leafSchemaNode);
        doReturn(decimalType).when(leafSchemaNode).getType();
        doReturn(SchemaPath.ROOT).when(leafSchemaNode).getPath();
        doReturn(QName.create("Cont1")).when(leafSchemaNode).getQName();

        assertEquals("new java.math.BigDecimal(\"111\")", typeProvider.getTypeDefaultConstruction(leafSchemaNode, "111"));

        // empty type
        final EmptyTypeDefinition emptyType = BaseTypes.emptyType();

        reset(leafSchemaNode);
        doReturn(emptyType).when(leafSchemaNode).getType();
        doReturn(SchemaPath.ROOT).when(leafSchemaNode).getPath();
        doReturn(QName.create("Cont1")).when(leafSchemaNode).getQName();

        assertEquals("new java.lang.Boolean(\"default value\")", typeProvider.getTypeDefaultConstruction(leafSchemaNode, "default value"));

        // enum type
        final EnumTypeDefinition enumType =  BaseTypes.enumerationTypeBuilder(refTypePath).build();

        reset(leafSchemaNode);
        doReturn(enumType).when(leafSchemaNode).getType();
        doReturn(SchemaPath.ROOT).when(leafSchemaNode).getPath();
        doReturn(QName.create("Cont1")).when(leafSchemaNode).getQName();

        try {
            assertEquals("\"default value\"", typeProvider.getTypeDefaultConstruction(leafSchemaNode, "default value"));
            fail("Expected NoSuchElementException");
        } catch (Exception e) {
            assertTrue( e instanceof NoSuchElementException);
        }

        // identityref type
        final IdentitySchemaNode identitySchemaNode = mock(IdentitySchemaNode.class);
        final IdentityrefTypeBuilder identityRefBuilder = BaseTypes.identityrefTypeBuilder(refTypePath);
        identityRefBuilder.setIdentity(identitySchemaNode);
        final IdentityrefTypeDefinition identityRef =  identityRefBuilder.build();

        reset(leafSchemaNode);
        doReturn(identityRef).when(leafSchemaNode).getType();
        doReturn(SchemaPath.ROOT).when(leafSchemaNode).getPath();
        doReturn(QName.create("Cont1")).when(leafSchemaNode).getQName();

        try {
            assertEquals("\"default value\"", typeProvider.getTypeDefaultConstruction(leafSchemaNode, "default value"));
            fail("Expected UnsupportedOperationException");
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
            assertEquals("Cannot get default construction for identityref type", e.getMessage());
        }
    }
}
