/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.yangtools.concepts.Registration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Singleton
@Component
@NonNullByDefault
public final class RouterDOMRpcProviderService implements DOMRpcProviderService {
    private final DOMRpcRouter router;

    @Inject
    @Activate
    public RouterDOMRpcProviderService(@Reference final DOMRpcRouter router) {
        this.router = requireNonNull(router);
    }

    @Override
    public Registration registerRpcImplementation(final DOMRpcImplementation implementation,
            final Set<DOMRpcIdentifier> rpcs) {
        return router.registerRpcImplementation(implementation, rpcs);
    }

    @Override
    public Registration registerRpcImplementations(final Map<DOMRpcIdentifier, DOMRpcImplementation> map) {
        return router.registerRpcImplementations(map);
    }
}
