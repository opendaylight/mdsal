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
import java.util.HexFormat;
import org.junit.jupiter.api.Test;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.binding.ChildOf;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataRoot;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class DataDataTreeIdentifierTest {
    private static final DataTreeIdentifier<TestDataObject1> TEST_IDENTIFIER1 = DataTreeIdentifier.of(
        LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(TestDataObject1.class));
    private static final DataTreeIdentifier<TestDataObject2> TEST_IDENTIFIER2 = DataTreeIdentifier.of(
        LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(TestDataObject2.class));

    @Test
    void basicTest() {
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
        assertEquals("""
            aced0005737200286f72672e6f70656e6461796c696768742e6d6473616c2e62696e64696e672e6170692e445449763100000000000\
            000010c000078707701017372002c6f72672e6f70656e6461796c696768742e79616e67746f6f6c732e79616e672e62696e64696e67\
            2e4949763500000000000000010c000078707704000000017372002b6f72672e6f70656e6461796c696768742e79616e67746f6f6c7\
            32e62696e64696e672e4e6f64655374657000000000000000000200024c000863617365547970657400114c6a6176612f6c616e672f\
            436c6173733b4c00047479706571007e00057870707672004d6f72672e6f70656e6461796c696768742e6d6473616c2e62696e64696\
            e672e6170692e4461746144617461547265654964656e746966696572546573742454657374446174614f626a656374310000000000\
            00000000000078707878""", HexFormat.of().formatHex(bytes));

        try (var ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            assertEquals(TEST_IDENTIFIER1, ois.readObject());
        }
    }

    private interface TestDataObject1 extends ChildOf<DataRoot<?>> {
        @Override
        default Class<? extends DataObject> implementedInterface() {
            return TestDataObject1.class;
        }
    }

    private interface TestDataObject2 extends ChildOf<DataRoot<?>> {
        @Override
        default Class<? extends DataObject> implementedInterface() {
            return TestDataObject2.class;
        }
    }
}
