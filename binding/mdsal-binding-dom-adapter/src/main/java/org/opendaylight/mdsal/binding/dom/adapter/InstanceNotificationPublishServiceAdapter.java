/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListenableFuture;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.InstanceNotificationPublishService;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMAdapterBuilder.Factory;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMInstanceNotificationPublishService;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.EventInstantAware;
import org.opendaylight.yangtools.yang.binding.InstanceNotification;

final class InstanceNotificationPublishServiceAdapter
        extends AbstractBindingAdapter<DOMInstanceNotificationPublishService>
        implements InstanceNotificationPublishService {
    static final Factory<InstanceNotificationPublishService> BUILDER_FACTORY = Builder::new;

    private InstanceNotificationPublishServiceAdapter(final AdapterContext adapterContext,
            final DOMInstanceNotificationPublishService domPublishService) {
        super(adapterContext, domPublishService);
    }

    @Override
    public <N extends InstanceNotification<N, P>, P extends DataObject> void putNotification(
            final DataTreeIdentifier<P> path, final N notification) throws InterruptedException {
        final var serializer = currentSerializer();
        getDelegate().putNotification(toDomPath(serializer, path), toDomNotification(serializer, notification));
    }

    @Override
    public <N extends InstanceNotification<N, P>, P extends DataObject>
            ListenableFuture<? extends Object> offerNotification(final DataTreeIdentifier<P> path,
                final N notification) {
        final var serializer = currentSerializer();
        return toBindingResult(getDelegate().offerNotification(toDomPath(serializer, path),
            toDomNotification(serializer, notification)));
    }

    @Override
    public <N extends InstanceNotification<N, P>, P extends DataObject>
            ListenableFuture<? extends Object> offerNotification(final DataTreeIdentifier<P> path,
                final N notification, final long timeout,  final TimeUnit unit) throws InterruptedException {
        final var serializer = currentSerializer();
        return toBindingResult(getDelegate().offerNotification(toDomPath(serializer, path),
            toDomNotification(serializer, notification), timeout, unit));
    }

    private static @NonNull ListenableFuture<? extends Object> toBindingResult(
            final @NonNull ListenableFuture<? extends Object> domResult) {
        return DOMInstanceNotificationPublishService.REJECTED.equals(domResult)
            ? InstanceNotificationPublishService.REJECTED
            : domResult;
    }

    private static @NonNull DOMDataTreeIdentifier toDomPath(final CurrentAdapterSerializer serializer,
            final DataTreeIdentifier<?> path) {
        // FIXME: implement this
        throw new UnsupportedOperationException();
    }

    private static @NonNull DOMNotification toDomNotification(final CurrentAdapterSerializer serializer,
            final InstanceNotification<?, ?> notification) {
        final Instant instant = notification instanceof EventInstantAware
                ? ((EventInstantAware) notification).eventInstant() : Instant.now();
        return LazySerializedDOMNotification.create(serializer, notification, instant);
    }

    private static final class Builder extends BindingDOMAdapterBuilder<InstanceNotificationPublishService> {
        Builder(final AdapterContext adapterContext) {
            super(adapterContext);
        }

        @Override
        public Set<Class<? extends DOMService>> getRequiredDelegates() {
            return ImmutableSet.of(DOMInstanceNotificationPublishService.class);
        }

        @Override
        protected InstanceNotificationPublishService createInstance(final ClassToInstanceMap<DOMService> delegates) {
            return new InstanceNotificationPublishServiceAdapter(adapterContext(),
                delegates.getInstance(DOMInstanceNotificationPublishService.class));
        }
    }
}
