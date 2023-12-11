/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

@ExtendWith(MockitoExtension.class)
class InMemoryDOMDataStoreFactoryTest {
    @Mock
    private DOMSchemaService domSchemaService;
    @Mock
    private DOMDataTreeChangeListener domDataTreeChangeListener;
    @Mock
    private AutoCloseable autoCloseable;

    @Test
    public void basicTest() throws Exception {
        final String testStoreName = "TestStore";
        doReturn(null).when(domSchemaService).registerSchemaContextListener(any());

        final var inMemoryDOMDataStore = InMemoryDOMDataStoreFactory.create(testStoreName, domSchemaService);
        assertNotNull(inMemoryDOMDataStore);
        assertEquals(testStoreName, inMemoryDOMDataStore.getIdentifier());

        doNothing().when(domDataTreeChangeListener).onInitialData();
        inMemoryDOMDataStore.onModelContextUpdated(TestModel.createTestContext());
        inMemoryDOMDataStore.registerTreeChangeListener(YangInstanceIdentifier.of(), domDataTreeChangeListener);

        doNothing().when(autoCloseable).close();
        inMemoryDOMDataStore.setCloseable(autoCloseable);
        inMemoryDOMDataStore.close();
        doThrow(UnsupportedOperationException.class).when(autoCloseable).close();
        inMemoryDOMDataStore.close();
        verify(autoCloseable, atLeast(2)).close();
    }
}