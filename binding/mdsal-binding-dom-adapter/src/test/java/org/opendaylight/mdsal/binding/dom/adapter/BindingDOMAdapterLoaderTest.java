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
import static org.mockito.MockitoAnnotations.initMocks;

import javax.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.yangtools.binding.data.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.yangtools.sal.binding.generator.impl.GeneratedClassLoadingStrategy;

public class BindingDOMAdapterLoaderTest {

    @Mock
    private DOMDataBroker domService;

    @Mock
    private BindingNormalizedNodeCodecRegistry mockCodecRegistry;

    private BindingDOMAdapterLoader bindingDOMAdapterLoader;
    private BindingDOMDataBrokerAdapter bindingDOMDataBrokerAdapter;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        bindingDOMAdapterLoader = new BindingDOMAdapterLoader(
                new BindingToNormalizedNodeCodec(GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy(),
                        mockCodecRegistry)) {
            @Nullable
            @Override
            protected DOMService getDelegate(Class<? extends DOMService> reqDeleg) {
                return domService;
            }
        };
    }

    @Test
    public void createBuilderTest() throws Exception {
        assertTrue(bindingDOMAdapterLoader.load(DataBroker.class).get() instanceof BindingDOMDataBrokerAdapter);
        domService = null;
        assertFalse(bindingDOMAdapterLoader.load(DataBroker.class).isPresent());
    }

    @Test
    public void createChainTest() throws Exception {
        bindingDOMDataBrokerAdapter = (BindingDOMDataBrokerAdapter) bindingDOMAdapterLoader.load(DataBroker.class).get();
        assertNotNull(bindingDOMDataBrokerAdapter.createTransactionChain(null));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void registerWithException() throws Exception {
        bindingDOMDataBrokerAdapter = (BindingDOMDataBrokerAdapter) bindingDOMAdapterLoader.load(DataBroker.class).get();
        bindingDOMDataBrokerAdapter.registerDataTreeChangeListener(null,null);
    }
}