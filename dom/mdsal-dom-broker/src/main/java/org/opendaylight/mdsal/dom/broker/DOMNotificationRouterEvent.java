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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.mdsal.dom.api.DOMNotificationListener;
import org.opendaylight.yangtools.yang.common.Empty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A single notification event in the notification router.
 */
final class DOMNotificationRouterEvent {
    private static final Logger LOG = LoggerFactory.getLogger(DOMNotificationRouterEvent.class);

    private final SettableFuture<Empty> future = SettableFuture.create();
    private final @NonNull DOMNotification notification;

    DOMNotificationRouterEvent(final DOMNotification notification) {
        this.notification = requireNonNull(notification);
    }

    ListenableFuture<Empty> future() {
        return future;
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    void deliverTo(final DOMNotificationListener listener) {
        try {
            listener.onNotification(notification);
        } catch (Exception e) {
            LOG.warn("Listener {} failed during notification delivery", listener, e);
        } finally {
            clear();
        }
    }

    void clear() {
        future.set(Empty.value());
    }
}
