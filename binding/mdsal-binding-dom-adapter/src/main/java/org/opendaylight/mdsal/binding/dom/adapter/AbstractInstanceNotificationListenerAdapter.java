/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.Executor;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMEvent;
import org.opendaylight.mdsal.dom.api.DOMInstanceNotificationListener;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.InstanceNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractInstanceNotificationListenerAdapter<P extends DataObject, N extends InstanceNotification<N, P>,
        L> implements DOMInstanceNotificationListener {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractInstanceNotificationListenerAdapter.class);

    private final @NonNull AdapterContext adapterContext;
    private final @NonNull Class<N> notificationClass;
    private final @NonNull Executor executor;
    private final @NonNull L delegate;

    AbstractInstanceNotificationListenerAdapter(final AdapterContext adapterContext, final Class<N> nofiticationClass,
            final L delegate, final Executor executor) {
        this.adapterContext = requireNonNull(adapterContext);
        notificationClass = requireNonNull(nofiticationClass);
        this.delegate = requireNonNull(delegate);
        this.executor = requireNonNull(executor);
    }

    @Override
    public final void onNotification(final DOMDataTreeIdentifier path, final DOMNotification notification) {
        final var serializer = adapterContext.currentSerializer();
        final var bindingNotification = notification instanceof DOMEvent
            ? serializer.fromNormalizedNodeNotification(notification.getType(), notification.getBody(),
                ((DOMEvent) notification).getEventInstant())
                : serializer.fromNormalizedNodeNotification(notification.getType(), notification.getBody());

        final N castNotification;
        try {
            castNotification = notificationClass.cast(bindingNotification);
        } catch (ClassCastException e) {
            LOG.warn("Mismatched notification type {}, not notifying listener", notification.getType(), e);
            return;
        }

        final var bindingPath = serializer.fromYangInstanceIdentifier(path.path()).toIdentifier();
        executor.execute(() -> onNotification(delegate, bindingPath, castNotification));
    }

    abstract void onNotification(@NonNull L delegate, @NonNull DataObjectIdentifier<?> path, @NonNull N notification);
}
