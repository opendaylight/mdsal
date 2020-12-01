/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import static com.google.common.base.Preconditions.checkArgument;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Map;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.yangtools.concepts.AbstractRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, configurationPid = "org.opendaylight.mdsal.replicate.netty.sink")
public final class NettyReplicationSink {
    private static final Logger LOG = LoggerFactory.getLogger(NettyReplicationSink.class);

    @Reference
    private BootstrapSupport bootstrapSupport;

    @Reference
    private DOMDataBroker dataBroker;

    @Reference
    private ClusterSingletonServiceProvider singletonService;

    private static final class Disabled extends AbstractRegistration {
        @Override
        protected void removeRegistration() {
            // no-op
        }
    }

    private NettyReplicationSink() {

    }

    @Activate
    void activate(final Map<String, Object> properties) throws UnknownHostException {
        final boolean enabled = (boolean) properties.getOrDefault("enabled", false);
        final String sourceHost = (String) properties.getOrDefault("source-host", "127.0.0.1");
        final InetAddress sourceAddress = InetAddress.getByName(sourceHost);
        final int sourcePort = (int) properties.getOrDefault("source-port", 9999);
        final int reconnectDelayMillis = (int) properties.getOrDefault("reconnect-delay-millis", 3000);
        final Duration reconnectDelay = Duration.ofMillis(reconnectDelayMillis);
        final int keepaliveIntervalSeconds = (int) properties.getOrDefault("keepalive-interval-seconds", 10);
        final Duration keepaliveInterval = Duration.ofSeconds(keepaliveIntervalSeconds);
        final int maxMissedKeepalives = (int) properties.getOrDefault("max-missed-keepalives", 5);

        createSink(bootstrapSupport, dataBroker, singletonService, enabled, sourceAddress, sourcePort,
                reconnectDelay, keepaliveInterval, maxMissedKeepalives);
    }

    static Registration createSink(final BootstrapSupport bootstrap, final DOMDataBroker broker,
                                  final ClusterSingletonServiceProvider singleton, final boolean enabled,
                                  final InetAddress sourceAddress, final int sourcePort, final Duration reconnectDelay,
                                  final Duration keepaliveInterval, final int maxMissedKeepalives) {
        LOG.debug("Sink {}", enabled ? "enabled" : "disabled");
        checkArgument(maxMissedKeepalives > 0, "max-missed-keepalives %s must be greater than 0", maxMissedKeepalives);
        return enabled ? singleton.registerClusterSingletonService(new SinkSingletonService(bootstrap,
                broker, new InetSocketAddress(sourceAddress, sourcePort), reconnectDelay, keepaliveInterval,
                maxMissedKeepalives)) : new Disabled();
    }
}
