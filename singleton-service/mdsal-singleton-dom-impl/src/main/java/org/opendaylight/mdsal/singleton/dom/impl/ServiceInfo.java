/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.singleton.dom.impl;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.singleton.dom.impl.ClusterSingletonServiceGroupImpl.ServiceState;

@NonNullByDefault
final class ServiceInfo {
    private final @Nullable ListenableFuture<?> future;
    private final ServiceState state;
    private final boolean removed;

    private ServiceInfo(final ServiceState state, final @Nullable ListenableFuture<?> future, final boolean removed) {
        this.state = requireNonNull(state);
        this.future = future;
        this.removed = removed;
    }

    static ServiceInfo stopped() {
        return new ServiceInfo(ServiceState.STOPPED, null, false);
    }

    ServiceState getState() {
        return state;
    }

    boolean isRemoved() {
        return removed;
    }

    Optional<ListenableFuture<?>> getFuture() {
        return (Optional) Optional.ofNullable(future);
    }

    ServiceInfo toRemoved() {
        verify(!removed, "Attempted to re-remove in %s", state);
        return new ServiceInfo(state, future, true);
    }

    ServiceInfo toState(final ServiceState state) {
        verify(this.state != state, "Attempted to re-transition into %s", state);
        return new ServiceInfo(state, null, removed);
    }

    ServiceInfo toState(final ServiceState state, final ListenableFuture<?> future) {
        verify(this.state != state, "Attempted to re-transition into %s", state);
        return new ServiceInfo(state, requireNonNull(future), removed);
    }
}