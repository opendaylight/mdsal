/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2026 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory.impl;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.ExecutorService;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMStoreConfigProperties;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMStoreFactory;
import org.opendaylight.yangtools.util.concurrent.SpecialExecutors;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Default implementation of {@link InMemoryDOMStoreFactory}.
 */
@Component
@NonNullByDefault
public final class InMemoryDOMStoreFactoryImpl implements InMemoryDOMStoreFactory {
    private final DataTreeFactory dataTreeFactory;

    @Activate
    public InMemoryDOMStoreFactoryImpl(@Reference final DataTreeFactory dataTreeFactory) {
        this.dataTreeFactory = requireNonNull(dataTreeFactory);
    }

    @Override
    public InMemoryDOMStoreImpl create(final String name, final DataTreeConfiguration dataTreeConfig,
            final InMemoryDOMStoreConfigProperties properties, final @Nullable DOMSchemaService schemaService) {
        final var dataChangeListenerExecutor = createExecutorService(name, properties);
        final var dataStore = new InMemoryDOMStoreImpl(name, dataTreeFactory, dataTreeConfig,
            dataChangeListenerExecutor, properties.getMaxDataChangeListenerQueueSize(),
            properties.getDebugTransactions());

        if (schemaService != null) {
            schemaService.registerSchemaContextListener(dataStore::onModelContextUpdated);
        }

        return dataStore;
    }

    private static ExecutorService createExecutorService(final String name,
            final InMemoryDOMStoreConfigProperties props) {
        // For DataChangeListener notifications we use an executor that provides the fastest task execution time to get
        // higher throughput as DataChangeListeners typically provide much of the business logic for a data model.
        // If the executor queue size limit is reached, subsequent submitted notifications will block the calling
        // thread.
        return SpecialExecutors.newBlockingBoundedFastThreadPool(
            props.getMaxDataChangeExecutorPoolSize(), props.getMaxDataChangeExecutorQueueSize(),
            name + "-DCL", InMemoryDOMStoreFactory.class);
    }
}
