/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.TimeUnit;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;

/**
 * A {@link DOMService} which allows its user to publish top-level (YANG 1.0) {@link DOMNotification}s. It provides two
 * styles of initiating the notification delivery, similar to {@link java.util.concurrent.BlockingQueue}:
 * <ul>
 *   <li>a put-style method which waits until the implementation can accept the notification for delivery, and</li>
 *   <li>an offer-style method, which attempts to enqueue the notification, but allows the caller to specify that it
 *       should never wait, or put an upper bound on how long it is going to wait</li>
 * </ul>
 */
public interface DOMNotificationPublishService
        extends DOMService<DOMNotificationPublishService, DOMNotificationPublishService.Extension> {
    /**
     * Marker interface for an extension to {@link DOMNotificationPublishService}.
     */
    interface Extension extends DOMService.Extension<DOMNotificationPublishService, Extension> {
        // Marker interface
    }

    /**
     * Well-known value indicating that the implementation is currently not able to accept a notification.
     */
    @NonNull ListenableFuture<?> REJECTED = FluentFutures.immediateFailedFluentFuture(
        new DOMNotificationRejectedException("Unacceptable blocking conditions encountered"));

    /**
     * Publish a notification. The result of this method is a {@link ListenableFuture} which will
     * complete once the notification has been delivered to all immediate registrants. The type of
     * the object resulting from the future is not defined and implementations may use it to convey
     * additional information related to the publishing process.
     * Abstract subclasses can refine the return type as returning a promise of a more specific
     * type, e.g.:
     * public interface DeliveryStatus { int getListenerCount(); } ListenableFuture&lt;? extends
     * DeliveryStatus&gt;[ putNotification(DOMNotification notification);
     * Once the Future succeeds, the resulting object can be queried for traits using instanceof,
     * e.g:
     * // Can block when (for example) the implemention's ThreadPool queue is full Object o =
     * service.putNotification(notif).get(); if (o instanceof DeliveryStatus) { DeliveryStatus ds =
     * (DeliveryStatus)o; LOG.debug("Notification was received by {} listeners",
     * ds.getListenerCount();); } }
     * In case an implementation is running out of resources, it can block the calling thread until
     * enough resources become available to accept the notification for processing, or it is
     * interrupted.
     *
     * <p>Caution: completion here means that the implementation has completed processing of the
     * notification. This does not mean that all existing registrants have seen the notification.
     * Most importantly, the delivery process at other cluster nodes may have not begun yet.
     *
     * @param notification Notification to be published.
     * @return A listenable future which will report completion when the service has finished
     *         propagating the notification to its immediate registrants.
     * @throws InterruptedException if interrupted while waiting
     * @throws NullPointerException if notification is null.
     */
    @NonNull ListenableFuture<? extends Object> putNotification(@NonNull DOMNotification notification)
            throws InterruptedException;

    /**
     * Attempt to publish a notification. The result of this method is a {@link ListenableFuture}
     * which will complete once the notification has been delivered to all immediate registrants.
     * The type of the object resulting from the future is not defined and implementations may use
     * it to convey additional information related to the publishing process. Unlike
     * {@link #putNotification(DOMNotification)}, this method is guaranteed not to block if the
     * underlying implementation encounters contention.
     *
     * @param notification Notification to be published.
     * @return A listenable future which will report completion when the service has finished
     *         propagating the notification to its immediate registrants, or {@link #REJECTED} if
     *         resource constraints prevent the implementation from accepting the notification for
     *         delivery.
     * @throws NullPointerException if notification is null.
     */
    @NonNull ListenableFuture<? extends Object> offerNotification(@NonNull DOMNotification notification);

    /**
     * Attempt to publish a notification. The result of this method is a {@link ListenableFuture}
     * which will complete once the notification has been delivered to all immediate registrants.
     * The type of the object resulting from the future is not defined and implementations may use
     * it to convey additional information related to the publishing process. Unlike
     * {@link #putNotification(DOMNotification)}, this method is guaranteed to block more than the
     * specified timeout.
     *
     * @param notification Notification to be published.
     * @param timeout how long to wait before giving up, in units of unit
     * @param unit a TimeUnit determining how to interpret the timeout parameter
     * @return A listenable future which will report completion when the service has finished
     *         propagating the notification to its immediate registrants, or {@link #REJECTED} if
     *         resource constraints prevent the implementation from accepting the notification for
     *         delivery.
     * @throws InterruptedException if interrupted while waiting
     * @throws NullPointerException if notification or unit is null.
     * @throws IllegalArgumentException if timeout is negative.
     */
    @NonNull ListenableFuture<? extends Object> offerNotification(@NonNull DOMNotification notification,
            long timeout, @NonNull TimeUnit unit) throws InterruptedException;
}
