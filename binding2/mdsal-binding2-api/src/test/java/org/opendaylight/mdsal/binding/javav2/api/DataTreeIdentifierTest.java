/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;

public class DataTreeIdentifierTest {

    private static final DataTreeIdentifier<TestTreeNode1> TEST_IDENTIFIER1 = DataTreeIdentifier.create(
            LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(TestTreeNode1.class));
    private static final DataTreeIdentifier<TestTreeNode2> TEST_IDENTIFIER2 = DataTreeIdentifier.create(
            LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(TestTreeNode2.class));

    @Test
    public void basicTest() throws Exception {
        assertEquals(LogicalDatastoreType.OPERATIONAL, TEST_IDENTIFIER1.getDatastoreType());
        assertEquals(InstanceIdentifier.create(TestTreeNode1.class), TEST_IDENTIFIER1.getRootIdentifier());
    }

    @Test
    public void containsTest() {
        assertTrue("Contains", TEST_IDENTIFIER1.contains(DataTreeIdentifier.create(LogicalDatastoreType.OPERATIONAL,
                InstanceIdentifier.create(TestTreeNode1.class))));
        assertFalse("Not Contains", TEST_IDENTIFIER1.contains(TEST_IDENTIFIER2));
    }

    @Test
    public void hashCodeTest() {
        assertEquals("hashCode", TEST_IDENTIFIER1.hashCode(), DataTreeIdentifier.create(
                LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(TestTreeNode1.class)).hashCode());
        assertNotEquals("hashCode", TEST_IDENTIFIER1.hashCode(), TEST_IDENTIFIER2.hashCode());
    }

    @Test
    public void equalsTest() {
        assertTrue("Equals", TEST_IDENTIFIER1.equals(TEST_IDENTIFIER1));
        assertTrue("Equals", TEST_IDENTIFIER1.equals(DataTreeIdentifier.create(LogicalDatastoreType.OPERATIONAL,
                InstanceIdentifier.create(TestTreeNode1.class))));
        assertFalse("Different", TEST_IDENTIFIER1.equals(DataTreeIdentifier.create(LogicalDatastoreType.CONFIGURATION,
                InstanceIdentifier.create(TestTreeNode1.class))));
        assertFalse("Different", TEST_IDENTIFIER1.equals(TEST_IDENTIFIER2));
        assertFalse("Equals null", TEST_IDENTIFIER1.equals(null));
        assertFalse("Different object", TEST_IDENTIFIER1.equals(new Object()));
    }

    private static class TestTreeNode1 implements TreeNode {
    }

    private static class TestTreeNode2 implements TreeNode {
    }
}
