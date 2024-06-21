/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.TimeUnit;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.Notification;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;

/**
 * A {@link BindingService} which allows its users to submit YANG-modeled top-level (YANG 1) {@link Notification}s for
 * delivery. There are three methods of submission, following the patters from
 * {@link java.util.concurrent.BlockingQueue}:
 * <ul>
 *   <li>{@link #putNotification(Notification)}, which may block indefinitely if the implementation cannot allocate
 *       resources to accept the notification,</li>
 *   <li>{@link #offerNotification(Notification)}, which does not block if face of resource starvation,</li>
 *   <li>{@link #offerNotification(Notification, int, TimeUnit)}, which may block for specified time if resources are
 *       thin.</li>
 * </ul>
 *
 * <p>
 * The actual delivery to listeners is asynchronous and implementation-specific. Users of this interface should not make
 * any assumptions as to whether the notification has or has not been seen.
 */
public interface NotificationPublishService extends BindingService {
    /**
     * Well-known value indicating that the binding-aware implementation is currently not able to accept a notification.
     */
    @NonNull ListenableFuture<Object> REJECTED = FluentFutures.immediateFailedFluentFuture(
            new NotificationRejectedException("Rejected due to resource constraints."));

    /**
     * Publishes a notification to subscribed listeners. This initiates the process of sending the notification, but
     * delivery to the listeners can happen asynchronously, potentially after a call to this method returns.
     *
     * <b>Note:</b> This call will block when the notification queue is full.
     *
     * @param notification the notification to publish.
     * @throws InterruptedException if interrupted while waiting
     * @throws NullPointerException if the notification is null
     */
    void putNotification(@NonNull Notification<?> notification) throws InterruptedException;

    /**
     * Publishes a notification to subscribed listeners. This initiates the process of sending the notification, but
     * delivery to the listeners can happen asynchronously, potentially after a call to this method returns.
     *
     * <p>
     * Still guaranteed not to block. Returns Listenable Future which will complete once.
     *
     * @param notification the notification to publish.
     * @return A listenable future which will report completion when the service has finished propagating the
     *         notification to its immediate registrants, or {@link #REJECTED} if resource constraints prevent
     * @throws NullPointerException if the notification is null
     */
    @NonNull ListenableFuture<? extends Object> offerNotification(@NonNull Notification<?> notification);

    /**
     * Publishes a notification to subscribed listeners. This initiates the process of sending the notification, but
     * delivery to the listeners can happen asynchronously, potentially after a call to this method returns. This method
     * is guaranteed not to block more than the specified timeout.
     *
     * @param notification the notification to publish.
     * @param timeout how long to wait before giving up, in units of unit
     * @param unit a TimeUnit determining how to interpret the timeout parameter
     * @return A listenable future which will report completion when the service has finished propagating the
     *         notification to its immediate registrants, or {@link #REJECTED} if resource constraints prevent
     * @throws InterruptedException if interrupted while waiting
     * @throws NullPointerException if the notification or unit is null
     * @throws IllegalArgumentException if timeout is negative.
     */
    @NonNull ListenableFuture<? extends Object> offerNotification(@NonNull Notification<?> notification,
            int timeout, @NonNull TimeUnit unit) throws InterruptedException;
}
