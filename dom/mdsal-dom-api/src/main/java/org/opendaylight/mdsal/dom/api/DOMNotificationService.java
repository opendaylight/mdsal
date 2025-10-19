/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

/**
 * A {@link DOMService} which allows its users to subscribe to receive top-level (YANG 1.0) {@link DOMNotification}s.
 */
public interface DOMNotificationService extends DOMService<DOMNotificationService, DOMNotificationService.Extension> {
    /**
     * Marker interface for an extension to {@link DOMNotificationService}.
     */
    interface Extension extends DOMService.Extension<DOMNotificationService, Extension> {
        // Marker interface
    }

    /**
     * Register a {@link DOMNotificationListener} to receive a set of notifications. As with other
     * {@link Registration}-based interfaces, registering an instance multiple times results in
     * notifications being delivered for each registration.
     *
     * @param listener Notification instance to register
     * @param types Notification types which should be delivered to the listener. Duplicate entries are processed only
     *        once, null entries are ignored.
     * @return Registration handle. Invoking {@link Registration#close()} will stop the delivery of notifications to the
     *         listener
     * @throws IllegalArgumentException if types is empty or contains an invalid element, such as {@code null} or a
     *         schema node identifier which does not represent a valid {@link DOMNotification} type.
     * @throws NullPointerException if either of the arguments is {@code null}
     */
    @NonNull Registration registerNotificationListener(@NonNull DOMNotificationListener listener,
        @NonNull Collection<Absolute> types);

    /**
     * Register a {@link DOMNotificationListener} to receive a set of notifications. As with other
     * {@link Registration}-based interfaces, registering an instance multiple times results in
     * notifications being delivered for each registration.
     *
     * @param listener Notification instance to register
     * @param types Notification types which should be delivered to the listener. Duplicate entries are processed only
     *        once, null entries are ignored.
     * @return Registration handle. Invoking {@link Registration#close()} will stop the delivery of notifications to the
     *         listener
     * @throws IllegalArgumentException if types is empty or contains an invalid element, such as {@code null} or a
     *         schema node identifier which does not represent a valid {@link DOMNotification} type.
     * @throws NullPointerException if listener is {@code null}
     */
    default @NonNull Registration registerNotificationListener(final @NonNull DOMNotificationListener listener,
            final Absolute... types) {
        return registerNotificationListener(listener, List.of(types));
    }

    /**
     * Register a number of {@link DOMNotificationListener}s to receive some notification notifications. As with other
     * {@link Registration}-based interfaces, registering an instance multiple times results in
     * notifications being delivered for each registration.
     *
     * @param typeToListener Specification of which types to listen to with which listeners
     * @throws NullPointerException if {@code typeToListener} is {@code null}
     */
    @NonNull Registration registerNotificationListeners(@NonNull Map<Absolute, DOMNotificationListener> typeToListener);
}
