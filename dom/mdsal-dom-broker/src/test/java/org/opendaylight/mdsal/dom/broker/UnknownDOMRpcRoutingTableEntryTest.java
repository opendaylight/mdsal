/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.opendaylight.mdsal.dom.api.DOMOperationImplementation;
import org.opendaylight.mdsal.dom.broker.util.TestModel;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class UnknownDOMRpcRoutingTableEntryTest extends TestUtils {
    private static final List<DOMOperationImplementation> TEST_LIST = new ArrayList<>();

    @Test
    public void basicTest() throws Exception {
        final Map<YangInstanceIdentifier, List<DOMOperationImplementation>> emptyImpls = new HashMap<>();
        final List<YangInstanceIdentifier> addList1 = new ArrayList<>();
        final List<YangInstanceIdentifier> addList2 = new ArrayList<>();
        final DOMOperationImplementation testClass = getTestOperationImplementation();
        final UnknownDOMRpcRoutingTableEntry test = new UnknownDOMRpcRoutingTableEntry(SchemaPath.ROOT, emptyImpls);

        TEST_LIST.add(testClass);
        emptyImpls.put(YangInstanceIdentifier.EMPTY, TEST_LIST);

        assertNotNull(test);
        assertNotNull(test.newInstance(emptyImpls));
        test.invokeRpc(TEST_CONTAINER, (result, throwable) -> {
            assertNotNull(throwable);
        });
        assertNotNull(test.getImplementations());
        assertEquals(test.getImplementations(YangInstanceIdentifier.EMPTY), TEST_LIST);
        assertTrue(test.containsContext(YangInstanceIdentifier.EMPTY));
        assertTrue(test.registeredIdentifiers().contains(YangInstanceIdentifier.EMPTY));

        addList1.add(YangInstanceIdentifier.EMPTY);
        addList1.add(YangInstanceIdentifier.of(TestModel.TEST_QNAME));
        addList2.add(YangInstanceIdentifier.of(TestModel.TEST2_QNAME));

        final AbstractDOMOperationRoutingTableEntry tst = test.add(testClass, addList1);
        final AbstractDOMOperationRoutingTableEntry tst1 = tst.add(testClass, addList2);
        final AbstractDOMOperationRoutingTableEntry tst2 = tst1.remove(testClass, addList1);

        assertEquals(1, test.getImplementations().size());
        assertEquals(2, tst.getImplementations().size());
        assertEquals(3, tst1.getImplementations().size());
        assertNotNull(tst2.getImplementations());
        assertEquals(2, tst2.getImplementations().size());
    }
}