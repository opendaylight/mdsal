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
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.javav2.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class BindingGeneratorImplTest {

    @Test
    public void generatedTypesTest() throws Exception {
        final BindingGenerator bg = new BindingGeneratorImpl(false);
        final SchemaContext context = YangParserTestUtils.parseYangSource("/generator/test-list.yang");
        final List<Type> generateTypes = bg.generateTypes(context);

        assertNotNull(generateTypes);
        assertTrue(!generateTypes.isEmpty());
        assertEquals(7, generateTypes.size());
        final int[] test_i = { 0 };
        for (final Type t : generateTypes) {
            if (t instanceof GeneratedTransferObject) {
                final GeneratedTransferObject genTransferObj = (GeneratedTransferObject) t;
                assertBaseGeneratedType(genTransferObj, "MyListKey",
                        "org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314.key.my_list.wrapper");
                assertEquals(3, genTransferObj.getProperties().size());
                int test_j = 0;
                for (final GeneratedProperty generatedProperty : genTransferObj.getProperties()) {
                    switch (generatedProperty.getName()) {
                        case "key":
                            assertEquals("MyListKey", generatedProperty.getReturnType().getName());
                            test_j++;
                            break;
                        case "key1":
                            assertEquals("MyListKey1", generatedProperty.getReturnType().getName());
                            test_j++;
                            break;
                        case "key2":
                            assertEquals("MyListKey2", generatedProperty.getReturnType().getName());
                            test_j++;
                            break;
                        default:
                            fail();
                    }
                }
                assertEquals(3, test_j);
                test_i[0] += 1;
            } else {
                testActualType((GeneratedType) t, test_i);
            }
        }
        assertEquals(7, test_i[0]);
    }

    @Test
    public void generateTypesDescriptionsTest() throws Exception {
        final BindingGenerator bg = new BindingGeneratorImpl(true);
        final SchemaContext context = YangParserTestUtils.parseYangSources("/base/with_import/");
        assertNotNull(context);

        final List<Type> generateTypes = bg.generateTypes(context, context.getModules());
        assertNotNull(generateTypes);
        assertTrue(!generateTypes.isEmpty());

        for (final Type type : generateTypes) {
            if (type.getName().equals("TestData")) {
                final String description = ((GeneratedType) type).getDescription().get();
                description
                        .contains("    import test-import { prefix \"imported-test\"; revision-date 2017-04-21; }\n\n");
                description.contains("    revision 2017-02-06;\n\n");
                description.contains("    typedef my-type {\n        type int8;\n    }");
                description.contains("    container *my-cont {\n    }\n");
            }
        }
    }

    private void testActualType(final GeneratedType t, final int[] test_i) {
        MethodSignature methodSignature = null;
        switch (t.getName()) {
            case "TestListData":
                assertEquals("org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314",
                        t.getPackageName());
                methodSignature = t.getMethodDefinitions().get(0);
                assertMethod(t, "getMyList", "List", "java.util", methodSignature);
                test_i[0] += 1;
                break;
            case "MyListKey":
                assertEquals("org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314.key.my_list",
                        t.getPackageName());
                methodSignature = t.getMethodDefinitions().get(0);
                assertMethod(t, "getKey", "String", "java.lang", methodSignature);
                test_i[0] += 1;
                break;
            case "MyListKey1":
                assertEquals("org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314.key.my_list",
                        t.getPackageName());
                methodSignature = t.getMethodDefinitions().get(0);
                assertMethod(t, "getKey1", "String", "java.lang", methodSignature);
                test_i[0] += 1;
                break;
            case "MyListKey2":
                assertEquals("org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314.key.my_list",
                        t.getPackageName());
                methodSignature = t.getMethodDefinitions().get(0);
                assertMethod(t, "getKey2", "String", "java.lang", methodSignature);
                test_i[0] += 1;
                break;
            case "MyListFoo":
                assertEquals("org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314.data.my_list",
                        t.getPackageName());
                methodSignature = t.getMethodDefinitions().get(0);
                assertMethod(t, "getFoo", "String", "java.lang", methodSignature);
                test_i[0] += 1;
                break;
            case "MyList":
                assertEquals("org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314.data",
                        t.getPackageName());
                assertEquals(5, t.getMethodDefinitions().size());
                int test_j = 0;
                for (final MethodSignature m : t.getMethodDefinitions()) {
                    switch (m.getName()) {
                        case "getKey":
                                assertMethod(t, "getKey", "MyListKey",
                                        "org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314.key.my_list.wrapper",
                                        m);
                            test_j++;
                            break;
                        case "getMyListKey1":
                            assertMethod(t, "getMyListKey1", "MyListKey1",
                                    "org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314.key.my_list",
                                    m);
                            test_j++;
                            break;
                        case "getMyListKey2":
                            assertMethod(t, "getMyListKey2", "MyListKey2",
                                    "org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314.key.my_list",
                                    m);
                            test_j++;
                            break;
                        case "getMyListFoo":
                            assertMethod(t, "getMyListFoo", "MyListFoo",
                                    "org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314.data.my_list",
                                    m);
                            test_j++;
                            break;
                        case "getMyListKey":
                            assertMethod(t, "getMyListKey", "MyListKey",
                                    "org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314.key.my_list",
                                    m);
                            test_j++;
                            break;
                        default:
                            fail();
                    }
                }
                assertEquals(5, test_j);
                test_i[0] += 1;
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
