/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishDemandExtension;
import org.opendaylight.yangtools.concepts.Registration;

@NonNullByDefault
final class RouterDOMNotificationPublishDemandExtension implements DOMNotificationPublishDemandExtension {
    private final DOMNotificationRouter router;

    RouterDOMNotificationPublishDemandExtension(final DOMNotificationRouter router) {
        this.router = requireNonNull(router);
    }

    @Override
    public Registration registerDemandListener(final DemandListener listener) {
        return router.registerDemandListener(listener);
    }
}
