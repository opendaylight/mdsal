/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.mdsal.dom.api.DOMNotificationListener;
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishService;
import org.opendaylight.mdsal.dom.api.DOMNotificationService;
import org.opendaylight.mdsal.dom.spi.DOMNotificationSubscriptionListener;
import org.opendaylight.mdsal.dom.spi.DOMNotificationSubscriptionListenerRegistry;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, configurationPid = "org.opendaylight.mdsal.dom.notification", service = {
        DOMNotificationService.class, DOMNotificationPublishService.class,
        DOMNotificationSubscriptionListenerRegistry.class
})
@Designate(ocd = OSGiDOMNotificationRouter.Config.class)
public final class OSGiDOMNotificationRouter implements DOMNotificationService, DOMNotificationPublishService,
        DOMNotificationSubscriptionListenerRegistry {
    @ObjectClassDefinition()
    public @interface Config {
        @AttributeDefinition(name = "notification-queue-depth")
        int queueDepth() default 65536;
    }

    private static final Logger LOG = LoggerFactory.getLogger(OSGiDOMNotificationRouter.class);

    private DOMNotificationRouter router;

    @Activate
    void activate(final Config config) {
        router = DOMNotificationRouter.create(config.queueDepth());
        LOG.info("DOM Notification Router started");
    }

    @Deactivate
    void deactivate() {
        router.close();
        router = null;
        LOG.info("DOM Notification Router stopped");
    }

    @Override
    public <L extends DOMNotificationSubscriptionListener> ListenerRegistration<L> registerSubscriptionListener(
            final L listener) {
        return router.registerSubscriptionListener(listener);
    }

    @Override
    public ListenableFuture<? extends Object> putNotification(final DOMNotification notification)
            throws InterruptedException {
        return router.putNotification(notification);
    }

    @Override
    public ListenableFuture<? extends Object> offerNotification(final DOMNotification notification) {
        return router.offerNotification(notification);
    }

    @Override
    public ListenableFuture<? extends Object> offerNotification(final DOMNotification notification,
            final long timeout, final TimeUnit unit) throws InterruptedException {
        return router.offerNotification(notification, timeout, unit);
    }

    @Override
    public <T extends DOMNotificationListener> ListenerRegistration<T> registerNotificationListener(final T listener,
            final Collection<Absolute> types) {
        return router.registerNotificationListener(listener, types);
    }
}
