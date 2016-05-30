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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementationNotAvailableException;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class GlobalDOMRpcRoutingTableEntryTest extends TestUtils{

    @Test
    public void basicTest() throws Exception {
        final RpcDefinition rpcDefinition = mock(RpcDefinition.class);
        final Map<YangInstanceIdentifier, List<DOMRpcImplementation>> rpcImplementations = new HashMap<>();
        final List<DOMRpcImplementation> rpcImplementation = new ArrayList<>();
        final YangInstanceIdentifier yangInstanceIdentifier = YangInstanceIdentifier.builder().build();

        doReturn(SchemaPath.ROOT).when(rpcDefinition).getPath();

        rpcImplementation.add(getTestRpcImplementation());
        rpcImplementations.put(yangInstanceIdentifier, rpcImplementation);

        final GlobalDOMRpcRoutingTableEntry globalDOMRpcRoutingTableEntry =
                new GlobalDOMRpcRoutingTableEntry(rpcDefinition, new HashMap<>());

        assertTrue(globalDOMRpcRoutingTableEntry.getSchemaPath().equals(SchemaPath.ROOT));
        assertTrue(globalDOMRpcRoutingTableEntry.getImplementations().isEmpty());
        assertFalse(globalDOMRpcRoutingTableEntry
                .newInstance(rpcImplementations).getImplementations().isEmpty());
        assertTrue(globalDOMRpcRoutingTableEntry
                .newInstance(rpcImplementations).getImplementations().containsValue(rpcImplementation));

        try{
            globalDOMRpcRoutingTableEntry.newInstance(rpcImplementations)
                    .invokeRpc(TEST_CONTAINER).checkedGet();
            fail("Expected DOMRpcImplementationNotAvailableException");
        }catch(DOMRpcImplementationNotAvailableException e){
            assertTrue(e.getMessage().contains(EXCEPTION_TEXT));
        }
    }
}