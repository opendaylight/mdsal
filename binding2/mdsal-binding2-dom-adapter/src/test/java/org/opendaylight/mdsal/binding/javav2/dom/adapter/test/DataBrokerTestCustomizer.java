/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.test;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import javassist.ClassPool;
import org.opendaylight.mdsal.binding.javav2.api.DataBroker;
import org.opendaylight.mdsal.binding.javav2.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.javav2.api.NotificationService;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.data.BindingDOMDataBrokerAdapter;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.notification.BindingDOMNotificationPublishServiceAdapter;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.notification.BindingDOMNotificationServiceAdapter;
import org.opendaylight.mdsal.binding.javav2.dom.codec.generator.api.TreeNodeSerializerGenerator;
import org.opendaylight.mdsal.binding.javav2.dom.codec.generator.impl.StreamWriterGenerator;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.mdsal.binding.javav2.runtime.javassist.JavassistUtils;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.broker.DOMNotificationRouter;
import org.opendaylight.mdsal.dom.broker.SerializedDOMDataBroker;
import org.opendaylight.mdsal.dom.spi.store.DOMStore;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMDataStore;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

@Beta
public class DataBrokerTestCustomizer {

    private DOMDataBroker domDataBroker;
    private final DOMNotificationRouter domNotificationRouter;
    private final MockSchemaService schemaService;
    private ImmutableMap<LogicalDatastoreType, DOMStore> datastores;
    private final BindingToNormalizedNodeCodec bindingToNormalized;

    public ImmutableMap<LogicalDatastoreType, DOMStore> createDatastores() {
        return ImmutableMap.<LogicalDatastoreType, DOMStore>builder()
                .put(LogicalDatastoreType.OPERATIONAL, createOperationalDatastore())
                .put(LogicalDatastoreType.CONFIGURATION, createConfigurationDatastore()).build();
    }

    public DataBrokerTestCustomizer() {
        schemaService = new MockSchemaService();
        final ClassPool pool = ClassPool.getDefault();
        final TreeNodeSerializerGenerator generator = StreamWriterGenerator.create(JavassistUtils.forClassPool(pool));
        final BindingNormalizedNodeCodecRegistry codecRegistry = new BindingNormalizedNodeCodecRegistry(generator);
        final GeneratedClassLoadingStrategy loading =
                (GeneratedClassLoadingStrategy) GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy();
        bindingToNormalized = new BindingToNormalizedNodeCodec(loading, codecRegistry);
        schemaService.registerSchemaContextListener(bindingToNormalized);
        domNotificationRouter = DOMNotificationRouter.create(16);
    }

    public DOMStore createConfigurationDatastore() {
        final InMemoryDOMDataStore store = new InMemoryDOMDataStore("CFG", MoreExecutors.newDirectExecutorService());
        schemaService.registerSchemaContextListener(store);
        return store;
    }

    public DOMStore createOperationalDatastore() {
        final InMemoryDOMDataStore store = new InMemoryDOMDataStore("OPER", MoreExecutors.newDirectExecutorService());
        schemaService.registerSchemaContextListener(store);
        return store;
    }

    public DOMDataBroker createDOMDataBroker() {
        return new SerializedDOMDataBroker(getDatastores(), getCommitCoordinatorExecutor());
    }

    public NotificationService createNotificationService() {
        return new BindingDOMNotificationServiceAdapter(bindingToNormalized.getCodecRegistry(), domNotificationRouter);
    }

    public NotificationPublishService createNotificationPublishService() {
        return new BindingDOMNotificationPublishServiceAdapter(bindingToNormalized, domNotificationRouter);
    }

    public ListeningExecutorService getCommitCoordinatorExecutor() {
        return MoreExecutors.newDirectExecutorService();
    }

    public DataBroker createDataBroker() {
        return new BindingDOMDataBrokerAdapter(getDOMDataBroker(), bindingToNormalized);
    }

    public BindingToNormalizedNodeCodec getBindingToNormalized() {
        return bindingToNormalized;
    }

    public DOMSchemaService getSchemaService() {
        return schemaService;
    }

    private DOMDataBroker getDOMDataBroker() {
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

    public void updateSchema(final SchemaContext ctx) {
        schemaService.changeSchema(ctx);
    }

    public DOMNotificationRouter getDomNotificationRouter() {
        return domNotificationRouter;
    }
}
