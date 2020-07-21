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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementationNotAvailableException;
import org.opendaylight.mdsal.dom.broker.DOMRpcRouter.OperationInvocation;
import org.opendaylight.mdsal.dom.broker.util.TestModel;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

public class GlobalDOMRpcRoutingTableEntryTest extends TestUtils {
    @Test
    public void basicTest() {
        final Map<YangInstanceIdentifier, List<DOMRpcImplementation>> rpcImplementations = new HashMap<>();
        final List<DOMRpcImplementation> rpcImplementation = new ArrayList<>();
        final RpcDefinition rpcDefinition = mock(RpcDefinition.class);
        final YangInstanceIdentifier yangInstanceIdentifier = YangInstanceIdentifier.builder().build();

        doReturn(TestModel.TEST2_QNAME).when(rpcDefinition).getQName();
        final GlobalDOMRpcRoutingTableEntry globalDOMRpcRoutingTableEntry = new GlobalDOMRpcRoutingTableEntry(
                rpcDefinition, new HashMap<>());
        rpcImplementation.add(getTestRpcImplementation());
        rpcImplementations.put(yangInstanceIdentifier, rpcImplementation);

        assertEquals(Absolute.of(TestModel.TEST2_QNAME), globalDOMRpcRoutingTableEntry.getType());
        assertTrue(globalDOMRpcRoutingTableEntry.getImplementations().isEmpty());
        assertFalse(globalDOMRpcRoutingTableEntry.newInstance(rpcImplementations).getImplementations().isEmpty());
        assertTrue(globalDOMRpcRoutingTableEntry.newInstance(rpcImplementations).getImplementations().containsValue(
                rpcImplementation));

        final ListenableFuture<?> future = OperationInvocation.invoke(
            globalDOMRpcRoutingTableEntry.newInstance(rpcImplementations), TEST_CONTAINER);

        final ExecutionException ex = assertThrows(ExecutionException.class, () -> future.get(5, TimeUnit.SECONDS));
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(DOMRpcImplementationNotAvailableException.class));
        assertThat(cause.getMessage(), containsString(EXCEPTION_TEXT));
    }
}
