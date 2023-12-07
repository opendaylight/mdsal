/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableSet;
import java.util.EventListener;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

/**
 * A {@link DOMNotificationPublishService.Extension} providing a view into which {@link DOMNotification}s the extended
 * {@link DOMNotificationPublishService} is interested in receiving. This is typically driven by listener registrations
 * in the {@link DOMNotificationService} consuming published notifications.
 */
@Beta
@NonNullByDefault
public interface DOMNotificationPublishDemandExtension extends DOMNotificationPublishService.Extension {
    /**
     * Register a new {@link DemandListener}.
     *
     * @param listener the listener to register
     * @return A {@link Registration}
     * @throws NullPointerException if {@code listener} is {@code null}
     */
    Registration registerDemandListener(DemandListener listener);

    /**
     * Listener for changes in demand for {@link DOMNotification} types. Changes are communnicated via
     * {@link #onDemandUpdated(ImmutableSet)}, which reports the delta between last reported and current state.
     */
    interface DemandListener extends EventListener {
        /**
         * Update the view of what {@link DOMNotification}s are in demand.
         *
         * @param neededTypes currently-needed notification types
         */
        void onDemandUpdated(ImmutableSet<Absolute> neededTypes);
    }
}
