/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.singleton.api;

import org.opendaylight.yangtools.concepts.Registration;

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
public interface ClusterSingletonServiceProvider {
    /**
     * Method registers {@link ClusterSingletonService} to Provider.
     * Method throws {@link RuntimeException} for unexpected state, so be careful with implementation.
     * Note: RuntimeException is implemented as a notification about some problems with registration and client
     * has to implement some strategy for handling this issue.
     * TODO: RuntimeException is not a transparent contract for handling unexpected state and it needs to be
     * replaced with a specific documented Exception or it needs to add another contract definition for a client
     * notification about the unexpected state reason in {@link ClusterSingletonService}.
     * RuntimeException implementation is a hotfix for an unwanted API contract changes in boron release only.
     *
     * @param service ClusterSingletonService instance
     * @return {@link Registration} registration
     */
    Registration registerClusterSingletonService(ClusterSingletonService service);
}
