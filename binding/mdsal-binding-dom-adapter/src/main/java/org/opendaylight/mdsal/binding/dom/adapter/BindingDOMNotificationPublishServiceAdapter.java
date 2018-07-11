/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMAdapterBuilder.Factory;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishService;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.yangtools.yang.binding.Notification;

public class BindingDOMNotificationPublishServiceAdapter extends AbstractBindingAdapter<DOMNotificationPublishService>
        implements NotificationPublishService {

    static final Factory<NotificationPublishService> BUILDER_FACTORY = Builder::new;

    public BindingDOMNotificationPublishServiceAdapter(final DOMNotificationPublishService domPublishService,
            final BindingToNormalizedNodeCodec codec) {
        super(codec, domPublishService);
    }

    @Deprecated
    public BindingDOMNotificationPublishServiceAdapter(final BindingToNormalizedNodeCodec codec,
            final DOMNotificationPublishService domPublishService) {
        this(domPublishService, codec);
    }

    public BindingToNormalizedNodeCodec getCodecRegistry() {
        return getCodec();
    }

    public DOMNotificationPublishService getDomPublishService() {
        return getDelegate();
    }

    @Override
    public void putNotification(final Notification notification) throws InterruptedException {
        getDelegate().putNotification(toDomNotification(notification));
    }

    @Override
    public ListenableFuture<? extends Object> offerNotification(final Notification notification) {
        ListenableFuture<?> offerResult = getDelegate().offerNotification(toDomNotification(notification));
        return DOMNotificationPublishService.REJECTED.equals(offerResult)
                ? NotificationPublishService.REJECTED
                : offerResult;
    }

    @Override
    public ListenableFuture<? extends Object> offerNotification(final Notification notification,
                                                 final int timeout, final TimeUnit unit) throws InterruptedException {
        ListenableFuture<?> offerResult = getDelegate().offerNotification(toDomNotification(notification), timeout,
            unit);
        return DOMNotificationPublishService.REJECTED.equals(offerResult)
                ? NotificationPublishService.REJECTED
                : offerResult;
    }

    private DOMNotification toDomNotification(final Notification notification) {
        return LazySerializedDOMNotification.create(getCodec(), notification);
    }

    protected static class Builder extends BindingDOMAdapterBuilder<NotificationPublishService> {

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
