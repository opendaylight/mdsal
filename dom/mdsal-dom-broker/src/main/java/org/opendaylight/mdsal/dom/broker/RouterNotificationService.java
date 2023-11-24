/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.mdsal.dom.api.DOMNotificationListener;
import org.opendaylight.mdsal.dom.api.DOMNotificationService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * {@link DOMNotificationService} based on {@link DOMNotificationRouter}.
 */
@Singleton
@Component
public final class RouterNotificationService implements DOMNotificationService {
    private final DOMNotificationRouter router;

    @Inject
    @Activate
    public RouterNotificationService(@Reference final DOMNotificationRouter router) {
        this.router = requireNonNull(router);
    }

    @Override
    public <T extends DOMNotificationListener> ListenerRegistration<T> registerNotificationListener(final T listener,
            final Collection<Absolute> types) {
        return router.registerNotificationListener(listener, types);
    }

    @Override
    public Registration registerNotificationListeners(final Map<Absolute, DOMNotificationListener> typeToListener) {
        return router.registerNotificationListeners(typeToListener);
    }
}
