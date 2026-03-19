/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.netty;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.singleton.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.api.ServiceGroupIdentifier;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SinkSingletonService implements ClusterSingletonService {
    private static final Logger LOG =
            LoggerFactory.getLogger(SinkSingletonService.class);
    private static final ServiceGroupIdentifier SGID =
            new ServiceGroupIdentifier(SinkSingletonService.class.getName());

    private final List<TreeReplica> replicas;

    SinkSingletonService(NettyReplicationConfig config) {
        this.replicas = createReplicas(config);

        LOG.info(
                "Replication sink from {} waiting for cluster-wide mastership for trees {}",
                config.sourceAddress(),
                config.trees());
    }

    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return SGID;
    }

    @Override
    public synchronized void instantiateServiceInstance() {
        for (TreeReplica replica : replicas) {
            replica.start();
        }
    }

    @Override
    public synchronized ListenableFuture<?> closeServiceInstance() {
        boolean success = true;
        for (TreeReplica replica : replicas) {
            success &= replica.close();
        }

        return FluentFutures.immediateBooleanFluentFuture(success);
    }

    private List<TreeReplica> createReplicas(
            NettyReplicationConfig config) {
        final List<TreeReplica> ret = new ArrayList<>(config.trees().size());
        for (DOMDataTreeIdentifier tree : config.trees()) {
            ret.add(new TreeReplica(config, tree));
        }
        return List.copyOf(ret);
    }
}