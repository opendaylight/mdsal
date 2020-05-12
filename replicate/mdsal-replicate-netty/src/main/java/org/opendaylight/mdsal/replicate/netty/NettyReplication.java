/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.yangtools.concepts.AbstractRegistration;
import org.opendaylight.yangtools.concepts.Registration;

public final class NettyReplication {
    private static final class Disabled extends AbstractRegistration {
        @Override
        protected void removeRegistration() {
            // no-op
        }
    }

    private NettyReplication() {
        // Hidden on purpose
    }

    public static Registration createSink(final DOMDataBroker dataBroker,
            final ClusterSingletonServiceProvider singletonService, final boolean enabled,
            final InetAddress sourceAddress, final int sourcePort, final Duration reconnectDelay) {
        return enabled ? singletonService.registerClusterSingletonService(new SinkSingletonService(dataBroker,
            new InetSocketAddress(sourceAddress, sourcePort), reconnectDelay)) : new Disabled();
    }

    public static Registration createSource(final DOMDataBroker dataBroker,
            final ClusterSingletonServiceProvider singletonService, final boolean enabled, final int listenPort) {
        return enabled
                ? singletonService.registerClusterSingletonService(new SourceSingletonService(dataBroker, listenPort))
                        : new Disabled();
    }
}
