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
import org.opendaylight.mdsal.binding.javav2.api.DataBroker;
import org.opendaylight.mdsal.binding.javav2.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.javav2.api.NotificationService;
import org.opendaylight.mdsal.binding.javav2.api.RpcActionConsumerRegistry;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.data.BindingDOMDataBrokerAdapter;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.notification.BindingDOMNotificationPublishServiceAdapter;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.notification.BindingDOMNotificationServiceAdapter;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.operation.BindingDOMOperationServiceAdapter;
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

    @SuppressWarnings("checkstyle:GenericWhitespace")
    private static final Map<Class<?>, BindingDOMAdapterBuilder.Factory<?>> FACTORIES =
            ImmutableMap.<Class<?>, BindingDOMAdapterBuilder.Factory<?>> builder()
                    .put(DataBroker.class, BindingDOMDataBrokerAdapter.BUILDER_FACTORY)
                    .put(NotificationPublishService.class, BindingDOMNotificationPublishServiceAdapter.BUILDER_FACTORY)
                    .put(NotificationService.class, BindingDOMNotificationServiceAdapter.BUILDER_FACTORY)
                    .put(RpcActionConsumerRegistry.class, BindingDOMOperationServiceAdapter.BUILDER_FACTORY)
                    .build();

    private final BindingToNormalizedNodeCodec codec;

    public BindingDOMAdapterLoader(final BindingToNormalizedNodeCodec codec) {
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