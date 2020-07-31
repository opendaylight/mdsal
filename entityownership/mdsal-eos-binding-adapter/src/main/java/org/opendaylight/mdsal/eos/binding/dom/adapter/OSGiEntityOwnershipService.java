/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.binding.dom.adapter;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.opendaylight.mdsal.binding.dom.adapter.AdapterContext;
import org.opendaylight.mdsal.eos.binding.api.Entity;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipCandidateRegistration;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipListener;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipListenerRegistration;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipService;
import org.opendaylight.mdsal.eos.common.api.CandidateAlreadyRegisteredException;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipState;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
@Component(immediate = true, property = "type=default")
public final class OSGiEntityOwnershipService implements EntityOwnershipService {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiEntityOwnershipService.class);

    @Reference
    DOMEntityOwnershipService domService = null;
    @Reference
    AdapterContext adapterContext = null;

    private BindingDOMEntityOwnershipServiceAdapter delegate;

    @Override
    public EntityOwnershipCandidateRegistration registerCandidate(final Entity entity)
            throws CandidateAlreadyRegisteredException {
        return delegate.registerCandidate(entity);
    }

    @Override
    public EntityOwnershipListenerRegistration registerListener(final String entityType,
            final EntityOwnershipListener listener) {
        return delegate.registerListener(entityType, listener);
    }

    @Override
    public Optional<EntityOwnershipState> getOwnershipState(final Entity forEntity) {
        return delegate.getOwnershipState(forEntity);
    }

    @Override
    public boolean isCandidateRegistered(final Entity forEntity) {
        return delegate.isCandidateRegistered(forEntity);
    }

    @Activate
    void activate() {
        LOG.info("Entity Ownership Service adapter starting");
        delegate = new BindingDOMEntityOwnershipServiceAdapter(domService, adapterContext);
        LOG.info("Entity Ownership Service adapter started");
    }

    @Deactivate
    void deactivate() {
        LOG.info("Entity Ownership Service adapter stopping");
        delegate.close();
        delegate = null;
        LOG.info("Entity Ownership Service adapter stopped");

    }
}
