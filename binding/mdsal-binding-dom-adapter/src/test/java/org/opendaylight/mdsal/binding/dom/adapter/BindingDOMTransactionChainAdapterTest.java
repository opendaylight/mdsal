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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import com.google.common.collect.ImmutableClassToInstanceMap;
import javax.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;

public class BindingDOMTransactionChainAdapterTest {

    @Mock
    private DOMDataBroker domService;

    @Mock
    private DOMTransactionChain transactionChain;

    @Mock
    private BindingNormalizedNodeCodecRegistry mockCodecRegistry;

    private BindingDOMTransactionChainAdapter bindingDOMTransactionChainAdapter;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        doReturn(transactionChain).when(domService).createTransactionChain(any());
        doReturn(ImmutableClassToInstanceMap.of()).when(domService).getExtensions();

        BindingDOMAdapterLoader bindingDOMAdapterLoader = new BindingDOMAdapterLoader(
                new BindingToNormalizedNodeCodec(GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy(),
                        mockCodecRegistry)) {
            @Nullable
            @Override
            protected DOMService getDelegate(final Class<? extends DOMService> reqDeleg) {
                return domService;
            }
        };

        BindingDOMDataBrokerAdapter bindingDOMDataBrokerAdapter =
                (BindingDOMDataBrokerAdapter) bindingDOMAdapterLoader.load(DataBroker.class).get();
        bindingDOMTransactionChainAdapter =
                (BindingDOMTransactionChainAdapter) bindingDOMDataBrokerAdapter.createTransactionChain(null);
        assertNotNull(bindingDOMTransactionChainAdapter.getDelegate());
        doNothing().when(transactionChain).close();
        bindingDOMTransactionChainAdapter.close();
        verify(transactionChain).close();
    }

    @Test
    public void readTransactionTest() throws Exception {
        doReturn(mock(DOMDataTreeReadTransaction.class)).when(transactionChain).newReadOnlyTransaction();
        assertNotNull(bindingDOMTransactionChainAdapter.newReadOnlyTransaction());
    }

    @Test
    public void writeTransactionTest() throws Exception {
        doReturn(mock(DOMDataTreeWriteTransaction.class)).when(transactionChain).newWriteOnlyTransaction();
        assertNotNull(bindingDOMTransactionChainAdapter.newWriteOnlyTransaction());
    }
}