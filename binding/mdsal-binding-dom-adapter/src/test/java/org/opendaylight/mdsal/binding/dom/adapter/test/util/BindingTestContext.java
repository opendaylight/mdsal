/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.test.util;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Set;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.MountPointService;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.binding.api.RpcService;
import org.opendaylight.mdsal.binding.dom.adapter.AdapterContext;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMDataBrokerAdapter;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMMountPointServiceAdapter;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMNotificationPublishServiceAdapter;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMNotificationServiceAdapter;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMRpcProviderServiceAdapter;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMRpcServiceAdapter;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMMountPointService;
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishService;
import org.opendaylight.mdsal.dom.api.DOMNotificationService;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.mdsal.dom.broker.DOMMountPointServiceImpl;
import org.opendaylight.mdsal.dom.broker.DOMNotificationRouter;
import org.opendaylight.mdsal.dom.broker.DOMRpcRouter;
import org.opendaylight.mdsal.dom.broker.SerializedDOMDataBroker;
import org.opendaylight.mdsal.dom.spi.store.DOMStore;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMDataStore;
import org.opendaylight.yangtools.binding.meta.YangModuleInfo;
import org.opendaylight.yangtools.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

@Beta
public class BindingTestContext implements AutoCloseable {
    private final ListeningExecutorService executor;

    private final boolean startWithSchema;

    private DOMMountPointService biMountImpl;

    private ImmutableMap<LogicalDatastoreType, DOMStore> newDatastores;

    private DOMDataBroker newDOMDataBroker;

    private final MockSchemaService mockSchemaService = new MockSchemaService();

    private DataBroker dataBroker;

    private RpcService baConsumerRpc;

    private BindingDOMRpcProviderServiceAdapter baProviderRpc;
    private DOMRpcRouter domRouter;

    private NotificationPublishService publishService;

    private NotificationService listenService;

    private DOMNotificationPublishService domPublishService;

    private DOMNotificationService domListenService;

    private Set<YangModuleInfo> schemaModuleInfos;

    public DOMDataBroker getDomAsyncDataBroker() {
        return newDOMDataBroker;
    }

    public AdapterContext getCodec() {
        return mockSchemaService;
    }

    protected BindingTestContext(final ListeningExecutorService executor, final boolean startWithSchema) {
        this.executor = executor;
        this.startWithSchema = startWithSchema;
    }

    public void startDomDataBroker() {
    }

    public void startNewDataBroker() {
        checkState(executor != null, "Executor needs to be set");
        checkState(newDOMDataBroker != null, "DOM Data Broker must be set");
        dataBroker = new BindingDOMDataBrokerAdapter(mockSchemaService, newDOMDataBroker);
    }

    public void startNewDomDataBroker() {
        checkState(executor != null, "Executor needs to be set");
        final var operStore = new InMemoryDOMDataStore("OPER", MoreExecutors.newDirectExecutorService());
        final var configStore = new InMemoryDOMDataStore("CFG", MoreExecutors.newDirectExecutorService());
        newDatastores = ImmutableMap.<LogicalDatastoreType, DOMStore>builder()
                .put(LogicalDatastoreType.OPERATIONAL, operStore)
                .put(LogicalDatastoreType.CONFIGURATION, configStore)
                .build();

        newDOMDataBroker = new SerializedDOMDataBroker(newDatastores, executor);

        mockSchemaService.registerSchemaContextListener(configStore::onModelContextUpdated);
        mockSchemaService.registerSchemaContextListener(operStore::onModelContextUpdated);
    }

    public void startBindingDataBroker() {

    }

    public void startBindingBroker() {
        checkState(executor != null, "Executor needs to be set");

        baConsumerRpc = new BindingDOMRpcServiceAdapter(mockSchemaService, getDomRpcInvoker());
        baProviderRpc = new BindingDOMRpcProviderServiceAdapter(mockSchemaService, getDomRpcRegistry());
        final MountPointService mountService = new BindingDOMMountPointServiceAdapter(mockSchemaService, biMountImpl);
    }

    public void startForwarding() {

    }

    public void startBindingToDomMappingService() {
        // No-op, really
    }

    private void updateYangSchema(final Set<YangModuleInfo> moduleInfos) {
        mockSchemaService.changeSchema(BindingRuntimeHelpers.createRuntimeContext(moduleInfos));
    }

    public EffectiveModelContext getContext() {
        return mockSchemaService.getGlobalContext();
    }

    public void start() {
        startNewDomDataBroker();

        startDomBroker();
        startDomMountPoint();
        startBindingToDomMappingService();
        startNewDataBroker();
        startBindingNotificationBroker();
        startBindingBroker();

        startForwarding();

        if (schemaModuleInfos != null) {
            updateYangSchema(schemaModuleInfos);
        } else if (startWithSchema) {
            loadYangSchemaFromClasspath();
        }
    }

    private void startDomMountPoint() {
        biMountImpl = new DOMMountPointServiceImpl();
    }

    private void startDomBroker() {
        checkState(executor != null);
        domRouter = new DOMRpcRouter(mockSchemaService);
    }

    public void startBindingNotificationBroker() {
        checkState(executor != null);
        final var router = new DOMNotificationRouter(16);
        domPublishService = router.notificationPublishService();
        domListenService = router.notificationService();
        publishService = new BindingDOMNotificationPublishServiceAdapter(mockSchemaService, domPublishService);
        listenService = new BindingDOMNotificationServiceAdapter(mockSchemaService, domListenService);

    }

    public void loadYangSchemaFromClasspath() {
        updateYangSchema(BindingRuntimeHelpers.loadModuleInfos());
    }

    public DOMRpcProviderService getDomRpcRegistry() {
        return domRouter.rpcProviderService();
    }

    public DOMRpcService getDomRpcInvoker() {
        return domRouter.rpcService();
    }

    public RpcProviderService getBindingRpcProviderRegistry() {
        return baProviderRpc;
    }

    public RpcService getBindingRpcService() {
        return baConsumerRpc;
    }

    @Override
    public void close() throws Exception {

    }

    public DOMMountPointService getDomMountProviderService() {
        return biMountImpl;
    }

    public DataBroker getDataBroker() {
        return dataBroker;
    }

    public void setSchemaModuleInfos(final Set<YangModuleInfo> moduleInfos) {
        schemaModuleInfos = moduleInfos;
    }
}
