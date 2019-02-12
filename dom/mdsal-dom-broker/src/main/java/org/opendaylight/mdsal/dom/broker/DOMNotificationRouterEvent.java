/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.lmax.disruptor.EventFactory;
import java.util.Collection;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.mdsal.dom.api.DOMNotificationListener;
import org.opendaylight.yangtools.concepts.ListenerRegistration;

/**
 * A single notification event in the disruptor ringbuffer. These objects are reused,
 * so they do have mutable state.
 */
final class DOMNotificationRouterEvent {
    static final EventFactory<DOMNotificationRouterEvent> FACTORY = DOMNotificationRouterEvent::new;

    private Collection<ListenerRegistration<? extends DOMNotificationListener>> subscribers;
    private DOMNotification notification;
    private SettableFuture<Void> future;

    private DOMNotificationRouterEvent() {
        // Hidden on purpose, initialized in initialize()
    }

    @SuppressWarnings("checkstyle:hiddenField")
    ListenableFuture<Void> initialize(final DOMNotification notification,
            final Collection<ListenerRegistration<? extends DOMNotificationListener>> subscribers) {
        this.notification = requireNonNull(notification);
        this.subscribers = requireNonNull(subscribers);
        this.future = SettableFuture.create();
        return this.future;
    }

    void deliverNotification() {
        for (ListenerRegistration<? extends DOMNotificationListener> r : subscribers) {
            final DOMNotificationListener l = r.getInstance();
            if (l != null) {
                l.onNotification(notification);
            }
        }
    }

    void setFuture() {
        future.set(null);
    }

}