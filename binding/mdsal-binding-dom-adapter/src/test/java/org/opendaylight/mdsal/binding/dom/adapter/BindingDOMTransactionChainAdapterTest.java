/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.TransactionChain;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;
import org.opendaylight.mdsal.dom.spi.PingPongMergingDOMDataBroker;
import org.opendaylight.yangtools.binding.data.codec.spi.BindingDOMCodecServices;

@RunWith(Parameterized.class)
public class BindingDOMTransactionChainAdapterTest {
    public enum TransactionChainType implements Function<DataBroker, TransactionChain> {
        NORMAL {
            @Override
            public TransactionChain apply(final DataBroker broker) {
                return broker.createTransactionChain();
            }

            @Override
            void mockRead(final DOMTransactionChain chain) {
                doReturn(mock(DOMDataTreeReadTransaction.class)).when(chain).newReadOnlyTransaction();
            }

            @Override
            void mockWrite(final DOMTransactionChain chain) {
                doReturn(mock(DOMDataTreeWriteTransaction.class)).when(chain).newWriteOnlyTransaction();
            }

        },
        MERGING {
            @Override
            public TransactionChain apply(final DataBroker broker) {
                return broker.createMergingTransactionChain();
            }

            @Override
            void mockRead(final DOMTransactionChain chain) {
                mockReadWrite(chain);
            }

            @Override
            void mockWrite(final DOMTransactionChain chain) {
                mockReadWrite(chain);
            }
        };

        abstract void mockRead(DOMTransactionChain chain);

        abstract void mockWrite(DOMTransactionChain chain);
    }

    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

    @Parameters
    public static Collection<Object[]> parameters() {
        return List.of(new Object[] { TransactionChainType.NORMAL }, new Object[] { TransactionChainType.MERGING });
    }

    private final TransactionChainType type;

    @Mock
    private PingPongMergingDOMDataBroker domService;
    @Mock
    private DOMTransactionChain transactionChain;
    @Mock
    private BindingDOMCodecServices mockCodecRegistry;

    private BindingDOMTransactionChainAdapter bindingDOMTransactionChainAdapter;

    public BindingDOMTransactionChainAdapterTest(final TransactionChainType type) {
        this.type = requireNonNull(type);
    }

    @Before
    public void setUp() {
        doReturn(transactionChain).when(domService).createTransactionChain();
        if (type == TransactionChainType.MERGING) {
            doCallRealMethod().when(domService).createMergingTransactionChain();
        }


        BindingDOMAdapterLoader bindingDOMAdapterLoader = new BindingDOMAdapterLoader(
                new ConstantAdapterContext(mockCodecRegistry)) {
            @Override
            protected DOMService<?, ?> getDelegate(final Class<? extends DOMService<?, ?>> reqDeleg) {
                return domService;
            }
        };

        BindingDOMDataBrokerAdapter bindingDOMDataBrokerAdapter =
                (BindingDOMDataBrokerAdapter) bindingDOMAdapterLoader.load(DataBroker.class).orElseThrow();
        bindingDOMTransactionChainAdapter =
            (BindingDOMTransactionChainAdapter) type.apply(bindingDOMDataBrokerAdapter);
        assertNotNull(bindingDOMTransactionChainAdapter.getDelegate());

    }

    static void mockReadWrite(final DOMTransactionChain chain) {
        doReturn(mock(DOMDataTreeReadWriteTransaction.class)).when(chain).newReadWriteTransaction();
    }

    @Test
    public void closeTest() {
        doNothing().when(transactionChain).close();
        bindingDOMTransactionChainAdapter.close();
        verify(transactionChain).close();
    }

    @Test
    public void readTransactionTest() {
        type.mockRead(transactionChain);
        assertNotNull(bindingDOMTransactionChainAdapter.newReadOnlyTransaction());
    }

    @Test
    public void writeTransactionTest() {
        type.mockWrite(transactionChain);
        assertNotNull(bindingDOMTransactionChainAdapter.newWriteOnlyTransaction());
    }

    @Test
    public void readWriteTransactionTest() {
        mockReadWrite(transactionChain);
        assertNotNull(bindingDOMTransactionChainAdapter.newReadWriteTransaction());
    }
}
