/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.EventListener;
import java.util.concurrent.Executor;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceNotification;
import org.opendaylight.yangtools.yang.binding.KeyedListNotification;

/**
 * A {@link BindingService} which allows clients to subscribe to {@link InstanceNotification}s and
 * {@link KeyedListNotification}s.
 */
@Beta
public interface InstanceNotificationService extends BindingService {

    <P extends DataObject, N extends InstanceNotification<N, P>, T extends Listener<P, N>>
        @NonNull Registration registerListener(InstanceNotificationSpec<N, P> spec, DataTreeIdentifier<P> path,
            T listener, Executor executor);

    default <P extends DataObject, N extends InstanceNotification<N, P>, T extends Listener<P, N>>
        @NonNull Registration registerListener(final InstanceNotificationSpec<N, P> spec,
            final DataTreeIdentifier<P> path, final T listener) {
        return registerListener(spec, path, listener, MoreExecutors.directExecutor());
    }

    <P extends DataObject & Identifiable<K>, N extends KeyedListNotification<N, P, K>, K extends Identifier<P>,
        T extends KeyedListListener<P, N, K>> @NonNull Registration registerListener(
            InstanceNotificationSpec<N, P> spec, DataTreeIdentifier<P> path, T listener, Executor executor);

    default <P extends DataObject & Identifiable<K>, N extends KeyedListNotification<N, P, K>, K extends Identifier<P>,
        T extends KeyedListListener<P, N, K>> @NonNull Registration registerListener(
            final InstanceNotificationSpec<N, P> spec, final DataTreeIdentifier<P> path, final T listener) {
        return registerListener(spec, path, listener, MoreExecutors.directExecutor());
    }

    /*
     * Interface for listeners on instance (YANG 1.1) notifications.
     */
    @FunctionalInterface
    interface Listener<P extends DataObject, N extends InstanceNotification<N, P>> extends EventListener {
        /**
         * Process an instance notification.
         *
         * @param path Instance path
         * @param notification Notification body
         */
        void onNotification(@NonNull DataTreeIdentifier<P> path, @NonNull N notification);
    }

    /**
     * Interface for listeners on instance (YANG 1.1) notifications defined in a {@code list} with a {@code key}.
     */
    @FunctionalInterface
    interface KeyedListListener<P extends DataObject & Identifiable<K>, N extends KeyedListNotification<N, P, K>,
            K extends Identifier<P>> extends EventListener {
        /**
         * Process an instance notification.
         *
         * @param path Instance path
         * @param notification Notification body
         */
        // FIXME: DataTreeIdentifier does not have a Keyed flavor
        void onNotification(@NonNull DataTreeIdentifier<P> path, @NonNull N notification);
    }
}
