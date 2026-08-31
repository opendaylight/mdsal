/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2026 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;

/**
 * A factory for creating {@link InMemoryDOMDataStore} instances.
 *
 * @author Thomas Pantelis
 */
@NonNullByDefault
public interface InMemoryDOMStoreFactory {
    /**
     * Creates an {@link InMemoryDOMDataStore} instance.
     *
     * @param name the name of the data store
     * @param dataTreeConfig the {@link DataTreeConfiguration} to use
     * @param properties configuration properties for the InMemoryDOMDataStore instance
     * @param schemaService the {@link DOMSchemaService} to which to register the data store
     * @return an InMemoryDOMDataStore instance
     */
    InMemoryDOMDataStore create(String name, DataTreeConfiguration dataTreeConfig,
        InMemoryDOMDataStoreConfigProperties properties, @Nullable DOMSchemaService schemaService);

    /**
     * Creates an {@link InMemoryDOMDataStore} instance with default properties.
     *
     * @param name the name of the data store
     * @param config the {@link DataTreeConfiguration} to use
     * @param schemaService the {@link DOMSchemaService} to which to register the data store
     * @return an InMemoryDOMDataStore instance
     */
    default InMemoryDOMDataStore create(final String name, final DataTreeConfiguration config,
            final @Nullable DOMSchemaService schemaService) {
        return create(name, config, InMemoryDOMDataStoreConfigProperties.getDefault(), schemaService);
    }
}
