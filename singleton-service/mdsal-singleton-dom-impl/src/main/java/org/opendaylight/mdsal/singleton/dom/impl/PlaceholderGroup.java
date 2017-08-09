/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.singleton.dom.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.Verify;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.mdsal.eos.common.api.CandidateAlreadyRegisteredException;
import org.opendaylight.mdsal.eos.common.api.GenericEntity;
import org.opendaylight.mdsal.eos.common.api.GenericEntityOwnershipChange;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.yangtools.concepts.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class PlaceholderGroup<P extends Path<P>, E extends GenericEntity<P>,
        C extends GenericEntityOwnershipChange<P, E>> extends ClusterSingletonServiceGroup<P, E, C> {
    private static final Logger LOG = LoggerFactory.getLogger(PlaceholderGroup.class);

    private final List<ClusterSingletonService> services = new ArrayList<>(0);
    private final ClusterSingletonServiceGroup<P, E, C> previous;
    private final ListenableFuture<?> closeFuture;

    private volatile ClusterSingletonServiceGroup<P, E, C> successor;

    PlaceholderGroup(final ClusterSingletonServiceGroup<P, E, C> previous, final ListenableFuture<?> closeFuture) {
        this.previous = requireNonNull(previous);
        this.closeFuture = requireNonNull(closeFuture);
    }

    @Override
    public String getIdentifier() {
        return previous.getIdentifier();
    }

    @Override
    void initialize() throws CandidateAlreadyRegisteredException {
        throw new UnsupportedOperationException("This should never be invoked");
    }

    @Override
    void registerService(final ClusterSingletonService service) {
        verifyNoSuccessor();
        services.add(service);
        LOG.debug("{}: added service {}", this, service);
    }

    @Override
    boolean unregisterService(final ClusterSingletonService service) {
        final ClusterSingletonServiceGroup<P, E, C> local = successor;
        if (local != null) {
            return local.unregisterService(service);
        }

        services.remove(service);
        LOG.debug("{}: removed service {}", this, service);
        return false;
    }

    @Override
    void ownershipChanged(final C ownershipChange) {
        // This really should not happen, but let's be defensive
        final ClusterSingletonServiceGroup<P, E, C> local = successor;
        (local == null ? previous : local).ownershipChanged(ownershipChange);
    }

    @Override
    ListenableFuture<?> closeClusterSingletonGroup() {
        final ClusterSingletonServiceGroup<P, E, C> local = successor;
        return local == null ? closeFuture : local.closeClusterSingletonGroup();
    }

    // Note: this is a leaked structure, the caller can reuse it at will, but has to regard
    List<ClusterSingletonService> getServices() {
        verifyNoSuccessor();
        LOG.trace("{}: returning services {}", this, services);
        return services;
    }

    void setSuccessor(final ClusterSingletonServiceGroup<P, E, C> successor) {
        verifyNoSuccessor();
        this.successor = Verify.verifyNotNull(successor);
        LOG.debug("{}: successor set to {}", this, successor);
    }

    private void verifyNoSuccessor() {
        Verify.verify(successor == null, "Placeholder already superseded by %s", successor);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", getIdentifier()).toString();
    }
}
