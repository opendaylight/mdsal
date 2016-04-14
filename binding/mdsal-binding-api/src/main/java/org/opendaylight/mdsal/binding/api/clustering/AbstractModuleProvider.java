/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.api.clustering;

import org.opendaylight.mdsal.common.api.clustering.AbstractGenericModuleProvider;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Binding version of {@link AbstractGenericModuleProvider}
 */
public abstract class AbstractModuleProvider
        extends
        AbstractGenericModuleProvider<InstanceIdentifier<?>, Entity, EntityOwnershipChange, EntityOwnershipListener, EntityOwnershipService, EntityOwnershipListenerRegistration>
        implements EntityOwnershipListener {

    /**
     * Initialization all needed class internal property for {@link AbstractModuleProvider}
     *
     * @param entityOwnershipService - we need only {@link EntityOwnershipService}
     */
    public AbstractModuleProvider(final EntityOwnershipService entityOwnershipService) {
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
