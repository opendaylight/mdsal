/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.yang.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

import java.net.URI;
import java.util.NoSuchElementException;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.util.generated.type.builder.GeneratedTypeBuilderImpl;
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
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class TypeProviderImplTest {

    @Test(expected = IllegalArgumentException.class)
    public void testLeafRefRelativeSelfReference() {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResource(
            "/leafref/leafref-relative-invalid.yang");
        final Module moduleRelative = schemaContext.findModules(URI.create("urn:xml:ns:yang:lrr")).iterator().next();
        final AbstractTypeProvider typeProvider = new RuntimeTypeProvider(schemaContext);

        final QName listNode = QName.create(moduleRelative.getQNameModule(), "neighbor");
        final QName leafNode = QName.create(moduleRelative.getQNameModule(), "neighbor-id");
        DataSchemaNode leafref = ((ListSchemaNode) moduleRelative.getDataChildByName(listNode))
                .getDataChildByName(leafNode);
        LeafSchemaNode leaf = (LeafSchemaNode) leafref;
        TypeDefinition<?> leafType = leaf.getType();
        typeProvider.javaTypeForSchemaDefinitionType(leafType, leaf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLeafRefAbsoluteSelfReference() {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResource(
            "/leafref/leafref-absolute-invalid.yang");
        final Module moduleRelative = schemaContext.findModules(URI.create("urn:xml:ns:yang:lra")).iterator().next();
        final AbstractTypeProvider typeProvider = new RuntimeTypeProvider(schemaContext);

        final QName listNode = QName.create(moduleRelative.getQNameModule(), "neighbor");
        final QName leafNode = QName.create(moduleRelative.getQNameModule(), "neighbor-id");
        DataSchemaNode leafref = ((ListSchemaNode) moduleRelative.getDataChildByName(listNode))
                .getDataChildByName(leafNode);
        LeafSchemaNode leaf = (LeafSchemaNode) leafref;
        TypeDefinition<?> leafType = leaf.getType();
        Type leafrefResolvedType = typeProvider.javaTypeForSchemaDefinitionType(leafType, leaf);
        assertNotNull(leafrefResolvedType);
    }

    @Test
    public void testLeafRefRelativeAndAbsoluteValidReference() {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResource("/leafref/leafref-valid.yang");
        final Module moduleValid = schemaContext.findModules(URI.create("urn:xml:ns:yang:lrv")).iterator().next();
        final AbstractTypeProvider typeProvider = new RuntimeTypeProvider(schemaContext);

        final QName listNode = QName.create(moduleValid.getQNameModule(), "neighbor");
        final QName leaf1Node = QName.create(moduleValid.getQNameModule(), "neighbor-id");
        DataSchemaNode leafrefRel = ((ListSchemaNode) moduleValid.getDataChildByName(listNode))
                .getDataChildByName(leaf1Node);
        LeafSchemaNode leafRel = (LeafSchemaNode) leafrefRel;
        TypeDefinition<?> leafTypeRel = leafRel.getType();
        Type leafrefRelResolvedType = typeProvider.javaTypeForSchemaDefinitionType(leafTypeRel, leafRel);
        assertNotNull(leafrefRelResolvedType);

        final QName leaf2Node = QName.create(moduleValid.getQNameModule(), "neighbor2-id");
        DataSchemaNode leafrefAbs = ((ListSchemaNode) moduleValid.getDataChildByName(listNode))
                .getDataChildByName(leaf2Node);
        LeafSchemaNode leafAbs = (LeafSchemaNode) leafrefAbs;
        TypeDefinition<?> leafTypeAbs = leafAbs.getType();
        Type leafrefAbsResolvedType = typeProvider.javaTypeForSchemaDefinitionType(leafTypeAbs, leafAbs);
        assertNotNull(leafrefAbsResolvedType);
    }

    @Test
    public void testMethodsOfTypeProviderImpl() {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResource("/base-yang-types.yang");

        final AbstractTypeProvider typeProvider = new RuntimeTypeProvider(schemaContext);

        final SchemaPath refTypePath = SchemaPath.create(true, QName.create("", "cont1"), QName.create("", "list1"));
        final GeneratedTypeBuilderImpl refType = new GeneratedTypeBuilderImpl("org.opendaylight.yangtools.test",
            "TestType");
        typeProvider.putReferencedType(refTypePath, refType);
        final StringTypeDefinition stringType = BaseTypes.stringType();

        final LeafSchemaNode leafSchemaNode = mock(LeafSchemaNode.class);
        doReturn(stringType).when(leafSchemaNode).getType();
        doReturn(SchemaPath.ROOT).when(leafSchemaNode).getPath();
        doReturn(QName.create("", "Cont1")).when(leafSchemaNode).getQName();

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
        doReturn(QName.create("", "Cont1")).when(leafSchemaNode).getQName();

        assertEquals("new byte[] {-45}", typeProvider.getTypeDefaultConstruction(leafSchemaNode, "01"));

        // boolean type
        final BooleanTypeDefinition booleanType = BaseTypes.booleanType();

        reset(leafSchemaNode);
        doReturn(booleanType).when(leafSchemaNode).getType();
        doReturn(SchemaPath.ROOT).when(leafSchemaNode).getPath();
        doReturn(QName.create("", "Cont1")).when(leafSchemaNode).getQName();

        assertEquals("java.lang.Boolean.FALSE", typeProvider.getTypeDefaultConstruction(leafSchemaNode, "false"));

        // decimal type
        final DecimalTypeDefinition decimalType = BaseTypes.decimalTypeBuilder(refTypePath).setFractionDigits(4)
                .build();

        reset(leafSchemaNode);
        doReturn(decimalType).when(leafSchemaNode).getType();
        doReturn(SchemaPath.ROOT).when(leafSchemaNode).getPath();
        doReturn(QName.create("", "Cont1")).when(leafSchemaNode).getQName();

        assertEquals("new java.math.BigDecimal(\"111\")", typeProvider.getTypeDefaultConstruction(leafSchemaNode,
            "111"));

        // empty type
        final EmptyTypeDefinition emptyType = BaseTypes.emptyType();

        reset(leafSchemaNode);
        doReturn(emptyType).when(leafSchemaNode).getType();
        doReturn(SchemaPath.ROOT).when(leafSchemaNode).getPath();
        doReturn(QName.create("", "Cont1")).when(leafSchemaNode).getQName();

        assertEquals("java.lang.Boolean.valueOf(\"default value\")", typeProvider.getTypeDefaultConstruction(
            leafSchemaNode, "default value"));

        // enum type
        final EnumTypeDefinition enumType =  BaseTypes.enumerationTypeBuilder(refTypePath).build();

        reset(leafSchemaNode);
        doReturn(enumType).when(leafSchemaNode).getType();
        doReturn(SchemaPath.ROOT).when(leafSchemaNode).getPath();
        doReturn(QName.create("", "Cont1")).when(leafSchemaNode).getQName();

        try {
            assertEquals("\"default value\"", typeProvider.getTypeDefaultConstruction(leafSchemaNode, "default value"));
            fail("Expected NoSuchElementException");
        } catch (Exception e) {
            assertTrue(e instanceof NoSuchElementException);
        }

        // identityref type
        final IdentitySchemaNode identitySchemaNode = mock(IdentitySchemaNode.class);
        final IdentityrefTypeBuilder identityRefBuilder = BaseTypes.identityrefTypeBuilder(refTypePath);
        identityRefBuilder.addIdentity(identitySchemaNode);
        final IdentityrefTypeDefinition identityRef =  identityRefBuilder.build();

        reset(leafSchemaNode);
        doReturn(identityRef).when(leafSchemaNode).getType();
        doReturn(SchemaPath.ROOT).when(leafSchemaNode).getPath();
        doReturn(QName.create("", "Cont1")).when(leafSchemaNode).getQName();

        try {
            assertEquals("\"default value\"", typeProvider.getTypeDefaultConstruction(leafSchemaNode, "default value"));
            fail("Expected UnsupportedOperationException");
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
            assertEquals("Cannot get default construction for identityref type", e.getMessage());
        }
    }
}
