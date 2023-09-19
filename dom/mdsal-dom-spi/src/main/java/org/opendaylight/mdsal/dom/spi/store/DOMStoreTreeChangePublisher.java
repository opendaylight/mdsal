/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.store;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * Interface implemented by DOMStore implementations which allow registration
 * of {@link DOMDataTreeChangeListener} instances.
 */
public interface DOMStoreTreeChangePublisher {
    /**
     * Registers a {@link DOMDataTreeChangeListener} to receive notifications
     * when data changes under a given path in the conceptual data tree.
     *
     * <p>
     * You are able to register for notifications  for any node or subtree
     * which can be represented using {@link YangInstanceIdentifier}.
     *
     * <p>
     * You are able to register for data change notifications for a subtree or leaf
     * even if it does not exist. You will receive notification once that node is created.
     *
     * <p>
     * If there is any pre-existing data in data tree on path for which you are
     * registering, you will receive initial data change event, which will
     * contain all pre-existing data, marked as created. If the data at the supplied
     * path does not exist, you will also receive initial data change event, which will
     * contain empty data tree modification, marked as unmodified.
     *
     * <p>
     * By default, every listener instance is acceptable for change notifications when running in clustered environment.
     * Use {@link #registerTreeChangeListener(YangInstanceIdentifier, DOMDataTreeChangeListener, boolean)}
     * to set acceptance policy explicitly.
     *
     * <p>
     * This method returns a {@link ListenerRegistration} object. To "unregister" the listener
     * call the {@link ListenerRegistration#close()} method on this returned object.
     *
     * <p>
     * You MUST explicitly unregister your listener when you no longer want to receive
     * notifications. This is especially true in OSGi environments, where failure to
     * do so during bundle shutdown can lead to stale listeners being still registered.
     *
     * @param treeId Data tree identifier of the subtree which should be watched for changes.
     * @param listener Listener instance which is being registered
     * @return Listener registration object.
     */
    <L extends DOMDataTreeChangeListener> @NonNull ListenerRegistration<L> registerTreeChangeListener(
            @NonNull YangInstanceIdentifier treeId, @NonNull L listener);

    /**
     * Registers a {@link DOMDataTreeChangeListener} to receive notifications
     * when data changes under a given path in the conceptual data tree.
     *
     * <p>
     * You are able to register for notifications  for any node or subtree
     * which can be represented using {@link YangInstanceIdentifier}.
     *
     * <p>
     * You are able to register for data change notifications for a subtree or leaf
     * even if it does not exist. You will receive notification once that node is created.
     *
     * <p>
     * If there is any pre-existing data in data tree on path for which you are
     * registering, you will receive initial data change event, which will
     * contain all pre-existing data, marked as created. If the data at the supplied
     * path does not exist, you will also receive initial data change event, which will
     * contain empty data tree modification, marked as unmodified.
     *
     * <p>
     * This method returns a {@link ListenerRegistration} object. To "unregister" the listener
     * call the {@link ListenerRegistration#close()} method on this returned object.
     *
     * <p>
     * You MUST explicitly unregister your listener when you no longer want to receive
     * notifications. This is especially true in OSGi environments, where failure to
     * do so during bundle shutdown can lead to stale listeners being still registered.
     *
     * @param treeId Data tree identifier of the subtree which should be watched for changes.
     * @param listener Listener instance which is being registered
     * @param masterNodeOnly Flag indicating listener acceptance policy in clustered environment.
     *      If value is "true" then the listener will accept change notifications only on a master node,
     *      same listener instances on other nodes won't be notified. If value is "false" then every
     *      listener instance will be notified.
     * @return Listener registration object.
     */
    default <L extends DOMDataTreeChangeListener> @NonNull ListenerRegistration<L> registerTreeChangeListener(
            final @NonNull YangInstanceIdentifier treeId, final @NonNull L listener, final boolean masterNodeOnly) {
        return registerTreeChangeListener(treeId, listener);
    }
}
