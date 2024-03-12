/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.jupiter.api.Test;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

class DOMDataTreeIdentifierTest {
    private static final String REF_LISTS = "ref-lists";
    private static final String TEST_LISTS = "test-lists";
    private static final String COMPARE_FIRST_LISTS = "A-test-lists";
    private static final String COMPARE_SECOND_LISTS = "B-test-lists";
    private static final QNameModule TEST_MODULE =
        QNameModule.of("urn:opendaylight:params:xml:ns:yang:controller:md:sal:test:store");
    private static final YangInstanceIdentifier REF_YII_IID =
        YangInstanceIdentifier.of(QName.create(TEST_MODULE, REF_LISTS));
    private static final YangInstanceIdentifier TEST_YII_IID =
        YangInstanceIdentifier.of(QName.create(TEST_MODULE, TEST_LISTS));
    private static final DOMDataTreeIdentifier REF_TREE =
        DOMDataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL, REF_YII_IID);
    private static final DOMDataTreeIdentifier TEST_DIFF_TREE =
        DOMDataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL,TEST_YII_IID);

    @Test
    void constructTest() {
        assertNotNull(DOMDataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL, REF_YII_IID), "Instantiation");
    }

    @Test
    void hashCodeTest() {
        assertEquals(REF_TREE.hashCode(),
            DOMDataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL, REF_YII_IID).hashCode());
        assertNotEquals(REF_TREE.hashCode(), TEST_DIFF_TREE.hashCode());
    }

    @Test
    void equalsTest() {
        assertEquals(REF_TREE, DOMDataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL, REF_YII_IID));
        assertNotEquals(REF_TREE, DOMDataTreeIdentifier.of(LogicalDatastoreType.CONFIGURATION, REF_YII_IID));
        assertEquals(REF_TREE, REF_TREE);
        assertNotEquals(REF_TREE, new Object());
        assertNotEquals(REF_TREE, TEST_DIFF_TREE);
    }

    @Test
    void compareToTest() {
        final var compareFirstIid = YangInstanceIdentifier.of(QName.create(TEST_MODULE, COMPARE_FIRST_LISTS));
        final var compareSecondIid = YangInstanceIdentifier.of(QName.create(TEST_MODULE, COMPARE_SECOND_LISTS));

        assertEquals(0, REF_TREE.compareTo(DOMDataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL, REF_YII_IID)));
        assertNotEquals(0,
            REF_TREE.compareTo(DOMDataTreeIdentifier.of(LogicalDatastoreType.CONFIGURATION, REF_YII_IID)));
        assertEquals(1, DOMDataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL,
            YangInstanceIdentifier.of(QName.create(TEST_MODULE, REF_LISTS), QName.create(TEST_MODULE, TEST_LISTS)))
                .compareTo(REF_TREE));
        assertTrue(DOMDataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL, compareFirstIid)
            .compareTo(DOMDataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL, compareSecondIid)) < 0);
        assertTrue(DOMDataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL, compareSecondIid)
            .compareTo(DOMDataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL, compareFirstIid)) > 0);
    }

    @Test
    void containsTest() {
        assertTrue(REF_TREE.contains(DOMDataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL, REF_YII_IID)));
        assertEquals(false, REF_TREE.contains(TEST_DIFF_TREE));
    }

    @Test
    void toStringTest() {
        assertEquals("DOMDataTreeIdentifier{datastore=OPERATIONAL, "
            + "root=/(urn:opendaylight:params:xml:ns:yang:controller:md:sal:test:store)ref-lists}",
            REF_TREE.toString());
    }

    @Test
    void serializationTest() throws Exception {
        final var bos = new ByteArrayOutputStream();
        try (var oos = new ObjectOutputStream(bos)) {
            oos.writeObject(REF_TREE);
        }

        final var bytes = bos.toByteArray();
        assertEquals(275, bytes.length);

        try (var ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            assertEquals(REF_TREE, ois.readObject());
        }
    }
}
