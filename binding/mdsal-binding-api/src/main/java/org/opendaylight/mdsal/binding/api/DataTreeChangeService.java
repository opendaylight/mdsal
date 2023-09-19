/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * A {@link BindingService} which allows users to register for changes to a subtree.
 */
public interface DataTreeChangeService extends BindingService {
    /**
     * Registers a {@link DataTreeChangeListener} to receive notifications
     * when data changes under a given path in the conceptual data tree.
     *
     * <p>
     * You are able to register for notifications  for any node or subtree
     * which can be represented using {@link DataTreeIdentifier}.
     *
     * <p>
     * You are able to register for data change notifications for a subtree or leaf
     * even if it does not exist. You will receive notification once that node is created.
     *
     * <p>
     * If there is any pre-existing data in the data tree for the path for which you are
     * registering, you will receive an initial data change event, which will
     * contain all pre-existing data, marked as created.
     *
     * <p>
     * By default, every listener instance is acceptable for change notifications when running in clustered
     * environment. Use {@link #registerDataTreeChangeListener(DataTreeIdentifier, DataTreeChangeListener, boolean)}
     * to set acceptance policy explicitly.
     *
     * <p>
     * This method returns a {@link ListenerRegistration} object. To "unregister" your listener
     * call the {@link ListenerRegistration#close()} method on the returned object.
     *
     * <p>
     * You MUST explicitly unregister your listener when you no longer want to receive
     * notifications. This is especially true in OSGi environments, where failure to
     * do so during bundle shutdown can lead to stale listeners being still registered.
     *
     * @param treeId Data tree identifier of the subtree which should be watched for changes.
     * @param listener Listener instance which is being registered
     * @return Listener registration object
     */
    <T extends DataObject, L extends DataTreeChangeListener<T>> @NonNull ListenerRegistration<L>
            registerDataTreeChangeListener(@NonNull DataTreeIdentifier<T> treeId, @NonNull L listener);

    /**
     * Registers a {@link DataTreeChangeListener} to receive notifications
     * when data changes under a given path in the conceptual data tree.
     *
     * <p>
     * You are able to register for notifications  for any node or subtree
     * which can be represented using {@link DataTreeIdentifier}.
     *
     * <p>
     * You are able to register for data change notifications for a subtree or leaf
     * even if it does not exist. You will receive notification once that node is created.
     *
     * <p>
     * If there is any pre-existing data in the data tree for the path for which you are
     * registering, you will receive an initial data change event, which will
     * contain all pre-existing data, marked as created.
     *
     * <p>
     * By default, every listener instance is acceptable for change notifications when running in clustered
     * environment. Use {@link #registerDataTreeChangeListener(DataTreeIdentifier, DataTreeChangeListener, boolean)}
     * to set acceptance policy explicitly.
     *
     * <p>
     * This method returns a {@link ListenerRegistration} object. To "unregister" your listener
     * call the {@link ListenerRegistration#close()} method on the returned object.
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
     * @return Listener registration object
     */
    default <T extends DataObject, L extends DataTreeChangeListener<T>> @NonNull ListenerRegistration<L>
        registerDataTreeChangeListener(final @NonNull DataTreeIdentifier<T> treeId, final @NonNull L listener,
            final boolean masterNodeOnly) {
        // needs override only for cluster implementation
        return registerDataTreeChangeListener(treeId, listener);
    }

    /**
     * Registers a {@link DataListener} to receive notifications
     * when data changes under a given path in the conceptual data tree.
     *
     * <p>
     * You are able to register for notifications  for any node or subtree
     * which can be represented using {@link DataTreeIdentifier}.
     *
     * <p>
     * This method returns a {@link Registration} object. To "unregister" your listener
     * call the {@link Registration#close()} method on the returned object.
     *
     * <p>
     * You MUST explicitly unregister your listener when you no longer want to receive
     * notifications. This is especially true in OSGi environments, where failure to
     * do so during bundle shutdown can lead to stale listeners being still registered.
     *
     * @implSpec This method provides {@link DataListenerAdapter} as listener during
     *      the registration of {@link DataTreeChangeListener}. This would allow users
     *      to know the last state of data instead of getting details about what changed
     *      in the entire tree.
     * @param treeId Data tree identifier of the subtree which should be watched for changes.
     * @param listener Listener instance which is being registered
     * @return Listener registration object
     */
    default <T extends DataObject> @NonNull Registration registerDataListener(
            final @NonNull DataTreeIdentifier<T> treeId, final @NonNull DataListener<T> listener) {
        return registerDataTreeChangeListener(checkNotWildcard(treeId), new DataListenerAdapter<>(listener));
    }

