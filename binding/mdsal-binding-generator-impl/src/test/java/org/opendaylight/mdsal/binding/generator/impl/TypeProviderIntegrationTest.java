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

import com.google.common.collect.ImmutableMap;
import java.util.Set;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.mdsal.binding.yang.types.AbstractTypeProvider;
import org.opendaylight.mdsal.binding.yang.types.CodegenTypeProvider;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class TypeProviderIntegrationTest {
    private static final String PKG = "org.opendaylight.yang.gen.v1.urn.opendaylight.test.rev131008.";
    private static SchemaContext CONTEXT;
    private AbstractTypeProvider provider;
    private Module module;

    @BeforeClass
    public static void setup() {
        CONTEXT = YangParserTestUtils.parseYangResources(TypeProviderIntegrationTest.class, "/type-provider/test.yang",
            "/ietf/ietf-inet-types.yang");
        assertNotNull(CONTEXT);
    }

    @AfterClass
    public static void teardown() {
        CONTEXT = null;
    }

    @Before
    public void init() {
        provider = new CodegenTypeProvider(CONTEXT, ImmutableMap.of());
        module = CONTEXT.findModule("test", Revision.of("2013-10-08")).get();
    }

    @Test
    public void testGetTypeDefaultConstructionBinary() {
        final QName leafNode1 = QName.create(module.getQNameModule(), "leaf-binary");
        LeafSchemaNode leaf = (LeafSchemaNode) module.findDataChildByName(leafNode1).get();
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new byte[] {77, 97, 110}", actual);

        final QName leafNode2 = QName.create(module.getQNameModule(), "ext-binary");
        leaf = (LeafSchemaNode) module.findDataChildByName(leafNode2).get();
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyBinary(new byte[] {77, 97, 110})", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionBits() {
        final QName leafNode1 = QName.create(module.getQNameModule(), "leaf-bits");
        LeafSchemaNode leaf = (LeafSchemaNode) module.findDataChildByName(leafNode1).get();
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "TestData.LeafBits(false, false, true)", actual);

        final QName leafNode2 = QName.create(module.getQNameModule(), "ext-bits");
        leaf = (LeafSchemaNode) module.findDataChildByName(leafNode2).get();
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyBits(false, false, true)", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionBoolean() {
        final QName leafNode1 = QName.create(module.getQNameModule(), "leaf-boolean");
        LeafSchemaNode leaf = (LeafSchemaNode) module.findDataChildByName(leafNode1).get();
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("java.lang.Boolean.TRUE", actual);

        final QName leafNode2 = QName.create(module.getQNameModule(), "ext-boolean");
        leaf = (LeafSchemaNode) module.findDataChildByName(leafNode2).get();
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyBoolean(java.lang.Boolean.TRUE)", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionDecimal() {
        final QName leafNode1 = QName.create(module.getQNameModule(), "leaf-decimal64");
        LeafSchemaNode leaf = (LeafSchemaNode) module.findDataChildByName(leafNode1).get();
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new java.math.BigDecimal(\"3.14\")", actual);

        final QName leafNode2 = QName.create(module.getQNameModule(), "ext-decimal64");
        leaf = (LeafSchemaNode) module.findDataChildByName(leafNode2).get();
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyDecimal64(new java.math.BigDecimal(\"3.14\"))", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionEmpty() {
        final QName leafNode1 = QName.create(module.getQNameModule(), "leaf-empty");
        LeafSchemaNode leaf = (LeafSchemaNode) module.findDataChildByName(leafNode1).get();
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("java.lang.Boolean.FALSE", actual);

        final QName leafNode2 = QName.create(module.getQNameModule(), "ext-empty");
        leaf = (LeafSchemaNode) module.findDataChildByName(leafNode2).get();
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyEmpty(java.lang.Boolean.FALSE)", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionEnumeration() {
        final QName leafNode1 = QName.create(module.getQNameModule(), "leaf-enumeration");
        LeafSchemaNode leaf = (LeafSchemaNode) module.findDataChildByName(leafNode1).get();
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("org.opendaylight.yang.gen.v1.urn.opendaylight.test.rev131008.LeafEnumeration.Seven", actual);

        final QName leafNode2 = QName.create(module.getQNameModule(), "ext-enumeration");
        leaf = (LeafSchemaNode) module.findDataChildByName(leafNode2).get();
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals(PKG + "MyEnumeration.Seven", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionInt8() {
        final QName leafNode1 = QName.create(module.getQNameModule(), "leaf-int8");
        LeafSchemaNode leaf = (LeafSchemaNode) module.findDataChildByName(leafNode1).get();
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("java.lang.Byte.valueOf(\"11\")", actual);

        final QName leafNode2 = QName.create(module.getQNameModule(), "ext-int8");
        leaf = (LeafSchemaNode) module.findDataChildByName(leafNode2).get();
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyInt8(java.lang.Byte.valueOf(\"11\"))", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionInt16() {
        final QName leafNode1 = QName.create(module.getQNameModule(), "leaf-int16");
        LeafSchemaNode leaf = (LeafSchemaNode) module.findDataChildByName(leafNode1).get();
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("java.lang.Short.valueOf(\"111\")", actual);

        final QName leafNode2 = QName.create(module.getQNameModule(), "ext-int16");
        leaf = (LeafSchemaNode) module.findDataChildByName(leafNode2).get();
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyInt16(java.lang.Short.valueOf(\"111\"))", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionInt32() {
        final QName leafNode1 = QName.create(module.getQNameModule(), "leaf-int32");
        LeafSchemaNode leaf = (LeafSchemaNode) module.findDataChildByName(leafNode1).get();
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("java.lang.Integer.valueOf(\"1111\")", actual);

        final QName leafNode2 = QName.create(module.getQNameModule(), "ext-int32");
        leaf = (LeafSchemaNode) module.findDataChildByName(leafNode2).get();
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyInt32(java.lang.Integer.valueOf(\"1111\"))", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionInt64() {
        final QName leafNode1 = QName.create(module.getQNameModule(), "leaf-int64");
        LeafSchemaNode leaf = (LeafSchemaNode) module.findDataChildByName(leafNode1).get();
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("java.lang.Long.valueOf(\"11111\")", actual);

        final QName leafNode2 = QName.create(module.getQNameModule(), "ext-int64");
        leaf = (LeafSchemaNode) module.findDataChildByName(leafNode2).get();
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyInt64(java.lang.Long.valueOf(\"11111\"))", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionLeafref1() {
        final QName leafNode1 = QName.create(module.getQNameModule(), "leaf-leafref");
        LeafSchemaNode leaf = (LeafSchemaNode) module.findDataChildByName(leafNode1).get();
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new java.math.BigDecimal(\"1.234\")", actual);

        final QName leafNode2 = QName.create(module.getQNameModule(), "ext-leafref");
        leaf = (LeafSchemaNode) module.findDataChildByName(leafNode2).get();
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new java.math.BigDecimal(\"1.234\")", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionLeafref2() {
        final QName leafNode1 = QName.create(module.getQNameModule(), "leaf-leafref1");
        LeafSchemaNode leaf = (LeafSchemaNode) module.findDataChildByName(leafNode1).get();
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyBinary(new byte[] {77, 97, 110})", actual);

        final QName leafNode2 = QName.create(module.getQNameModule(), "ext-leafref1");
        leaf = (LeafSchemaNode) module.findDataChildByName(leafNode2).get();
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyBinary(new byte[] {77, 97, 110})", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionString() {
        final QName leafNode1 = QName.create(module.getQNameModule(), "leaf-string");
        LeafSchemaNode leaf = (LeafSchemaNode) module.findDataChildByName(leafNode1).get();
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("\"name\"", actual);

        final QName leafNode2 = QName.create(module.getQNameModule(), "ext-string");
        leaf = (LeafSchemaNode) module.findDataChildByName(leafNode2).get();
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyString(\"name\")", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionUint8() {
        final QName leafNode1 = QName.create(module.getQNameModule(), "leaf-uint8");
        LeafSchemaNode leaf = (LeafSchemaNode) module.findDataChildByName(leafNode1).get();
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("org.opendaylight.yangtools.yang.common.Uint8.valueOf(\"11\")", actual);

        final QName leafNode2 = QName.create(module.getQNameModule(), "ext-uint8");
        leaf = (LeafSchemaNode) module.findDataChildByName(leafNode2).get();
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyUint8(org.opendaylight.yangtools.yang.common.Uint8.valueOf(\"11\"))", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionUint16() {
        final QName leafNode1 = QName.create(module.getQNameModule(), "leaf-uint16");
        LeafSchemaNode leaf = (LeafSchemaNode) module.findDataChildByName(leafNode1).get();
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("org.opendaylight.yangtools.yang.common.Uint16.valueOf(\"111\")", actual);

        final QName leafNode2 = QName.create(module.getQNameModule(), "ext-uint16");
        leaf = (LeafSchemaNode) module.findDataChildByName(leafNode2).get();
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyUint16(org.opendaylight.yangtools.yang.common.Uint16.valueOf(\"111\"))", actual);
    }

    @Test
    public void testGetTypeDefaultConstructionUint32() {
        final QName leafNode1 = QName.create(module.getQNameModule(), "leaf-uint32");
        LeafSchemaNode leaf = (LeafSchemaNode) module.findDataChildByName(leafNode1).get();
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("org.opendaylight.yangtools.yang.common.Uint32.valueOf(\"1111\")", actual);

        final QName leafNode2 = QName.create(module.getQNameModule(), "ext-uint32");
        leaf = (LeafSchemaNode) module.findDataChildByName(leafNode2).get();
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyUint32(org.opendaylight.yangtools.yang.common.Uint32.valueOf(\"1111\"))",
            actual);
    }

    @Test
    public void testGetTypeDefaultConstructionUint64() {
        final QName leafNode1 = QName.create(module.getQNameModule(), "leaf-uint64");
        LeafSchemaNode leaf = (LeafSchemaNode) module.findDataChildByName(leafNode1).get();
        String actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("org.opendaylight.yangtools.yang.common.Uint64.valueOf(\"11111\")", actual);

        final QName leafNode2 = QName.create(module.getQNameModule(), "ext-uint64");
        leaf = (LeafSchemaNode) module.findDataChildByName(leafNode2).get();
        actual = provider.getTypeDefaultConstruction(leaf);
        assertEquals("new " + PKG + "MyUint64(org.opendaylight.yangtools.yang.common.Uint64.valueOf(\"11111\"))",
                actual);
    }

    @Test
    public void testGetTypeDefaultConstruction() {
        final QName leafNode = QName.create(module.getQNameModule(), "ip-leaf");
        LeafSchemaNode leaf = (LeafSchemaNode) module.findDataChildByName(leafNode).get();
        String actual = provider.getTypeDefaultConstruction(leaf);
        String exp = "new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924."
                + "Ipv4Address(\"0.0.0.1\")";
        assertEquals(exp, actual);
    }

    @Test
    public void testGetTypeDefaultConstructionUnion() {
        final QName leafNode1 = QName.create(module.getQNameModule(), "leaf-union");
        LeafSchemaNode leaf = (LeafSchemaNode) module.findDataChildByName(leafNode1).get();
        String actual = provider.getTypeDefaultConstruction(leaf);
        String expected = "new " + PKG + "TestData.LeafUnion(\"111\".toCharArray())";
        assertEquals(expected, actual);

        final QName leafNode2 = QName.create(module.getQNameModule(), "ext-union");
        leaf = (LeafSchemaNode) module.findDataChildByName(leafNode2).get();
        actual = provider.getTypeDefaultConstruction(leaf);
        expected = "new " + PKG + "MyUnion(\"111\".toCharArray())";
        assertEquals(expected, actual);
    }

    @Test
    public void testGetTypeDefaultConstructionUnionNested()  {
        final QName containerNode1 = QName.create(module.getQNameModule(), "c1");
        ContainerSchemaNode c1 = (ContainerSchemaNode) module.findDataChildByName(containerNode1).get();
        final QName containerNode2 = QName.create(module.getQNameModule(), "c2");
        ContainerSchemaNode c2 = (ContainerSchemaNode) c1.findDataChildByName(containerNode2).get();
        final QName containerNode3 = QName.create(module.getQNameModule(), "c3");
        ContainerSchemaNode c3 = (ContainerSchemaNode) c2.findDataChildByName(containerNode3).get();
        final QName leafNode = QName.create(module.getQNameModule(), "id");
        LeafSchemaNode leaf = (LeafSchemaNode) c3.findDataChildByName(leafNode).get();

        String actual = provider.getTypeDefaultConstruction(leaf);
        String expected = "new " + PKG + "NestedUnion(\"111\".toCharArray())";
        assertEquals(expected, actual);
    }

    @Test
    public void testGetParamNameFromType() {
        module = CONTEXT.findModule("ietf-inet-types", Revision.of("2010-09-24")).get();
        Set<TypeDefinition<?>> types = module.getTypeDefinitions();
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
