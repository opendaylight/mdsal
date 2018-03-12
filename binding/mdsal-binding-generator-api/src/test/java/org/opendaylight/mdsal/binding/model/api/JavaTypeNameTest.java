/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import org.junit.Test;

public class JavaTypeNameTest {

    @Test
    public void testOperations() {
        final JavaTypeName byteName = JavaTypeName.create(byte.class);
        assertEquals("", byteName.packageName());
        assertEquals("byte", byteName.simpleName());
        assertEquals("byte", byteName.toString());
        assertEquals(Optional.empty(), byteName.immediatelyEnclosingClass());

        final JavaTypeName charName = byteName.createSibling("char");
        assertEquals("", charName.packageName());
        assertEquals("char", charName.simpleName());
        assertEquals("char", charName.toString());
        assertEquals(Optional.empty(), charName.immediatelyEnclosingClass());

        final JavaTypeName threadName = JavaTypeName.create(Thread.class);
        assertEquals("java.lang", threadName.packageName());
        assertEquals("Thread", threadName.simpleName());
        assertEquals("java.lang.Thread", threadName.toString());
        assertEquals(Optional.empty(), threadName.immediatelyEnclosingClass());
        assertTrue(threadName.canCreateEnclosed("Foo"));
        assertFalse(threadName.canCreateEnclosed("Thread"));
        assertEquals(threadName, JavaTypeName.create("java.lang", "Thread"));

        final JavaTypeName stringName = threadName.createSibling("String");
        assertEquals("java.lang", stringName.packageName());
        assertEquals("String", stringName.simpleName());
        assertEquals("java.lang.String", stringName.toString());
        assertEquals(Optional.empty(), stringName.immediatelyEnclosingClass());
        assertEquals(stringName, JavaTypeName.create("java.lang", "String"));

        final JavaTypeName enclosedName = threadName.createEnclosed("Foo");
        assertEquals("java.lang", enclosedName.packageName());
        assertEquals("Foo", enclosedName.simpleName());
        assertEquals("java.lang.Thread.Foo", enclosedName.toString());
        assertEquals(Optional.of(threadName), enclosedName.immediatelyEnclosingClass());

        final JavaTypeName uehName = JavaTypeName.create(Thread.UncaughtExceptionHandler.class);
        assertEquals("java.lang", uehName.packageName());
        assertEquals("UncaughtExceptionHandler", uehName.simpleName());
        assertEquals("java.lang.Thread.UncaughtExceptionHandler", uehName.toString());
        assertEquals(Optional.of(threadName), uehName.immediatelyEnclosingClass());
        assertTrue(uehName.canCreateEnclosed("Foo"));
        assertFalse(uehName.canCreateEnclosed("Thread"));
        assertFalse(uehName.canCreateEnclosed("UncaughtExceptionHandler"));

        final JavaTypeName siblingName = uehName.createSibling("Foo");
        assertEquals("java.lang", siblingName.packageName());
        assertEquals("Foo", siblingName.simpleName());
        assertEquals("java.lang.Thread.Foo", siblingName.toString());
        assertEquals(Optional.of(threadName), siblingName.immediatelyEnclosingClass());
        assertTrue(siblingName.canCreateEnclosed("UncaughtExceptionHandler"));
        assertFalse(siblingName.canCreateEnclosed("Thread"));
        assertFalse(siblingName.canCreateEnclosed("Foo"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEmptyPackage() {
        JavaTypeName.create("", "Foo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEmptyName() {
        JavaTypeName.create("foo", "");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCanCreateEnclosedPrimitive() {
        JavaTypeName.create(byte.class).canCreateEnclosed("foo");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCreateEnclosedPrimitive() {
        JavaTypeName.create(byte.class).createEnclosed("foo");
    }
}
