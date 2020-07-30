/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.osgi;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.opendaylight.mdsal.binding.api.ActionProviderService;
import org.opendaylight.mdsal.binding.api.ActionService;
import org.opendaylight.mdsal.binding.api.BindingService;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeService;
import org.opendaylight.mdsal.binding.api.MountPointService;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.api.RpcConsumerRegistry;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.binding.dom.adapter.spi.AdapterFactory;
import org.opendaylight.mdsal.dom.api.DOMActionProviderService;
import org.opendaylight.mdsal.dom.api.DOMActionService;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataTreeService;
import org.opendaylight.mdsal.dom.api.DOMMountPointService;
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishService;
import org.opendaylight.mdsal.dom.api.DOMNotificationService;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A component which watches the OSGi Service Registry for known {@link DOMService}s and publishes corresponding
 * {@link BindingService}s backed by them.
 *
 * @author Robert Varga
 */
@Component(immediate = true)
public final class DynamicBindingAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(DynamicBindingAdapter.class);

    private List<AbstractAdaptingTracker<?, ?, ?>> trackers = ImmutableList.of();

    @Reference
    AdapterFactory factory = null;
    @Reference(target = "(component.factory=" + OSGiActionService.FACTORY_NAME + ")")
    ComponentFactory actionServiceFactory = null;
    @Reference(target = "(component.factory=" + OSGiActionProviderService.FACTORY_NAME + ")")
    ComponentFactory actionProviderServiceFactory = null;
    @Reference(target = "(component.factory=" + OSGiDataBroker.FACTORY_NAME + ")")
    ComponentFactory dataBrokerFactory = null;
    @Reference(target = "(component.factory=" + OSGiMountPointService.FACTORY_NAME + ")")
    ComponentFactory mountPointServiceFactory = null;
    @Reference(target = "(component.factory=" + OSGiNotificationService.FACTORY_NAME + ")")
    ComponentFactory notificationServiceFactory = null;
    @Reference(target = "(component.factory=" + OSGiNotificationPublishService.FACTORY_NAME + ")")
    ComponentFactory notificationPublishServiceFactory = null;
    @Reference(target = "(component.factory=" + OSGiRpcConsumerRegistry.FACTORY_NAME + ")")
    ComponentFactory rpcConsumerRegistryFactory = null;
    @Reference(target = "(component.factory=" + OSGiRpcProviderService.FACTORY_NAME + ")")
    ComponentFactory rpcProviderServiceFactory = null;

    @Activate
    void activate(final BundleContext ctx) {
        trackers = ImmutableList.of(
            new AdaptingComponentTracker<>(ctx, DOMDataBroker.class, DataBroker.class, factory::createDataBroker,
                    dataBrokerFactory),
            new AdaptingTracker<>(ctx, DOMDataTreeService.class, DataTreeService.class, factory::createDataTreeService),
            new AdaptingComponentTracker<>(ctx, DOMMountPointService.class, MountPointService.class,
                    factory::createMountPointService, mountPointServiceFactory),
            new AdaptingComponentTracker<>(ctx, DOMNotificationService.class, NotificationService.class,
                    factory::createNotificationService, notificationServiceFactory),
            new AdaptingComponentTracker<>(ctx, DOMNotificationPublishService.class, NotificationPublishService.class,
                    factory::createNotificationPublishService, notificationPublishServiceFactory),
            new AdaptingComponentTracker<>(ctx, DOMRpcService.class, RpcConsumerRegistry.class,
                    factory::createRpcConsumerRegistry, rpcConsumerRegistryFactory),
            new AdaptingComponentTracker<>(ctx, DOMRpcProviderService.class, RpcProviderService.class,
                    factory::createRpcProviderService, rpcProviderServiceFactory),
            new AdaptingComponentTracker<>(ctx, DOMActionService.class, ActionService.class,
                    factory::createActionService, actionServiceFactory),
            new AdaptingComponentTracker<>(ctx, DOMActionProviderService.class, ActionProviderService.class,
                factory::createActionProviderService, actionProviderServiceFactory));

        LOG.debug("Starting {} DOMService trackers", trackers.size());
        trackers.forEach(ServiceTracker::open);
        LOG.info("{} DOMService trackers started", trackers.size());
    }

    @Deactivate
    void deactivate() {
        LOG.debug("Stopping {} DOMService trackers", trackers.size());
        if (!trackers.isEmpty()) {
            trackers.forEach(AbstractAdaptingTracker::close);
            LOG.info("{} DOMService trackers stopped", trackers.size());
        }
        trackers = ImmutableList.of();
    }
}
