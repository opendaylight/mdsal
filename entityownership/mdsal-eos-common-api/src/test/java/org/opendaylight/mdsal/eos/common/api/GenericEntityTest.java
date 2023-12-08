/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.common.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.concepts.HierarchicalIdentifier;

class GenericEntityTest {
    @Test
    void basicTest() {
        final var testClass = new TestClass();
        final var genericEntity = new GenericEntity<>("testType", testClass);
        final var genericEntityDiff = new GenericEntity<>("differentTestType", new TestClassDiff());

        assertEquals(TestClass.class, genericEntity.getIdentifier().getClass());
        assertEquals("testType", genericEntity.getType());
        assertTrue(genericEntity.toString().contains("testType"));
        assertNotEquals(genericEntity.hashCode(), genericEntityDiff.hashCode());
        assertTrue(genericEntity.equals(genericEntity));
        assertTrue(genericEntity.equals(new GenericEntity<>("testType", testClass)));
        assertFalse(genericEntity.equals(genericEntityDiff));
        assertFalse(genericEntity.equals(""));
        assertFalse(genericEntity.equals(new GenericEntity<>("differentTestType", testClass)));
    }

    private static final class TestClass implements HierarchicalIdentifier<TestClass> {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        @Override
        public boolean contains(final TestClass other) {
            return false;
        }
    }

    private static final class TestClassDiff implements HierarchicalIdentifier<TestClassDiff> {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        @Override
        public boolean contains(final TestClassDiff other) {
            return false;
        }
    }
}