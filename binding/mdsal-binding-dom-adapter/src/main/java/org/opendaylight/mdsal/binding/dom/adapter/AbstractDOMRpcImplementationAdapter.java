/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;

abstract sealed class AbstractDOMRpcImplementationAdapter<T> implements DOMRpcImplementation
        permits LegacyDOMRpcImplementationAdapter, DOMRpcImplementationAdapter {
    // Default implementations are 0, we need to perform some translation, hence we have a slightly higher cost
    private static final int COST = 1;

    private final AdapterContext adapterContext;
    private final @NonNull T delegate;

    AbstractDOMRpcImplementationAdapter(final AdapterContext adapterContext, final T delegate) {
        this.adapterContext = requireNonNull(adapterContext);
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public final long invocationCost() {
        return COST;
    }

    final @NonNull CurrentAdapterSerializer currentSerializer() {
        return adapterContext.currentSerializer();
    }

    final @NonNull T delegate() {
        return delegate;
    }
}
