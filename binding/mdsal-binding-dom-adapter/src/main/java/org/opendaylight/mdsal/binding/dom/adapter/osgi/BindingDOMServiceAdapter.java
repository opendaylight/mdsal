/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.osgi;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.mdsal.binding.api.BindingService;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeService;
import org.opendaylight.mdsal.binding.api.MountPointService;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.api.RpcConsumerRegistry;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMDataBrokerAdapter;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMDataTreeServiceAdapter;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMMountPointServiceAdapter;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMNotificationPublishServiceAdapter;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMNotificationServiceAdapter;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMRpcProviderServiceAdapter;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMRpcServiceAdapter;
import org.opendaylight.mdsal.binding.dom.adapter.BindingToNormalizedNodeCodec;
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

/**
 * A component which watches the OSGi Service Registry for known {@link DOMService}s and publishes corresponding
 * {@link BindingService}s backed by them.
 *
 * @author Robert Varga
 */
public final class BindingDOMServiceAdapter implements AutoCloseable {
    @GuardedBy("this")
    private Collection<ServiceTracker<?, ?>> trackers;

    public BindingDOMServiceAdapter(final BindingToNormalizedNodeCodec codec, final BundleContext ctx) {
        trackers = ImmutableList.of(
            new AdaptingTracker<>(ctx, DOMDataBroker.class, DataBroker.class, codec,
                    BindingDOMDataBrokerAdapter::new),
            new AdaptingTracker<>(ctx, DOMDataTreeService.class, DataTreeService.class, codec,
                    BindingDOMDataTreeServiceAdapter::create),
            new AdaptingTracker<>(ctx, DOMMountPointService.class, MountPointService.class, codec,
                    BindingDOMMountPointServiceAdapter::new),
            new AdaptingTracker<>(ctx, DOMNotificationService.class, NotificationService.class, codec,
                    BindingDOMNotificationServiceAdapter::new),
            new AdaptingTracker<>(ctx, DOMNotificationPublishService.class, NotificationPublishService.class, codec,
                    BindingDOMNotificationPublishServiceAdapter::new),
            new AdaptingTracker<>(ctx, DOMRpcService.class, RpcConsumerRegistry.class, codec,
                    BindingDOMRpcServiceAdapter::new),
            new AdaptingTracker<>(ctx, DOMRpcProviderService.class, RpcProviderService.class, codec,
                    BindingDOMRpcProviderServiceAdapter::new));

        trackers.forEach(ServiceTracker::open);
    }

    @Override
    public void close() {
        final Collection<ServiceTracker<?, ?>> toClose;
        synchronized (this) {
            toClose = trackers;
            trackers = ImmutableList.of();
        }

        toClose.forEach(ServiceTracker::close);
    }
}
