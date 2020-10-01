/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.DataTreeListener;
import org.opendaylight.mdsal.binding.api.DataTreeLoopException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducer;
import org.opendaylight.mdsal.dom.api.DOMDataTreeService;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class BindingDOMDataTreeServiceAdapterTest extends AbstractAdapterTest {

    private BindingDOMDataTreeServiceAdapter bindingDOMDataTreeServiceAdapter;

    @Mock
    private DOMDataTreeService delegate;

    @Override
    @Before
    public void before() {
        super.before();

        bindingDOMDataTreeServiceAdapter = new BindingDOMDataTreeServiceAdapter(codec, delegate);
    }

    @Test
    public void createProducerTest() {
        doReturn(mock(DOMDataTreeProducer.class)).when(delegate).createProducer(any());
        assertNotNull(bindingDOMDataTreeServiceAdapter.createProducer(ImmutableSet.of()));
        verify(delegate).createProducer(any());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void registerListenerTest() throws DataTreeLoopException {
        bindingDOMDataTreeServiceAdapter.registerListener(mock(DataTreeListener.class), ImmutableSet.of(), false,
                ImmutableSet.of());
    }
}