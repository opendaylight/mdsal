/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.singleton.dom.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.mdsal.eos.common.api.CandidateAlreadyRegisteredException;
import org.opendaylight.mdsal.eos.common.api.GenericEntity;
import org.opendaylight.mdsal.eos.common.api.GenericEntityOwnershipChange;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.yangtools.concepts.Path;

final class PlaceholderGroup<P extends Path<P>, E extends GenericEntity<P>,
        C extends GenericEntityOwnershipChange<P, E>> extends ClusterSingletonServiceGroup<P, E, C> {
    private final List<ClusterSingletonService> services = new ArrayList<>(0);
    private final ClusterSingletonServiceGroup<P, E, C> previous;
    private final ListenableFuture<?> closeFuture;

    private ClusterSingletonServiceGroup<P, E, C> next;

    PlaceholderGroup(final ClusterSingletonServiceGroup<P, E, C> previous, final ListenableFuture<?> closeFuture) {
        this.previous = requireNonNull(previous);
        this.closeFuture = requireNonNull(closeFuture);
    }

    @Override
    public String getIdentifier() {
        return previous.getIdentifier();
    }

    @Override
    void initializationClusterSingletonGroup() throws CandidateAlreadyRegisteredException {
        throw new UnsupportedOperationException("This should never be invoked");
    }

    @Override
    void registerService(final ClusterSingletonService service) {
        verifyNoNext();
        services.add(service);
    }

    @Override
    boolean unregisterService(final ClusterSingletonService service) {
        if (next != null) {
            return next.unregisterService(service);
        }

        services.remove(service);
        return false;
    }

    @Override
    void ownershipChanged(final C ownershipChange) {
        verifyNoNext();
        previous.ownershipChanged(ownershipChange);
    }

    @Override
    ListenableFuture<?> closeClusterSingletonGroup() {
        return next == null ? closeFuture : next.closeClusterSingletonGroup();
    }

    List<ClusterSingletonService> getServices() {
        verifyNoNext();
        return ImmutableList.copyOf(services);
    }

    private void verifyNoNext() {
        Verify.verify(next == null, "Placeholder already superseded by %s", next);
    }
}
