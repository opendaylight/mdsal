/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.mdsal.dom.broker.TestUtils.EXCEPTION_TEXT;
import static org.opendaylight.mdsal.dom.broker.TestUtils.TEST_CONTAINER;
import static org.opendaylight.mdsal.dom.broker.TestUtils.getTestRpcImplementation;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementationNotAvailableException;
import org.opendaylight.mdsal.dom.broker.DOMRpcRouter.OperationInvocation;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

public class GlobalDOMRpcRoutingTableEntryTest {
    @Test
    public void basicTest() {
        final Map<YangInstanceIdentifier, List<DOMRpcImplementation>> rpcImplementations = new HashMap<>();
        final List<DOMRpcImplementation> rpcImplementation = new ArrayList<>();
        final YangInstanceIdentifier yangInstanceIdentifier = YangInstanceIdentifier.builder().build();

        final GlobalDOMRpcRoutingTableEntry globalDOMRpcRoutingTableEntry = new GlobalDOMRpcRoutingTableEntry(
            TestModel.TEST2_QNAME, new HashMap<>());
        rpcImplementation.add(getTestRpcImplementation());
        rpcImplementations.put(yangInstanceIdentifier, rpcImplementation);

        assertEquals(TestModel.TEST2_QNAME, globalDOMRpcRoutingTableEntry.getType());
        assertTrue(globalDOMRpcRoutingTableEntry.getImplementations().isEmpty());
        assertFalse(globalDOMRpcRoutingTableEntry.newInstance(rpcImplementations).getImplementations().isEmpty());
        assertTrue(globalDOMRpcRoutingTableEntry.newInstance(rpcImplementations).getImplementations().containsValue(
                rpcImplementation));

        final ListenableFuture<?> future = OperationInvocation.invoke(
            globalDOMRpcRoutingTableEntry.newInstance(rpcImplementations), TEST_CONTAINER);

        final var cause = assertThrows(ExecutionException.class, () -> Futures.getDone(future)).getCause();
        assertThat(cause, instanceOf(DOMRpcImplementationNotAvailableException.class));
        assertThat(cause.getMessage(), containsString(EXCEPTION_TEXT));
    }
}
