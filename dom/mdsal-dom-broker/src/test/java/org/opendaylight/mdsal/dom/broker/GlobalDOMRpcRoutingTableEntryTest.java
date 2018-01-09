/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.opendaylight.mdsal.dom.api.DOMOperationImplementation;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class GlobalDOMRpcRoutingTableEntryTest extends TestUtils {

    @Test
    public void basicTest() throws Exception {
        final Map<YangInstanceIdentifier, List<DOMOperationImplementation>> rpcImplementations = new HashMap<>();
        final List<DOMOperationImplementation> rpcImplementation = new ArrayList<>();
        final RpcDefinition rpcDefinition = mock(RpcDefinition.class);
        final YangInstanceIdentifier yangInstanceIdentifier = YangInstanceIdentifier.builder().build();

        doReturn(SchemaPath.ROOT).when(rpcDefinition).getPath();
        final GlobalDOMRpcRoutingTableEntry globalDOMRpcRoutingTableEntry = new GlobalDOMRpcRoutingTableEntry(
                rpcDefinition, new HashMap<>());
        rpcImplementation.add(getTestOperationImplementation());
        rpcImplementations.put(yangInstanceIdentifier, rpcImplementation);

        assertTrue(globalDOMRpcRoutingTableEntry.getSchemaPath().equals(SchemaPath.ROOT));
        assertTrue(globalDOMRpcRoutingTableEntry.getImplementations().isEmpty());
        assertFalse(globalDOMRpcRoutingTableEntry.newInstance(rpcImplementations).getImplementations().isEmpty());
        assertTrue(globalDOMRpcRoutingTableEntry.newInstance(rpcImplementations).getImplementations().containsValue(
                rpcImplementation));

        globalDOMRpcRoutingTableEntry.newInstance(rpcImplementations)
            .invokeRpc(TEST_CONTAINER, (result, throwable) -> {
                assertTrue(throwable.getMessage().contains(EXCEPTION_TEXT));
            });
    }
}