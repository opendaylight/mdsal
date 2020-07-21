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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementationNotAvailableException;
import org.opendaylight.mdsal.dom.broker.DOMRpcRouter.OperationInvocation;
import org.opendaylight.mdsal.dom.broker.util.TestModel;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;

public class RoutedDOMRpcRoutingTableEntryTest extends TestUtils {
    @Test
    public void basicTest()  {
        final RpcDefinition rpcDefinition = mock(RpcDefinition.class);
        doReturn(TestModel.TEST2_QNAME).when(rpcDefinition).getQName();

        final RoutedDOMRpcRoutingTableEntry routedDOMRpcRoutingTableEntry =
                new RoutedDOMRpcRoutingTableEntry(rpcDefinition, TestModel.TEST_PATH, new HashMap<>());
        assertNotNull(routedDOMRpcRoutingTableEntry.newInstance(new HashMap<>()));

        final ListenableFuture<?> future = OperationInvocation.invoke(routedDOMRpcRoutingTableEntry, TEST_CHILD);
        final ExecutionException ex = assertThrows(ExecutionException.class, () -> future.get(5, TimeUnit.SECONDS));
        assertThat(ex.getCause(), instanceOf(DOMRpcImplementationNotAvailableException.class));
    }
}
