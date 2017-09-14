/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.util.generated.type.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.Enumeration;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.type.EnumPairBuilder;

public class EnumerationBuilderImplTest {

    private final QName qName = QName.create("TestQName", "2014-10-10", "TestLocalQName");
    private final String DESCRIPTION = "Test description of Enum";
    private final String packageName = "org.opendaylight.test";
    private final String name = "TestName";
    private final String moduleName = "TestModuleName";
    private final String reference = "TestRef";
    private final String valueName = "TestValue";
    private final String valueDescription = "Value used for test";
    private final int value = 12;
    private Enumeration enumeration;
    private EnumerationBuilderImpl enumerationBuilder;
    private EnumerationBuilderImpl enumerationBuilderSame;
    private EnumerationBuilderImpl enumerationBuilderOtherName;
    private EnumerationBuilderImpl enumerationBuilderOtherPackage;

    @Before
    public void setup() {
        enumerationBuilder = new EnumerationBuilderImpl(packageName, name);
        enumerationBuilder.setDescription(DESCRIPTION);
        enumerationBuilder.setModuleName(moduleName);
        enumerationBuilder.setReference(reference);
        enumerationBuilder.setSchemaPath(Collections.singletonList(qName));
        enumerationBuilder.addValue(valueName, value, valueDescription);
        enumerationBuilder.addAnnotation(packageName, "TestAnnotation");
        enumerationBuilderSame = new EnumerationBuilderImpl(packageName, name);
        enumerationBuilderOtherName = new EnumerationBuilderImpl(packageName, "SomeOtherName");
        enumerationBuilderOtherPackage = new EnumerationBuilderImpl("org.opendaylight.other", name);
        enumeration = enumerationBuilder.toInstance(enumerationBuilder);
    }

    @Test
    public void testAddNullAnnotation() {
        assertNull(enumerationBuilder.addAnnotation(null, null));
        assertNull(enumerationBuilder.addAnnotation(null, "test"));
        assertNull(enumerationBuilder.addAnnotation(packageName, null));
    }

    @Test
    public void testEnumerationBuilder() {
        assertEquals(packageName + "." + name, enumerationBuilder.getFullyQualifiedName());
        assertEquals(name , enumerationBuilder.getName());
        assertEquals(packageName, enumerationBuilder.getPackageName());

        assertNotEquals(enumerationBuilder, null);
        assertEquals(enumerationBuilder, enumerationBuilder);
        assertNotEquals(enumerationBuilder, "string");
        assertNotEquals(enumerationBuilder, enumerationBuilderOtherName);
        assertNotEquals(enumerationBuilder, enumerationBuilderOtherPackage);
        assertEquals(enumerationBuilder,enumerationBuilderSame);
    }

    @Test
    public void testEnumeration() {
        assertEquals(name, enumeration.getName());
        assertEquals(packageName, enumeration.getPackageName());
        assertEquals(null, enumeration.getComment());
        assertEquals(enumerationBuilder, enumeration.getParentType());
        assertEquals(DESCRIPTION, enumeration.getDescription());
        assertEquals(moduleName, enumeration.getModuleName());
        assertEquals(packageName + '.' + name, enumeration.getFullyQualifiedName());
        assertEquals(reference, enumeration.getReference());
        assertEquals(Collections.singletonList(qName), enumeration.getSchemaPath());
        assertEquals(Collections.EMPTY_LIST, enumeration.getEnclosedTypes());
        assertEquals(Collections.EMPTY_LIST, enumeration.getEnumerations());
        assertEquals(Collections.EMPTY_LIST, enumeration.getMethodDefinitions());
        assertEquals(Collections.EMPTY_LIST, enumeration.getConstantDefinitions());
        assertEquals(Collections.EMPTY_LIST, enumeration.getProperties());
        assertEquals(Collections.EMPTY_LIST, enumeration.getImplements());
        assertNotNull(enumeration.getValues());
        assertNotNull(enumeration.getAnnotations());

        assertFalse(enumeration.isAbstract());
        assertNotEquals(enumeration, null);
        assertEquals(enumeration, enumeration);
        assertNotEquals(enumeration, "string");

        final Enumeration enumerationOtherPackage = enumerationBuilderOtherPackage.toInstance(enumerationBuilderOtherPackage);
        assertNotEquals(enumeration, enumerationOtherPackage);

        final Enumeration enumerationOtherName = enumerationBuilderOtherName.toInstance(enumerationBuilderOtherName);
        assertNotEquals(enumeration, enumerationOtherName);

        enumerationBuilderSame.addValue(valueName, value, valueDescription);
        final Enumeration enumerationSame = enumerationBuilderSame.toInstance(enumerationBuilderSame);
        assertEquals(enumeration, enumerationSame);

        final EnumerationBuilderImpl enumerationBuilderSame1 = new EnumerationBuilderImpl(packageName, name);
        final Enumeration enumerationSame1 = enumerationBuilderSame1.toInstance(enumerationBuilderSame1);
        enumerationBuilderSame1.addValue(valueName, 14, valueDescription);
        // Enums are equal thanks to same package name and local name
        assertEquals(enumeration, enumerationSame1);
    }

    @Test
    public void testEnumerationToString() {
        final String formattedString =
                "public enum " + name + " {\n" +
                "\t TestValue " + "(12 );\n" +
                "}";
        final String s = "Enumeration [packageName="+packageName+", definingType="+packageName+"."+name+", name="+name+
                ", values=[EnumPair [name=TestValue, mappedName=TestValue, value=12]]]";

        assertEquals(s, enumeration.toString());
        assertEquals(formattedString, enumeration.toFormattedString());

        assertEquals("EnumerationBuilderImpl " +
                "[packageName=org.opendaylight.test, name=TestName, " +
                "values=[EnumPair [name=TestValue, mappedName=TestValue, value=12]]]",
                enumerationBuilder.toString());
    }

    @Test
    public void testUpdateEnumPairsFromEnumTypeDef() {
        final EnumTypeDefinition enumTypeDefinition = BaseTypes.enumerationTypeBuilder(SchemaPath.SAME)
                .addEnum(EnumPairBuilder.create("SomeName", 42).setDescription("Some Other Description")
                    .setReference("Some other reference").build()).build();
        enumerationBuilder.updateEnumPairsFromEnumTypeDef(enumTypeDefinition);
    }
}
