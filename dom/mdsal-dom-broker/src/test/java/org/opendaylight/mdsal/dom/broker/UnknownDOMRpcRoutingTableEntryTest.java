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
import static org.opendaylight.mdsal.dom.broker.TestUtils.TEST_CONTAINER;
import static org.opendaylight.mdsal.dom.broker.TestUtils.getTestRpcImplementation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

public class UnknownDOMRpcRoutingTableEntryTest {
    private static final List<DOMRpcImplementation> TEST_LIST = new ArrayList<>();

    @Test
    public void basicTest() {
        final Map<YangInstanceIdentifier, List<DOMRpcImplementation>> emptyImpls = new HashMap<>();
        final List<YangInstanceIdentifier> addList1 = new ArrayList<>();
        final List<YangInstanceIdentifier> addList2 = new ArrayList<>();
        final DOMRpcImplementation testClass = getTestRpcImplementation();
        final UnknownDOMRpcRoutingTableEntry test =
                new UnknownDOMRpcRoutingTableEntry(TestModel.TEST_QNAME, emptyImpls);

        TEST_LIST.add(testClass);
        emptyImpls.put(YangInstanceIdentifier.of(), TEST_LIST);

        assertNotNull(test);
        assertNotNull(test.newInstance(emptyImpls));
        assertNotNull(OperationInvocation.invoke(test, TEST_CONTAINER));
        assertNotNull(test.getImplementations());
        assertEquals(test.getImplementations(YangInstanceIdentifier.of()), TEST_LIST);
        assertTrue(test.containsContext(YangInstanceIdentifier.of()));
        assertTrue(test.registeredIdentifiers().contains(YangInstanceIdentifier.of()));

        addList1.add(YangInstanceIdentifier.of());
        addList1.add(YangInstanceIdentifier.of(TestModel.TEST_QNAME));
        addList2.add(YangInstanceIdentifier.of(TestModel.TEST2_QNAME));

        final AbstractDOMRpcRoutingTableEntry tst = (AbstractDOMRpcRoutingTableEntry) test.add(testClass, addList1);
        final AbstractDOMRpcRoutingTableEntry tst1 = (AbstractDOMRpcRoutingTableEntry) tst.add(testClass, addList2);
        final AbstractDOMRpcRoutingTableEntry tst2 = (AbstractDOMRpcRoutingTableEntry) tst1.remove(testClass, addList1);

        assertEquals(1, test.getImplementations().size());
        assertEquals(2, tst.getImplementations().size());
        assertEquals(3, tst1.getImplementations().size());
        assertNotNull(tst2.getImplementations());
        assertEquals(2, tst2.getImplementations().size());
    }
}