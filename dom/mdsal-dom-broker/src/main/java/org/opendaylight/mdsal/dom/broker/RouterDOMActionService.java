/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMActionService;
import org.opendaylight.mdsal.dom.spi.ForwardingDOMActionService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Singleton
@Component(service = DOMActionService.class)
public final class RouterDOMActionService extends ForwardingDOMActionService {
    private final @NonNull DOMActionService delegate;

    @Inject
    @Activate
    public RouterDOMActionService(@Reference final DOMRpcRouter router) {
        delegate = router.actionService();
    }

    @Override
    protected DOMActionService delegate() {
        return delegate;
    }
}
