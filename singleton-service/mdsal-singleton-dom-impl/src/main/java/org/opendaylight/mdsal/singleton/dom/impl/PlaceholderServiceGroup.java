/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.singleton.dom.impl;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.mdsal.eos.common.api.CandidateAlreadyRegisteredException;
import org.opendaylight.mdsal.eos.common.api.EntityOwnershipStateChange;
import org.opendaylight.mdsal.eos.dom.api.DOMEntity;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Intermediate place-holder to catch user requests while asynchronous shutdown of previous incarnation of
 * a {@link ServiceGroup} finishes.
 */
final class PlaceholderServiceGroup extends ServiceGroup {
    private static final Logger LOG = LoggerFactory.getLogger(PlaceholderServiceGroup.class);

    private final List<ServiceRegistration> services = new ArrayList<>(0);
    private final ServiceGroup previous;
    private final ListenableFuture<?> closeFuture;

    private volatile ServiceGroup successor;

    PlaceholderServiceGroup(final ServiceGroup previous, final ListenableFuture<?> closeFuture) {
        this.previous = requireNonNull(previous);
        this.closeFuture = requireNonNull(closeFuture);
    }

    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return previous.getIdentifier();
    }

    @Override
    void initialize() throws CandidateAlreadyRegisteredException {
        throw new UnsupportedOperationException("This should never be invoked");
    }

    @Override
    void registerService(final ServiceRegistration reg) {
        verifyNoSuccessor();
        services.add(reg);
        LOG.debug("{}: added service {}", this, reg.getInstance());
    }

    @Override
    ListenableFuture<?> unregisterService(final ServiceRegistration reg) {
        verifyNoSuccessor();
        services.remove(reg);
        LOG.debug("{}: removed service {}", this, reg.getInstance());
        return null;
    }

    @Override
    void ownershipChanged(final DOMEntity entity, final EntityOwnershipStateChange change, final boolean inJeopardy) {
        // This really should not happen, but let's be defensive
        final var local = successor;
        (local == null ? previous : local).ownershipChanged(entity, change, inJeopardy);
    }

    @Override
    ListenableFuture<?> closeClusterSingletonGroup() {
        final var local = successor;
        return local == null ? closeFuture : local.closeClusterSingletonGroup();
    }

    // Note: this is a leaked structure, the caller can reuse it at will, but has to regard
    List<ServiceRegistration> getServices() {
        verifyNoSuccessor();
        LOG.trace("{}: returning services {}", this, services);
        return services;
    }

    void setSuccessor(final ServiceGroup successor) {
        verifyNoSuccessor();
        this.successor = verifyNotNull(successor);
        LOG.debug("{}: successor set to {}", this, successor);
    }

    private void verifyNoSuccessor() {
        verify(successor == null, "Placeholder already superseded by %s", successor);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", getIdentifier()).toString();
    }
}
