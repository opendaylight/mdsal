/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.spi.builder;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.ClassToInstanceMap;
import org.opendaylight.mdsal.binding.javav2.api.BindingService;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.dom.api.DOMService;

/**
 * Binding DOM adapter builder instance.
 *
 * @param <T>
 *            - Binding Service type
 */
@Beta
public abstract class BindingDOMAdapterBuilder<T extends BindingService> extends AdapterBuilder<T, DOMService> {

    private BindingToNormalizedNodeCodec codec;

    protected abstract T createInstance(BindingToNormalizedNodeCodec myCodec, ClassToInstanceMap<DOMService> delegates);

    @Override
    protected final T createInstance(final ClassToInstanceMap<DOMService> delegates) {
        Preconditions.checkState(codec != null);
        return createInstance(codec, delegates);
    }

    /**
     * Set codec for builder.
     *
     * @param codec
     *            - binding normalized node codec
     */
    public void setCodec(final BindingToNormalizedNodeCodec codec) {
        this.codec = codec;
    }

    /**
     * Factory for creating of new builder.
     *
     * @param <T>
     *            - Binding Service type
     */
    public interface Factory<T extends BindingService> {

        /**
         * Prepare new builder for service.
         *
         * @return adapter builder
         */
        BindingDOMAdapterBuilder<T> newBuilder();
    }
}
