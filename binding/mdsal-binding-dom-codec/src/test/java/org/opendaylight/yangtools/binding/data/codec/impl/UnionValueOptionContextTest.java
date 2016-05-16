package org.opendaylight.yangtools.binding.data.codec.impl;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * Created by peter.nosal
 */
public class UnionValueOptionContextTest {
    private static UnionValueOptionContext TEST_UVOC_1;
    private static UnionValueOptionContext TEST_UVOC_2;

    @Before
    public void setUp() throws Exception {
        final Method methodFoo1 = TestDataObject1.class.getMethod("foo");
        TEST_UVOC_1 = new UnionValueOptionContext(
                TestDataObject1.class, methodFoo1, ValueTypeCodec.EMPTY_CODEC);

        final Method methodFoo2 = TestDataObject1.class.getMethod("foo");
        TEST_UVOC_2 = new UnionValueOptionContext(
                TestDataObject2.class, methodFoo2, ValueTypeCodec.EMPTY_CODEC);
    }

    @Test
    public void hashCodeTest() throws Exception {
        final Method methodFoo1 = TestDataObject1.class.getMethod("foo");
        final UnionValueOptionContext TEST_UVOC =
                new UnionValueOptionContext(TestDataObject1.class, methodFoo1, ValueTypeCodec.EMPTY_CODEC);

        assertEquals("HashCode", TEST_UVOC.hashCode(), TEST_UVOC_1.hashCode());
        assertNotEquals("HashCode", TEST_UVOC_1.hashCode(), TEST_UVOC_2.hashCode());
    }

    @Test
    public void equalsTest() throws Exception {
        final Method methodFoo1 = TestDataObject1.class.getMethod("foo");
        final UnionValueOptionContext TEST_UVOC =
                new UnionValueOptionContext(TestDataObject1.class, methodFoo1, ValueTypeCodec.EMPTY_CODEC);

        assertTrue("Equals", TEST_UVOC_1.equals(TEST_UVOC));
        assertFalse("Not equals", TEST_UVOC_1.equals(TEST_UVOC_2));
    }

    protected static final class TestDataObject1{
        public void foo(){}
    }

    protected static final class TestDataObject2{
        public void foo(){}
    }
}