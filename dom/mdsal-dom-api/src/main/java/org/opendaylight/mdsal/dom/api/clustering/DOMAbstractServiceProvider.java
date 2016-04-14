/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.api.clustering;

import org.opendaylight.mdsal.common.api.clustering.AbstractGenericServiceProvider;
import org.opendaylight.mdsal.common.api.clustering.GenericEntityOwnershipService;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * Binding version of {@link AbstractGenericServiceProvider}
 */
public abstract class DOMAbstractServiceProvider extends
        AbstractGenericServiceProvider<YangInstanceIdentifier, DOMEntity, DOMEntityOwnershipChange, DOMEntityOwnershipListener, DOMEntityOwnershipService, DOMEntityOwnershipListenerRegistration>
        implements DOMEntityOwnershipListener {

    /**
     * Initialization all needed class internal property for {@link DOMAbstractServiceProvider}
     *
     * @param entityOwnershipService - we need only {@link GenericEntityOwnershipService}
     * @param serviceIdentifier - service registration identificator
     */
    public DOMAbstractServiceProvider(final DOMEntityOwnershipService entityOwnershipService,
            final String serviceIdentifier) {
        super(entityOwnershipService, serviceIdentifier);
    }

    @Override
    protected final DOMEntity createEntity(final String type, final String ident) {
        return new DOMEntity(type, ident);
    }

    @Override
    protected final DOMEntityOwnershipListenerRegistration registerListener(final String type,
            final DOMEntityOwnershipService eos) {
        return eos.registerListener(type, this);
    }
}
