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
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.yangtools.concepts.AbstractRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, configurationPid = "org.opendaylight.mdsal.replicate.netty.sink")
@Designate(ocd = NettyReplicationSink.Config.class)
public final class NettyReplicationSink {
    private static final Logger LOG = LoggerFactory.getLogger(NettyReplicationSink.class);

    @ObjectClassDefinition
    public @interface Config {
        @AttributeDefinition(name = "enabled")
        boolean enabled() default false;

        @AttributeDefinition(name = "source-host")
        String sourceHost() default "127.0.0.1";

        @AttributeDefinition(name = "source-port")
        int sourcePort() default 9999;

        @AttributeDefinition(name = "reconnect-delay-millis")
        int reconnectDelayMillis() default 3000;

        @AttributeDefinition(name = "keepalive-interval-seconds")
        int keepAliveIntervalSeconds() default 10;

        @AttributeDefinition(name = "max-missed-keepalives")
        int maxMissedKeepalives() default 5;
    }

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

    public NettyReplicationSink() {
        // Visible for DI
    }

    @Activate
    void activate(final Config config) throws UnknownHostException {
        final InetAddress sourceAddress = InetAddress.getByName(config.sourceHost());
        final Duration reconnectDelay = Duration.ofMillis(config.reconnectDelayMillis());
        final Duration keepaliveInterval = Duration.ofSeconds(config.keepAliveIntervalSeconds());

        createSink(bootstrapSupport, dataBroker, singletonService, config.enabled(), sourceAddress,
                config.sourcePort(), reconnectDelay, keepaliveInterval, config.maxMissedKeepalives());
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
