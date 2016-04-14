/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.api.clustering;

import org.opendaylight.mdsal.common.api.clustering.AbstractGenericProvider;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Abstract class {@link AbstractProjectProvider} provide a single cluster project instance
 * functionality for every ProjectProvider. So we will have only one full instantiated project in
 * whole cluster. Single instance functionality is realized by a Double candidate aproach.
 * First candidate represent a cluster role for every instance and second candidate represent
 * a quard for changing role in cluster. Master has registrated both candidate and both are holding
 * mastership for their entities. Main candidate hold the project role in cluster and second has to be
 * closed after full finish async. closing instance.
 */
public abstract class AbstractProjectProvider
        extends
        AbstractGenericProvider<InstanceIdentifier<?>, Entity, EntityOwnershipChange, EntityOwnershipListener, EntityOwnershipService, EntityOwnershipListenerRegistration>
        implements EntityOwnershipListener {

    /**
     * Initialization all needed class internal property for {@link AbstractProjectProvider}
     *
     * @param entityOwnershipService - we need only {@link EntityOwnershipService}
     */
    public AbstractProjectProvider(final EntityOwnershipService entityOwnershipService) {
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

}
