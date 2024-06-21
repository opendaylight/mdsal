/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.osgi;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.yangtools.binding.Notification;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component(factory = OSGiNotificationPublishService.FACTORY_NAME)
public final class OSGiNotificationPublishService extends AbstractAdaptedService<NotificationPublishService>
        implements NotificationPublishService {
    // OSGi DS Component Factory name
    static final String FACTORY_NAME = "org.opendaylight.mdsal.binding.dom.adapter.osgi.OSGiNotificationPublishService";

    @Activate
    public OSGiNotificationPublishService(final Map<String, ?> properties) {
        super(NotificationPublishService.class, properties);
    }

    @Deactivate
    void deactivate(final int reason) {
        stop(reason);
    }

    @Override
    public void putNotification(final Notification<?> notification) throws InterruptedException {
        delegate.putNotification(notification);
    }

    @Override
    public ListenableFuture<? extends Object> offerNotification(final Notification<?> notification) {
        return delegate.offerNotification(notification);
    }

    @Override
    public ListenableFuture<? extends Object> offerNotification(final Notification<?> notification, final int timeout,
            final TimeUnit unit) throws InterruptedException {
        return delegate.offerNotification(notification, timeout, unit);
    }
}
