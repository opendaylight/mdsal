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

import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.generator.api.BindingGenerator;
import org.opendaylight.mdsal.binding.javav2.model.api.Enumeration;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.javav2.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class BindingGeneratorImplTest {

    @Test
    public void genTypesTypeDefTest() throws Exception {
        final BindingGeneratorImpl bg = new BindingGeneratorImpl(false);
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResource("/generator/test-typedef.yang");
        final List<Type> generateTypes = bg.generateTypes(schemaContext);
        assertNotNull(generateTypes);
        for (final Type genType : generateTypes) {
            if (genType.getName().equals("MyInnerCont") && genType.getPackageName()
                    .equals("org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.rev170206.data.asteriskmy_cont")) {
                final GeneratedType gt = (GeneratedType) genType;
                for (final MethodSignature methodSignature : gt.getMethodDefinitions()) {
                    if (methodSignature.getName().equals("getMyLeaf2")) {
                        assertEquals(methodSignature.getReturnType().getName(), "MyType");
                    }
                }
            }
        }
    }

    @Test
    public void generatedTypesEnumTest() throws Exception {
        final BindingGenerator bg = new BindingGeneratorImpl(false);
        final SchemaContext context = YangParserTestUtils.parseYangResource("/generator/apple-test.yang");
        final List<Type> generateTypes = bg.generateTypes(context);
        assertNotNull(generateTypes);
        assertTrue(!generateTypes.isEmpty());
        for (final Type type : generateTypes) {
            if (type.getName().equals("Apple") && type.getPackageName()
                    .equals("org.opendaylight.mdsal.gen.javav2.urn.test.simple.apple.rev170503.data")) {
                final GeneratedType gt = (GeneratedType) type;
                final Enumeration enumeration = gt.getEnumerations().get(0);
                assertEquals("Apple1", enumeration.getName());
                assertEquals("org.opendaylight.mdsal.gen.javav2.urn.test.simple.apple.rev170503.data.Apple",
                        enumeration.getPackageName());
                assertEquals("apple", enumeration.getValues().get(0).getName());
                assertEquals("APPLE", enumeration.getValues().get(0).getMappedName());
                assertEquals("apple1", enumeration.getValues().get(1).getName());
                assertEquals("APPLE1", enumeration.getValues().get(1).getMappedName());
            }
        }
    }

    @Test
    public void generatedTypesUsesEnumLeafTest() {
        final BindingGenerator bg = new BindingGeneratorImpl(false);
        final SchemaContext context = YangParserTestUtils.parseYangResources(BindingGeneratorImplTest.class,
            "/uses-statement/test-uses-leaf-innertype-base.yang", "/uses-statement/test-uses-leaf-innertype.yang");
        final List<Type> generateTypes = bg.generateTypes(context);
        assertNotNull(generateTypes);
        assertTrue(!generateTypes.isEmpty());
        for (final Type type : generateTypes) {
            if (type.getName().equals("MyCont") && type.getPackageName()
                    .equals("org.opendaylight.mdsal.gen.javav2.urn.test.uses.leaf.innertype.base.rev170809.data")) {
                final GeneratedType gt = (GeneratedType) type;
                final MethodSignature methodSignature = gt.getMethodDefinitions().get(0);
                assertEquals("ErrorType", methodSignature.getReturnType().getName());
            }

            if (type.getName().equals("MyCont") && type.getPackageName()
                    .equals("org.opendaylight.mdsal.gen.javav2.urn.test.uses.leaf.innertype.rev170809.data")) {
                final GeneratedType gt = (GeneratedType) type;
                final MethodSignature methodSignature = gt.getMethodDefinitions().get(0);
                assertEquals("ErrorType", methodSignature.getReturnType().getName());
            }
        }
    }

    @Test
    public void generatedTypesUsesBitsLeafTest() {
        final BindingGenerator bg = new BindingGeneratorImpl(false);
        final SchemaContext context = YangParserTestUtils.parseYangResources(BindingGeneratorImplTest.class,
            "/uses-statement/test-uses-leaf-innertype2-base.yang", "/uses-statement/test-uses-leaf-innertype2.yang");
        final List<Type> generateTypes = bg.generateTypes(context);
        assertNotNull(generateTypes);
        assertTrue(!generateTypes.isEmpty());
        for (final Type type : generateTypes) {
            if (type.getName().equals("MyCont") && type.getPackageName()
                    .equals("org.opendaylight.mdsal.gen.javav2.urn.test.uses.leaf.innertype2.base.rev170809.data")) {
                final GeneratedType gt = (GeneratedType) type;
                for (MethodSignature methodSignature : gt.getMethodDefinitions()) {
                    if (methodSignature.getName().equals("getLeafBits")) {
                        assertEquals("LeafBits", methodSignature.getReturnType().getName());
                    }
                }

            }

            if (type.getName().equals("MyCont") && type.getPackageName()
                    .equals("org.opendaylight.mdsal.gen.javav2.urn.test.uses.leaf.innertype2.rev170809.data")) {
                final GeneratedType gt = (GeneratedType) type;
                for (MethodSignature methodSignature : gt.getMethodDefinitions()) {
                    if (methodSignature.getName().equals("getLeafBits")) {
                        assertEquals("LeafBits", methodSignature.getReturnType().getName());
                    }
                }
            }
        }
    }

    @Test
    public void generatedTypesUsesUnionLeafTest() {
        final BindingGenerator bg = new BindingGeneratorImpl(false);
        final SchemaContext context = YangParserTestUtils.parseYangResources(BindingGeneratorImplTest.class,
            "/uses-statement/test-uses-leaf-innertype2-base.yang", "/uses-statement/test-uses-leaf-innertype2.yang");
        final List<Type> generateTypes = bg.generateTypes(context);
        assertNotNull(generateTypes);
        assertTrue(!generateTypes.isEmpty());
        for (final Type type : generateTypes) {
            if (type.getName().equals("MyCont") && type.getPackageName()
                    .equals("org.opendaylight.mdsal.gen.javav2.urn.test.uses.leaf.innertype2.base.rev170809.data")) {
                final GeneratedType gt = (GeneratedType) type;
                for (MethodSignature methodSignature : gt.getMethodDefinitions()) {
                    if (methodSignature.getName().equals("getLeafUnion")) {
                        assertEquals("LeafUnion", methodSignature.getReturnType().getName());
                    }
                }

            }

            if (type.getName().equals("MyCont") && type.getPackageName()
                    .equals("org.opendaylight.mdsal.gen.javav2.urn.test.uses.leaf.innertype2.rev170809.data")) {
                final GeneratedType gt = (GeneratedType) type;
                for (MethodSignature methodSignature : gt.getMethodDefinitions()) {
                    if (methodSignature.getName().equals("getLeafUnion")) {
                        assertEquals("LeafUnion", methodSignature.getReturnType().getName());
                    }
                }
            }
        }
    }

    @Test
    public void generatedTypesUsesLeafTest() {
        final BindingGenerator bg = new BindingGeneratorImpl(false);
        final SchemaContext context = YangParserTestUtils.parseYangResources(BindingGeneratorImplTest.class,
            "/uses-statement/test-uses-leaf-innertype2-base.yang", "/uses-statement/test-uses-leaf-innertype2.yang");
        final List<Type> generateTypes = bg.generateTypes(context);
        assertNotNull(generateTypes);
        assertTrue(!generateTypes.isEmpty());
        for (final Type type : generateTypes) {
            if (type.getName().equals("MyCont") && type.getPackageName()
                    .equals("org.opendaylight.mdsal.gen.javav2.urn.test.uses.leaf.innertype2.base.rev170809.data")) {
                final GeneratedType gt = (GeneratedType) type;
                for (MethodSignature methodSignature : gt.getMethodDefinitions()) {
                    if (methodSignature.getName().equals("getLeafDecimal64")) {
                        assertEquals("BigDecimal", methodSignature.getReturnType().getName());
                    }
                }

            }

            if (type.getName().equals("MyCont") && type.getPackageName()
                    .equals("org.opendaylight.mdsal.gen.javav2.urn.test.uses.leaf.innertype2.rev170809.data")) {
                final GeneratedType gt = (GeneratedType) type;
                for (MethodSignature methodSignature : gt.getMethodDefinitions()) {
                    if (methodSignature.getName().equals("getLeafDecimal64")) {
                        assertEquals("BigDecimal", methodSignature.getReturnType().getName());
                    }
                }
            }
        }
    }

    @Test
    public void generatedTypesUsesLeafInnertype3Test() {
        final BindingGenerator bg = new BindingGeneratorImpl(false);
        final SchemaContext context = YangParserTestUtils.parseYangResources(BindingGeneratorImplTest.class,
            "/uses-statement/test-uses-leaf-innertype3-base.yang",
            "/uses-statement/test-uses-leaf-innertype3.yang");
        final List<Type> generateTypes = bg.generateTypes(context);
        assertNotNull(generateTypes);
        assertTrue(!generateTypes.isEmpty());
        for (final Type type : generateTypes) {
            if (type.getName().equals("MyCont") && type.getPackageName()
                    .equals("org.opendaylight.mdsal.gen.javav2.urn.test.uses.leaf.innertype3.rev170809.data")) {
                final GeneratedType gt = (GeneratedType) type;
                for (MethodSignature methodSignature : gt.getMethodDefinitions()) {
                    if (methodSignature.getName().equals("getBandwidth")) {
                        assertEquals("Bandwidth", methodSignature.getReturnType().getName());
                    }
                }

            }

            if (type.getName().equals("Open") && type.getPackageName()
                    .equals("org.opendaylight.mdsal.gen.javav2.urn.test.uses.leaf.innertype3.rev170809.data")) {
                final GeneratedType gt = (GeneratedType) type;
                for (MethodSignature methodSignature : gt.getMethodDefinitions()) {
                    if (methodSignature.getName().equals("getVersion")) {
                        assertEquals("ProtocolVersion", methodSignature.getReturnType().getName());
                    }

                    if (methodSignature.getName().equals("getLeafUnion")) {
                        assertEquals("LeafUnion", methodSignature.getReturnType().getName());
                    }
                }

            }
        }
    }

    @Test
    public void generatedTypesTest() {
        final BindingGenerator bg = new BindingGeneratorImpl(false);
        final SchemaContext context = YangParserTestUtils.parseYangResource("/generator/test-list.yang");
        final List<Type> generateTypes = bg.generateTypes(context);

        assertNotNull(generateTypes);
        assertTrue(!generateTypes.isEmpty());
        assertEquals(3, generateTypes.size());
        final int[] test_i = { 0 };
        for (final Type t : generateTypes) {
            if (t instanceof GeneratedTransferObject) {
                final GeneratedTransferObject genTransferObj = (GeneratedTransferObject) t;
                assertBaseGeneratedType(genTransferObj, "MyListKey",
                        "org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314.key.my_list");
                assertEquals(3, genTransferObj.getProperties().size());
                int testJ = 0;
                for (final GeneratedProperty generatedProperty : genTransferObj.getProperties()) {
                    switch (generatedProperty.getName()) {
                        case "keyReservedWord":
                            assertEquals("String", generatedProperty.getReturnType().getName());
                            testJ++;
                            break;
                        case "key1":
                            assertEquals("String", generatedProperty.getReturnType().getName());
                            testJ++;
                            break;
                        case "key2":
                            assertEquals("String", generatedProperty.getReturnType().getName());
                            testJ++;
                            break;
                        default:
                            fail();
                    }
                }
                assertEquals(3, testJ);
                test_i[0] += 1;
            } else {
                testActualType((GeneratedType) t, test_i);
            }
        }
        assertEquals(3, test_i[0]);
    }

    @Test
    public void generateTypesIdentityTest() throws Exception {
        final BindingGenerator bg = new BindingGeneratorImpl(true);
        final SchemaContext context = YangParserTestUtils.parseYangResourceDirectory("/identity/");
        assertNotNull(context);

        final List<Type> generateTypes = bg.generateTypes(context, context.getModules());
        assertNotNull(generateTypes);
        assertTrue(!generateTypes.isEmpty());
        for (final Type type : generateTypes) {
            if (type.getFullyQualifiedName()
                    .equals("org.opendaylight.mdsal.gen.javav2.identity3.module.rev170708.ident.Iden1")) {
                final GeneratedType genType = (GeneratedType)type;
                assertEquals("org.opendaylight.mdsal.gen.javav2.identity3.module.rev170708.ident.Iden2",
                    genType.getImplements().stream().findFirst().get().getFullyQualifiedName());

            }
            if (type.getFullyQualifiedName()
                    .equals("org.opendaylight.mdsal.gen.javav2.identity3.module.rev170708.ident.Iden2")) {
                final GeneratedType genType = (GeneratedType)type;
                assertEquals("org.opendaylight.mdsal.gen.javav2.identity.import_.rev170602.ident.Iden1",
                    genType.getImplements().stream().findFirst().get().getFullyQualifiedName());

            }
            if (type.getFullyQualifiedName()
                    .equals("org.opendaylight.mdsal.gen.javav2.identity3.module.rev170708.ident.Iden3")) {
                final GeneratedType genType = (GeneratedType)type;
                assertEquals("org.opendaylight.mdsal.gen.javav2.identity3.module.rev170708.ident.Iden1",
                    genType.getImplements().stream().findFirst().get().getFullyQualifiedName());

            }
            if (type.getFullyQualifiedName()
                .equals("org.opendaylight.mdsal.gen.javav2.identity4.module.rev180227.ident.Iden3")) {
                final GeneratedType genType = (GeneratedType)type;
                genType.getImplements().stream().forEach(impl -> {
                    final String fqn = impl.getFullyQualifiedName();
                    assertTrue(fqn.equals(
                            "org.opendaylight.mdsal.gen.javav2.identity4.module.rev180227.ident.Iden1")
                        || fqn.equals(
                            "org.opendaylight.mdsal.gen.javav2.identity4.module.rev180227.ident.Iden2"));
                });

            }
        }
    }

    private void testActualType(final GeneratedType type, final int[] testI) {
        MethodSignature methodSignature = null;
        switch (type.getName()) {
            case "TestListData":
                assertEquals("org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314",
                        type.getPackageName());
                methodSignature = type.getMethodDefinitions().get(0);
                assertMethod(type, "getMyList", "List", "java.util", methodSignature);
                testI[0] += 1;
                break;
            case "MyListKey":
                assertEquals("org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314.key.my_list",
                        type.getPackageName());
                methodSignature = type.getMethodDefinitions().get(0);
                assertMethod(type, "getKey", "String", "java.lang", methodSignature);
                testI[0] += 1;
                break;
            case "MyListKey1":
                assertEquals("org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314.key.my_list",
                        type.getPackageName());
                methodSignature = type.getMethodDefinitions().get(0);
                assertMethod(type, "getKey1", "String", "java.lang", methodSignature);
                testI[0] += 1;
                break;
            case "MyListKey2":
                assertEquals("org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314.key.my_list",
                        type.getPackageName());
                methodSignature = type.getMethodDefinitions().get(0);
                assertMethod(type, "getKey2", "String", "java.lang", methodSignature);
                testI[0] += 1;
                break;
            case "MyListFoo":
                assertEquals("org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314.data.my_list",
                        type.getPackageName());
                methodSignature = type.getMethodDefinitions().get(0);
                assertMethod(type, "getFoo", "String", "java.lang", methodSignature);
                testI[0] += 1;
                break;
            case "MyList":
                assertEquals("org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314.data",
                        type.getPackageName());
                assertEquals(5, type.getMethodDefinitions().size());
                int testJ = 0;
                for (final MethodSignature m : type.getMethodDefinitions()) {
                    switch (m.getName()) {
                        case "getIdentifier":
                            assertMethod(type, "getIdentifier", "MyListKey",
                                "org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314.key.my_list",
                                        m);
                            testJ++;
                            break;
                        case "getKey1":
                            assertMethod(type, "getKey1", "String","java.lang", m);
                            testJ++;
                            break;
                        case "getKey2":
                            assertMethod(type, "getKey2", "String","java.lang", m);
                            testJ++;
                            break;
                        case "getFoo":
                            assertMethod(type, "getFoo", "String","java.lang", m);
                            testJ++;
                            break;
                        case "getKeyReservedWord":
                            assertMethod(type, "getKeyReservedWord", "String","java.lang", m);
                            testJ++;
                            break;
                        default:
                            fail();
                    }
                }
                assertEquals(5, testJ);
                testI[0] += 1;
                break;
            default:
                fail();
        }
    }

    private void assertBaseGeneratedType(final GeneratedType genType, final String name, final String packageName) {
        assertEquals(name, genType.getName());

    }

    private void assertMethod(final GeneratedType definingTypeExpected, final String nameExpected,
            final String typeNameExpected, final String typePackageNameExpected,
            final MethodSignature methodSignature) {
        assertEquals(definingTypeExpected, methodSignature.getDefiningType());
        assertEquals(nameExpected, methodSignature.getName());
        assertEquals(typeNameExpected, methodSignature.getReturnType().getName());
        assertEquals(typePackageNameExpected, methodSignature.getReturnType().getPackageName());
    }

}
