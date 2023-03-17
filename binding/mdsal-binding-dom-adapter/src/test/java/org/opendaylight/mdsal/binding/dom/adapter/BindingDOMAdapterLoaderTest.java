/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableClassToInstanceMap;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.TransactionChainListener;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.yang.gen.v1.bug8449.rev170516.Top;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class BindingDOMAdapterLoaderTest {
    @Mock
    private DOMDataBroker domService;
    @Mock
    private AdapterContext mockContext;

    private BindingDOMAdapterLoader bindingDOMAdapterLoader;

    @Before
    public void setUp() {
        doReturn(ImmutableClassToInstanceMap.of()).when(domService).getExtensions();
        bindingDOMAdapterLoader = new BindingDOMAdapterLoader(mockContext) {
            @Override
            protected DOMService getDelegate(final Class<? extends DOMService> reqDeleg) {
                return domService;
            }
        };
    }

    @Test
    public void createBuilderTest() {
        assertDataBrokerAdapter();
        domService = null;
        assertEquals(Optional.empty(), bindingDOMAdapterLoader.load(DataBroker.class));
    }

    @Test
    public void createChainTest() {
        final var adapter = assertDataBrokerAdapter();
        assertNotNull(adapter.createTransactionChain(mock(TransactionChainListener.class)));
    }

    @Test
    public void registerWithException() {
        final var adapter = assertDataBrokerAdapter();
        final var ex = assertThrows(UnsupportedOperationException.class, () -> adapter.registerDataTreeChangeListener(
                DataTreeIdentifier.create(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(Top.class)),
                mock(DataTreeChangeListener.class)));
        assertEquals("Underlying data broker does not expose DOMDataTreeChangeService.", ex.getMessage());
        final var ex2 = assertThrows(UnsupportedOperationException.class, () -> adapter.registerDataTreeChangeListener(
                LogicalDatastoreType.OPERATIONAL,
                org.opendaylight.mdsal.binding.api.InstanceIdentifier.create(Top.class),
                mock(DataTreeChangeListener.class)
        ));
        assertEquals("Underlying data broker does not expose DOMDataTreeChangeService.", ex.getMessage());
    }

    private BindingDOMDataBrokerAdapter assertDataBrokerAdapter() {
        final var service = bindingDOMAdapterLoader.load(DataBroker.class).orElseThrow();
        assertThat(service, instanceOf(BindingDOMDataBrokerAdapter.class));
        return (BindingDOMDataBrokerAdapter) service;
    }
}