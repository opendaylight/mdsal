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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.eos.binding.api.Entity;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipCandidateRegistration;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipListener;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipListenerRegistration;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipService;
import org.opendaylight.mdsal.eos.common.api.CandidateAlreadyRegisteredException;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipState;
import org.opendaylight.mdsal.eos.dom.api.DOMEntity;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter that bridges between the binding and DOM EntityOwnershipService interfaces.
 *
 * @author Thomas Pantelis
 */
public class BindingDOMEntityOwnershipServiceAdapter implements EntityOwnershipService, AutoCloseable {
    static final Logger LOG = LoggerFactory.getLogger(BindingDOMEntityOwnershipServiceAdapter.class);

    private final @NonNull DOMEntityOwnershipService domService;
    private final @NonNull BindingNormalizedNodeSerializer conversionCodec;

    public BindingDOMEntityOwnershipServiceAdapter(final @NonNull DOMEntityOwnershipService domService,
            @NonNull final BindingNormalizedNodeSerializer conversionCodec) {
        this.domService = requireNonNull(domService);
        this.conversionCodec = requireNonNull(conversionCodec);
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
            domService.registerListener(entityType, new DOMEntityOwnershipListenerAdapter(listener, conversionCodec)));
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
        return new DOMEntity(entity.getType(), conversionCodec.toYangInstanceIdentifier(entity.getIdentifier()));
    }

    @Override
    public void close() {
    }
}
