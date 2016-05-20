/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.broker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.util.concurrent.CheckedFuture;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.junit.Test;
import org.opendaylight.mdsal.dom.api.DOMRpcException;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.mdsal.dom.broker.test.util.TestModel;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class UnknownDOMRpcRoutingTableEntryTest {

    private static final DataContainerChild<?, ?> OUTER_LIST = ImmutableNodes.mapNodeBuilder(TestModel.OUTER_LIST_QNAME)
            .withChild(ImmutableNodes.mapEntry(TestModel.OUTER_LIST_QNAME, TestModel.ID_QNAME, 1))
            .build();

    private static final NormalizedNode<?, ?> TEST_CONTAINER = Builders.containerBuilder()
            .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(TestModel.TEST_QNAME))
            .withChild(OUTER_LIST)
            .build();

    private static final List<DOMRpcImplementation> TEST_LIST = new ArrayList<>();

    @Test
    public void basicTest() throws Exception {
        final Map<YangInstanceIdentifier, List<DOMRpcImplementation>> emptyImpls = new HashMap<>();
        final TestClass TESTCLASS = new TestClass();
        final List<YangInstanceIdentifier> ADD_LIST_1 = new ArrayList<>();
        final List<YangInstanceIdentifier> ADD_LIST_2 = new ArrayList<>();

        TEST_LIST.add(TESTCLASS);
        emptyImpls.put(YangInstanceIdentifier.EMPTY, TEST_LIST);

        UnknownDOMRpcRoutingTableEntry test = new UnknownDOMRpcRoutingTableEntry(SchemaPath.ROOT, emptyImpls);

        assertNotNull(test);
        assertNotNull(test.newInstance(emptyImpls));
        assertNotNull(test.invokeRpc(TEST_CONTAINER));

        assertNotNull(test.getImplementations());
        assertEquals(test.getImplementations(YangInstanceIdentifier.EMPTY), TEST_LIST);

        assertTrue(test.containsContext(YangInstanceIdentifier.EMPTY));
        assertTrue(test.registeredIdentifiers().contains(YangInstanceIdentifier.EMPTY));

        ADD_LIST_1.add(YangInstanceIdentifier.EMPTY);
        ADD_LIST_1.add(YangInstanceIdentifier.of(TestModel.TEST_QNAME));
        ADD_LIST_2.add(YangInstanceIdentifier.of(TestModel.TEST2_QNAME));

        final AbstractDOMRpcRoutingTableEntry tst = test.add(TESTCLASS, ADD_LIST_1);
        final AbstractDOMRpcRoutingTableEntry tst1 = tst.add(TESTCLASS, ADD_LIST_2);
        final AbstractDOMRpcRoutingTableEntry tst2 = tst1.remove(TESTCLASS, ADD_LIST_1);

        assertEquals(1, test.getImplementations().size());
        assertEquals(2, tst.getImplementations().size());
        assertEquals(3, tst1.getImplementations().size());
        assertEquals(2, tst2.getImplementations().size());
    }

    private class TestClass implements DOMRpcImplementation{
        @Nonnull
        @Override
        public CheckedFuture<DOMRpcResult, DOMRpcException> invokeRpc(@Nonnull DOMRpcIdentifier rpc, @Nullable NormalizedNode<?, ?> input) {
            return null;
        }
    }
}