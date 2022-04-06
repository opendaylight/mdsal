/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2021 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMEvent;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.mdsal.dom.api.DOMNotificationListener;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

abstract class AbstractDOMNotificationListenerAdapter implements DOMNotificationListener {
    private final AdapterContext adapterContext;

    AbstractDOMNotificationListenerAdapter(final AdapterContext adapterContext) {
        this.adapterContext = requireNonNull(adapterContext);
    }

    @Override
    public final void onNotification(final DOMNotification notification) {
        onNotification(notification.getType(), verifyNotNull(deserialize(notification)));
    }

    abstract void onNotification(@NonNull Absolute domType, @NonNull Notification<?> notification);

    abstract Set<Absolute> getSupportedNotifications();

    private Notification<?> deserialize(final DOMNotification notification) {
        if (notification instanceof LazySerializedNotification) {
            // TODO: This is a routed-back notification, for which we may end up losing event time here, but that is
            //       okay, for now at least.
            return ((LazySerializedNotification) notification).getBindingData();
        }

        final var serializer = adapterContext.currentSerializer();
        final var result = notification instanceof DOMEvent
            ? serializer.fromNormalizedNodeNotification(notification.getType(), notification.getBody(),
                ((DOMEvent) notification).getEventInstant())
                : serializer.fromNormalizedNodeNotification(notification.getType(), notification.getBody());
        verify(result instanceof Notification, "Unexpected codec result %s", result);
        return (Notification<?>) result;
    }
}
