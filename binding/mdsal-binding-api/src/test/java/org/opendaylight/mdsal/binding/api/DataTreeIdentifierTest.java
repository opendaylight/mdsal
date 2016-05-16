package org.opendaylight.mdsal.binding.api;

import org.junit.Test;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import static org.junit.Assert.*;

/**
 * Created by peter.nosal
 */
public class DataTreeIdentifierTest {
    private static final DataTreeIdentifier<TestDataObject1> TEST_IDENTIFIER1 =
            DataTreeIdentifier.create(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(TestDataObject1.class));
    private static final DataTreeIdentifier<TestDataObject2> TEST_IDENTIFIER2 =
            DataTreeIdentifier.create(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(TestDataObject2.class));

    @Test
    public void containsTest(){
        assertTrue("Contains", TEST_IDENTIFIER1.contains(
                DataTreeIdentifier.create(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(TestDataObject1.class))));

        assertFalse("Not Contains", TEST_IDENTIFIER1.contains(TEST_IDENTIFIER2));
    }

    @Test
    public void hashCodeTest(){
        assertEquals("hashCode", TEST_IDENTIFIER1.hashCode(),
                DataTreeIdentifier.create(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(TestDataObject1.class)).hashCode());

        assertNotEquals("hashCode", TEST_IDENTIFIER1.hashCode(), TEST_IDENTIFIER2.hashCode());
    }

    @Test
    public void equalsTest(){
        assertTrue("Equals", TEST_IDENTIFIER1.equals(
                DataTreeIdentifier.create(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(TestDataObject1.class))));

        assertFalse("Different", TEST_IDENTIFIER1.equals(
                DataTreeIdentifier.create(LogicalDatastoreType.CONFIGURATION, InstanceIdentifier.create(TestDataObject1.class))));

        assertFalse("Different", TEST_IDENTIFIER1.equals(TEST_IDENTIFIER2));
    }

    private static class TestDataObject1 implements DataObject {
        @Override
        public Class<? extends DataContainer> getImplementedInterface() {
            return null;
        }
    }

    private static class TestDataObject2 implements DataObject {
        @Override
        public Class<? extends DataContainer> getImplementedInterface() {
            return null;
        }
    }
}