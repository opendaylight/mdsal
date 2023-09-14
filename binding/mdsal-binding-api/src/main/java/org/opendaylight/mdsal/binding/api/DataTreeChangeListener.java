/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import java.util.Collection;
import java.util.EventListener;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Interface implemented by classes interested in receiving notifications about
 * data tree changes. It provides a cursor-based view of the change, which has potentially
 * lower overhead and allow more flexible consumption of change event.
 */
public interface DataTreeChangeListener<T extends DataObject> extends EventListener {
    /**
     * Invoked when there was data change for the supplied path, which was used
     * to register this listener.
     *
     * <p>
     * This method may be also invoked during registration of the listener if
     * there is any pre-existing data in the conceptual data tree for supplied
     * path. This initial event will contain all pre-existing data as created.
     *
     * <p>
     * Note: If there is no pre-existing data, the method {@link #onInitialData} will be invoked.
     *
     * <p>
     * A data change event may be triggered spuriously, e.g. such that data before
     * and after compare as equal. Implementations of this interface are expected
     * to recover from such events. Event producers are expected to exert reasonable
     * effort to suppress such events.
     *
     *<p>
     * In other words, it is completely acceptable to observe
     * a {@link DataObjectModification}, while the state observed before and
     * after- data items compare as equal.
     *
     * @param changes Collection of change events, may not be null or empty.
     */
    void onDataTreeChanged(@NonNull Collection<DataTreeModification<T>> changes);

    /**
     * Invoked only once during registration of the listener if there was no data in the conceptual data tree
     * for the supplied path, which was used to register this listener, and after this
     * {@link #onDataTreeChanged(Collection)} would always be invoked for data changes.
     *
     * <p>
     * Default implementation does nothing and is appropriate for users who do not care about ascertaining
     * initial state.
     */
    // FIXME: 8.0.0: this method should be non-default
    default void onInitialData() {
        //no-op
    }

    /**
     * Defines the listener notification mode within a clustered environment.
     *
     * <p>
     * <ul>
     * <li>{@link ClusterMode#LEADER_ONLY} - default value, means the listener will accept notification only if running
     * on a Leader (Master) node. Expected to be used if the listener itself produces data and/or events based on
     * incoming notifications. Single active instance within a cluster will prevent same (duplicate) data/events
     * generation on multiple nodes.</li>
     * <li>{@link ClusterMode#EVERY_NODE} - means the listener will accept notification on every node of a cluster.
     * Expected to be used when the listener serves local service(s) states (cache) synchronization within a cluster
     * based on incoming events.</li>
     * </ul>
     *
     * @return cluster mode value
     */
    default @NonNull ClusterMode clusterMode() {
        return ClusterMode.LEADER_ONLY;
    }

    /**
     * Listener notification modes enumeration for clustered environment case.
     */
    enum ClusterMode {
        /**
         * Notification is active on Leader (Master) node only.
         */
        LEADER_ONLY,
        /**
         * Notification is active on every node of a cluster.
         */
        EVERY_NODE
    }
}
