/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.Executor;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * A {@link DOMService} providing access to subscription to YANG 1.1 instance notifications.
 */
@Beta
@NonNullByDefault
public interface DOMInstanceNotificationService
        extends DOMService<DOMInstanceNotificationService, DOMInstanceNotificationService.Extension> {
    /**
     * Marker interface for an extension to {@link DOMInstanceNotificationService}.
     */
    interface Extension extends DOMService.Extension<DOMInstanceNotificationService, Extension> {
        // Marker interface
    }

    /**
     * Register a {@link DOMInstanceNotificationListener} for a particular {@code path} and notification {@code type}.
     *
     * @param path Instance notification's parent path, must not be empty, but may be a wildcard identifier.
     * @param type Notification type, i.e. its schema node QName
     * @param listener Listener to deliver notifications to
     * @param executor Executor to use for invoking
     * @return Listener's registration object
     * @throws NullPointerException if any of the arguments is null
     * @throws IllegalArgumentException if the {@code path} is empty or if the combination of {@code path} and
     *                                  {@code type} does not identify an instance notification
     */
    Registration registerNotificationListener(DOMDataTreeIdentifier path, QName type,
        DOMInstanceNotificationListener listener, Executor executor);

    /**
     * Register a {@link DOMInstanceNotificationListener} for a particular {@code path} and notification {@code type}.
     * This method is a convenience equivalent to
     * {@code registerNotificationListener(path, type, listener, MoreExecutors.directExecutor())}.
     *
     * @param path Instance notification's parent path, must not be empty, but may be a wildcard identifier.
     * @param type Notification type, i.e. its schema node QName
     * @param listener Listener to deliver notifications to
     * @return Listener's registration object
     * @throws NullPointerException if any of the arguments is null
     * @throws IllegalArgumentException if the {@code path} is empty or if the combination of {@code path} and
     *                                  {@code type} does not identify an instance notification
     */
    default Registration registerNotificationListener(final DOMDataTreeIdentifier path, final QName type,
            final DOMInstanceNotificationListener listener) {
        return registerNotificationListener(path, type, listener, MoreExecutors.directExecutor());
    }
}
