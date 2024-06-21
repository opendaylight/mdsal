/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.TimeUnit;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.InstanceNotification;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * A {@link BindingService} which allows its users to submit YANG-modeled top-level (YANG 1.1)
 * {@link InstanceNotification}s for delivery.
 */
@Beta
public interface InstanceNotificationPublishService extends BindingService {
    /**
     * Create a new {@link Publisher} for a {@link InstanceNotificationSpec}. Returned interface may be freely reused
     * and accessed concurrently from multiple threads.
     *
     * @param <N> Generated InstanceNotification interface type
     * @param <P> Notification parent type
     * @param notificationSpec Specification of an {@link InstanceNotification}
     * @return A {@link Publisher} handle
     * @throws NullPointerException if {@code notificationSpec} is {@code null}
     * @throws IllegalArgumentException when {@code notificationSpec} cannot be resolved
     */
    <N extends InstanceNotification<N, P>, P extends DataObject> @NonNull Publisher<N, P> newPublisher(
        InstanceNotificationSpec<N, P> notificationSpec);

    /**
     * Interface for publishing {@link InstanceNotification} of a particular type bound to an instantiation. There are
     * three methods of submission, following the patters from {@link java.util.concurrent.BlockingQueue}:
     * <ul>
     *   <li>{@link #putNotification(InstanceIdentifier, InstanceNotification)}, which may block indefinitely if the
     *       implementation cannot allocate resources to accept the notification,</li>
     *   <li>{@link #offerNotification(InstanceIdentifier, InstanceNotification)}, which does not block if face of
     *       resource starvation,</li>
     *   <li>{@link #offerNotification(InstanceIdentifier, InstanceNotification, long, TimeUnit)}, which may block for
     *       specified time if resources are thin.</li>
     * </ul>
     *
     * <p>
     * The actual delivery to listeners is asynchronous and implementation-specific. Users of this interface should not
     * make any assumptions as to whether the notification has or has not been seen.
     *
     * @param <N> Generated InstanceNotification interface type
     * @param <P> Notification parent type
     */
    @Beta
    interface Publisher<N extends InstanceNotification<N, P>, P extends DataObject> {
        /**
         * Well-known value indicating that the binding-aware implementation is currently not able to accept a
         * notification.
         */
        @NonNull ListenableFuture<Object> REJECTED = FluentFutures.immediateFailedFluentFuture(
                new NotificationRejectedException("Rejected due to resource constraints."));

        /**
         * Publishes a notification to subscribed listeners. This initiates the process of sending the notification, but
         * delivery to the listeners can happen asynchronously, potentially after a call to this method returns.
         *
         * <b>Note:</b> This call will block when the notification queue is full.
         *
         * @param path parent path
         * @param notification the notification to publish.
         * @throws InterruptedException if interrupted while waiting
         * @throws NullPointerException if any argument is null
         */
        void putNotification(@NonNull InstanceIdentifier<P> path, @NonNull N notification) throws InterruptedException;

        /**
         * Publishes a notification to subscribed listeners. This initiates the process of sending the notification, but
         * delivery to the listeners can happen asynchronously, potentially after a call to this method returns.
         *
         * <p>
         * Still guaranteed not to block. Returns Listenable Future which will complete once the delivery is completed.
         *
         * @param path parent path
         * @param notification the notification to publish.
         * @return A listenable future which will report completion when the service has finished propagating the
         *         notification to its immediate registrants, or {@link #REJECTED} if resource constraints prevent
         * @throws NullPointerException if any argument is null
         */
        @NonNull ListenableFuture<? extends Object> offerNotification(@NonNull InstanceIdentifier<P> path,
            @NonNull N notification);

        /**
         * Publishes a notification to subscribed listeners. This initiates the process of sending the notification, but
         * delivery to the listeners can happen asynchronously, potentially after a call to this method returns. This
         * method is guaranteed not to block more than the specified timeout.
         *
         * @param path parent path
         * @param notification the notification to publish.
         * @param timeout how long to wait before giving up, in units of unit
         * @param unit a TimeUnit determining how to interpret the timeout parameter
         * @return A listenable future which will report completion when the service has finished propagating the
         *         notification to its immediate registrants, or {@link #REJECTED} if resource constraints prevent
         * @throws InterruptedException if interrupted while waiting
         * @throws NullPointerException if any argument is null
         * @throws IllegalArgumentException if timeout is negative.
         */
        @NonNull ListenableFuture<? extends Object> offerNotification(@NonNull InstanceIdentifier<P> path,
            @NonNull N notification, long timeout, @NonNull TimeUnit unit) throws InterruptedException;
    }
}
