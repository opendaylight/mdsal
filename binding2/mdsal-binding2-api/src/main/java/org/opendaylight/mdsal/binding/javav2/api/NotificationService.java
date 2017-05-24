/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.api;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.NotificationListener;
import org.opendaylight.yangtools.concepts.ListenerRegistration;

/**
 * Notification broker which allows clients to subscribe for and publish YANG-modeled notifications.
 *
 *<p>
 * Each YANG module which defines notifications results in a generated interface
 * <code>{ModuleName}Listener</code> which handles all the notifications defined in the YANG model.
 * Each notification type translates to a specific method of the form
 * <code>on{NotificationType}</code> on the generated interface. The generated interface also
 * extends the {@link org.opendaylight.mdsal.binding.javav2.spec.runtime.NotificationListener} interface and
 * implementations are registered using
 * {@link #registerNotificationListener(org.opendaylight.mdsal.binding.javav2.spec.runtime.NotificationListener)}
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
 *
 * <p>
 * YANG 1.1: in case of notification tied to data note (container, list):
 *
 * <pre>
 * module example {
 *      ...
 *
 *      container cont {
 *          notification notify {
 *              ...
 *          }
 *      ...
 *      }
 * }
 * </pre>
 *
 * <p>
 * The generated interface will be:
 *
 * <pre>
 * public interface ExampleListener extends NotificationListener {
 *     void onContNotify(Notify notification);
 *
 * }
 * </pre>
 */
@Beta
public interface NotificationService extends BindingService {

    /**
     * Registers a listener which implements a YANG-generated notification interface derived from
     * {@link NotificationListener}. The listener is registered for all notifications present in
     * the implemented interface.
     *
     * <p>
     * @param listener the listener implementation that will receive notifications.
     * @param <T> listener type
     * @return a {@link ListenerRegistration} instance that should be used to unregister the listener
     *         by invoking the {@link ListenerRegistration#close()} method when no longer needed.
     */
    <T extends NotificationListener> ListenerRegistration<T> registerNotificationListener(T listener);
}
