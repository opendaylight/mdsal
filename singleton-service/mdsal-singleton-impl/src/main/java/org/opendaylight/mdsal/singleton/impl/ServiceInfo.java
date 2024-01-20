/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal-singleton.impl;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.ListenableFuture;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal-singleton.impl.ActiveServiceGroup.ServiceState;

final class ServiceInfo {
    static final @NonNull ServiceInfo STARTED = new ServiceInfo(ServiceState.STARTED, null);

    private final @Nullable ListenableFuture<?> future;
    private final @NonNull ServiceState state;

    private ServiceInfo(final ServiceState state, final @Nullable ListenableFuture<?> future) {
        this.state = requireNonNull(state);
        this.future = future;
    }

    @NonNull ServiceState getState() {
        return state;
    }

    @NonNull ListenableFuture<?> getFuture() {
        return verifyNotNull(future);
    }

    @NonNull ServiceInfo toState(final @NonNull ServiceState newState) {
        verify(state != newState, "Attempted to re-transition into %s", state);
        return new ServiceInfo(newState, null);
    }

    @NonNull ServiceInfo toState(final @NonNull ServiceState newState, final @NonNull ListenableFuture<?> newFuture) {
        verify(state != newState, "Attempted to re-transition into %s", state);
        return new ServiceInfo(newState, requireNonNull(newFuture));
    }
}
