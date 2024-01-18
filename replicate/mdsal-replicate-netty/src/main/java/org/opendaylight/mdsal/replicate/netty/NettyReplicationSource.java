/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.VisibleForTesting;
import java.time.Duration;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataBroker.DataTreeChangeExtension;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
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

@Component(service = { }, configurationPid = "org.opendaylight.mdsal.replicate.netty.source")
@Designate(ocd = NettyReplicationSource.Config.class)
public final class NettyReplicationSource {
    private static final Logger LOG = LoggerFactory.getLogger(NettyReplicationSource.class);

    @ObjectClassDefinition
    public @interface Config {
        @AttributeDefinition(name = "enabled")
        boolean enabled() default false;

        @AttributeDefinition(name = "listen-port")
        int listenPort() default 9999;

        @AttributeDefinition(name = "keepalive-interval-seconds")
        int keepAliveIntervalSeconds() default 10;

        @AttributeDefinition(name = "max-missed-keepalives")
        int maxMissedKeepalives() default 5;
    }

    private Registration reg;

    @Activate
    public NettyReplicationSource(@Reference final BootstrapSupport bootstrapSupport,
            @Reference final DOMDataBroker dataBroker,
            @Reference final ClusterSingletonServiceProvider singletonService, final Config config) {
        reg = createSource(bootstrapSupport, dataBroker, singletonService, config.enabled(), config.listenPort(),
            Duration.ofSeconds(config.keepAliveIntervalSeconds()), config.maxMissedKeepalives());
    }

    @Deactivate
    void deactivate() {
        reg.close();
        reg = null;
    }

    @VisibleForTesting
    static Registration createSource(final BootstrapSupport bootstrap, final DOMDataBroker broker,
            final ClusterSingletonServiceProvider singleton, final boolean enabled, final int listenPort,
            final Duration keepaliveInterval, final int maxMissedKeepalives) {
        LOG.debug("Source {}", enabled ? "enabled" : "disabled");
        final var dtcs = verifyNotNull(broker.extension(DataTreeChangeExtension.class),
            "Missing DOMDataTreeChangeService in broker %s", broker);
        checkArgument(maxMissedKeepalives > 0, "max-missed-keepalives %s must be greater than 0", maxMissedKeepalives);
        return enabled ? singleton.registerClusterSingletonService(new SourceSingletonService(bootstrap,
                dtcs, listenPort, keepaliveInterval, maxMissedKeepalives)) : new NoOpRegistration();
    }
}
