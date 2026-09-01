/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doNothing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.spi.FixedDOMSchemaService;
import org.opendaylight.mdsal.dom.store.inmemory.dagger.InMemoryDOMStoreFactoryModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.dagger.ReferenceDataTreeFactoryModule;

@ExtendWith(MockitoExtension.class)
class InMemoryDOMDataStoreFactoryTest {
    @Mock
    private DOMDataTreeChangeListener domDataTreeChangeListener;

    @Test
    void basicTest() throws Exception {
        final String testStoreName = "TestStore";

        final var factory = InMemoryDOMStoreFactoryModule.provideInMemoryDOMStoreFactory(
            ReferenceDataTreeFactoryModule.provideDataTreeFactory());
        final var schemaService = new FixedDOMSchemaService(TestModel.createTestContext());

        try (var store = factory.create(testStoreName, DataTreeConfiguration.DEFAULT_OPERATIONAL, schemaService)) {
            assertNotNull(store);
            assertEquals(testStoreName, store.getIdentifier());

            doNothing().when(domDataTreeChangeListener).onInitialData();
            store.registerTreeChangeListener(YangInstanceIdentifier.of(), domDataTreeChangeListener);
        }
    }
}
