/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

class DOMDataTreePrefixTableTest {
    @Test
    void basicTest() {
        final var domDataTreePrefixTable = DOMDataTreePrefixTable.create();
        final var testObject = new Object();
        final var yangInstanceIdentifier = YangInstanceIdentifier.of(QName.create("", "test"));
        final var domDataTreeIdentifier =
                new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, yangInstanceIdentifier);

        domDataTreePrefixTable.store(domDataTreeIdentifier, testObject);
        assertEquals(QName.create("", "test"),
                domDataTreePrefixTable.lookup(domDataTreeIdentifier).getIdentifier().getNodeType());
        domDataTreePrefixTable.remove(domDataTreeIdentifier);
        assertNull(domDataTreePrefixTable.lookup(domDataTreeIdentifier));

        final var invalidDOMDataTreeIdentifier =
                new DOMDataTreeIdentifier(LogicalDatastoreType.CONFIGURATION, yangInstanceIdentifier);
        domDataTreePrefixTable.remove(invalidDOMDataTreeIdentifier);
        assertNull(domDataTreePrefixTable.lookup(invalidDOMDataTreeIdentifier));
    }
}