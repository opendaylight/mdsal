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

@Beta
public interface NotificationPublishService {

    ListenableFuture<Object> REJECTED = Futures.immediateFailedFuture(
        new NotificationRejectedException("Rejected due to resource constraints."));

    void putNotification(Notification notification) throws InterruptedException;

    ListenableFuture<?> offerNotification(Notification notification);

    ListenableFuture<?> offerNotification(Notification notification, int timeout, TimeUnit unit)
        throws InterruptedException;
}
