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

import org.junit.jupiter.api.Test;

class JavaTypeNameTest {
    @Test
    void testOperations() {
        final var byteName = JavaTypeName.create(byte.class);
        assertEquals("", byteName.packageName());
        assertEquals("byte", byteName.simpleName());
        assertEquals("byte", byteName.toString());
        assertNull(byteName.immediatelyEnclosingClass());
        assertSame(byteName, byteName.topLevelClass());
        assertEquals(of("byte"), byteName.localNameComponents());
        assertEquals("byte", byteName.localName());

        final var charName = byteName.createSibling("char");
        assertEquals("", charName.packageName());
        assertEquals("char", charName.simpleName());
        assertEquals("char", charName.toString());
        assertNull(charName.immediatelyEnclosingClass());
        assertSame(charName, charName.topLevelClass());
        assertEquals(of("char"), charName.localNameComponents());
        assertEquals("char", charName.localName());

        final var threadName = JavaTypeName.create(Thread.class);
        assertEquals("java.lang", threadName.packageName());
        assertEquals("Thread", threadName.simpleName());
        assertEquals("java.lang.Thread", threadName.toString());
        assertNull(threadName.immediatelyEnclosingClass());
        assertTrue(threadName.canCreateEnclosed("Foo"));
        assertFalse(threadName.canCreateEnclosed("Thread"));
        assertEquals(threadName, JavaTypeName.create("java.lang", "Thread"));
        assertSame(threadName, threadName.topLevelClass());
        assertEquals(of("Thread"), threadName.localNameComponents());
        assertEquals("Thread", threadName.localName());

        final var stringName = threadName.createSibling("String");
        assertEquals("java.lang", stringName.packageName());
        assertEquals("String", stringName.simpleName());
        assertEquals("java.lang.String", stringName.toString());
        assertNull(stringName.immediatelyEnclosingClass());
        assertEquals(stringName, JavaTypeName.create("java.lang", "String"));

        final var enclosedName = threadName.createEnclosed("Foo");
        assertEquals("java.lang", enclosedName.packageName());
        assertEquals("Foo", enclosedName.simpleName());
        assertEquals("java.lang.Thread.Foo", enclosedName.toString());
        assertEquals(threadName, enclosedName.immediatelyEnclosingClass());
        assertSame(threadName, enclosedName.topLevelClass());
        assertEquals(of("Thread", "Foo"), enclosedName.localNameComponents());
        assertEquals("Thread.Foo", enclosedName.localName());

        final var uehName = JavaTypeName.create(Thread.UncaughtExceptionHandler.class);
        assertEquals("java.lang", uehName.packageName());
        assertEquals("UncaughtExceptionHandler", uehName.simpleName());
        assertEquals("java.lang.Thread.UncaughtExceptionHandler", uehName.toString());
        assertEquals(threadName, uehName.immediatelyEnclosingClass());
        assertTrue(uehName.canCreateEnclosed("Foo"));
        assertFalse(uehName.canCreateEnclosed("Thread"));
        assertFalse(uehName.canCreateEnclosed("UncaughtExceptionHandler"));

        final var siblingName = uehName.createSibling("Foo");
        assertEquals("java.lang", siblingName.packageName());
        assertEquals("Foo", siblingName.simpleName());
        assertEquals("java.lang.Thread.Foo", siblingName.toString());
        assertEquals(threadName, siblingName.immediatelyEnclosingClass());
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

    @Test
    void testCompareTo() {
        final var byteName = JavaTypeName.create(byte.class);
        final var charName = byteName.createSibling("char");
        final var threadName = JavaTypeName.create(Thread.class);
        final var stringName = threadName.createSibling("String");
        final var enclosedName = threadName.createEnclosed("Foo");
        final var uehName = JavaTypeName.create(Thread.UncaughtExceptionHandler.class);
        final var siblingName = uehName.createSibling("Bar");
        final var otherStringName = JavaTypeName.create("java.util", "String");

        assertEqualTo(byteName, byteName);
        assertLessThan(byteName, charName);
        assertGreaterThan(byteName, threadName);
        assertGreaterThan(byteName, stringName);
        assertGreaterThan(byteName, enclosedName);
        assertGreaterThan(byteName, uehName);
        assertGreaterThan(byteName, siblingName);
        assertGreaterThan(byteName, otherStringName);

        assertGreaterThan(charName, byteName);
        assertEqualTo(charName, charName);
        assertGreaterThan(charName, threadName);
        assertGreaterThan(charName, stringName);
        assertGreaterThan(charName, enclosedName);
        assertGreaterThan(charName, uehName);
        assertGreaterThan(charName, siblingName);
        assertGreaterThan(charName, otherStringName);

        assertLessThan(threadName, byteName);
        assertLessThan(threadName, charName);
        assertEqualTo(threadName, threadName);
        assertGreaterThan(threadName, stringName);
        assertGreaterThan(threadName, enclosedName);
        assertLessThan(threadName, uehName);
        assertGreaterThan(threadName, siblingName);
        assertGreaterThan(threadName, otherStringName);

        assertLessThan(stringName, byteName);
        assertLessThan(stringName, charName);
        assertLessThan(stringName, threadName);
        assertEqualTo(stringName, stringName);
        assertGreaterThan(stringName, enclosedName);
        assertLessThan(stringName, uehName);
        assertGreaterThan(stringName, siblingName);
        assertLessThan(stringName, otherStringName);

        assertLessThan(enclosedName, byteName);
        assertLessThan(enclosedName, charName);
        assertLessThan(enclosedName, threadName);
        assertLessThan(enclosedName, stringName);
        assertEqualTo(enclosedName, enclosedName);
        assertLessThan(enclosedName, uehName);
        assertGreaterThan(enclosedName, siblingName);
        assertLessThan(enclosedName, otherStringName);

        assertLessThan(uehName, byteName);
        assertLessThan(uehName, charName);
        assertGreaterThan(uehName, threadName);
        assertGreaterThan(uehName, stringName);
        assertGreaterThan(uehName, enclosedName);
        assertEqualTo(uehName, JavaTypeName.create(Thread.UncaughtExceptionHandler.class));
        assertGreaterThan(uehName, siblingName);
        assertGreaterThan(uehName, otherStringName);

        assertLessThan(siblingName, byteName);
        assertLessThan(siblingName, charName);
        assertLessThan(siblingName, threadName);
        assertLessThan(siblingName, stringName);
        assertLessThan(siblingName, enclosedName);
        assertLessThan(siblingName, uehName);
        assertEqualTo(siblingName, siblingName);
        assertLessThan(siblingName, otherStringName);

        assertLessThan(otherStringName, byteName);
        assertLessThan(otherStringName, charName);
        assertLessThan(otherStringName, threadName);
        assertGreaterThan(otherStringName, stringName);
        assertGreaterThan(otherStringName, enclosedName);
        assertLessThan(otherStringName, uehName);
        assertGreaterThan(otherStringName, siblingName);
        assertEqualTo(otherStringName, otherStringName);
    }

    private static void assertLessThan(final JavaTypeName receiver, final JavaTypeName other) {
        assertTrue(assertCompareTo(receiver, other) < 0);
    }

    private static void assertGreaterThan(final JavaTypeName receiver, final JavaTypeName other) {
        assertTrue(assertCompareTo(receiver, other) > 0);
    }

    private static void assertEqualTo(final JavaTypeName receiver, final JavaTypeName other) {
        assertEquals(0, assertCompareTo(receiver, other));
    }

    private static int assertCompareTo(final JavaTypeName receiver, final JavaTypeName other) {
        return receiver.compareTo(other);
    }
}
