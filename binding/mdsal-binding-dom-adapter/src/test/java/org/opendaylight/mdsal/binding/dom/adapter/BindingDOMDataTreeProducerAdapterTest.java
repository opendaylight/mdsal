/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.MoreExecutors;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.mdsal.binding.api.DataTreeProducerException;
import org.opendaylight.mdsal.binding.dom.adapter.test.util.BindingBrokerTestFactory;
import org.opendaylight.mdsal.binding.dom.adapter.test.util.BindingTestContext;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCursorAwareTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducer;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducerBusyException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducerException;

public class BindingDOMDataTreeProducerAdapterTest {

    private BindingDOMDataTreeProducerAdapter bindingDOMDataTreeProducerAdapter;
    private BindingToNormalizedNodeCodec codec;

    @Mock
    private DOMDataTreeProducer delegate;

    @Before
    public void setUp() {
        initMocks(this);
        final BindingBrokerTestFactory testFactory = new BindingBrokerTestFactory();
        testFactory.setExecutor(MoreExecutors.newDirectExecutorService());
        final BindingTestContext testContext = testFactory.getTestContext();
        testContext.start();
        codec = testContext.getCodec();
        bindingDOMDataTreeProducerAdapter = new BindingDOMDataTreeProducerAdapter(codec, delegate);
    }

    @Test
    public void createTransactionTest() {
        doReturn(mock(DOMDataTreeCursorAwareTransaction.class)).when(delegate).createTransaction(true);
        assertNotNull(bindingDOMDataTreeProducerAdapter.createTransaction(true));
        verify(delegate).createTransaction(true);
    }

    @Test
    public void createTest() {
        assertNotNull(BindingDOMDataTreeProducerAdapter.create(delegate, codec));
    }

    @Test
    public void createProducerTest() {
        doReturn(mock(DOMDataTreeProducer.class)).when(delegate).createProducer(any());
        assertNotNull(bindingDOMDataTreeProducerAdapter.createProducer(ImmutableSet.of()));
        verify(delegate).createProducer(any());
    }

    @Test
    public void closeTest() throws DataTreeProducerException, DOMDataTreeProducerException {
        reset(delegate);
        bindingDOMDataTreeProducerAdapter.close();
        verify(delegate).close();
    }

    @Test(expected = DataTreeProducerException.class)
    public void closeTestWithException1() throws DataTreeProducerException, DOMDataTreeProducerException {
        doThrow(new DOMDataTreeProducerBusyException("test")).when(delegate).close();
        bindingDOMDataTreeProducerAdapter.close();
    }

    @Test(expected = DataTreeProducerException.class)
    public void closeTestWithException2() throws DataTreeProducerException, DOMDataTreeProducerException {
        doThrow(new DOMDataTreeProducerException("test")).when(delegate).close();
        bindingDOMDataTreeProducerAdapter.close();
    }
}