/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ClassToInstanceMap;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.BindingService;
import org.opendaylight.mdsal.dom.api.DOMService;

abstract class BindingDOMAdapterBuilder<T extends BindingService> extends AdapterBuilder<T, DOMService> {

    @FunctionalInterface
    interface Factory<T extends BindingService> {

        BindingDOMAdapterBuilder<T> newBuilder();
    }

    private AdapterContext codec;

    void setCodec(final AdapterContext codec) {
        this.codec = requireNonNull(codec);
    }

    @Override
    protected final T createInstance(final ClassToInstanceMap<DOMService> delegates) {
        checkState(codec != null);
        return createInstance(codec, delegates);
    }

    abstract @NonNull T createInstance(@NonNull AdapterContext myCodec,
            @NonNull ClassToInstanceMap<@NonNull DOMService> delegates);
}
