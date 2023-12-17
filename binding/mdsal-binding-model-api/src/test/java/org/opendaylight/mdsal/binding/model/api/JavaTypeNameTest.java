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
        // 'byte'
        final var byteName = JavaTypeName.create(byte.class);
        // 'char'
        final var charName = byteName.createSibling("char");
        final var jlThread = JavaTypeName.create("java.lang", "Thread");
        final var jlString = JavaTypeName.create("java.lang", "String");
        final var jlThreadFoo = jlThread.createEnclosed("Foo");
        final var jlThreadFooBar = jlThreadFoo.createEnclosed("Bar");
        final var jlThreadUEH = jlThread.createEnclosed("UncaughtExceptionHandler");
        final var juString = JavaTypeName.create("java.util", "String");

        assertEqualTo(byteName, byteName);
        assertLessThan(byteName, charName);
        assertLessThan(byteName, jlThread);
        assertLessThan(byteName, jlString);
        assertLessThan(byteName, jlThreadFoo);
        assertLessThan(byteName, jlThreadUEH);
        assertLessThan(byteName, jlThreadFooBar);
        assertLessThan(byteName, juString);

        assertGreaterThan(charName, byteName);
        assertEqualTo(charName, charName);
        assertLessThan(charName, jlThread);
        assertLessThan(charName, jlString);
        assertLessThan(charName, jlThreadFoo);
        assertLessThan(charName, jlThreadUEH);
        assertLessThan(charName, jlThreadFooBar);
        assertLessThan(charName, juString);

        assertGreaterThan(jlThread, byteName);
        assertGreaterThan(jlThread, charName);
        assertEqualTo(jlThread, jlThread);
        assertGreaterThan(jlThread, jlString);
        assertLessThan(jlThread, jlThreadFoo);
        assertLessThan(jlThread, jlThreadUEH);
        assertLessThan(jlThread, jlThreadFooBar);
        assertLessThan(jlThread, juString);

        assertGreaterThan(jlString, byteName);
        assertGreaterThan(jlString, charName);
        assertLessThan(jlString, jlThread);
        assertEqualTo(jlString, jlString);
        assertLessThan(jlString, jlThreadFoo);
        assertLessThan(jlString, jlThreadUEH);
        assertLessThan(jlString, jlThreadFooBar);
        assertLessThan(jlString, juString);

        assertGreaterThan(jlThreadFoo, byteName);
        assertGreaterThan(jlThreadFoo, charName);
        assertGreaterThan(jlThreadFoo, jlThread);
        assertGreaterThan(jlThreadFoo, jlString);
        assertEqualTo(jlThreadFoo, jlThreadFoo);
        assertLessThan(jlThreadFoo, jlThreadUEH);
        assertLessThan(jlThreadFoo, jlThreadFooBar);
        assertLessThan(jlThreadFoo, juString);

        assertGreaterThan(jlThreadUEH, byteName);
        assertGreaterThan(jlThreadUEH, charName);
        assertGreaterThan(jlThreadUEH, jlThread);
        assertGreaterThan(jlThreadUEH, jlString);
        assertGreaterThan(jlThreadUEH, jlThreadFoo);
        assertEqualTo(jlThreadUEH, JavaTypeName.create(Thread.UncaughtExceptionHandler.class));
        assertGreaterThan(jlThreadUEH, jlThreadFooBar);
        assertLessThan(jlThreadUEH, juString);

        assertGreaterThan(jlThreadFooBar, byteName);
        assertGreaterThan(jlThreadFooBar, charName);
        assertEqualTo(jlThreadFooBar, jlThread);
        assertEqualTo(jlThreadFooBar, jlString);
        assertEqualTo(jlThreadFooBar, jlThreadFoo);
        assertEqualTo(jlThreadFooBar, jlThreadUEH);
        assertEqualTo(jlThreadFooBar, jlThreadFooBar);
        assertLessThan(jlThreadFooBar, juString);

        assertGreaterThan(juString, byteName);
        assertGreaterThan(juString, charName);
        assertEqualTo(juString, jlThread);
        assertEqualTo(juString, jlString);
        assertEqualTo(juString, jlThreadFoo);
        assertEqualTo(juString, jlThreadUEH);
        assertEqualTo(juString, jlThreadFooBar);
        assertEqualTo(juString, juString);
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
