/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.test;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import javassist.ClassPool;
import org.opendaylight.mdsal.binding.javav2.api.DataBroker;
import org.opendaylight.mdsal.binding.javav2.api.MountPointService;
import org.opendaylight.mdsal.binding.javav2.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.javav2.api.NotificationService;
import org.opendaylight.mdsal.binding.javav2.api.RpcActionConsumerRegistry;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.data.BindingDOMDataBrokerAdapter;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.mountpoint.BindingDOMMountPointServiceAdapter;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.notification.BindingDOMNotificationPublishServiceAdapter;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.notification.BindingDOMNotificationServiceAdapter;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.operation.BindingDOMOperationProviderServiceAdapter;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.operation.BindingDOMOperationServiceAdapter;
import org.opendaylight.mdsal.binding.javav2.dom.codec.generator.api.TreeNodeSerializerGenerator;
import org.opendaylight.mdsal.binding.javav2.dom.codec.generator.impl.StreamWriterGenerator;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.mdsal.binding.javav2.runtime.context.ModuleInfoBackedContext;
import org.opendaylight.mdsal.binding.javav2.runtime.javassist.JavassistUtils;
import org.opendaylight.mdsal.binding.javav2.runtime.reflection.BindingReflections;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.YangModuleInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMMountPointService;
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishService;
import org.opendaylight.mdsal.dom.api.DOMNotificationService;
import org.opendaylight.mdsal.dom.api.DOMOperationProviderService;
import org.opendaylight.mdsal.dom.api.DOMOperationService;
import org.opendaylight.mdsal.dom.broker.DOMMountPointServiceImpl;
import org.opendaylight.mdsal.dom.broker.DOMNotificationRouter;
import org.opendaylight.mdsal.dom.broker.DOMRpcRouter;
import org.opendaylight.mdsal.dom.broker.SerializedDOMDataBroker;
import org.opendaylight.mdsal.dom.spi.store.DOMStore;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMDataStore;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

@Beta
public class BindingTestContext implements AutoCloseable {

    private final MockSchemaService mockSchemaService = new MockSchemaService();
    private final ListeningExecutorService executor;
    private final ClassPool classPool;
    private final boolean startWithSchema;

    private DOMMountPointService biMountImpl;
    private BindingToNormalizedNodeCodec codec;
    private ImmutableMap<LogicalDatastoreType, DOMStore> newDatastores;
    private DOMDataBroker newDOMDataBroker;
    private DataBroker dataBroker;
    private RpcActionConsumerRegistry baConsumerRpc;
    private BindingDOMOperationProviderServiceAdapter baProviderRpc;
    private DOMRpcRouter domRouter;
    private NotificationPublishService publishService;
    private NotificationService listenService;
    private DOMNotificationPublishService domPublishService;
    private DOMNotificationService domListenService;

    public DOMDataBroker getDomAsyncDataBroker() {
        return newDOMDataBroker;
    }

    public BindingToNormalizedNodeCodec getCodec() {
        return codec;
    }

    protected BindingTestContext(final ListeningExecutorService executor, final ClassPool classPool,
            final boolean startWithSchema) {
        this.executor = executor;
        this.classPool = classPool;
        this.startWithSchema = startWithSchema;
    }

    public void startDomDataBroker() {
    }

    public void startNewDataBroker() {
        checkState(executor != null, "Executor needs to be set");
        checkState(newDOMDataBroker != null, "DOM Data Broker must be set");
        dataBroker = new BindingDOMDataBrokerAdapter(newDOMDataBroker, codec);
    }

    public void startNewDomDataBroker() {
        checkState(executor != null, "Executor needs to be set");
        final InMemoryDOMDataStore operStore =
                new InMemoryDOMDataStore("OPER", MoreExecutors.newDirectExecutorService());
        final InMemoryDOMDataStore configStore =
                new InMemoryDOMDataStore("CFG", MoreExecutors.newDirectExecutorService());
        newDatastores =
                ImmutableMap.<LogicalDatastoreType, DOMStore>builder().put(LogicalDatastoreType.OPERATIONAL, operStore)
                        .put(LogicalDatastoreType.CONFIGURATION, configStore).build();

        newDOMDataBroker = new SerializedDOMDataBroker(newDatastores, executor);

        mockSchemaService.registerSchemaContextListener(configStore);
        mockSchemaService.registerSchemaContextListener(operStore);
    }

    public void startBindingDataBroker() {

    }

    public void startBindingBroker() {
        checkState(executor != null, "Executor needs to be set");

        baConsumerRpc = new BindingDOMOperationServiceAdapter(getDomOperationInvoker(), codec);
        baProviderRpc = new BindingDOMOperationProviderServiceAdapter(getDomOperationRegistry(), codec);
        final MountPointService mountService = new BindingDOMMountPointServiceAdapter(biMountImpl, codec);
    }

    public void startForwarding() {

    }

    public void startBindingToDomMappingService() {
        checkState(classPool != null, "ClassPool needs to be present");

        final TreeNodeSerializerGenerator generator =
                StreamWriterGenerator.create(JavassistUtils.forClassPool(classPool));
        final BindingNormalizedNodeCodecRegistry codecRegistry = new BindingNormalizedNodeCodecRegistry(generator);
        final GeneratedClassLoadingStrategy loading =
                (GeneratedClassLoadingStrategy) GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy();
        codec = new BindingToNormalizedNodeCodec(loading, codecRegistry);
        mockSchemaService.registerSchemaContextListener(codec);
    }

    private void updateYangSchema(final ImmutableSet<YangModuleInfo> moduleInfos) {
        mockSchemaService.changeSchema(getContext(moduleInfos));
    }

    private SchemaContext getContext(final ImmutableSet<YangModuleInfo> moduleInfos) {
        final ModuleInfoBackedContext ctx = ModuleInfoBackedContext.create();
        ctx.addModuleInfos(moduleInfos);
        return ctx.tryToCreateSchemaContext().get();
    }

    public SchemaContext getContext() {
        return mockSchemaService.getSchemaContext();
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
        if (startWithSchema) {
            loadYangSchemaFromClasspath();
        }
    }

    private void startDomMountPoint() {
        biMountImpl = new DOMMountPointServiceImpl();
    }

    private void startDomBroker() {
        checkState(executor != null);
        domRouter = new DOMRpcRouter();
        mockSchemaService.registerSchemaContextListener(domRouter);
    }

    public void startBindingNotificationBroker() {
        checkState(executor != null);
        final DOMNotificationRouter router = DOMNotificationRouter.create(16);
        domPublishService = router;
        domListenService = router;
        publishService = new BindingDOMNotificationPublishServiceAdapter(codec, domPublishService);
        listenService = new BindingDOMNotificationServiceAdapter(codec, domListenService);

    }

    public void loadYangSchemaFromClasspath() {
        final ImmutableSet<YangModuleInfo> moduleInfos = BindingReflections.loadModuleInfos();
        updateYangSchema(moduleInfos);
    }

    public DOMOperationProviderService getDomOperationRegistry() {
        return domRouter;
    }

    public DOMOperationService getDomOperationInvoker() {
        return domRouter;
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
}
