/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import static com.google.common.base.Verify.verify;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.yangtools.concepts.AbstractRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NettyReplication {
    private static final Logger LOG = LoggerFactory.getLogger(NettyReplication.class);

    private static final class Disabled extends AbstractRegistration {
        @Override
        protected void removeRegistration() {
            // no-op
        }
    }

    private NettyReplication() {
        // Hidden on purpose
    }

    public static Registration createSink(final BootstrapSupport bootstrapSupport, final DOMDataBroker dataBroker,
            final ClusterSingletonServiceProvider singletonService, final boolean enabled,
            final InetAddress sourceAddress, final int sourcePort, final Duration reconnectDelay,
        final Duration keepaliveInterval, final int maxMissedKeepalives) {
        LOG.debug("Sink {}", enabled ? "enabled" : "disabled");
        return enabled ? singletonService.registerClusterSingletonService(new SinkSingletonService(bootstrapSupport,
            dataBroker, new InetSocketAddress(sourceAddress, sourcePort), reconnectDelay, keepaliveInterval,
            maxMissedKeepalives)) : new Disabled();
    }

    public static Registration createSource(final BootstrapSupport bootstrapSupport, final DOMDataBroker dataBroker,
            final ClusterSingletonServiceProvider singletonService, final boolean enabled, final int listenPort,
        final Duration keepaliveInterval) {
        LOG.debug("Source {}", enabled ? "enabled" : "disabled");
        final DOMDataTreeChangeService dtcs = dataBroker.getExtensions().getInstance(DOMDataTreeChangeService.class);
        verify(dtcs != null, "Missing DOMDataTreeChangeService in broker %s", dataBroker);

        return enabled ? singletonService.registerClusterSingletonService(new SourceSingletonService(bootstrapSupport,
            dtcs, listenPort, keepaliveInterval)) : new Disabled();
    }
}
