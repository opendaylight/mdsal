/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.common.api.clustering;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import org.opendaylight.yangtools.concepts.Path;

/**
 * {@link ClusterSingletonServiceGroup} represent internal wrapper for whole group of
 * {@link ClusterSingletonService} services. All EntityOwnershipChange has to applied
 * for all registred services in same time and in same way.
 * So this interface represents a single cluster service group instance. All registred
 * services has only one fully instantiated service instance in a cluster at one time
 * on same Cluster Node. This is realized via a double candidate approach where a service
 * group instance maintains a candidate registration for ownership of the service entity
 * in the cluster and also a registration that acts as a guard to ensure a service group
 * instance has fully closed prior to relinquishing service ownership. To achieve
 * ownership of the service group, a service group candidate must hold ownership
 * of both these entities.
 *
 * @param <P> the instance identifier path type
 * @param <E> the GenericEntity type
 * @param <C> the GenericEntityOwnershipChange type
 */
interface ClusterSingletonServiceGroup<P extends Path<P>, E extends GenericEntity<P>,
                                       C extends GenericEntityOwnershipChange<P, E>> {

    /**
     * This method must be called once on startup to initialize this group.
     * It means create relevant Group Entity Candidate Registration.
     */
    void initializationClusterSingletonGroup();

    /**
     * Method registers service to ClusterSingletonServiceGroup. If the actual Cluster
     * Node holds Leadership, {@link ClusterSingletonService#instantiateServiceInstance()}
     * has to be called otherwise do nothig and wait for change cluster node leadership.
     *
     * @param service instance
     * @return closable {@link ClusterSingletonServiceRegistration}
     */
    ClusterSingletonServiceRegistration registerService(ClusterSingletonService service);

    /**
     * Method provides possibility to restart some service from group without change
     * leadership for whole group. {@link ClusterSingletonServiceRegistration#close()}
     * implementation has to call this service.
     *
     * @param service instance
     */
    void unregisterService(ClusterSingletonService service);

    /**
     * Method implementation has to apply ownershipChange for all registred services.
     *
     * @param ownershipChange change role for ClusterSingletonServiceGroup
     */
    void ownershipChanged(final C ownershipChange);

    /**
     * Method has to call {@link ClusterSingletonService#closeServiceInstance()} for all registred services.
     *
     * @return {@link ListenableFuture} in list for all Future from closing {@link ClusterSingletonService}
     */
    ListenableFuture<List<Void>> closingClusterSingletonGroup();

}

