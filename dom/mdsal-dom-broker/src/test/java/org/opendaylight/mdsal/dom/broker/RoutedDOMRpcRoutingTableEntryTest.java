/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.opendaylight.mdsal.dom.broker.TestUtils.TEST_CHILD;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementationNotAvailableException;
import org.opendaylight.mdsal.dom.broker.DOMRpcRouter.OperationInvocation;
import org.opendaylight.mdsal.dom.broker.util.TestModel;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class RoutedDOMRpcRoutingTableEntryTest {
    @Mock
    public RpcDefinition rpcDefinition;

    private RoutedDOMRpcRoutingTableEntry entry;

    @Before
    public void before() {
        doReturn(TestModel.TEST2_QNAME).when(rpcDefinition).getQName();
        entry = new RoutedDOMRpcRoutingTableEntry(rpcDefinition, TestModel.TEST_PATH, Map.of());
    }

    @Test
    public void testNewInstance() {
        final RoutedDOMRpcRoutingTableEntry instance = entry.newInstance(Map.of());
        assertEquals(TestModel.TEST2_QNAME, entry.getType());
        assertEquals(Map.of(), instance.getImplementations());
    }

    @Test
    public void testUnregistered()  {
        final ListenableFuture<?> future = OperationInvocation.invoke(entry, TEST_CHILD);
        final Throwable cause = assertThrows(ExecutionException.class, () -> Futures.getDone(future)).getCause();
        assertThat(cause, instanceOf(DOMRpcImplementationNotAvailableException.class));
        assertEquals("No implementation of RPC "
            + "(urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom:store:test?revision=2014-03-13)test2 "
            + "available",
            cause.getMessage());
    }
}
