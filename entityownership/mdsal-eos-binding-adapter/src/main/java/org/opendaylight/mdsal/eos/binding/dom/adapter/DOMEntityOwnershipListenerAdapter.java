/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.binding.dom.adapter;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.opendaylight.mdsal.binding.dom.adapter.AdapterContext;
import org.opendaylight.mdsal.eos.binding.api.Entity;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipChange;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipListener;
import org.opendaylight.mdsal.eos.dom.api.DOMEntity;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipChange;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipListener;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter that bridges between the binding and DOM EntityOwnershipListener interfaces.
 *
 * @author Thomas Pantelis
 */
final class DOMEntityOwnershipListenerAdapter implements DOMEntityOwnershipListener {
    private static final Logger LOG = LoggerFactory.getLogger(DOMEntityOwnershipListenerAdapter.class);

    private final EntityOwnershipListener bindingListener;
    private final AdapterContext adapterContext;

    DOMEntityOwnershipListenerAdapter(final EntityOwnershipListener bindingListener,
            final AdapterContext adapterContext) {
        this.bindingListener = requireNonNull(bindingListener);
        this.adapterContext = requireNonNull(adapterContext);
    }

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE", justification = "generic getEntity()")
    public void ownershipChanged(final DOMEntityOwnershipChange ownershipChange) {
        final DOMEntity domEntity = ownershipChange.getEntity();
        final YangInstanceIdentifier domId = domEntity.getIdentifier();
        final InstanceIdentifier<?> bindingId;
        try {
            bindingId = verifyNotNull(adapterContext.currentSerializer().fromYangInstanceIdentifier(domId));
        } catch (RuntimeException e) {
            LOG.error("Error converting DOM entity ID {} to binding InstanceIdentifier", domId, e);
            return;
        }

        final Entity bindingEntity = new Entity(domEntity.getType(), bindingId);
        final EntityOwnershipChange change = new EntityOwnershipChange(bindingEntity,
            ownershipChange.getState(), ownershipChange.inJeopardy());
        try {
            bindingListener.ownershipChanged(change);
        } catch (Exception e) {
            LOG.error("Listener {} failed during change notification {}", bindingListener, change, e);
        }
    }
}
