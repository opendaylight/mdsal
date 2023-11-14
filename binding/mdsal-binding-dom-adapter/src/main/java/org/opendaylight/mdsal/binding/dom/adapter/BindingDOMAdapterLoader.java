/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.opendaylight.mdsal.binding.api.ActionProviderService;
import org.opendaylight.mdsal.binding.api.ActionService;
import org.opendaylight.mdsal.binding.api.BindingService;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.InstanceNotificationPublishService;
import org.opendaylight.mdsal.binding.api.InstanceNotificationService;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.api.RpcService;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMAdapterBuilder.Factory;
import org.opendaylight.mdsal.dom.api.DOMService;

public abstract class BindingDOMAdapterLoader extends AdapterLoader<BindingService, DOMService> {
    private static final Map<Class<?>,BindingDOMAdapterBuilder.Factory<?>> FACTORIES =
            ImmutableMap.<Class<?>, BindingDOMAdapterBuilder.Factory<?>>builder()
            .put(NotificationService.class, BindingDOMNotificationServiceAdapter.BUILDER_FACTORY)
            .put(NotificationPublishService.class, BindingDOMNotificationPublishServiceAdapter.BUILDER_FACTORY)
            .put(DataBroker.class, BindingDOMDataBrokerAdapter.BUILDER_FACTORY)
            .put(RpcService.class, BindingDOMRpcServiceAdapter.BUILDER_FACTORY)
            .put(ActionService.class, ActionServiceAdapter.BUILDER_FACTORY)
            .put(ActionProviderService.class, ActionProviderServiceAdapter.BUILDER_FACTORY)
            .put(InstanceNotificationService.class, InstanceNotificationServiceAdapter.BUILDER_FACTORY)
            .put(InstanceNotificationPublishService.class,
                InstanceNotificationPublishServiceAdapter.BUILDER_FACTORY)
            .build();

    private final AdapterContext codec;

    public BindingDOMAdapterLoader(final AdapterContext codec) {
        this.codec = requireNonNull(codec);
    }

    @Override
    protected final AdapterBuilder<? extends BindingService, DOMService> createBuilder(
                final Class<? extends BindingService> key) {
        final Factory<?> factory = FACTORIES.get(key);
        checkArgument(factory != null, "Unsupported service type %s", key);
        return factory.newBuilder(codec);
    }
}
