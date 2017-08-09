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
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;

/**
 * Package protected help class represent a Delegator for {@link ClusterSingletonService}
 * instance and {@link ClusterSingletonServiceRegistration} implementation.
 * Close registration means remove {@link ClusterSingletonService} instance from internal
 * ClusterSingletonServiceGroup list reference.
 *
 *<p>
 * Close {@link ClusterSingletonServiceRegistration} is prepared for a possible restart
 * service or application in osgi container. Any another services from group can not be
 * stopped.
 */
class ClusterSingletonServiceRegistrationDelegator extends AbstractObjectRegistration<ClusterSingletonService>
        implements ClusterSingletonServiceRegistration, ClusterSingletonService {

    private final ClusterSingletonServiceGroup<?, ?, ?> group;

    ClusterSingletonServiceRegistrationDelegator(final ClusterSingletonService service,
            final ClusterSingletonServiceGroup<?, ?, ?> group) {
        super(Preconditions.checkNotNull(service));
        this.group = Preconditions.checkNotNull(group);
    }

    @Override
    public void instantiateServiceInstance() {
        getInstance().instantiateServiceInstance();
    }

    @Override
    public ListenableFuture<Void> closeServiceInstance() {
        return getInstance().closeServiceInstance();
    }

    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return getInstance().getIdentifier();
    }

    public String getServiceGroupIdentifier() {
        return getInstance().getIdentifier().getValue();
    }

    @Override
    protected void removeRegistration() {
        group.unregisterService(this);
    }
}