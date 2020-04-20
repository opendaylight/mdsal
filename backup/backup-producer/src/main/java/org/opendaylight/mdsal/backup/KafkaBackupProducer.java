/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.backup;

import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;

/**
 * Bean producing the {@link KafkaBackupProducerService} and registering it as a ClusterSingleton.
 */

public class KafkaBackupProducer {

    private DOMDataBroker domDataBroker;
    private ClusterSingletonServiceProvider clusterSingletonServiceProvider;
    private ClusterSingletonService backupProducerService;
    private ClusterSingletonServiceRegistration serviceRegistration;

    public KafkaBackupProducer(final DOMDataBroker dataBroker,
            final ClusterSingletonServiceProvider singletonServiceProvider) {
        this.domDataBroker = dataBroker;
        this.clusterSingletonServiceProvider = singletonServiceProvider;
        this.backupProducerService = new KafkaBackupProducerService(domDataBroker);
    }

    public void setDomDataBroker(final DOMDataBroker dataBroker) {
        this.domDataBroker = dataBroker;
    }

    public void setClusterSingletonServiceProvider(final ClusterSingletonServiceProvider singletonServiceProvider) {
        this.clusterSingletonServiceProvider = singletonServiceProvider;
    }

    public void init() {
        serviceRegistration = clusterSingletonServiceProvider.registerClusterSingletonService(backupProducerService);
    }

    public void close() {
        if (serviceRegistration != null) {
            serviceRegistration.close();
        } else {
            backupProducerService.closeServiceInstance();
        }
    }
}
