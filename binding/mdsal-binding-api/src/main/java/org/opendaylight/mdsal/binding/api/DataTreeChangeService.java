/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * A {@link BindingService} which allows users to register for changes to a subtree.
 */
public interface DataTreeChangeService extends BindingService {
    /**
     * Registers a {@link DataTreeChangeListener} to receive notifications when data changes under a given path in the
     * conceptual data tree.
     *
     * <p>
     * You are able to register for notifications  for any node or subtree which can be represented using
     * {@link DataTreeIdentifier}.
     *
     * <p>
     * You are able to register for data change notifications for a subtree or leaf even if it does not exist. You will
     * receive notification once that node is created.
     *
     * <p>
     * If there is any pre-existing data in the data tree for the path for which you are registering, you will receive
     * an initial data change event, which will contain all pre-existing data, marked as created.
     *
     * <p>
     * This method returns a {@link Registration} object. To "unregister" your listener for changes call the
     * {@link Registration#close()} method on the returned object.
     *
     * <p>
     * You <b>MUST</b> explicitly unregister your listener when you no longer want to receive notifications. This is
     * especially true in OSGi environments, where failure to do so during bundle shutdown can lead to stale listeners
     * being still registered.
     *
     * @param treeId Data tree identifier of the subtree which should be watched for changes
     * @param listener Listener instance which is being registered
     * @return a Registration object, which may be used to unregister your listener using {@link Registration#close()}
     *         to stop delivery of change events.
     */
    @Deprecated(since = "13.0.0", forRemoval = true)
    default <T extends DataObject> @NonNull Registration registerDataTreeChangeListener(
            final @NonNull DataTreeIdentifier<T> treeId, final @NonNull DataTreeChangeListener<T> listener) {
        return listener instanceof ClusteredDataTreeChangeListener ? registerTreeChangeListener(treeId, listener)
            : registerLegacyTreeChangeListener(treeId, listener);
    }

    @Deprecated(since = "13.0.0", forRemoval = true)
    default <T extends DataObject> @NonNull Registration registerDataTreeChangeListener(
            final @NonNull DataTreeIdentifier<T> treeId, final @NonNull ClusteredDataTreeChangeListener<T> listener) {
        return registerTreeChangeListener(treeId, listener);
    }

    <T extends DataObject> @NonNull Registration registerTreeChangeListener(
        @NonNull DataTreeIdentifier<T> treeId, @NonNull DataTreeChangeListener<T> listener);

    @Deprecated(since = "13.0.0", forRemoval = true)
    <T extends DataObject> @NonNull Registration registerLegacyTreeChangeListener(
        @NonNull DataTreeIdentifier<T> treeId, @NonNull DataTreeChangeListener<T> listener);

    /**
     * Registers a {@link DataTreeChangeListener} to receive notifications when data changes under a given path in the
     * conceptual data tree.
     *
     * <p>
     * You are able to register for notifications  for any node or subtree which can be represented using
     * {@link DataTreeIdentifier}.
     *
     * <p>
     * This method returns a {@link Registration} object. To "unregister" your listener for changes call the
     * {@link Registration#close()} method on the returned object.
     *
     * <p>
     * You <b>MUST</b> explicitly unregister your listener when you no longer want to receive notifications. This is
     * especially true in OSGi environments, where failure to do so during bundle shutdown can lead to stale listeners
     * being still registered.
     *
     * @implSpec This method provides {@link DataListenerAdapter} as listener during the registration of
     *           {@link DataTreeChangeListener}. This would allow users to know the last state of data instead of
     *           getting details about what changed in the entire tree.
     *
     * @param treeId Data tree identifier of the subtree which should be watched for changes.
     * @param listener Listener instance which is being registered
     * @return Listener registration object, which may be used to unregister your listener using
     *         {@link Registration#close()} to stop delivery of change events.
     */
    default <T extends DataObject> @NonNull Registration registerDataListener(
            final @NonNull DataTreeIdentifier<T> treeId, final @NonNull DataListener<T> listener) {
        return registerTreeChangeListener(checkNotWildcard(treeId), new DataListenerAdapter<>(listener));
    }

    /**
     * Registers a {@link DataTreeChangeListener} to receive notifications about the last data state when it changes
     * under a given path in the conceptual data tree.
     *
     * <p>
     * You are able to register for notifications  for any node or subtree which can be represented using
     * {@link DataTreeIdentifier}.
     *
     * <p>
     * This method returns a {@link Registration} object. To "unregister" your listener for changes call the
     * {@link Registration#close()} method on the returned object.
     *
     * <p>
     * You <b>MUST</b> explicitly unregister your listener when you no longer want to receive notifications. This is
     * especially true in OSGi environments, where failure to do so during bundle shutdown can lead to stale listeners
     * being still registered.
     *
     * @implSpec This method provides {@link DataChangeListenerAdapter} as listener during the registration of
     *           {@link DataTreeChangeListener}, which provides a comparison of before-value and after-value.
     *
     * @param treeId Data tree identifier of the subtree which should be watched for changes.
     * @param listener Listener instance which is being registered
     * @return Listener registration object, which may be used to unregister your listener using
     *         {@link Registration#close()} to stop  delivery of change events.
     */
    default <T extends DataObject> @NonNull Registration registerDataChangeListener(
            final @NonNull DataTreeIdentifier<T> treeId, final @NonNull DataChangeListener<T> listener) {
        return registerTreeChangeListener(checkNotWildcard(treeId), new DataChangeListenerAdapter<>(listener));
    }

    private static <T extends DataObject> @NonNull DataTreeIdentifier<T> checkNotWildcard(
            final DataTreeIdentifier<T> treeId) {
        final var instanceIdentifier = treeId.path();
        if (instanceIdentifier.isWildcarded()) {
            throw new IllegalArgumentException("Cannot register listener for wildcard " + instanceIdentifier);
        }
        return treeId;
    }
}