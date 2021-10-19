/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

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

    abstract void onNotification(@NonNull Absolute domType, @NonNull Notification notification);

    private Notification deserialize(final DOMNotification notification) {
        if (notification instanceof LazySerializedDOMNotification) {
            // TODO: This is a routed-back notification, for which we may end up losing event time here, but that is
            //       okay, for now at least.
            return ((LazySerializedDOMNotification) notification).getBindingData();
        }

        final CurrentAdapterSerializer serializer = adapterContext.currentSerializer();
        return notification instanceof DOMEvent ? serializer.fromNormalizedNodeNotification(notification.getType(),
            notification.getBody(), ((DOMEvent) notification).getEventInstant())
                : serializer.fromNormalizedNodeNotification(notification.getType(), notification.getBody());
    }
}
