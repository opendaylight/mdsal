/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.mdsal.dom.api.DOMActionProviderService;
import org.opendaylight.mdsal.dom.api.DOMActionService;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMMountPointService;
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishService;
import org.opendaylight.mdsal.dom.api.DOMNotificationService;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.broker.pingpong.PingPongDataBroker;
import org.opendaylight.mdsal.dom.spi.DOMNotificationSubscriptionListenerRegistry;

/**
 * Wiring for dependency injection (DI).
 *
 * <p>This class does not depend on any particular DI framework.
 *
 * @author Michael Vorburger.ch
 */
@Singleton
public class DOMBrokerWiring {

    private final DOMNotificationRouter domNotificationRouter;
    private final DOMRpcRouter domRpcRouter;
    private final DOMMountPointServiceImpl domMountPointService;
    private final PingPongDataBroker pingPongDOMDataBroker;

    @Inject
    public DOMBrokerWiring(DOMNotificationRouterConfig notificationConfig, DOMSchemaService schemaService,
            DOMDataBroker defaultDOMDataBroker) {
        domNotificationRouter = DOMNotificationRouter.create(notificationConfig.queueDepth(),
                notificationConfig.spinTime(), notificationConfig.parkTime(), notificationConfig.timeUnit());
        domRpcRouter = DOMRpcRouter.newInstance(schemaService);
        domMountPointService = new DOMMountPointServiceImpl();
        pingPongDOMDataBroker = new PingPongDataBroker(defaultDOMDataBroker);
    }

    @PreDestroy
    public void close() {
        domNotificationRouter.close(); // This was actually missing in the original BP dom-broker.xml
        domRpcRouter.close();
    }

    public DOMNotificationService getDOMNotificationService() {
        return domNotificationRouter;
    }

    public DOMNotificationPublishService getDOMNotificationPublishService() {
        return domNotificationRouter;
    }

    public DOMNotificationSubscriptionListenerRegistry getDOMNotificationSubscriptionListenerRegistry() {
        return domNotificationRouter;
    }

    public DOMRpcService getDOMRpcService() {
        return domRpcRouter.getRpcService();
    }

    public DOMRpcProviderService getDOMRpcProviderService() {
        return domRpcRouter.getRpcProviderService();
    }

    public DOMActionService getDOMActionService() {
        return domRpcRouter.getActionService();
    }

    public DOMActionProviderService getDOMActionProviderService() {
        return domRpcRouter.getActionProviderService();
    }

    public DOMMountPointService getDOMMountPointService() {
        return domMountPointService;
    }

    public DOMDataBroker getPingPongDOMDataBroker() {
        return pingPongDOMDataBroker;
    }
}
