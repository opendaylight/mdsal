/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

public class DOMDataTreePrefixTableTest {

    @Test
    public void basicTest() throws Exception {
        final DOMDataTreePrefixTable<Object> domDataTreePrefixTable = DOMDataTreePrefixTable.create();
        final Object testObject = new Object();
        final YangInstanceIdentifier yangInstanceIdentifier = YangInstanceIdentifier.of(QName.create("test"));
        final DOMDataTreeIdentifier domDataTreeIdentifier =
                new DOMDataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, yangInstanceIdentifier);

        domDataTreePrefixTable.store(domDataTreeIdentifier, testObject);
        assertEquals(QName.create("test"),
                domDataTreePrefixTable.lookup(domDataTreeIdentifier).getIdentifier().getNodeType());
        domDataTreePrefixTable.remove(domDataTreeIdentifier);
        assertNull(domDataTreePrefixTable.lookup(domDataTreeIdentifier).getIdentifier());

        final DOMDataTreeIdentifier invalidDOMDataTreeIdentifier =
                new DOMDataTreeIdentifier(LogicalDatastoreType.CONFIGURATION, yangInstanceIdentifier);
        domDataTreePrefixTable.remove(invalidDOMDataTreeIdentifier);
        assertNull(domDataTreePrefixTable.lookup(invalidDOMDataTreeIdentifier));
    }
}