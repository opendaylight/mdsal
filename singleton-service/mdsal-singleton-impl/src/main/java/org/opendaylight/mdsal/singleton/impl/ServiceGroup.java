/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.singleton.impl;

import com.google.common.util.concurrent.ListenableFuture;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.eos.common.api.CandidateAlreadyRegisteredException;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipStateChange;
import org.opendaylight.mdsal.eos.dom.api.DOMEntity;
import org.opendaylight.mdsal.singleton.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.api.ServiceGroupIdentifier;
import org.opendaylight.yangtools.concepts.Identifiable;

/**
 * {@link ServiceGroup} maintains a group of {@link ClusterSingletonService} instances.
 * All EntityOwnershipChange notifications have to applied to all registered services at the same time in the same
 * manner. All registered services have only one instantiated service instance in a cluster at one time on same
 * Cluster Node. This is realized via a double candidate approach where a service group instance maintains a candidate
 * registration for ownership of the service entity in the cluster and also a registration that acts as a guard to
 * ensure a service group instance has fully closed prior to relinquishing service ownership. To achieve ownership
 * of the service group, a service group candidate must hold ownership of both these entities.
 */
abstract sealed class ServiceGroup implements Identifiable<ServiceGroupIdentifier>
        permits ActiveServiceGroup, PlaceholderServiceGroup {
    /**
     * This method must be called once on startup to initialize this group and register the relevant group entity
     * candidate. It means create relevant Group Entity Candidate Registration.
     */
    abstract void initialize() throws CandidateAlreadyRegisteredException;

    /**
     * This method registers a service instance for this service group. If the local node has ownership of the service
     * group, the {@link ClusterSingletonService#instantiateServiceInstance()} method is called. Otherwise, the method
     * is called once the local node gains ownership.
     *
     * @param service instance
     */
    abstract void registerService(ServiceRegistration reg);

    /**
     * Method provides possibility to restart some service from group without change leadership for whole group.
     * {@link ServiceRegistration#removeRegistration()} implementation has to call this service.
     *
     * <p>
     * Candidates are signed for group, so unregistration for group with one service has to trigger new election only
     * otherwise we can see same behavior as on server without clustering.
     *
     * @param service instance
     * @return Future which completes when this instance is shutdown if this was the last registration, null otherwise
     */
    abstract @Nullable ListenableFuture<?> unregisterService(ServiceRegistration reg);

    /**
     * Method implementation has to apply ownershipChange for all registered services.
     *
     * @param entity the entity whose ownership status changed
     * @param change the change the entity underwent
     * @param inJeopardy {@code true} if ownership is in jeopardy and the reported change may be inaccurate
     */
    abstract void ownershipChanged(DOMEntity entity, EntityOwnershipStateChange change, boolean inJeopardy);

    /**
     * Closes this service group. All registered service providers are also closed. Please be careful
     * and use this method without active EOS listener only.
     *
     * @return {@link ListenableFuture} in list for all Future from closing {@link ClusterSingletonService}
     */
    abstract ListenableFuture<?> closeClusterSingletonGroup();
}

