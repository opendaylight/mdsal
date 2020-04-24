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
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.mdsal.dom.api.DOMNotificationListener;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A single notification event in the notification router.
 */
final class DOMNotificationRouterEvent {
    private static final Logger LOG = LoggerFactory.getLogger(DOMNotificationRouterEvent.class);

    static final EventFactory<DOMNotificationRouterEvent> FACTORY = DOMNotificationRouterEvent::new;

    private ListenerRegistration<? extends DOMNotificationListener> subscriber;
    private DOMNotification notification;
    private SettableFuture<Void> future;

    private DOMNotificationRouterEvent() {
        // Hidden on purpose, initialized in initialize()
    }

    @SuppressWarnings("checkstyle:hiddenField")
    ListenableFuture<Void> initialize(final DOMNotification notification,
            final ListenerRegistration<? extends DOMNotificationListener> subscriber) {
        this.notification = requireNonNull(notification);
        this.subscriber = requireNonNull(subscriber);
        this.future = SettableFuture.create();
        return this.future;
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    void deliverNotification() {
        final DOMNotificationListener listener = subscriber.getInstance();
        if (listener != null) {
            listener.onNotification(notification);
        }
        setFuture();
    }

    private void setFuture() {
        future.set(null);
    }
}
