/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.clustering;

import com.google.common.base.Preconditions;
import org.opendaylight.mdsal.binding.api.clustering.Entity;
import org.opendaylight.mdsal.binding.api.clustering.EntityOwnershipChange;
import org.opendaylight.mdsal.binding.api.clustering.EntityOwnershipListener;
import org.opendaylight.mdsal.binding.dom.adapter.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.dom.api.clustering.DOMEntityOwnershipChange;
import org.opendaylight.mdsal.dom.api.clustering.DOMEntityOwnershipListener;

/**
 * Adapter that bridges between the binding and DOM EntityOwnershipListener interfaces.
 *
 * @author Thomas Pantelis
 */
class DOMEntityOwnershipListenerAdapter implements DOMEntityOwnershipListener {
    private final BindingToNormalizedNodeCodec conversionCodec;
    private final EntityOwnershipListener bindingListener;

    DOMEntityOwnershipListenerAdapter(final EntityOwnershipListener bindingListener,
            final BindingToNormalizedNodeCodec conversionCodec) {
        this.bindingListener = Preconditions.checkNotNull(bindingListener);
        this.conversionCodec = Preconditions.checkNotNull(conversionCodec);
    }

    @Override
    public void ownershipChanged(final DOMEntityOwnershipChange ownershipChange) {
        try {
            final Entity entity = new Entity(ownershipChange.getEntity().getType(), conversionCodec.toBinding(
                    ownershipChange.getEntity().getIdentifier()).get());
            bindingListener.ownershipChanged(new EntityOwnershipChange(entity, ownershipChange.getState(),
                    ownershipChange.inJeopardy()));
        } catch (final Exception e) {
            BindingDOMEntityOwnershipServiceAdapter.LOG.error("Error converting DOM entity ID {} to binding InstanceIdentifier",
                    ownershipChange.getEntity().getIdentifier(), e);
        }
    }
}