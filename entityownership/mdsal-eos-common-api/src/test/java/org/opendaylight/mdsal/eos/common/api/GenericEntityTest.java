/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.common.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import javax.annotation.Nonnull;
import org.junit.Test;
import org.opendaylight.mdsal.eos.common.api.GenericEntity;
import org.opendaylight.yangtools.concepts.Path;

public class GenericEntityTest {

    @Test
    public void basicTest() throws Exception {
        final TestClass testClass = new TestClass();
        final GenericEntity genericEntity = new GenericEntity<>("testType", testClass);
        final GenericEntity genericEntityDiff = new GenericEntity<>("differentTestType", new TestClassDiff());

        assertEquals(TestClass.class, genericEntity.getIdentifier().getClass());
        assertEquals("testType", genericEntity.getType());
        assertTrue(genericEntity.toString().contains("testType"));
        assertNotEquals(genericEntity.hashCode(), genericEntityDiff.hashCode());
        assertTrue(genericEntity.equals(genericEntity));
        assertTrue(genericEntity.equals(new GenericEntity<>("testType", testClass)));
        assertFalse(genericEntity.equals(genericEntityDiff));
        assertFalse(genericEntity.equals(new String()));
        assertFalse(genericEntity.equals(new GenericEntity<>("differentTestType", testClass)));
    }

    private final class TestClass implements Path {
        @Override
        public boolean contains(@Nonnull Path other) {
            return false;
        }
    }

    private final class TestClassDiff implements Path {
        @Override
        public boolean contains(@Nonnull Path other) {
            return false;
        }
    }
}