/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.api.clustering;

import org.opendaylight.mdsal.common.api.clustering.AbstractGenericServiceProvider;
import org.opendaylight.mdsal.common.api.clustering.GenericEntityOwnershipService;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Binding version of {@link AbstractGenericServiceProvider}
 */
public abstract class AbstractServiceProvider extends
        AbstractGenericServiceProvider<InstanceIdentifier<?>, Entity, EntityOwnershipChange, EntityOwnershipListener, EntityOwnershipService, EntityOwnershipListenerRegistration>
        implements EntityOwnershipListener {

    /**
     * Initialization all needed class internal property for {@link AbstractServiceProvider}
     *
     * @param entityOwnershipService - we need only {@link GenericEntityOwnershipService}
     * @param serviceIdentifier - service registration identificator
     */
    public AbstractServiceProvider(final EntityOwnershipService entityOwnershipService,
            final String serviceIdentifier) {
        super(entityOwnershipService, serviceIdentifier);
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
