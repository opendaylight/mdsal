/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.opendaylight.controller.md.sal.dom.store.impl.TestModel;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;

public class InMemoryDOMDataStoreFactoryTest {

    @Test
    public void basicTest() throws Exception {
        final String testStoreName = "TestStore";
        final DOMSchemaService domSchemaService = mock(DOMSchemaService.class);
        doReturn(null).when(domSchemaService).registerSchemaContextListener(any(SchemaContextListener.class));

        final InMemoryDOMDataStore inMemoryDOMDataStore =
                InMemoryDOMDataStoreFactory.create(testStoreName, domSchemaService);
        assertNotNull(inMemoryDOMDataStore);
        assertEquals(testStoreName, inMemoryDOMDataStore.getIdentifier());

        final DOMDataTreeChangeListener domDataTreeChangeListener = mock(DOMDataTreeChangeListener.class);
        doReturn("testListener").when(domDataTreeChangeListener).toString();
        doNothing().when(domDataTreeChangeListener).onDataTreeChanged(any());
        inMemoryDOMDataStore.onGlobalContextUpdated(TestModel.createTestContext());
        inMemoryDOMDataStore.registerTreeChangeListener(YangInstanceIdentifier.EMPTY, domDataTreeChangeListener);

        final AutoCloseable autoCloseable = mock(AutoCloseable.class);
        doNothing().when(autoCloseable).close();
        inMemoryDOMDataStore.setCloseable(autoCloseable);
        inMemoryDOMDataStore.close();
        verify(autoCloseable).close();
    }
}