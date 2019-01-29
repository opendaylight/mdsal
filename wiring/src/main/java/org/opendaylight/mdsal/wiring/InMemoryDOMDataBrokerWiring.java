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
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMDataStoreConfigProperties;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMDataStoreFactory;

/**
 * Wiring for an in-memory DOMDataBroker, useful for dependency injection (DI) with your favourite (any) DI framework.
 *
 * @author Michael Vorburger.ch
 */
@Singleton
public class InMemoryDOMDataBrokerWiring {

    // This class should ideally be in org.opendaylight.mdsal.dom.store.inmemory
    // instead of here in org.opendaylight.mdsal.wiring, but it (currently) cannot,
    // because org.opendaylight.mdsal.dom.broker (from where we need SerializedDOMDataBroker)
    // depends on inmemory (instead of the other way around), and move this would therefore
    // create a cyclic dependency cycle.

    private final DOMDataBroker domDataBroker;

    @Inject
    public InMemoryDOMDataBrokerWiring(DOMSchemaService schemaService,
            InMemoryDOMDataStoreConfigProperties properties) {

        Map<LogicalDatastoreType, DOMStore> datastores = ImmutableMap.of(
                OPERATIONAL, createDOMStore(OPERATIONAL, schemaService, properties),
                CONFIGURATION, createDOMStore(CONFIGURATION, schemaService, properties));

        DOMDataBroker memoryDB = new SerializedDOMDataBroker(datastores, createCommitCoordinatorExecutor());
        this.domDataBroker = new CheckingDOMDataBroker(memoryDB, schemaService);
    }

    public DOMDataBroker getDOMDataBroker() {
        return domDataBroker;
    }

    protected DOMStore createDOMStore(LogicalDatastoreType type, DOMSchemaService schemaService,
            InMemoryDOMDataStoreConfigProperties properties) {
        return InMemoryDOMDataStoreFactory.create(type.name(), properties, schemaService);
    }

    protected ListeningExecutorService createCommitCoordinatorExecutor() {
        return MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
    }
}
