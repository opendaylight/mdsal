/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.api;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.TimeUnit;
import org.opendaylight.mdsal.binding.javav2.spec.base.Notification;

/**
 * A {@link NotificationService} which also allows its users to submit YANG-modeled notifications for delivery.
 *
 * <p>
 * There are three methods of submission, following the patters from {@link java.util.concurrent.BlockingQueue}:
 *
 * <p>
 * - {@link #putNotification(org.opendaylight.mdsal.binding.javav2.spec.base.Notification)}, which may block
 * indefinitely if the implementation cannot allocate resources to accept the notification,
 * - {@link #offerNotification(org.opendaylight.mdsal.binding.javav2.spec.base.Notification)}, which does not block if
 * face of resource starvation,
 * - {@link #offerNotification(org.opendaylight.mdsal.binding.javav2.spec.base.Notification, int, TimeUnit)}, which may
 * block for specified time if resources are thin.
 *
 *<p>
 * The actual delivery to listeners is asynchronous and implementation-specific.
 * Users of this interface should not make any assumptions as to whether the
 * notification has or has not been seen.
 */

@Beta
public interface NotificationPublishService extends BindingService {

    ListenableFuture<Object> REJECTED = Futures.immediateFailedFuture(
        new NotificationRejectedException("Rejected due to resource constraints."));

    void putNotification(Notification<?> notification) throws InterruptedException;

    ListenableFuture<?> offerNotification(Notification<?> notification);

    ListenableFuture<?> offerNotification(Notification<?> notification, int timeout, TimeUnit unit)
        throws InterruptedException;

}
