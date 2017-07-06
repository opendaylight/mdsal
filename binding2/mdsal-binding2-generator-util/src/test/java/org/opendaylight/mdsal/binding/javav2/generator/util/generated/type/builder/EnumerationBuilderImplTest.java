/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.generator.context.ModuleContext;
import org.opendaylight.mdsal.binding.javav2.model.api.Enumeration;
import org.opendaylight.yangtools.yang.model.api.Status;

public class EnumerationBuilderImplTest {

    @Test
    public void sameEnumTest() {
        ModuleContext context = new ModuleContext();
        EnumerationBuilderImpl enumerationBuilderImpl = new EnumerationBuilderImpl("package.same.test", "test", context);
        Enumeration enumeration = enumerationBuilderImpl.toInstance(enumerationBuilderImpl);
        String formattedString = enumeration.toFormattedString();

        assertNotNull(formattedString);
        assertTrue(!formattedString.isEmpty());
        assertTrue(formattedString.contains("Test"));

        enumerationBuilderImpl = new EnumerationBuilderImpl("package.same.test", "Test", context);
        enumeration = enumerationBuilderImpl.toInstance(enumerationBuilderImpl);
        formattedString = enumeration.toFormattedString();

        assertNotNull(formattedString);
        assertTrue(!formattedString.isEmpty());
        assertTrue(formattedString.contains("Test1"));
    }

    @Test
    public void enumTest() {
        final EnumerationBuilderImpl enumerationBuilderImpl =
                new EnumerationBuilderImpl("package.test", "test**", new ModuleContext());
        enumerationBuilderImpl.addValue("value", 1, "des", "ref", Status.CURRENT);
        final Enumeration enumeration = enumerationBuilderImpl.toInstance(enumerationBuilderImpl);
        final String formattedString = enumeration.toFormattedString();

        assertNotNull(formattedString);
        assertTrue(!formattedString.isEmpty());
        assertTrue(formattedString.contains("public enum TestAsteriskAsterisk {"));
        assertTrue(formattedString.contains("VALUE"));
    }

    @Test
    public void enumUniqueTest() {
        final EnumerationBuilderImpl enumerationBuilderImpl =
                new EnumerationBuilderImpl("package.ex.ex.ex.test", "test", new ModuleContext());
        enumerationBuilderImpl.addValue("foo", 1, "des", "ref", Status.CURRENT);
        enumerationBuilderImpl.addValue("Foo", 1, "des", "ref", Status.CURRENT);
        enumerationBuilderImpl.addValue("foo1", 1, "des", "ref", Status.CURRENT);
        enumerationBuilderImpl.addValue("Foo1", 1, "des", "ref", Status.CURRENT);
        enumerationBuilderImpl.addValue("FOO1", 1, "des", "ref", Status.CURRENT);
        enumerationBuilderImpl.addValue("FOO", 1, "des", "ref", Status.CURRENT);
        enumerationBuilderImpl.addValue("foO1", 1, "des", "ref", Status.CURRENT);
        enumerationBuilderImpl.addValue("foO2", 1, "des", "ref", Status.CURRENT);
        enumerationBuilderImpl.addValue("foO2", 1, "des", "ref", Status.CURRENT);
        enumerationBuilderImpl.addValue("Foo*", 1, "des", "ref", Status.CURRENT);
        enumerationBuilderImpl.addValue("foo*", 1, "des", "ref", Status.CURRENT);

        enumerationBuilderImpl.addValue("f__11", 1, "des", "ref", Status.CURRENT);
        enumerationBuilderImpl.addValue("f__1_1", 1, "des", "ref", Status.CURRENT);
        enumerationBuilderImpl.addValue("f__1", 1, "des", "ref", Status.CURRENT);
        enumerationBuilderImpl.addValue("F__1", 1, "des", "ref", Status.CURRENT);
        enumerationBuilderImpl.addValue("f_1_1", 1, "des", "ref", Status.CURRENT);
        enumerationBuilderImpl.addValue("F_1_1", 1, "des", "ref", Status.CURRENT);

        enumerationBuilderImpl.addValue("fo", 1, "des", "ref", Status.CURRENT);
        enumerationBuilderImpl.addValue("Fo", 1, "des", "ref", Status.CURRENT);
        enumerationBuilderImpl.addValue("fO", 1, "des", "ref", Status.CURRENT);
        enumerationBuilderImpl.addValue("FO", 1, "des", "ref", Status.CURRENT);

        final Enumeration enumeration = enumerationBuilderImpl.toInstance(enumerationBuilderImpl);
        final String formattedString = enumeration.toFormattedString();

        assertNotNull(formattedString);
        assertTrue(!formattedString.isEmpty());
        assertTrue(formattedString.contains("FOO"));
        assertTrue(formattedString.contains("FOO_1"));
        assertTrue(formattedString.contains("FOO1"));
        assertTrue(formattedString.contains("FOO1_1"));
        assertTrue(formattedString.contains("FOO1_2"));
        assertTrue(formattedString.contains("FOO_2"));
        assertTrue(formattedString.contains("FOO1_3"));
        assertTrue(formattedString.contains("FOO2"));
        assertTrue(formattedString.contains("FOO2_1"));
        assertTrue(formattedString.contains("FOO_ASTERISK"));
        assertTrue(formattedString.contains("FOO_ASTERISK_1"));

        assertTrue(formattedString.contains("F_11"));
        assertTrue(formattedString.contains("F_1_1"));
        assertTrue(formattedString.contains("F_1"));
        assertTrue(formattedString.contains("F_1_2"));
        assertTrue(formattedString.contains("F_1_1_1"));
        assertTrue(formattedString.contains("F_1_1_2"));

        assertTrue(formattedString.contains("FO"));
        assertTrue(formattedString.contains("FO_1"));
        assertTrue(formattedString.contains("FO_2"));
        assertTrue(formattedString.contains("FO_3"));
    }

