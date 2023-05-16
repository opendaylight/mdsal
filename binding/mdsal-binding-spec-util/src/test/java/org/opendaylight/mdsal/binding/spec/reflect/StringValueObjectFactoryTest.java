/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.spec.reflect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import org.junit.Test;

public class StringValueObjectFactoryTest {
    private static final Lookup LOOKUP = MethodHandles.lookup();

    @Test
    public void createTest() throws Exception {
        final var stringValueObjectFactory = StringValueObjectFactory.create(LOOKUP, TestClass.class, "testTemplate");
        assertNotNull(stringValueObjectFactory);
        assertEquals("testTemplate", stringValueObjectFactory.getTemplate().toString());
    }

    @Test
    public void newInstanceTest() throws Exception {
        final var instance = StringValueObjectFactory.create(LOOKUP, TestClass.class, "testTemplate");
        assertEquals("instanceTest", instance.newInstance("instanceTest").toString());
    }

    @Test
    public void createTestNoConstructor() {
        assertThrows(IllegalArgumentException.class, () ->  StringValueObjectFactory.create(LOOKUP, Object.class, ""));
    }

    @Test
    public void createTestNoField() {
        assertThrows(IllegalArgumentException.class, () -> StringValueObjectFactory.create(LOOKUP, String.class, ""));
    }

    public static final class TestClass {
        @SuppressWarnings("checkstyle:memberName")
        private final String _value;

        public TestClass(final TestClass parent) {
            _value = parent._value;
        }

        public TestClass(final String value) {
            _value = value;
        }

        @Override
        public String toString() {
            return _value;
        }
    }
}
