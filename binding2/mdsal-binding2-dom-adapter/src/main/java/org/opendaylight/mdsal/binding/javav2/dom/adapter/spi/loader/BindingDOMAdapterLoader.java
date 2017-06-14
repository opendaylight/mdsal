/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.spi.loader;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.api.BindingService;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.spi.builder.AdapterBuilder;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.spi.builder.BindingDOMAdapterBuilder;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.spi.builder.BindingDOMAdapterBuilder.Factory;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.dom.api.DOMService;

/**
 * Loader for factory of services.
 */
@Beta
public abstract class BindingDOMAdapterLoader extends AdapterLoader<BindingService, DOMService> {

    // TODO add all factory of services
    private static final Map<Class<?>, BindingDOMAdapterBuilder.Factory<?>> FACTORIES =
            ImmutableMap.<Class<?>, BindingDOMAdapterBuilder.Factory<?>> builder().build();

    private final BindingToNormalizedNodeCodec codec;

    public BindingDOMAdapterLoader(final BindingToNormalizedNodeCodec codec) {
        super();
        this.codec = codec;
    }

    @Nonnull
    @Override
    protected final AdapterBuilder<? extends BindingService, DOMService>
            createBuilder(final Class<? extends BindingService> key) {
        final Factory<?> factory = FACTORIES.get(key);
        Preconditions.checkArgument(factory != null, "Unsupported service type %s", key);
        final BindingDOMAdapterBuilder<?> builder = factory.newBuilder();
        builder.setCodec(codec);
        return builder;
    }
}