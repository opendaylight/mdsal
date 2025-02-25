/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.dom.simple.di;

import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.mdsal.eos.common.api.CandidateAlreadyRegisteredException;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipState;
import org.opendaylight.mdsal.eos.dom.api.DOMEntity;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipListener;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipService;
import org.opendaylight.mdsal.eos.dom.simple.SimpleDOMEntityOwnershipService;
import org.opendaylight.yangtools.concepts.Registration;

/**
 * Simple {@link DOMEntityOwnershipService} operating as an isolated island. It has no awareness of the world outside
 * of itself.
 */
@Singleton
public final class LocalDOMEntityOwnershipService implements DOMEntityOwnershipService {
    private final SimpleDOMEntityOwnershipService delegate = new SimpleDOMEntityOwnershipService();

    @Inject
    public LocalDOMEntityOwnershipService() {
        // Exposed for DI
    }

    @Override
    public Optional<EntityOwnershipState> getOwnershipState(final DOMEntity forEntity) {
        return delegate.getOwnershipState(forEntity);
    }

    @Override
    public boolean isCandidateRegistered(final DOMEntity forEntity) {
        return delegate.isCandidateRegistered(forEntity);
    }

    @Override
    public Registration registerCandidate(final DOMEntity entity) throws CandidateAlreadyRegisteredException {
        return delegate.registerCandidate(entity);
    }

    @Override
    public Registration registerListener(final String entityType, final DOMEntityOwnershipListener listener) {
        return delegate.registerListener(entityType, listener);
    }
}
