/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opendaylight.mdsal.binding.model.api.TypeName;

public class TypeNameTest {

    @Rule
    public ExpectedException expException = ExpectedException.none();

    @Test
    public void testHashCode() {
        TypeName baseType1 = TypeName.create("org.opendaylight.yangtools.test", "Test");
        TypeName baseType2 = TypeName.create("org.opendaylight.yangtools.test", "Test2");
        assertNotEquals(baseType1.hashCode(), baseType2.hashCode());
    }

    @Test
    public void testToString() {
        TypeName baseType = TypeName.create("org.opendaylight.yangtools.test", "Test");
        assertTrue(baseType.toString().contains("org.opendaylight.yangtools.test.Test"));
        baseType = TypeName.create(byte[].class);
        assertTrue(baseType.toString().contains("byte[]"));
    }

    @Test
    public void testEquals() {
        TypeName baseType1 = TypeName.create("org.opendaylight.yangtools.test", "Test");
        TypeName baseType2 = TypeName.create("org.opendaylight.yangtools.test", "Test2");
        TypeName baseType3 = null;
        TypeName baseType4 = TypeName.create("org.opendaylight.yangtools.test", "Test");
        TypeName baseType5 = TypeName.create("org.opendaylight.yangtools.test1", "Test");

        assertFalse(baseType1.equals(baseType2));
        assertFalse(baseType1.equals(baseType3));
        assertTrue(baseType1.equals(baseType4));
        assertFalse(baseType1.equals(baseType5));
        assertFalse(baseType1.equals(null));
    }
}
