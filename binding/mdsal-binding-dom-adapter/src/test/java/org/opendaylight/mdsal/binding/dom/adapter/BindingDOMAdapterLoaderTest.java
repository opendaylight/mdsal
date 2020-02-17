/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableClassToInstanceMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.TransactionChainListener;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.generator.impl.DefaultBindingRuntimeGenerator;
import org.opendaylight.mdsal.binding.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMService;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class BindingDOMAdapterLoaderTest {

    @Mock
    private DOMDataBroker domService;

    @Mock
    private BindingNormalizedNodeCodecRegistry mockCodecRegistry;

    private BindingDOMAdapterLoader bindingDOMAdapterLoader;
    private BindingDOMDataBrokerAdapter bindingDOMDataBrokerAdapter;

    @Before
    public void setUp() {
        doReturn(ImmutableClassToInstanceMap.of()).when(domService).getExtensions();
        bindingDOMAdapterLoader = new BindingDOMAdapterLoader(new BindingToNormalizedNodeCodec(
                new DefaultBindingRuntimeGenerator(), GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy(),
                mockCodecRegistry)) {
            @Override
            protected DOMService getDelegate(final Class<? extends DOMService> reqDeleg) {
                return domService;
            }
        };
    }

    @Test
    public void createBuilderTest() {
        assertTrue(bindingDOMAdapterLoader.load(DataBroker.class).get() instanceof BindingDOMDataBrokerAdapter);
        domService = null;
        assertFalse(bindingDOMAdapterLoader.load(DataBroker.class).isPresent());
    }

    @Test
    public void createChainTest() {
        bindingDOMDataBrokerAdapter
                = (BindingDOMDataBrokerAdapter) bindingDOMAdapterLoader.load(DataBroker.class).get();
        assertNotNull(bindingDOMDataBrokerAdapter.createTransactionChain(mock(TransactionChainListener.class)));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void registerWithException() {
        bindingDOMDataBrokerAdapter
                = (BindingDOMDataBrokerAdapter) bindingDOMAdapterLoader.load(DataBroker.class).get();
        bindingDOMDataBrokerAdapter.registerDataTreeChangeListener(null, null);
    }
}