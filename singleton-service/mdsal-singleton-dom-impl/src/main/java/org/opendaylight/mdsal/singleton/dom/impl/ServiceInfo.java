/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.singleton.dom.impl;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.ListenableFuture;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.singleton.dom.impl.ClusterSingletonServiceGroupImpl.ServiceState;

@NonNullByDefault
@SuppressFBWarnings(value = "NP_NULL_PARAM_DEREF_NONVIRTUAL", justification = "SpotBugs does not grok @Nullable field")
final class ServiceInfo {
    private static final ServiceInfo STARTED = new ServiceInfo(ServiceState.STARTED, null);

    private final @Nullable ListenableFuture<?> future;
    private final ServiceState state;

    private ServiceInfo(final ServiceState state, final @Nullable ListenableFuture<?> future) {
        this.state = requireNonNull(state);
        this.future = future;
    }

    static ServiceInfo started() {
        return STARTED;
    }

    ServiceState getState() {
        return state;
    }

    ListenableFuture<?> getFuture() {
        return verifyNotNull(future);
    }

    ServiceInfo toState(final ServiceState newState) {
        verify(state != newState, "Attempted to re-transition into %s", state);
        return new ServiceInfo(newState, null);
    }

    ServiceInfo toState(final ServiceState newState, final ListenableFuture<?> newFuture) {
        verify(state != newState, "Attempted to re-transition into %s", state);
        return new ServiceInfo(newState, requireNonNull(newFuture));
    }
}
