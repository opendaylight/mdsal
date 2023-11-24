/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.BindingService;
import org.opendaylight.mdsal.dom.api.DOMService;

abstract class BindingDOMAdapterBuilder<T extends BindingService> extends AdapterBuilder<T, DOMService<?, ?>> {

    @FunctionalInterface
    interface Factory<T extends BindingService> {

        @NonNull BindingDOMAdapterBuilder<T> newBuilder(@NonNull AdapterContext adapterContext);
    }

    private final @NonNull AdapterContext adapterContext;

    BindingDOMAdapterBuilder(final AdapterContext adapterContext) {
        this.adapterContext = requireNonNull(adapterContext);
    }

    protected final @NonNull AdapterContext adapterContext() {
        return adapterContext;
    }
}
