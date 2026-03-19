/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.singleton.api.ClusterSingletonServiceProvider;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
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

        @AttributeDefinition(name = "replicate-configuration")
        boolean replicateConfiguration() default true;

        @AttributeDefinition(name = "replicate-operational")
        boolean replicateOperational() default false;
    }

    private Registration reg;

    @Activate
    public NettyReplicationSink(@Reference final BootstrapSupport bootstrapSupport,
            @Reference final DOMDataBroker dataBroker,
            @Reference final ClusterSingletonServiceProvider singletonService,
                                final Config config)
                throws UnknownHostException {

        reg = new NoOpRegistration();

        if (config.enabled()) {
            //do not process the rest of the config unless replication is enabled.
            List<DOMDataTreeIdentifier> trees = addTrees(config);

            if (!trees.isEmpty()) {
                //config is validating upon creation
                NettyReplicationConfig nettyReplicationConfig = new NettyReplicationConfig(
                        bootstrapSupport,
                        dataBroker,
                        new InetSocketAddress(config.sourceHost(), config.sourcePort()),
                        Duration.ofMillis(config.reconnectDelayMillis()),
                        Duration.ofSeconds(config.keepAliveIntervalSeconds()),
                        config.maxMissedKeepalives(),
                        trees);

                reg = createSinkReg(singletonService, nettyReplicationConfig);
            }
        }
    }

    private static List<DOMDataTreeIdentifier> addTrees(Config config) {
        List<DOMDataTreeIdentifier> trees = new ArrayList<>();
        if (config.replicateConfiguration()) {
            final DOMDataTreeIdentifier confTree = DOMDataTreeIdentifier.of(LogicalDatastoreType.CONFIGURATION,
                    YangInstanceIdentifier.of());
            LOG.info("Replicating CONFIGURATION store");
            trees.add(confTree);
        }
        if (config.replicateOperational()) {
            final DOMDataTreeIdentifier opTree = DOMDataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL,
                    YangInstanceIdentifier.of());
            LOG.info("Replicating OPERATIONAL store");
            trees.add(opTree);
        }
        return trees;
    }

    static Registration createSinkReg(final ClusterSingletonServiceProvider singletonService,
                                      final NettyReplicationConfig config) {
        return singletonService.registerClusterSingletonService(new SinkSingletonService(config));
    }

    @Deactivate
    void deactivate() {
        reg.close();
        reg = null;
    }
}
