/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class JavaTypeNameTest {
    @Test
    void testOperations() {
        final JavaTypeName byteName = JavaTypeName.create(byte.class);
        assertEquals("", byteName.packageName());
        assertEquals("byte", byteName.simpleName());
        assertEquals("byte", byteName.toString());
        assertNull(byteName.immediatelyEnclosingClass());
        assertSame(byteName, byteName.topLevelClass());
        assertEquals(of("byte"), byteName.localNameComponents());
        assertEquals("byte", byteName.localName());

        final JavaTypeName charName = byteName.createSibling("char");
        assertEquals("", charName.packageName());
        assertEquals("char", charName.simpleName());
        assertEquals("char", charName.toString());
        assertNull(charName.immediatelyEnclosingClass());
        assertSame(charName, charName.topLevelClass());
        assertEquals(of("char"), charName.localNameComponents());
        assertEquals("char", charName.localName());

        final JavaTypeName threadName = JavaTypeName.create(Thread.class);
        assertEquals("java.lang", threadName.packageName());
        assertEquals("Thread", threadName.simpleName());
        assertEquals("java.lang.Thread", threadName.toString());
        assertEquals(Optional.empty(), threadName.immediatelyEnclosingClass());
        assertTrue(threadName.canCreateEnclosed("Foo"));
        assertFalse(threadName.canCreateEnclosed("Thread"));
        assertEquals(threadName, JavaTypeName.create("java.lang", "Thread"));
        assertSame(threadName, threadName.topLevelClass());
        assertEquals(of("Thread"), threadName.localNameComponents());
        assertEquals("Thread", threadName.localName());

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
        assertSame(threadName, enclosedName.topLevelClass());
        assertEquals(of("Thread", "Foo"), enclosedName.localNameComponents());
        assertEquals("Thread.Foo", enclosedName.localName());

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

        assertTrue(threadName.equals(JavaTypeName.create(Thread.class)));
        assertTrue(threadName.equals(threadName));
        assertFalse(threadName.equals(null));
        assertFalse(threadName.equals("foo"));
    }

    @Test
    void testCreateEmptyPackage() {
        assertThrows(IllegalArgumentException.class, () -> JavaTypeName.create("", "Foo"));
    }

    @Test
    public void testCreateEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> JavaTypeName.create("foo", ""));
    }

    @Test
    void testCanCreateEnclosedPrimitive() {
        assertThrows(UnsupportedOperationException.class,
            () -> JavaTypeName.create(byte.class).canCreateEnclosed("foo"));
    }

    @Test
    void testCreateEnclosedPrimitive() {
        assertThrows(UnsupportedOperationException.class,
            () -> JavaTypeName.create(byte.class).createEnclosed("foo"));
    }
}
