/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opendaylight.mdsal.dom.broker.TestUtils.EXCEPTION_TEXT;
import static org.opendaylight.mdsal.dom.broker.TestUtils.TEST_CONTAINER;
import static org.opendaylight.mdsal.dom.broker.TestUtils.getTestRpcImplementation;

import com.google.common.util.concurrent.Futures;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementationNotAvailableException;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

class GlobalDOMRpcRoutingTableEntryTest {
    @Test
    void basicTest() {
        final var rpcImplementations = new HashMap<YangInstanceIdentifier, List<DOMRpcImplementation>>();
        final var rpcImplementation = new ArrayList<DOMRpcImplementation>();
        final var yangInstanceIdentifier = YangInstanceIdentifier.builder().build();

        final var globalDOMRpcRoutingTableEntry = new GlobalDOMRpcRoutingTableEntry(
            TestModel.TEST2_QNAME, new HashMap<>());
        rpcImplementation.add(getTestRpcImplementation());
        rpcImplementations.put(yangInstanceIdentifier, rpcImplementation);

        assertEquals(TestModel.TEST2_QNAME, globalDOMRpcRoutingTableEntry.getType());
        assertTrue(globalDOMRpcRoutingTableEntry.getImplementations().isEmpty());
        assertFalse(globalDOMRpcRoutingTableEntry.newInstance(rpcImplementations).getImplementations().isEmpty());
        assertTrue(globalDOMRpcRoutingTableEntry.newInstance(rpcImplementations).getImplementations().containsValue(
                rpcImplementation));

        final var future = OperationInvocation.invoke(
            globalDOMRpcRoutingTableEntry.newInstance(rpcImplementations), TEST_CONTAINER);

        final var ee = assertThrows(ExecutionException.class, () -> Futures.getDone(future));
        final var cause = assertInstanceOf(DOMRpcImplementationNotAvailableException.class, ee.getCause());
        assertThat(cause.getMessage()).contains(EXCEPTION_TEXT);
    }
}
