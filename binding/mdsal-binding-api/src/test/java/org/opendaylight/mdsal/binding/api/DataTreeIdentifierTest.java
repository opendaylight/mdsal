/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.Test;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class DataTreeIdentifierTest {

    private static final DataTreeIdentifier<TestDataObject1> TEST_IDENTIFIER1 = DataTreeIdentifier.create(
            LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(TestDataObject1.class));
    private static final DataTreeIdentifier<TestDataObject2> TEST_IDENTIFIER2 = DataTreeIdentifier.create(
            LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(TestDataObject2.class));

    @Test
    public void basicTest() throws Exception {
        assertEquals(LogicalDatastoreType.OPERATIONAL, TEST_IDENTIFIER1.getDatastoreType());
        assertEquals(InstanceIdentifier.create(TestDataObject1.class), TEST_IDENTIFIER1.getRootIdentifier());
    }

    @Test
    public void containsTest() {
        assertTrue("Contains", TEST_IDENTIFIER1.contains(DataTreeIdentifier.create(LogicalDatastoreType.OPERATIONAL,
                InstanceIdentifier.create(TestDataObject1.class))));
        assertFalse("Not Contains", TEST_IDENTIFIER1.contains(TEST_IDENTIFIER2));
    }

    @Test
    public void hashCodeTest() {
        assertEquals("hashCode", TEST_IDENTIFIER1.hashCode(), DataTreeIdentifier.create(
                LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(TestDataObject1.class)).hashCode());
        assertNotEquals("hashCode", TEST_IDENTIFIER1.hashCode(), TEST_IDENTIFIER2.hashCode());
    }

    @Test
    public void equalsTest() {
        assertTrue("Equals", TEST_IDENTIFIER1.equals(TEST_IDENTIFIER1));
        assertTrue("Equals", TEST_IDENTIFIER1.equals(DataTreeIdentifier.create(LogicalDatastoreType.OPERATIONAL,
                InstanceIdentifier.create(TestDataObject1.class))));
        assertFalse("Different", TEST_IDENTIFIER1.equals(DataTreeIdentifier.create(LogicalDatastoreType.CONFIGURATION,
                InstanceIdentifier.create(TestDataObject1.class))));
        assertFalse("Different", TEST_IDENTIFIER1.equals(TEST_IDENTIFIER2));
        assertFalse("Equals null", TEST_IDENTIFIER1.equals(null));
        assertFalse("Different object", TEST_IDENTIFIER1.equals(new Object()));
    }

    @Test
    void serializationTest() throws Exception {
        final var bos = new ByteArrayOutputStream();
        try (var oos = new ObjectOutputStream(bos)) {
            oos.writeObject(TEST_IDENTIFIER1);
        }

        final var bytes = bos.toByteArray();
        assertEquals(728, bytes.length);

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
