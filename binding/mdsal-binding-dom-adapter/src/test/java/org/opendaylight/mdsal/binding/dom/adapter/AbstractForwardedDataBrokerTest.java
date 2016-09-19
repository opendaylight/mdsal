/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.concurrent.Executors;
import org.junit.Test;
import org.opendaylight.mdsal.binding.dom.adapter.test.util.BindingBrokerTestFactory;
import org.opendaylight.mdsal.binding.dom.adapter.test.util.BindingTestContext;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class AbstractForwardedDataBrokerTest {

    @Test
    public void basicTest() throws Exception {
        final BindingBrokerTestFactory testFactory = new BindingBrokerTestFactory();
        testFactory.setExecutor(Executors.newCachedThreadPool());

        final BindingTestContext testContext = testFactory.getTestContext();
        testContext.start();

        AbstractForwardedDataBroker forwardedDataBroker =
                new AbstractForwardedDataBrokerImpl(testContext.getDOMDataBroker(), testContext.getCodec());

        final NormalizedNode normalizedNode = mock(NormalizedNode.class);
        forwardedDataBroker.toBinding(InstanceIdentifier.create(DataObject.class),
                (Map) ImmutableMap.of(YangInstanceIdentifier.EMPTY, normalizedNode));
    }

    private class AbstractForwardedDataBrokerImpl extends AbstractForwardedDataBroker {

        private AbstractForwardedDataBrokerImpl(DOMDataBroker domDataBroker, BindingToNormalizedNodeCodec codec) {
            super(domDataBroker, codec);
        }
    }
}