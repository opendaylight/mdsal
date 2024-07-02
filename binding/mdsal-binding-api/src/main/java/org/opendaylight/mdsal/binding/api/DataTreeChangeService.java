/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.concepts.Registration;

/**
 * A {@link BindingService} which allows users to register for changes to a subtree.
 */
public interface DataTreeChangeService extends BindingService {
    /**
     * Registers a {@link DataTreeChangeListener} to receive notifications when data changes under a given path in the
     * conceptual data tree.
     *
     * <p>
     * You are able to register for notifications for any node or subtree which can be represented
     * {@link DataObjectReference} on a particular {@link LogicalDatastoreType}, irrespective of its existence. You will
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
     * You <b>MUST</b> explicitly unregister your listener when you no longer wish to receive notifications. This is
     * especially true in OSGi environments, where failure to do so during bundle shutdown can lead to stale listeners
     * being still registered.
     *
     * @param datastore a {@link LogicalDatastoreType}
     * @param subtrees {@link DataObjectReference} matching subtree which should be watched for changes
     * @param listener Listener instance which is being registered
     * @return a Registration object, which may be used to unregister your listener using {@link Registration#close()}
     *         to stop delivery of change events.
     */
    <T extends DataObject> @NonNull Registration registerTreeChangeListener(@NonNull LogicalDatastoreType datastore,
        @NonNull DataObjectReference<T> subtrees, @NonNull DataTreeChangeListener<T> listener);

    /**
     * Registers a {@link DataTreeChangeListener} to receive notifications when data changes under a given path in the
     * conceptual data tree.
     *
     * <p>
     * You are able to register for notifications  for any node or subtree which can be represented using
     * {@link DataTreeMatch}.
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
     * @deprecated Use
     *      {@link #registerTreeChangeListener(LogicalDatastoreType, DataObjectReference, DataTreeChangeListener)}
     *      instead.
     */
    @Deprecated(since = "14.0.0", forRemoval = true)
    default <T extends DataObject> @NonNull Registration registerTreeChangeListener(
            final @NonNull DataTreeMatch<T> treeId, final @NonNull DataTreeChangeListener<T> listener) {
        return registerTreeChangeListener(treeId.datastore(), treeId.getRootIdentifier().toReference(), listener);
    }

    /**
     * Registers a {@link DataTreeChangeListener} to receive notifications when data changes under a given path in the
     * conceptual data tree, with legacy semantics, where no events are delivered if this "cluster node" (further
     * undefined) is a "leader" (also not explicitly undefined).
     *
     * <p>
     * The sole known implementation, the Akka-based datastore, defines the difference in terms of RAFT, suspending
     * even delivery when the RAFT leader is not local. Even when there may be valid use cases for this, RAFT there
     * is a storage backend whose lifecycle is disconnected from this object.
     *
     * <p>
     * Aside from the above difference, this method is equivalent to
     * {@link #registerTreeChangeListener(LogicalDatastoreType, DataObjectReference, DataTreeChangeListener)}. If you
     * are unable to migrate, please contact us on <a href="email:discuss@lists.opendaylight.org">the mailing list</a>,
     *
     * @param datastore a {@link LogicalDatastoreType}
     * @param subtrees {@link DataObjectReference} matching subtree which should be watched for changes
     * @param listener Listener instance which is being registered
     * @return A {@link Registration} object, which may be used to unregister your listener using
     *         {@link Registration#close()} to stop delivery of change events.
     * @throws NullPointerException if any of the arguments is {@code null}
     */
    @Deprecated(since = "13.0.0", forRemoval = true)
    <T extends DataObject> @NonNull Registration registerLegacyTreeChangeListener(
        @NonNull LogicalDatastoreType datastore, @NonNull DataObjectReference<T> subtrees,
        @NonNull DataTreeChangeListener<T> listener);

    /**
     * Registers a {@link DataTreeChangeListener} to receive notifications when data changes under a given path in the
     * conceptual data tree, with legacy semantics, where no events are delivered if this "cluster node" (further
     * undefined) is a "leader" (also not explicitly undefined).
     *
     * <p>
     * The sole known implementation, the Akka-based datastore, defines the difference in terms of RAFT, suspending
     * even delivery when the RAFT leader is not local. Even when there may be valid use cases for this, RAFT there
     * is a storage backend whose lifecycle is disconnected from this object.
     *
     * <p>
     * Aside from the above difference, this method is equivalent to
     * {@link #registerTreeChangeListener(DataTreeMatch, DataTreeChangeListener)}. If you are unable to migrate,
     * please contact us on <a href="email:discuss@lists.opendaylight.org">the mailing list</a>
     *
     * @param treeId Data tree identifier of the subtree which should be watched for changes.
     * @param listener Listener instance which is being registered
     * @return A {@link Registration} object, which may be used to unregister your listener using
     *         {@link Registration#close()} to stop delivery of change events.
     * @throws NullPointerException if any of the arguments is {@code null}
     * @deprecated Use
     *      {@link #registerLegacyTreeChangeListener(LogicalDatastoreType, DataObjectReference, DataTreeChangeListener)}
     *             instead.
     */
    @Deprecated(since = "14.0.0", forRemoval = true)
    default <T extends DataObject> @NonNull Registration registerLegacyTreeChangeListener(
            final @NonNull DataTreeMatch<T> treeId, final @NonNull DataTreeChangeListener<T> listener) {
        return registerLegacyTreeChangeListener(treeId.datastore(), treeId.path().toReference(), listener);
    }

