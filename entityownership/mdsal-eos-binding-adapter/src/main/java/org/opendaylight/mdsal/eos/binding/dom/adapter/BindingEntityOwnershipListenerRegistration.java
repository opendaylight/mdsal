/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.binding.dom.adapter;

import com.google.common.base.Preconditions;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipListener;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipListenerRegistration;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipListenerRegistration;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;

/**
 * Implementation of EntityOwnershipListenerRegistration whose instances are returned from the
 * {@link BindingDOMEntityOwnershipServiceAdapter}.
 *
 * @author Thomas Pantelis
 */
class BindingEntityOwnershipListenerRegistration extends AbstractObjectRegistration<EntityOwnershipListener>
        implements EntityOwnershipListenerRegistration {
    private final String entityType;
    private final DOMEntityOwnershipListenerRegistration domRegistration;

    BindingEntityOwnershipListenerRegistration(final String entityType, final EntityOwnershipListener listener,
            final DOMEntityOwnershipListenerRegistration domRegistration) {
        super(listener);
        this.entityType = Preconditions.checkNotNull(entityType);
        this.domRegistration = Preconditions.checkNotNull(domRegistration);
    }

    @Override
    public String getEntityType() {
        return entityType;
    }

    @Override
    protected void removeRegistration() {
        domRegistration.close();
    }
}