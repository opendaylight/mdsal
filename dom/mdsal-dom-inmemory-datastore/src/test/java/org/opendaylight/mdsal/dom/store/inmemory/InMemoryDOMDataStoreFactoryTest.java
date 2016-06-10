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

import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;

public class InMemoryDOMDataStoreFactoryTest {

    @Test
    public void createTest() throws Exception {
        final String testStoreName = "TestStore";
        final DOMSchemaService domSchemaService = Mockito.mock(DOMSchemaService.class);
        Mockito.doReturn(null).when(domSchemaService).registerSchemaContextListener(any(SchemaContextListener.class));

        final InMemoryDOMDataStore inMemoryDOMDataStore =
                InMemoryDOMDataStoreFactory.create(testStoreName, domSchemaService);
        assertNotNull(inMemoryDOMDataStore);
        assertEquals(testStoreName, inMemoryDOMDataStore.getIdentifier());
    }
}