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

        @AttributeDefinition
        boolean replicate$_$configuration() default true;

        @AttributeDefinition
        boolean replicate$_$operational() default false;
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
                        new InetSocketAddress(config.source$_$host(), config.source$_$port()),
                        Duration.ofMillis(config.reconnect$_$delay$_$millis()),
                        Duration.ofSeconds(config.keepalive$_$interval$_$seconds()),
                        config.max$_$missed$_$keepalives(),
                        trees);

                reg = createSinkReg(singletonService, nettyReplicationConfig);
            }
        }
    }

    private static List<DOMDataTreeIdentifier> addTrees(Config config) {
        List<DOMDataTreeIdentifier> trees = new ArrayList<>();
        if (config.replicate$_$configuration()) {
            final DOMDataTreeIdentifier confTree = DOMDataTreeIdentifier.of(LogicalDatastoreType.CONFIGURATION,
                    YangInstanceIdentifier.of());
            LOG.info("Replicating CONFIGURATION store");
            trees.add(confTree);
        }
        if (config.replicate$_$operational()) {
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
