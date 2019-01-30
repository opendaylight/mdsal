/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.wiring.guice;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import javax.inject.Singleton;
import org.opendaylight.mdsal.dom.api.DOMActionProviderService;
import org.opendaylight.mdsal.dom.api.DOMActionService;
import org.opendaylight.mdsal.dom.api.DOMMountPointService;
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishService;
import org.opendaylight.mdsal.dom.api.DOMNotificationService;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.mdsal.dom.broker.DOMBrokerWiring;
import org.opendaylight.mdsal.dom.broker.DOMNotificationRouterConfig;
import org.opendaylight.mdsal.dom.spi.DOMNotificationSubscriptionListenerRegistry;

/**
 * Guice Module which binds an DOM broker services.
 *
 * @author Michael Vorburger.ch
 */
public class DOMBrokerModule implements Module {

    private final DOMNotificationRouterConfig domNotificationRouterConfig;

    public DOMBrokerModule(DOMNotificationRouterConfig domNotificationRouterConfig) {
        this.domNotificationRouterConfig = domNotificationRouterConfig;
    }

    public DOMBrokerModule() {
        // defaults copied from dom-broker.xml blueprint
        this(new DOMNotificationRouterConfig.Immutable(65536, 0, 0, MILLISECONDS));
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(DOMNotificationRouterConfig.class).toInstance(domNotificationRouterConfig);
    }

    @Provides
    @Singleton DOMRpcService getDOMRpcService(DOMBrokerWiring wiring) {
        return wiring.getDOMRpcService();
    }

    @Provides
    @Singleton DOMRpcProviderService getDOMRpcProviderService(DOMBrokerWiring wiring) {
        return wiring.getDOMRpcProviderService();
    }

    @Provides
    @Singleton DOMNotificationService getDOMNotificationService(DOMBrokerWiring wiring) {
        return wiring.getDOMNotificationService();
    }

    @Provides
    @Singleton DOMNotificationPublishService getDOMNotificationPublishService(DOMBrokerWiring wiring) {
        return wiring.getDOMNotificationPublishService();
    }

    @Provides
    @Singleton DOMActionProviderService getDOMActionProviderService(DOMBrokerWiring wiring) {
        return wiring.getDOMActionProviderService();
    }

    @Provides
    @Singleton DOMActionService getDOMActionService(DOMBrokerWiring wiring) {
        return wiring.getDOMActionService();
    }

    @Provides
    @Singleton DOMMountPointService getDOMMountPointService(DOMBrokerWiring wiring) {
        return wiring.getDOMMountPointService();
    }

    @Provides
    @Singleton
    DOMNotificationSubscriptionListenerRegistry getDOMNotificationSubscriptionListenerRegistry(DOMBrokerWiring wiring) {
        return wiring.getDOMNotificationSubscriptionListenerRegistry();
    }
}
