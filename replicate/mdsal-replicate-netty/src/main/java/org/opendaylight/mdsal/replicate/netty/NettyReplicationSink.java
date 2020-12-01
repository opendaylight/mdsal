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

@Component(configurationPid = "org.opendaylight.mdsal.replicate.netty.sink")
public final class NettyReplicationSink {
    private static final Logger LOG = LoggerFactory.getLogger(NettyReplicationSink.class);

    @Reference
    private static BootstrapSupport bootstrapSupport;

    @Reference
    private static DOMDataBroker dataBroker;

    @Reference
    private static ClusterSingletonServiceProvider singletonService;

    private static final class Disabled extends AbstractRegistration {
        @Override
        protected void removeRegistration() {
            // no-op
        }
    }

    private NettyReplicationSink() {

    }

    @Activate
    void activate(Map<String, Object> properties) throws UnknownHostException {
        boolean enabled = (boolean) properties.getOrDefault("enabled", false);
        String sourceHost = (String) properties.getOrDefault("source-host", "127.0.0.1");
        InetAddress sourceAddress = InetAddress.getByName(sourceHost);
        int sourcePort = (int) properties.getOrDefault("source-port", 9999);
        int reconnectDelayMillis = (int) properties.getOrDefault("reconnect-delay-millis", 3000);
        Duration reconnectDelay = Duration.ofMillis(reconnectDelayMillis);
        int keepaliveIntervalSeconds = (int) properties.getOrDefault("keepalive-interval-seconds", 10);
        Duration keepaliveInterval = Duration.ofSeconds(keepaliveIntervalSeconds);
        int maxMissedKeepalives = (int) properties.getOrDefault("max-missed-keepalives", 5);

        createSink(bootstrapSupport, dataBroker, singletonService, enabled, sourceAddress, sourcePort,
                reconnectDelay, keepaliveInterval, maxMissedKeepalives);
    }

    public static Registration createSink(final BootstrapSupport bootstrap, final DOMDataBroker broker,
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
