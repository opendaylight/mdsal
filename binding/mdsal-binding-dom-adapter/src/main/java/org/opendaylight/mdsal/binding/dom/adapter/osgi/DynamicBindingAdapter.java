/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.osgi;

import static org.opendaylight.mdsal.binding.dom.adapter.osgi.AdaptingTracker.DEFAULT_SERVICE_TYPE;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.mdsal.binding.api.BindingService;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeService;
import org.opendaylight.mdsal.binding.api.MountPointService;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.api.RpcConsumerRegistry;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.binding.dom.adapter.AdapterFactory;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataTreeService;
import org.opendaylight.mdsal.dom.api.DOMMountPointService;
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishService;
import org.opendaylight.mdsal.dom.api.DOMNotificationService;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A component which watches the OSGi Service Registry for known {@link DOMService}s and publishes corresponding
 * {@link BindingService}s backed by them.
 *
 * @author Robert Varga
 */
public final class DynamicBindingAdapter implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(DynamicBindingAdapter.class);

    @GuardedBy("this")
    private List<AdaptingTracker<?, ?>> trackers;

    public DynamicBindingAdapter(final AdapterFactory factory, final BundleContext ctx) {
        trackers = ImmutableList.of(
            new AdaptingTracker<>(ctx, DOMDataBroker.class, DataBroker.class, factory::createDataBroker,
                    DEFAULT_SERVICE_TYPE),
            new AdaptingTracker<>(ctx, DOMDataBroker.class, DataBroker.class, factory::createDataBroker,
                    "pingpong"),
            new AdaptingTracker<>(ctx, DOMDataTreeService.class, DataTreeService.class, factory::createDataTreeService,
                    DEFAULT_SERVICE_TYPE),
            new AdaptingTracker<>(ctx, DOMMountPointService.class, MountPointService.class,
                    factory::createMountPointService, DEFAULT_SERVICE_TYPE),
            new AdaptingTracker<>(ctx, DOMNotificationService.class, NotificationService.class,
                    factory::createNotificationService, DEFAULT_SERVICE_TYPE),
            new AdaptingTracker<>(ctx, DOMNotificationPublishService.class, NotificationPublishService.class,
                    factory::createNotificationPublishService, DEFAULT_SERVICE_TYPE),
            new AdaptingTracker<>(ctx, DOMRpcService.class, RpcConsumerRegistry.class,
                    factory::createRpcConsumerRegistry, DEFAULT_SERVICE_TYPE),
            new AdaptingTracker<>(ctx, DOMRpcProviderService.class, RpcProviderService.class,
                    factory::createRpcProviderService, DEFAULT_SERVICE_TYPE));

        LOG.debug("Starting {} DOMService trackers", trackers.size());
        trackers.forEach(ServiceTracker::open);
        LOG.info("{} DOMService trackers started", trackers.size());
    }

    @Override
    public void close() {
        final List<AdaptingTracker<?, ?>> toClose;
        synchronized (this) {
            toClose = trackers;
            trackers = ImmutableList.of();
        }

        LOG.debug("Stopping {} DOMService trackers", toClose.size());
        if (!toClose.isEmpty()) {
            toClose.forEach(AdaptingTracker::close);
            LOG.info("{} DOMService trackers stopped", toClose.size());
        }
    }
}
