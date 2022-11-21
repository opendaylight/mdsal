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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Futures;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementationNotAvailableException;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.mdsal.dom.broker.DOMRpcRouter.OperationInvocation;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class RoutedDOMRpcRoutingTableEntryTest {
    public static final YangInstanceIdentifier CTX_IN_INPUT =
        YangInstanceIdentifier.create(new NodeIdentifier(Rpcs.CTX));
    public static final YangInstanceIdentifier ONE_PATH = YangInstanceIdentifier.create(
        new NodeIdentifier(Rpcs.BAZ), NodeIdentifierWithPredicates.of(Rpcs.BAZ, Rpcs.NAME, "one"));
    public static final YangInstanceIdentifier TWO_PATH = YangInstanceIdentifier.create(
        new NodeIdentifier(Rpcs.BAZ), NodeIdentifierWithPredicates.of(Rpcs.BAZ, Rpcs.NAME, "two"));

    public static final ContainerNode ONE_INPUT = Builders.containerBuilder()
        .withNodeIdentifier(new NodeIdentifier(Rpcs.INPUT))
        .withChild(ImmutableNodes.leafNode(Rpcs.CTX, ONE_PATH))
        .build();
    public static final ContainerNode TWO_INPUT = Builders.containerBuilder()
        .withNodeIdentifier(new NodeIdentifier(Rpcs.INPUT))
        .withChild(ImmutableNodes.leafNode(Rpcs.CTX, TWO_PATH))
        .build();
    public static final ContainerNode GLOBAL_INPUT = Builders.containerBuilder()
        .withNodeIdentifier(new NodeIdentifier(Rpcs.INPUT))
        // This not covered by schema
        .withChild(ImmutableNodes.leafNode(Rpcs.NAME, "name"))
        .build();

    @Mock
    public DOMRpcImplementation impl;
    @Mock
    public DOMRpcResult result;
    public RoutedDOMRpcRoutingTableEntry entry;

    @Before
    public void before() {
        // Note: ImmutableMap.of() allows get(null), Map.of() does not
        entry = new RoutedDOMRpcRoutingTableEntry(Rpcs.BAR, CTX_IN_INPUT, ImmutableMap.of());
    }

    @Test
    public void testNewInstance() {
        final RoutedDOMRpcRoutingTableEntry instance = entry.newInstance(Map.of());
        assertEquals(Rpcs.BAR, entry.getType());
        assertEquals(Map.of(), instance.getImplementations());
    }

    @Test
    public void testUnregistered()  {
        assertRpcUnavailable(ONE_INPUT);
        assertRpcUnavailable(TWO_INPUT);
        assertRpcUnavailable(GLOBAL_INPUT);
    }

    @Test
    public void testRegisteredGlobal() {
        setPaths((YangInstanceIdentifier) null);
        assertRpcAvailable(GLOBAL_INPUT);
    }

    @Test
    public void testRegisteredGlobalOne() {
        setPaths((YangInstanceIdentifier) null);
        assertRpcAvailable(ONE_INPUT);
    }

    @Test
    public void testRegisteredGlobalTwo() {
        setPaths((YangInstanceIdentifier) null);
        assertRpcAvailable(TWO_INPUT);
    }

    @Test
    public void testRegisteredOne() {
        setPaths(ONE_PATH);
        assertRpcUnavailable(TWO_INPUT);
        assertRpcAvailable(ONE_INPUT);
    }

    @Test
    public void testRegisteredTwo() {
        setPaths(TWO_PATH);
        assertRpcUnavailable(ONE_INPUT);
        assertRpcAvailable(TWO_INPUT);
    }

    @Test
    public void testRemote() {
        setPaths(YangInstanceIdentifier.empty());
        assertRpcAvailable(ONE_INPUT);
    }

    @Test
    public void testWrongContext() {
        assertRpcUnavailable(Builders.containerBuilder()
            .withNodeIdentifier(new NodeIdentifier(Rpcs.INPUT))
            .withChild(ImmutableNodes.leafNode(Rpcs.CTX, "bad type"))
            .build());
    }

    private void setPaths(final YangInstanceIdentifier... paths) {
        final var map = new HashMap<YangInstanceIdentifier, List<DOMRpcImplementation>>();
        for (var path : paths) {
            map.put(path, List.of(impl));
        }
        entry = entry.newInstance(map);
    }

    private void assertRpcAvailable(final ContainerNode input) {
        doReturn(Futures.immediateFuture(result)).when(impl).invokeRpc(any(), any());

        final var future = OperationInvocation.invoke(entry, input);
        try {
            assertSame(result, Futures.getDone(future));
        } catch (ExecutionException e) {
            throw new AssertionError(e);
        }

        verify(impl).invokeRpc(any(), any());
    }

    private void assertRpcUnavailable(final ContainerNode input) {
        final var future = OperationInvocation.invoke(entry, input);
        final var cause = assertThrows(ExecutionException.class, () -> Futures.getDone(future)).getCause();
        assertThat(cause, instanceOf(DOMRpcImplementationNotAvailableException.class));
        assertEquals("No implementation of RPC (rpcs)bar available", cause.getMessage());
    }
}
