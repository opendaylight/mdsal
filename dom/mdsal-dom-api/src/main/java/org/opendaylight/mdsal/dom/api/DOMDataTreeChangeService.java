/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.ListenerRegistration;

/**
 * A {@link DOMServiceExtension} which allows users to register for changes to a
 * subtree.
 */
public interface DOMDataTreeChangeService extends DOMDataBrokerExtension {
    /**
     * Registers a {@link DOMDataTreeChangeListener} to receive notifications when data changes
     * under a given path in the conceptual data tree.
     *
     * <p>
     * You are able to register for notifications for any node or subtree which can be represented
     * using {@link DOMDataTreeIdentifier}.
     *
     * <p>
     * You are able to register for data change notifications for a subtree or leaf even if it does
     * not exist. You will receive notification once that node is created.
     *
     * <p>
     * If there is any pre-existing data in the data tree for the path for which you are
     * registering, you will receive an initial data change event, which will contain all
     * pre-existing data, marked as created.
     *
     * <p>
     * By default, every listener instance is acceptable for change notifications when running in clustered environment.
     * Use {@link #registerDataTreeChangeListener(DOMDataTreeIdentifier, DOMDataTreeChangeListener, boolean)}
     * to set acceptance policy explicitly.
     *
     * <p>
     * This method returns a {@link ListenerRegistration} object. To "unregister" your listener for
     * changes call the {@link ListenerRegistration#close()} method on the returned object.
     *
     * <p>
     * You MUST explicitly unregister your listener when you no longer want to receive
     * notifications. This is especially true in OSGi environments, where failure to do so during
     * bundle shutdown can lead to stale listeners being still registered.
     *
     * @param treeId Data tree identifier of the subtree which should be watched for changes.
     * @param listener Listener instance which is being registered
     * @param <L> Listener type
     * @return Listener registration object, which may be used to unregister your listener using
     *         {@link ListenerRegistration#close()} to stop delivery of change events.
     * @throws NullPointerException if any of the arguments is null
     */
    <L extends DOMDataTreeChangeListener> @NonNull ListenerRegistration<L> registerDataTreeChangeListener(
            @NonNull DOMDataTreeIdentifier treeId, @NonNull L listener);

    /**
     * Registers a {@link DOMDataTreeChangeListener} to receive notifications when data changes
     * under a given path in the conceptual data tree.
     *
     * <p>
     * You are able to register for notifications for any node or subtree which can be represented
     * using {@link DOMDataTreeIdentifier}.
     *
     * <p>
     * You are able to register for data change notifications for a subtree or leaf even if it does
     * not exist. You will receive notification once that node is created.
     *
     * <p>
     * If there is any pre-existing data in the data tree for the path for which you are
     * registering, you will receive an initial data change event, which will contain all
     * pre-existing data, marked as created.
     *
     * <p>
     * This method returns a {@link ListenerRegistration} object. To "unregister" your listener for
     * changes call the {@link ListenerRegistration#close()} method on the returned object.
     *
     * <p>
     * You MUST explicitly unregister your listener when you no longer want to receive
     * notifications. This is especially true in OSGi environments, where failure to do so during
     * bundle shutdown can lead to stale listeners being still registered.
     *
     * @param treeId Data tree identifier of the subtree which should be watched for changes.
     * @param listener Listener instance which is being registered
     * @param <L> Listener type
     * @param masterNodeOnly Flag indicating listener acceptance policy in clustered environment.
     *      If value is "true" then the listener will accept change notifications only on a master node,
     *      same listener instances on other nodes won't be notified. If value is "false" then every
     *      listener instance will be notified.
     * @return Listener registration object
     * @throws NullPointerException if any of the arguments is null
     */
    default <L extends DOMDataTreeChangeListener> @NonNull ListenerRegistration<L> registerDataTreeChangeListener(
            final @NonNull DOMDataTreeIdentifier treeId, final @NonNull L listener, final boolean masterNodeOnly) {
        return registerDataTreeChangeListener(treeId, listener);
    }
}
