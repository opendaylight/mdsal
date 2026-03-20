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
        @AttributeDefinition
        boolean enabled() default false;

        @AttributeDefinition
        String source$_$host() default "127.0.0.1";

        @AttributeDefinition
        int source$_$port() default 9999;

        @AttributeDefinition
        int reconnect$_$delay$_$millis() default 3000;

        @AttributeDefinition
        int keepalive$_$interval$_$seconds() default 10;

        @AttributeDefinition
        int max$_$missed$_$keepalives() default 5;
    }

    private Registration reg;

    @Activate
    public NettyReplicationSink(@Reference final BootstrapSupport bootstrapSupport,
            @Reference final DOMDataBroker dataBroker,
            @Reference final ClusterSingletonServiceProvider singletonService, final Config config)
                throws UnknownHostException {
        reg = createSink(bootstrapSupport, dataBroker, singletonService, config.enabled(),
            InetAddress.getByName(config.source$_$host()),
            config.source$_$port(), Duration.ofMillis(config.reconnect$_$delay$_$millis()),
            Duration.ofSeconds(config.keepalive$_$interval$_$seconds()), config.max$_$missed$_$keepalives());
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
