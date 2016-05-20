/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import org.junit.Test;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

public class DOMDataTreeIdentifierTest {
    private static final QNameModule TEST_MODULE =
            QNameModule.create(URI.create("urn:opendaylight:params:xml:ns:yang:controller:md:sal:test:store"), null);

    private static final String REF_LISTS = "ref-lists";
    private static final String TEST_LISTS = "test-lists";
    private static final String COMPARE_FIRST_LISTS = "A-test-lists";
    private static final String COMPARE_SECOND_LISTS = "B-test-lists";

    private static final YangInstanceIdentifier REF_YII_IID = YangInstanceIdentifier.create(
            new YangInstanceIdentifier.NodeIdentifier(QName.create(TEST_MODULE, REF_LISTS)));
    private static final YangInstanceIdentifier TEST_YII_IID = YangInstanceIdentifier.create(
            new YangInstanceIdentifier.NodeIdentifier(QName.create(TEST_MODULE, TEST_LISTS)));

    private static final DOMDataTreeIdentifier REF_TREE =
            new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, REF_YII_IID);
    private static final DOMDataTreeIdentifier TEST_DIFF_TREE =
            new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, TEST_YII_IID);

    @Test
    public void constructTest() throws Exception {
        assertNotNull("Instantiation", new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, REF_YII_IID));
    }

    @Test(expected = NullPointerException.class)
    public void firstArgumentNullTest() throws Exception {
        new DOMDataTreeIdentifier(null, REF_YII_IID);
    }

    @Test(expected = NullPointerException.class)
    public void secondArgumentNullTest() throws Exception {
        new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, null);
    }

    @Test
    public void hashCodeTest() throws Exception {
        assertEquals("hashCode", REF_TREE.hashCode(),
                new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, REF_YII_IID).hashCode());

        assertNotEquals("hashCode", REF_TREE.hashCode(), TEST_DIFF_TREE.hashCode());
    }

    @Test
    public void equalsTest() throws Exception {
        assertTrue("Equals same",
                REF_TREE.equals(new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, REF_YII_IID)));

        assertFalse("Different DataStoreType",
                REF_TREE.equals(new DOMDataTreeIdentifier(LogicalDatastoreType.CONFIGURATION, REF_YII_IID)));

        assertTrue("Equals same instance", REF_TREE.equals(REF_TREE));

        assertFalse("Different object", REF_TREE.equals(new Object()));

        assertFalse("Different instance", REF_TREE.equals(TEST_DIFF_TREE));
    }

    @Test
    public void compareToTest() throws Exception {
        final YangInstanceIdentifier COMPARE_FIRST_IID = YangInstanceIdentifier.create(
                new YangInstanceIdentifier.NodeIdentifier(QName.create(TEST_MODULE, COMPARE_FIRST_LISTS)));
        final YangInstanceIdentifier COMPARE_SECOND_IID = YangInstanceIdentifier.create(
                new YangInstanceIdentifier.NodeIdentifier(QName.create(TEST_MODULE, COMPARE_SECOND_LISTS)));

        assertEquals("Compare same to same",
                REF_TREE.compareTo(new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, REF_YII_IID)), 0);

        assertNotEquals("Compare same to same with different datastore",
                REF_TREE.compareTo(new DOMDataTreeIdentifier(LogicalDatastoreType.CONFIGURATION, REF_YII_IID)), 0);

        assertEquals("Compare same to same with different datastore",
                new DOMDataTreeIdentifier(
                        LogicalDatastoreType.OPERATIONAL,
                        YangInstanceIdentifier.create(
                                YangInstanceIdentifier.NodeIdentifier.create(QName.create(TEST_MODULE, REF_LISTS)),
                                YangInstanceIdentifier.NodeIdentifier.create(QName.create(TEST_MODULE, TEST_LISTS))))
                .compareTo(REF_TREE), 1);

        assertTrue("Compare first to second",
                new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, COMPARE_FIRST_IID).compareTo(
                        new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, COMPARE_SECOND_IID)) < 0);

        assertTrue("Compare second to first",
                new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, COMPARE_SECOND_IID).compareTo(
                        new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, COMPARE_FIRST_IID)) > 0);
    }

    @Test
    public void containsTest() throws Exception {
        assertTrue("Contains",
                REF_TREE.contains(new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, REF_YII_IID)));

        assertEquals("Not contains", false, REF_TREE.contains(TEST_DIFF_TREE));
    }

    @Test
    public void toStringTest() throws Exception {
        assertTrue("ToString", REF_TREE.toString().contains(REF_TREE.getRootIdentifier().toString())
                            && REF_TREE.toString().contains(REF_TREE.getDatastoreType().toString()));
    }
}