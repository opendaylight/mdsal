/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

/**
 * This exception indicates that given notification can not be processed by corresponding mechanism.
 * More info can be provided in message.
 *
 * <p>
 * Expected use: {@link NotificationPublishService}
 */
public class NotificationRejectedException extends Exception {
    private static final long serialVersionUID = 1L;

    public NotificationRejectedException(final String message) {
        super(message);
    }

    public NotificationRejectedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
