/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.binding.dom.adapter;

import com.google.common.base.Preconditions;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.eos.binding.api.Entity;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipChange;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipListener;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipChange;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipListener;

/**
 * Adapter that bridges between the binding and DOM EntityOwnershipListener interfaces.
 *
 * @author Thomas Pantelis
 */
class DOMEntityOwnershipListenerAdapter implements DOMEntityOwnershipListener {
    private final BindingNormalizedNodeSerializer conversionCodec;
    private final EntityOwnershipListener bindingListener;

    DOMEntityOwnershipListenerAdapter(final EntityOwnershipListener bindingListener,
            final BindingNormalizedNodeSerializer conversionCodec) {
        this.bindingListener = Preconditions.checkNotNull(bindingListener);
        this.conversionCodec = Preconditions.checkNotNull(conversionCodec);
    }

    @Override
    public void ownershipChanged(final DOMEntityOwnershipChange ownershipChange) {
        try {
            final Entity entity = new Entity(ownershipChange.getEntity().getType(),
                    conversionCodec.fromYangInstanceIdentifier(ownershipChange.getEntity().getIdentifier()));
            bindingListener.ownershipChanged(new EntityOwnershipChange(entity, ownershipChange.getState(),
                    ownershipChange.inJeopardy()));
        } catch (final Exception e) {
            BindingDOMEntityOwnershipServiceAdapter.LOG.error("Error converting DOM entity ID {} to binding InstanceIdentifier",
                    ownershipChange.getEntity().getIdentifier(), e);
        }
    }
}