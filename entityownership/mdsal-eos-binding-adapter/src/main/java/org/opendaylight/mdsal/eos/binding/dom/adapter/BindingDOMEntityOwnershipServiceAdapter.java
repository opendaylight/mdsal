/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.binding.dom.adapter;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.lang.AutoCloseable;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.dom.adapter.BindingToNormalizedNodeCodec;
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

    private final DOMEntityOwnershipService domService;
    private final BindingToNormalizedNodeCodec conversionCodec;

    public BindingDOMEntityOwnershipServiceAdapter(@Nonnull DOMEntityOwnershipService domService,
            @Nonnull BindingToNormalizedNodeCodec conversionCodec) {
        this.domService = Preconditions.checkNotNull(domService);
        this.conversionCodec = Preconditions.checkNotNull(conversionCodec);
    }

    @Override
    public EntityOwnershipCandidateRegistration registerCandidate(Entity entity)
            throws CandidateAlreadyRegisteredException {
        return new BindingEntityOwnershipCandidateRegistration(domService.registerCandidate(toDOMEntity(entity)), entity);
    }

    @Override
    public EntityOwnershipListenerRegistration registerListener(String entityType, EntityOwnershipListener listener) {
        return new BindingEntityOwnershipListenerRegistration(entityType, listener, domService.
                registerListener(entityType, new DOMEntityOwnershipListenerAdapter(listener, conversionCodec)));
    }

    @Override
    public Optional<EntityOwnershipState> getOwnershipState(Entity forEntity) {
        return domService.getOwnershipState(toDOMEntity(forEntity));
    }

    @Override
    public boolean isCandidateRegistered(Entity forEntity) {
        return domService.isCandidateRegistered(toDOMEntity(forEntity));
    }

    private DOMEntity toDOMEntity(Entity entity) {
        return new DOMEntity(entity.getType(), conversionCodec.toNormalized(entity.getIdentifier()));
    }

    @Override
    public void close() {
    }
}
