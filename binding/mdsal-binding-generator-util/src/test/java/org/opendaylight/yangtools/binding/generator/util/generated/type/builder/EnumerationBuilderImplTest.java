/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.util.generated.type.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.model.api.Enumeration;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.EnumPairImpl;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;

public class EnumerationBuilderImplTest {

    private EnumerationBuilderImpl enumerationBuilder;
    private EnumerationBuilderImpl enumerationBuilderSame;
    private EnumerationBuilderImpl enumerationBuilderOtherName;
    private EnumerationBuilderImpl enumerationBuilderOtherPackage;
    private final String DESCRIPTION = "Test description of Enum";
    private final String packageName = "org.opendaylight.test";
    private final String name = "TestName";
    private final String moduleName = "TestModuleName";
    private final String reference = "TestRef";
    private final String valueName = "TestValue";
    private final String valueDescription = "Value used for test";
    private final int value = 12;
    private Enumeration enumeration;
    private final QName qName = QName.create("TestQName", "10-10-2014", "TestLocalQName");


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

        Enumeration enumerationOtherPackage = enumerationBuilderOtherPackage.toInstance(enumerationBuilderOtherPackage);
        assertNotEquals(enumeration, enumerationOtherPackage);

        Enumeration enumerationOtherName = enumerationBuilderOtherName.toInstance(enumerationBuilderOtherName);
        assertNotEquals(enumeration, enumerationOtherName);

        enumerationBuilderSame.addValue(valueName, value, valueDescription);
        Enumeration enumerationSame = enumerationBuilderSame.toInstance(enumerationBuilderSame);
        assertEquals(enumeration, enumerationSame);

        EnumerationBuilderImpl enumerationBuilderSame1 = new EnumerationBuilderImpl(packageName, name);
        Enumeration enumerationSame1 = enumerationBuilderSame1.toInstance(enumerationBuilderSame1);
        enumerationBuilderSame1.addValue(valueName, 14, valueDescription);
        // Enums are equal thanks to same package name and local name
        assertEquals(enumeration, enumerationSame1);
    }

    @Test
    public void testEnumerationToString() {
        String formattedString =
                "public enum " + name + " {\n" +
                "\t TestValue " + "(12 );\n" +
                "}";
        String s = "Enumeration [packageName="+packageName+", definingType="+packageName+"."+name+", name="+name+
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
        EnumTypeDefinition enumTypeDefinition = BaseTypes.enumerationTypeBuilder(SchemaPath.SAME)
                .addEnum(new EnumPairImpl("SomeName", 42, "Some Other Description", "Some other reference",
                    Status.CURRENT, ImmutableList.of())).build();
        enumerationBuilder.updateEnumPairsFromEnumTypeDef(enumTypeDefinition);
    }
}
