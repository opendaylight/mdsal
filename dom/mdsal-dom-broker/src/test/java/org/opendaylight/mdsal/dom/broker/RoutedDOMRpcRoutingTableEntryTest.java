/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import org.junit.Test;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementationNotAvailableException;
import org.opendaylight.mdsal.dom.broker.test.util.TestModel;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class RoutedDOMRpcRoutingTableEntryTest extends TestUtils {

    @Test
    public void basicTest() throws Exception {
        final RpcDefinition rpcDefinition = mock(RpcDefinition.class);
        doReturn(SchemaPath.ROOT).when(rpcDefinition).getPath();

        RoutedDOMRpcRoutingTableEntry routedDOMRpcRoutingTableEntry =
                new RoutedDOMRpcRoutingTableEntry(rpcDefinition, TestModel.TEST_PATH, new HashMap<>());
        assertNotNull(routedDOMRpcRoutingTableEntry.newInstance(new HashMap<>()));

        try {
            routedDOMRpcRoutingTableEntry.invokeRpc(TEST_CHILD).checkedGet();
            fail("Expected DOMRpcImplementationNotAvailableException");
        } catch (Exception e) {
            assertTrue(e instanceof DOMRpcImplementationNotAvailableException);
        }
    }
}