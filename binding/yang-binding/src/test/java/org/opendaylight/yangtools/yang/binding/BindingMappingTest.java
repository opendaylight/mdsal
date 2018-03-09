/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import static com.google.common.collect.ImmutableList.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;

public class BindingMappingTest {

    @Test
    public void basicTest() {
        assertTrue(BindingMapping.getRootPackageName(QName.create(QNameModule.create(URI.create("test:URI"),
                Revision.of("2017-10-26")), "test")).contains("test.uri"));
        assertNull(BindingMapping.normalizePackageName(null));
        assertTrue(BindingMapping.normalizePackageName("1testpublic").contains("_1testpublic"));
        assertTrue(BindingMapping.getMethodName(QName.create("testNS", "testLocalName")).equals("testLocalName"));
        assertTrue(BindingMapping.getMethodName("TestYangIdentifier").equals("testYangIdentifier"));
        assertTrue(BindingMapping.getClassName(QName.create("testNS", "testClass")).equals("TestClass"));
        assertTrue(BindingMapping.getClassName("testClass").equals("TestClass"));
        assertTrue(BindingMapping.getGetterSuffix(QName.create("test", "test")).equals("Test"));
        assertTrue(BindingMapping.getGetterSuffix(QName.create("test", "class")).equals("XmlClass"));
        assertTrue(BindingMapping.getPropertyName("Test").equals("test"));
        assertTrue(BindingMapping.getPropertyName("test").equals("test"));
        assertTrue(BindingMapping.getPropertyName("Class").equals("xmlClass"));
        assertEquals("_5", BindingMapping.getPropertyName("5"));
        assertEquals("", BindingMapping.getPropertyName(""));
        assertEquals("", BindingMapping.getClassName(""));
    }

    @Test(expected = UnsupportedOperationException.class)
    @SuppressWarnings({ "checkstyle:illegalThrows", "checkstyle:avoidHidingCauseException" })
    public void privateConstructTest() throws Throwable {
        final Constructor<BindingMapping> bindingMappingConstructor = BindingMapping.class.getDeclaredConstructor();
        assertFalse(bindingMappingConstructor.isAccessible());

        bindingMappingConstructor.setAccessible(true);
        try {
            bindingMappingConstructor.newInstance();
            fail("Expected exception for calling private constructor");
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    @Test
    public void mapEnumAssignedNamesTest() {
        // Okay identifier
        assertEqualMapping(of("__"), of("__"));
        assertEqualMapping(of("True"), of("true"));
        assertEqualMapping(of("ĽaľahoPapľuhu"), of("ľaľaho papľuhu"));

        // Mostly okay, but numbers need to be prefixed
        assertEqualMapping(of("AZ", "_09"), of("a-z", "0-9"));

        // Invalid identifier (conflicts with a Java 9 keyword)
        assertEqualMapping(of("$_"), of("_"));

        // Invalid characters, fall back to bijection
        assertEqualMapping(of("$$2A$"), of("*"));
        assertEqualMapping(of("$$2E$"), of("."));
        assertEqualMapping(of("$$2F$"), of("/"));
        assertEqualMapping(of("$$3F$"), of("?"));
        assertEqualMapping(of("$a$2A$a"), of("a*a"));

        // Conflict, fallback to bijection
        assertEqualMapping(of("aZ", "$a$2D$z"), of("aZ", "a-z"));
        assertEqualMapping(of("$a2$2E$5", "a25"), of("a2.5", "a25"));
        assertEqualMapping(of("$ľaľaho$20$papľuhu", "$ľaľaho$20$$20$papľuhu"), of("ľaľaho papľuhu", "ľaľaho  papľuhu"));
    }

    private static void assertEqualMapping(final List<String> mapped, final List<String> yang) {
        assertEquals(mapped.size(), yang.size());
        final Map<String, String> expected = new HashMap<>();
        for (int i = 0; i < mapped.size(); ++i) {
            expected.put(yang.get(i), mapped.get(i));
        }

        assertEquals(expected, BindingMapping.mapEnumAssignedNames(yang));
    }
}
