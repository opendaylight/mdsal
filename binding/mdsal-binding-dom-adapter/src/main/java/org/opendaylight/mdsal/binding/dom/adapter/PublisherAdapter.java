/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.TimeUnit;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.InstanceNotificationPublishService.Publisher;
import org.opendaylight.mdsal.binding.api.InstanceNotificationSpec;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMInstanceNotificationPublishService;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.InstanceNotification;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

/**
 * An implementation of {@link Publisher} backed by a {@link DOMInstanceNotificationPublishService}.
 */
final class PublisherAdapter<N extends InstanceNotification<N, P>, P extends DataObject>
        extends AbstractBindingAdapter<DOMInstanceNotificationPublishService> implements Publisher<N, P> {
    private final @NonNull Absolute notificationPath;

    PublisherAdapter(final AdapterContext adapterContext, final DOMInstanceNotificationPublishService domPublishService,
            final InstanceNotificationSpec<?, ?> spec) {
        super(adapterContext, domPublishService);
        notificationPath = currentSerializer().getNotificationPath(spec);
    }

    @Override
    public void putNotification(final DataObjectIdentifier<P> path, final N notification) throws InterruptedException {
        final var serializer = currentSerializer();
        getDelegate().putNotification(toDomPath(serializer, path), toDomNotification(serializer, notification));
    }

    @Override
    public ListenableFuture<? extends Object> offerNotification(final DataObjectIdentifier<P> path,
            final N notification) {
        final var serializer = currentSerializer();
        return toBindingResult(getDelegate().offerNotification(toDomPath(serializer, path),
            toDomNotification(serializer, notification)));
    }

    @Override
    public ListenableFuture<? extends Object> offerNotification(final DataObjectIdentifier<P> path,
            final N notification, final long timeout, final TimeUnit unit) throws InterruptedException {
        final var serializer = currentSerializer();
        return toBindingResult(getDelegate().offerNotification(toDomPath(serializer, path),
            toDomNotification(serializer, notification), timeout, unit));
    }

    private static @NonNull ListenableFuture<? extends Object> toBindingResult(
            final @NonNull ListenableFuture<? extends Object> domResult) {
        return DOMInstanceNotificationPublishService.REJECTED.equals(domResult) ? REJECTED : domResult;
    }

    private static @NonNull DOMDataTreeIdentifier toDomPath(final CurrentAdapterSerializer serializer,
            final DataObjectIdentifier<?> path) {
        return DOMDataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL, serializer.toYangInstanceIdentifier(path));
    }

    private @NonNull DOMNotification toDomNotification(final CurrentAdapterSerializer serializer,
            final InstanceNotification<?, ?> notification) {
        return new LazySerializedInstanceNotification(serializer, notificationPath, notification);
    }
}
