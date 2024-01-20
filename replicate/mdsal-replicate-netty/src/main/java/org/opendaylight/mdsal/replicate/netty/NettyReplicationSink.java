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
import org.opendaylight.mdsal.singleton.api.ClusterSingletonServiceProvider;
import org.opendaylight.yangtools.concepts.Registration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = { }, configurationPid = "org.opendaylight.mdsal.replicate.netty.sink")
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

    private Registration reg;

    @Activate
    public NettyReplicationSink(@Reference final BootstrapSupport bootstrapSupport,
            @Reference final DOMDataBroker dataBroker,
            @Reference final ClusterSingletonServiceProvider singletonService, final Config config)
                throws UnknownHostException {
        reg = createSink(bootstrapSupport, dataBroker, singletonService, config.enabled(),
            InetAddress.getByName(config.sourceHost()),
            config.sourcePort(), Duration.ofMillis(config.reconnectDelayMillis()),
            Duration.ofSeconds(config.keepAliveIntervalSeconds()), config.maxMissedKeepalives());
    }

    @Deactivate
    void deactivate() {
        reg.close();
        reg = null;
    }

    static Registration createSink(final BootstrapSupport bootstrap, final DOMDataBroker broker,
            final ClusterSingletonServiceProvider singleton, final boolean enabled,
            final InetAddress sourceAddress, final int sourcePort, final Duration reconnectDelay,
            final Duration keepaliveInterval, final int maxMissedKeepalives) {
        LOG.debug("Sink {}", enabled ? "enabled" : "disabled");
        checkArgument(maxMissedKeepalives > 0, "max-missed-keepalives %s must be greater than 0", maxMissedKeepalives);
        return enabled ? singleton.registerClusterSingletonService(new SinkSingletonService(bootstrap,
                broker, new InetSocketAddress(sourceAddress, sourcePort), reconnectDelay, keepaliveInterval,
                maxMissedKeepalives)) : new NoOpRegistration();
    }
}
