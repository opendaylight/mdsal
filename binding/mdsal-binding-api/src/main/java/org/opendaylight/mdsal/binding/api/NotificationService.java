/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import com.google.common.util.concurrent.MoreExecutors;
import java.util.EventListener;
import java.util.concurrent.Executor;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.binding.NotificationListener;

/**
 * Notification broker which allows clients to subscribe for and publish YANG-modeled notifications.
 */
public interface NotificationService extends BindingService {
    /**
     * Registers a listener which implements a YANG-generated notification interface derived from
     * {@link NotificationListener}. The listener is registered for all notifications present in the implemented
     * interface.
     *
     * <p>
     * Each YANG module which defines notifications results in a generated interface <code>{ModuleName}Listener</code>
     * which handles all the notifications defined in the YANG model. Each notification type translates to a specific
     * method of the form <code>on{NotificationType}</code> on the generated interface. The generated interface also
     * extends the {@link org.opendaylight.yangtools.yang.binding.NotificationListener} interface and implementations
     * are registered using this method.
     *
     * <b>Dispatch Listener Example</b>
     *
     * <p>
     * Lets assume we have following YANG model:
     *
     * <pre>
     * module example {
     *      ...
     *
     *      notification start {
     *          ...
     *      }
     *
     *      notification stop {
     *           ...
     *      }
     * }
     * </pre>
     *
     * <p>
     * The generated interface will be:
     *
     * <pre>
     * public interface ExampleListener extends NotificationListener {
     *     void onStart(Start notification);
     *
     *     void onStop(Stop notification);
     * }
     * </pre>
     *
     * <p>
     * The following defines an implementation of the generated interface:
     *
     * <pre>
     * public class MyExampleListener implements ExampleListener {
     *     public void onStart(Start notification) {
     *         // do something
     *     }
     *
     *     public void onStop(Stop notification) {
     *         // do something
     *     }
     * }
     * </pre>
     *
     * <p>
     * The implementation is registered as follows:
     *
     * <pre>
     * MyExampleListener listener = new MyExampleListener();
     * ListenerRegistration&lt;NotificationListener&gt; reg = service.registerNotificationListener(listener);
     * </pre>
     *
     * <p>
     * The {@code onStart} method will be invoked when someone publishes a {@code Start} notification and the
     * {@code onStop} method will be invoked when someone publishes a {@code Stop} notification.
     *
     * @param <T> NotificationListener type
     * @param listener the listener implementation that will receive notifications.
     * @return a {@link ListenerRegistration} instance that should be used to unregister the listener
     *         by invoking the {@link ListenerRegistration#close()} method when no longer needed.
     * @deprecated Prefer {@link #registerListener(Class, Listener)} or
     *             {@link #registerListener(Class, Listener, Executor)} instead.
     */
    @Deprecated(since = "10.0.0", forRemoval = true)
    <T extends NotificationListener> @NonNull ListenerRegistration<T> registerNotificationListener(@NonNull T listener);

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
     * Interface for listeners on global (YANG 1.0) notifications. Such notifications are identified by their generated
     * interface which extends {@link Notification}. Each listener instance can listen to only a single notification
     * type.
     *
     * @param <N> Notification type
     */
    @FunctionalInterface
    interface Listener<N extends Notification<N> & DataObject> extends EventListener {
        /**
         * Process a global notification.
         *
         * @param notification Notification body
         */
        void onNotification(@NonNull N notification);
    }
}
