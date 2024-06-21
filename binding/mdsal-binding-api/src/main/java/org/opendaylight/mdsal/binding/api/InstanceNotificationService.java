/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.Executor;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.InstanceIdentifier;
import org.opendaylight.yangtools.binding.InstanceNotification;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.KeyAware;
import org.opendaylight.yangtools.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.binding.KeyedListNotification;
import org.opendaylight.yangtools.concepts.Registration;

/**
 * A {@link BindingService} which allows clients to subscribe to (YANG 1.1) {@link InstanceNotification}s and
 * {@link KeyedListNotification}s.
 */
@Beta
public interface InstanceNotificationService extends BindingService {

    <P extends DataObject, N extends InstanceNotification<N, P>> @NonNull Registration registerListener(
        InstanceNotificationSpec<N, P> spec, InstanceIdentifier<P> path, Listener<P, N> listener, Executor executor);

    default <P extends DataObject, N extends InstanceNotification<N, P>> @NonNull Registration registerListener(
            final InstanceNotificationSpec<N, P> spec, final InstanceIdentifier<P> path,
            final Listener<P, N> listener) {
        return registerListener(spec, path, listener, MoreExecutors.directExecutor());
    }

    <P extends DataObject & KeyAware<K>, N extends KeyedListNotification<N, P, K>, K extends Key<P>>
        @NonNull Registration registerListener(InstanceNotificationSpec<N, P> spec, KeyedInstanceIdentifier<P, K> path,
            KeyedListListener<P, N, K> listener, Executor executor);

    default <P extends DataObject & KeyAware<K>, N extends KeyedListNotification<N, P, K>, K extends Key<P>>
            @NonNull Registration registerListener(final InstanceNotificationSpec<N, P> spec,
                final KeyedInstanceIdentifier<P, K> path, final KeyedListListener<P, N, K> listener) {
        return registerListener(spec, path, listener, MoreExecutors.directExecutor());
    }

    /*
     * Interface for listeners on instance (YANG 1.1) notifications.
     */
    @FunctionalInterface
    interface Listener<P extends DataObject, N extends InstanceNotification<N, P>> {
        /**
         * Process an instance notification.
         *
         * @param path Instance path
         * @param notification Notification body
         */
        void onNotification(@NonNull InstanceIdentifier<P> path, @NonNull N notification);
    }

    /**
     * Interface for listeners on instance (YANG 1.1) notifications defined in a {@code list} with a {@code key}.
     */
    @FunctionalInterface
    interface KeyedListListener<P extends DataObject & KeyAware<K>, N extends KeyedListNotification<N, P, K>,
            K extends Key<P>> {
        /**
         * Process an instance notification.
         *
         * @param path Instance path
         * @param notification Notification body
         */
        void onNotification(@NonNull KeyedInstanceIdentifier<P, K> path, @NonNull N notification);
    }
}
