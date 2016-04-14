/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.singleton.binding.impl;

import org.opendaylight.mdsal.eos.binding.api.Entity;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipChange;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipListener;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipListenerRegistration;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipService;
import org.opendaylight.mdsal.eos.common.api.GenericEntityOwnershipService;
import org.opendaylight.mdsal.singleton.binding.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.spi.AbstractClusterSingletonServiceProviderImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.core.general.entity.rev150930.EntityKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Binding version of {@link AbstractClusterSingletonServiceProviderImpl}
 */
public final class ClusterSingletonServiceProviderImpl extends
        AbstractClusterSingletonServiceProviderImpl<InstanceIdentifier<?>, Entity,
                                                    EntityOwnershipChange,
                                                    EntityOwnershipListener,
                                                    EntityOwnershipService,
                                                    EntityOwnershipListenerRegistration>
        implements ClusterSingletonServiceProvider {

    /**
     * Initialization all needed class internal property for {@link ClusterSingletonServiceProviderImpl}
     *
     * @param entityOwnershipService - we need only {@link GenericEntityOwnershipService}
     */
    public ClusterSingletonServiceProviderImpl(final EntityOwnershipService entityOwnershipService) {
        super(entityOwnershipService);
    }

    @Override
    protected final Entity createEntity(final String type, final String ident) {
        return new Entity(type, ident);
    }

    @Override
    protected final EntityOwnershipListenerRegistration registerListener(final String type,
            final EntityOwnershipService eos) {
        return eos.registerListener(type, this);
    }

    @Override
    protected final String getServiceIdentifierFromEntity(final Entity entity) {
        final InstanceIdentifier<?> ii = entity.getIdentifier();
        final EntityKey entityKey = ii.firstKeyOf(
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsal.core.general.entity.rev150930.Entity.class);
        return entityKey.getName();
    }
}
