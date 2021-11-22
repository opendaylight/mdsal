/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListenableFuture;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMAdapterBuilder.Factory;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishService;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.yangtools.yang.binding.EventInstantAware;
import org.opendaylight.yangtools.yang.binding.Notification;

@VisibleForTesting
public class BindingDOMNotificationPublishServiceAdapter extends AbstractBindingAdapter<DOMNotificationPublishService>
        implements NotificationPublishService {

    static final Factory<NotificationPublishService> BUILDER_FACTORY = Builder::new;

    public BindingDOMNotificationPublishServiceAdapter(final AdapterContext adapterContext,
            final DOMNotificationPublishService domPublishService) {
        super(adapterContext, domPublishService);
    }

    public DOMNotificationPublishService getDomPublishService() {
        return getDelegate();
    }

    @Override
    public void putNotification(final Notification<?> notification) throws InterruptedException {
        getDelegate().putNotification(toDomNotification(notification));
    }

    @Override
    public ListenableFuture<? extends Object> offerNotification(final Notification<?> notification) {
        ListenableFuture<?> offerResult = getDelegate().offerNotification(toDomNotification(notification));
        return DOMNotificationPublishService.REJECTED.equals(offerResult)
                ? NotificationPublishService.REJECTED
                : offerResult;
    }

    @Override
    public ListenableFuture<? extends Object> offerNotification(final Notification<?> notification, final int timeout,
            final TimeUnit unit) throws InterruptedException {
        ListenableFuture<?> offerResult = getDelegate().offerNotification(toDomNotification(notification), timeout,
            unit);
        return DOMNotificationPublishService.REJECTED.equals(offerResult)
                ? NotificationPublishService.REJECTED
                : offerResult;
    }

    private @NonNull DOMNotification toDomNotification(final Notification<?> notification) {
        final Instant instant = notification instanceof EventInstantAware
                ? ((EventInstantAware) notification).eventInstant() : Instant.now();
        return LazySerializedDOMNotification.create(currentSerializer(), notification, instant);
    }

    protected static class Builder extends BindingDOMAdapterBuilder<NotificationPublishService> {
        Builder(final AdapterContext adapterContext) {
            super(adapterContext);
        }

        @Override
        public Set<Class<? extends DOMService>> getRequiredDelegates() {
            return ImmutableSet.of(DOMNotificationPublishService.class);
        }

        @Override
        protected NotificationPublishService createInstance(final ClassToInstanceMap<DOMService> delegates) {
            return new BindingDOMNotificationPublishServiceAdapter(adapterContext(),
                delegates.getInstance(DOMNotificationPublishService.class));
        }
    }
}
