/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Futures;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementationNotAvailableException;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;

@ExtendWith(MockitoExtension.class)
class RoutedDOMRpcRoutingTableEntryTest {
    private static final YangInstanceIdentifier CTX_IN_INPUT =
        YangInstanceIdentifier.of(new NodeIdentifier(Rpcs.CTX));
    private static final YangInstanceIdentifier ONE_PATH = YangInstanceIdentifier.of(
        new NodeIdentifier(Rpcs.BAZ), NodeIdentifierWithPredicates.of(Rpcs.BAZ, Rpcs.NAME, "one"));
    private static final YangInstanceIdentifier TWO_PATH = YangInstanceIdentifier.of(
        new NodeIdentifier(Rpcs.BAZ), NodeIdentifierWithPredicates.of(Rpcs.BAZ, Rpcs.NAME, "two"));

    private static final ContainerNode ONE_INPUT = ImmutableNodes.newContainerBuilder()
        .withNodeIdentifier(new NodeIdentifier(Rpcs.INPUT))
        .withChild(ImmutableNodes.leafNode(Rpcs.CTX, ONE_PATH))
        .build();
    private static final ContainerNode TWO_INPUT = ImmutableNodes.newContainerBuilder()
        .withNodeIdentifier(new NodeIdentifier(Rpcs.INPUT))
        .withChild(ImmutableNodes.leafNode(Rpcs.CTX, TWO_PATH))
        .build();
    private static final ContainerNode GLOBAL_INPUT = ImmutableNodes.newContainerBuilder()
        .withNodeIdentifier(new NodeIdentifier(Rpcs.INPUT))
        // This not covered by schema
        .withChild(ImmutableNodes.leafNode(Rpcs.NAME, "name"))
        .build();

    @Mock
    private DOMRpcImplementation impl;
    @Mock
    private DOMRpcResult result;

    private RoutedDOMRpcRoutingTableEntry entry;

    @BeforeEach
    void beforeEach() {
        // Note: ImmutableMap.of() allows get(null), Map.of() does not
        entry = new RoutedDOMRpcRoutingTableEntry(Rpcs.BAR, CTX_IN_INPUT, ImmutableMap.of());
    }

    @Test
    void testNewInstance() {
        final RoutedDOMRpcRoutingTableEntry instance = entry.newInstance(Map.of());
        assertEquals(Rpcs.BAR, entry.getType());
        assertEquals(Map.of(), instance.getImplementations());
    }

    @Test
    void testUnregistered()  {
        assertRpcUnavailable(ONE_INPUT);
        assertRpcUnavailable(TWO_INPUT);
        assertRpcUnavailable(GLOBAL_INPUT);
    }

    @Test
    void testRegisteredGlobal() {
        setPaths((YangInstanceIdentifier) null);
        assertRpcAvailable(GLOBAL_INPUT);
    }

    @Test
    void testRegisteredGlobalOne() {
        setPaths((YangInstanceIdentifier) null);
        assertRpcAvailable(ONE_INPUT);
    }

    @Test
    void testRegisteredGlobalTwo() {
        setPaths((YangInstanceIdentifier) null);
        assertRpcAvailable(TWO_INPUT);
    }

    @Test
    void testRegisteredOne() {
        setPaths(ONE_PATH);
        assertRpcUnavailable(TWO_INPUT);
        assertRpcAvailable(ONE_INPUT);
    }

    @Test
    void testRegisteredTwo() {
        setPaths(TWO_PATH);
        assertRpcUnavailable(ONE_INPUT);
        assertRpcAvailable(TWO_INPUT);
    }

    @Test
    void testRemote() {
        setPaths(YangInstanceIdentifier.of());
        assertRpcAvailable(ONE_INPUT);
    }

    @Test
    void testWrongContext() {
        assertRpcUnavailable(ImmutableNodes.newContainerBuilder()
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
        final var ee = assertThrows(ExecutionException.class, () -> Futures.getDone(future));
        final var cause = assertInstanceOf(DOMRpcImplementationNotAvailableException.class, ee.getCause());
        assertEquals("No implementation of RPC (rpcs)bar available", cause.getMessage());
    }
}
