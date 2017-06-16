/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.notification;

import com.google.common.annotations.Beta;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.opendaylight.mdsal.binding.javav2.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.spi.builder.BindingDOMAdapterBuilder;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.spi.builder.BindingDOMAdapterBuilder.Factory;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.serialized.LazySerializedDOMNotification;
import org.opendaylight.mdsal.binding.javav2.spec.base.Notification;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishService;
import org.opendaylight.mdsal.dom.api.DOMService;

/**
 * Adapter for notification publisher service.
 */
@Beta
public class BindingDOMNotificationPublishServiceAdapter implements NotificationPublishService, AutoCloseable {

    public static final Factory<NotificationPublishService> BUILDER_FACTORY = Builder::new;

    private final BindingToNormalizedNodeCodec codecRegistry;
    private final DOMNotificationPublishService domPublishService;

    public BindingDOMNotificationPublishServiceAdapter(final BindingToNormalizedNodeCodec codec,
            final DOMNotificationPublishService domPublishService) {
        this.codecRegistry = codec;
        this.domPublishService = domPublishService;
    }

    public BindingToNormalizedNodeCodec getCodecRegistry() {
        return codecRegistry;
    }

    public DOMNotificationPublishService getDomPublishService() {
        return domPublishService;
    }

    @Override
    public void putNotification(final Notification<?> notification) throws InterruptedException {
        domPublishService.putNotification(toDomNotification(notification));
    }

    @Override
    public ListenableFuture<? extends Object> offerNotification(final Notification<?> notification) {
        final ListenableFuture<?> offerResult = domPublishService.offerNotification(toDomNotification(notification));
        return DOMNotificationPublishService.REJECTED.equals(offerResult) ? NotificationPublishService.REJECTED
                : offerResult;
    }

    @Override
    public ListenableFuture<? extends Object> offerNotification(final Notification<?> notification, final int timeout,
            final TimeUnit unit) throws InterruptedException {
        final ListenableFuture<?> offerResult =
                domPublishService.offerNotification(toDomNotification(notification), timeout, unit);
        return DOMNotificationPublishService.REJECTED.equals(offerResult) ? NotificationPublishService.REJECTED
                : offerResult;
    }

    private DOMNotification toDomNotification(final Notification<?> notification) {
        return LazySerializedDOMNotification.create(codecRegistry, notification);
    }

    @Override
    public void close() throws Exception {
        // NOOP
    }

    private static class Builder extends BindingDOMAdapterBuilder<NotificationPublishService> {

        @Override
        public Set<Class<? extends DOMService>> getRequiredDelegates() {
            return ImmutableSet.of(DOMNotificationPublishService.class);
        }

        @Override
        protected NotificationPublishService createInstance(final BindingToNormalizedNodeCodec codec,
                final ClassToInstanceMap<DOMService> delegates) {
            final DOMNotificationPublishService domPublish = delegates.getInstance(DOMNotificationPublishService.class);
            return new BindingDOMNotificationPublishServiceAdapter(codec, domPublish);
        }
    }
}
