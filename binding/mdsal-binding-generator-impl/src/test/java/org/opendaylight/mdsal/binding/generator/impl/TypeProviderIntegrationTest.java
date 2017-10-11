/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Set;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.mdsal.binding.yang.types.TypeProviderImpl;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class TypeProviderIntegrationTest {
    private final String PKG = "org.opendaylight.yang.gen.v1.urn.opendaylight.test.rev131008.";
    private static SchemaContext context;
    private TypeProviderImpl provider;
    private Module m;

    @BeforeClass
    public static void setup() {
        context = YangParserTestUtils.parseYangResources(TypeProviderIntegrationTest.class, "/type-provider/test.yang",
            "/ietf/ietf-inet-types.yang");
        assertNotNull(context);
    }

    @Before
    public void init() {
        provider = new TypeProviderImpl(context);
        m = context.findModuleByName("test", QName.parseRevision("2013-10-08"));
    }

    @Test
    public void testGetTypeDefaultConstructionBinary() {
        final QName leafNode1 = QName.create(m.getQNameModule(), "leaf-binary");
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName(leafNode1);
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new byte[] {77, 97, 110}", actual);

        final QName leafNode2 = QName.create(m.getQNameModule(), "ext-binary");
        leaf = (LeafSchemaNode) m.getDataChildByName(leafNode2);
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyBinary(new byte[] {77, 97, 110})", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionBits() {
        final QName leafNode1 = QName.create(m.getQNameModule(), "leaf-bits");
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName(leafNode1);
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "TestData.LeafBits(false, false, true)", actual);

        final QName leafNode2 = QName.create(m.getQNameModule(), "ext-bits");
        leaf = (LeafSchemaNode) m.getDataChildByName(leafNode2);
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyBits(false, false, true)", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionBoolean() {
        final QName leafNode1 = QName.create(m.getQNameModule(), "leaf-boolean");
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName(leafNode1);
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("java.lang.Boolean.TRUE", actual);

        final QName leafNode2 = QName.create(m.getQNameModule(), "ext-boolean");
        leaf = (LeafSchemaNode) m.getDataChildByName(leafNode2);
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyBoolean(java.lang.Boolean.TRUE)", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionDecimal() {
        final QName leafNode1 = QName.create(m.getQNameModule(), "leaf-decimal64");
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName(leafNode1);
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new java.math.BigDecimal(\"3.14\")", actual);

        final QName leafNode2 = QName.create(m.getQNameModule(), "ext-decimal64");
        leaf = (LeafSchemaNode) m.getDataChildByName(leafNode2);
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyDecimal64(new java.math.BigDecimal(\"3.14\"))", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionEmpty() {
        final QName leafNode1 = QName.create(m.getQNameModule(), "leaf-empty");
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName(leafNode1);
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("java.lang.Boolean.FALSE", actual);

        final QName leafNode2 = QName.create(m.getQNameModule(), "ext-empty");
        leaf = (LeafSchemaNode) m.getDataChildByName(leafNode2);
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyEmpty(java.lang.Boolean.FALSE)", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionEnumeration() {
        final QName leafNode1 = QName.create(m.getQNameModule(), "leaf-enumeration");
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName(leafNode1);
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("org.opendaylight.yang.gen.v1.urn.opendaylight.test.rev131008.LeafEnumeration.Seven", actual);

        final QName leafNode2 = QName.create(m.getQNameModule(), "ext-enumeration");
        leaf = (LeafSchemaNode) m.getDataChildByName(leafNode2);
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals(PKG + "MyEnumeration.Seven", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionInt8() {
        final QName leafNode1 = QName.create(m.getQNameModule(), "leaf-int8");
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName(leafNode1);
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("java.lang.Byte.valueOf(\"11\")", actual);

        final QName leafNode2 = QName.create(m.getQNameModule(), "ext-int8");
        leaf = (LeafSchemaNode) m.getDataChildByName(leafNode2);
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyInt8(java.lang.Byte.valueOf(\"11\"))", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionInt16() {
        final QName leafNode1 = QName.create(m.getQNameModule(), "leaf-int16");
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName(leafNode1);
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("java.lang.Short.valueOf(\"111\")", actual);

        final QName leafNode2 = QName.create(m.getQNameModule(), "ext-int16");
        leaf = (LeafSchemaNode) m.getDataChildByName(leafNode2);
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyInt16(java.lang.Short.valueOf(\"111\"))", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionInt32() {
        final QName leafNode1 = QName.create(m.getQNameModule(), "leaf-int32");
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName(leafNode1);
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("java.lang.Integer.valueOf(\"1111\")", actual);

        final QName leafNode2 = QName.create(m.getQNameModule(), "ext-int32");
        leaf = (LeafSchemaNode) m.getDataChildByName(leafNode2);
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyInt32(java.lang.Integer.valueOf(\"1111\"))", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionInt64() {
        final QName leafNode1 = QName.create(m.getQNameModule(), "leaf-int64");
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName(leafNode1);
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("java.lang.Long.valueOf(\"11111\")", actual);

        final QName leafNode2 = QName.create(m.getQNameModule(), "ext-int64");
        leaf = (LeafSchemaNode) m.getDataChildByName(leafNode2);
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyInt64(java.lang.Long.valueOf(\"11111\"))", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionLeafref1() {
        final QName leafNode1 = QName.create(m.getQNameModule(), "leaf-leafref");
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName(leafNode1);
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new java.math.BigDecimal(\"1.234\")", actual);

        final QName leafNode2 = QName.create(m.getQNameModule(), "ext-leafref");
        leaf = (LeafSchemaNode) m.getDataChildByName(leafNode2);
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new java.math.BigDecimal(\"1.234\")", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionLeafref2() {
        final QName leafNode1 = QName.create(m.getQNameModule(), "leaf-leafref1");
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName(leafNode1);
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyBinary(new byte[] {77, 97, 110})", actual);

        final QName leafNode2 = QName.create(m.getQNameModule(), "ext-leafref1");
        leaf = (LeafSchemaNode) m.getDataChildByName(leafNode2);
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyBinary(new byte[] {77, 97, 110})", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionString() {
        final QName leafNode1 = QName.create(m.getQNameModule(), "leaf-string");
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName(leafNode1);
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("\"name\"", actual);

        final QName leafNode2 = QName.create(m.getQNameModule(), "ext-string");
        leaf = (LeafSchemaNode) m.getDataChildByName(leafNode2);
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyString(\"name\")", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionUint8() {
        final QName leafNode1 = QName.create(m.getQNameModule(), "leaf-uint8");
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName(leafNode1);
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("java.lang.Short.valueOf(\"11\")", actual);

        final QName leafNode2 = QName.create(m.getQNameModule(), "ext-uint8");
        leaf = (LeafSchemaNode) m.getDataChildByName(leafNode2);
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyUint8(java.lang.Short.valueOf(\"11\"))", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionUint16() {
        final QName leafNode1 = QName.create(m.getQNameModule(), "leaf-uint16");
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName(leafNode1);
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("java.lang.Integer.valueOf(\"111\")", actual);

        final QName leafNode2 = QName.create(m.getQNameModule(), "ext-uint16");
        leaf = (LeafSchemaNode) m.getDataChildByName(leafNode2);
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyUint16(java.lang.Integer.valueOf(\"111\"))", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionUint32() {
        final QName leafNode1 = QName.create(m.getQNameModule(), "leaf-uint32");
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName(leafNode1);
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("java.lang.Long.valueOf(\"1111\")", actual);

        final QName leafNode2 = QName.create(m.getQNameModule(), "ext-uint32");
        leaf = (LeafSchemaNode) m.getDataChildByName(leafNode2);
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyUint32(java.lang.Long.valueOf(\"1111\"))", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionUint64() {
        final QName leafNode1 = QName.create(m.getQNameModule(), "leaf-uint64");
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName(leafNode1);
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new java.math.BigInteger(\"11111\")", actual);

        final QName leafNode2 = QName.create(m.getQNameModule(), "ext-uint64");
        leaf = (LeafSchemaNode) m.getDataChildByName(leafNode2);
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyUint64(new java.math.BigInteger(\"11111\"))", actual);
    }

    @Test
    public void testGetTypeDefaultConstruction() {
        final QName leafNode = QName.create(m.getQNameModule(), "ip-leaf");
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName(leafNode);
        String actual = provider.getTypeDefaultConstruction(leaf);
        String exp = "new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address(\"0.0.0.1\")";
        assertEquals(exp, actual);
    }

    @Test
    public void testGetTypeDefaultConstructionUnion() {
        final QName leafNode1 = QName.create(m.getQNameModule(), "leaf-union");
        LeafSchemaNode leaf = (LeafSchemaNode) m.getDataChildByName(leafNode1);
        String actual = provider.getTypeDefaultConstruction(leaf);
        String expected = "new " + PKG + "TestData.LeafUnion(\"111\".toCharArray())";
        assertEquals(expected, actual);

        final QName leafNode2 = QName.create(m.getQNameModule(), "ext-union");
        leaf = (LeafSchemaNode) m.getDataChildByName(leafNode2);
        actual = provider.getTypeDefaultConstruction(leaf);
        expected = "new " + PKG + "MyUnion(\"111\".toCharArray())";
        assertEquals(expected, actual);
    }

    @Test
    public void testGetTypeDefaultConstructionUnionNested()  {
        final QName containerNode1 = QName.create(m.getQNameModule(), "c1");
        ContainerSchemaNode c1 = (ContainerSchemaNode) m.getDataChildByName(containerNode1);
        final QName containerNode2 = QName.create(m.getQNameModule(), "c2");
        ContainerSchemaNode c2 = (ContainerSchemaNode) c1.getDataChildByName(containerNode2);
        final QName containerNode3 = QName.create(m.getQNameModule(), "c3");
        ContainerSchemaNode c3 = (ContainerSchemaNode) c2.getDataChildByName(containerNode3);
        final QName leafNode = QName.create(m.getQNameModule(), "id");
        LeafSchemaNode leaf = (LeafSchemaNode) c3.getDataChildByName(leafNode);

        String actual = provider.getTypeDefaultConstruction(leaf);
        String expected = "new " + PKG + "NestedUnion(\"111\".toCharArray())";
        assertEquals(expected, actual);
    }

    @Test
    public void testGetParamNameFromType() {
        m = context.findModuleByName("ietf-inet-types", QName.parseRevision("2010-09-24"));
        Set<TypeDefinition<?>> types = m.getTypeDefinitions();
        TypeDefinition<?> ipv4 = null;
        TypeDefinition<?> ipv6 = null;
        TypeDefinition<?> ipv4Pref = null;
        TypeDefinition<?> ipv6Pref = null;
        for (TypeDefinition<?> type : types) {
            if ("ipv4-address".equals(type.getQName().getLocalName())) {
                ipv4 = type;
            } else if ("ipv6-address".equals(type.getQName().getLocalName())) {
                ipv6 = type;
            } else if ("ipv4-prefix".equals(type.getQName().getLocalName())) {
                ipv4Pref = type;
            } else if ("ipv6-prefix".equals(type.getQName().getLocalName())) {
                ipv6Pref = type;
            }
        }

        assertNotNull(ipv4);
        assertNotNull(ipv6);
        assertNotNull(ipv4Pref);
        assertNotNull(ipv6Pref);
        assertEquals("ipv4Address", provider.getParamNameFromType(ipv4));
        assertEquals("ipv6Address", provider.getParamNameFromType(ipv6));
        assertEquals("ipv4Prefix", provider.getParamNameFromType(ipv4Pref));
        assertEquals("ipv6Prefix", provider.getParamNameFromType(ipv6Pref));
    }
}