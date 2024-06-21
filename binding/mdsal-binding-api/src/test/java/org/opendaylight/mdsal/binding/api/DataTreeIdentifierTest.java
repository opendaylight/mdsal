/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.jupiter.api.Test;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.binding.ChildOf;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataRoot;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class DataTreeIdentifierTest {
    private static final DataTreeIdentifier<TestDataObject1> TEST_IDENTIFIER1 =
        DataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(TestDataObject1.class));
    private static final DataTreeIdentifier<TestDataObject2> TEST_IDENTIFIER2 =
        DataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(TestDataObject2.class));

    @Test
    void basicTest() throws Exception {
        assertEquals(LogicalDatastoreType.OPERATIONAL, TEST_IDENTIFIER1.datastore());
        assertEquals(InstanceIdentifier.create(TestDataObject1.class), TEST_IDENTIFIER1.path());
    }

    @Test
    void containsTest() {
        assertTrue(TEST_IDENTIFIER1.contains(
            DataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(TestDataObject1.class))));
        assertFalse(TEST_IDENTIFIER1.contains(TEST_IDENTIFIER2));
    }

    @Test
    void hashCodeTest() {
        assertEquals(TEST_IDENTIFIER1.hashCode(),
            DataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(TestDataObject1.class))
                .hashCode());
        assertNotEquals(TEST_IDENTIFIER1.hashCode(), TEST_IDENTIFIER2.hashCode());
    }

    @Test
    void equalsTest() {
        assertEquals(TEST_IDENTIFIER1, TEST_IDENTIFIER1);
        assertEquals(TEST_IDENTIFIER1,
            DataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL,InstanceIdentifier.create(TestDataObject1.class)));
        assertNotEquals(TEST_IDENTIFIER1,
            DataTreeIdentifier.of(LogicalDatastoreType.CONFIGURATION,
                InstanceIdentifier.create(TestDataObject1.class)));
        assertNotEquals(TEST_IDENTIFIER1, TEST_IDENTIFIER2);
        assertNotEquals(TEST_IDENTIFIER1, null);
        assertNotEquals(TEST_IDENTIFIER1, new Object());
    }

    @Test
    void serializationTest() throws Exception {
        final var bos = new ByteArrayOutputStream();
        try (var oos = new ObjectOutputStream(bos)) {
            oos.writeObject(TEST_IDENTIFIER1);
        }

        final var bytes = bos.toByteArray();
        assertEquals(327, bytes.length);

        try (var ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            assertEquals(TEST_IDENTIFIER1, ois.readObject());
        }
    }

    private interface TestDataObject1 extends ChildOf<DataRoot> {
        @Override
        default Class<? extends DataObject> implementedInterface() {
            return TestDataObject1.class;
        }
    }

    private interface TestDataObject2 extends ChildOf<DataRoot> {
        @Override
        default Class<? extends DataObject> implementedInterface() {
            return TestDataObject2.class;
        }
    }
}
