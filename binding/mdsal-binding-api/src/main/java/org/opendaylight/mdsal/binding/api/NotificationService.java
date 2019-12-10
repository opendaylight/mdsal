/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import java.util.EventListener;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceNotification;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.binding.NotificationListener;

/**
 * Notification broker which allows clients to subscribe for and publish YANG-modeled notifications.
 *
 *<p>
 * Each YANG module which defines notifications results in a generated interface
 * <code>{ModuleName}Listener</code> which handles all the notifications defined in the YANG model.
 * Each notification type translates to a specific method of the form
 * <code>on{NotificationType}</code> on the generated interface. The generated interface also
 * extends the {@link org.opendaylight.yangtools.yang.binding.NotificationListener} interface and
 * implementations are registered using
 * {@link #registerNotificationListener(org.opendaylight.yangtools.yang.binding.NotificationListener)}
 * method.
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
 * The <code>onStart</code> method will be invoked when someone publishes a <code>Start</code>
 * notification and the <code>onStop</code> method will be invoked when someone publishes a
 * <code>Stop</code> notification.
 */
public interface NotificationService extends BindingService {
    /**
     * Registers a listener which implements a YANG-generated notification interface derived from
     * {@link NotificationListener}. The listener is registered for all notifications present in
     * the implemented interface.
     *
     * <p>
     * @param listener the listener implementation that will receive notifications.
     * @return a {@link ListenerRegistration} instance that should be used to unregister the listener
     *         by invoking the {@link ListenerRegistration#close()} method when no longer needed.
     */
    <T extends NotificationListener> @NonNull ListenerRegistration<T> registerNotificationListener(@NonNull T listener);

    <N extends Notification, T extends Listener<N>> @NonNull ListenerRegistration<T> registerListener(Class<N> type,
            T listener);

    <P extends DataObject, N extends InstanceNotification<N, P>, T extends InstanceListener<P, N>>
        @NonNull ListenerRegistration<T> registerListener(Class<N> type, InstanceIdentifier<P> path, T listener);

    <P extends DataObject & Identifiable<K>, N extends InstanceNotification<N, P>, K extends Identifier<P>,
        T extends KeyedListListener<P, N, K>> @NonNull ListenerRegistration<T> registerListener(Class<N> type,
                KeyedInstanceIdentifier<P, K> path, T listener);

    @FunctionalInterface
    interface Listener<N extends Notification> extends EventListener {

        void onNotification(@NonNull N Notification);
    }

    @FunctionalInterface
    interface InstanceListener<P extends DataObject, N extends InstanceNotification<N, P>> extends EventListener {

        void onNotification(@NonNull InstanceIdentifier<P> path, @NonNull N notification);
    }

    @FunctionalInterface
    interface KeyedListListener<P extends DataObject & Identifiable<K>, N extends InstanceNotification<N, P>,
            K extends Identifier<P>> extends EventListener {

        void onNotification(@NonNull KeyedInstanceIdentifier<P, K> path, @NonNull N notification);
    }
}
