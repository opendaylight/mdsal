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
import org.opendaylight.mdsal.dom.api.DOMActionAvailabilityExtension;
import org.opendaylight.yangtools.concepts.Registration;
import org.osgi.service.component.annotations.Reference;

@NonNullByDefault
final class RouterDOMActionAvailabilityExtension implements DOMActionAvailabilityExtension {
    private final DOMRpcRouter router;

    RouterDOMActionAvailabilityExtension(@Reference final DOMRpcRouter router) {
        this.router = requireNonNull(router);
    }

    @Override
    public Registration registerAvailabilityListener(final AvailabilityListener listener) {
        return router.registerAvailabilityListener(listener);
    }
}
