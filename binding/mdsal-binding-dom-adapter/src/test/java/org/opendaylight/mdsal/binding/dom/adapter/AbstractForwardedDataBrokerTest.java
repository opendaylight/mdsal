/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.concurrent.Executors;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.adapter.test.util.BindingBrokerTestFactory;
import org.opendaylight.mdsal.binding.dom.adapter.test.util.BindingTestContext;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.augment.rev140709.TreeLeafOnlyAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.Top;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.test.binding.rev140701.two.level.list.TopLevelListKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class AbstractForwardedDataBrokerTest {

    private static final TopLevelListKey TOP_FOO_KEY = new TopLevelListKey("foo");
    private static final InstanceIdentifier<TopLevelList> BA_TOP_LEVEL_LIST =
            InstanceIdentifier.builder(Top.class).child(TopLevelList.class, TOP_FOO_KEY).build();
    private static final InstanceIdentifier<TreeLeafOnlyAugment> BA_TREE_LEAF_ONLY =
            BA_TOP_LEVEL_LIST.augmentation(TreeLeafOnlyAugment.class);
    private AbstractForwardedDataBroker forwardedDataBroker;
    private YangInstanceIdentifier data;
    private NormalizedNode<?, ?> normalizedNode;

    @Before
    public void basicTest() throws Exception {
        final BindingBrokerTestFactory testFactory = new BindingBrokerTestFactory();
        testFactory.setExecutor(Executors.newCachedThreadPool());

        final BindingTestContext testContext = testFactory.getTestContext();
        testContext.start();
        data = testContext.getCodec().toYangInstanceIdentifier(BA_TREE_LEAF_ONLY);
        normalizedNode = testContext.getCodec().toNormalizedNode(BA_TOP_LEVEL_LIST,
                new TopLevelListBuilder().setKey(TOP_FOO_KEY).build()).getValue();
        forwardedDataBroker =
                new AbstractForwardedDataBrokerImpl(testContext.getDomAsyncDataBroker(), testContext.getCodec());
    }

    @Test
    public void toBindingTestWithMap() throws Exception {
        final NormalizedNode<?, ?> normalizedNode = mock(NormalizedNode.class);
        assertNotNull(forwardedDataBroker.toBinding(BA_TREE_LEAF_ONLY, ImmutableMap.of(data, normalizedNode)));
    }

    @Test
    public void toBindingTestWithSet() throws Exception {
        assertNotNull(forwardedDataBroker.toBinding(BA_TREE_LEAF_ONLY, ImmutableSet.of(data)));
    }

    @Test
    public void toBindingDataTest() throws Exception {
        assertNotNull(forwardedDataBroker.toBindingData(BA_TOP_LEVEL_LIST, normalizedNode));
    }

    private class AbstractForwardedDataBrokerImpl extends AbstractForwardedDataBroker {

        private AbstractForwardedDataBrokerImpl(final DOMDataBroker domDataBroker,
                final BindingToNormalizedNodeCodec codec) {
            super(domDataBroker, codec);
        }
    }
}
