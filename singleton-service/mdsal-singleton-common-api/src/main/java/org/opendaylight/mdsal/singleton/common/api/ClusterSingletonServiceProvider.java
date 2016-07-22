/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.singleton.common.api;

/**
 * {@link ClusterSingletonServiceProvider} provides a functionality to register and group services
 * {@link ClusterSingletonService} by service group identifier. Services could be Applications or RPCs.
 * Provider provides a functionality which allows to have only one fully instantiated service instance
 * in a cluster at one time and service group means to have all service instances for the same group
 * situated on same Cluster Node. This is realized via a double candidate approach where a service
 * group instance maintains a candidate registration for ownership of the service group entity in the
 * cluster and also a registration that acts as a guard to ensure a service group instance has fully
 * closed prior to relinquishing service group ownership. To achieve ownership of the service group,
 * a service group candidate must hold ownership of both these entities.
 */
public interface ClusterSingletonServiceProvider extends AutoCloseable {

    /**
     * Method registers {@link ClusterSingletonService} to Provider.
     *
     * @param service ClusterSingletonService instance
     * @return {@link AutoCloseable} registration
     */
    ClusterSingletonServiceRegistration registerClusterSingletonService(ClusterSingletonService service);

}
