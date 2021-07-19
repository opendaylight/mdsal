/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import org.junit.BeforeClass;
import org.junit.Test;

public class UnionValueOptionContextTest {
    private static UnionValueOptionContext TEST_UVOC_1;
    private static UnionValueOptionContext TEST_UVOC_2;

    @BeforeClass
    public static void beforeClass() throws Exception {
        final Lookup lookup = MethodHandles.lookup();
        final Method methodFoo1 = TestDataObject1.class.getMethod("foo");
        final Method methodFoo2 = TestDataObject2.class.getMethod("foo");
        TEST_UVOC_1 = new UnionValueOptionContext(TestUnion.class, TestDataObject1.class, methodFoo1,
            SchemaUnawareCodec.NOOP_CODEC, lookup);
        TEST_UVOC_2 = new UnionValueOptionContext(TestUnion.class, TestDataObject2.class, methodFoo2,
            SchemaUnawareCodec.NOOP_CODEC, lookup);
    }

    @Test
    public void hashCodeTest() throws Exception {
        final Method methodFoo1 = TestDataObject1.class.getMethod("foo");
        final UnionValueOptionContext test_uvoc = new UnionValueOptionContext(TestUnion.class, TestDataObject1.class,
            methodFoo1, SchemaUnawareCodec.NOOP_CODEC, MethodHandles.lookup());

        assertEquals("HashCode", test_uvoc.hashCode(), TEST_UVOC_1.hashCode());
        assertNotEquals("HashCode", TEST_UVOC_1.hashCode(), TEST_UVOC_2.hashCode());
    }

    @Test
    public void equalsTest() throws Exception {
        final Method methodFoo1 = TestDataObject1.class.getMethod("foo");
        final UnionValueOptionContext test_uvoc = new UnionValueOptionContext(TestUnion.class, TestDataObject1.class,
            methodFoo1, SchemaUnawareCodec.NOOP_CODEC, MethodHandles.lookup());

        assertEquals(TEST_UVOC_1, test_uvoc);
        assertNotEquals(TEST_UVOC_1, TEST_UVOC_2);
    }

    public static final class TestDataObject1 {
        public void foo() {

        }
    }

    public static final class TestDataObject2 {
        public void foo() {

        }
    }

    public static final class TestUnion {
        public TestUnion(final TestDataObject1 arg) {

        }

        public TestUnion(final TestDataObject2 arg) {

        }
    }
}
