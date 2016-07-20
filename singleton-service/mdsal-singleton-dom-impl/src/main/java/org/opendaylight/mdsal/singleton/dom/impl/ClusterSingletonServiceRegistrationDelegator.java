/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.singleton.dom.impl;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;

/**
 * Package protected help class represent a Delegator for {@link ClusterSingletonService}
 * instance and {@link ClusterSingletonServiceRegistration} implementation.
 * Close registration means remove {@link ClusterSingletonService} instance from internal
 * ClusterSingletonServiceGroup list reference.
 *
 * Close {@link ClusterSingletonServiceRegistration} is prepared for a possible restart
 * service or application in osgi container. Any another services from group can not be
 * stopped.
 */
class ClusterSingletonServiceRegistrationDelegator
        implements ClusterSingletonServiceRegistration, ClusterSingletonService {

    private final ClusterSingletonService service;
    private final ClusterSingletonServiceGroup<?, ?, ?> group;

    ClusterSingletonServiceRegistrationDelegator(final ClusterSingletonService service,
            final ClusterSingletonServiceGroup<?, ?, ?> group) {
        this.service = Preconditions.checkNotNull(service);
        this.group = Preconditions.checkNotNull(group);
    }

    @Override
    public void close() throws Exception {
        group.unregisterService(this);
    }

    @Override
    public void instantiateServiceInstance() {
        service.instantiateServiceInstance();
    }

    @Override
    public ListenableFuture<Void> closeServiceInstance() {
        return service.closeServiceInstance();
    }

    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return service.getIdentifier();
    }

    public String getServiceGroupIdentifier() {
        return service.getIdentifier().getValue();
    }
}