    /**
     * Registers a {@link DataListener} to receive notifications
     * when data changes under a given path in the conceptual data tree.
     *
     * <p>
     * You are able to register for notifications  for any node or subtree
     * which can be represented using {@link DataTreeIdentifier}.
     *
     * <p>
     * This method returns a {@link Registration} object. To "unregister" your listener
     * call the {@link Registration#close()} method on the returned object.
     *
     * <p>
     * You MUST explicitly unregister your listener when you no longer want to receive
     * notifications. This is especially true in OSGi environments, where failure to
     * do so during bundle shutdown can lead to stale listeners being still registered.
     *
     * @implSpec This method provides {@link DataListenerAdapter} as listener during
     *      the registration of {@link DataTreeChangeListener}. This would allow users
     *      to know the last state of data instead of getting details about what changed
     *      in the entire tree.
     * @param treeId Data tree identifier of the subtree which should be watched for changes.
     * @param listener Listener instance which is being registered
     * @param masterNodeOnly Flag indicating listener acceptance policy in clustered environment.
     *      If value is "true" then the listener will accept change notifications only on a master node,
     *      same listener instances on other nodes won't be notified. If value is "false" then every
     *      listener instance will be notified.
     * @return Listener registration object
     */
    default <T extends DataObject> @NonNull Registration registerDataListener(
            final @NonNull DataTreeIdentifier<T> treeId, final @NonNull DataListener<T> listener,
            final boolean masterNodeOnly) {
        return registerDataTreeChangeListener(checkNotWildcard(treeId), new DataListenerAdapter<>(listener),
            masterNodeOnly);
    }

    /**
     * Registers a {@link DataChangeListener} to receive notifications about the last data state
     * when it changes under a given path in the conceptual data  tree.
     *
     * <p>
     * You are able to register for notifications  for any node or subtree
     * which can be represented using {@link DataTreeIdentifier}.
     *
     * <p>
     * This method returns a {@link Registration} object. To "unregister" your listener
     * call the {@link Registration#close()} method on the returned object.
     *
     * <p>
     * You MUST explicitly unregister your listener when you no longer want to receive
     * notifications. This is especially true in OSGi environments, where failure to
     * do so during bundle shutdown can lead to stale listeners being still registered.
     *
     * @implSpec This method provides {@link DataChangeListenerAdapter} as listener during
     *      the registration of {@link DataTreeChangeListener}, which provides a comparison
     *      of before-value and after-value.
     * @param treeId Data tree identifier of the subtree which should be watched for changes.
     * @param listener Listener instance which is being registered
     * @return Listener registration object
     */
    default <T extends DataObject> @NonNull Registration registerDataChangeListener(
            final @NonNull DataTreeIdentifier<T> treeId, final @NonNull DataChangeListener<T> listener) {
        return registerDataTreeChangeListener(checkNotWildcard(treeId), new DataChangeListenerAdapter<>(listener));
    }

    /**
     * Registers a {@link DataChangeListener} to receive notifications about the last data state
     * when it changes under a given path in the conceptual data  tree.
     *
     * <p>
     * You are able to register for notifications  for any node or subtree
     * which can be represented using {@link DataTreeIdentifier}.
     *
     * <p>
     * This method returns a {@link Registration} object. To "unregister" your listener
     * call the {@link Registration#close()} method on the returned object.
     *
     * <p>
     * You MUST explicitly unregister your listener when you no longer want to receive
     * notifications. This is especially true in OSGi environments, where failure to
     * do so during bundle shutdown can lead to stale listeners being still registered.
     *
     * @implSpec This method provides {@link DataChangeListenerAdapter} as listener during
     *      the registration of {@link DataTreeChangeListener}, which provides a comparison
     *      of before-value and after-value.
     * @param treeId Data tree identifier of the subtree which should be watched for changes.
     * @param listener Listener instance which is being registered
     * @param masterNodeOnly Flag indicating listener acceptance policy in clustered environment.
     *      If value is "true" then the listener will accept change notifications only on a master node,
     *      same listener instances on other nodes won't be notified. If value is "false" then every
     *      listener instance will be notified.
     * @return Listener registration object
     */
    default <T extends DataObject> @NonNull Registration registerDataChangeListener(
            final @NonNull DataTreeIdentifier<T> treeId, final @NonNull DataChangeListener<T> listener,
            final boolean masterNodeOnly) {
        return registerDataTreeChangeListener(checkNotWildcard(treeId), new DataChangeListenerAdapter<>(listener),
            masterNodeOnly);
    }

    private static <T extends DataObject> @NonNull DataTreeIdentifier<T> checkNotWildcard(
            final DataTreeIdentifier<T> treeId) {
        final @NonNull InstanceIdentifier<T> instanceIdentifier = treeId.getRootIdentifier();
        if (instanceIdentifier.isWildcarded()) {
            throw new IllegalArgumentException("Cannot register listener for wildcard " + instanceIdentifier);
        }
        return treeId;
    }
}