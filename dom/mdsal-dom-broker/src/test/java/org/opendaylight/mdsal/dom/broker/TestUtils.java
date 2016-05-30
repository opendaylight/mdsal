/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.broker;

import static org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes.leafNode;

import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.dom.api.DOMRpcException;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementationNotAvailableException;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.mdsal.dom.broker.test.util.TestModel;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapEntryNodeBuilder;

abstract class TestUtils {

    private static final DataContainerChild<?, ?> OUTER_LIST = ImmutableNodes.mapNodeBuilder(TestModel.OUTER_LIST_QNAME)
            .withChild(ImmutableNodes.mapEntry(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, 1))
            .build();

    private static final String TOP_LEVEL_LIST_FOO_KEY_VALUE = "foo";
    private static final QName TOP_QNAME = TestModel.ID_QNAME;
    private static final QName TOP_LEVEL_LIST_QNAME = QName.create(TOP_QNAME, "top-level-list");
    private static final QName TOP_LEVEL_LIST_KEY_QNAME = QName.create(TOP_QNAME, "name");


    private final static MapEntryNode topLevelListNormalized = ImmutableMapEntryNodeBuilder.create()
            .withNodeIdentifier(
                    new YangInstanceIdentifier.NodeIdentifierWithPredicates(
                            TOP_LEVEL_LIST_QNAME, TOP_LEVEL_LIST_KEY_QNAME, TOP_LEVEL_LIST_FOO_KEY_VALUE))
            .withChild(leafNode(TOP_LEVEL_LIST_KEY_QNAME, TOP_LEVEL_LIST_FOO_KEY_VALUE))
            .build();

    private static final DataContainerChild<?, ?> CHILD_LIST = ImmutableNodes.mapNodeBuilder(TestModel.TEST_QNAME)
            .withNodeIdentifier(NodeIdentifier.create(TestModel.TEST_QNAME))
            .withChild(topLevelListNormalized)
            .build();

    static final NormalizedNode<?, ?> TEST_CONTAINER = Builders.containerBuilder()
            .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(TestModel.TEST_QNAME))
            .withChild(OUTER_LIST)
            .build();

    static final NormalizedNode<?, ?> TEST_CHILD = Builders.containerBuilder()
            .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(TestModel.TEST_QNAME))
            .withChild(CHILD_LIST)
            .build();

    static final String EXCEPTION_TEXT = "TestRpcImplementationException";

    static TestRpcImplementation getTestRpcImplementation(){
        return new TestRpcImplementation();
    }

    private static final class TestRpcImplementation implements DOMRpcImplementation {
        private final CheckedFuture<DOMRpcResult, DOMRpcException> unknownRpc;

        private TestRpcImplementation() {
            unknownRpc = Futures.immediateFailedCheckedFuture(
                    new DOMRpcImplementationNotAvailableException(EXCEPTION_TEXT));
        }

        @Nonnull
        public CheckedFuture<DOMRpcResult, DOMRpcException> invokeRpc(
                @Nonnull DOMRpcIdentifier rpc, @Nullable NormalizedNode<?, ?> input) {
            return unknownRpc;
        }
    }
}
