/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.test.util;

import static com.google.common.base.Preconditions.checkState;

import org.opendaylight.mdsal.dom.broker.DOMMountPointServiceImpl;

import org.opendaylight.mdsal.dom.broker.DOMNotificationRouter;
import org.opendaylight.mdsal.dom.broker.DOMRpcRouter;
import org.opendaylight.mdsal.dom.broker.SerializedDOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMMountPointService;
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishService;
import org.opendaylight.mdsal.dom.api.DOMNotificationService;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.mdsal.binding.api.RpcConsumerRegistry;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.MountPointService;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMDataBrokerAdapter;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMMountPointServiceAdapter;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMNotificationPublishServiceAdapter;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMNotificationServiceAdapter;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMRpcProviderServiceAdapter;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMRpcServiceAdapter;
import org.opendaylight.mdsal.binding.dom.adapter.BindingToNormalizedNodeCodec;
import com.google.common.annotations.Beta;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MutableClassToInstanceMap;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import javassist.ClassPool;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.dom.store.impl.InMemoryDOMDataStore;
import org.opendaylight.controller.sal.binding.api.BindingAwareService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.controller.sal.binding.impl.RootBindingAwareBroker;
import org.opendaylight.controller.sal.core.api.Broker.ProviderSession;
import org.opendaylight.controller.sal.core.api.BrokerService;
import org.opendaylight.controller.sal.core.spi.data.DOMStore;
import org.opendaylight.controller.sal.dom.broker.BrokerImpl;
import org.opendaylight.yangtools.binding.data.codec.gen.impl.DataObjectSerializerGenerator;
import org.opendaylight.yangtools.binding.data.codec.gen.impl.StreamWriterGenerator;
import org.opendaylight.yangtools.binding.data.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.yangtools.sal.binding.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.yangtools.sal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.yangtools.sal.binding.generator.util.JavassistUtils;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

@Beta
public class BindingTestContext implements AutoCloseable {


    private BindingToNormalizedNodeCodec codec;

    private RootBindingAwareBroker baBrokerImpl;

    private BrokerImpl biBrokerImpl;

    private final ListeningExecutorService executor;
    private final ClassPool classPool;

    private final boolean startWithSchema;

    private DOMMountPointService biMountImpl;

    private ImmutableMap<LogicalDatastoreType, DOMStore> newDatastores;

    private DOMDataBroker newDOMDataBroker;

    private final MockSchemaService mockSchemaService = new MockSchemaService();

    private DataBroker dataBroker;

    private RpcConsumerRegistry baConsumerRpc;

    private BindingDOMRpcProviderServiceAdapter baProviderRpc;
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

    protected BindingTestContext(final ListeningExecutorService executor, final ClassPool classPool, final boolean startWithSchema) {
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
        final InMemoryDOMDataStore operStore = new InMemoryDOMDataStore("OPER", MoreExecutors.sameThreadExecutor());
        final InMemoryDOMDataStore configStore = new InMemoryDOMDataStore("CFG", MoreExecutors.sameThreadExecutor());
        newDatastores = ImmutableMap.<LogicalDatastoreType, DOMStore>builder()
                .put(LogicalDatastoreType.OPERATIONAL, operStore)
                .put(LogicalDatastoreType.CONFIGURATION, configStore)
                .build();

        newDOMDataBroker = new SerializedDOMDataBroker(newDatastores, executor);

        mockSchemaService.registerSchemaContextListener(configStore);
        mockSchemaService.registerSchemaContextListener(operStore);
    }

    public void startBindingDataBroker() {

    }

    public void startBindingBroker() {
        checkState(executor != null, "Executor needs to be set");

        baConsumerRpc = new BindingDOMRpcServiceAdapter(getDomRpcInvoker(), codec);
        baProviderRpc = new BindingDOMRpcProviderServiceAdapter(getDomRpcRegistry(), codec);
        final MountPointService mountService = new BindingDOMMountPointServiceAdapter(biMountImpl, codec);

        final ImmutableClassToInstanceMap<BindingAwareService> consumerServices =
                ImmutableClassToInstanceMap.<BindingAwareService>builder().build();
        final ImmutableClassToInstanceMap<BindingAwareService> providerServices =
                ImmutableClassToInstanceMap.<BindingAwareService>builder().build();;
        baBrokerImpl = new RootBindingAwareBroker("test", consumerServices, providerServices);
    }

    public void startForwarding() {

    }

    private ProviderSession createMockContext() {

        final ClassToInstanceMap<BrokerService> domBrokerServices = ImmutableClassToInstanceMap
                .<BrokerService> builder()
                //
                .put(DOMRpcRouter.class, biBrokerImpl.getRouter()) //
                .put(DOMMountPointService.class, biMountImpl)
                .build();

        return new ProviderSession() {

            @Override
            public <T extends BrokerService> T getService(final Class<T> service) {
                return domBrokerServices.getInstance(service);
            }

            @Override
            public boolean isClosed() {
                return false;
            }

            @Override
            public void close() {
            }
        };
    }

    public void startBindingToDomMappingService() {
        checkState(classPool != null, "ClassPool needs to be present");

        final DataObjectSerializerGenerator generator = StreamWriterGenerator.create(JavassistUtils.forClassPool(classPool));
        final BindingNormalizedNodeCodecRegistry codecRegistry = new BindingNormalizedNodeCodecRegistry(generator);
        final GeneratedClassLoadingStrategy loading = GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy();
        codec = new BindingToNormalizedNodeCodec(loading,  codecRegistry);
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

        final ClassToInstanceMap<BrokerService> services = MutableClassToInstanceMap.create();
        services.put(DOMRpcService.class, domRouter);

        biBrokerImpl = new BrokerImpl(domRouter,services);

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



    public RpcProviderRegistry getBindingRpcRegistry() {
        return baBrokerImpl.getRpcProviderRegistry();
    }

    public DOMRpcProviderService getDomRpcRegistry() {
        return domRouter;
    }

    public DOMRpcService getDomRpcInvoker() {
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
