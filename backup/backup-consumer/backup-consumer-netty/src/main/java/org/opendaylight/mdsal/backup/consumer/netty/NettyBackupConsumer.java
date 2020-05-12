/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.backup.consumer.netty;

import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean producing the {@link NettyBackupConsumerService} and registering it as a ClusterSingleton.
 */
public class NettyBackupConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(NettyBackupConsumer.class);

    private DOMDataBroker domDataBroker;
    private ClusterSingletonServiceProvider clusterSingletonServiceProvider;
    private ClusterSingletonService backupConsumerService;
    private ClusterSingletonServiceRegistration serviceRegistration;

    public NettyBackupConsumer(final DOMDataBroker domDataBroker,
            final ClusterSingletonServiceProvider clusterSingletonServiceProvider) {
        this.domDataBroker = domDataBroker;
        this.clusterSingletonServiceProvider = clusterSingletonServiceProvider;
    }

    public void setDomDataBroker(final DOMDataBroker dataBroker) {
        this.domDataBroker = dataBroker;
    }

    public void setClusterSingletonServiceProvider(final ClusterSingletonServiceProvider singletonServiceProvider) {
        this.clusterSingletonServiceProvider = singletonServiceProvider;
    }

    public void init() {
        backupConsumerService = new NettyBackupConsumerService(domDataBroker);
        serviceRegistration = clusterSingletonServiceProvider.registerClusterSingletonService(backupConsumerService);
    }

    public void close() {
        if (serviceRegistration != null) {
            serviceRegistration.close();
        } else {
            backupConsumerService.closeServiceInstance();
        }
    }
}