    @Test
    public void asteriskInEnumTest() {
        final EnumerationBuilderImpl enumerationBuilderImpl =
                new EnumerationBuilderImpl("package.ex.test", "test**", new ModuleContext());
        enumerationBuilderImpl.addValue("val**ue", 1, "des", "ref", Status.CURRENT);
        enumerationBuilderImpl.addValue("val*ue", 1, "des", "ref", Status.CURRENT);
        enumerationBuilderImpl.addValue("*value*", 1, "des", "ref", Status.CURRENT);
        enumerationBuilderImpl.addValue("*", 1, "des", "ref", Status.CURRENT);
        final Enumeration enumeration = enumerationBuilderImpl.toInstance(enumerationBuilderImpl);
        final String formattedString = enumeration.toFormattedString();

        assertNotNull(formattedString);
        assertTrue(!formattedString.isEmpty());
        assertTrue(formattedString.contains("public enum TestAsteriskAsterisk {"));
        assertTrue(formattedString.contains("VAL_ASTERISK_ASTERISK_UE"));
        assertTrue(formattedString.contains("VAL_ASTERISK_UE"));
        assertTrue(formattedString.contains("ASTERISK_VALUE_ASTERISK"));
        assertTrue(formattedString.contains("ASTERISK"));
    }

    @Test
    public void reverseSolidusInEnumTest() {
        final EnumerationBuilderImpl enumerationBuilderImpl =
                new EnumerationBuilderImpl("package.ex.ex.test", "test\\\\", new ModuleContext());
        enumerationBuilderImpl.addValue("val\\\\ue", 1, "des", "ref", Status.CURRENT);
        enumerationBuilderImpl.addValue("val\\ue", 1, "des", "ref", Status.CURRENT);
        enumerationBuilderImpl.addValue("\\value\\", 1, "des", "ref", Status.CURRENT);
        enumerationBuilderImpl.addValue("\\", 1, "des", "ref", Status.CURRENT);
        final Enumeration enumeration = enumerationBuilderImpl.toInstance(enumerationBuilderImpl);
        final String formattedString = enumeration.toFormattedString();
        assertNotNull(formattedString);
        assertTrue(!formattedString.isEmpty());
        assertTrue(formattedString.contains("public enum TestReverseSolidusReverseSolidus {"));
        assertTrue(formattedString.contains("VAL_REVERSE_SOLIDUS_REVERSE_SOLIDUS_UE"));
        assertTrue(formattedString.contains("VAL_REVERSE_SOLIDUS_UE"));
        assertTrue(formattedString.contains("REVERSE_SOLIDUS_VALUE_REVERSE_SOLIDUS"));
        assertTrue(formattedString.contains("REVERSE_SOLIDUS"));
    }
}
