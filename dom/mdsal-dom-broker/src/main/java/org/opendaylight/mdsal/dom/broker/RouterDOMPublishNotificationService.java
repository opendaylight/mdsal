/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishService;
import org.opendaylight.mdsal.dom.spi.ForwardingDOMNotificationPublishService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Singleton
@Component(service = DOMNotificationPublishService.class)
public final class RouterDOMPublishNotificationService extends ForwardingDOMNotificationPublishService {
    private final @NonNull DOMNotificationPublishService delegate;

    @Inject
    @Activate
    public RouterDOMPublishNotificationService(@Reference final DOMNotificationRouter router) {
        delegate = router.notificationPublishService();
    }

    @Override
    protected DOMNotificationPublishService delegate() {
        return delegate;
    }
}
