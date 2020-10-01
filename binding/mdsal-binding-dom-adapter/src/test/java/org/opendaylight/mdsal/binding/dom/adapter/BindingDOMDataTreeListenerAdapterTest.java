/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.MoreExecutors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.DataTreeListener;
import org.opendaylight.mdsal.binding.dom.adapter.test.util.BindingBrokerTestFactory;
import org.opendaylight.mdsal.binding.dom.adapter.test.util.BindingTestContext;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListeningException;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class BindingDOMDataTreeListenerAdapterTest {

    private BindingDOMDataTreeListenerAdapter bindingDOMDataTreeListenerAdapter;

    @Mock
    private DataTreeListener delegate;

    @Before
    public void setUp() throws Exception {
        final BindingBrokerTestFactory testFactory = new BindingBrokerTestFactory();
        testFactory.setExecutor(MoreExecutors.newDirectExecutorService());
        final BindingTestContext testContext = testFactory.getTestContext();
        testContext.start();

        bindingDOMDataTreeListenerAdapter =
            new BindingDOMDataTreeListenerAdapter(delegate, testContext.getCodec(), LogicalDatastoreType.OPERATIONAL);
    }

    @Test
    public void onDataTreeChanged() throws Exception {
        bindingDOMDataTreeListenerAdapter.onDataTreeChanged(ImmutableSet.of(), ImmutableMap.of());
        verify(delegate).onDataTreeChanged(any(), any());
    }

    @Test
    public void onDataTreeFailedTest() throws Exception {
        bindingDOMDataTreeListenerAdapter.onDataTreeFailed(ImmutableSet.of(new DOMDataTreeListeningException("test")));
        verify(delegate).onDataTreeFailed(any());
    }
}