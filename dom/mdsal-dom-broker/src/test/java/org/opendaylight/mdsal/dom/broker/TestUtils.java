/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMRpcFuture;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementationNotAvailableException;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;

final class TestUtils {
    private static final MapNode OUTER_LIST = ImmutableNodes.newSystemMapBuilder()
        .withNodeIdentifier(new NodeIdentifier(TestModel.OUTER_LIST_QNAME))
        .withChild(ImmutableNodes.newMapEntryBuilder()
            .withNodeIdentifier(NodeIdentifierWithPredicates.of(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, 1))
            .withChild(ImmutableNodes.leafNode(TestModel.ID_QNAME, 1))
            .build())
        .build();

    private static final String TOP_LEVEL_LIST_FOO_KEY_VALUE = "foo";
    private static final QName TOP_QNAME = TestModel.ID_QNAME;
    private static final QName TOP_LEVEL_LIST_QNAME = QName.create(TOP_QNAME, "top-level-list");
    private static final QName TOP_LEVEL_LIST_KEY_QNAME = QName.create(TOP_QNAME, "name");

    private static final MapEntryNode TOP_LEVEL_LIST_NODE = ImmutableNodes.newMapEntryBuilder()
        .withNodeIdentifier(NodeIdentifierWithPredicates.of(
            TOP_LEVEL_LIST_QNAME, TOP_LEVEL_LIST_KEY_QNAME, TOP_LEVEL_LIST_FOO_KEY_VALUE))
        .withChild(ImmutableNodes.leafNode(TOP_LEVEL_LIST_KEY_QNAME, TOP_LEVEL_LIST_FOO_KEY_VALUE))
        .build();

    private static final MapNode CHILD_LIST = ImmutableNodes.newSystemMapBuilder()
        .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
        .withChild(TOP_LEVEL_LIST_NODE)
        .build();

    static final ContainerNode TEST_CONTAINER = ImmutableNodes.newContainerBuilder()
        .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
        .withChild(OUTER_LIST)
        .build();

    static final ContainerNode TEST_CHILD = ImmutableNodes.newContainerBuilder()
        .withNodeIdentifier(new NodeIdentifier(TestModel.TEST_QNAME))
        .withChild(CHILD_LIST)
        .build();

    static final String EXCEPTION_TEXT = "TestRpcImplementationException";

    private TestUtils() {
        // Hidden on purpose
    }

    static MapEntryNode mapEntry(final QName listName, final QName keyName, final Object keyValue) {
        return ImmutableNodes.newMapEntryBuilder()
            .withNodeIdentifier(NodeIdentifierWithPredicates.of(listName, keyName, keyValue))
            .withChild(ImmutableNodes.leafNode(keyName, keyValue))
            .build();
    }

    static TestRpcImplementation getTestRpcImplementation() {
        return new TestRpcImplementation();
    }

    private static final class TestRpcImplementation implements DOMRpcImplementation {
        private final @NonNull DOMRpcFuture unknownRpc;

        TestRpcImplementation() {
            unknownRpc = DOMRpcFuture.failed(new DOMRpcImplementationNotAvailableException(EXCEPTION_TEXT));
        }

        @Override
        public DOMRpcFuture invokeRpc(final DOMRpcIdentifier rpc, final ContainerNode input) {
            requireNonNull(input);
            return unknownRpc;
        }
    }
}
