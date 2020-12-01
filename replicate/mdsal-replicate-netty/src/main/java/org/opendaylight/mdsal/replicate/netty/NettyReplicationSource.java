/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;

import java.time.Duration;
import java.util.Map;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.yangtools.concepts.AbstractRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
//import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(configurationPid = "org.opendaylight.mdsal.replicate.netty.source")
public final class NettyReplicationSource {
    private static final Logger LOG = LoggerFactory.getLogger(NettyReplicationSource.class);

    //@Reference
    private static BootstrapSupport bootstrapSupport;

    //@Reference
    private static DOMDataBroker dataBroker;

    //@Reference
    private static ClusterSingletonServiceProvider singletonService;

    private static final class Disabled extends AbstractRegistration {
        @Override
        protected void removeRegistration() {
            // no-op
        }
    }

    private NettyReplicationSource() {

    }

    @Activate
    public void activate(Map<String, Object> properties) {
        boolean enabled = (boolean) properties.getOrDefault("enabled", false);
        int listenPort = (int) properties.getOrDefault("listen-port", 9999);
        int keepaliveIntervalSeconds = (int) properties.getOrDefault("keepalive-interval-seconds", 10);
        Duration keepaliveInterval = Duration.ofSeconds(keepaliveIntervalSeconds);
        int maxMissedKeepalives = (int) properties.getOrDefault("max-missed-keepalives", 5);

        createSource(bootstrapSupport, dataBroker, singletonService, enabled, listenPort,
                keepaliveInterval, maxMissedKeepalives);
    }

    public static Registration createSource(final BootstrapSupport bootstrap, final DOMDataBroker broker,
                                    final ClusterSingletonServiceProvider singleton, final boolean enabled,
                                    final int listenPort, final Duration keepaliveInterval,
                                    final int maxMissedKeepalives) {
        LOG.debug("Source {}", enabled ? "enabled" : "disabled");
        final DOMDataTreeChangeService dtcs = broker.getExtensions().getInstance(DOMDataTreeChangeService.class);
        verify(dtcs != null, "Missing DOMDataTreeChangeService in broker %s", broker);
        checkArgument(maxMissedKeepalives > 0, "max-missed-keepalives %s must be greater than 0", maxMissedKeepalives);
        return enabled ? singleton.registerClusterSingletonService(new SourceSingletonService(bootstrap,
                dtcs, listenPort, keepaliveInterval, maxMissedKeepalives)) : new Disabled();
    }
}
