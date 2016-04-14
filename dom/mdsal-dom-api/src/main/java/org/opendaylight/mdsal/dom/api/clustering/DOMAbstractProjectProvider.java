/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.api.clustering;

import org.opendaylight.mdsal.common.api.clustering.AbstractGenericProvider;
import org.opendaylight.mdsal.common.api.clustering.GenericEntityOwnershipService;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * Abstract class {@link DOMAbstractProjectProvider} provide a single cluster project instance
 * functionality for every ProjectProvider. So we will have only one full instantiated project in
 * whole cluster. Single instance functionality is realized by a Double candidate aproach.
 * First candidate represent a cluster role for every instance and second candidate represent
 * a quard for changing role in cluster. Master has registrated both candidate and both are holding
 * mastership for their entities. Main candidate hold the project role in cluster and second has to be
 * closed after full finish async. closing instance.
 */
public abstract class DOMAbstractProjectProvider
        extends
        AbstractGenericProvider<YangInstanceIdentifier, DOMEntity, DOMEntityOwnershipChange, DOMEntityOwnershipListener, DOMEntityOwnershipService, DOMEntityOwnershipListenerRegistration>
        implements DOMEntityOwnershipListener {

    /**
     * Initialization all needed class internal property for {@link DOMAbstractProjectProvider}
     *
     * @param entityOwnershipService - we need only {@link GenericEntityOwnershipService}
     */
    public DOMAbstractProjectProvider(final DOMEntityOwnershipService entityOwnershipService) {
        super(entityOwnershipService);
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