    /**
     * Registers a {@link DataTreeChangeListener} to receive notifications when data changes under a given path in the
     * conceptual data tree.
     *
     * <p>
     * You are able to register for notifications  for any node or subtree which can be represented using
     * {@link DataTreeMatch}.
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
     * @deprecated This interface relies on magic of {@link ClusteredDataTreeChangeListener}. See
     *             {@link #registerLegacyTreeChangeListener(DataTreeMatch, DataTreeChangeListener)} for migration
     *             guidance.
     */
    @Deprecated(since = "13.0.0", forRemoval = true)
    default <T extends DataObject> @NonNull Registration registerDataTreeChangeListener(
            final @NonNull DataTreeMatch<T> treeId, final @NonNull DataTreeChangeListener<T> listener) {
        return listener instanceof ClusteredDataTreeChangeListener ? registerTreeChangeListener(treeId, listener)
            : registerLegacyTreeChangeListener(treeId, listener);
    }

    /**
     * Registers a {@link ClusteredDataTreeChangeListener} to receive notifications when data changes under a given path
     * in the conceptual data tree. This is a migration shorthand for
     * {@code registerDataTreeListener(treeId, listener)}.
     *
     * @param treeId Data tree identifier of the subtree which should be watched for changes.
     * @param listener Listener instance which is being registered
     * @return A {@link Registration} object, which may be used to unregister your listener using
     *         {@link Registration#close()} to stop delivery of change events.
     * @throws NullPointerException if any of the arguments is {@code null}
     * @deprecated Use {@link #registerTreeChangeListener(DataTreeMatch, DataTreeChangeListener)} instead.
     */
    @Deprecated(since = "13.0.0", forRemoval = true)
    default <T extends DataObject> @NonNull Registration registerDataTreeChangeListener(
            final @NonNull DataTreeMatch<T> treeId, final @NonNull ClusteredDataTreeChangeListener<T> listener) {
        return registerTreeChangeListener(treeId, listener);
    }

    /**
     * Registers a {@link DataTreeChangeListener} to receive notifications when data changes under a given path in the
     * conceptual data tree.
     *
     * <p>
     * You are able to register for notifications  for any node or subtree which can be represented using
     * {@link DataTreeMatch}.
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
     * @param datastore a {@link LogicalDatastoreType}
     * @param path {@link DataObjectIdentifier} which should be watched for changes
     * @param listener Listener instance which is being registered
     * @return Listener registration object, which may be used to unregister your listener using
     *         {@link Registration#close()} to stop delivery of change events.
     */
    default <T extends DataObject> @NonNull Registration registerDataListener(
            final @NonNull LogicalDatastoreType datastore, final @NonNull DataObjectIdentifier<T> path,
            final @NonNull DataListener<T> listener) {
        return registerTreeChangeListener(datastore, path, new DataListenerAdapter<>(listener));
    }

    /**
     * Registers a {@link DataTreeChangeListener} to receive notifications when data changes under a given path in the
     * conceptual data tree.
     *
     * <p>
     * You are able to register for notifications  for any node or subtree which can be represented using
     * {@link DataTreeMatch}.
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
    @Deprecated(since = "14.0.0", forRemoval = true)
    default <T extends DataObject> @NonNull Registration registerDataListener(
            final @NonNull DataTreeMatch<T> treeId, final @NonNull DataListener<T> listener) {
        return registerDataListener(treeId.datastore(), LegacyUtils.legacyToIdentifier(treeId.path()), listener);
    }

    /**
     * Registers a {@link DataTreeChangeListener} to receive notifications about the last data state when it changes
     * under a given path in the conceptual data tree.
     *
     * <p>
     * You are able to register for notifications  for any node or subtree which can be represented using
     * {@link DataTreeMatch}.
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
     * @param datastore a {@link LogicalDatastoreType}
     * @param path {@link DataObjectIdentifier} which should be watched for changes
     * @param listener Listener instance which is being registered
     * @return Listener registration object, which may be used to unregister your listener using
     *         {@link Registration#close()} to stop  delivery of change events.
     */
    default <T extends DataObject> @NonNull Registration registerDataChangeListener(
            final @NonNull LogicalDatastoreType datastore, final @NonNull DataObjectIdentifier<T> path,
            final @NonNull DataChangeListener<T> listener) {
        return registerTreeChangeListener(datastore, path, new DataChangeListenerAdapter<>(listener));
    }

    /**
     * Registers a {@link DataTreeChangeListener} to receive notifications about the last data state when it changes
     * under a given path in the conceptual data tree.
     *
     * <p>
     * You are able to register for notifications  for any node or subtree which can be represented using
     * {@link DataTreeMatch}.
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
     * @deprecated Use
     *             {@link #registerDataChangeListener(LogicalDatastoreType, DataObjectIdentifier, DataChangeListener)}
     *             instead.
     */
    @Deprecated(since = "14.0.0", forRemoval = true)
    default <T extends DataObject> @NonNull Registration registerDataChangeListener(
            final @NonNull DataTreeMatch<T> treeId, final @NonNull DataChangeListener<T> listener) {
        return registerTreeChangeListener(treeId.datastore(), LegacyUtils.legacyToIdentifier(treeId.path()),
            new DataChangeListenerAdapter<>(listener));
    }
}