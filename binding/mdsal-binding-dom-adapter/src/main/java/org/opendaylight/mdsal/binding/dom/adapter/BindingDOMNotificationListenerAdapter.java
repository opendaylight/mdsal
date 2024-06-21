/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import java.util.concurrent.Executor;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.NotificationService.CompositeListener.Component;
import org.opendaylight.mdsal.binding.api.NotificationService.Listener;
import org.opendaylight.mdsal.dom.api.DOMEvent;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.mdsal.dom.api.DOMNotificationListener;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.Notification;
import org.opendaylight.yangtools.binding.reflect.BindingReflections;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

final class BindingDOMNotificationListenerAdapter<N extends Notification<N> & DataObject>
        implements DOMNotificationListener {
    private final AdapterContext adapterContext;
    private final Listener<N> delegate;
    private final Executor executor;
    private final Class<N> type;

    BindingDOMNotificationListenerAdapter(final AdapterContext adapterContext, final Class<N> type,
            final Listener<N> delegate, final Executor executor) {
        this.adapterContext = requireNonNull(adapterContext);
        this.type = requireNonNull(type);
        this.delegate = requireNonNull(delegate);
        this.executor = requireNonNull(executor);
    }

    BindingDOMNotificationListenerAdapter(final AdapterContext adapterContext, final Component<N> component,
            final Executor executor) {
        this(adapterContext, component.type(), component.listener(), executor);
    }

    @NonNull Absolute schemaPath() {
        return Absolute.of(BindingReflections.findQName(type));
    }

    @Override
    public void onNotification(final DOMNotification notification) {
        final var binding = type.cast(verifyNotNull(deserialize(notification)));
        executor.execute(() -> delegate.onNotification(binding));
    }

    private Notification<?> deserialize(final DOMNotification notification) {
        if (notification instanceof LazySerializedNotification lazy) {
            // TODO: This is a routed-back notification, for which we may end up losing event time here, but that is
            //       okay, for now at least.
            return lazy.getBindingData();
        }

        final var serializer = adapterContext.currentSerializer();
        final var result = notification instanceof DOMEvent domEvent
            ? serializer.fromNormalizedNodeNotification(notification.getType(), notification.getBody(),
                domEvent.getEventInstant())
                : serializer.fromNormalizedNodeNotification(notification.getType(), notification.getBody());
        verify(result instanceof Notification, "Unexpected codec result %s", result);
        return (Notification<?>) result;
    }
}
