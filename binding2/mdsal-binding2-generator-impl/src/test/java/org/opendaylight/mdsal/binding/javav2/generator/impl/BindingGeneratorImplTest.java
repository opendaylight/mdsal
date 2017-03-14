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
    public void test() throws Exception {
        final BindingGenerator bg = new BindingGeneratorImpl(false);
        final SchemaContext context = YangParserTestUtils.parseYangSource("/generator/test-list.yang");
        final List<Type> generateTypes = bg.generateTypes(context);
        assertNotNull(generateTypes);
        assertTrue(!generateTypes.isEmpty());
        assertEquals(7, generateTypes.size());

        Type type = generateTypes.get(0);
        assertNotNull(type);
        assertTrue(type instanceof GeneratedType);

        GeneratedType genType = (GeneratedType) type;
        assertBaseGeneratedType(genType, "TestListData",
                "org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314");

        MethodSignature methodSignature = genType.getMethodDefinitions().get(0);
        assertMethod(genType, "getMyList", "List", "java.util", methodSignature);

        type = generateTypes.get(3);
        assertNotNull(type);
        assertTrue(type instanceof GeneratedType);

        genType = (GeneratedType) type;
        assertBaseGeneratedType(genType, "MyListKey",
                "org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314.key.my_list");

        methodSignature = genType.getMethodDefinitions().get(0);
        assertMethod(genType, "getKey", "String", "java.lang", methodSignature);

        type = generateTypes.get(4);
        assertNotNull(type);
        assertTrue(type instanceof GeneratedType);

        genType = (GeneratedType) type;
        assertBaseGeneratedType(genType, "MyListKey1",
                "org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314.key.my_list");

        methodSignature = genType.getMethodDefinitions().get(0);
        assertMethod(genType, "getKey1", "String", "java.lang", methodSignature);

        type = generateTypes.get(5);
        assertNotNull(type);
        assertTrue(type instanceof GeneratedType);

        genType = (GeneratedType) type;
        assertBaseGeneratedType(genType, "MyListKey2",
                "org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314.key.my_list");

        methodSignature = genType.getMethodDefinitions().get(0);
        assertMethod(genType, "getKey2", "String", "java.lang", methodSignature);

        type = generateTypes.get(6);
        assertNotNull(type);
        assertTrue(type instanceof GeneratedType);

        genType = (GeneratedType) type;
        assertBaseGeneratedType(genType, "MyListFoo",
                "org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314.data.my_list");

        methodSignature = genType.getMethodDefinitions().get(0);
        assertMethod(genType, "getFoo", "String", "java.lang", methodSignature);

        type = generateTypes.get(2);
        assertNotNull(type);
        assertTrue(type instanceof GeneratedType);

        genType = (GeneratedType) type;
        assertBaseGeneratedType(genType, "MyList",
                "org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314.data");

        methodSignature = genType.getMethodDefinitions().get(0);
        assertMethod(genType, "getMyListKey", "MyListKey",
                "org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314.key.my_list", methodSignature);
        methodSignature = genType.getMethodDefinitions().get(1);
        assertMethod(genType, "getMyListKey1", "MyListKey1",
                "org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314.key.my_list", methodSignature);
        methodSignature = genType.getMethodDefinitions().get(2);
        assertMethod(genType, "getMyListKey2", "MyListKey2",
                "org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314.key.my_list", methodSignature);
        methodSignature = genType.getMethodDefinitions().get(3);
        assertMethod(genType, "getMyListFoo", "MyListFoo",
                "org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314.data.my_list", methodSignature);
        methodSignature = genType.getMethodDefinitions().get(4);
        assertMethod(genType, "getKey", "MyListKey",
                "org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314.key.my_list.wrapper",
                methodSignature);

        type = generateTypes.get(1);
        assertNotNull(type);
        assertTrue(type instanceof GeneratedTransferObject);

        final GeneratedTransferObject genTransferObj = (GeneratedTransferObject) type;
        assertBaseGeneratedType(genTransferObj, "MyListKey",
                "org.opendaylight.mdsal.gen.javav2.urn.test.simple.test.list.rev170314.key.my_list.wrapper");
        assertEquals(3, genTransferObj.getProperties().size());
        assertPorperties("key", generateTypes.get(3), genTransferObj.getProperties().get(0));
        assertPorperties("key1", generateTypes.get(4), genTransferObj.getProperties().get(1));
        assertPorperties("key2", generateTypes.get(5), genTransferObj.getProperties().get(2));
    }

    private void assertPorperties(final String name, final Type type, final GeneratedProperty generatedProperty) {
        assertEquals(name, generatedProperty.getName());
        assertEquals(type, generatedProperty.getReturnType());
    }

    private void assertBaseGeneratedType(final GeneratedType genType, final String name, final String packageName) {
        assertEquals(name, genType.getName());
        assertEquals(packageName, genType.getPackageName());
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
