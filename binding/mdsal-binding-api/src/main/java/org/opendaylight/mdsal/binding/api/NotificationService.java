/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Set;
import java.util.concurrent.Executor;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.Notification;
import org.opendaylight.yangtools.concepts.Registration;

/**
 * Notification broker which allows clients to subscribe for and publish YANG 1.0 notifications, which is to say
 * {@code notification} statements occurring directly within a {@code module} or a {@code submodule} statement.
 */
public interface NotificationService extends BindingService {
    /**
     * Registers a {@link Listener} to receive callbacks for {@link Notification}s of a particular type.
     *
     * @param <N> Notification type
     * @param type Notification type class
     * @param listener The listener implementation that will receive notifications
     * @param executor Executor to use for invoking the listener's methods
     * @return a {@link Registration} instance that should be used to unregister the listener by invoking the
     *        {@link Registration#close()} method when no longer needed
     */
    <N extends Notification<N> & DataObject> @NonNull Registration registerListener(Class<N> type, Listener<N> listener,
        Executor executor);

    /**
     * Registers a {@link Listener} to receive callbacks for {@link Notification}s of a particular type.
     *
     * @implSpec
     *     This method is equivalent to {@code registerListener(type, listener, MoreExecutors.directExecutor())}, i.e.
     *     the listener will be invoked on some implementation-specific thread.
     *
     * @param <N> Notification type
     * @param type Notification type class
     * @param listener The listener implementation that will receive notifications
     * @return a {@link Registration} instance that should be used to unregister the listener by invoking the
     *        {@link Registration#close()} method when no longer needed
     */
    default <N extends Notification<N> & DataObject> @NonNull Registration registerListener(final Class<N> type,
            final Listener<N> listener) {
        return registerListener(type, listener, MoreExecutors.directExecutor());
    }

    /**
     * Registers a {@link Listener} to receive callbacks for {@link Notification}s of a particular type.
     *
     * @param listener Composite listener containing listener implementations that will receive notifications
     * @param executor Executor to use for invoking the listener's methods
     * @return a {@link Registration} instance that should be used to unregister the listener by invoking the
     *        {@link Registration#close()} method when no longer needed
     */
    @Beta
    @NonNull Registration registerCompositeListener(CompositeListener listener, Executor executor);

    /**
     * Registers a {@link Listener} to receive callbacks for {@link Notification}s of a particular type.
     *
     * @implSpec
     *     This method is equivalent to {@code registerCompositeListener(listener, MoreExecutors.directExecutor())},
     *     i.e. listeners will be invoked on some implementation-specific thread.
     *
     * @param listener Composite listener containing listener implementations that will receive notifications
     * @return a {@link Registration} instance that should be used to unregister the listener by invoking the
     *        {@link Registration#close()} method when no longer needed
     */
    @Beta
    default @NonNull Registration registerCompositeListener(final CompositeListener listener) {
        return registerCompositeListener(listener, MoreExecutors.directExecutor());
    }

    /**
     * Interface for listeners on global (YANG 1.0) notifications. Such notifications are identified by their generated
     * interface which extends {@link Notification}. Each listener instance can listen to only a single notification
     * type.
     *
     * @param <N> Notification type
     */
    @FunctionalInterface
    interface Listener<N extends Notification<N> & DataObject> {
        /**
         * Process a global notification.
         *
         * @param notification Notification body
         */
        void onNotification(@NonNull N notification);
    }

    /**
     * A composite listener. This class allows registering multiple {@link Listener}s in a single operation. Constituent
     * listeners are available through {@link #constituents()}.
     */
    @Beta
    record CompositeListener(@NonNull Set<CompositeListener.Component<?>> constituents) {
        @Beta
        public record Component<T extends Notification<T> & DataObject>(@NonNull Class<T> type, Listener<T> listener) {
            public Component {
                requireNonNull(type);
                requireNonNull(listener);
                checkArgument(DataObject.class.isAssignableFrom(type), "%s is not a DataObject", type);
                checkArgument(Notification.class.isAssignableFrom(type), "%s is not a Notification", type);
            }
        }

        public CompositeListener {
            requireNonNull(constituents);
            checkArgument(!constituents.isEmpty(), "Composite listener requires at least one constituent listener");
        }
    }
}
