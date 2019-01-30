/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.spi;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.mdsal.binding.api.ActionProviderService;
import org.opendaylight.mdsal.binding.api.ActionService;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.MountPointService;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.api.RpcConsumerRegistry;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.binding.dom.adapter.BindingWiring;
import org.opendaylight.mdsal.dom.api.DOMActionProviderService;
import org.opendaylight.mdsal.dom.api.DOMActionService;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMMountPointService;
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishService;
import org.opendaylight.mdsal.dom.api.DOMNotificationService;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMRpcService;

/**
 * Wiring for dependency injection (DI).
 *
 * @see BindingWiring
 *
 * @author Michael Vorburger.ch
 */
@Singleton
public class AdapterFactoryWiring {

    private final DataBroker dataBroker;
    // private final DataTreeService dataTreeService;
    private final NotificationService notificationService;
    private final NotificationPublishService notificationPublishService;
    private final MountPointService mountPointService;
    private final RpcConsumerRegistry rpcConsumerRegistry;
    private final RpcProviderService rpcProviderService;
    private final ActionService actionService;
    private final ActionProviderService actionProviderService;

    @Inject
    public AdapterFactoryWiring(BindingWiring bindingWiring, DOMDataBroker domDataBroker,
            DOMNotificationService domNotificationService, DOMNotificationPublishService domNotificationPublishService,
            DOMMountPointService domMountPointService, DOMRpcService domRpcConsumerRegistry,
            DOMRpcProviderService domRpcProviderService, DOMActionService domActionService,
            DOMActionProviderService domActionProviderService) {
        AdapterFactory adapterFactory = bindingWiring.getAdapterFactory();
        dataBroker = adapterFactory.createDataBroker(domDataBroker);
        // dataTreeService = adapterFactory.createDataTreeService(domDataTreeService);
        notificationService = adapterFactory.createNotificationService(domNotificationService);
        notificationPublishService = adapterFactory.createNotificationPublishService(domNotificationPublishService);
        mountPointService = adapterFactory.createMountPointService(domMountPointService);
        rpcConsumerRegistry = adapterFactory.createRpcConsumerRegistry(domRpcConsumerRegistry);
        rpcProviderService = adapterFactory.createRpcProviderService(domRpcProviderService);
        actionService = adapterFactory.createActionService(domActionService);
        actionProviderService = adapterFactory.createActionProviderService(domActionProviderService);
    }

    public DataBroker getDataBroker() {
        return dataBroker;
    }

//  public DataTreeService getDataTreeService() {
//      return dataTreeService;
//  }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    public NotificationPublishService getNotificationPublishService() {
        return notificationPublishService;
    }

    public MountPointService getMountPointService() {
        return mountPointService;
    }

    public RpcConsumerRegistry getRpcConsumerRegistry() {
        return rpcConsumerRegistry;
    }

    public RpcProviderService getRpcProviderService() {
        return rpcProviderService;
    }

    public ActionService getActionService() {
        return actionService;
    }

    public ActionProviderService getActionProviderService() {
        return actionProviderService;
    }
}
