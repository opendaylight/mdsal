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
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

public class DOMDataTreePrefixTableEntryTest {
    @Test
    public void basicTest() throws Exception {
        final DOMDataTreePrefixTableEntry<Object> domDataTreePrefixTableEntry = new DOMDataTreePrefixTableEntry<>();
        final NodeIdentifier pathArgument = new NodeIdentifier(QName.create("", "pathArgument"));
        final Object testObject = new Object();
        final YangInstanceIdentifier yangInstanceIdentifier = YangInstanceIdentifier.of(QName.create("", "test"));

        assertEquals(pathArgument, new DOMDataTreePrefixTableEntry<>(pathArgument).getIdentifier());
        domDataTreePrefixTableEntry.store(yangInstanceIdentifier, testObject);
        assertEquals(QName.create("", "test"),
                domDataTreePrefixTableEntry.lookup(yangInstanceIdentifier).getIdentifier().getNodeType());
        domDataTreePrefixTableEntry.remove(yangInstanceIdentifier);
        domDataTreePrefixTableEntry.remove(yangInstanceIdentifier);
        assertNull(domDataTreePrefixTableEntry.lookup(yangInstanceIdentifier));
    }
}