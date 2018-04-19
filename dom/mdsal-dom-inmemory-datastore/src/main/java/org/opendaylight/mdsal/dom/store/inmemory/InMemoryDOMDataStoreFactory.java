/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import java.util.concurrent.ExecutorService;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.yangtools.util.concurrent.SpecialExecutors;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathSchemaContextFactory;

/**
 * A factory for creating InMemoryDOMDataStore instances.
 *
 * @author Thomas Pantelis
 */
@NonNullByDefault
public final class InMemoryDOMDataStoreFactory {

    private InMemoryDOMDataStoreFactory() {
    }

    /**
     * Creates an InMemoryDOMDataStore instance with default properties.
     *
     * @param name the name of the data store
     * @param schemaService the SchemaService to which to register the data store.
     * @return an InMemoryDOMDataStore instance
     */
    public static InMemoryDOMDataStore create(final String name, final @Nullable DOMSchemaService schemaService) {
        return create(name, InMemoryDOMDataStoreConfigProperties.getDefault(), schemaService);
    }

    /**
     * Creates an XPathAwareInMemoryDOMDataStore instance with default properties.
     *
     * @param name the name of the data store
     * @param xpathContextFactory XPathSchemaContextFactory to use
     * @param schemaService the SchemaService to which to register the data store.
     * @return an InMemoryDOMDataStore instance
     */
    public static XPathAwareInMemoryDOMDataStore create(final String name,
            final XPathSchemaContextFactory xpathContextFactory, final @Nullable DOMSchemaService schemaService) {
        return create(name, InMemoryDOMDataStoreConfigProperties.getDefault(), xpathContextFactory, schemaService);
    }

    /**
     * Creates an InMemoryDOMDataStore instance.
     *
     * @param name the name of the data store
     * @param properties configuration properties for the InMemoryDOMDataStore instance.
     * @param schemaService the SchemaService to which to register the data store.
     * @return an InMemoryDOMDataStore instance
     */
    public static InMemoryDOMDataStore create(final String name, final InMemoryDOMDataStoreConfigProperties properties,
            @Nullable final DOMSchemaService schemaService) {
        final ExecutorService dataChangeListenerExecutor = createExecutorService(name, properties);
        final InMemoryDOMDataStore dataStore = new InMemoryDOMDataStore(name, dataChangeListenerExecutor,
            properties.getMaxDataChangeListenerQueueSize(), properties.getDebugTransactions());

        if (schemaService != null) {
            schemaService.registerSchemaContextListener(dataStore);
        }

        return dataStore;
    }

    /**
     * Creates an XPathAwareInMemoryDOMDataStore instance.
     *
     * @param name the name of the data store
     * @param properties configuration properties for the InMemoryDOMDataStore instance.
     * @param xpathContextFactory XPathSchemaContextFactory to use
     * @param schemaService the SchemaService to which to register the data store.
     * @return an InMemoryDOMDataStore instance
     */
    public static XPathAwareInMemoryDOMDataStore create(final String name,
            final InMemoryDOMDataStoreConfigProperties properties, final XPathSchemaContextFactory xpathContextFactory,
            @Nullable final DOMSchemaService schemaService) {
        final ExecutorService dataChangeListenerExecutor = createExecutorService(name, properties);
        final XPathAwareInMemoryDOMDataStore dataStore = new XPathAwareInMemoryDOMDataStore(name,
            dataChangeListenerExecutor, properties.getMaxDataChangeListenerQueueSize(),
            properties.getDebugTransactions(), xpathContextFactory);

        if (schemaService != null) {
            schemaService.registerSchemaContextListener(dataStore);
        }

        return dataStore;
    }

    private static ExecutorService createExecutorService(final String name,
            final InMemoryDOMDataStoreConfigProperties props) {
        // For DataChangeListener notifications we use an executor that provides the fastest
        // task execution time to get higher throughput as DataChangeListeners typically provide
        // much of the business logic for a data model. If the executor queue size limit is reached,
        // subsequent submitted notifications will block the calling thread.
        return SpecialExecutors.newBlockingBoundedFastThreadPool(
            props.getMaxDataChangeExecutorPoolSize(), props.getMaxDataChangeExecutorQueueSize(),
            name + "-DCL", InMemoryDOMDataStore.class);
    }
}
