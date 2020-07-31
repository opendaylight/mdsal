/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.adapter.AdapterContext;
import org.opendaylight.mdsal.eos.binding.api.Entity;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipCandidateRegistration;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipListener;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipListenerRegistration;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipService;
import org.opendaylight.mdsal.eos.common.api.CandidateAlreadyRegisteredException;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipState;
import org.opendaylight.mdsal.eos.dom.api.DOMEntity;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipService;

/**
 * Adapter that bridges between the binding and DOM EntityOwnershipService interfaces.
 *
 * @author Thomas Pantelis
 */
@Singleton
public class BindingDOMEntityOwnershipServiceAdapter implements EntityOwnershipService, AutoCloseable {
    private final @NonNull DOMEntityOwnershipService domService;
    private final @NonNull AdapterContext adapterContext;

    @Inject
    public BindingDOMEntityOwnershipServiceAdapter(final @NonNull DOMEntityOwnershipService domService,
            final @NonNull AdapterContext adapterContext) {
        this.domService = requireNonNull(domService);
        this.adapterContext = requireNonNull(adapterContext);
    }

    @Override
    public EntityOwnershipCandidateRegistration registerCandidate(final Entity entity)
            throws CandidateAlreadyRegisteredException {
        return new BindingEntityOwnershipCandidateRegistration(
                domService.registerCandidate(toDOMEntity(entity)), entity);
    }

    @Override
    public EntityOwnershipListenerRegistration registerListener(final String entityType,
            final EntityOwnershipListener listener) {
        return new BindingEntityOwnershipListenerRegistration(entityType, listener,
            domService.registerListener(entityType, new DOMEntityOwnershipListenerAdapter(listener, adapterContext)));
    }

    @Override
    public Optional<EntityOwnershipState> getOwnershipState(final Entity forEntity) {
        return domService.getOwnershipState(toDOMEntity(forEntity));
    }

    @Override
    public boolean isCandidateRegistered(final Entity forEntity) {
        return domService.isCandidateRegistered(toDOMEntity(forEntity));
    }

    private @NonNull DOMEntity toDOMEntity(final Entity entity) {
        return new DOMEntity(entity.getType(),
            adapterContext.currentSerializer().toYangInstanceIdentifier(entity.getIdentifier()));
    }

    @Override
    public void close() {
    }
}
