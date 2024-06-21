/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.test;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.dom.adapter.AdapterContext;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMDataBrokerAdapter;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMNotificationPublishServiceAdapter;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMNotificationServiceAdapter;
import org.opendaylight.mdsal.binding.dom.adapter.test.util.MockSchemaService;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.broker.DOMNotificationRouter;
import org.opendaylight.mdsal.dom.broker.SerializedDOMDataBroker;
import org.opendaylight.mdsal.dom.spi.store.DOMStore;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMDataStore;
import org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeContext;

public abstract class AbstractDataBrokerTestCustomizer {
    private final DOMNotificationRouter domNotificationRouter = new DOMNotificationRouter(16);
    private final MockSchemaService schemaService = new MockSchemaService();

    private DOMDataBroker domDataBroker;
    private ImmutableMap<LogicalDatastoreType, DOMStore> datastores;

    public ImmutableMap<LogicalDatastoreType, DOMStore> createDatastores() {
        return ImmutableMap.<LogicalDatastoreType, DOMStore>builder()
                .put(LogicalDatastoreType.OPERATIONAL, createOperationalDatastore())
                .put(LogicalDatastoreType.CONFIGURATION,createConfigurationDatastore())
                .build();
    }

    public DOMStore createConfigurationDatastore() {
        final var store = new InMemoryDOMDataStore("CFG", getDataTreeChangeListenerExecutor());
        schemaService.registerSchemaContextListener(store::onModelContextUpdated);
        return store;
    }

    public DOMStore createOperationalDatastore() {
        final var store = new InMemoryDOMDataStore("OPER", getDataTreeChangeListenerExecutor());
        schemaService.registerSchemaContextListener(store::onModelContextUpdated);
        return store;
    }

    public DOMDataBroker createDOMDataBroker() {
        return new SerializedDOMDataBroker(getDatastores(), getCommitCoordinatorExecutor());
    }

    public NotificationService createNotificationService() {
        return new BindingDOMNotificationServiceAdapter(schemaService, domNotificationRouter.notificationService());
    }

    public NotificationPublishService createNotificationPublishService() {
        return new BindingDOMNotificationPublishServiceAdapter(schemaService,
            domNotificationRouter.notificationPublishService());
    }

    public abstract ListeningExecutorService getCommitCoordinatorExecutor();

    public ListeningExecutorService getDataTreeChangeListenerExecutor() {
        return MoreExecutors.newDirectExecutorService();
    }

    public DataBroker createDataBroker() {
        return new BindingDOMDataBrokerAdapter(schemaService, getDOMDataBroker());
    }

    public AdapterContext getAdapterContext() {
        return schemaService;
    }

    public DOMSchemaService getSchemaService() {
        return schemaService;
    }

    public DOMDataBroker getDOMDataBroker() {
        if (domDataBroker == null) {
            domDataBroker = createDOMDataBroker();
        }
        return domDataBroker;
    }

    private synchronized ImmutableMap<LogicalDatastoreType, DOMStore> getDatastores() {
        if (datastores == null) {
            datastores = createDatastores();
        }
        return datastores;
    }

    public void updateSchema(final BindingRuntimeContext ctx) {
        schemaService.changeSchema(ctx);
    }

    public DOMNotificationRouter getDomNotificationRouter() {
        return domNotificationRouter;
    }
}
