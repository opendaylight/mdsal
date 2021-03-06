/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.mdsal.binding.model.util.BindingTypes.TYPE_OBJECT;

import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.util.BindingTypes;
import org.opendaylight.mdsal.binding.model.util.Types;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Mdsal406TypeObjectTest {
    private static EffectiveModelContext CONTEXT;

    @BeforeClass
    public static void beforeClass() {
        CONTEXT = YangParserTestUtils.parseYangResources(ExtendedTypedefTest.class,
            "/type-provider/test.yang", "/ietf-models/ietf-inet-types.yang");
    }

    @AfterClass
    public static void afterClass() {
        CONTEXT = null;
    }

    @Test
    public void typeObjectTest() {
        final List<Type> generateTypes = DefaultBindingGenerator.generateFor(CONTEXT);
        assertNotNull(generateTypes);

        final Type typedefType = generateTypes.stream().filter(type -> type.getFullyQualifiedName()
            .equals("org.opendaylight.yang.gen.v1.urn.opendaylight.test.rev131008.MyBinary")).findFirst().get();

        assertTrue(typedefType instanceof GeneratedType);
        assertNotNull(((GeneratedType)  typedefType).getImplements());
        Type objectType = ((GeneratedType)  typedefType).getImplements().stream()
                .filter(type -> type.getFullyQualifiedName()
                .equals("org.opendaylight.yangtools.yang.binding.ScalarTypeObject")).findAny().get();
        assertEquals(BindingTypes.scalarTypeObject(Types.BYTE_ARRAY), objectType);
    }

    @Test
    public void typeObjectForBitsTypedefTest() {
        final List<Type> generateTypes = DefaultBindingGenerator.generateFor(CONTEXT);
        assertNotNull(generateTypes);

        final Type typedefType = generateTypes.stream().filter(type -> type.getFullyQualifiedName()
                .equals("org.opendaylight.yang.gen.v1.urn.opendaylight.test.rev131008.MyBits")).findFirst().get();

        assertTrue(typedefType instanceof GeneratedType);
        assertNotNull(((GeneratedType)  typedefType).getImplements());
        Type objectType = ((GeneratedType)  typedefType).getImplements().stream()
                .filter(type -> type.getFullyQualifiedName()
                        .equals("org.opendaylight.yangtools.yang.binding.TypeObject")).findAny().get();
        assertEquals(TYPE_OBJECT, objectType);
    }

    @Test
    public void typeObjectForUnionTypedefTest() {
        final List<Type> generateTypes = DefaultBindingGenerator.generateFor(CONTEXT);
        assertNotNull(generateTypes);

        final Type typedefType = generateTypes.stream().filter(type -> type.getFullyQualifiedName()
                .equals("org.opendaylight.yang.gen.v1.urn.opendaylight.test.rev131008.MyUnion")).findFirst().get();

        assertTrue(typedefType instanceof GeneratedType);
        assertNotNull(((GeneratedType)  typedefType).getImplements());
        Type objectType = ((GeneratedType)  typedefType).getImplements().stream()
                .filter(type -> type.getFullyQualifiedName()
                        .equals("org.opendaylight.yangtools.yang.binding.TypeObject")).findAny().get();
        assertEquals(TYPE_OBJECT, objectType);
    }
}
