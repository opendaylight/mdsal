/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Singleton
@Component
@NonNullByDefault
// FIXME: rename to RouterDOMNotificationPublishService
public final class RouterDOMPublishNotificationService implements DOMNotificationPublishService {
    private final List<Extension> supportedExtensions;
    private final DOMNotificationRouter router;

    @Inject
    @Activate
    public RouterDOMPublishNotificationService(@Reference final DOMNotificationRouter router) {
        this.router = requireNonNull(router);
        supportedExtensions = List.of(new RouterDOMNotificationPublishDemandExtension(router));
    }

    @Override
    public List<Extension> supportedExtensions() {
        return supportedExtensions;
    }

    @Override
    public ListenableFuture<?> putNotification(final DOMNotification notification) throws InterruptedException {
        return router.putNotificationImpl(notification);
    }

    @Override
    public ListenableFuture<?> offerNotification(final DOMNotification notification) {
        return router.offerNotification(notification);
    }

    @Override
    public ListenableFuture<?> offerNotification(final DOMNotification notification, final long timeout,
            final TimeUnit unit) throws InterruptedException {
        return router.offerNotification(notification, timeout, unit);
    }
}
