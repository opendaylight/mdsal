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
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.mdsal.dom.spi.ForwardingDOMRpcProviderService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Singleton
@Component(service = DOMRpcProviderService.class)
public final class RouterDOMRpcProviderService extends ForwardingDOMRpcProviderService {
    private final @NonNull DOMRpcProviderService delegate;

    @Inject
    @Activate
    public RouterDOMRpcProviderService(@Reference final DOMRpcRouter router) {
        delegate = router.rpcProviderService();
    }

    @Override
    protected DOMRpcProviderService delegate() {
        return delegate;
    }
}
