/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker.dagger;

import dagger.Module;
import dagger.Provides;
import jakarta.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.dom.api.DOMActionProviderService;
import org.opendaylight.mdsal.dom.api.DOMActionService;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.broker.DOMRpcRouter;
import org.opendaylight.mdsal.dom.broker.RouterDOMActionProviderService;
import org.opendaylight.mdsal.dom.broker.RouterDOMActionService;
import org.opendaylight.mdsal.dom.broker.RouterDOMRpcProviderService;
import org.opendaylight.mdsal.dom.broker.RouterDOMRpcService;
import org.opendaylight.odlparent.dagger.ResourceSupport;

/**
 * A Dagger module providing access to operation invocation services, namely {@link DOMActionService},
 * {@link DOMActionProviderService}, {@link DOMRpcService} and {@link DOMRpcProviderService}.
 */
@Module
@NonNullByDefault
@SuppressWarnings("exports")
public interface DOMRpcRouterModule {
    @Provides
    @Singleton
    static DOMRpcRouter provideDOMRpcRouter(final ResourceSupport resourceSupport,
            final DOMSchemaService schemaService) {
        return resourceSupport.register(new DOMRpcRouter(schemaService));
    }

    @Provides
    @Singleton
    static DOMActionService provideDOMActionService(final DOMRpcRouter rpcRouter) {
        return new RouterDOMActionService(rpcRouter);
    }

    @Provides
    @Singleton
    static DOMActionProviderService provideDOMActionProviderService(final DOMRpcRouter rpcRouter) {
        return new RouterDOMActionProviderService(rpcRouter);
    }

    @Provides
    @Singleton
    static DOMRpcService provideDOMRpcService(final DOMRpcRouter rpcRouter) {
        return new RouterDOMRpcService(rpcRouter);
    }

    @Provides
    @Singleton
    static DOMRpcProviderService provideDOMRpcProviderService(final DOMRpcRouter rpcRouter) {
        return new RouterDOMRpcProviderService(rpcRouter);
    }
}
