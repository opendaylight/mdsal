/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.wiring;

import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.CONFIGURATION;
import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.OPERATIONAL;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Map;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.broker.SerializedDOMDataBroker;
import org.opendaylight.mdsal.dom.spi.store.DOMStore;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMDataStore;

/**
 * Wiring for an in-memory DOMDataBroker, useful for dependency injection (DI) with your favourite (any) DI framework.
 *
 * @author Michael Vorburger.ch
 */
@Singleton
public class InMemoryWiring {

    private final DOMDataBroker domDataBroker;

    @Inject
    public InMemoryWiring(DOMSchemaService schemaService) {
        Map<LogicalDatastoreType, DOMStore> datastores = ImmutableMap.of(
                OPERATIONAL, createDOMStore("OPER", schemaService),
                CONFIGURATION, createDOMStore("CFG", schemaService));

        this.domDataBroker = new SerializedDOMDataBroker(datastores, createCommitCoordinatorExecutor());
    }

    public DOMDataBroker getDOMDataBroker() {
        return domDataBroker;
    }

    protected DOMStore createDOMStore(String name, DOMSchemaService schemaService) {
        InMemoryDOMDataStore store = new InMemoryDOMDataStore(name, createDataTreeChangeListenerExecutor());
        schemaService.registerSchemaContextListener(store);
        return store;
    }

    protected ListeningExecutorService createDataTreeChangeListenerExecutor() {
        return MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
    }

    protected ListeningExecutorService createCommitCoordinatorExecutor() {
        return MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
    }
}
