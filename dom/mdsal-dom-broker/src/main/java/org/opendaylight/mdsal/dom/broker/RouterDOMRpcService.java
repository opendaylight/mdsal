/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.ListenableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.dom.api.DOMRpcAvailabilityListener;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Singleton
@Component
@NonNullByDefault
public final class RouterDOMRpcService implements DOMRpcService {
    private final DOMRpcRouter router;

    @Inject
    @Activate
    public RouterDOMRpcService(@Reference final DOMRpcRouter router) {
        this.router = requireNonNull(router);
    }

    @Override
    public ListenableFuture<? extends DOMRpcResult> invokeRpc(final QName type, final ContainerNode input) {
        return router.invokeRpc(type, input);
    }

    @Override
    public Registration registerRpcListener(final DOMRpcAvailabilityListener listener) {
        return router.registerRpcListener(listener);
    }
}
