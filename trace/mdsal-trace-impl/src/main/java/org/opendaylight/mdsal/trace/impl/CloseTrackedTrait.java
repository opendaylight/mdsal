/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.trace.impl;

import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Implementation of {@link CloseTracked} which can be used as a field in another class which implements
 * {@link CloseTracked} and delegates its methods to this.
 *
 * <p>This is useful if that class already has another parent class. If it does not, then it's typically more convenient
 * to just extend AbstractCloseTracked.
 *
 * @author Michael Vorburger.ch
 */
final class CloseTrackedTrait<T extends CloseTracked<T>> implements CloseTracked<T> {
    // NB: It's important that we keep a Throwable here, and not directly the StackTraceElement[] !
    // This is because creating a new Throwable() is a lot less expensive in terms of runtime overhead
    // than actually calling its getStackTrace(), which we can delay until we really need to.
    // see also e.g. https://stackoverflow.com/a/26122232/421602
    private final @Nullable Throwable allocationContext;
    private final CloseTrackedRegistry<T> closeTrackedRegistry;
    private final CloseTracked<T> realCloseTracked;

    CloseTrackedTrait(final CloseTrackedRegistry<T> transactionChainRegistry, final CloseTracked<T> realCloseTracked) {
        if (transactionChainRegistry.isDebugContextEnabled()) {
            // NB: We're NOT doing the (expensive) getStackTrace() here just yet (only below)
            // TODO When we're on Java 9, then instead use the new java.lang.StackWalker API..
            allocationContext = new Throwable();
        } else {
            allocationContext = null;
        }
        this.realCloseTracked = requireNonNull(realCloseTracked, "realCloseTracked");
        closeTrackedRegistry = requireNonNull(transactionChainRegistry, "transactionChainRegistry");
        closeTrackedRegistry.add(this);
    }

    @Override
    @SuppressFBWarnings("PZLA_PREFER_ZERO_LENGTH_ARRAYS")
    public @Nullable StackTraceElement[] getAllocationContextStackTrace() {
        final var local = allocationContext;
        return local != null ? local.getStackTrace() : null;
    }

    public void removeFromTrackedRegistry() {
        closeTrackedRegistry.remove(this);
    }

    @Override
    public CloseTracked<T> getRealCloseTracked() {
        return realCloseTracked;
    }
}
